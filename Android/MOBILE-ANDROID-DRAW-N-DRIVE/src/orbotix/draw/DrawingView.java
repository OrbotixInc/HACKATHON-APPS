package orbotix.draw;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * User: brandon
 * Date: 9/27/11
 * Time: 10:21 AM
 */
public class DrawingView extends View {

    private static final float ACCELEROMETER_UP_THRESHOLD = 10.0f;
    private static final float ACCELEROMETER_DOWN_THRESHOLD = 5.0f;
    private static final long MAX_SHAKE_TIME = 1000;

    private enum PadType {fullscreen, window}
    private PadType padType;
    private PathListener mListener;
    private Paint mPaint;
    private Point lastPoint;

    private boolean drawing;
    private int mDrawingPointerId;
    //private ArrayList<Path> paths;
    private int mCurrentPathIndex;

    private int mColor;
    private Display display;

    private SensorManager mSensorManager;
    private boolean shakePeaking;
    private boolean drawingPaused;
    private int mRecentShakes;
    private Handler mHandler = new Handler();
    private Bitmap bitmap;
    private Canvas canvas;

    public interface PathListener {
        public void pathDidStart(Point point, int pointerId);
        public void pathDidChange(Point point, float pressure);
        public void pathDidEnd(Point point, int pointerId);
        public void pathCancelled();
    }

	public DrawingView(Context context) {
		super(context);
        padType = PadType.fullscreen;
        setup(context);
	}

	public DrawingView(Context context, AttributeSet attributes) {
		super(context, attributes);
        TypedArray attrsArray = context.obtainStyledAttributes(attributes, R.styleable.orbotix_draw_DrawingView);
        if (attrsArray != null && attrsArray.hasValue(0)) {
            padType = attrsArray.getInt(0, 0) == 0 ? PadType.fullscreen : PadType.window;
        } else {
            padType = PadType.fullscreen;
        }
        setup(context);
	}

    private void setup(Context context) {
        display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        bitmap = Bitmap.createBitmap(display.getWidth(), display.getHeight(), Bitmap.Config.ARGB_4444);
        canvas = new Canvas(bitmap);
        //paths = new ArrayList<Path>(100);
        //paths.add(new Path());
        mCurrentPathIndex = 0;
        mDrawingPointerId = -1;
        mPaint = new Paint();
        mPaint.setFlags(Paint.DITHER_FLAG|Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mColor = Color.BLACK;
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(4.0f);
        shakePeaking = false;
        drawingPaused = false;
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorEventListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
    }

    public void setColor(int newColor) {
        mColor = newColor;
    }

    public int getColor() {
        return mColor;
    }

    public void shutdown() {
        mSensorManager.unregisterListener(mSensorEventListener);
        mListener = null;
    }

    public void setPathListener(PathListener listener) {
        mListener = listener;
    }

    @Override
    public void onDraw(Canvas canvas) {
        /*for (Path path : paths) {
            canvas.drawPath(path, mPaint);
        }*/

        canvas.drawBitmap(bitmap, 0.0f, 0.0f, mPaint);
    }

    public boolean useTouchEvent(MotionEvent event, int index) {
        boolean handled = false;
        int pointer_id = event.getPointerId(index);
        float x = event.getX(index);
        float y = event.getY(index);
        switch (event.getAction()) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                if (mDrawingPointerId == -1) {
                    mDrawingPointerId = pointer_id;
                    drawing = true;
                    Point point = new Point((int)x, (int)y);
                    if (!drawingPaused) {
                        //paths.get(mCurrentPathIndex).moveTo(point.x, point.y);
                        lastPoint = new Point((int)x, (int)y);
                    }
                    if (mListener != null) {
                        mListener.pathDidStart(point, pointer_id);
                    }
                    handled = true;

                }
                break;
            
            case MotionEvent.ACTION_MOVE:

                if (pointer_id == mDrawingPointerId) {
                    Point point = new Point((int)x, (int)y);
                    if (!drawingPaused) {
                        //paths.get(mCurrentPathIndex).lineTo(point.x, point.y);
                        mPaint.setColor(mColor);
                        canvas.drawLine((float)lastPoint.x, (float)lastPoint.y, (float)point.x, (float)point.y, mPaint);
                        lastPoint = new Point(point);
                    }
                    if (mListener != null) {
                        mListener.pathDidChange(point, event.getPressure(index));
                    }
                    handled = true;
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (drawing && pointer_id == mDrawingPointerId) {
                    Point point = new Point((int)x, (int)y);
                    if (!drawingPaused) {
                        mPaint.setColor(mColor);
                        canvas.drawLine((float)lastPoint.x, (float)lastPoint.y, (float)point.x, (float)point.y, mPaint);
                    }
                    mCurrentPathIndex++;
                    if (mListener != null) {
                        mListener.pathDidEnd(point, pointer_id);
                    }
                    drawing = false;
                    mDrawingPointerId = -1;
                    handled = true;
                }
                break;

            default:
                break;
        }
        invalidate();
        return handled;
    }

    public void clearCanvas() {
        //paths = new ArrayList<Path>();
        //paths.add(new Path());
        bitmap = Bitmap.createBitmap(display.getWidth(), display.getHeight(), Bitmap.Config.ARGB_4444);
        canvas.setBitmap(bitmap);
        mCurrentPathIndex = 0;
        invalidate();
        if (mListener != null) {
            mListener.pathCancelled();
        }
    }

    public boolean isDrawing() {
        return drawing;
    }

    private void clearShakes() {
        mRecentShakes = 0;
    }

    public void pauseDrawing() {
        drawingPaused = true;
    }

    public void resumeDrawing() {
        drawingPaused = false;
    }

    private Runnable clearShakesRunnable = new Runnable() {
        @Override
        public void run() {
            clearShakes();
        }
    };

    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float shakeMagnitude = Math.abs(event.values[0]) + Math.abs(event.values[1]);
            if (!shakePeaking && shakeMagnitude > ACCELEROMETER_UP_THRESHOLD) {
                mHandler.removeCallbacks(clearShakesRunnable);
                shakePeaking = true;
                mRecentShakes++;
                if (mRecentShakes > 2) {
                    clearCanvas();
                    clearShakes();
                } else {
                    mHandler.postDelayed(clearShakesRunnable, MAX_SHAKE_TIME);
                }
            } else if (shakePeaking && shakeMagnitude < ACCELEROMETER_DOWN_THRESHOLD) {
                shakePeaking = false;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    };
}
