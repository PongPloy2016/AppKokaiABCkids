package com.pongploydev.kokaiabckids.drawingView

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot

class KidTracingCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnTracingListener {
        fun onProgressUpdate(progress: Float)
        fun onTracingCompleted()
    }

    var tracingListener: OnTracingListener? = null

    // Character to trace
    var currentSymbol: String = "1"
        set(value) {
            field = value
            if (width > 0 && height > 0) {
                initCheckpoints(width, height)
            }
            clearCanvas()
            invalidate()
        }

    // Image to color (Animals, Fruits, etc.)
    var guideImageRes: Int = 0
        set(value) {
            field = value
            if (width > 0 && height > 0) {
                initCheckpoints(width, height)
            }
            clearCanvas()
            invalidate()
        }

    private var guideBitmap: Bitmap? = null
    private val guideBitmapDstRect = RectF()

    // Colors & Paints
    var currentColor: Int = Color.parseColor("#FF5252") // Default red crayon
        set(value) {
            field = value
            isEraser = false
            updateBrushPaint()
        }

    var isEraser: Boolean = false
        set(value) {
            field = value
            updateBrushPaint()
        }

    private var brushSize: Float = 42f

    // Drawing paths & bitmaps
    private var drawCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null
    private val canvasPaint = Paint(Paint.DITHER_FLAG)
    private val currentPath = Path()
    private val userStrokePaint = Paint()

    // Guide Paints
    private val paperLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val characterOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val characterDottedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val guideCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val guideTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val guideArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Touch tracking
    private var mX = 0f
    private var mY = 0f
    private val TOUCH_TOLERANCE = 4f

    // Progress tracking
    private var totalPointsCovered = 0
    private var targetCheckpoints = mutableListOf<PointF>()
    private var checkedPoints = BooleanArray(0)
    private var isCompletedTriggered = false

    init {
        setupPaints()
    }

    private fun setupPaints() {
        userStrokePaint.apply {
            color = currentColor
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = brushSize
        }

        paperLinePaint.apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        characterOutlinePaint.apply {
            color = Color.parseColor("#E8EAF6")
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 72f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        characterDottedPaint.apply {
            color = Color.parseColor("#9FA8DA")
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
            pathEffect = DashPathEffect(floatArrayOf(12f, 16f), 0f)
        }

        guideCirclePaint.apply {
            color = Color.parseColor("#0288D1")
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        guideTextPaint.apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }

        guideArrowPaint.apply {
            color = Color.parseColor("#0288D1")
            isAntiAlias = true
            strokeWidth = 5f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
    }

    private fun updateBrushPaint() {
        if (isEraser) {
            userStrokePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            userStrokePaint.strokeWidth = brushSize * 1.5f
        } else {
            userStrokePaint.xfermode = null
            userStrokePaint.color = currentColor
            userStrokePaint.strokeWidth = brushSize
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            drawCanvas = Canvas(canvasBitmap!!)
            initCheckpoints(w, h)
        }
    }

    data class GuideCircleInfo(val x: Float, val y: Float, val label: String, val dirX: Float = 0f, val dirY: Float = 0f)
    private val guideNumberedPoints = mutableListOf<GuideCircleInfo>()
    private val dynamicCharPath = Path()

    private fun initCheckpoints(w: Int, h: Int) {
        targetCheckpoints.clear()
        guideNumberedPoints.clear()
        dynamicCharPath.reset()
        guideBitmap = null
        val cx = w / 2f
        val cy = h / 2f

        if (guideImageRes != 0) {
            try {
                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val rawBm = BitmapFactory.decodeResource(resources, guideImageRes, opts)
                if (rawBm != null) {
                    val maxW = w * 0.85f
                    val maxH = h * 0.72f
                    val scale = minOf(maxW / rawBm.width.toFloat(), maxH / rawBm.height.toFloat())
                    val scaledW = rawBm.width * scale
                    val scaledH = rawBm.height * scale
                    val left = cx - scaledW / 2f
                    val top = cy - scaledH / 2f
                    guideBitmapDstRect.set(left, top, left + scaledW, top + scaledH)
                    guideBitmap = rawBm

                    // Sample grid checkpoints covering the image for coloring detection
                    val numCols = 6
                    val numRows = 6
                    val stepX = scaledW / (numCols + 1)
                    val stepY = scaledH / (numRows + 1)
                    for (ix in 1..numCols) {
                        for (iy in 1..numRows) {
                            targetCheckpoints.add(PointF(left + ix * stepX, top + iy * stepY))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Generate sample points along the current symbol
            when (currentSymbol) {
                "1" -> {
                    // Slanted stem up to peak, then straight down to base, then flat base
                    val topPeakX = cx
                    val topPeakY = cy - 220f
                    val startSlantX = cx - 90f
                    val startSlantY = cy - 140f
                    val bottomY = cy + 220f

                    // Slant stroke
                    for (i in 0..5) {
                        val t = i / 5f
                        targetCheckpoints.add(PointF(startSlantX + t * (topPeakX - startSlantX), startSlantY + t * (topPeakY - startSlantY)))
                    }
                    // Down stem
                    for (i in 0..12) {
                        val t = i / 12f
                        targetCheckpoints.add(PointF(cx, topPeakY + t * (bottomY - topPeakY)))
                    }
                    // Bottom foot bar
                    for (i in 0..6) {
                        val t = (i - 3) / 3f
                        targetCheckpoints.add(PointF(cx + t * 100f, bottomY))
                    }
                }
                "2" -> {
                    // Arc top right, diagonal to bottom left, horizontal right
                    val r = 110f
                    val topY = cy - 140f
                    for (deg in 180 downTo 0 step 25) {
                        val rad = Math.toRadians(deg.toDouble())
                        targetCheckpoints.add(PointF(cx + (r * Math.cos(rad)).toFloat(), topY - (r * Math.sin(rad)).toFloat()))
                    }
                    // Diagonal down left
                    for (i in 0..8) {
                        val t = i / 8f
                        targetCheckpoints.add(PointF((cx + r) - t * (2 * r), topY + t * 240f))
                    }
                    // Horizontal base
                    for (i in 0..8) {
                        val t = i / 8f
                        targetCheckpoints.add(PointF((cx - r) + t * (2 * r + 20f), topY + 240f))
                    }
                }
                "3" -> {
                    val r = 100f
                    val topY = cy - 110f
                    val bottomY = cy + 110f
                    for (deg in 180 downTo -45 step 30) {
                        val rad = Math.toRadians(deg.toDouble())
                        targetCheckpoints.add(PointF(cx + (r * Math.cos(rad)).toFloat(), topY - (r * Math.sin(rad)).toFloat()))
                    }
                    for (deg in 45 downTo -180 step 30) {
                        val rad = Math.toRadians(deg.toDouble())
                        targetCheckpoints.add(PointF(cx + (r * Math.cos(rad)).toFloat(), bottomY - (r * Math.sin(rad)).toFloat()))
                    }
                }
                "0" -> {
                    val rx = 110f
                    val ry = 200f
                    for (deg in 90 downTo -270 step 20) {
                        val rad = Math.toRadians(deg.toDouble())
                        targetCheckpoints.add(PointF(cx + (rx * Math.cos(rad)).toFloat(), cy - (ry * Math.sin(rad)).toFloat()))
                    }
                }
                else -> {
                    // Dynamic vector extraction for Thai letters (ก-ฮ), English letters (A-Z), numbers (๑-๑๐)
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        textSize = if (currentSymbol.length > 1) 240f else 320f
                        typeface = Typeface.DEFAULT_BOLD
                        textAlign = Paint.Align.CENTER
                    }
                    val bounds = Rect()
                    textPaint.getTextBounds(currentSymbol, 0, currentSymbol.length, bounds)
                    val charY = cy - bounds.exactCenterY()
                    val charX = cx
                    textPaint.getTextPath(currentSymbol, 0, currentSymbol.length, charX, charY, dynamicCharPath)

                    val pm = PathMeasure(dynamicCharPath, false)
                    var contourIdx = 0
                    do {
                        val length = pm.length
                        if (length > 15f) {
                            val step = 28f
                            var dist = 0f
                            val pos = FloatArray(2)
                            val tan = FloatArray(2)

                            pm.getPosTan(0f, pos, tan)
                            if (contourIdx < 3) {
                                guideNumberedPoints.add(
                                    GuideCircleInfo(pos[0], pos[1], (contourIdx + 1).toString(), tan[0], tan[1])
                                )
                            }

                            while (dist < length) {
                                pm.getPosTan(dist, pos, tan)
                                targetCheckpoints.add(PointF(pos[0], pos[1]))
                                dist += step
                            }
                            pm.getPosTan(length, pos, tan)
                            targetCheckpoints.add(PointF(pos[0], pos[1]))
                            contourIdx++
                        }
                    } while (pm.nextContour())

                    if (targetCheckpoints.isEmpty()) {
                        for (i in 0..15) {
                            val t = (i - 7.5f) / 7.5f
                            targetCheckpoints.add(PointF(cx, cy + t * 200f))
                        }
                    }
                }
            }
        }

        checkedPoints = BooleanArray(targetCheckpoints.size)
        totalPointsCovered = 0
        isCompletedTriggered = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Draw notebook ruled lines
        drawNotebookBackground(canvas, w, h)

        // 2. Draw Target Guide (Animal picture or character vector guide)
        if (guideBitmap != null && !guideBitmap!!.isRecycled) {
            val imgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                alpha = 245
            }
            canvas.drawBitmap(guideBitmap!!, null, guideBitmapDstRect, imgPaint)
        } else {
            drawCharacterGuide(canvas, w, h)
        }

        // 3. Draw User's Strokes (from Bitmap canvas)
        canvasBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, canvasPaint)
        }

        // Draw live drawing stroke
        canvas.drawPath(currentPath, userStrokePaint)
    }

    private fun drawNotebookBackground(canvas: Canvas, w: Float, h: Float) {
        // Soft white paper
        canvas.drawColor(Color.parseColor("#FDFDFB"))

        // Horizontal ruled notebook lines
        val lineSpacing = h / 9f
        for (i in 1..8) {
            val y = i * lineSpacing
            if (i == 4) {
                // Middle dashed guide line
                paperLinePaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
                paperLinePaint.color = Color.parseColor("#CFD8DC")
            } else {
                paperLinePaint.pathEffect = null
                paperLinePaint.color = Color.parseColor("#ECEFF1")
            }
            canvas.drawLine(30f, y, w - 30f, y, paperLinePaint)
        }
        paperLinePaint.pathEffect = null
    }

    private fun drawCharacterGuide(canvas: Canvas, w: Float, h: Float) {
        val cx = w / 2f
        val cy = h / 2f

        val guidePath = Path()

        when (currentSymbol) {
            "1" -> {
                val topPeakX = cx
                val topPeakY = cy - 220f
                val startSlantX = cx - 90f
                val startSlantY = cy - 140f
                val bottomY = cy + 220f

                // Outline path
                guidePath.moveTo(startSlantX, startSlantY)
                guidePath.lineTo(topPeakX, topPeakY)
                guidePath.lineTo(cx, bottomY)

                // Base foot line
                guidePath.moveTo(cx - 100f, bottomY)
                guidePath.lineTo(cx + 100f, bottomY)

                // Draw thick outline
                canvas.drawPath(guidePath, characterOutlinePaint)
                // Draw dotted center path
                canvas.drawPath(guidePath, characterDottedPaint)

                // Stroke 1 Guide (Slanted Up)
                drawGuideCircle(canvas, startSlantX, startSlantY, "1")
                drawArrow(canvas, startSlantX + 20f, startSlantY - 20f, topPeakX - 20f, topPeakY + 20f)

                // Stroke 2 Guide (Straight Down)
                drawGuideCircle(canvas, cx + 45f, topPeakY, "2")
                drawArrow(canvas, cx + 45f, topPeakY + 30f, cx + 45f, bottomY - 30f)

                // Stroke 3 Guide (Bottom base)
                drawGuideCircle(canvas, cx - 110f, bottomY, "3")
                drawArrow(canvas, cx - 70f, bottomY, cx + 70f, bottomY)
            }
            "2" -> {
                val r = 110f
                val topY = cy - 140f
                val bottomY = cy + 100f

                guidePath.moveTo(cx - r, topY)
                guidePath.quadTo(cx - r, topY - r, cx, topY - r)
                guidePath.quadTo(cx + r, topY - r, cx + r, topY)
                guidePath.lineTo(cx - r, bottomY)
                guidePath.lineTo(cx + r + 20f, bottomY)

                canvas.drawPath(guidePath, characterOutlinePaint)
                canvas.drawPath(guidePath, characterDottedPaint)

                drawGuideCircle(canvas, cx - r, topY, "1")
                drawGuideCircle(canvas, cx - r, bottomY, "2")
            }
            "0" -> {
                val rx = 110f
                val ry = 190f
                guidePath.addOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), Path.Direction.CCW)
                canvas.drawPath(guidePath, characterOutlinePaint)
                canvas.drawPath(guidePath, characterDottedPaint)

                drawGuideCircle(canvas, cx, cy - ry, "1")
                drawArrow(canvas, cx - 30f, cy - ry + 10f, cx - 90f, cy)
            }
            else -> {
                // 1. Draw character background fill with soft pastel tint
                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#F0F4F8")
                    style = Paint.Style.FILL
                }
                canvas.drawPath(dynamicCharPath, fillPaint)

                // 2. Draw character boundary outline
                val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#C5CAE9")
                    style = Paint.Style.STROKE
                    strokeWidth = 12f
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                canvas.drawPath(dynamicCharPath, strokePaint)

                // 3. Draw dotted centerline on character
                val dottedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#7986CB")
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                    pathEffect = DashPathEffect(floatArrayOf(12f, 14f), 0f)
                }
                canvas.drawPath(dynamicCharPath, dottedPaint)

                // 4. Draw numbered start circles & direction arrows
                guideNumberedPoints.forEach { guide ->
                    drawGuideCircle(canvas, guide.x, guide.y, guide.label)
                    if (guide.dirX != 0f || guide.dirY != 0f) {
                        val len = Math.hypot(guide.dirX.toDouble(), guide.dirY.toDouble()).toFloat()
                        if (len > 0.001f) {
                            val normX = guide.dirX / len
                            val normY = guide.dirY / len
                            drawArrow(
                                canvas,
                                guide.x + normX * 10f,
                                guide.y + normY * 10f,
                                guide.x + normX * 45f,
                                guide.y + normY * 45f
                            )
                        }
                    }
                }
            }
        }
    }

    private fun drawGuideCircle(canvas: Canvas, x: Float, y: Float, number: String) {
        canvas.drawCircle(x, y, 22f, guideCirclePaint)
        canvas.drawText(number, x, y + 10f, guideTextPaint)
    }

    private fun drawArrow(canvas: Canvas, fromX: Float, fromY: Float, toX: Float, toY: Float) {
        canvas.drawLine(fromX, fromY, toX, toY, guideArrowPaint)
        // Arrowhead
        val angle = Math.atan2((toY - fromY).toDouble(), (toX - fromX).toDouble())
        val arrowHeadLength = 20f
        val x1 = (toX - arrowHeadLength * Math.cos(angle - Math.PI / 6)).toFloat()
        val y1 = (toY - arrowHeadLength * Math.sin(angle - Math.PI / 6)).toFloat()
        val x2 = (toX - arrowHeadLength * Math.cos(angle + Math.PI / 6)).toFloat()
        val y2 = (toY - arrowHeadLength * Math.sin(angle + Math.PI / 6)).toFloat()

        canvas.drawLine(toX, toY, x1, y1, guideArrowPaint)
        canvas.drawLine(toX, toY, x2, y2, guideArrowPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                checkHit(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
        }
        return true
    }

    private fun touchStart(x: Float, y: Float) {
        currentPath.reset()
        currentPath.moveTo(x, y)
        mX = x
        mY = y
        checkHit(x, y)
    }

    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            currentPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp() {
        currentPath.lineTo(mX, mY)
        drawCanvas?.drawPath(currentPath, userStrokePaint)
        currentPath.reset()
    }

    private fun checkHit(x: Float, y: Float) {
        if (targetCheckpoints.isEmpty()) return

        for (i in targetCheckpoints.indices) {
            if (!checkedPoints[i]) {
                val pt = targetCheckpoints[i]
                val dist = hypot((x - pt.x).toDouble(), (y - pt.y).toDouble())
                if (dist <= 65.0) {
                    checkedPoints[i] = true
                    totalPointsCovered++
                }
            }
        }

        val progress = if (targetCheckpoints.isNotEmpty()) {
            totalPointsCovered.toFloat() / targetCheckpoints.size.toFloat()
        } else 0f

        tracingListener?.onProgressUpdate(progress.coerceIn(0f, 1f))

        if (progress >= 0.75f && !isCompletedTriggered) {
            isCompletedTriggered = true
            tracingListener?.onTracingCompleted()
        }
    }

    fun clearCanvas() {
        canvasBitmap?.eraseColor(Color.TRANSPARENT)
        currentPath.reset()
        if (targetCheckpoints.isNotEmpty()) {
            checkedPoints = BooleanArray(targetCheckpoints.size)
        }
        totalPointsCovered = 0
        isCompletedTriggered = false
        tracingListener?.onProgressUpdate(0f)
        invalidate()
    }
}
