package com.pongploydev.kokaiabckids.activity.learning

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BorderColor
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FormatAlignJustify
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.confixdata.DataManager
import com.pongploydev.kokaiabckids.confixdata.ThaiLearningDictionary
import com.pongploydev.kokaiabckids.drawingView.KidTracingCanvasView
import com.pongploydev.kokaiabckids.drawingView.KidTracingCanvasView.BrushTool
import com.pongploydev.kokaiabckids.model.ThaiAlphabetRepository
import com.pongploydev.profilefeature.auth.SupabaseAuthManager
import com.pongploydev.profilefeature.data.SupabaseDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FunTracingActivity : AppCompatActivity() {

    data class TracingItem(
        val symbol: String,
        val name: String,
        val thaiName: String = "",
        val rhyme: String = "",
        val imageRes: Int = 0,
        val audioRawRes: Int = 0,
        val soundResName: String = ""
    )

    private val itemList = mutableListOf<TracingItem>()
    private var categoryMode: String = "THAI"
    private var initialSymbol: String = "ก"
    private var initialName: String = "ก ไก่"
    private var initialThaiName: String = ""
    private var initialRhyme: String = ""
    private var initialImageRes: Int = 0
    private var initialAudioRawRes: Int = 0
    private var initialSoundResName: String = ""
    private var categoryPosition: Int = 1
    private var initialSelectedPosition: Int = 0

    private lateinit var dataManager: SupabaseDataManager
    private lateinit var authManager: SupabaseAuthManager

    private var successSound: MediaPlayer? = null
    private var letterMediaPlayer: MediaPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private var canvasViewRef: KidTracingCanvasView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Match light status bar background with dark legible icons
        @Suppress("DEPRECATION")
        window.statusBarColor = AndroidColor.parseColor("#E6F4FE")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        dataManager = SupabaseDataManager.getInstance(this)
        authManager = SupabaseAuthManager.getInstance(this)

        initIntentData()
        loadCategoryItems()
        setupSounds()
        setupTTS()

        val initialStars = dataManager.getUserTotalStars()

        setContent {
            MaterialTheme {
                FunTracingScreen(
                    items = itemList,
                    initialIndex = initialSelectedPosition,
                    categoryMode = categoryMode,
                    categoryPosition = categoryPosition,
                    initialStars = initialStars,
                    fallbackItem = TracingItem(
                        symbol = initialSymbol,
                        name = initialName,
                        thaiName = initialThaiName,
                        rhyme = initialRhyme,
                        imageRes = initialImageRes,
                        audioRawRes = initialAudioRawRes,
                        soundResName = initialSoundResName
                    ),
                    onCanvasCreated = { view -> canvasViewRef = view },
                    onSpeakItem = { item -> playLetterSound(item) },
                    onSaveArtwork = { item -> saveCurrentArtwork(item) },
                    onRewardCompleted = { item, onStarsUpdated ->
                        awardStarsAndSaveProgress(item, onStarsUpdated)
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun initIntentData() {
        intent?.let {
            initialSymbol = it.getStringExtra("symbol") ?: "ก"
            initialName = it.getStringExtra("name") ?: "ก ไก่"
            initialThaiName = it.getStringExtra("thaiName") ?: initialName
            initialRhyme = it.getStringExtra("rhyme") ?: ""
            initialImageRes = it.getIntExtra("imageRes", 0)
            initialAudioRawRes = it.getIntExtra("audioRawRes", 0)
            initialSoundResName = it.getStringExtra("soundResName") ?: ""
            categoryMode = it.getStringExtra("categoryMode") ?: "THAI"
            categoryPosition = it.getIntExtra("categoryPosition", 1)
            initialSelectedPosition = it.getIntExtra("selectedPosition", 0)
        }
    }

    private fun loadCategoryItems() {
        itemList.clear()
        when (categoryMode) {
            "THAI" -> {
                ThaiAlphabetRepository.getThaiAlphabetList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "THAI_VOWELS" -> {
                ThaiAlphabetRepository.getThaiVowelList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "TONE_GROUPS" -> {
                ThaiAlphabetRepository.getToneGroupsList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "NUMBERS" -> {
                val list = if (categoryPosition == 0) ThaiAlphabetRepository.getEnglishNumbersList() else ThaiAlphabetRepository.getNumbersList()
                list.forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "ENGLISH" -> {
                ThaiAlphabetRepository.getEnglishAlphabetList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "ANIMALS" -> {
                ThaiAlphabetRepository.getAnimalsList().forEach {
                    val cleanSymbol = it.name.split("•").firstOrNull()?.trim() ?: it.letter
                    itemList.add(TracingItem(cleanSymbol, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "FRUITS" -> {
                ThaiAlphabetRepository.getFruitsList().forEach {
                    val cleanSymbol = it.name.split("•").firstOrNull()?.trim() ?: it.letter
                    itemList.add(TracingItem(cleanSymbol, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "VEHICLES" -> {
                ThaiAlphabetRepository.getVehiclesList().forEach {
                    val cleanSymbol = it.name.split("•").firstOrNull()?.trim() ?: it.letter
                    itemList.add(TracingItem(cleanSymbol, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "BODY_PARTS" -> {
                ThaiAlphabetRepository.getBodyPartsList().forEach {
                    val cleanSymbol = it.name.split("•").firstOrNull()?.trim() ?: it.letter
                    itemList.add(TracingItem(cleanSymbol, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "COLORS_DAYS" -> {
                ThaiAlphabetRepository.getColorsAndDaysList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            "SPELLING" -> {
                ThaiAlphabetRepository.getSpellingList().forEach {
                    itemList.add(TracingItem(it.letter, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                }
            }
            else -> {
                when (categoryPosition) {
                    20 -> ThaiAlphabetRepository.getThaiAlphabetList().forEach {
                        itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                    }
                    21 -> ThaiAlphabetRepository.getThaiVowelList().forEach {
                        itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                    }
                    22 -> ThaiAlphabetRepository.getColorsAndDaysList().forEach {
                        itemList.add(TracingItem(it.letter, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                    }
                    23 -> ThaiAlphabetRepository.getSpellingList().forEach {
                        itemList.add(TracingItem(it.letter, it.name, it.letter, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                    }
                    in 0..19 -> {
                        val data = DataManager.getDataByCategory(categoryPosition)
                        if (data.isNotEmpty()) {
                            data.forEachIndexed { idx, model ->
                                val sym = if (categoryPosition == 0) ('A' + idx).toString() else if (categoryPosition == 1) idx.toString() else (idx + 1).toString()
                                val thaiLabel = ThaiLearningDictionary.toThai(model.showTitle, model.speakTitle)
                                itemList.add(TracingItem(sym, thaiLabel.ifBlank { model.showTitle ?: sym }, model.showTitle ?: "", model.speakTitle ?: ""))
                            }
                        } else {
                            ThaiAlphabetRepository.getThaiAlphabetList().forEach {
                                itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                            }
                        }
                    }
                    else -> {
                        ThaiAlphabetRepository.getThaiAlphabetList().forEach {
                            itemList.add(TracingItem(it.letter, it.name, it.name, it.rhyme, it.imageRes, it.audioRawRes, it.soundResName))
                        }
                    }
                }
            }
        }

        if (initialSelectedPosition !in itemList.indices && itemList.isNotEmpty()) {
            val matchedIndex = itemList.indexOfFirst {
                it.symbol == initialSymbol ||
                    it.name == initialName ||
                    (it.imageRes != 0 && it.imageRes == initialImageRes)
            }
            initialSelectedPosition = if (matchedIndex >= 0) matchedIndex else 0
        }
    }

    private fun setupSounds() {
        try {
            successSound = MediaPlayer.create(this, R.raw.is_sound_success_sound)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupTTS() {
        try {
            textToSpeech = TextToSpeech(this) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val activeItem = itemList.getOrNull(initialSelectedPosition)
                    val sym = activeItem?.symbol ?: initialSymbol
                    val title = activeItem?.name ?: initialName
                    val hasThai = sym.any { it in '\u0E00'..'\u0E7F' } ||
                        title.any { it in '\u0E00'..'\u0E7F' }
                    textToSpeech?.language = if (hasThai) Locale("th", "TH") else Locale.US
                    isTtsReady = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playLetterSound(item: TracingItem) {
        try {
            var played = false
            if (item.audioRawRes != 0) {
                playRawAudio(item.audioRawRes)
                played = true
            } else if (item.soundResName.isNotEmpty()) {
                val resId = resources.getIdentifier(item.soundResName, "raw", packageName)
                if (resId != 0) {
                    playRawAudio(resId)
                    played = true
                }
            }

            if (!played && isTtsReady) {
                val hasThai = item.symbol.any { it in '\u0E00'..'\u0E7F' } ||
                    item.name.any { it in '\u0E00'..'\u0E7F' }
                textToSpeech?.language = if (hasThai) Locale("th", "TH") else Locale.US
                val speakText = if (item.rhyme.isNotEmpty()) item.rhyme else item.name
                textToSpeech?.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "tracing_speak")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playRawAudio(resId: Int) {
        try {
            letterMediaPlayer?.release()
            letterMediaPlayer = MediaPlayer.create(this, resId)?.apply {
                setOnCompletionListener {
                    release()
                    letterMediaPlayer = null
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playSuccessSound() {
        try {
            successSound?.let {
                if (it.isPlaying) it.seekTo(0)
                it.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun awardStarsAndSaveProgress(item: TracingItem, onStarsUpdated: (Int) -> Unit) {
        playSuccessSound()
        val userId = authManager.getStoredUser()?.id
            ?: FirebaseAuth.getInstance().currentUser?.uid
            ?: authManager.getGuestUserId()

        lifecycleScope.launch {
            val (newStars, _) = dataManager.addStars(userId, 10)
            dataManager.saveLearningProgress(
                userId = userId,
                categoryKey = "tracing_${categoryMode.lowercase(Locale.US)}",
                itemKey = item.symbol,
                score = 100
            )
            onStarsUpdated(newStars)
        }
    }

    private fun saveCurrentArtwork(item: TracingItem) {
        val canvas = canvasViewRef ?: return
        val bitmap = canvas.exportArtworkBitmap()
        if (bitmap == null) {
            Toast.makeText(this, "ไม่สามารถสร้างไฟล์รูปภาพได้", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val saved = try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "KokaiABC_${item.symbol}_$timestamp.png"
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/KokaiABCKids")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    var stream: OutputStream? = null
                    try {
                        stream = contentResolver.openOutputStream(uri)
                        if (stream != null) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        }
                    } finally {
                        stream?.close()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val doneValues = ContentValues().apply {
                            put(MediaStore.Images.Media.IS_PENDING, 0)
                        }
                        contentResolver.update(uri, doneValues, null, null)
                    }
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            } finally {
                if (!bitmap.isRecycled) bitmap.recycle()
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@FunTracingActivity,
                    if (saved) "บันทึกรูปภาพลงแกลเลอรีแล้ว" else "บันทึกรูปภาพไม่สำเร็จ",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        successSound?.release()
        successSound = null
        letterMediaPlayer?.release()
        letterMediaPlayer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}

private data class BrushSizeOption(val label: String, val sizeDp: Float, val dotDp: Int)

private val BRUSH_SIZE_OPTIONS = listOf(
    BrushSizeOption("S", 10f, 8),
    BrushSizeOption("M", 18f, 13),
    BrushSizeOption("L", 30f, 18)
)

private val COLOR_PALETTE = listOf(
    Color(0xFFEF4444), // Red
    Color(0xFFF97316), // Orange
    Color(0xFFFACC15), // Yellow
    Color(0xFF84CC16), // Lime
    Color(0xFF22C55E), // Green
    Color(0xFF14B8A6), // Teal
    Color(0xFF0EA5E9), // Sky
    Color(0xFF2563EB), // Blue
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFF8D5524), // Brown
    Color(0xFF1F2937)  // Charcoal
)

@Composable
private fun FunTracingScreen(
    items: List<FunTracingActivity.TracingItem>,
    initialIndex: Int,
    categoryMode: String,
    categoryPosition: Int,
    initialStars: Int,
    fallbackItem: FunTracingActivity.TracingItem,
    onCanvasCreated: (KidTracingCanvasView) -> Unit,
    onSpeakItem: (FunTracingActivity.TracingItem) -> Unit,
    onSaveArtwork: (FunTracingActivity.TracingItem) -> Unit,
    onRewardCompleted: (FunTracingActivity.TracingItem, (Int) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceAtLeast(0)) }
    val currentItem = if (items.isNotEmpty() && currentIndex in items.indices) items[currentIndex] else fallbackItem
    val totalCount = items.size.coerceAtLeast(1)

    var totalStars by remember { mutableIntStateOf(initialStars) }
    var tracingProgress by remember { mutableFloatStateOf(0f) }
    var hasCompletedCurrent by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    var selectedTool by remember { mutableStateOf(BrushTool.CRAYON) }
    var selectedColor by remember { mutableStateOf(COLOR_PALETTE.first()) }
    var selectedSizeIndex by remember { mutableIntStateOf(1) }
    var showPaperLines by remember { mutableStateOf(true) }
    var showGuide by remember { mutableStateOf(true) }

    var canUndo by remember { mutableStateOf(false) }
    var canRedo by remember { mutableStateOf(false) }
    var canvasView by remember { mutableStateOf<KidTracingCanvasView?>(null) }

    val animatedProgress by animateFloatAsState(
        targetValue = tracingProgress.coerceIn(0f, 1f),
        animationSpec = tween(250),
        label = "tracingProgress"
    )

    // Speak automatically when switching item
    LaunchedEffect(currentIndex) {
        onSpeakItem(currentItem)
    }

    fun triggerCompletion() {
        if (!hasCompletedCurrent) {
            hasCompletedCurrent = true
            tracingProgress = 1f
            onRewardCompleted(currentItem) { updatedStars ->
                totalStars = updatedStars
            }
        }
        showCompleteDialog = true
    }

    fun goToIndex(newIndex: Int) {
        if (newIndex !in items.indices) return
        currentIndex = newIndex
        hasCompletedCurrent = false
        tracingProgress = 0f
        val nextItem = items[newIndex]
        canvasView?.loadItem(nextItem.symbol, nextItem.imageRes)
    }

    val titleText = remember(currentItem, categoryMode, categoryPosition) {
        if (currentItem.imageRes != 0) {
            "ระบายสี ${currentItem.name}"
        } else {
            "ฝึกเขียน ${currentItem.name}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE6F4FE),
                        Color(0xFFF3F8FD),
                        Color(0xFFF8FAFC)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        // ==================== TOP BAR ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFDCE7F3)),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onBack() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "ย้อนกลับ",
                        tint = Color(0xFF1F2937),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Clickable title to replay audio
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSpeakItem(currentItem) }
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = "ฟังเสียง",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Real Star Counter
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$totalStars",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }

        // ==================== PROGRESS & QUICK TOOLS STRIP ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Slim 3-star progress bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFDAE8F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF22C55E))
                            )
                        )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                val starThresholds = listOf(0.25f, 0.55f, 0.72f)
                starThresholds.forEach { threshold ->
                    val earned = tracingProgress >= threshold || hasCompletedCurrent
                    Icon(
                        imageVector = if (earned) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                        contentDescription = null,
                        tint = if (earned) Color(0xFFF59E0B) else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (items.size > 1) {
                Text(
                    text = "${currentIndex + 1}/$totalCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Action Tools Row (Undo, Redo, Sound, Lines, Guide, Save, Clear)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ToolIconChip(
                    icon = Icons.AutoMirrored.Rounded.Undo,
                    contentDescription = "ย้อนกลับเส้น",
                    enabled = canUndo,
                    onClick = { canvasView?.undo() }
                )
                ToolIconChip(
                    icon = Icons.AutoMirrored.Rounded.Redo,
                    contentDescription = "ทำซ้ำ",
                    enabled = canRedo,
                    onClick = { canvasView?.redo() }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ToolIconChip(
                    icon = Icons.Rounded.FormatAlignJustify,
                    contentDescription = "แสดงหรือซ่อนเส้นบรรทัด",
                    active = showPaperLines,
                    onClick = {
                        showPaperLines = !showPaperLines
                        canvasView?.showPaperLines = showPaperLines
                    }
                )
                ToolIconChip(
                    icon = if (showGuide) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    contentDescription = "แสดงหรือซ่อนแบบร่าง",
                    active = showGuide,
                    onClick = {
                        showGuide = !showGuide
                        canvasView?.showGuide = showGuide
                    }
                )
                ToolIconChip(
                    icon = Icons.Rounded.SaveAlt,
                    contentDescription = "บันทึกรูปภาพ",
                    onClick = { onSaveArtwork(currentItem) }
                )
                ToolIconChip(
                    icon = Icons.Rounded.DeleteOutline,
                    contentDescription = "ล้างกระดาน",
                    tint = Color(0xFFDC2626),
                    onClick = {
                        canvasView?.clearCanvas()
                        hasCompletedCurrent = false
                        tracingProgress = 0f
                    }
                )
            }
        }

        // ==================== MAIN DRAWING CANVAS ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 2.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFDCE6F2)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    KidTracingCanvasView(ctx).apply {
                        currentColor = selectedColor.toArgb()
                        currentTool = selectedTool
                        brushSizeDp = BRUSH_SIZE_OPTIONS[selectedSizeIndex].sizeDp
                        this.showPaperLines = showPaperLines
                        this.showGuide = showGuide
                        loadItem(currentItem.symbol, currentItem.imageRes)
                        tracingListener = object : KidTracingCanvasView.OnTracingListener {
                            override fun onProgressUpdate(progress: Float) {
                                post { tracingProgress = progress }
                            }

                            override fun onTracingCompleted() {
                                post {
                                    if (!hasCompletedCurrent) {
                                        triggerCompletion()
                                    }
                                }
                            }
                        }
                        onHistoryChanged = { undoAvail, redoAvail ->
                            post {
                                canUndo = undoAvail
                                canRedo = redoAvail
                            }
                        }
                        canvasView = this
                        onCanvasCreated(this)
                    }
                },
                update = { view ->
                    view.currentTool = selectedTool
                    if (selectedTool != BrushTool.ERASER) {
                        view.currentColor = selectedColor.toArgb()
                    }
                    view.brushSizeDp = BRUSH_SIZE_OPTIONS[selectedSizeIndex].sizeDp
                    view.showPaperLines = showPaperLines
                    view.showGuide = showGuide
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ==================== BOTTOM DRAWING DOCK ====================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
            color = Color.White,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Row 1: Brush Tools + Brush Size Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BrushModeChip(
                            label = "ดินสอ",
                            icon = Icons.Rounded.Create,
                            selected = selectedTool == BrushTool.CRAYON,
                            accentColor = Color(0xFF2563EB),
                            onClick = { selectedTool = BrushTool.CRAYON }
                        )
                        BrushModeChip(
                            label = "ไฮไลท์",
                            icon = Icons.Rounded.BorderColor,
                            selected = selectedTool == BrushTool.MARKER,
                            accentColor = Color(0xFF0D9488),
                            onClick = { selectedTool = BrushTool.MARKER }
                        )
                        BrushModeChip(
                            label = "สายรุ้ง",
                            icon = Icons.Rounded.Palette,
                            selected = selectedTool == BrushTool.RAINBOW,
                            accentColor = Color(0xFF9333EA),
                            onClick = { selectedTool = BrushTool.RAINBOW }
                        )
                        BrushModeChip(
                            label = "ประกาย",
                            icon = Icons.Rounded.AutoAwesome,
                            selected = selectedTool == BrushTool.SPARKLE,
                            accentColor = Color(0xFFD97706),
                            onClick = { selectedTool = BrushTool.SPARKLE }
                        )
                        BrushModeChip(
                            label = "ยางลบ",
                            icon = Icons.Rounded.CleaningServices,
                            selected = selectedTool == BrushTool.ERASER,
                            accentColor = Color(0xFFE11D48),
                            onClick = { selectedTool = BrushTool.ERASER }
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 3 Brush size dots (S / M / L)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BRUSH_SIZE_OPTIONS.forEachIndexed { idx, opt ->
                                val isSelected = selectedSizeIndex == idx
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else Color.Transparent)
                                        .clickable { selectedSizeIndex = idx },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(opt.dotDp.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (selectedTool == BrushTool.ERASER) Color(0xFF64748B)
                                                else selectedColor
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                // Row 2: 12-Color Palette
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    COLOR_PALETTE.forEach { color ->
                        val isSelected = selectedColor == color &&
                            selectedTool != BrushTool.ERASER &&
                            selectedTool != BrushTool.RAINBOW
                        val swatchSize by animateDpAsState(
                            targetValue = if (isSelected) 34.dp else 28.dp,
                            label = "swatchSize"
                        )
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clickable {
                                    selectedColor = color
                                    if (selectedTool == BrushTool.ERASER || selectedTool == BrushTool.RAINBOW) {
                                        selectedTool = BrushTool.CRAYON
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .border(2.5.dp, color, CircleShape)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(swatchSize)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.padding(horizontal = 14.dp)
                )

                // Row 3: Prev / Done / Next Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val canPrev = currentIndex > 0
                    val canNext = currentIndex < items.size - 1

                    OutlinedButton(
                        onClick = { goToIndex(currentIndex - 1) },
                        enabled = canPrev,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (canPrev) Color(0xFFCBD5E1) else Color(0xFFE2E8F0)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            contentDescription = "ก่อนหน้า",
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "ก่อนหน้า",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { triggerCompletion() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "เสร็จสิ้น",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = { goToIndex(currentIndex + 1) },
                        enabled = canNext,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (canNext) Color(0xFFCBD5E1) else Color(0xFFE2E8F0)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "ถัดไป",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = "ถัดไป",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCompleteDialog) {
        val hasNext = currentIndex < items.size - 1
        val actionText = if (currentItem.imageRes != 0) "ระบายสี" else "ฝึกเขียน"
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "ยอดเยี่ยมมาก!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "$actionText \"${currentItem.name}\" เสร็จเรียบร้อยแล้ว ได้รับเพิ่ม +10 ดาว",
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (hasNext) {
                    val nextItem = items[currentIndex + 1]
                    Button(
                        onClick = {
                            showCompleteDialog = false
                            goToIndex(currentIndex + 1)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("ถัดไป: ${nextItem.symbol}", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            showCompleteDialog = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("เสร็จสิ้น", color = Color.White)
                    }
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showCompleteDialog = false
                            canvasView?.clearCanvas()
                            hasCompletedCurrent = false
                            tracingProgress = 0f
                        }
                    ) {
                        Text("ทำอีกครั้ง")
                    }
                    TextButton(
                        onClick = {
                            showCompleteDialog = false
                            onSaveArtwork(currentItem)
                        }
                    ) {
                        Text("บันทึกรูป")
                    }
                }
            }
        )
    }
}

@Composable
private fun ToolIconChip(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    active: Boolean = false,
    tint: Color = Color(0xFF334155),
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            active -> Color(0xFFE0F2FE)
            else -> Color.White
        },
        label = "toolChipBg"
    )
    val iconColor = when {
        !enabled -> Color(0xFFCBD5E1)
        active -> Color(0xFF0284C7)
        else -> tint
    }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(1.dp, if (active) Color(0xFF7DD3FC) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .size(36.dp)
            .clickable(enabled = enabled) { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun BrushModeChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(11.dp),
        color = if (selected) accentColor.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accentColor else Color(0xFFE2E8F0)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) accentColor else Color(0xFF64748B),
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) accentColor else Color(0xFF475569)
            )
        }
    }
}
