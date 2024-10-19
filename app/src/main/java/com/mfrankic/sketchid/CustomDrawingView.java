package com.mfrankic.sketchid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

public class CustomDrawingView extends View {

    private Path drawPath;
    private Paint drawPaint;
    private Paint canvasPaint;
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;
    private OnStrokeListener onStrokeListener;

    private final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

    public interface OnStrokeListener {
        void onStroke(float x, float y, long timestamp, String action);
    }

    public void setOnStrokeListener(OnStrokeListener listener) {
        this.onStrokeListener = listener;
    }

    public CustomDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(Color.BLACK);
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setStrokeWidth(5 * displayMetrics.density);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);

        int minSize = (int) (300 * displayMetrics.density);
        int maxSize = (int) (500 * displayMetrics.density);

        size = Math.max(minSize, Math.min(size, maxSize));

        int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(finalMeasureSpec, finalMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        Drawable grid = ResourcesCompat.getDrawable(getResources(), R.drawable.grid, null);
        if (grid != null) {
            grid.setAlpha(50);
        }
        setBackground(grid);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        long timestamp = event.getEventTime();

        float normalizedX = touchX / getWidth();
        float normalizedY = touchY / getHeight();

        if (normalizedX < 0.0f || normalizedX > 1.0f || normalizedY < 0.0f || normalizedY > 1.0f) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                onStrokeListener.onStroke(normalizedX, normalizedY, timestamp, "ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                onStrokeListener.onStroke(normalizedX, normalizedY, timestamp, "ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                drawPath.lineTo(touchX, touchY);
                drawCanvas.drawPath(drawPath, drawPaint);
                onStrokeListener.onStroke(normalizedX, normalizedY, timestamp, "ACTION_UP");
                drawPath.reset();
                performClick();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }
}
