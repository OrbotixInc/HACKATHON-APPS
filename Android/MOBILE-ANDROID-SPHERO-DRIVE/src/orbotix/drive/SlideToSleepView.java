package orbotix.drive;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * A custom widget that represents a orbotix.golf.golf ball going into a cup. The view functions a lot like a slider, progress bar,
 * or seek bar in that it has a sliding element on a long track. The SlideToScoreView will start with a ball on the
 * left end of the slider and a cup will occupy the opposite side of the slider. When slid all the way to the cup on
 * the other side, the ball will drop into the cup using a custom animation and the view will notify its listeners
 * that there was a "score". If the ball is not slid all the way to the other side, it will animate back to its
 * starting position and no "score" will be triggered.
 *
 * @author Orbotix Inc.
 * @since 0.12.0
 */
public class SlideToSleepView extends View{

    private OnSleepListener mListener;
    private Bitmap mSliderBackground;
    private Bitmap mSliderBall;
    private Bitmap mTempBall;

    private Point mBackgroundPosition;
    private Point mBallPosition;

    private boolean dragging = false;
    private boolean resetting = false;
    private boolean scoring = false;
    private boolean unScoring = false;

    private float resetStep = 0.0f;
    private float scoreStep = 0.0f;

    private Handler mHandler = new Handler();
    private Runnable mInvalidateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    /**
     */
    public interface OnSleepListener {
        /**
         */
        public void onSleep();
    }

    private void notifyListener() {
        if (mListener != null) {
            mListener.onSleep();
        }
    }

    public SlideToSleepView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSliderBackground = BitmapFactory.decodeResource(getResources(), R.drawable.slidetosleep_background);
        mSliderBall = BitmapFactory.decodeResource(getResources(), R.drawable.slidetosleep_arrow);
    }

    /**
     * Sets the {@link orbotix.drive.SlideToSleepView.OnSleepListener} for this view to be called when the view is slid to the score position.
     *
     * @param listener the listener to be notified when this view is slid to the score position.
     */
    public void setOnSleepListener(OnSleepListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // since we know the resources that will be in this view and, for now, we only want this view to be one size
        // we will just set the dimensions to be the max width and max height of the view elements.
        setMeasuredDimension(mSliderBackground.getWidth(), mSliderBall.getHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int height = bottom - top;
        int width = right - left;
        int widthMiddle = (width - mSliderBackground.getWidth()) / 2;
        int heightMiddle = (height - mSliderBackground.getHeight()) / 2;
        mBackgroundPosition = new Point(widthMiddle, heightMiddle);
        heightMiddle = (height - mSliderBall.getHeight()) / 2;
        if (mBallPosition == null) {
            mBallPosition = new Point(mBackgroundPosition.x, heightMiddle);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mSliderBackground, mBackgroundPosition.x, mBackgroundPosition.y, null);
        if (resetting) {
            resetStep *= 1.08f;
            int newX = (int)(mBallPosition.x - resetStep);
            if (newX < mBackgroundPosition.x) {
                resetting = false;
                newX = mBackgroundPosition.x;
            }
            mBallPosition = new Point(newX, mBallPosition.y);
            mHandler.postDelayed(mInvalidateRunnable, 1);
        } else if (scoring) {
            scoreStep *=0.95f;
            if (mSliderBall.getWidth() <= 0.5 * mTempBall.getWidth()) {
                scoring = false;
                scoreStep = 0.5f;
                notifyListener();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        unScoring = true;
                        invalidate();
                    }
                }, 1000);
            }
            int width = (int)((float)mTempBall.getWidth() * scoreStep);
            int height = (int)((float)mTempBall.getHeight() * scoreStep);
            mSliderBall = Bitmap.createScaledBitmap(mTempBall, width, height, true);
            mBallPosition = new Point((int)(mBallPosition.x), (getHeight() - mSliderBall.getHeight()) / 2);
            mHandler.postDelayed(mInvalidateRunnable, 1);
        } else if (unScoring) {
            scoreStep /= 0.95f;
            if (mSliderBall.getHeight() >= mTempBall.getHeight()) {
                unScoring = false;
                mSliderBall = mTempBall.copy(Bitmap.Config.ARGB_8888, true);
                reset();
                scoreStep = 1.0f;
            }
            int width = (int)((float)mTempBall.getWidth() * scoreStep);
            int height = (int)((float)mTempBall.getHeight() * scoreStep);
            mSliderBall = Bitmap.createScaledBitmap(mTempBall, width, height, true);
            mBallPosition = new Point((int)(mBallPosition.x), (getHeight() - mSliderBall.getHeight()) / 2);
            mHandler.postDelayed(mInvalidateRunnable, 1);
        }

        canvas.drawBitmap(mSliderBall, mBallPosition.x, mBallPosition.y, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (touchInsideObject(event, mBallPosition, mSliderBall)) {
                    dragging = true;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    updateBallPosition(event.getX());
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                dragging = false;
                if (mBallPosition.x > (mBackgroundPosition.x + mSliderBackground.getWidth() - (1.5 * mSliderBall.getWidth()))) {
                    score();
                } else {
                    reset();
                }
                return true;
        }
        return false;
    }

    private void score() {
        notifyListener();
        reset();
    }

    private void reset() {
        resetStep = 1.0f;
        resetting = true;
        invalidate();
    }

    private void updateBallPosition(float touchX) {
        float newXPosition = touchX - (mSliderBall.getWidth() / 2);
        if (newXPosition < mBackgroundPosition.x) {
            mBallPosition.x = mBackgroundPosition.x;
        } else if (newXPosition > (mBackgroundPosition.x + mSliderBackground.getWidth()) - mSliderBall.getWidth()) {
            mBallPosition.x = (mBackgroundPosition.x + mSliderBackground.getWidth()) - mSliderBall.getWidth();
        } else {
            mBallPosition = new Point((int)newXPosition, mBallPosition.y);
        }
    }

    private boolean touchInsideObject(MotionEvent touchEvent, Point objectPosition, Bitmap object) {
        float x = touchEvent.getX();
        float y = touchEvent.getY();
        if (x >= objectPosition.x && x <= (objectPosition.x + object.getWidth())) {
            if (y >= objectPosition.y && y <= (objectPosition.y + object.getHeight())) {
                return true;
            }
        }
        return false;
    }

}
