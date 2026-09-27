package com.pongploydev.priyankvex.paintapp.activity.learning.showKids

import android.content.ContentValues
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class ColoringGameActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        setContent {
            KidsColoringStudioScreen(
                onBack = { finish() },
                onSpeak = { text -> speakText(text) }
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("th"))
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
            }
        }
    }

    private fun speakText(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ColoringTTS")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

// ==================== DATA MODELS ====================

data class ColoredStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val isEraser: Boolean = false,
    val isRainbow: Boolean = false
)

data class ColoringTemplate(
    val id: String,
    val title: String,
    val category: String,
    val emoji: String,
    val resId: Int?
)

enum class ActiveTool {
    BRUSH,
    RAINBOW,
    ERASER
}

// ==================== TEMPLATE CATALOG ====================

val COLORING_TEMPLATES = listOf(
    // สัตว์น่ารัก
    ColoringTemplate("cat", "น้องแมวเหมียว", "สัตว์น่ารัก", "🐱", R.drawable.c_cat),
    ColoringTemplate("dog", "น้องหมาเพื่อนรัก", "สัตว์น่ารัก", "🐶", R.drawable.d_dog),
    ColoringTemplate("fish", "ปลาน้อยว่ายน้ำ", "สัตว์น่ารัก", "🐟", R.drawable.f_fish),
    ColoringTemplate("lion", "สิงโตเจ้าป่า", "สัตว์น่ารัก", "🦁", R.drawable.lion),
    ColoringTemplate("giraffe", "พี่ยีราฟคอยาว", "สัตว์น่ารัก", "🦒", R.drawable.giraffe),
    ColoringTemplate("bear", "พี่หมีใจดี", "สัตว์น่ารัก", "🐻", R.drawable.bear),
    ColoringTemplate("fox", "จิ้งจอกแสนซน", "สัตว์น่ารัก", "🦊", R.drawable.fox),
    ColoringTemplate("hedgehog", "คุณเม่นตัวกลม", "สัตว์น่ารัก", "🦔", R.drawable.hedgehog),

    // พยัญชนะไทย
    ColoringTemplate("thai_k1", "ก ไก่", "พยัญชนะไทย", "🐔", R.drawable.thai_card_1),
    ColoringTemplate("thai_k2", "ข ไข่", "พยัญชนะไทย", "🥚", R.drawable.thai_card_2),
    ColoringTemplate("thai_k4", "ค ควาย", "พยัญชนะไทย", "🐃", R.drawable.thai_card_4),
    ColoringTemplate("thai_k7", "ง งู", "พยัญชนะไทย", "🐍", R.drawable.thai_card_7),
    ColoringTemplate("thai_k10", "ช ช้าง", "พยัญชนะไทย", "🐘", R.drawable.thai_card_10),
    ColoringTemplate("thai_k27", "ป ปลา", "พยัญชนะไทย", "🐠", R.drawable.thai_card_27),

    // ยานพาหนะ
    ColoringTemplate("police", "รถตำรวจวี้หว่อ", "ยานพาหนะ", "🚓", R.drawable.police_car),
    ColoringTemplate("truck", "รถบรรทุกพลังแกร่ง", "ยานพาหนะ", "🚚", R.drawable.truck),
    ColoringTemplate("bus", "รถบัสพาเที่ยว", "ยานพาหนะ", "🚌", R.drawable.bus),

    // ผลไม้ & ขนม
    ColoringTemplate("strawberry", "สตรอว์เบอร์รี่หวานฉ่ำ", "ของอร่อย", "🍓", R.drawable.strawberry),
    ColoringTemplate("pizza", "พิซซ่าชีสยืด", "ของอร่อย", "🍕", R.drawable.pizza),
    ColoringTemplate("lemon", "เลมอนสดชื่น", "ของอร่อย", "🍋", R.drawable.lemon),

    // วาดอิสระ
    ColoringTemplate("blank", "กระดาษวาดภาพอิสระ", "วาดอิสระ", "🎨", null)
)

val KIDS_PALETTE = listOf(
    Color(0xFFFF3B30), // แดง
    Color(0xFFFF9500), // ส้ม
    Color(0xFFFFCC00), // เหลือง
    Color(0xFF34C759), // เขียวมะนาว
    Color(0xFF007E33), // เขียวเข้ม
    Color(0xFF32ADE6), // ฟ้า
    Color(0xFF007AFF), // น้ำเงิน
    Color(0xFFAF52DE), // ม่วง
    Color(0xFFFF2D55), // ชมพูบานเย็น
    Color(0xFFFFB3C6), // พาสเทลชมพู
    Color(0xFFA2845E), // น้ำตาล
    Color(0xFF8E8E93), // เทา
    Color(0xFF1C1C1E), // ดำ
    Color(0xFFFFFFFF)  // ขาว
)

// ==================== MAIN COMPOSABLE ====================

@Composable
fun KidsColoringStudioScreen(
    onBack: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // State
    var templateIndex by remember { mutableIntStateOf(0) }
    val currentTemplate = COLORING_TEMPLATES[templateIndex]

    var currentTool by remember { mutableStateOf(ActiveTool.BRUSH) }
    var currentColor by remember { mutableStateOf(KIDS_PALETTE[0]) }
    var currentBrushWidth by remember { mutableFloatStateOf(24f) }

    // Canvas Strokes & History
    var strokes by remember { mutableStateOf<List<ColoredStroke>>(emptyList()) }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val undoStack = remember { mutableStateListOf<ColoredStroke>() }
    val redoStack = remember { mutableStateListOf<ColoredStroke>() }

    // Guide opacity: 0.15f (light), 0.40f (medium), 0.75f (vivid), 0.0f (hidden)
    var guideOpacity by remember { mutableFloatStateOf(0.40f) }

    // Dialog States
    var showTemplateSelector by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Timer & Scoring
    var playTimeSeconds by remember { mutableIntStateOf(0) }
    var userStars by remember { mutableIntStateOf(0) }
    var userLevel by remember { mutableIntStateOf(1) }

    val dataManager = remember { SupabaseDataManager.getInstance(context) }
    val authManager = remember { SupabaseAuthManager.getInstance(context) }
    val storedUser = remember { authManager.getStoredUser() }
    val userId = storedUser?.id ?: FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"

    // Initial load
    LaunchedEffect(Unit) {
        userStars = dataManager.getUserTotalStars()
        userLevel = dataManager.getUserCurrentLevel()

        if (userId.isNotEmpty()) {
            val prof = dataManager.fetchUserProfile(userId)
            prof.getOrNull()?.let {
                userStars = it.totalStars
                userLevel = it.currentLevel
            }
        }
    }

    // Play timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            playTimeSeconds++
        }
    }

    BackHandler {
        if (strokes.isNotEmpty()) {
            showClearConfirm = true
        } else {
            onBack()
        }
    }

    // Main Studio Layout
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF4F6FB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Top Header Bar
            KidsColoringTopBar(
                template = currentTemplate,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                guideOpacity = guideOpacity,
                onBack = onBack,
                onUndo = {
                    if (undoStack.isNotEmpty()) {
                        val last = undoStack.removeLast()
                        redoStack.add(last)
                        strokes = undoStack.toList()
                        playLocalSound(context, R.raw.button)
                    }
                },
                onRedo = {
                    if (redoStack.isNotEmpty()) {
                        val item = redoStack.removeLast()
                        undoStack.add(item)
                        strokes = undoStack.toList()
                        playLocalSound(context, R.raw.button)
                    }
                },
                onToggleGuide = {
                    guideOpacity = when (guideOpacity) {
                        0.40f -> 0.75f
                        0.75f -> 0.0f
                        0.0f -> 0.20f
                        else -> 0.40f
                    }
                    val msg = when (guideOpacity) {
                        0.75f -> "ลายเส้นชัดเจน 🌟"
                        0.0f -> "ซ่อนลายเส้นแล้ว 👁️"
                        0.20f -> "ลายเส้นจาง 🌿"
                        else -> "ลายเส้นปกติ ✨"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                },
                onOpenTemplateDialog = { showTemplateSelector = true },
                onFinish = {
                    if (strokes.isEmpty()) {
                        Toast.makeText(context, "ลองระบายสีก่อนน้า สู้ๆ จ้า! 🎨", Toast.LENGTH_SHORT).show()
                    } else {
                        playLocalSound(context, R.raw.is_sound_success_sound)
                        onSpeak("ว้าว ภาพระบายสี ${currentTemplate.title} สวยงามมากเลยจ้า ได้รับ 3 ดาวไปเลย!")
                        showCelebration = true

                        // Save scores
                        scope.launch {
                            val earnedStars = 3
                            val (newStars, nextLvl) = dataManager.addStars(userId, earnedStars)
                            userStars = newStars
                            userLevel = nextLvl

                            if (userId.isNotEmpty()) {
                                dataManager.saveQuizResult(
                                    userId = userId,
                                    topicId = 200,
                                    topicName = "ระบายสี: ${currentTemplate.title}",
                                    score = 100,
                                    totalQuestions = 1,
                                    timeSpentSeconds = playTimeSeconds
                                )
                                dataManager.saveLearningProgress(userId, "coloring", currentTemplate.id, 100)
                            }
                        }
                    }
                }
            )

            // 2. Middle Body: Canvas + Side Tools (if Landscape) or Centered Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Rail Tools (if landscape)
                    if (isLandscape) {
                        LandscapeToolsPanel(
                            currentTool = currentTool,
                            currentBrushWidth = currentBrushWidth,
                            onSelectTool = {
                                currentTool = it
                                playLocalSound(context, R.raw.button)
                            },
                            onChangeBrushWidth = {
                                currentBrushWidth = it
                                playLocalSound(context, R.raw.button)
                            },
                            onClearCanvas = { showClearConfirm = true }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Main Drawing Canvas Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(3.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                            .shadow(4.dp, RoundedCornerShape(20.dp))
                            .onSizeChanged { canvasSize = it }
                    ) {
                        // Layer 1: Guide Background Image
                        if (currentTemplate.resId != null && guideOpacity > 0f) {
                            Image(
                                painter = painterResource(id = currentTemplate.resId),
                                contentDescription = currentTemplate.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentScale = ContentScale.Fit,
                                alpha = guideOpacity
                            )
                        }

                        // Layer 2: Interactive Compose Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(currentTool, currentColor, currentBrushWidth) {
                                    awaitEachGesture {
                                        val down = awaitFirstDown()
                                        down.consume()
                                        var strokePoints = listOf(down.position)
                                        currentPoints = strokePoints

                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val drag = event.changes.firstOrNull { it.id == down.id } ?: break
                                            if (drag.pressed) {
                                                drag.consume()
                                                strokePoints = strokePoints + drag.position
                                                currentPoints = strokePoints
                                            } else {
                                                break
                                            }
                                        }

                                        if (strokePoints.isNotEmpty()) {
                                            val stroke = ColoredStroke(
                                                points = strokePoints,
                                                color = currentColor,
                                                strokeWidth = currentBrushWidth,
                                                isEraser = currentTool == ActiveTool.ERASER,
                                                isRainbow = currentTool == ActiveTool.RAINBOW
                                            )
                                            strokes = strokes + stroke
                                            undoStack.add(stroke)
                                            redoStack.clear()
                                            currentPoints = emptyList()
                                        }
                                    }
                                }
                        ) {
                            val activeStrokeList = if (currentPoints.isNotEmpty()) {
                                strokes + ColoredStroke(
                                    points = currentPoints,
                                    color = currentColor,
                                    strokeWidth = currentBrushWidth,
                                    isEraser = currentTool == ActiveTool.ERASER,
                                    isRainbow = currentTool == ActiveTool.RAINBOW
                                )
                            } else {
                                strokes
                            }

                            for (stroke in activeStrokeList) {
                                if (stroke.points.size > 1) {
                                    val path = Path().apply {
                                        moveTo(stroke.points[0].x, stroke.points[0].y)
                                        for (i in 1 until stroke.points.size) {
                                            lineTo(stroke.points[i].x, stroke.points[i].y)
                                        }
                                    }
                                    if (stroke.isEraser) {
                                        drawPath(
                                            path = path,
                                            color = Color.White,
                                            style = Stroke(
                                                width = stroke.strokeWidth,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    } else if (stroke.isRainbow) {
                                        drawPath(
                                            path = path,
                                            brush = Brush.sweepGradient(
                                                listOf(
                                                    Color(0xFFFF3B30),
                                                    Color(0xFFFF9500),
                                                    Color(0xFFFFCC00),
                                                    Color(0xFF34C759),
                                                    Color(0xFF007AFF),
                                                    Color(0xFFAF52DE),
                                                    Color(0xFFFF2D55),
                                                    Color(0xFFFF3B30)
                                                )
                                            ),
                                            style = Stroke(
                                                width = stroke.strokeWidth,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    } else {
                                        drawPath(
                                            path = path,
                                            color = stroke.color,
                                            style = Stroke(
                                                width = stroke.strokeWidth,
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    }
                                } else if (stroke.points.isNotEmpty()) {
                                    drawCircle(
                                        color = if (stroke.isEraser) Color.White else if (stroke.isRainbow) Color(0xFFFF9500) else stroke.color,
                                        radius = stroke.strokeWidth / 2f,
                                        center = stroke.points[0]
                                    )
                                }
                            }
                        }

                        // Floating Previous & Next Template Arrows on Canvas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    if (templateIndex > 0) {
                                        templateIndex--
                                        strokes = emptyList()
                                        undoStack.clear()
                                        redoStack.clear()
                                        playLocalSound(context, R.raw.button)
                                    }
                                },
                                enabled = templateIndex > 0,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                    .shadow(2.dp, CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ก่อนหน้า", tint = if (templateIndex > 0) Color(0xFF6366F1) else Color.LightGray)
                            }

                            IconButton(
                                onClick = {
                                    if (templateIndex < COLORING_TEMPLATES.size - 1) {
                                        templateIndex++
                                        strokes = emptyList()
                                        undoStack.clear()
                                        redoStack.clear()
                                        playLocalSound(context, R.raw.button)
                                    }
                                },
                                enabled = templateIndex < COLORING_TEMPLATES.size - 1,
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
                                    .shadow(2.dp, CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "ถัดไป", tint = if (templateIndex < COLORING_TEMPLATES.size - 1) Color(0xFF6366F1) else Color.LightGray)
                            }
                        }

                        // Bottom-Right mini template thumbnail badge
                        if (currentTemplate.resId != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(10.dp)
                                    .size(54.dp)
                                    .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(12.dp))
                                    .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = currentTemplate.resId),
                                    contentDescription = null,
                                    modifier = Modifier.size(42.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }

            // 3. Bottom Controls Panel: Palette & Tools (if Portrait)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .shadow(8.dp)
                    .padding(vertical = 6.dp)
            ) {
                // Color Palette Row
                ColorPaletteRow(
                    selectedColor = currentColor,
                    isRainbowActive = currentTool == ActiveTool.RAINBOW,
                    isEraserActive = currentTool == ActiveTool.ERASER,
                    onSelectColor = { color ->
                        currentColor = color
                        if (currentTool != ActiveTool.BRUSH) {
                            currentTool = ActiveTool.BRUSH
                        }
                        playLocalSound(context, R.raw.button)
                    },
                    onOpenColorPicker = { showColorPicker = true }
                )

                // Bottom Tools Row (Portrait Mode)
                if (!isLandscape) {
                    PortraitToolsRow(
                        currentTool = currentTool,
                        currentBrushWidth = currentBrushWidth,
                        onSelectTool = {
                            currentTool = it
                            playLocalSound(context, R.raw.button)
                        },
                        onChangeBrushWidth = {
                            currentBrushWidth = it
                            playLocalSound(context, R.raw.button)
                        },
                        onClearCanvas = { showClearConfirm = true }
                    )
                }
            }
        }
    }

    // ==================== DIALOGS ====================

    // 1. Template Selector Dialog
    if (showTemplateSelector) {
        TemplateSelectorDialog(
            currentIndex = templateIndex,
            onSelect = { idx ->
                templateIndex = idx
                strokes = emptyList()
                undoStack.clear()
                redoStack.clear()
                showTemplateSelector = false
                playLocalSound(context, R.raw.chiotrue)
                onSpeak("ระบายสี ${COLORING_TEMPLATES[idx].title} กันเถอะ!")
            },
            onDismiss = { showTemplateSelector = false }
        )
    }

    // 2. Clear Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Text("ล้างภาพวาด?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Text("ต้องการลบภาพที่ระบายทั้งหมด เพื่อเริ่มวาดใหม่ใช่ไหมครับ?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        strokes = emptyList()
                        undoStack.clear()
                        redoStack.clear()
                        showClearConfirm = false
                        playLocalSound(context, R.raw.button)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("ล้างภาพวาด", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("ยกเลิก")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 3. Color Picker Dialog
    if (showColorPicker) {
        CustomColorPickerDialog(
            onSelectColor = { c ->
                currentColor = c
                currentTool = ActiveTool.BRUSH
                showColorPicker = false
                playLocalSound(context, R.raw.button)
            },
            onDismiss = { showColorPicker = false }
        )
    }

    // 4. Celebration Win Dialog
    if (showCelebration) {
        ColoringCelebrationDialog(
            templateTitle = currentTemplate.title,
            timeSpentSeconds = playTimeSeconds,
            userStars = userStars,
            onSaveImage = {
                saveDrawingBitmap(
                    context = context,
                    strokes = strokes,
                    templateResId = currentTemplate.resId,
                    width = if (canvasSize.width > 0) canvasSize.width else 1080,
                    height = if (canvasSize.height > 0) canvasSize.height else 1080,
                    onSuccess = {
                        Toast.makeText(context, "บันทึกภาพลงแกลเลอรีสำเร็จ! 📸", Toast.LENGTH_LONG).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "บันทึกไม่สำเร็จ: $err", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onNextPicture = {
                showCelebration = false
                if (templateIndex < COLORING_TEMPLATES.size - 1) {
                    templateIndex++
                } else {
                    templateIndex = 0
                }
                strokes = emptyList()
                undoStack.clear()
                redoStack.clear()
                playTimeSeconds = 0
            },
            onDismiss = { showCelebration = false }
        )
    }
}

// ==================== SUB COMPOSABLES ====================

@Composable
fun KidsColoringTopBar(
    template: ColoringTemplate,
    canUndo: Boolean,
    canRedo: Boolean,
    guideOpacity: Float,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleGuide: () -> Unit,
    onOpenTemplateDialog: () -> Unit,
    onFinish: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(42.dp)
                .background(Color.White, CircleShape)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "กลับ", tint = Color(0xFF1E293B))
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Template Selector Badge Button
        Card(
            onClick = onOpenTemplateDialog,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.height(42.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(template.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = template.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Guide Visibility Toggle
        IconButton(
            onClick = onToggleGuide,
            modifier = Modifier
                .size(42.dp)
                .background(if (guideOpacity > 0f) Color(0xFFE0E7FF) else Color.White, CircleShape)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(
                Icons.Default.Visibility,
                contentDescription = "ความชัดภาพร่าง",
                tint = if (guideOpacity > 0f) Color(0xFF4F46E5) else Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Undo
        IconButton(
            onClick = onUndo,
            enabled = canUndo,
            modifier = Modifier
                .size(42.dp)
                .background(Color.White, CircleShape)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Undo,
                contentDescription = "ย้อนกลับ",
                tint = if (canUndo) Color(0xFF1E293B) else Color.LightGray
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Redo
        IconButton(
            onClick = onRedo,
            enabled = canRedo,
            modifier = Modifier
                .size(42.dp)
                .background(Color.White, CircleShape)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Redo,
                contentDescription = "ทำซ้ำ",
                tint = if (canRedo) Color(0xFF1E293B) else Color.LightGray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Finish / Claim Star Button
        Button(
            onClick = onFinish,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981)
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            modifier = Modifier.height(42.dp)
        ) {
            Text("เสร็จแล้ว 🎉", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
        }
    }
}

@Composable
fun ColorPaletteRow(
    selectedColor: Color,
    isRainbowActive: Boolean,
    isEraserActive: Boolean,
    onSelectColor: (Color) -> Unit,
    onOpenColorPicker: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(KIDS_PALETTE) { color ->
            val isSelected = !isRainbowActive && !isEraserActive && selectedColor == color
            val scale by animateFloatAsState(if (isSelected) 1.25f else 1.0f, label = "colorScale")

            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isSelected) 3.5.dp else 1.5.dp,
                        color = if (isSelected) Color(0xFF6366F1) else Color(0xFFCBD5E1),
                        shape = CircleShape
                    )
                    .clickable { onSelectColor(color) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = if (color == Color.White) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        item {
            // More Colors Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            listOf(
                                Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
                            )
                        )
                    )
                    .border(2.dp, Color(0xFFE2E8F0), CircleShape)
                    .clickable { onOpenColorPicker() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Palette, contentDescription = "เลือกสีเพิ่มเติม", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun PortraitToolsRow(
    currentTool: ActiveTool,
    currentBrushWidth: Float,
    onSelectTool: (ActiveTool) -> Unit,
    onChangeBrushWidth: (Float) -> Unit,
    onClearCanvas: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tool: Brush
        ToolToggleButton(
            title = "พู่กัน",
            emoji = "🖌️",
            isActive = currentTool == ActiveTool.BRUSH,
            onClick = { onSelectTool(ActiveTool.BRUSH) }
        )

        // Tool: Rainbow
        ToolToggleButton(
            title = "สายรุ้ง",
            emoji = "✨",
            isActive = currentTool == ActiveTool.RAINBOW,
            onClick = { onSelectTool(ActiveTool.RAINBOW) }
        )

        // Tool: Eraser
        ToolToggleButton(
            title = "ยางลบ",
            emoji = "🧽",
            isActive = currentTool == ActiveTool.ERASER,
            onClick = { onSelectTool(ActiveTool.ERASER) }
        )

        // Brush Sizes: S, M, L, XL
        Row(
            modifier = Modifier
                .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrushSizeItem(size = 12f, current = currentBrushWidth, onClick = { onChangeBrushWidth(12f) })
            BrushSizeItem(size = 24f, current = currentBrushWidth, onClick = { onChangeBrushWidth(24f) })
            BrushSizeItem(size = 40f, current = currentBrushWidth, onClick = { onChangeBrushWidth(40f) })
            BrushSizeItem(size = 60f, current = currentBrushWidth, onClick = { onChangeBrushWidth(60f) })
        }

        // Clear Canvas
        IconButton(
            onClick = onClearCanvas,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFFEE2E2), CircleShape)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "ล้าง", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun LandscapeToolsPanel(
    currentTool: ActiveTool,
    currentBrushWidth: Float,
    onSelectTool: (ActiveTool) -> Unit,
    onChangeBrushWidth: (Float) -> Unit,
    onClearCanvas: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(68.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolToggleButton(
                title = "แปรง",
                emoji = "🖌️",
                isActive = currentTool == ActiveTool.BRUSH,
                onClick = { onSelectTool(ActiveTool.BRUSH) }
            )

            ToolToggleButton(
                title = "สายรุ้ง",
                emoji = "✨",
                isActive = currentTool == ActiveTool.RAINBOW,
                onClick = { onSelectTool(ActiveTool.RAINBOW) }
            )

            ToolToggleButton(
                title = "ลบ",
                emoji = "🧽",
                isActive = currentTool == ActiveTool.ERASER,
                onClick = { onSelectTool(ActiveTool.ERASER) }
            )
        }

        // Sizes
        Column(
            modifier = Modifier
                .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BrushSizeItem(size = 12f, current = currentBrushWidth, onClick = { onChangeBrushWidth(12f) })
            BrushSizeItem(size = 24f, current = currentBrushWidth, onClick = { onChangeBrushWidth(24f) })
            BrushSizeItem(size = 40f, current = currentBrushWidth, onClick = { onChangeBrushWidth(40f) })
            BrushSizeItem(size = 60f, current = currentBrushWidth, onClick = { onChangeBrushWidth(60f) })
        }

        // Clear
        IconButton(
            onClick = onClearCanvas,
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFFEE2E2), CircleShape)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "ล้าง", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ToolToggleButton(
    title: String,
    emoji: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isActive) Color(0xFF6366F1) else Color(0xFFF1F5F9)
    val textColor = if (isActive) Color.White else Color(0xFF475569)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bg,
        modifier = Modifier.size(width = 54.dp, height = 48.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 16.sp)
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun BrushSizeItem(
    size: Float,
    current: Float,
    onClick: () -> Unit
) {
    val isSelected = current == size
    val dotDp = when (size) {
        12f -> 8.dp
        24f -> 14.dp
        40f -> 20.dp
        else -> 26.dp
    }

    Box(
        modifier = Modifier
            .size(30.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(dotDp)
                .background(
                    if (isSelected) Color(0xFF6366F1) else Color(0xFF94A3B8),
                    CircleShape
                )
        )
    }
}

// ==================== DIALOGS ====================

@Composable
fun TemplateSelectorDialog(
    currentIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("ทั้งหมด") }
    val categories = listOf("ทั้งหมด", "สัตว์น่ารัก", "พยัญชนะไทย", "ยานพาหนะ", "ของอร่อย", "วาดอิสระ")

    val filteredList = COLORING_TEMPLATES.mapIndexed { idx, item -> idx to item }.filter { (_, item) ->
        selectedCategory == "ทั้งหมด" || item.category == selectedCategory
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🖼️ เลือกภาพระบายสี",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด")
                    }
                }

                // Category Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF6366F1) else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Templates Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredList) { (origIdx, template) ->
                        val isCurrent = origIdx == currentIndex
                        Card(
                            onClick = { onSelect(origIdx) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isCurrent) Color(0xFFEEF2FF) else Color(0xFFF8FAFC)),
                            border = BorderStroke(
                                width = if (isCurrent) 2.5.dp else 1.dp,
                                color = if (isCurrent) Color(0xFF6366F1) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (template.resId != null) {
                                    Image(
                                        painter = painterResource(id = template.resId),
                                        contentDescription = template.title,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .padding(4.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🎨", fontSize = 32.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = template.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomColorPickerDialog(
    onSelectColor: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = listOf(
        Color(0xFFFF3B30), Color(0xFFFF9500), Color(0xFFFFCC00), Color(0xFF34C759),
        Color(0xFF00C853), Color(0xFF32ADE6), Color(0xFF007AFF), Color(0xFF5856D6),
        Color(0xFFAF52DE), Color(0xFFFF2D55), Color(0xFFFF69B4), Color(0xFFFFB3C6),
        Color(0xFF8D6E63), Color(0xFFA2845E), Color(0xFF4E342E), Color(0xFF263238),
        Color(0xFF607D8B), Color(0xFF90A4AE), Color(0xFFCFD8DC), Color(0xFFFFFFFF),
        Color(0xFFFF5252), Color(0xFFFF7043), Color(0xFFFFCA28), Color(0xFF66BB6A)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎨 เลือกสีตามใจชอบ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.height(240.dp)
                ) {
                    items(extendedColors) { c ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(1.5.dp, Color(0xFFCBD5E1), CircleShape)
                                .clickable { onSelectColor(c) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0))
                ) {
                    Text("ปิด", color = Color(0xFF475569))
                }
            }
        }
    }
}

@Composable
fun ColoringCelebrationDialog(
    templateTitle: String,
    timeSpentSeconds: Int,
    userStars: Int,
    onSaveImage: () -> Unit,
    onNextPicture: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉 สุดยอดเลยน้องๆ! 🌟", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(6.dp))
                Text("ระบายสี \"$templateTitle\" ได้สวยงามมาก", fontSize = 14.sp, color = Color(0xFF64748B))

                Spacer(modifier = Modifier.height(18.dp))

                // Stars Display
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐", fontSize = 42.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⭐", fontSize = 56.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⭐", fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("คะแนนที่ได้", fontSize = 12.sp, color = Color(0xFF92400E))
                        Text("+100 แต้ม", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ดาวสะสม", fontSize = 12.sp, color = Color(0xFF92400E))
                        Text("⭐ +3 ($userStars)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("เวลาที่ใช้", fontSize = 12.sp, color = Color(0xFF92400E))
                        val m = timeSpentSeconds / 60
                        val s = timeSpentSeconds % 60
                        Text("${m}:${"%02d".format(s)} นาที", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save to Gallery Button
                OutlinedButton(
                    onClick = onSaveImage,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF6366F1))
                ) {
                    Text("📸 บันทึกรูปลงในเครื่อง", fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Next Picture Button
                Button(
                    onClick = onNextPicture,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("🎨 ไประบายภาพต่อไป", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// ==================== IMAGE EXPORT HELPER ====================

fun saveDrawingBitmap(
    context: Context,
    strokes: List<ColoredStroke>,
    templateResId: Int?,
    width: Int,
    height: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val w = if (width > 0) width else 1080
        val h = if (height > 0) height else 1080

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        // Draw Template Image as background outline
        if (templateResId != null) {
            val templateBm = BitmapFactory.decodeResource(context.resources, templateResId)
            if (templateBm != null) {
                val scale = Math.min((w * 0.85f) / templateBm.width, (h * 0.85f) / templateBm.height)
                val destW = (templateBm.width * scale).toInt()
                val destH = (templateBm.height * scale).toInt()
                val left = (w - destW) / 2
                val top = (h - destH) / 2
                val destRect = android.graphics.Rect(left, top, left + destW, top + destH)

                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    alpha = 180
                }
                canvas.drawBitmap(templateBm, null, destRect, paint)
                templateBm.recycle()
            }
        }

        // Draw Strokes
        val strokePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
        }

        for (stroke in strokes) {
            strokePaint.strokeWidth = stroke.strokeWidth
            if (stroke.isEraser) {
                strokePaint.color = android.graphics.Color.WHITE
            } else if (stroke.isRainbow) {
                strokePaint.color = android.graphics.Color.parseColor("#FF9500")
            } else {
                strokePaint.color = stroke.color.toArgb()
            }

            if (stroke.points.size > 1) {
                val path = android.graphics.Path()
                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[i].x, stroke.points[i].y)
                }
                canvas.drawPath(path, strokePaint)
            } else if (stroke.points.isNotEmpty()) {
                val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    style = android.graphics.Paint.Style.FILL
                    color = strokePaint.color
                }
                canvas.drawCircle(stroke.points[0].x, stroke.points[0].y, stroke.strokeWidth / 2f, fillPaint)
            }
        }

        // Save to MediaStore
        val filename = "kids_coloring_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KidsColoring")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            onSuccess()
        } else {
            onError("Cannot create MediaStore record")
        }
        bitmap.recycle()
    } catch (e: Exception) {
        onError(e.message ?: "Unknown error")
    }
}

private fun playLocalSound(context: Context, resId: Int) {
    try {
        val mp = MediaPlayer.create(context, resId)
        mp?.setOnCompletionListener { it.release() }
        mp?.start()
    } catch (_: Exception) {}
}