package orbotix.drive;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DriveWheelView extends View {

    private static final int BOTTOM_BOOST_SPACE = 3;
    private static final int BOTTOM_WHEEL_SPACE = 24;
    private static final int BOOST_WHEEL_SPACE = 14;
    private static final int PUCK_EDGE_OVERLAP = 35;
    private int puckEdgeOverlap;

    private static final int CENTER_OFFSET_POSITION = 0;
    private static final int TYPE_POSITION = 1;

    private static final int JOYSTICK = 0;
    private static final int TILT = 1;

	private Bitmap wheel;
	private Bitmap puck;
	private Bitmap boostButton;
	private Bitmap boostButtonOn;
	private Bitmap leftBoostButton;
    private Bitmap rightBoostButton;
    private Bitmap background;

	private Rect bounds;
    private Rect backgroundFrame, backgroundBounds;
	private Rect puckBounds;
	private RectF puckFrame;
	private Rect wheelBounds;
	private Rect wheelFrame;
	private Rect leftBoostButtonBounds;
	private Rect leftBoostButtonFrame;
    private Rect rightBoostButtonBounds;
    private Rect rightBoostButtonFrame;

	private Matrix puckTranslation;

	private TouchMotionListener touchMotionListener;

	private boolean handleTouchEvents = true;
	private boolean draggingPuck = false;
	private int		draggingPuckPointerId;
	private boolean leftBoostDown = false;
    private boolean rightBoostDown = false;
	private int 	leftBoostPointerId;
    private int     rightBoostPointerId;
    private int bottomBoostSpace, bottomWheelSpace, boostWheelSpace;

    private enum Type {joystick, tilt}
    private Type type;

	public interface TouchMotionListener {
		public void onStart();
		public void onMove(float positionX, float positionY);
		public void onStop();
		public void onBoost();
	}

	public DriveWheelView(Context context) {
		super(context);
		commonConstruction(context);
	}

	public DriveWheelView(Context context, AttributeSet attributes) {
		super(context, attributes);
        TypedArray attrsArray = context.obtainStyledAttributes(attributes, R.styleable.orbotix_drive_DriveWheelView);
        switch (attrsArray.getInt(TYPE_POSITION, JOYSTICK)) {
            case JOYSTICK:
                type = Type.joystick;
                break;
            case TILT:
                type = Type.tilt;
                break;
        }
		commonConstruction(context);
	}

	private void commonConstruction(Context context) {
        switch (type) {
            default:
            case joystick:
                wheel = BitmapFactory.decodeResource(getResources(), R.drawable.drive_wheel);
                puck = BitmapFactory.decodeResource(getResources(), R.drawable.drive_puck);
                break;
            case tilt:
                wheel = BitmapFactory.decodeResource(getResources(), R.drawable.tilt_wheel);
                puck = BitmapFactory.decodeResource(getResources(), R.drawable.tilt_puck);
                break;
        }

        background = BitmapFactory.decodeResource(getResources(), R.drawable.drive_background);
        boostButton = BitmapFactory.decodeResource(getResources(), R.drawable.boost_button);
        boostButtonOn = BitmapFactory.decodeResource(getResources(), R.drawable.boost_button_pressed);
        leftBoostButton = boostButton;
        rightBoostButton = boostButton;

        backgroundBounds = new Rect(0, 0, background.getWidth(), background.getHeight());
        puckBounds = new Rect(0, 0, puck.getWidth(), puck.getHeight());
        wheelBounds = new Rect(0, 0, wheel.getWidth(), wheel.getHeight());
        leftBoostButtonBounds = new Rect(0, 0, boostButton.getWidth(), boostButton.getHeight());
        rightBoostButtonBounds = new Rect(0, 0, boostButton.getWidth(), boostButton.getHeight());

        puckTranslation = new Matrix();
        float density = context.getResources().getDisplayMetrics().density;
        puckEdgeOverlap = Math.round(density * (float)PUCK_EDGE_OVERLAP);
        bottomBoostSpace = Math.round(density * (float)BOTTOM_BOOST_SPACE);
        bottomWheelSpace = Math.round(density * (float)BOTTOM_WHEEL_SPACE);
        boostWheelSpace = Math.round(density * (float)BOOST_WHEEL_SPACE);
    }

	public int getDrivePadWidth() {
		return wheelBounds.width() - puckBounds.width() + 2* puckEdgeOverlap;
	}

	public int getDrivePadHeight() {
		return wheelBounds.height() - puckBounds.height() + 2* puckEdgeOverlap;
	}

	public void updatePuckPosition(float heading, float magnitude) {
		float heading_radians = (float) (heading * Math.PI/180.0);
		float x = (float) ((wheelBounds.centerX() - puckBounds.centerX() + puckEdgeOverlap) * magnitude * Math.sin((double)heading_radians));
		float y = (float) (-(wheelBounds.centerY() - puckBounds.centerY() + puckEdgeOverlap) * magnitude * Math.cos((double)heading_radians));

		puckTranslation.setTranslate(x, y);
		invalidate();
	}

	public boolean handleTouchEvents() {
		return handleTouchEvents;
	}

	public void setHandleTouchEvents(boolean newState) {
		handleTouchEvents = newState;
	}

	public void setTouchMotionListener(TouchMotionListener listener) {
		touchMotionListener = listener;
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(background.getWidth(), background.getHeight());
    }

    @Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		bounds = new Rect(0, 0, right - left, bottom - top);
        backgroundFrame = new Rect((bounds.centerX() - background.getWidth()/2),
                (bounds.bottom - background.getHeight()),
                (bounds.centerX() + background.getWidth()/2),
                (bounds.bottom));

        wheelFrame = new Rect((bounds.centerX() - wheel.getWidth()/2),
                (backgroundBounds.bottom - wheel.getHeight() - bottomWheelSpace),
                (bounds.centerX() + wheel.getWidth()/2),
                (backgroundBounds.bottom - bottomWheelSpace));
        puckFrame = new RectF((wheelFrame.centerX() - puck.getWidth()/2),
                (wheelFrame.centerY() - puck.getHeight()/2),
                (wheelFrame.centerX() + puck.getWidth()/2),
                (wheelFrame.centerY() + puck.getHeight()/2));
        leftBoostButtonFrame = new Rect((wheelFrame.left - boostButton.getWidth() - boostWheelSpace),
                (bounds.bottom - boostButton.getHeight() - bottomBoostSpace),
                (wheelFrame.left - boostWheelSpace),
                (bounds.bottom - bottomBoostSpace));
        rightBoostButtonFrame = new Rect((wheelFrame.right + boostWheelSpace),
                (bounds.bottom - boostButton.getHeight() - bottomBoostSpace),
                (wheelFrame.right + boostButton.getWidth() + boostWheelSpace),
                (bounds.bottom - bottomBoostSpace));
    }
/*
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}*/

	@Override
	protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(background, null, backgroundFrame, null);
		canvas.drawBitmap(wheel, wheelBounds, wheelFrame, null);
		canvas.drawBitmap(leftBoostButton, leftBoostButtonBounds, leftBoostButtonFrame, null);
		canvas.drawBitmap(rightBoostButton, rightBoostButtonBounds, rightBoostButtonFrame, null);

		RectF translated_rect = new RectF();
		puckTranslation.mapRect(translated_rect, puckFrame);
		canvas.drawBitmap(puck, puckBounds, translated_rect, null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!handleTouchEvents) return false;

        boolean handled = false;

        int pointer_index = event.getActionIndex();
        int pointer_id = event.getPointerId(pointer_index);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                if (type == Type.joystick && hitPuck(event, pointer_index)) {
                    draggingPuck = true;
                    draggingPuckPointerId = pointer_id;
                    handled = true;
                    if (touchMotionListener != null) {
                        touchMotionListener.onStart();
                    }
                } else if (hitLeftBoostButton(event, pointer_index)) {
                    // boost!!!
                    handled = true;
                    leftBoostButton = boostButtonOn;
                    invalidate();
                    leftBoostDown = true;
                    leftBoostPointerId = pointer_id;
                    if (touchMotionListener != null) {
                        // always send a boost
                        touchMotionListener.onBoost();
                    }
                } else if (hitRightBoostButton(event, pointer_index)) {
                    // boost!!!
                    handled = true;
                    rightBoostButton = boostButtonOn;
                    invalidate();
                    rightBoostDown = true;
                    rightBoostPointerId = pointer_id;
                    if (touchMotionListener != null) {
                        // always send a boost
                        touchMotionListener.onBoost();
                    }
                } else if (hitWheel(event, pointer_index)) {
                    handled = true;
                }
                break;

		case MotionEvent.ACTION_MOVE:
			if (type == Type.joystick && draggingPuck && draggingPuckPointerId == pointer_id) {
				float delta_x = event.getX(pointer_index);
				float delta_y = event.getY(pointer_index);

				// adjust to drive wheel coordinates
				delta_x = delta_x - wheelFrame.left - puckBounds.centerX() + puckEdgeOverlap;
				if (delta_x < 0) {
					delta_x = 0;
				} else if (delta_x > (wheelFrame.right - puckBounds.centerX() + puckEdgeOverlap)) {
					delta_x = wheelFrame.right - puckBounds.centerX() + puckEdgeOverlap;
				}

                delta_y = delta_y - wheelFrame.top - puckBounds.centerY() + puckEdgeOverlap;
                if (delta_y < 0) {
                    delta_y = 0;
                } else if (delta_y > (wheelFrame.bottom - puckBounds.centerY() + puckEdgeOverlap)) {
                    delta_y = wheelFrame.bottom - puckBounds.centerY() + puckEdgeOverlap;
                }

                if (touchMotionListener != null) {
                    touchMotionListener.onMove(delta_x, delta_y);
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if (type == Type.joystick && draggingPuck && draggingPuckPointerId == pointer_id) {
                    puckTranslation.reset();
                    invalidate();
                    if (touchMotionListener != null) {
                        touchMotionListener.onStop();
                    }
                    draggingPuck = false;
                    handled = true;
                } else if (leftBoostDown && leftBoostPointerId == pointer_id) {
                    leftBoostDown = false;
                    handled= true;
                    leftBoostButton = boostButton;
                    invalidate();
                } else if (rightBoostDown && rightBoostPointerId == pointer_id) {
                    rightBoostDown = false;
                    handled= true;
                    rightBoostButton = boostButton;
                    invalidate();
                }
                break;

            default:
                break;
        }

		return handled;
	}

	private boolean hitWheel(MotionEvent event, int pointer) {
		boolean hit = false;
		float x_from_center = event.getX(pointer) - bounds.centerX();
		float y_from_center = event.getY(pointer) - bounds.centerY();
		float touch_radius = (float) Math.sqrt((x_from_center * x_from_center + y_from_center * y_from_center));
		if (touch_radius <= (wheel.getWidth()/2) && touch_radius > (puck.getWidth()/2)) {
			hit = true;
		}
		return hit;
	}

	private boolean hitPuck(MotionEvent event, int pointer) {
		return puckFrame.contains((int)event.getX(pointer), (int)event.getY(pointer));
	}

	private boolean hitLeftBoostButton(MotionEvent event, int pointer) {
		return leftBoostButtonFrame.contains((int)event.getX(pointer), (int)event.getY(pointer));
	}

	private boolean hitRightBoostButton(MotionEvent event, int pointer) {
		return rightBoostButtonFrame.contains((int)event.getX(pointer), (int)event.getY(pointer));
	}
}
