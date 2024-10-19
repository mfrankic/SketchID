package com.mfrankic.sketchid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class CustomDrawingView extends View {

    private Path drawPath;
    private Paint drawPaint;
    private Paint canvasPaint;
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;

    private DrawingDataDao drawingDataDao;
    private TextView pressureDisplay;
    private int userId;
    private int imageId;

    public CustomDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    public void init(DrawingDataDao drawingDataDao, TextView pressureDisplay, int userId, int imageId, OnValidationCompleteListener listener) {
        this.drawingDataDao = drawingDataDao;
        this.pressureDisplay = pressureDisplay;
        this.userId = userId;
        this.imageId = imageId;

        // Validate user and image IDs asynchronously
        AsyncTask.execute(() -> {
            boolean isValid = validateForeignKeys();
            if (!isValid) {
                Log.e("InitializationError", "Invalid foreign keys: userId=" + userId + ", imageId=" + imageId);
            }
            // Notify the listener of the validation result on the main thread
            post(() -> listener.onValidationComplete(isValid));
        });

        setupDrawing(); // Initialize drawing setup
    }

    private void setupDrawing() {
        drawPath = new Path();  // Initialize the drawPath here
        drawPaint = new Paint();
        drawPaint.setColor(Color.BLACK);
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // Check if the drawPath is initialized before drawing
        if (drawPath != null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
            canvas.drawPath(drawPath, drawPaint);  // Draw the path on the canvas
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        float pressure = event.getPressure();
        long timestamp = event.getEventTime();

        drawPaint.setStrokeWidth(pressure * 20);

        if (pressureDisplay != null) {
            pressureDisplay.setText(String.format("Line Thickness: %spx", pressure * 20));
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (drawPath == null) {
                    drawPath = new Path();  // Ensure drawPath is initialized before use
                }
                drawPath.moveTo(touchX, touchY);
                saveDrawingData(timestamp, touchX, touchY, pressure);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                saveDrawingData(timestamp, touchX, touchY, pressure);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();  // Reset the path after the user lifts their finger
                performClick();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private void saveDrawingData(final long timestamp, final float x, final float y, final float pressure) {
        if (drawingDataDao != null) {
            AsyncTask.execute(() -> {
                DrawingData data = new DrawingData(timestamp, x, y, pressure, userId, imageId);
                drawingDataDao.insertDrawingData(data);
            });
        }
    }

    private boolean validateForeignKeys() {
        UserDao userDao = AppDatabase.getInstance(getContext()).userDao();
        User user = userDao.getUserById(userId);  // Query for user

        ImageDao imageDao = AppDatabase.getInstance(getContext()).imageDao();
        Image image = imageDao.getImageById(imageId);  // Query for image

        if (user == null) {
            Log.e("ForeignKeyError", "User with ID " + userId + " does not exist.");
            return false;
        }

        if (image == null) {
            Log.e("ForeignKeyError", "Image with ID " + imageId + " does not exist.");
            return false;
        }

        return true;
    }

    public void clearCanvas() {
        drawCanvas.drawColor(Color.WHITE);
        drawPath = new Path();  // Reinitialize the path after clearing the canvas
        invalidate();
    }
}
