package com.pongploydev.kokaiabckids.drawingView

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class KidTracingCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class BrushTool {
        CRAYON,
        MARKER,
        RAINBOW,
        SPARKLE,
        ERASER
    }

    interface OnTracingListener {
        fun onProgressUpdate(progress: Float)
        fun onTracingCompleted()
    }

    var tracingListener: OnTracingListener? = null
    var onHistoryChanged: ((canUndo: Boolean, canRedo: Boolean) -> Unit)? = null

    private val density = resources.displayMetrics.density

    private var isBatchLoading = false

    // Character to trace (when no PNG guide image is set)
    var currentSymbol: String = "ก"
        set(value) {
            if (field == value) return
            field = value
            if (!isBatchLoading) {
                if (width > 0 && height > 0) {
                    initCheckpoints(width, height)
                }
                resetDrawingHard()
                invalidate()
            }
        }

    // Drawable guide image (Thai letters with illustrations, animals, etc.)
    var guideImageRes: Int = 0
        set(value) {
            if (field == value) return
            field = value
            if (!isBatchLoading) {
                if (width > 0 && height > 0) {
                    initCheckpoints(width, height)
                }
                resetDrawingHard()
                invalidate()
            }
        }

    var currentColor: Int = Color.parseColor("#EF4444")
        set(value) {
            field = value
            if (currentTool == BrushTool.ERASER) {
                currentTool = BrushTool.CRAYON
            }
        }

    var currentTool: BrushTool = BrushTool.CRAYON

    // Backwards-compatible flag
    var isEraser: Boolean
        get() = currentTool == BrushTool.ERASER
        set(value) {
            currentTool = if (value) BrushTool.ERASER else BrushTool.CRAYON
        }

    // Brush size in dp (Small = 10dp, Medium = 18dp, Large = 30dp)
    var brushSizeDp: Float = 18f

    var showPaperLines: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    var showGuide: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    private data class StrokeEntry(
        val tool: BrushTool,
        val color: Int,
        val strokeWidthPx: Float,
        val points: MutableList<PointF> = mutableListOf(),
        val startHue: Float = 0f
    )

    // Snapshot-based undo/redo so even Clear Canvas can be undone cleanly
    private val undoStack = ArrayDeque<List<StrokeEntry>>()
    private val redoStack = ArrayDeque<List<StrokeEntry>>()
    private val strokes = mutableListOf<StrokeEntry>()
    private var activeStroke: StrokeEntry? = null
    private var rainbowHueCounter = 0f

    private var guideBitmap: Bitmap? = null
    private val guideBitmapDstRect = RectF()

    // Offscreen bitmap for clean eraser compositing without black artifacts
    private var strokeLayerBitmap: Bitmap? = null
    private var strokeLayerCanvas: Canvas? = null
    private val bitmapBlitPaint = Paint(Paint.DITHER_FLAG)
    private val guideImgPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val guideFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F1F5FB")
        style = Paint.Style.FILL
    }
    private val guideStrokeOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CBD5E1")
        style = Paint.Style.STROKE
        strokeWidth = 5f * density
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val sparklePath = Path()

    // Reusable paints
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isDither = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val sparkleDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val eraserXfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    private val reusablePath = Path()

    // Background & Guide paints
    private val paperLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.5f * density
        style = Paint.Style.STROKE
    }
    private val characterOutlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E8EAF6")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val characterDottedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7986CB")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(12f * density, 14f * density), 0f)
    }
    private val guideCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0288D1")
        style = Paint.Style.FILL
    }
    private val guideTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 14f * density
        typeface = Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    private val guideArrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0288D1")
        strokeWidth = 2.5f * density
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private data class GuideCircleInfo(
        val x: Float,
        val y: Float,
        val label: String,
        val dirX: Float = 0f,
        val dirY: Float = 0f
    )

    private val guideNumberedPoints = mutableListOf<GuideCircleInfo>()
    private val dynamicCharPath = Path()

    // Progress tracking
    private var totalPointsCovered = 0
    private val targetCheckpoints = mutableListOf<PointF>()
    private var checkedPoints = BooleanArray(0)
    private var isCompletedTriggered = false

    private val touchTolerancePx = 2.5f * density

    fun loadItem(symbol: String, imageRes: Int) {
        isBatchLoading = true
        currentSymbol = symbol
        guideImageRes = imageRes
        isBatchLoading = false
        if (width > 0 && height > 0) {
            initCheckpoints(width, height)
        }
        resetDrawingHard()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            strokeLayerBitmap?.recycle()
            strokeLayerBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            strokeLayerCanvas = Canvas(strokeLayerBitmap!!)
            initCheckpoints(w, h)
            rebuildStrokeLayer()
        }
    }

    /**
     * Auto-trims transparent and near-white empty margins around drawable guide bitmaps
     * so letters & illustrations fill the drawing canvas comfortably for kids.
     */
    private fun trimWhitespaceMargins(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        if (w <= 4 || h <= 4) return src

        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1

        val step = if (w * h > 400_000) 2 else 1
        var y = 0
        while (y < h) {
            val rowOffset = y * w
            var x = 0
            while (x < w) {
                val px = pixels[rowOffset + x]
                val alpha = (px ushr 24) and 0xFF
                if (alpha > 18) {
                    val r = (px ushr 16) and 0xFF
                    val g = (px ushr 8) and 0xFF
                    val b = px and 0xFF
                    val isNearWhite = r > 246 && g > 246 && b > 246
                    if (!isNearWhite) {
                        if (x < minX) minX = x
                        if (x > maxX) maxX = x
                        if (y < minY) minY = y
                        if (y > maxY) maxY = y
                    }
                }
                x += step
            }
            y += step
        }

        if (maxX <= minX || maxY <= minY) return src

        val padX = ((maxX - minX) * 0.04f).toInt()
        val padY = ((maxY - minY) * 0.04f).toInt()
        val cropLeft = (minX - padX).coerceAtLeast(0)
        val cropTop = (minY - padY).coerceAtLeast(0)
        val cropRight = (maxX + padX).coerceAtMost(w - 1)
        val cropBottom = (maxY + padY).coerceAtMost(h - 1)
        val cropW = (cropRight - cropLeft + 1).coerceAtLeast(1)
        val cropH = (cropBottom - cropTop + 1).coerceAtLeast(1)

        // Only crop if it meaningfully trims empty margins
        return if (cropW < w * 0.96f || cropH < h * 0.96f) {
            Bitmap.createBitmap(src, cropLeft, cropTop, cropW, cropH)
        } else {
            src
        }
    }

    private fun initCheckpoints(w: Int, h: Int) {
        targetCheckpoints.clear()
        guideNumberedPoints.clear()
        dynamicCharPath.reset()
        guideBitmap?.let {
            if (!it.isRecycled) it.recycle()
        }
        guideBitmap = null

        val cx = w / 2f
        val cy = h / 2f
        val minDim = min(w, h).toFloat()

        characterOutlinePaint.strokeWidth = minDim * 0.085f
        characterDottedPaint.strokeWidth = 3f * density

        if (guideImageRes != 0) {
            try {
                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val rawBm = BitmapFactory.decodeResource(resources, guideImageRes, opts)
                if (rawBm != null) {
                    val trimmed = trimWhitespaceMargins(rawBm)
                    if (trimmed !== rawBm) {
                        rawBm.recycle()
                    }
                    // Fill up to 90% width / 84% height so tracing is big and comfortable
                    val maxW = w * 0.90f
                    val maxH = h * 0.84f
                    val scale = min(maxW / trimmed.width.toFloat(), maxH / trimmed.height.toFloat())
                    val scaledW = trimmed.width * scale
                    val scaledH = trimmed.height * scale
                    val left = cx - scaledW / 2f
                    val top = cy - scaledH / 2f
                    guideBitmapDstRect.set(left, top, left + scaledW, top + scaledH)
                    guideBitmap = trimmed

                    // Sample checkpoints evenly over actual content area
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
            // Dynamic vector extraction scaled generously to the canvas size
            val targetTextSize = when {
                currentSymbol.length <= 1 -> minDim * 0.64f
                currentSymbol.length == 2 -> minDim * 0.48f
                else -> minDim * 0.34f
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = targetTextSize
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            val bounds = Rect()
            textPaint.getTextBounds(currentSymbol, 0, currentSymbol.length, bounds)

            // Fit horizontally if symbol is wide
            val maxTextW = w * 0.80f
            if (bounds.width() > maxTextW && bounds.width() > 0) {
                textPaint.textSize = targetTextSize * (maxTextW / bounds.width().toFloat())
                textPaint.getTextBounds(currentSymbol, 0, currentSymbol.length, bounds)
            }

            val charY = cy - bounds.exactCenterY()
            textPaint.getTextPath(currentSymbol, 0, currentSymbol.length, cx, charY, dynamicCharPath)

            val pm = PathMeasure(dynamicCharPath, false)
            var contourIdx = 0
            do {
                val length = pm.length
                if (length > 15f) {
                    val step = max(24f, length / 18f)
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
                    targetCheckpoints.add(PointF(cx, cy + t * (h * 0.25f)))
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

        // 1. Paper background + optional ruled lines
        drawNotebookBackground(canvas, w, h)

        // 2. Guide illustration or dotted vector letter
        if (showGuide) {
            if (guideBitmap != null && !guideBitmap!!.isRecycled) {
                canvas.drawBitmap(guideBitmap!!, null, guideBitmapDstRect, guideImgPaint)
            } else {
                drawCharacterGuide(canvas)
            }
        }

        // 3. Draw user's stroke layer (rendered offscreen so eraser works cleanly)
        strokeLayerBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, bitmapBlitPaint)
        }
    }

    private fun drawNotebookBackground(canvas: Canvas, w: Float, h: Float) {
        canvas.drawColor(Color.parseColor("#FFFFFF"))
        if (!showPaperLines) return

        val margin = 18f * density
        val lineSpacing = h / 7f
        for (i in 1..6) {
            val y = i * lineSpacing
            if (i == 3 || i == 4) {
                paperLinePaint.pathEffect = DashPathEffect(floatArrayOf(8f * density, 8f * density), 0f)
                paperLinePaint.color = Color.parseColor("#DCE3EC")
            } else {
                paperLinePaint.pathEffect = null
                paperLinePaint.color = Color.parseColor("#EFF3F8")
            }
            canvas.drawLine(margin, y, w - margin, y, paperLinePaint)
        }
        paperLinePaint.pathEffect = null
    }

    private fun drawCharacterGuide(canvas: Canvas) {
        canvas.drawPath(dynamicCharPath, guideFillPaint)
        canvas.drawPath(dynamicCharPath, guideStrokeOutlinePaint)
        canvas.drawPath(dynamicCharPath, characterDottedPaint)

        guideNumberedPoints.forEach { guide ->
            drawGuideCircle(canvas, guide.x, guide.y, guide.label)
            if (guide.dirX != 0f || guide.dirY != 0f) {
                val len = hypot(guide.dirX.toDouble(), guide.dirY.toDouble()).toFloat()
                if (len > 0.001f) {
                    val normX = guide.dirX / len
                    val normY = guide.dirY / len
                    drawArrow(
                        canvas,
                        guide.x + normX * (16f * density),
                        guide.y + normY * (16f * density),
                        guide.x + normX * (34f * density),
                        guide.y + normY * (34f * density)
                    )
                }
            }
        }
    }

    private fun drawGuideCircle(canvas: Canvas, x: Float, y: Float, number: String) {
        val radius = 12f * density
        canvas.drawCircle(x, y, radius, guideCirclePaint)
        canvas.drawText(number, x, y + 5f * density, guideTextPaint)
    }

    private fun drawArrow(canvas: Canvas, fromX: Float, fromY: Float, toX: Float, toY: Float) {
        canvas.drawLine(fromX, fromY, toX, toY, guideArrowPaint)
        val angle = atan2((toY - fromY).toDouble(), (toX - fromX).toDouble())
        val arrowHeadLength = 9f * density
        val x1 = (toX - arrowHeadLength * cos(angle - Math.PI / 6)).toFloat()
        val y1 = (toY - arrowHeadLength * sin(angle - Math.PI / 6)).toFloat()
        val x2 = (toX - arrowHeadLength * cos(angle + Math.PI / 6)).toFloat()
        val y2 = (toY - arrowHeadLength * sin(angle + Math.PI / 6)).toFloat()
        canvas.drawLine(toX, toY, x1, y1, guideArrowPaint)
        canvas.drawLine(toX, toY, x2, y2, guideArrowPaint)
    }

    private fun rebuildStrokeLayer() {
        val targetCanvas = strokeLayerCanvas ?: return
        targetCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        strokes.forEach { drawStrokeOnCanvas(targetCanvas, it) }
        activeStroke?.let { drawStrokeOnCanvas(targetCanvas, it) }
    }

    private fun drawStrokeOnCanvas(targetCanvas: Canvas, entry: StrokeEntry) {
        val pts = entry.points
        if (pts.isEmpty()) return

        strokePaint.reset()
        strokePaint.isAntiAlias = true
        strokePaint.isDither = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeJoin = Paint.Join.ROUND
        strokePaint.strokeCap = Paint.Cap.ROUND
        strokePaint.strokeWidth = entry.strokeWidthPx

        when (entry.tool) {
            BrushTool.CRAYON -> {
                strokePaint.color = entry.color
                drawSmoothPath(targetCanvas, pts, strokePaint)
            }
            BrushTool.MARKER -> {
                strokePaint.color = entry.color
                strokePaint.alpha = 135
                strokePaint.strokeWidth = entry.strokeWidthPx * 1.25f
                drawSmoothPath(targetCanvas, pts, strokePaint)
            }
            BrushTool.ERASER -> {
                strokePaint.xfermode = eraserXfermode
                strokePaint.strokeWidth = entry.strokeWidthPx * 1.45f
                drawSmoothPath(targetCanvas, pts, strokePaint)
            }
            BrushTool.RAINBOW -> {
                if (pts.size == 1) {
                    strokePaint.color = Color.HSVToColor(floatArrayOf(entry.startHue % 360f, 0.85f, 0.96f))
                    targetCanvas.drawPoint(pts[0].x, pts[0].y, strokePaint)
                } else {
                    var hue = entry.startHue
                    for (i in 1 until pts.size) {
                        val p0 = pts[i - 1]
                        val p1 = pts[i]
                        val segDist = hypot((p1.x - p0.x).toDouble(), (p1.y - p0.y).toDouble()).toFloat()
                        hue = (hue + segDist * 0.25f) % 360f
                        strokePaint.color = Color.HSVToColor(floatArrayOf(hue, 0.85f, 0.96f))
                        targetCanvas.drawLine(p0.x, p0.y, p1.x, p1.y, strokePaint)
                    }
                }
            }
            BrushTool.SPARKLE -> {
                // Soft colored glow base
                strokePaint.color = entry.color
                strokePaint.alpha = 190
                strokePaint.maskFilter = BlurMaskFilter(4f * density, BlurMaskFilter.Blur.NORMAL)
                drawSmoothPath(targetCanvas, pts, strokePaint)
                strokePaint.maskFilter = null

                // Crisp bright core
                strokePaint.color = Color.WHITE
                strokePaint.alpha = 165
                strokePaint.strokeWidth = entry.strokeWidthPx * 0.32f
                drawSmoothPath(targetCanvas, pts, strokePaint)

                // Small sparkle stars distributed deterministically along points
                val spacing = max(2, (entry.strokeWidthPx / (6f * density)).toInt() + 2)
                for (i in pts.indices step spacing) {
                    val p = pts[i]
                    val offsetSign = if (i % 2 == 0) 1f else -1f
                    val ox = p.x + offsetSign * (entry.strokeWidthPx * 0.38f)
                    val oy = p.y - offsetSign * (entry.strokeWidthPx * 0.28f)
                    val starR = max(2.5f * density, entry.strokeWidthPx * 0.16f)
                    sparkleDotPaint.color = if (i % 4 == 0) Color.parseColor("#FFE082") else Color.WHITE
                    drawMiniSparkle(targetCanvas, ox, oy, starR, sparkleDotPaint)
                }
            }
        }
    }

    private fun drawSmoothPath(canvas: Canvas, pts: List<PointF>, paint: Paint) {
        if (pts.size == 1) {
            canvas.drawPoint(pts[0].x, pts[0].y, paint)
            return
        }
        reusablePath.reset()
        reusablePath.moveTo(pts[0].x, pts[0].y)
        for (i in 1 until pts.size) {
            val prev = pts[i - 1]
            val curr = pts[i]
            reusablePath.quadTo(prev.x, prev.y, (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f)
        }
        val last = pts.last()
        reusablePath.lineTo(last.x, last.y)
        canvas.drawPath(reusablePath, paint)
    }

    private fun drawMiniSparkle(canvas: Canvas, cx: Float, cy: Float, r: Float, paint: Paint) {
        sparklePath.reset()
        sparklePath.moveTo(cx, cy - r)
        sparklePath.lineTo(cx + r * 0.3f, cy - r * 0.3f)
        sparklePath.lineTo(cx + r, cy)
        sparklePath.lineTo(cx + r * 0.3f, cy + r * 0.3f)
        sparklePath.lineTo(cx, cy + r)
        sparklePath.lineTo(cx - r * 0.3f, cy + r * 0.3f)
        sparklePath.lineTo(cx - r, cy)
        sparklePath.lineTo(cx - r * 0.3f, cy - r * 0.3f)
        sparklePath.close()
        canvas.drawPath(sparklePath, paint)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                val newStroke = StrokeEntry(
                    tool = currentTool,
                    color = currentColor,
                    strokeWidthPx = brushSizeDp * density,
                    points = mutableListOf(PointF(x, y)),
                    startHue = rainbowHueCounter
                )
                activeStroke = newStroke
                if (currentTool != BrushTool.ERASER) {
                    checkHit(x, y)
                }
                rebuildStrokeLayer()
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val stroke = activeStroke ?: return true
                val last = stroke.points.lastOrNull()
                if (last == null || abs(x - last.x) >= touchTolerancePx || abs(y - last.y) >= touchTolerancePx) {
                    stroke.points.add(PointF(x, y))
                    if (stroke.tool == BrushTool.RAINBOW && last != null) {
                        val d = hypot((x - last.x).toDouble(), (y - last.y).toDouble()).toFloat()
                        rainbowHueCounter = (rainbowHueCounter + d * 0.25f) % 360f
                    }
                    if (stroke.tool != BrushTool.ERASER) {
                        checkHit(x, y)
                    }
                    rebuildStrokeLayer()
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                activeStroke?.let { finished ->
                    if (event.actionMasked == MotionEvent.ACTION_UP && finished.points.size == 1) {
                        performClick()
                    }
                    pushUndoSnapshot()
                    strokes.add(finished)
                    redoStack.clear()
                    activeStroke = null
                    rebuildStrokeLayer()
                    notifyHistoryChanged()
                    invalidate()
                }
            }
        }
        return true
    }

    private fun checkHit(x: Float, y: Float) {
        if (targetCheckpoints.isEmpty()) return

        val hitRadius = max(36f * density, brushSizeDp * density * 1.4f)
        for (i in targetCheckpoints.indices) {
            if (!checkedPoints[i]) {
                val pt = targetCheckpoints[i]
                val dist = hypot((x - pt.x).toDouble(), (y - pt.y).toDouble())
                if (dist <= hitRadius) {
                    checkedPoints[i] = true
                    totalPointsCovered++
                }
            }
        }

        val progress = totalPointsCovered.toFloat() / targetCheckpoints.size.toFloat()
        tracingListener?.onProgressUpdate(progress.coerceIn(0f, 1f))

        if (progress >= 0.72f && !isCompletedTriggered) {
            isCompletedTriggered = true
            tracingListener?.onTracingCompleted()
        }
    }

    private fun pushUndoSnapshot() {
        undoStack.addLast(strokes.toList())
        if (undoStack.size > 30) {
            undoStack.removeFirst()
        }
    }

    private fun notifyHistoryChanged() {
        onHistoryChanged?.invoke(undoStack.isNotEmpty(), redoStack.isNotEmpty())
    }

    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        redoStack.addLast(strokes.toList())
        val previous = undoStack.removeLast()
        strokes.clear()
        strokes.addAll(previous)
        rebuildStrokeLayer()
        recomputeProgressFromStrokes()
        notifyHistoryChanged()
        invalidate()
        return true
    }

    fun redo(): Boolean {
        if (redoStack.isEmpty()) return false
        undoStack.addLast(strokes.toList())
        val next = redoStack.removeLast()
        strokes.clear()
        strokes.addAll(next)
        rebuildStrokeLayer()
        recomputeProgressFromStrokes()
        notifyHistoryChanged()
        invalidate()
        return true
    }

    private fun recomputeProgressFromStrokes() {
        if (targetCheckpoints.isEmpty()) return
        checkedPoints = BooleanArray(targetCheckpoints.size)
        totalPointsCovered = 0
        val hitRadius = 40f * density
        for (entry in strokes) {
            if (entry.tool == BrushTool.ERASER) continue
            for (pt in entry.points) {
                for (i in targetCheckpoints.indices) {
                    if (!checkedPoints[i]) {
                        val cp = targetCheckpoints[i]
                        if (hypot((pt.x - cp.x).toDouble(), (pt.y - cp.y).toDouble()) <= hitRadius) {
                            checkedPoints[i] = true
                            totalPointsCovered++
                        }
                    }
                }
            }
        }
        val progress = totalPointsCovered.toFloat() / targetCheckpoints.size.toFloat()
        if (progress < 0.72f) isCompletedTriggered = false
        tracingListener?.onProgressUpdate(progress.coerceIn(0f, 1f))
    }

    fun clearCanvas() {
        if (strokes.isNotEmpty()) {
            pushUndoSnapshot()
            redoStack.clear()
        }
        strokes.clear()
        activeStroke = null
        strokeLayerCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (targetCheckpoints.isNotEmpty()) {
            checkedPoints = BooleanArray(targetCheckpoints.size)
        }
        totalPointsCovered = 0
        isCompletedTriggered = false
        tracingListener?.onProgressUpdate(0f)
        notifyHistoryChanged()
        invalidate()
    }

    private fun resetDrawingHard() {
        undoStack.clear()
        redoStack.clear()
        strokes.clear()
        activeStroke = null
        strokeLayerCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (targetCheckpoints.isNotEmpty()) {
            checkedPoints = BooleanArray(targetCheckpoints.size)
        }
        totalPointsCovered = 0
        isCompletedTriggered = false
        tracingListener?.onProgressUpdate(0f)
        notifyHistoryChanged()
    }

    fun hasDrawing(): Boolean = strokes.isNotEmpty()

    fun exportArtworkBitmap(): Bitmap? {
        if (width <= 0 || height <= 0) return null
        val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        draw(c)
        return out
    }
}
