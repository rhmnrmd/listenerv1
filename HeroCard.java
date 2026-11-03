package id.garrypedia.listener;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;

/** Kartu ungu bergrid dengan bingkai putus-putus, sama seperti .hero-card di web. */
final class HeroCard extends LinearLayout {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grid = new Paint();
    private final Paint dash = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clip = new Path();
    private final RectF rect = new RectF();

    HeroCard(Context c) {
        super(c);
        setOrientation(VERTICAL);
        setWillNotDraw(false);
        grid.setColor(0x12FFFFFF);
        grid.setStrokeWidth(1f);
        dash.setStyle(Paint.Style.STROKE);
        dash.setColor(0x55FFFFFF);
        dash.setStrokeWidth(Ui.dp(c, 1));
        dash.setPathEffect(new DashPathEffect(new float[]{Ui.dp(c, 4), Ui.dp(c, 4)}, 0));
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setRoundRect(0, 0, v.getWidth(), v.getHeight(), Ui.dp(v.getContext(), 28));
            }
        });
        setElevation(Ui.dp(c, 6));
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                setOutlineSpotShadowColor(0xFF6D28D9);
                setOutlineAmbientShadowColor(0x556D28D9);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        float r = Ui.dp(getContext(), 28);
        rect.set(0, 0, w, h);
        clip.reset();
        clip.addRoundRect(rect, r, r, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(clip);
        fill.setShader(new LinearGradient(0, 0, w, h, Ui.GRAD, new float[]{0f, 0.65f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, fill);
        float step = Ui.dp(getContext(), 26);
        for (float x = 0; x < w; x += step) canvas.drawLine(x, 0, x, h, grid);
        for (float y = 0; y < h; y += step) canvas.drawLine(0, y, w, y, grid);
        canvas.restore();
        float in = Ui.dp(getContext(), 10), rr = Ui.dp(getContext(), 20);
        rect.set(in, in, w - in, h - in);
        canvas.drawRoundRect(rect, rr, rr, dash);
    }
}
