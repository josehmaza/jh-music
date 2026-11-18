package com.jh.caraudio;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Sombra y resplandor detrás del cuadro de la carátula.
 * En Light Mode suaviza la sombra oscura para que la tarjeta blanca flote limpia.
 */
public class AlbumGlowView extends View {

    private static final float OUTSET_DP = 48f;
    private static final float CORNER_DP = 20f;

    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nearPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint widePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;
    private int glowColor = Color.rgb(56, 189, 248);
    private boolean lightMode = false;

    public AlbumGlowView(Context context) {
        this(context, null);
    }

    public AlbumGlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        shadowPaint.setMaskFilter(new BlurMaskFilter(24 * density, BlurMaskFilter.Blur.NORMAL));
        nearPaint.setMaskFilter(new BlurMaskFilter(22 * density, BlurMaskFilter.Blur.NORMAL));
        widePaint.setMaskFilter(new BlurMaskFilter(32 * density, BlurMaskFilter.Blur.NORMAL));
        updatePaints();
    }

    public void setLightMode(boolean light) {
        if (this.lightMode == light) return;
        this.lightMode = light;
        updatePaints();
        invalidate();
    }

    public void setGlowColor(int color) {
        if (glowColor == color) return;
        glowColor = color;
        updatePaints();
        invalidate();
    }

    private void updatePaints() {
        int r = Color.red(glowColor), g = Color.green(glowColor), b = Color.blue(glowColor);
        shadowPaint.setColor(lightMode ? Color.argb(42, 15, 23, 42) : Color.argb(179, 0, 0, 0));
        nearPaint.setColor(Color.argb(lightMode ? 68 : 140, r, g, b));
        widePaint.setColor(Color.argb(lightMode ? 36 : 77, r, g, b));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float outset = OUTSET_DP * density;
        float left = outset, top = outset;
        float right = getWidth() - outset, bottom = getHeight() - outset;
        if (right <= left || bottom <= top) return;
        float corner = CORNER_DP * density;

        float wide = 10 * density;
        rect.set(left - wide, top - wide, right + wide, bottom + wide);
        canvas.drawRoundRect(rect, corner + wide, corner + wide, widePaint);

        float drop = 12 * density;
        rect.set(left, top + drop, right, bottom + drop);
        canvas.drawRoundRect(rect, corner, corner, shadowPaint);

        float near = 2 * density;
        rect.set(left - near, top - near, right + near, bottom + near);
        canvas.drawRoundRect(rect, corner + near, corner + near, nearPaint);
    }
}
