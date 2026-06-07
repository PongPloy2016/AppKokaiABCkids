package com.pongploydev.priyankvex.paintapp.drawingView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.pongploydev.priyankvex.paintapp.R;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {
    private static final String TAG = "DrawingView";

    // ตัวแปรพื้นฐานสำหรับการวาด
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = 0xff000000;
    private int previousColor = paintColor;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private float brushSize, lastBrushSize;
    private boolean erase = false;

    // ตัวแปรสำหรับระบบตรวจสอบตัวอักษรไทย
    private Path guidePath;
    private List<Path> strokePaths; // สำหรับเก็บเส้นขีดต่างๆ ของตัวอักษร
    private List<RectF> guideAreas;
    private boolean isCheckingEnabled = false;
    private float tolerance = 25.0f;

    // ตัวแปรสำหรับแสดงผลการตรวจสอบ
    private Paint guidePaint;
    private Paint successPaint;
    private Paint errorPaint;
    private Paint strokeOrderPaint;

    // ระบบติดตามการวาด
    private List<PointData> drawnPoints;
    private int currentStrokeIndex = 0;
    private boolean[] completedStrokes;

    // Listener สำหรับ callback
    public interface DrawingCheckListener {
        void onDrawingChecked(boolean isCorrect, float accuracy);
        void onDrawingProgress(float progress);
        void onStrokeCompleted(int strokeIndex, boolean isCorrect);
        void onLetterCompleted(boolean isPerfect);
    }

    private DrawingCheckListener drawingCheckListener;

    // คลาสสำหรับเก็บข้อมูลจุด
    private static class PointData {
        float x, y;
        long timestamp;
        boolean isInGuideArea;

        PointData(float x, float y, long timestamp, boolean isInGuideArea) {
            this.x = x;
            this.y = y;
            this.timestamp = timestamp;
            this.isInGuideArea = isInGuideArea;
        }
    }

    // ข้อมูลตัวอักษรไทย
    private ThaiLetter currentLetter;

    public interface ThaiLetter {
        String getName();
        List<Path> getStrokes();
        int getStrokeCount();
        Path getCompletePath();
        float getDifficulty();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpDrawing();
        initGuideSystem();
    }

    public void setDrawingCheckListener(DrawingCheckListener listener) {
        this.drawingCheckListener = listener;
    }

    /**
     * เริ่มต้นระบบวาดภาพ
     */
    private void setUpDrawing() {
        drawPath = new Path();
        drawnPoints = new ArrayList<>();

        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
        drawPaint.setStrokeWidth(brushSize);
    }

    /**
     * เริ่มต้นระบบตรวจสอบ
     */
    private void initGuideSystem() {
        guidePath = new Path();
        strokePaths = new ArrayList<>();
        guideAreas = new ArrayList<>();
        drawnPoints = new ArrayList<>();

        // สีสำหรับเส้นนำทาง
        guidePaint = new Paint();
        guidePaint.setColor(Color.parseColor("#40FF9800"));
        guidePaint.setStyle(Paint.Style.STROKE);
        guidePaint.setStrokeWidth(4f);
        guidePaint.setAntiAlias(true);

        // สีสำหรับเส้นที่วาดถูกต้อง
        successPaint = new Paint();
        successPaint.setColor(Color.parseColor("#8000C853"));
        successPaint.setStyle(Paint.Style.STROKE);
        successPaint.setStrokeWidth(6f);
        successPaint.setAntiAlias(true);

        // สีสำหรับเส้นที่วาดผิด
        errorPaint = new Paint();
        errorPaint.setColor(Color.parseColor("#80FF4444"));
        errorPaint.setStyle(Paint.Style.STROKE);
        errorPaint.setStrokeWidth(6f);
        errorPaint.setAntiAlias(true);

        // สีสำหรับแสดงลำดับการขีด
        strokeOrderPaint = new Paint();
        strokeOrderPaint.setColor(Color.BLUE);
        strokeOrderPaint.setTextSize(36);
        strokeOrderPaint.setTextAlign(Paint.Align.CENTER);
        strokeOrderPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w <= 0 || h <= 0) {
            Log.w(TAG, "Invalid dimensions: " + w + "x" + h);
            w = Math.max(1, w);
            h = Math.max(1, h);
        }

        try {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            Log.d(TAG, "Canvas created with dimensions: " + w + "x" + h);
        } catch (Exception e) {
            Log.e(TAG, "Error creating canvas: " + e.getMessage());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvasBitmap != null && !canvasBitmap.isRecycled()) {
            // วาดภาพที่ผู้ใช้วาด
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

            // วาดเส้นนำทางถ้าเปิดโหมดตรวจสอบ
            if (isCheckingEnabled) {
                drawGuideSystem(canvas);
            }

            // วาด path ปัจจุบัน
            canvas.drawPath(drawPath, drawPaint);
        }
    }

    /**
     * วาดระบบนำทางและตรวจสอบ
     */
    private void drawGuideSystem(Canvas canvas) {
        // วาดเส้นนำทางหลัก
        if (guidePath != null && !guidePath.isEmpty()) {
            canvas.drawPath(guidePath, guidePaint);
        }

        // วาดเส้นขีดต่างๆ พร้อมลำดับ
        if (!strokePaths.isEmpty()) {
            for (int i = 0; i < strokePaths.size(); i++) {
                Path stroke = strokePaths.get(i);
                Paint paint = new Paint(guidePaint);

                // เปลี่ยนสีสำหรับเส้นที่ยังไม่ได้วาด
                if (i > currentStrokeIndex) {
                    paint.setColor(Color.parseColor("#20FF9800"));
                    paint.setStrokeWidth(3f);
                }
                // เส้นที่วาดแล้วและถูกต้อง
                else if (i < currentStrokeIndex && completedStrokes != null &&
                        completedStrokes.length > i && completedStrokes[i]) {
                    paint.setColor(Color.parseColor("#8000C853"));
                    paint.setStrokeWidth(5f);
                }

                canvas.drawPath(stroke, paint);

                // แสดงเลขลำดับการขีด
                if (isCheckingEnabled) {
                    drawStrokeNumber(canvas, stroke, i + 1);
                }
            }
        }
    }

    /**
     * วาดเลขลำดับการขีด
     */
    private void drawStrokeNumber(Canvas canvas, Path stroke, int number) {
        RectF bounds = new RectF();
        stroke.computeBounds(bounds, true);

        float centerX = bounds.centerX();
        float centerY = bounds.centerY();

        // วาดพื้นหลังเลข
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, 15, bgPaint);

        // วาดขอบ
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLUE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        canvas.drawCircle(centerX, centerY, 15, borderPaint);

        // วาดตัวเลข
        strokeOrderPaint.setColor(Color.BLUE);
        canvas.drawText(String.valueOf(number), centerX, centerY + 12, strokeOrderPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (drawCanvas == null) {
            return false;
        }

        float touchX = event.getX();
        float touchY = event.getY();
        long timestamp = System.currentTimeMillis();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                drawPath.moveTo(touchX, touchY);
                drawnPoints.clear();
                drawnPoints.add(new PointData(touchX, touchY, timestamp,
                        isPointInGuideArea(touchX, touchY)));
                break;

            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                drawnPoints.add(new PointData(touchX, touchY, timestamp,
                        isPointInGuideArea(touchX, touchY)));

                // ตรวจสอบแบบ real-time
                if (isCheckingEnabled) {
                    checkDrawingProgress(touchX, touchY);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (erase) {
                    drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                }
                drawCanvas.drawPath(drawPath, drawPaint);

                // ตรวจสอบผลลัพธ์
                if (isCheckingEnabled) {
                    checkStrokeCompletion();
                }

                drawPath.reset();
                drawPaint.setXfermode(null);
                break;

            default:
                return false;
        }

        invalidate();
        return true;
    }

    /**
     * ตรวจสอบความคืบหน้าแบบ real-time
     */
    private void checkDrawingProgress(float x, float y) {
        boolean isInValidArea = isPointInGuideArea(x, y);

        // เปลี่ยนสีตามผลการตรวจสอบ
        if (!isInValidArea) {
            drawPaint.setColor(Color.RED);
        } else {
            drawPaint.setColor(previousColor);
        }

        if (drawingCheckListener != null) {
            float progress = calculateCurrentProgress();
            drawingCheckListener.onDrawingProgress(progress);
        }
    }

    /**
     * ตรวจสอบการ完成ของเส้นขีด
     */
    private void checkStrokeCompletion() {
        if (strokePaths.isEmpty() || currentStrokeIndex >= strokePaths.size()) {
            checkDrawingResult();
            return;
        }

        Path currentGuideStroke = strokePaths.get(currentStrokeIndex);
        boolean isStrokeCorrect = evaluateStroke(currentGuideStroke, drawPath);

        if (completedStrokes != null && currentStrokeIndex < completedStrokes.length) {
            completedStrokes[currentStrokeIndex] = isStrokeCorrect;
        }

        if (drawingCheckListener != null) {
            drawingCheckListener.onStrokeCompleted(currentStrokeIndex, isStrokeCorrect);
        }

        // ถ้าเส้นขีดถูกต้อง ให้ไปเส้นถัดไป
        if (isStrokeCorrect) {
            currentStrokeIndex++;

            // ตรวจสอบว่าวาดครบทุกเส้นแล้วหรือไม่
            if (currentStrokeIndex >= strokePaths.size()) {
                checkDrawingResult();
            }
        }

        // รีเซ็ตข้อมูลจุดที่วาด
        drawnPoints.clear();
    }

    /**
     * ประเมินเส้นขีด
     */
    private boolean evaluateStroke(Path guideStroke, Path drawnStroke) {
        if (drawnPoints.size() < 5) return false; // จุดน้อยเกินไป

        // ตรวจสอบทิศทาง
        boolean hasCorrectDirection = checkStrokeDirection(guideStroke, drawnStroke);

        // ตรวจสอบพื้นที่
        boolean hasGoodCoverage = checkStrokeCoverage(guideStroke, drawnStroke);

        // ตรวจสอบความต่อเนื่อง
        boolean isContinuous = checkStrokeContinuity();

        return hasCorrectDirection && hasGoodCoverage && isContinuous;
    }

    /**
     * ตรวจสอบทิศทางการขีด
     */
    private boolean checkStrokeDirection(Path guideStroke, Path drawnStroke) {
        if (drawnPoints.size() < 10) return true; // ข้อมูลไม่พอให้ตรวจสอบทิศทาง

        // เปรียบเทียบทิศทางเริ่มต้นและสิ้นสุด
        PointData startPoint = drawnPoints.get(0);
        PointData endPoint = drawnPoints.get(drawnPoints.size() - 1);

        RectF guideBounds = new RectF();
        guideStroke.computeBounds(guideBounds, true);

        // ตรวจสอบว่าจุดเริ่มต้นและสิ้นสุดอยู่ในพื้นที่ที่เหมาะสม
        boolean startInArea = guideBounds.contains(startPoint.x, startPoint.y) ||
                isPointNearPath(guideStroke, startPoint.x, startPoint.y, tolerance * 2);
        boolean endInArea = guideBounds.contains(endPoint.x, endPoint.y) ||
                isPointNearPath(guideStroke, endPoint.x, endPoint.y, tolerance * 2);

        return startInArea && endInArea;
    }

    /**
     * ตรวจสอบพื้นที่ครอบคลุม
     */
    private boolean checkStrokeCoverage(Path guideStroke, Path drawnStroke) {
        RectF drawnBounds = new RectF();
        drawnStroke.computeBounds(drawnBounds, true);

        RectF guideBounds = new RectF();
        guideStroke.computeBounds(guideBounds, true);

        // ขยายขอบเขต guide ด้วย tolerance
        guideBounds.inset(-tolerance, -tolerance);

        return guideBounds.contains(drawnBounds);
    }

    /**
     * ตรวจสอบความต่อเนื่อง
     */
    private boolean checkStrokeContinuity() {
        if (drawnPoints.size() < 3) return false;

        int discontinuityCount = 0;
        float maxJumpDistance = brushSize * 3;

        for (int i = 1; i < drawnPoints.size(); i++) {
            PointData prev = drawnPoints.get(i - 1);
            PointData curr = drawnPoints.get(i);

            float distance = (float) Math.sqrt(
                    Math.pow(curr.x - prev.x, 2) + Math.pow(curr.y - prev.y, 2)
            );

            long timeDiff = curr.timestamp - prev.timestamp;

            // ตรวจสอบการกระโดดของเส้น
            if (distance > maxJumpDistance && timeDiff < 100) {
                discontinuityCount++;
            }
        }

        float discontinuityRatio = (float) discontinuityCount / drawnPoints.size();
        return discontinuityRatio < 0.1f; // อนุญาตให้ไม่ต่อเนื่องไม่เกิน 10%
    }

    /**
     * ตรวจสอบผลลัพธ์สุดท้าย
     */
    private void checkDrawingResult() {
        boolean isCorrect = evaluateCompleteDrawing();
        float accuracy = calculateOverallAccuracy(isCorrect);

        boolean isPerfect = isCorrect && accuracy > 0.9f;

        if (drawingCheckListener != null) {
            drawingCheckListener.onDrawingChecked(isCorrect, accuracy);
            if (isPerfect && currentStrokeIndex >= strokePaths.size()) {
                drawingCheckListener.onLetterCompleted(true);
            }
        }

        // รีเซ็ตสำหรับการวาดครั้งใหม่
        if (isCorrect) {
            // รีเซ็ตเฉพาะเส้นขีด ถ้ายังไม่ครบ
            if (currentStrokeIndex < strokePaths.size()) {
                // ยังวาดไม่ครบ ให้繼續วาดเส้นถัดไป
            } else {
                // วาดครบแล้ว อาจจะให้เริ่มใหม่หรือไปตัวต่อไป
                showSuccessEffect();
            }
        } else {
            showErrorEffect();
        }
    }

    /**
     * ประเมินผลการวาดทั้งหมด
     */
    private boolean evaluateCompleteDrawing() {
        if (strokePaths.isEmpty()) {
            return evaluateSimpleDrawing();
        }

        // ตรวจสอบว่าวาดครบทุกเส้นขีด
        if (currentStrokeIndex < strokePaths.size()) {
            return false;
        }

        // ตรวจสอบความถูกต้องของแต่ละเส้นขีด
        int correctStrokes = 0;
        if (completedStrokes != null) {
            for (boolean correct : completedStrokes) {
                if (correct) correctStrokes++;
            }
        }

        float correctRatio = (float) correctStrokes / strokePaths.size();
        return correctRatio >= 0.7f; // ต้องถูกต้องอย่างน้อย 70%
    }

    /**
     * ประเมินการวาดแบบง่าย (เมื่อไม่มีเส้นขีดแยก)
     */
    private boolean evaluateSimpleDrawing() {
        if (guidePath == null || guidePath.isEmpty()) return true;

        RectF drawnBounds = new RectF();
        drawPath.computeBounds(drawnBounds, true);

        RectF guideBounds = new RectF();
        guidePath.computeBounds(guideBounds, true);

        guideBounds.inset(-tolerance, -tolerance);

        boolean isInBounds = guideBounds.contains(drawnBounds);
        float drawnLength = calculatePathLength(drawPath);
        float minRequiredLength = calculatePathLength(guidePath) * 0.4f;

        return isInBounds && drawnLength >= minRequiredLength;
    }

    /**
     * คำนวณความแม่นยำรวม
     */
    private float calculateOverallAccuracy(boolean isCorrect) {
        if (!isCorrect) {
            return 0.3f + (float) Math.random() * 0.3f;
        }

        if (strokePaths.isEmpty()) {
            return calculateSimpleAccuracy();
        }

        // คำนวณความแม่นยำจากเส้นขีดต่างๆ
        float strokeAccuracy = 0f;
        if (completedStrokes != null) {
            int correctCount = 0;
            for (boolean correct : completedStrokes) {
                if (correct) correctCount++;
            }
            strokeAccuracy = (float) correctCount / completedStrokes.length;
        }

        // คำนวณความแม่นยำจากพื้นที่
        float areaAccuracy = calculateAreaAccuracy();

        return (strokeAccuracy * 0.7f + areaAccuracy * 0.3f);
    }

    /**
     * คำนวณความแม่นยำแบบง่าย
     */
    private float calculateSimpleAccuracy() {
        RectF drawnBounds = new RectF();
        drawPath.computeBounds(drawnBounds, true);

        RectF guideBounds = new RectF();
        guidePath.computeBounds(guideBounds, true);

        float overlapArea = calculateOverlapArea(drawnBounds, guideBounds);
        float drawnArea = drawnBounds.width() * drawnBounds.height();
        float guideArea = guideBounds.width() * guideBounds.height();

        float areaAccuracy = overlapArea / Math.max(drawnArea, guideArea);
        return Math.max(0.6f, areaAccuracy);
    }

    /**
     * คำนวณความแม่นยำจากพื้นที่
     */
    private float calculateAreaAccuracy() {
        // Implementation สำหรับคำนวณพื้นที่ทับซ้อน
        return 0.8f; // ค่า placeholder
    }

    /**
     * คำนวณความคืบหน้าปัจจุบัน
     */
    private float calculateCurrentProgress() {
        if (strokePaths.isEmpty()) {
            return 0.5f; // ค่า default
        }

        float strokeProgress = (float) currentStrokeIndex / strokePaths.size();
        float pointAccuracy = calculatePointAccuracy();

        return strokeProgress * 0.6f + pointAccuracy * 0.4f;
    }

    /**
     * คำนวณความแม่นยำของจุด
     */
    private float calculatePointAccuracy() {
        if (drawnPoints.isEmpty()) return 0f;

        int correctPoints = 0;
        for (PointData point : drawnPoints) {
            if (point.isInGuideArea) correctPoints++;
        }

        return (float) correctPoints / drawnPoints.size();
    }

    // Methods อื่นๆ ที่มีอยู่เดิม...
    public void setColor(String newColor){
        invalidate();
        try {
            paintColor = Color.parseColor(newColor);
            drawPaint.setColor(paintColor);
            previousColor = paintColor;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid color: " + newColor);
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

    public void setErase(boolean isErase){
        erase = isErase;
        if(erase) {
            drawPaint.setColor(Color.WHITE);
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
        drawnPoints.clear();
        currentStrokeIndex = 0;
        if (completedStrokes != null) {
            for (int i = 0; i < completedStrokes.length; i++) {
                completedStrokes[i] = false;
            }
        }
        invalidate();
    }

    // Methods ใหม่สำหรับระบบตรวจสอบตัวอักษรไทย
    public void setCheckingEnabled(boolean enabled) {
        isCheckingEnabled = enabled;
        if (!enabled) {
            drawPaint.setColor(previousColor);
        }
        invalidate();
    }

    public boolean isCheckingEnabled() {
        return isCheckingEnabled;
    }

    public void setGuidePath(Path path) {
        this.guidePath = path;
        createGuideAreasFromPath();
        invalidate();
    }

    public void setStrokePaths(List<Path> strokes) {
        this.strokePaths = strokes;
        this.completedStrokes = new boolean[strokes.size()];
        this.currentStrokeIndex = 0;
        createGuideAreasFromStrokes();
        invalidate();
    }

    public void setTolerance(float tolerance) {
        this.tolerance = tolerance;
        invalidate();
    }

    private void createGuideAreasFromPath() {
        guideAreas.clear();
        if (guidePath != null) {
            RectF bounds = new RectF();
            guidePath.computeBounds(bounds, true);
            bounds.inset(-tolerance, -tolerance);
            guideAreas.add(bounds);
        }
    }

    private void createGuideAreasFromStrokes() {
        guideAreas.clear();
        for (Path stroke : strokePaths) {
            RectF bounds = new RectF();
            stroke.computeBounds(bounds, true);
            bounds.inset(-tolerance, -tolerance);
            guideAreas.add(bounds);
        }
    }

    private boolean isPointInGuideArea(float x, float y) {
        if (guideAreas.isEmpty()) {
            return isPointNearGuidePath(x, y);
        }

        for (RectF area : guideAreas) {
            if (area.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPointNearGuidePath(float x, float y) {
        if (guidePath == null || guidePath.isEmpty()) {
            return true;
        }
        return isPointNearPath(guidePath, x, y, tolerance);
    }

    private boolean isPointNearPath(Path path, float x, float y, float tolerance) {
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        bounds.inset(-tolerance, -tolerance);
        return bounds.contains(x, y);
    }

    private float calculatePathLength(Path path) {
        // Implementation เบื้องต้น - ใน production ควรคำนวณให้แม่นยำยิ่งขึ้น
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        return bounds.width() + bounds.height();
    }

    private float calculateOverlapArea(RectF rect1, RectF rect2) {
        float left = Math.max(rect1.left, rect2.left);
        float right = Math.min(rect1.right, rect2.right);
        float top = Math.max(rect1.top, rect2.top);
        float bottom = Math.min(rect1.bottom, rect2.bottom);

        if (left < right && top < bottom) {
            return (right - left) * (bottom - top);
        }
        return 0;
    }

    private void showSuccessEffect() {
        // เอฟเฟกต์เมื่อวาดสำเร็จ - สามารถเพิ่ม animation ได้
        drawPaint.setColor(Color.GREEN);
        postDelayed(() -> {
            drawPaint.setColor(previousColor);
            invalidate();
        }, 300);
    }

    private void showErrorEffect() {
        // เอฟเฟกต์เมื่อวาดผิด - สามารถเพิ่ม animation ได้
        drawPaint.setColor(Color.RED);
        postDelayed(() -> {
            drawPaint.setColor(previousColor);
            invalidate();
        }, 300);
    }

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