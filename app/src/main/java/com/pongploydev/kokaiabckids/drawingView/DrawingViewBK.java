package com.pongploydev.kokaiabckids.drawingView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.pongploydev.kokaiabckids.R;

/**
 * Created by Priyank(@priyankvex) on 5/9/15.
 *
 * Class which provides the view on which drawing takes place.
 */
public class DrawingViewBK extends View{
    private int getheight;
    private int getWidth;

    private int getheight_image;
    private int getWidth_image;

    // To hold the path that will be drawn.
    private Path drawPath;
    // Paint object to draw drawPath and drawCanvas.
    private Paint drawPaint, canvasPaint;
    // initial color
    private int paintColor = 0xff000000;
    private int previousColor = paintColor;
    // canvas on which drawing takes place.
    private Canvas drawCanvas;
    // canvas bitmap
    private Bitmap canvasBitmap;
    // Brush stroke width
    private float brushSize, lastBrushSize;
    // To enable and disable erasing mode.
    private boolean erase = false;

    private int next_image;
    private int black_image;

    private static final String TAG = "DrawingView";

    public DrawingViewBK(Context context, AttributeSet attrs){
        super(context, attrs);
        setUpDrawing();
    }

    /**
     * Initialize all objects required for drawing here.
     * One time initialization reduces resource consumption.
     */
    private void setUpDrawing(){
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        // Making drawing smooth.
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        // Initial brush size is medium.
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
        drawPaint.setStrokeWidth(brushSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // ตรวจสอบว่า width และ height มากกว่า 0
        if (w <= 0 || h <= 0) {
            Log.w(TAG, "Invalid dimensions: " + w + "x" + h);
            // ใช้ขนาด default ขั้นต่ำ
            w = Math.max(1, w);
            h = Math.max(1, h);
        }

        try {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);

            getWidth = w;
            getheight = h;
            getWidth_image = w;
            getheight_image = h;

            Log.d(TAG, "Canvas created with dimensions: " + w + "x" + h);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error creating bitmap: " + e.getMessage());
            // สร้าง bitmap ขนาด default ถ้าเกิด error
            int defaultSize = 100;
            canvasBitmap = Bitmap.createBitmap(defaultSize, defaultSize, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            getWidth = defaultSize;
            getheight = defaultSize;
            getWidth_image = defaultSize;
            getheight_image = defaultSize;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory error: " + e.getMessage());
            // สร้าง bitmap ขนาดเล็กลง
            int smallSize = 50;
            canvasBitmap = Bitmap.createBitmap(smallSize, smallSize, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            getWidth = smallSize;
            getheight = smallSize;
            getWidth_image = smallSize;
            getheight_image = smallSize;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // ตรวจสอบว่า canvasBitmap ไม่เป็น null
        if (canvasBitmap != null && !canvasBitmap.isRecycled()) {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
            canvas.drawPath(drawPath, drawPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // ตรวจสอบว่า drawCanvas พร้อมใช้งาน
        if (drawCanvas == null) {
            return false;
        }

        // X and Y position of user touch.
        float touchX = event.getX();
        float touchY = event.getY();
        // Draw the path according to the touch event taking place.
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                if (erase){
                    drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                }
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                drawPaint.setXfermode(null);
                break;
            default:
                return false;
        }

        // invalidate the view so that canvas is redrawn.
        invalidate();
        return true;
    }

    public void setColor(String newColor){
        // invalidate the view
        invalidate();
        try {
            paintColor = Color.parseColor(newColor);
            drawPaint.setColor(paintColor);
            previousColor = paintColor;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid color: " + newColor);
            // ใช้สี default ถ้า color ไม่ถูกต้อง
            paintColor = Color.BLACK;
            drawPaint.setColor(paintColor);
            previousColor = paintColor;
        }
    }

    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize = pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize = lastSize;
    }

    public float getLastBrushSize(){
        return lastBrushSize;
    }

    public void setNextImage(int nextImage){
        next_image = nextImage;
        setUpDrawing();
    }

    public float getNextImag(){
        return next_image;
    }

    public void setErase(boolean isErase){
        //set erase true or false
        erase = isErase;
        if(erase) {
            drawPaint.setColor(Color.WHITE);
            //drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        else {
            drawPaint.setColor(previousColor);
            drawPaint.setXfermode(null);
        }
    }

    public void startNew(){
        if (drawCanvas != null) {
            drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        }
        invalidate();
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        // ตรวจสอบ parameters
        if (bm == null || bm.isRecycled()) {
            return null;
        }

        if (newWidth <= 0 || newHeight <= 0) {
            Log.w(TAG, "Invalid resize dimensions: " + newWidth + "x" + newHeight);
            return bm;
        }

        int width = bm.getWidth();
        int height = bm.getHeight();

        if (width <= 0 || height <= 0) {
            Log.w(TAG, "Invalid bitmap dimensions: " + width + "x" + height);
            return bm;
        }

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        try {
            // "RECREATE" THE NEW BITMAP
            Bitmap resizedBitmap = Bitmap.createBitmap(
                    bm, 0, 0, width, height, matrix, false);
            return resizedBitmap;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Error resizing bitmap: " + e.getMessage());
            return bm;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory while resizing bitmap: " + e.getMessage());
            return bm;
        }
    }

    // Method สำหรับเคลียร์ resources
    public void cleanup() {
        if (canvasBitmap != null && !canvasBitmap.isRecycled()) {
            canvasBitmap.recycle();
            canvasBitmap = null;
        }
        drawCanvas = null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cleanup();
    }
}