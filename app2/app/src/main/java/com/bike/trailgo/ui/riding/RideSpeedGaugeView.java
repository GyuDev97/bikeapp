package com.bike.trailgo.ui.riding;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/** A speed gauge whose green arc is drawn from the current speed instead of a static image. */
public class RideSpeedGaugeView extends View {
    private static final float MAX_SPEED_KMH = 40f;

    private final Paint trackPaint = createPaint("#E1E4E7", 16f);
    private final Paint progressPaint = createPaint("#237A35", 16f);
    private final Paint tickPaint = createPaint("#D4D9D5", 2f);
    private final RectF arcBounds = new RectF();
    private float speedKmh;

    public RideSpeedGaugeView(Context context) {
        super(context);
        init();
    }

    public RideSpeedGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RideSpeedGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setSpeedKmh(float speedKmh) {
        float safeSpeed = Math.max(0f, Math.min(MAX_SPEED_KMH, speedKmh));
        if (this.speedKmh != safeSpeed) {
            this.speedKmh = safeSpeed;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float stroke = dp(16f);
        trackPaint.setStrokeWidth(stroke);
        progressPaint.setStrokeWidth(stroke);
        tickPaint.setStrokeWidth(dp(2f));

        float centerX = getWidth() / 2f;
        float bottom = getHeight() - dp(17f);
        float radius = Math.min(centerX - dp(28f), bottom - dp(16f));
        if (radius <= 0f) {
            return;
        }

        arcBounds.set(centerX - radius, bottom - radius, centerX + radius, bottom + radius);
        canvas.drawArc(arcBounds, 180f, 180f, false, trackPaint);

        float progress = speedKmh / MAX_SPEED_KMH;
        if (progress > 0f) {
            canvas.drawArc(arcBounds, 180f, 180f * progress, false, progressPaint);
        }

        float outerRadius = radius - dp(18f);
        float innerRadius = outerRadius - dp(12f);
        for (int i = 0; i <= 6; i++) {
            double angle = Math.PI + (Math.PI * i / 6d);
            float startX = centerX + (float) (Math.cos(angle) * outerRadius);
            float startY = bottom + (float) (Math.sin(angle) * outerRadius);
            float endX = centerX + (float) (Math.cos(angle) * innerRadius);
            float endY = bottom + (float) (Math.sin(angle) * innerRadius);
            canvas.drawLine(startX, startY, endX, endY, tickPaint);
        }
    }

    private Paint createPaint(String color, float strokeWidthDp) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setColor(Color.parseColor(color));
        paint.setStrokeWidth(dp(strokeWidthDp));
        return paint;
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
