package orbotix.drive;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import orbotix.robot.util.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A widget that allows control of a Sphero™ using an RC car like interface.
 *
 * @author Orbotix Inc.
 */
public class RcControlView extends View {

    private static final long UPDATE_INTERVAL = 150; // millis
    private static final int CONTROL_EDGE_PADDING = 10; // pixels
    private int controlEdgePadding; // pixels
    private static final int BOOST_X_SPACING = 12;
    private int boostXSpacing;
    private static final int BOOST_Y_SPACING = 32;
    private int boostYSpacing;
    private static final int BOOST_QUEUE_SIZE = 20;
    private static final int BOOST_DISTANCE_TOLERANCE = 70;
    private static final int UNBOOST_DISTANCE_TOLERANCE = 40;

    private Bitmap mBackground;
    private Bitmap mStickLR;
    private Bitmap mStickUD;
    private Bitmap mBoostingBar;

    private Point mBoostingBarOrigin;

    private Rect mBounds;
    private Rect mStickLRBounds;
    private Rect mStickUDBounds;

    private int pointerUDStick;
    private int pointerLRStick;

    private boolean controllingUDStick = false;
    private boolean controllingLRStick = false;

    private FifoAutoQueue mQueue;
    private boolean boosting = false;
    private int boostedX;

    private ScheduledThreadPoolExecutor mLoopThreadPool;

    private float mGas;
    private float mSteering;
    private int mGasControlLength;
    private int mSteeringControlLength; // to one side, so half of the total width
    private int mControlCenterEdgeOffset;

    private OnRcUpdateListener mListener;
    private Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onRCUpdate(mGas, mSteering);
            }
        }
    };

    public interface OnRcUpdateListener {
        public void onRCUpdate(float gas, float steering);
        public void onStop();
        public void onBoost();
    }

    public RcControlView(Context context) {
        super(context);
        loadImages(context);
        setupControlBoundaries();
        resetControls();
    }

    public RcControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadImages(context);
        setupControlBoundaries();
        resetControls();
    }

    public void setOnRcUpdateListener(OnRcUpdateListener listener) {
        mListener = listener;
    }

    public void startControlSystem() {
        if (mLoopThreadPool == null) {
            mLoopThreadPool = new ScheduledThreadPoolExecutor(1);
        }

        mLoopThreadPool.scheduleAtFixedRate(mUpdater, UPDATE_INTERVAL, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void stopControlSystem() {
        if (mLoopThreadPool == null) return;

        mLoopThreadPool.shutdownNow();
        mLoopThreadPool = null;
    }

    private void loadImages(Context context) {
        mBackground = BitmapFactory.decodeResource(context.getResources(), R.drawable.rc_background);
        mBoostingBar = BitmapFactory.decodeResource(context.getResources(), R.drawable.rc_boosting_bar);
        mStickLR = BitmapFactory.decodeResource(context.getResources(), R.drawable.rc_slider_left_right);
        mStickLRBounds = new Rect(0, 0, mStickLR.getWidth(), mStickLR.getHeight());
        mStickUD = BitmapFactory.decodeResource(context.getResources(), R.drawable.rc_slider_up_down);
        mStickUDBounds = new Rect(0, 0, mStickUD.getWidth(), mStickUD.getHeight());
    }

    private void resetControls() {
        mGas = 0.0f;
        mSteering = 0.0f;
        boosting = false;
    }

    private void setupControlBoundaries() {
        Log.d("Orbotix", "density: " + getContext().getResources().getDisplayMetrics().density);
        controlEdgePadding = (int)(getContext().getResources().getDisplayMetrics().density * CONTROL_EDGE_PADDING);
        boostXSpacing = (int)(getContext().getResources().getDisplayMetrics().density * BOOST_X_SPACING);
        boostYSpacing = (int)(getContext().getResources().getDisplayMetrics().density * BOOST_Y_SPACING);
        mGasControlLength = mBackground.getHeight() - (2 * controlEdgePadding) - mStickUD.getHeight();
        mSteeringControlLength = mGasControlLength / 2;
    }

    private int getGasContolHeight() {
        return (int)(mGas * mGasControlLength);
    }

    private int getSteeringOffset() {
        return (int)(mSteering * mSteeringControlLength);
    }

    private Rect getStickUDFrame() {
        int shift = 0;
        if (boosting) {
            shift = 50;
        }
        int stickUDBottom = mBounds.bottom - controlEdgePadding - getGasContolHeight();
        return new Rect(mControlCenterEdgeOffset - mStickUDBounds.centerX() + shift,
                stickUDBottom - mStickUDBounds.height(),
                mControlCenterEdgeOffset + mStickUDBounds.centerX() + shift,
                stickUDBottom);
    }

    private Rect getStickLRFrame() {
        int stickLRCenterX = mBounds.right - mControlCenterEdgeOffset + getSteeringOffset();
        return new Rect(stickLRCenterX - mStickLRBounds.centerX(),
                mBounds.centerY() - mStickLRBounds.centerY(),
                stickLRCenterX + mStickLRBounds.centerX(),
                mBounds.centerY() + mStickLRBounds.centerY());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBackground.getWidth(), mBackground.getHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mBounds = new Rect(0, 0, right - left, bottom - top);
        mControlCenterEdgeOffset = mBounds.centerX() / 2;
        mBoostingBarOrigin = new Point(mControlCenterEdgeOffset + boostXSpacing, boostYSpacing);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBackground, mBounds, mBounds, null);
        if (boosting) {
            canvas.drawBitmap(mBoostingBar, mBoostingBarOrigin.x, mBoostingBarOrigin.y, null);
        }
        canvas.drawBitmap(mStickLR, mStickLRBounds, getStickLRFrame(), null);
        canvas.drawBitmap(mStickUD, mStickUDBounds, getStickUDFrame(), null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;

        int pointer_index = event.getActionIndex();
        int pointer_id = event.getPointerId(pointer_index);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                if (hitUDStick(event, pointer_index)) {
                    pointerUDStick = pointer_id;
                    mQueue = new FifoAutoQueue(BOOST_QUEUE_SIZE);
                    controllingUDStick = true;
                    handled = true;
                }

                if (hitLRStick(event, pointer_index)) {
                    pointerLRStick = pointer_id;
                    controllingLRStick = true;
                    handled = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerCount = event.getPointerCount();
                for (int i = 0; i < pointerCount; i++) {
                    int pointerId = event.getPointerId(i);

                    if (controllingUDStick && pointerId == pointerUDStick) {
                        float differenceFromBottom = mBounds.bottom - mStickUDBounds.centerY() -
                                controlEdgePadding - event.getY(i);
                        mGas = differenceFromBottom / (float)mGasControlLength;
                        mGas = (float)Value.clamp(mGas, 0.0, 1.0);
                        handled = true;

                        if (!boosting) {
                            lookForBoost((int)event.getX(i));
                        } else {
                            if (DriveActivity.DEBUG) {
                                Log.d(DriveActivity.TAG, String.format("boostedX - x = %d", (boostedX - ((int)event.getX(i)))));
                            }
                            if (boostedX - ((int)event.getX(i)) > UNBOOST_DISTANCE_TOLERANCE) {
                                boosting = false;
                                mQueue = new FifoAutoQueue(BOOST_QUEUE_SIZE);
                            }
                        }

                        invalidate();
                    }

                    if (controllingLRStick && pointerId == pointerLRStick) {
                        float differenceFromCenter = event.getX(i) - (mBounds.right - mControlCenterEdgeOffset);
                        mSteering = differenceFromCenter / (float)mSteeringControlLength;
                        mSteering = (float)Value.clamp(mSteering, -1.0, 1.0);
                        handled = true;
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (controllingUDStick && pointer_id == pointerUDStick) {
                    controllingUDStick = false;
                    resetUDStick();
                    handled = true;
                }

                if (controllingLRStick && pointer_id == pointerLRStick) {
                    controllingLRStick = false;
                    resetLRStick();
                    handled = true;
                }
                break;
        }
        
        return handled;
    }

    private void lookForBoost(int x) {
        mQueue.add(x);
        if (mQueue.full()) {
            if (mQueue.getLast() - mQueue.getFirst() > BOOST_DISTANCE_TOLERANCE) {
                // Boost
                boosting = true;
                boostedX = x;
                if (mListener != null) {
                    mListener.onBoost();
                }
            }
        }
    }

    private void resetUDStick() {
        mGas = 0.0f;
        boosting = false;
        if (mListener != null) {
            mListener.onStop();
        }
        invalidate();
    }

    private void resetLRStick() {
        mSteering = 0.0f;
        invalidate();
    }

    private boolean hitUDStick(MotionEvent event, int pointer) {
        return getStickUDFrame().contains((int) event.getX(pointer), (int) event.getY(pointer));
    }

    private boolean hitLRStick(MotionEvent event, int pointer) {
        return getStickLRFrame().contains((int) event.getX(pointer), (int) event.getY(pointer));
    }

}
