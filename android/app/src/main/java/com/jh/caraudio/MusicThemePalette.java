package com.jh.caraudio;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Paleta de colores y generador de fondos ligeros para JH MUSIC:
 * - Light Mode (Daylight Cockpit): blanco porcelana, gris hielo, Azul Cobalto (#2563EB) y Turquesa (#0D9488) para Automix.
 * - Dark Mode (Obsidian Modern): negro obsidiana, cian (#38BDF8) y magenta (#F43F5E) para Automix.
 *
 * Incluye sincronización bidireccional de tema con JH Launcher vía archivo compartido (.jh_theme_state)
 * y broadcast explícito (com.jh.action.THEME_CHANGED).
 */
final class MusicThemePalette {

    static final String ACTION_THEME_CHANGED = "com.jh.action.THEME_CHANGED";
    static final String EXTRA_LIGHT_THEME = "extra_light_theme";
    static final String SHARED_THEME_FILE = ".jh_theme_state";
    static final String LAUNCHER_PKG = "com.jh.launcher";

    final boolean light;
    final float density;

    final int rootBase;
    final int stripBg;
    final int panelBg;
    final int cardBg;
    final int surface2;
    final int surface3;
    final int border;
    final int borderSoft;

    final int accent;
    final int accentSoft;
    final int accentBorder;
    final int mixAccent;
    final int mixSoft;
    final int mixBorder;

    final int emerald;
    final int emeraldSoft;
    final int emeraldBorder;
    final int crimson;

    final int text1;
    final int text2;
    final int textMuted;

    final int playBg;
    final int playIcon;
    final int skipColor;
    final int trackBg;
    final int menuBackdrop;

    MusicThemePalette(boolean light, float density) {
        this.light = light;
        this.density = density;

        if (light) {
            rootBase = 0xFFE8EEF6;
            stripBg = 0xFFF4F7FB;
            panelBg = 0xFFFFFFFF;
            cardBg = 0xFFF7FAFE;
            surface2 = 0xFFEEF2F8;
            surface3 = 0xFFE2E8F0;
            border = 0xFFD8E1EC;
            borderSoft = 0xFFE6EDF5;

            accent = 0xFF1D4ED8;
            accentSoft = 0xFFDBEAFE;
            accentBorder = 0xFF93C5FD;
            mixAccent = 0xFF7C3AED;
            mixSoft = 0xFFF5F3FF;
            mixBorder = 0xFFA78BFA;

            emerald = 0xFF059669;
            emeraldSoft = 0x1F059669;
            emeraldBorder = 0x60059669;
            crimson = 0xFFEF4444;

            text1 = 0xFF0F172A;
            text2 = 0xFF64748B;
            textMuted = 0xFF94A3B8;

            playBg = 0xFFDBEAFE;
            playIcon = 0xFF1D4ED8;
            skipColor = 0xFF334155;
            trackBg = 0xFFCBD5E1;
            menuBackdrop = 0x800F172A;
        } else {
            rootBase = 0xFF07090C;
            stripBg = 0xFF0C0E12;
            panelBg = 0xEB101318;
            cardBg = 0xFF141922;
            surface2 = 0xFF151B26;
            surface3 = 0xFF1E2636;
            border = 0x18FFFFFF;
            borderSoft = 0x10FFFFFF;

            accent = 0xFF38BDF8;
            accentSoft = 0x2438BDF8;
            accentBorder = 0x7338BDF8;
            mixAccent = 0xFFF43F5E;
            mixSoft = 0x24F43F5E;
            mixBorder = 0x73F43F5E;

            emerald = 0xFF10B981;
            emeraldSoft = 0x2410B981;
            emeraldBorder = 0x6B10B981;
            crimson = 0xFFF43F5E;

            text1 = 0xFFF1F5F9;
            text2 = 0xFF94A3B8;
            textMuted = 0xFF64748B;

            playBg = 0xFFFFFFFF;
            playIcon = 0xFF090B0E;
            skipColor = 0xFFF1F5F9;
            trackBg = 0x29FFFFFF;
            menuBackdrop = 0x8C000000;
        }
    }

    int dp(float v) {
        return Math.max(1, Math.round(v * density));
    }

    GradientDrawable box(int fill, int strokeColor, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(fill);
        d.setCornerRadius(radiusDp * density);
        if (strokeColor != 0) d.setStroke(dp(1f), strokeColor);
        return d;
    }

    GradientDrawable circle(int fill, int strokeColor, float strokeDp) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(fill);
        if (strokeColor != 0 && strokeDp > 0f) d.setStroke(dp(strokeDp), strokeColor);
        return d;
    }

    Drawable actionBtnBg() {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_pressed}, box(accentSoft, accent, 12f));
        sld.addState(new int[]{}, box(surface2, borderSoft, 12f));
        return sld;
    }

    Drawable adjCardBg() {
        StateListDrawable sld = new StateListDrawable();
        int normalFill = light ? panelBg : cardBg;
        sld.addState(new int[]{android.R.attr.state_pressed}, box(accentSoft, accentBorder, 14f));
        sld.addState(new int[]{}, box(normalFill, border, 14f));
        return sld;
    }

    Drawable skipBtnBg() {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_pressed}, circle(accentSoft, accentBorder, 1.5f));
        sld.addState(new int[]{android.R.attr.state_activated}, circle(accentSoft, accentBorder, 1.5f));
        sld.addState(new int[]{}, circle(0x00000000, 0, 0f));
        return sld;
    }

    Drawable playBtnBg() {
        StateListDrawable sld = new StateListDrawable();
        if (light) {
            sld.addState(new int[]{android.R.attr.state_pressed}, circle(0xFFBFDBFE, accent, 2f));
            sld.addState(new int[]{android.R.attr.state_activated}, circle(0xFFBFDBFE, accent, 2f));
            sld.addState(new int[]{}, circle(playBg, accentBorder, 2f));
        } else {
            sld.addState(new int[]{android.R.attr.state_pressed}, circle(0xFFE2E8F0, 0, 0f));
            sld.addState(new int[]{android.R.attr.state_activated}, circle(0xFFE2E8F0, 0, 0f));
            sld.addState(new int[]{}, circle(playBg, 0, 0f));
        }
        return sld;
    }

    Drawable tabBg(boolean selected) {
        if (selected) {
            return light ? box(accentSoft, accentBorder, 11f) : box(accent, accent, 11f);
        }
        return box(surface2, borderSoft, 11f);
    }

    int tabTextColor(boolean selected) {
        if (selected) {
            return light ? accent : 0xFF06090F;
        }
        return text2;
    }

    /** Vinilo protagonista para el placeholder cuando la canción no tiene carátula. */
    static Bitmap createVinylBitmap(int sizePx, boolean lightMode) {
        Bitmap bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        float s = sizePx / 120f;
        c.scale(s, s);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setStyle(Paint.Style.FILL);
        Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);

        fill.setShader(new RadialGradient(60f, 60f, 57f,
                new int[]{0xFF1E293B, 0xFF0D111A, 0xFF05070B},
                new float[]{0f, 0.68f, 1f}, Shader.TileMode.CLAMP));
        c.drawCircle(60, 60, 57, fill);
        fill.setShader(null);

        int rimColor = lightMode ? Color.argb(115, 37, 99, 235) : Color.argb(145, 56, 189, 248);
        int grooveAccent = lightMode ? Color.argb(95, 59, 130, 246) : Color.argb(110, 56, 189, 248);
        drawRing(c, stroke, 57f, 1.5f, rimColor, null);
        drawRing(c, stroke, 49f, 1.0f, Color.argb(42, 255, 255, 255), null);
        drawRing(c, stroke, 42f, 1.2f, Color.argb(38, 255, 255, 255), new float[]{16, 5, 10, 4});
        drawRing(c, stroke, 35f, 1.2f, grooveAccent, new float[]{22, 7, 9, 5});
        drawRing(c, stroke, 28f, 1.0f, Color.argb(36, 255, 255, 255), null);

        fill.setColor(Color.argb(38, 255, 255, 255));
        RectF disc = new RectF(3, 3, 117, 117);
        Path beam = new Path();
        beam.moveTo(60, 60);
        beam.lineTo(17, 17);
        beam.arcTo(disc, 225f, 34f);
        beam.close();
        beam.moveTo(60, 60);
        beam.lineTo(103, 103);
        beam.arcTo(disc, 45f, 34f);
        beam.close();
        c.drawPath(beam, fill);

        fill.setColor(lightMode ? 0xFF2563EB : 0xFF0066FF);
        c.drawCircle(60, 60, 19f, fill);
        drawRing(c, stroke, 19f, 2f, lightMode ? 0xFF60A5FA : 0xFF38BDF8, null);
        drawRing(c, stroke, 13f, 1f, Color.argb(85, 255, 255, 255), null);
        fill.setColor(0xFF0B0F17);
        c.drawCircle(60, 60, 4.5f, fill);
        drawRing(c, stroke, 4.5f, 1.2f, Color.WHITE, null);
        return bmp;
    }

    private static void drawRing(Canvas c, Paint p, float r, float w, int color, float[] dash) {
        p.setStrokeWidth(w);
        p.setColor(color);
        p.setPathEffect(dash != null ? new DashPathEffect(dash, 0) : null);
        c.drawCircle(60, 60, r, p);
    }

    /** Lee el tema compartido (.jh_theme_state); null si aún no existe. */
    static Boolean readSharedTheme() {
        try {
            File f = new File(Environment.getExternalStorageDirectory(), SHARED_THEME_FILE);
            if (!f.exists() || f.length() == 0 || f.length() > 64) return null;
            try (FileInputStream fis = new FileInputStream(f)) {
                byte[] buf = new byte[(int) f.length()];
                int n = fis.read(buf);
                if (n <= 0) return null;
                String val = new String(buf, 0, n, StandardCharsets.UTF_8).trim();
                if ("light".equalsIgnoreCase(val)) return Boolean.TRUE;
                if ("dark".equalsIgnoreCase(val)) return Boolean.FALSE;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    /** Guarda el tema compartido (.jh_theme_state) y avisa a JH Launcher. */
    static void broadcastAndSaveTheme(Context ctx, boolean light) {
        try {
            File f = new File(Environment.getExternalStorageDirectory(), SHARED_THEME_FILE);
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write((light ? "light" : "dark").getBytes(StandardCharsets.UTF_8));
            }
        } catch (Throwable ignored) {}
        try {
            Intent intent = new Intent(ACTION_THEME_CHANGED);
            intent.setPackage(LAUNCHER_PKG);
            intent.putExtra(EXTRA_LIGHT_THEME, light);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            ctx.sendBroadcast(intent);
        } catch (Throwable ignored) {}
    }
}
