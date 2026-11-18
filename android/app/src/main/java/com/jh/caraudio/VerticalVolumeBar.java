package com.jh.caraudio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Control vertical de volumen ergonómico para auto (adaptado a Tema Claro y Oscuro).
 */
public class VerticalVolumeBar extends View {

    public interface OnVolumeChangeListener {
        void onVolumeChanged(int percent, boolean fromUser);
    }

    private int progress = 75;
    private boolean lightMode = false;
    private OnVolumeChangeListener listener;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF trackRect = new RectF();
    private final RectF fillRect = new RectF();

    public VerticalVolumeBar(Context context) {
        super(context);
        init();
    }

    public VerticalVolumeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalVolumeBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        trackPaint.setStyle(Paint.Style.FILL);
        trackBorderPaint.setStyle(Paint.Style.STROKE);
        trackBorderPaint.setStrokeWidth(dpToPx(1.2f));
        fillPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbBorderPaint.setStyle(Paint.Style.STROKE);
        thumbBorderPaint.setStrokeWidth(dpToPx(2.5f));
        thumbGlowPaint.setStyle(Paint.Style.FILL);
        applyColors();
    }

    public void setLightMode(boolean light) {
        if (this.lightMode == light) return;
        this.lightMode = light;
        applyColors();
        invalidate();
    }

    private void applyColors() {
        if (lightMode) {
            trackPaint.setColor(0xFFCBD5E1);
            trackBorderPaint.setColor(0xFFB6C3D4);
            thumbPaint.setColor(0xFFFFFFFF);
            thumbBorderPaint.setColor(0xFF2563EB);
            thumbGlowPaint.setColor(0x332563EB);
        } else {
            trackPaint.setColor(Color.parseColor("#11141a"));
            trackBorderPaint.setColor(Color.parseColor("#283244"));
            thumbPaint.setColor(Color.parseColor("#f1f5f9"));
            thumbBorderPaint.setColor(Color.parseColor("#0d1117"));
            thumbGlowPaint.setColor(Color.parseColor("#5538bdf8"));
        }
    }

    public void setProgress(int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        if (this.progress != clamped) {
            this.progress = clamped;
            invalidate();
        }
    }

    public int getProgress() {
        return progress;
    }

    public void setOnVolumeChangeListener(OnVolumeChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float trackW = dpToPx(16f);
        float thumbRadius = dpToPx(13f);
        float padY = thumbRadius + dpToPx(4f);
        float usableH = h - (padY * 2f);

        float trackLeft = (w - trackW) / 2f;
        float trackRight = trackLeft + trackW;
        float trackTop = padY;
        float trackBottom = h - padY;

        trackRect.set(trackLeft, trackTop, trackRight, trackBottom);
        canvas.drawRoundRect(trackRect, trackW / 2f, trackW / 2f, trackPaint);
        canvas.drawRoundRect(trackRect, trackW / 2f, trackW / 2f, trackBorderPaint);

        float fillTop = trackBottom - (usableH * (progress / 100f));
        if (progress > 0) {
            fillRect.set(trackLeft, fillTop, trackRight, trackBottom);
            int topC = lightMode ? 0xFF3B82F6 : 0xFF60A5FA;
            int botC = lightMode ? 0xFF2563EB : 0xFF38BDF8;
            fillPaint.setShader(new LinearGradient(trackLeft, fillTop, trackLeft, trackBottom,
                    topC, botC, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(fillRect, trackW / 2f, trackW / 2f, fillPaint);
        }

        float thumbY = fillTop;
        float thumbX = w / 2f;
        canvas.drawCircle(thumbX, thumbY, thumbRadius + dpToPx(2f), thumbGlowPaint);
        canvas.drawCircle(thumbX, thumbY, thumbRadius, thumbPaint);
        canvas.drawCircle(thumbX, thumbY, thumbRadius, thumbBorderPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                updateTouchProgress(event.getY());
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                updateTouchProgress(event.getY());
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void updateTouchProgress(float touchY) {
        int h = getHeight();
        float thumbRadius = dpToPx(10f);
        float padY = thumbRadius + dpToPx(4f);
        float usableH = h - (padY * 2f);

        float fraction = 1.0f - ((touchY - padY) / usableH);
        fraction = Math.max(0f, Math.min(1f, fraction));
        int newPercent = Math.round(fraction * 100);

        if (newPercent != progress) {
            progress = newPercent;
            invalidate();
            if (listener != null) {
                listener.onVolumeChanged(progress, true);
            }
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
