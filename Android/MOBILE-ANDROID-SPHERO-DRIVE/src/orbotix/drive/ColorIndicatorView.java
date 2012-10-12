package orbotix.drive;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class ColorIndicatorView extends View {

    private Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.color_brick_bg);
    private Bitmap gloss = BitmapFactory.decodeResource(getResources(), R.drawable.color_brick_gloss);

    private static final float INSET_X = 8.5f;
    private static final float INSET_Y = 7.5f;
    private float insetX, insetY;

    private static final float CORNER_RADIUS = 4.0f;
    private float cornerRadius;

	private Paint fillPaint = new Paint();
    private RectF colorRect;

	public ColorIndicatorView(Context context) {
		super(context);
		fillPaint.setColor(Color.argb(0, 0, 0, 0));
        setupConstants();
	}

	public ColorIndicatorView(Context context, AttributeSet attributes) {
		super(context, attributes);
		fillPaint.setColor(Color.argb(0, 0, 0, 0));
        setupConstants();
	}

    private void setupConstants() {
        float density = getContext().getResources().getDisplayMetrics().density;
        insetX = density * INSET_X;
        insetY = density * INSET_Y;
        cornerRadius = density * CORNER_RADIUS;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
        colorRect = new RectF(0.0f, 0.0f, (float)background.getWidth(), (float)background.getHeight());
        colorRect.inset(insetX, insetY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(background.getWidth(), background.getHeight());
    }

	@Override
	protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(background, 0, 0, null);
		canvas.drawRoundRect(colorRect, cornerRadius, cornerRadius, fillPaint);
        canvas.drawBitmap(gloss, 0, 0, null);
	}

	public void setColor(int red, int green, int blue) {
		fillPaint.setColor(Color.rgb(red, green, blue));
		invalidate();
	}


}
