//package com.pongploydev.priyankvex.paintapp.activity.learning.showKids
//
//// <--- [เปลี่ยนแปลง] 1. เพิ่ม import
//
//import android.annotation.SuppressLint
//import android.app.AlertDialog
//import android.content.ContentValues
//import android.content.DialogInterface
//import android.content.res.Configuration
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.Color
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.CountDownTimer
//import android.os.Handler
//import android.os.Looper
//import android.provider.MediaStore
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import android.widget.GridLayout
//import android.widget.ImageButton
//import android.widget.ImageView
//import android.widget.SeekBar
//import android.widget.TextView
//import android.widget.Toast
//import androidx.annotation.ArrayRes
//import androidx.annotation.ColorInt
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.button.MaterialButton
//import com.pongploydev.priyankvex.paintapp.R
//import com.pongploydev.priyankvex.paintapp.activity.MainActivity
//import com.pongploydev.priyankvex.paintapp.drawingView.DrawingView
//import eltos.simpledialogfragment.SimpleDialog
//import eltos.simpledialogfragment.SimpleDialog.OnDialogResultListener.BUTTON_POSITIVE
//import eltos.simpledialogfragment.color.SimpleColorDialog
//import eltos.simpledialogfragment.color.SimpleColorWheelDialog
//import java.io.OutputStream
//
//
//// <--- [เปลี่ยนแปลง] 2. เพิ่มการ implement OnDialogResultListener
//class ColoringGameActivity : AppCompatActivity(), SimpleDialog.OnDialogResultListener {
//
//    // UI Components
//    private lateinit var mDrawingView: DrawingView
//    private lateinit var textScore: TextView
//    private lateinit var textTime: TextView
//    private lateinit var textBrushSize: TextView
//    private lateinit var seekBarBrush: SeekBar
//    private lateinit var guideImage: ImageView
//    private lateinit var textTips: TextView
//
//    // Control Buttons
//    private lateinit var buttonBack: ImageButton
//    private lateinit var buttonHelp: ImageButton
//    private lateinit var buttonSave: MaterialButton
//    private lateinit var buttonNew: MaterialButton
//    private lateinit var buttonBrush: MaterialButton
//    private lateinit var buttonErase: MaterialButton
//    private lateinit var buttonColorPicker: MaterialButton // ปุ่มใหม่สำหรับ Color Picker
//
//    // Color Buttons
//    private lateinit var colorRed: ImageButton
//    private lateinit var colorOrange: ImageButton
//    private lateinit var colorYellow: ImageButton
//    private lateinit var colorGreen: ImageButton
//    private lateinit var colorBlue: ImageButton
//    private lateinit var colorPurple: ImageButton
//    private lateinit var colorPink: ImageButton
//    private lateinit var colorBrown: ImageButton
//    private lateinit var colorBlack: ImageButton
//    private lateinit var colorWhite: ImageButton
//
//    private var smallBrush = 0f
//    var mediumBrush: Float = 0f
//    var largeBrush: Float = 0f
//
//
//    // Game State
//    private var score = 0
//
//    private  var counter = 0
//
//    private var timeElapsed = 0L
//    private var isEraseMode = false
//    private var currentBrushSize = 25f
//    private var isLandscape = false
//    private lateinit var timer: CountDownTimer
//    private var currentColor = "#FF6B6B" // สีปัจจุบัน
//
//    @ColorInt
//    private  val color = -0x63d850
//
//
//    // Color Management
//    private val colorButtons = mutableListOf<ImageButton>()
//    private var currentColorButton: ImageButton? = null
//    private val colorMap = mapOf(
//        "#FF6B6B" to "🔴 สีแดง",
//        "#FF9E6D" to "🟠 สีส้ม",
//        "#FFD166" to "🟡 สีเหลือง",
//        "#06D6A0" to "🟢 สีเขียว",
//        "#4ECDC4" to "🔵 สีฟ้า",
//        "#7209B7" to "🟣 สีม่วง",
//        "#F15BB5" to "💖 สีชมพู",
//        "#964B00" to "🟤 สีน้ำตาล",
//        "#000000" to "⚫ สีดำ",
//        "#FFFFFF" to "⚪ สีขาว"
//    )
//
//
//
//    // <--- [เปลี่ยนแปลง] 3. เพิ่ม Tag สำหรับ Dialog
//    private val DIALOG_NEW_DRAWING = "newDrawingDialogTag"
//    private val DIALOG_EXIT = "exitDialogTag"
//    // (เพิ่ม Tag อื่นๆ ที่นี่ได้ถ้าต้องการแปลง Dialog อื่น)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_coloring)
//
//        // ตรวจสอบ orientation
//        isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
//
//        initializeViews()
//        setupDrawingView()
//        setupColorButtons()
//        setupToolButtons()
//        setupSeekBar()
//        setupColorPicker()
//        startTimer()
//        showWelcomeMessage()
//        setupBrushSizes()
//    }
//
//
//
//    private fun initializeViews() {
//        // Initialize main components
//        mDrawingView = findViewById(R.id.drawing)
//        textScore = findViewById(R.id.text_score)
//        textTime = findViewById(R.id.text_time)
//        textBrushSize = findViewById(R.id.text_brush_size)
//        seekBarBrush = findViewById(R.id.seekbar_brush)
//        guideImage = findViewById(R.id.guide_image)
//        textTips = findViewById(R.id.text_tips)
//
//        // Initialize control buttons
//        buttonBack = findViewById(R.id.button_back)
//        buttonHelp = findViewById(R.id.button_help)
//        buttonSave = findViewById(R.id.button_save)
//        buttonNew = findViewById(R.id.button_new)
//        buttonBrush = findViewById(R.id.button_brush)
//        buttonErase = findViewById(R.id.button_erase)
//        buttonColorPicker = findViewById(R.id.button_color_picker) // ปุ่ม Color Picker ใหม่
//
//        // Initialize color buttons
//        initializeColorButtons()
//
//        // ตั้งค่าเริ่มต้นตาม orientation
//        if (isLandscape) {
//            setupLandscapeLayout()
//        } else {
//            setupPortraitLayout()
//        }
//    }
//
//    private fun initializeColorButtons() {
//        colorRed = findViewById(R.id.color_red)
//        colorOrange = findViewById(R.id.color_orange)
//        colorYellow = findViewById(R.id.color_yellow)
//        colorGreen = findViewById(R.id.color_green)
//        colorBlue = findViewById(R.id.color_blue)
//        colorPurple = findViewById(R.id.color_purple)
//        colorPink = findViewById(R.id.color_pink)
//        colorBrown = findViewById(R.id.color_brown)
//        colorBlack = findViewById(R.id.color_black)
//        colorWhite = findViewById(R.id.color_white)
//
//        // Add to list for management
//        colorButtons.addAll(listOf(
//            colorRed, colorOrange, colorYellow, colorGreen,
//            colorBlue, colorPurple, colorPink, colorBrown,
//            colorBlack, colorWhite
//        ))
//    }
//
//    private fun setupLandscapeLayout() {
//        currentBrushSize = 20f
//        seekBarBrush.progress = 40
//        updateBrushSizeDisplay()
//        showTemporaryTips("💡 หมุนหน้าจอเป็นแนวตั้งสำหรับพื้นที่วาดภาพที่ใหญ่ขึ้น!")
//    }
//
//    private fun setupPortraitLayout() {
//        currentBrushSize = 25f
//        seekBarBrush.progress = 50
//        updateBrushSizeDisplay()
//    }
//
//    private fun setupDrawingView() {
//        mDrawingView.setBrushSize(currentBrushSize)
//        mDrawingView.setColor(currentColor)
//        mDrawingView.setErase(false)
//
//        // ตั้งค่า listener สำหรับการตรวจสอบการวาด
//        mDrawingView.setDrawingCheckListener(object : DrawingView.DrawingCheckListener {
//            override fun onDrawingChecked(isCorrect: Boolean, accuracy: Float) {
//                handleDrawingResult(isCorrect, accuracy)
//            }
//
//            override fun onDrawingProgress(progress: Float) {
//                // สามารถแสดงความคืบหน้าได้ถ้าต้องการ
//            }
//
//            override fun onStrokeCompleted(strokeIndex: Int, isCorrect: Boolean) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onLetterCompleted(isPerfect: Boolean) {
//                TODO("Not yet implemented")
//            }
//        })
//    }
//
//    private fun setupColorButtons() {
//        // Set up click listeners for all color buttons
//        colorRed.setOnClickListener { selectColor(colorRed, "#FF6B6B") }
//        colorOrange.setOnClickListener { selectColor(colorOrange, "#FF9E6D") }
//        colorYellow.setOnClickListener { selectColor(colorYellow, "#FFD166") }
//        colorGreen.setOnClickListener { selectColor(colorGreen, "#06D6A0") }
//        colorBlue.setOnClickListener { selectColor(colorBlue, "#4ECDC4") }
//        colorPurple.setOnClickListener { selectColor(colorPurple, "#7209B7") }
//        colorPink.setOnClickListener { selectColor(colorPink, "#F15BB5") }
//        colorBrown.setOnClickListener { selectColor(colorBrown, "#964B00") }
//        colorBlack.setOnClickListener { selectColor(colorBlack, "#000000") }
//        colorWhite.setOnClickListener { selectColor(colorWhite, "#FFFFFF") }
//
//        // Select initial color (red)
//        selectInitialColor()
//    }
//
//    private fun setupColorPicker() {
//        // ตั้งค่า Color Picker Dialog
//        buttonColorPicker.setOnClickListener {
//            showColorPickerDialog()
//        }
//    }
//
//    private fun showColorPickerDialog() {
//        // ใช้ AlertDialog ธรรมดาแต่มีสีสันสำหรับเด็ก
//        showCustomColorPickerDialog()
//    }
//
//    @SuppressLint("MissingInflatedId")
//    private fun showCustomColorPickerDialog() {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_color_picker, null)
//        val colorGrid = dialogView.findViewById<GridLayout>(R.id.color_grid_custom)
//        val btRandomColor = dialogView.findViewById<Button>(R.id.button_random_color)
//
//
//        btRandomColor.setOnClickListener {
////            SimpleColorDialog.build()
////                .title(R.string.pick_a_color)
////                .choiceMode(CustomListDialog.SINGLE_CHOICE)
////                .colors(this, SimpleColorDialog.COLORFUL_COLOR_PALLET)
////                .choiceIdPreset(longArrayOf(-0x874))
////                .choiceMin(2).choiceMax(5)
////                .allowCustom(true)
////                .showOutline(SimpleColorDialog.AUTO)
////                .show(this, "DIALOG_PICK_COLOR")
//            SimpleColorDialog.build()
//                .title(R.string.pick_a_color)
//                .allowCustom(true)
//                .colorPreset(0xff009688.toInt())
//
//                .show(this, "DIALOG_PICK_COLOR")
//                ;
//
//            (it as? android.app.Dialog)?.dismiss()
//
//
//        }
//
//
//
//
//        // สีเพิ่มเติมสำหรับเด็ก
//        val extendedColors = listOf(
//            "#FF6B6B" to "🔴", "#FF9E6D" to "🟠", "#FFD166" to "🟡",
//            "#06D6A0" to "🟢", "#4ECDC4" to "🔵", "#7209B7" to "🟣",
//            "#F15BB5" to "💖", "#964B00" to "🟤", "#000000" to "⚫", "#FFFFFF" to "⚪",
//            "#FF5252" to "❤️", "#FF4081" to "💕", "#E040FB" to "💜",
//            "#7C4DFF" to "🔮", "#536DFE" to "🔵", "#448AFF" to "💙",
//            "#40C4FF" to "🌐", "#18FFFF" to "💎", "#64FFDA" to "🧊",
//            "#69F0AE" to "🌿", "#B2FF59" to "🍏", "#EEFF41" to "💛",
//            "#FFFF00" to "⭐", "#FFD740" to "🌟", "#FFAB40" to "🎃",
//            "#FF6E40" to "🔥", "#795548" to "🪵", "#9E9E9E" to "⚪"
//        )
//
//        // สร้างปุ่มสีใน Grid
//        extendedColors.forEach { (colorHex, emoji) ->
//            val colorButton = Button(this).apply {
//                text = emoji
//                textSize = 16f
//                setBackgroundColor(Color.parseColor(colorHex))
//                setTextColor(if (isColorDark(Color.parseColor(colorHex))) Color.WHITE else Color.BLACK)
//                layoutParams = GridLayout.LayoutParams().apply {
//                    width = 0
//                    height = GridLayout.LayoutParams.WRAP_CONTENT
//                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
//                    setMargins(4, 4, 4, 4)
//                }
//                setOnClickListener {
//                    onCustomColorSelected(Color.parseColor(colorHex), colorHex)
//                    (it as? android.app.Dialog)?.dismiss()
//                }
//            }
//            colorGrid.addView(colorButton)
//        }
//
//        androidx.appcompat.app.AlertDialog.Builder(this)
//            .setTitle("🎨 เลือกสีสวยๆ")
//            .setView(dialogView)
//            .setNegativeButton("ปิด") { dialog, _ -> dialog.dismiss() }
//            .show()
//    }
//
//    private fun onCustomColorSelected(color: Int, colorHex: String) {
//        // อัพเดทสีปัจจุบัน
//        currentColor = colorHex
//        mDrawingView.setColor(colorHex)
//        mDrawingView.setErase(false)
//        isEraseMode = false
//        //updateToolButtons()
//
//        // แสดงข้อความเลือกสี
//        showCustomColorSelection(colorHex, color)
//
//        // อัพเดทปุ่ม Color Picker เพื่อแสดงสีที่เลือก
//        updateColorPickerButton(color)
//
//        // รีเซ็ตการเลือกปุ่มสีพื้นฐาน
//        resetBasicColorSelection()
//    }
//
//    private fun updateColorPickerButton(color: Int) {
//        // เปลี่ยนพื้นหลังปุ่ม Color Picker เป็นสีที่เลือก
//        buttonColorPicker.setBackgroundColor(color)
//
//        // เปลี่ยนสีไอคอนตามความเข้มของสีพื้นหลัง
////        if (isColorDark(color)) {
////            buttonColorPicker.setColorFilter(Color.WHITE)
////        } else {
////            buttonColorPicker.setColorFilter(Color.BLACK)
////        }
//    }
//
//    private fun isColorDark(color: Int): Boolean {
//        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
//        return darkness > 0.5
//    }
//
//    private fun resetBasicColorSelection() {
//        // รีเซ็ตปุ่มสีพื้นฐานทั้งหมด
//        colorButtons.forEach { button ->
//            // แก้ไข: ต้องแน่ใจว่า tag ไม่ใช่ null
//            button.tag?.toString()?.let { colorHex ->
//                try {
//                    val color = Color.parseColor(colorHex)
//                    button.setBackgroundColor(color)
//                } catch (e: Exception) {
//                    // จัดการกรณีที่ tag ไม่ใช่สี
//                }
//            }
//        }
//        currentColorButton = null
//    }
//
//    private fun showCustomColorSelection(colorHex: String, color: Int) {
//        val colorName = getColorName(color)
//        showSuccessMessage("เลือกสี: $colorName")
//
//        // แสดงค่าสี RGB
//        val rgbText = "RGB: ${Color.red(color)}, ${Color.green(color)}, ${Color.blue(color)}"
//        showTemporaryTips("$colorName\n$rgbText")
//    }
//
//    private fun getColorName(color: Int): String {
//        return when (color) {
//            Color.RED -> "🔴 สีแดง"
//            Color.BLUE -> "🔵 สีน้ำเงิน"
//            Color.GREEN -> "🟢 สีเขียว"
//            Color.YELLOW -> "🟡 สีเหลือง"
//            Color.MAGENTA -> "🟣 สีม่วง"
//            Color.CYAN -> "🔷 สีฟ้า"
//            Color.BLACK -> "⚫ สีดำ"
//            Color.WHITE -> "⚪ สีขาว"
//            else -> {
//                // ตรวจสอบสีที่ใกล้เคียง
//                val red = Color.red(color)
//                val green = Color.green(color)
//                val blue = Color.blue(color)
//
//                when {
//                    red > 200 && green < 100 && blue < 100 -> "🔴 สีแดง"
//                    red > 200 && green > 150 && blue < 100 -> "🟠 สีส้ม"
//                    red > 200 && green > 200 && blue < 100 -> "🟡 สีเหลือง"
//                    red < 100 && green > 150 && blue < 100 -> "🟢 สีเขียว"
//                    red < 100 && green > 200 && blue > 200 -> "🔵 สีฟ้า"
//                    red < 100 && green < 100 && blue > 150 -> "🔵 สีน้ำเงิน"
//                    red > 150 && green < 100 && blue > 150 -> "🟣 สีม่วง"
//                    red > 200 && green < 150 && blue > 150 -> "💖 สีชมพู"
//                    red > 100 && green > 50 && blue < 50 -> "🟤 สีน้ำตาล"
//                    else -> "🎨 สีพิเศษ"
//                }
//            }
//        }
//    }
//
//    private fun selectInitialColor() {
//        selectColor(colorRed, "#FF6B6B")
//    }
//
//    private fun selectColor(selectedButton: ImageButton, colorHex: String) {
//        // Update drawing view
//        currentColor = colorHex
//        mDrawingView.setColor(colorHex)
//        mDrawingView.setErase(false)
//        isEraseMode = false
//        //updateToolButtons()
//
//        // Update UI selection
//        updateColorSelection(selectedButton)
//
//        // อัพเดทปุ่ม Color Picker
//        updateColorPickerButton(Color.parseColor(colorHex))
//
//        // Show feedback
//        showColorSelection(colorHex)
//    }
//
//    private fun updateColorSelection(selectedButton: ImageButton) {
//        // Reset all buttons to normal state
//        colorButtons.forEach { button ->
//            // แก้ไข: ต้องแน่ใจว่า tag ไม่ใช่ null
//            button.tag?.toString()?.let { colorHex ->
//                try {
//                    val color = Color.parseColor(colorHex)
//                    button.setBackgroundColor(color)
//                } catch (e: Exception) {
//                    // จัดการกรณีที่ tag ไม่ใช่สี
//                }
//            }
//        }
//
//        // Add selection indicator to selected button
//        selectedButton.setBackgroundResource(R.drawable.color_selected_border)
//        currentColorButton = selectedButton
//    }
//
//    private fun setupToolButtons() {
//        // Back button
//        buttonBack.setOnClickListener {
//            showExitConfirmation()
//        }
//
//        // Help button
//        buttonHelp.setOnClickListener {
//            showInteractiveHelp()
//        }
//
//        // Save button
//        buttonSave.setOnClickListener {
//            showSaveOptions()
//        }
//
//        // New drawing button
//        buttonNew.setOnClickListener {
//            startNewDrawing()
//        }
//
//        // Brush button
//        buttonBrush.setOnClickListener {
//            showSizeChooserDialog("เลือกขนาดแปรง", false)
//
//            switchToBrushMode()
//        }
//
//        // Erase button
//        buttonErase.setOnClickListener {
//            switchToEraserMode()
//        }
//
//        // Update initial button states
//        //updateToolButtons()
//    }
//
//    private fun switchToBrushMode() {
//        isEraseMode = false
//        //mDrawingView.setErase(false)
//        //updateToolButtons()
//
//        showQuickMessage("เปลี่ยนเป็นโหมดแปรง 🖌️")
//    }
//
//    private fun switchToEraserMode() {
//        isEraseMode = true
//        mDrawingView.setErase(true)
//       // updateToolButtons()
//        showQuickMessage("เปลี่ยนเป็นโหมดยางลบ 🧽")
//    }
//
////    private fun updateToolButtons() {
////        if (isEraseMode) {
////            // Erase mode active
////            buttonErase.setBackgroundResource(R.drawable.tool_button_selected)
////            buttonBrush.setBackgroundResource(R.drawable.tool_button)
////        } else {
////            // Brush mode active
////            buttonBrush.setBackgroundResource(R.drawable.tool_button_selected)
////            buttonErase.setBackgroundResource(R.drawable.tool_button)
////        }
////    }
//
//    private fun showSizeChooserDialog(title: String?, isEraser: Boolean) {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(title)
//
//        val dialogView = getLayoutInflater().inflate(R.layout.dialog_brush_size, null)
//        builder.setView(dialogView)
//
//        val dialog = builder.create()
//        setupBrushSizeButtons(dialogView, dialog, isEraser)
//        dialog.show()
//    }
//
//
//    private fun setupBrushSizeButtons(dialogView: View, dialog: AlertDialog, isEraser: Boolean) {
//        val buttonIds = intArrayOf(R.id.small_brush, R.id.medium_brush, R.id.large_brush)
//        val sizes = floatArrayOf(smallBrush, mediumBrush, largeBrush)
//
//        for (i in buttonIds.indices) {
//            val button = dialogView.findViewById<ImageButton?>(buttonIds[i])
//            if (button != null) {
//                val size = sizes[i]
//                button.setOnClickListener(View.OnClickListener { v: View? ->
//                    if (isEraser) {
//                        mDrawingView.setErase(true)
//                        mDrawingView.setBrushSize(size)
//                    } else {
//                        mDrawingView.setBrushSize(size)
//                        mDrawingView.setLastBrushSize(size)
//                        mDrawingView.setErase(false)
//                    }
//                    dialog.dismiss()
//                })
//            }
//        }
//    }
//
//    private fun setupBrushSizes() {
//        smallBrush = getResources().getInteger(R.integer.small_size).toFloat()
//        mediumBrush = getResources().getInteger(R.integer.medium_size).toFloat()
//        largeBrush = getResources().getInteger(R.integer.large_size).toFloat()
//
//        mDrawingView.setBrushSize(mediumBrush)
//    }
//
//    private fun setupSeekBar() {
//        seekBarBrush.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                currentBrushSize = calculateBrushSize(progress)
//                mDrawingView.setBrushSize(currentBrushSize)
//                updateBrushSizeDisplay()
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                // Do nothing
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                // Do nothing
//            }
//        })
//    }
//
//    private fun calculateBrushSize(progress: Int): Float {
//        return when {
//            progress < 25 -> 10f + (progress / 25f) * 5f
//            progress < 50 -> 15f + ((progress - 25) / 25f) * 10f
//            progress < 75 -> 25f + ((progress - 50) / 25f) * 10f
//            else -> 35f + ((progress - 75) / 25f) * 15f
//        }
//    }
//
//    private fun updateBrushSizeDisplay() {
//        val sizeText = when {
//            currentBrushSize < 15 -> "เล็กมาก"
//            currentBrushSize < 25 -> "เล็ก"
//            currentBrushSize < 35 -> "กลาง"
//            else -> "ใหญ่"
//        }
//        textBrushSize.text = "✏️ ขนาด: $sizeText"
//    }
//
//    private fun startTimer() {
//        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                timeElapsed++
//                updateTimeDisplay()
//            }
//
//            override fun onFinish() {
//                // Should not happen with Long.MAX_VALUE
//            }
//        }
//        timer.start()
//    }
//
//    private fun handleDrawingResult(isCorrect: Boolean, accuracy: Float) {
//        runOnUiThread {
//            if (isCorrect) {
//                val pointsEarned = calculatePoints(accuracy)
//                score += pointsEarned
//                updateScoreDisplay()
//
//                showDrawingFeedback(isCorrect, accuracy, pointsEarned)
//
//                // Show special reward occasionally
//                if (accuracy > 0.8f && (0..3).random() == 0) {
//                    showSpecialReward()
//                }
//            } else {
//                showDrawingFeedback(isCorrect, accuracy, 0)
//            }
//        }
//    }
//
//    private fun calculatePoints(accuracy: Float): Int {
//        return when {
//            accuracy > 0.9f -> 30
//            accuracy > 0.7f -> 20
//            accuracy > 0.5f -> 10
//            else -> 5
//        }
//    }
//
//    private fun showDrawingFeedback(isCorrect: Boolean, accuracy: Float, pointsEarned: Int) {
//        val message = if (isCorrect) {
//            when {
//                accuracy > 0.9f -> "สุดยอด! 🏆\nได้ $pointsEarned คะแนน!"
//                accuracy > 0.7f -> "เก่งมาก! 🌟\nได้ $pointsEarned คะแนน"
//                accuracy > 0.5f -> "ดีแล้ว! 👍\nได้ $pointsEarned คะแนน"
//                else -> "พยายามอีกนิด! 💪\nได้ $pointsEarned คะแนน"
//            }
//        } else {
//            "ไม่เป็นไร! 😊\nลองใหม่อีกครั้ง"
//        }
//
//        showSuccessMessage(message)
//    }
//
//    private fun showColorSelection(colorHex: String) {
//        val colorName = colorMap[colorHex] ?: "🎨 สีสวย"
//        showQuickMessage(colorName)
//    }
//
//    private fun showWelcomeMessage() {
//        Handler(Looper.getMainLooper()).postDelayed({
//            showQuickMessage("ยินดีต้อนรับ! 🎨 มาลองระบายสีกันเถอะ!")
//        }, 500)
//    }
//
//    private fun showTemporaryTips(message: String) {
//        textTips.text = message
//        textTips.visibility = View.VISIBLE
//        Handler(Looper.getMainLooper()).postDelayed({
//            textTips.visibility = View.GONE
//        }, 3000)
//    }
//
//    private fun showQuickMessage(message: String) {
//
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    private fun showSuccessMessage(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
//    }
//
//    private fun showSpecialReward() {
//        val rewards = listOf(
//            "🎊 คุณคือศิลปินตัวจริง!",
//            "🌈 สีสันสวยงามมาก!",
//            "👑 ราชาแห่งการระบายสี!",
//            "🚀 สุดยอดฝีมือ!",
//            "⭐ ดาวเด่นแห่งศิลปะ!"
//        )
//        showSuccessMessage(rewards.random())
//    }
//
//    private fun updateScoreDisplay() {
//        textScore.text = "⭐ $score"
//    }
//
//    private fun updateTimeDisplay() {
//        val minutes = timeElapsed / 60
//        val seconds = timeElapsed % 60
//        textTime.text = "⏱️ ${minutes}:${"%02d".format(seconds)}"
//    }
//
//    // Dialog Methods
//    private fun showExitConfirmation() {
//        // (ยังคงใช้ AlertDialog เดิม, แต่สามารถแปลงเป็น SimpleDialog ได้)
//        androidx.appcompat.app.AlertDialog.Builder(this)
//            .setTitle("ออกจากเกม")
//            .setMessage("ต้องการออกจากเกมระบายสีไหม?\n\nคะแนนปัจจุบัน: $score\nเวลาเล่น: ${timeElapsed} วินาที")
//            .setPositiveButton("ออก") { dialog, which ->
//                finishWithAnimation()
//            }
//            .setNegativeButton("เล่นต่อ") { dialog, which ->
//                dialog.dismiss()
//            }
//            .setNeutralButton("เริ่มใหม่") { dialog, which ->
//                resetGame()
//                dialog.dismiss()
//            }
//            .show()
//    }
//
//    private fun showInteractiveHelp() {
//        val helpMessage = """
//            🎨 วิธีเล่นเกมระบายสี 🎨
//
//            🖌️ แปรงวาดภาพ:
//            - เลือกสีจากพาเลทด้านล่าง
//            - หรือกดปุ่ม 🎨 เพื่อเลือกสีพิเศษ
//            - วาดภาพตามจินตนาการ
//            - ใช้ขนาดแปรงที่เหมาะสม
//
//            🧽 ยางลบ:
//            - กดปุ่มยางลบเพื่อลบ
//            - ลบส่วนที่ผิดหรือต้องการเปลี่ยน
//
//            🎨 Color Picker:
//            - กดปุ่มพาเลทสีเพื่อเลือกสีใดก็ได้
//            - มีสีให้เลือกมากมาย
//            - เหมาะสำหรับศิลปินตัวจริง!
//
//            ⭐ ได้คะแนนจาก:
//            - การวาดภาพที่สวยงาม
//            - การใช้สีที่หลากหลาย
//            - ความคิดสร้างสรรค์
//
//            💾 บันทึกภาพเมื่อวาดเสร็จ!
//
//            สนุกกับการระบายสีนะ! 😊
//        """.trimIndent()
//
//        // (ยังคงใช้ AlertDialog เดิม)
//        androidx.appcompat.app.AlertDialog.Builder(this)
//            .setTitle("คำแนะนำการเล่น")
//            .setMessage(helpMessage)
//            .setPositiveButton("เข้าใจแล้ว") { dialog, which ->
//                showQuickMessage("สุขกับการระบายสี! 🎨")
//            }
//            .show()
//    }
//
//    private fun showSaveOptions() {
//        val options = arrayOf("บันทึกลง Gallery", "แชร์ภาพ", "บันทึกในแอป")
//
//        // (ยังคงใช้ AlertDialog เดิม, แต่สามารถแปลงเป็น SimpleListDialog ได้)
//        androidx.appcompat.app.AlertDialog.Builder(this)
//            .setTitle("บันทึกภาพ")
//            .setItems(options) { dialog, which ->
//                when (which) {
//                    0 -> saveToGallery()
//                    1 -> shareDrawing()
//                    2 -> saveInApp()
//                }
//            }
//            .setNegativeButton("ยกเลิก", null)
//            .show()
//    }
//
//    // <--- [เปลี่ยนแปลง] 4. แก้ไขฟังก์ชันให้ใช้ SimpleDialog
//    private fun showNewDrawingConfirmation() {
//        SimpleColorWheelDialog.build()
//            .color(color)
//            .alpha(true)
//            .show(this);
//    }
//
//    private fun showNewPaintingAlertDialog() {
//        AlertDialog.Builder(this)
//            .setTitle("ภาพใหม่")
//            .setMessage("เริ่มภาพใหม่? (ภาพปัจจุบันจะหายไป)")
//            .setPositiveButton(
//                "ตกลง",
//                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
//                    mDrawingView.startNew()
//                    // รีเซ็ตสถิติสำหรับภาพใหม่
//
//                    // รีเซ็ตพื้นที่วาด
//                })
//            .setNegativeButton("ยกเลิก", null)
//            .setCancelable(true)
//            .show()
//    }
//
//    fun showColorPicker(view: View?) {
//        @ArrayRes val pallet = intArrayOf(
//            SimpleColorDialog.MATERIAL_COLOR_PALLET,  // default if no pallet explicitly set
//            SimpleColorDialog.MATERIAL_COLOR_PALLET_DARK,
//            SimpleColorDialog.MATERIAL_COLOR_PALLET_LIGHT,
//            SimpleColorDialog.BEIGE_COLOR_PALLET,
//            SimpleColorDialog.COLORFUL_COLOR_PALLET
//        )[counter++ % 5]
//
//        val outline =
//            if (pallet == SimpleColorDialog.MATERIAL_COLOR_PALLET_LIGHT) Color.BLACK else if (pallet == SimpleColorDialog.BEIGE_COLOR_PALLET) SimpleColorDialog.AUTO else SimpleColorDialog.NONE
//
//        val dialog: SimpleColorDialog = SimpleColorDialog.build()
//            .title(R.string.pick_a_color)
//            .colors(this, pallet)
//            .colorPreset(color)
//            .allowCustom(true)
//            .showOutline(outline)
//        if (pallet == SimpleColorDialog.MATERIAL_COLOR_PALLET) {
//            dialog.colorNames(this, R.array.material_color_names)
//        }
//        dialog.show(this, "COLOR_DIALOG")
//
//        /** Results: [MainActivity.onResult]  */
//    }
//
//
//    fun showHsvWheel(view: View?) {
//        SimpleColorWheelDialog.build()
//            .color(color)
//            .alpha(true)
//            .show(this)
//
//        /** Results: [MainActivity.onResult]  */
//    }
//
//
//
//
//    // Game Actions
//    private fun startNewDrawing() {
//        mDrawingView.startNew()
//        // Add small penalty for starting new drawing
//        score = maxOf(0, score - 5)
//        showQuickMessage("เริ่มภาพใหม่! 🎨")
//        showNewPaintingAlertDialog()
//
//    }
//
//    private fun resetGame() {
//        score = 0
//        timeElapsed = 0L
//        mDrawingView.startNew()
//        updateScoreDisplay()
//        updateTimeDisplay()
//        showQuickMessage("เริ่มเกมใหม่! 🚀")
//    }
//
//    private fun saveToGallery() {
//        // TODO: Implement actual save to gallery functionality
//        saveDrawingToGallery()
//        addScore(10)
//    }
//
//    private fun shareDrawing() {
//        // TODO: Implement share functionality
//        showQuickMessage("เตรียมแชร์ภาพ... 📤")
//        addScore(5)
//    }
//
//    private fun saveInApp() {
//        // TODO: Implement in-app save
//        showSuccessMessage("บันทึกในแอปเรียบร้อย! 💾")
//        addScore(8)
//    }
//
//    private fun addScore(points: Int) {
//        score += points
//        updateScoreDisplay()
//    }
//
//    private fun finishWithAnimation() {
//        // Add any finishing animation here
//        finish()
//    }
//
//    // Configuration Changes
//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//
//        // Recreate activity when orientation changes
//        Handler(Looper.getMainLooper()).postDelayed({
//            recreate()
//        }, 100)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        // Pause timer when app goes to background
//        timer.cancel()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        // Restart timer when app comes to foreground
//        startTimer()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        timer.cancel()
//        // Clean up resources
//        mDrawingView.cleanup()
//    }
//
//    // <--- [เปลี่ยนแปลง] 5. เพิ่มฟังก์ชัน onResult เพื่อรับผลลัพธ์จาก Dialog
//    // <--- [เปลี่ยนแปลง] 1. ต้อง import BUTTON_POSITIVE ด้วย
//    // ... ภายใน Class ColoringGameActivity ...
//
//    override fun onResult(dialogTag: String, which: Int, extras: Bundle): Boolean {
//
//        // --- เคสที่ 1: Dialog ยืนยัน "เริ่มใหม่" (ที่ถูกต้อง) ---
//        if (dialogTag == DIALOG_NEW_DRAWING) {
//
//            // ตรวจสอบว่ากดปุ่ม "เริ่มใหม่" (Positive) หรือไม่
//            if (which == BUTTON_POSITIVE) { // <-- ใช้ BUTTON_POSITIVE ที่ import มา
//                startNewDrawing() // <--- นี่คือคำสั่งที่ถูกต้องสำหรับ Tag นี้
//            }
//            // ถ้ากด "ยกเลิก" (Negative) หรือ (Cancel) ก็ไม่ต้องทำอะไร
//            return true // บอกว่าเรารับผลลัพธ์นี้แล้ว
//        }
//
//         // --- เคสที่ 2: ถ้าคุณมี Dialog เลือกสี (ตัวอย่าง) ---
//        // สมมติว่าคุณสร้าง Tag ใหม่ชื่อ DIALOG_PICK_COLOR
//        // และเรียกใช้ SimpleColorDialog.build()...show(this, DIALOG_PICK_COLOR)
//
//        else if (dialogTag == "DIALOG_PICK_COLOR") {
//            if (which == BUTTON_POSITIVE) {
//
//                // นี่คือโค้ด Java ที่คุณส่งมา แปลงเป็น Kotlin:
//                val colors: IntArray? = extras.getIntArray(SimpleColorDialog.COLORS)
//
//                if (colors != null) {
//                    // กรณีเลือกหลายสี
//                    for (i in colors.indices) { // <-- วิธี loop ใน Kotlin
//                        @ColorInt val color: Int = colors[i]
//
//                        // handler.postDelayed({ ... }, 1000L * i) // <-- วิธีใช้ Lambda ใน Kotlin
//                        // คุณไม่มีฟังก์ชัน newColor(color)
//                        // แต่คุณสามารถแปลงค่าสีและสั่ง mDrawingView ให้เปลี่ยนสีได้
//
//                        val colorHex = String.format("#%06X", 0xFFFFFF and color)
//                        // อาจจะต้องเลื่อน delay หรืออัปเดตสีทันที
//                        mDrawingView.setColor(colorHex)
//
//                    }
//                } else {
//                     // กรณีเลือกสีเดียว (ไม่ใช่หลายสี)
//                     val singleColor = extras.getInt(SimpleColorDialog.COLOR)
//                     val colorHex = String.format("#%06X", 0xFFFFFF and singleColor)
//                     mDrawingView.setColor(colorHex)
//                     updateColorPickerButton(singleColor)
//                     resetBasicColorSelection()
//                }
//            }
//            return true
//        }
//
//        // (สามารถเพิ่มเงื่อนไข 'else if' สำหรับ Dialog อื่นๆ ที่นี่)
//        // เช่น else if (dialogTag == DIALOG_EXIT) { ... }
//
//        // คืนค่า false ถ้าไม่ใช่ Dialog ที่เรารู้จัก
//        return false
//    }
//
//    // นี่คือฟังก์ชันใหม่ทั้งหมด ให้เอาไปวางทับของเดิม
//    private fun saveDrawingToGallery() {
//        if (mDrawingView == null) {
//            showToast("Drawing view not available")
//            return
//        }
//
//        // 1. สร้าง Bitmap จาก DrawingView (วิธีใหม่ที่ไม่ใช้ getDrawingCache)
//        val bitmap = Bitmap.createBitmap(
//            mDrawingView.width,
//            mDrawingView.height,
//            Bitmap.Config.ARGB_8888
//        )
//        val canvas = Canvas(bitmap)
//        mDrawingView.draw(canvas)
//
//        // 2. เตรียมข้อมูลไฟล์ที่จะบันทึก
//        val filename = "drawing_" + System.currentTimeMillis() + ".png"
//        val contentValues = ContentValues().apply {
//            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
//            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
//
//            // บอกว่าจะเก็บไว้ในโฟลเดอร์ Pictures (สำหรับ Android 10+)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyPaintApp")
//                put(MediaStore.Images.Media.IS_PENDING, 1) // กำลังเขียน
//            }
//        }
//
//        // 3. ใช้ ContentResolver ในการสร้างไฟล์
//        val resolver = contentResolver
//        var uri: Uri? = null
//        var outputStream: OutputStream? = null
//
//        try {
//            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//            if (uri == null) {
//                throw java.io.IOException("Failed to create new MediaStore record.")
//            }
//
//            outputStream = resolver.openOutputStream(uri)
//            if (outputStream == null) {
//                throw java.io.IOException("Failed to get output stream.")
//            }
//
//            // 4. เขียน Bitmap ลงในไฟล์
//            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
//                throw java.io.IOException("Failed to save bitmap.")
//            }
//
//            // 5. (สำหรับ Android 10+) บอกว่าเขียนเสร็จแล้ว
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                contentValues.clear()
//                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
//                resolver.update(uri, contentValues, null, null)
//            }
//
//            showToast("บันทึกภาพสำเร็จ!")
//
//            // ไม่ต้องใช้ sendBroadcast(ACTION_MEDIA_SCANNER_SCAN_FILE) อีกต่อไป
//            // ระบบ MediaStore จะจัดการให้เอง
//
//        } catch (e: Exception) {
//            Log.e("Error saving image", e.message.toString())
//            showToast("เกิดข้อผิดพลาดในการบันทึก")
//
//            // ถ้าเกิด Error ให้ลบไฟล์ที่สร้างค้างไว้ออก
//            uri?.let { resolver.delete(it, null, null) }
//
//        } finally {
//            outputStream?.close()
//            // ไม่ต้อง destroyDrawingCache()
//        }
//    }
//
//    private fun showToast(message: String?) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//}