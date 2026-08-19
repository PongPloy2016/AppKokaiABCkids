package com.pongploydev.priyankvex.paintapp.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.model.LearningDataModel

/**
 * Adapter สำหรับแสดงข้อมูลการเรียนรู้ในรูปแบบ Grid/List
 * รองรับ Auto-Read Highlight และการจัดการต่างๆ
 *
 * @property context Context ของแอปพลิเคชัน
 * @property learningDataList รายการข้อมูลการเรียนรู้
 * @property categoryPosition ตำแหน่งของหมวดหมู่
 * @property displayType ประเภทการแสดงผล
 * @property onItemClick Callback สำหรับการคลิกที่รายการ
 * @property onItemLongClick Callback สำหรับการคลิกค้างที่รายการ (optional)
 * @property onItemFavoriteClick Callback สำหรับการคลิกปุ่ม favorite (optional)
 */
class SubHomeAdapter(
    private val context: Context,
    private val learningDataList: List<LearningDataModel>,
    private val categoryPosition: Int,
    private val displayType: Int,
    private val onItemClick: (Int, Int, LearningDataModel) -> Unit,
    private val onItemLongClick: ((Int, Int, LearningDataModel) -> Boolean)? = null,
    private val onItemFavoriteClick: ((Int, Int, LearningDataModel) -> Unit)? = null
) : RecyclerView.Adapter<SubHomeAdapter.ViewHolder>() {

    // ============================================
    // VARIABLES
    // ============================================

    // ตัวแปรเพื่อป้องกันการเรียก Animation ซ้ำ
    private val animatedItems = mutableSetOf<Int>()

    // ตัวแปรสำหรับเก็บสถานะ favorite
    private val favoriteItems = mutableSetOf<Int>()

    // ตัวแปรสำหรับเก็บตำแหน่งที่กำลังอ่านอยู่ (สำหรับ Auto-Read)
    private var currentReadingIndex = -1

    // ตัวแปรสำหรับเก็บตำแหน่งที่ถูกเลือกล่าสุด
    private var lastSelectedPosition = -1

    // สำหรับ Double Click Detection
    private var lastClickTime: Long = 0
    private var lastClickPosition: Int = -1

    // ============================================
    // VIEW HOLDER
    // ============================================

    /**
     * ViewHolder สำหรับเก็บ reference ไปยัง View
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cvCardSubHome)
        val imageView: ImageView = view.findViewById(R.id.imgSubHome)
        val linearLayout: LinearLayout? = view.findViewById(R.id.lloutCardSubHome)
        val frameLayout: FrameLayout? = view.findViewById(R.id.flCardContainer)
        val tvThaiTranslation: android.widget.TextView? = view.findViewById(R.id.tvThaiTranslation)
        val readingGlowOverlay: View? = view.findViewById(R.id.readingGlowOverlay)

        // สำหรับ Debug
        init {
            Log.d("ViewHolder", "ViewHolder created for item")

            // ตั้งค่าให้ CardView และ FrameLayout รับ click ได้
            cardView.isClickable = true
            cardView.isFocusable = true
            cardView.isFocusableInTouchMode = true

            // ตั้งค่าให้ FrameLayout (ถ้ามี)
            frameLayout?.let {
                it.isClickable = true
                it.isFocusable = true
                it.isFocusableInTouchMode = true
                Log.d("ViewHolder", "FrameLayout clickable set")
            }

            // ป้องกันไม่ให้ LinearLayout แย่ง event
            linearLayout?.let {
                it.isClickable = false
                it.isFocusable = false
                it.isFocusableInTouchMode = false
            }

            // ป้องกันไม่ให้ ImageView แย่ง event
            imageView.isClickable = false
            imageView.isFocusable = false
            imageView.isFocusableInTouchMode = false
        }
    }

    // ============================================
    // ADAPTER METHODS
    // ============================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("SubHomeAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_sub_list_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("SubHomeAdapter", "onBindViewHolder called for position: $position")

        val learningData = learningDataList[position]

        // 1. โหลดรูปภาพด้วย Glide
        loadImageWithGlide(holder, learningData)

        // 2. แสดงคำแปลภาษาไทยใต้คำศัพท์
        val thaiText = getThaiTranslation(learningData)
        if (holder.tvThaiTranslation != null) {
            if (thaiText.isNotEmpty()) {
                holder.tvThaiTranslation.visibility = View.VISIBLE
                holder.tvThaiTranslation.text = thaiText
            } else {
                holder.tvThaiTranslation.visibility = View.GONE
            }
        }

        // 3. เพิ่ม Animation เฉพาะเมื่อแสดงครั้งแรก
        applyEntranceAnimation(holder, position)

        // 4. ตั้งค่า Highlight สำหรับรายการที่กำลังอ่าน
        applyReadingHighlight(holder, position)

        // 5. ตั้งค่า Click Listeners (สำคัญ!)
        setupClickListeners(holder, position, learningData)

        Log.d("SubHomeAdapter", "Bind completed for position: $position")
    }

    override fun getItemCount(): Int = learningDataList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    // ============================================
    // PUBLIC METHODS
    // ============================================

    /**
     * ตั้งค่าตำแหน่งที่กำลังอ่านอยู่ (สำหรับ Auto-Read Highlight)
     */
    fun setCurrentReadingIndex(index: Int) {
        val previousIndex = currentReadingIndex
        currentReadingIndex = index

        // อัปเดตเฉพาะตำแหน่งที่เปลี่ยนแปลง
        if (previousIndex != -1 && previousIndex < itemCount) {
            notifyItemChanged(previousIndex)
        }
        if (currentReadingIndex != -1 && currentReadingIndex < itemCount) {
            notifyItemChanged(currentReadingIndex)
        }

        Log.d("SubHomeAdapter", "Current reading index changed from $previousIndex to $index")
    }

    /**
     * ตั้งค่าตำแหน่งที่ถูกเลือกล่าสุด
     */
    fun setLastSelectedPosition(position: Int) {
        val previousPosition = lastSelectedPosition
        lastSelectedPosition = position

        if (previousPosition != -1 && previousPosition < itemCount) {
            notifyItemChanged(previousPosition)
        }
        if (lastSelectedPosition != -1 && lastSelectedPosition < itemCount) {
            notifyItemChanged(lastSelectedPosition)
        }

        Log.d("SubHomeAdapter", "Last selected position changed to: $position")
    }

    /**
     * อัปเดตรายการข้อมูลใหม่
     */
    fun updateData(newData: List<LearningDataModel>) {
        // รีเซ็ต animated items สำหรับข้อมูลใหม่
        animatedItems.clear()

        // ในอนาคตควรใช้ DiffUtil สำหรับประสิทธิภาพที่ดีขึ้น
        // notifyDataSetChanged()

        Log.d("SubHomeAdapter", "Data updated, items count: ${newData.size}")
    }

    /**
     * เพิ่ม/ลบ รายการโปรด
     */
    fun toggleFavorite(position: Int) {
        if (favoriteItems.contains(position)) {
            favoriteItems.remove(position)
            Log.d("SubHomeAdapter", "Removed favorite at position: $position")
        } else {
            favoriteItems.add(position)
            Log.d("SubHomeAdapter", "Added favorite at position: $position")
        }

        if (position in 0 until itemCount) {
            notifyItemChanged(position)
        }
    }

    /**
     * ตรวจสอบว่ารายการนี้เป็นรายการโปรดหรือไม่
     */
    fun isFavorite(position: Int): Boolean {
        return favoriteItems.contains(position)
    }

    /**
     * เคลียร์ animation items เมื่อ adapter ถูกทำลาย
     */
    fun clearAnimationCache() {
        animatedItems.clear()
        Log.d("SubHomeAdapter", "Animation cache cleared")
    }

    /**
     * รีเซ็ตสถานะทั้งหมด
     */
    fun resetAllStates() {
        currentReadingIndex = -1
        lastSelectedPosition = -1
        favoriteItems.clear()
        animatedItems.clear()
        notifyDataSetChanged()
        Log.d("SubHomeAdapter", "All states reset")
    }

    /**
     * เปิดใช้งาน/ปิดใช้งานการคลิก
     */
    fun setClickable(enabled: Boolean) {
        // สามารถ implement ตามต้องการ
        Log.d("SubHomeAdapter", "Clickable set to: $enabled")
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    /**
     * โหลดรูปภาพด้วย Glide
     */
    private fun loadImageWithGlide(holder: ViewHolder, learningData: LearningDataModel) {
        try {
            Glide.with(context)
                .load(learningData.image)
                .apply(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.home_jobs)
                        .error(R.drawable.error_message_icon)
                        .centerCrop()
                        .override(400, 400) // ปรับขนาดเพื่อประสิทธิภาพ
                )
                .into(holder.imageView)
        } catch (e: Exception) {
            Log.e("SubHomeAdapter", "Error loading image: ${e.message}")
            holder.imageView.setImageResource(R.drawable.error_message_icon)
        }
    }

    /**
     * เพิ่ม entrance animation
     */
    private fun applyEntranceAnimation(holder: ViewHolder, position: Int) {
        if (!animatedItems.contains(position)) {
            try {
                val animation = AnimationUtils.loadAnimation(context, R.anim.bubble_anim)
                holder.cardView.startAnimation(animation)
                animatedItems.add(position)
                Log.d("SubHomeAdapter", "Animation applied for position: $position")
            } catch (e: Exception) {
                Log.e("SubHomeAdapter", "Error loading animation: ${e.message}")
            }
        }
    }

    /**
     * ดึงคำแปลภาษาไทยสำหรับรายการ
     */
    private fun getThaiTranslation(learningData: LearningDataModel): String {
        val showTitle = learningData.showTitle ?: ""
        val speakTitle = learningData.speakTitle ?: ""

        // หาก showTitle เป็นภาษาไทยอยู่แล้ว
        if (showTitle.any { it in '\u0E01'..'\u0E5B' }) {
            return showTitle
        }

        // ดึงภาษาไทยจาก speakTitle (เช่น "A for Apple ลูกแอปเปิล")
        val thaiPart = speakTitle.filter { it in '\u0E01'..'\u0E5B' || it == ' ' }.trim()
        if (thaiPart.isNotEmpty()) {
            return thaiPart
        }

        return when (showTitle.trim().lowercase()) {
            "apple" -> "แอปเปิ้ล"
            "bird" -> "นก"
            "ball" -> "ลูกบอล"
            "cat" -> "แมว"
            "dog" -> "สุนัข"
            "egg" -> "ไข่"
            "elephant" -> "ช้าง"
            "fish" -> "ปลา"
            "goat" -> "แพะ"
            "giraffe" -> "ยีราฟ"
            "hat" -> "หมวก"
            "horse" -> "ม้า"
            "ice cream" -> "ไอศกรีม"
            "joker" -> "ตัวตลก"
            "jam" -> "แยม"
            "kite" -> "ว่าว"
            "key" -> "กุญแจ"
            "lion" -> "สิงโต"
            "leaf" -> "ใบไม้"
            "monkey" -> "ลิง"
            "mushroom" -> "เห็ด"
            "nest", "nestling" -> "รังนก"
            "orange" -> "ส้ม"
            "owl" -> "นกฮูก"
            "parrot" -> "นกแก้ว"
            "pencil" -> "ดินสอ"
            "queen" -> "พระราชินี"
            "rabbit" -> "กระต่าย"
            "ring" -> "แหวน"
            "sun" -> "ดวงอาทิตย์"
            "train" -> "รถไฟ"
            "turtle" -> "เต่า"
            "umbrella" -> "ร่ม"
            "violin" -> "ไวโอลิน"
            "vase" -> "แจกัน"
            "watch" -> "นาฬิกา"
            "whale" -> "วาฬ"
            "xylophone" -> "ระนาดฝรั่ง"
            "yak" -> "จามรี"
            "yo yo", "yo-yo" -> "โยโย่"
            "zebra" -> "ม้าลาย"
            // ตัวเลข
            "zero" -> "ศูนย์"
            "one" -> "หนึ่ง"
            "two" -> "สอง"
            "three" -> "สาม"
            "four" -> "สี่"
            "five" -> "ห้า"
            "six" -> "หก"
            "seven" -> "เจ็ด"
            "eight" -> "แปด"
            "nine" -> "เก้า"
            "ten" -> "สิบ"
            "eleven" -> "สิบเอ็ด"
            "twelve" -> "สิบสอง"
            "thirteen" -> "สิบสาม"
            "fourteen" -> "สิบสี่"
            "fifteen" -> "สิบห้า"
            "sixteen" -> "สิบหก"
            "seventeen" -> "สิบเจ็ด"
            "eighteen" -> "สิบแปด"
            "nineteen" -> "สิบเก้า"
            "twenty" -> "ยี่สิบ"
            else -> showTitle
        }
    }

    /**
     * ตั้งค่า Highlight สำหรับรายการที่กำลังอ่าน
     */
    private fun applyReadingHighlight(holder: ViewHolder, position: Int) {
        runCatching {
            val isCurrentReading = (position == currentReadingIndex)

            if (isCurrentReading) {
                // ไฮไลต์การ์ดที่กำลังอ่าน พร้อมเส้นขอบเรืองแสงสีสว่าง
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFDE7"))
                holder.cardView.cardElevation = 16f
                holder.cardView.radius = 22f
                holder.readingGlowOverlay?.visibility = View.VISIBLE

                // ขยายขนาดขึ้นเล็กน้อย (Scale Animation) ให้เด็กดูตามทัน
                holder.cardView.animate()
                    .scaleX(1.06f)
                    .scaleY(1.06f)
                    .setDuration(250)
                    .start()

                Log.d("SubHomeAdapter", "Reading highlight for position: $position")
            } else if (position == lastSelectedPosition) {
                holder.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.selected_highlight)
                )
                holder.cardView.cardElevation = 10f
                holder.cardView.radius = 20f
                holder.readingGlowOverlay?.visibility = View.GONE

                holder.cardView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            } else {
                holder.cardView.setCardBackgroundColor(Color.WHITE)
                holder.cardView.cardElevation = 6f
                holder.cardView.radius = 20f
                holder.readingGlowOverlay?.visibility = View.GONE

                holder.cardView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start()
            }
        }.onFailure { e ->
            Log.e("SubHomeAdapter", "Error applying highlight: ${e.message}")
        }
    }

    /**
     * ตั้งค่า Click Listeners (แก้ไขแล้ว)
     */
    private fun setupClickListeners(holder: ViewHolder, position: Int, learningData: LearningDataModel) {
        Log.d("SubHomeAdapter", "Setting up click listeners for position: $position")

        // 1. ตั้งค่าให้กับ itemView (วิธีที่ปลอดภัยที่สุด)
        holder.itemView.setOnClickListener {
            Log.d("SubHomeAdapter", "itemView clicked at position: $position")
            handleItemClick(holder, position, learningData)
        }

        // 2. ตั้งค่าให้กับ CardView
        holder.cardView.setOnClickListener {
            Log.d("SubHomeAdapter", "CardView clicked at position: $position")
            handleItemClick(holder, position, learningData)
        }

        // 3. ตั้งค่าให้กับ FrameLayout (ถ้ามี)
        holder.frameLayout?.setOnClickListener {
            Log.d("SubHomeAdapter", "FrameLayout clicked at position: $position")
            handleItemClick(holder, position, learningData)
        }

        // 4. ตั้งค่าให้กับ ImageView (ป้องกันการแย่ง event)
        holder.imageView.setOnClickListener {
            Log.d("SubHomeAdapter", "ImageView clicked at position: $position")
            handleItemClick(holder, position, learningData)
        }

        // Long Click Listener
        val longClickListener = View.OnLongClickListener {
            Log.d("SubHomeAdapter", "Long click at position: $position")
            onItemLongClick?.invoke(categoryPosition, position, learningData) ?: false
        }

        holder.itemView.setOnLongClickListener(longClickListener)
        holder.cardView.setOnLongClickListener(longClickListener)
        holder.frameLayout?.setOnLongClickListener(longClickListener)

        // Double Click Detection
        setupDoubleClickListener(holder, position, learningData)

        // ตรวจสอบสถานะ
        Log.d("SubHomeAdapter", "Clickable states - CardView: ${holder.cardView.isClickable}, ImageView: ${holder.imageView.isClickable}")
    }

    /**
     * ฟังก์ชันจัดการการคลิก
     */
    private fun handleItemClick(holder: ViewHolder, position: Int, learningData: LearningDataModel) {
        Log.d("SubHomeAdapter", "Handling click for position: $position")

        try {
            // 1. อัปเดตตำแหน่งที่ถูกเลือกล่าสุด
            setLastSelectedPosition(position)

            // 2. เรียก callback
            onItemClick.invoke(categoryPosition, position, learningData)

            // 3. เพิ่ม animation feedback
            runCatching {
                val animation = AnimationUtils.loadAnimation(context, R.anim.click_feedback)
                holder.cardView.startAnimation(animation)
            }.onFailure { e ->
                Log.e("SubHomeAdapter", "Error loading click animation: ${e.message}")
            }

            // 4. Ripple effect (ถ้า foreground ถูกตั้งค่า)
            holder.cardView.isPressed = true
            holder.cardView.postDelayed({
                holder.cardView.isPressed = false
            }, 200)

            Log.d("SubHomeAdapter", "Click handled successfully for position: $position")
        } catch (e: Exception) {
            Log.e("SubHomeAdapter", "Error in handleItemClick: ${e.message}")
        }
    }

    /**
     * ตั้งค่า Double Click Listener
     */
    private fun setupDoubleClickListener(holder: ViewHolder, position: Int, learningData: LearningDataModel) {
        val doubleClickListener = View.OnClickListener { view ->
            val clickTime = System.currentTimeMillis()
            val timeDiff = clickTime - lastClickTime
            val positionDiff = if (lastClickPosition == position) 0 else 1

            Log.d("SubHomeAdapter", "Double click check: timeDiff=$timeDiff, positionDiff=$positionDiff")

            if (timeDiff < 500 && positionDiff == 0) { // Double click within 500ms on same item
                Log.d("SubHomeAdapter", "Double click detected at position: $position")

                // 1. Toggle favorite
                toggleFavorite(position)

                // 2. เรียก callback ถ้ามี
                onItemFavoriteClick?.invoke(categoryPosition, position, learningData)

                // 3. แสดง animation feedback
                runCatching {
                    val animation = AnimationUtils.loadAnimation(context, R.anim.click_feedback)
                    holder.cardView.startAnimation(animation)
                }.onFailure { e ->
                    Log.e("SubHomeAdapter", "Error loading favorite animation: ${e.message}")
                }

                // 4. รีเซ็ต
                lastClickTime = 0
                lastClickPosition = -1
            } else {
                // Single click หรือคลิกที่ตำแหน่งอื่น
                lastClickTime = clickTime
                lastClickPosition = position

                // Single click จะถูกจัดการใน handleItemClick
            }
        }

        // ตั้งค่า double click listener ให้กับ view หลักๆ
        holder.itemView.setOnClickListener(doubleClickListener)
        holder.cardView.setOnClickListener(doubleClickListener)
    }

    /**
     * อัปเดตสถานะ favorite (ถ้ามีใน layout)
     */
    private fun updateFavoriteState(holder: ViewHolder, position: Int) {
        // ถ้ามี ImageView สำหรับ favorite ใน layout
        /*
        val isFavorite = favoriteItems.contains(position)
        val favoriteIcon = if (isFavorite) {
            R.drawable.ic_favorite_filled
        } else {
            R.drawable.ic_favorite_outline
        }

        holder.favoriteIcon.setImageResource(favoriteIcon)
        holder.favoriteIcon.visibility = View.VISIBLE

        holder.favoriteIcon.setOnClickListener {
            toggleFavorite(position)
            onItemFavoriteClick?.invoke(categoryPosition, position, learningDataList[position])
        }
        */
    }

    /**
     * สร้าง gradient background ตามตำแหน่ง
     */
    private fun getGradientColorForPosition(position: Int): Int {
        // สร้าง gradient สีตามตำแหน่ง
        val hue = (position * 137.508) % 360 // Golden angle approximation
        return Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.4f, 0.95f))
    }

    /**
     * ตั้งค่า border ตามสถานะ
     */
    private fun applyBorderStyle(holder: ViewHolder, position: Int) {
        try {
            // สามารถปรับ border ได้ตามต้องการ
//            when {
//                position == currentReadingIndex -> {
//                    holder.cardView.strokeColor = ContextCompat.getColor(context, R.color.border_highlight)
//                    holder.cardView.strokeWidth = 4
//                }
//                favoriteItems.contains(position) -> {
//                    holder.cardView.strokeColor = ContextCompat.getColor(context, R.color.border_favorite)
//                    holder.cardView.strokeWidth = 3
//                }
//                else -> {
//                    holder.cardView.strokeColor = ContextCompat.getColor(context, R.color.border_normal)
//                    holder.cardView.strokeWidth = 1
//                }
//            }
        } catch (e: Exception) {
            Log.e("SubHomeAdapter", "Error applying border style: ${e.message}")
        }
    }

    /**
     * ตั้งค่าให้ View ทุกตัวสามารถคลิกได้
     */
    private fun setAllViewsClickable(holder: ViewHolder, clickable: Boolean) {
        holder.cardView.isClickable = clickable
        holder.cardView.isFocusable = clickable
        holder.itemView.isClickable = clickable
        holder.itemView.isFocusable = clickable
        holder.frameLayout?.isClickable = clickable
        holder.frameLayout?.isFocusable = clickable
        holder.imageView.isClickable = clickable
        holder.imageView.isFocusable = clickable
    }

    // ============================================
    // DEBUG METHODS
    // ============================================

    /**
     * ฟังก์ชันสำหรับ debug การคลิก
     */
    fun debugClickStatus(position: Int) {
        Log.d("SubHomeAdapter", "=== DEBUG for position $position ===")
        Log.d("SubHomeAdapter", "Current reading index: $currentReadingIndex")
        Log.d("SubHomeAdapter", "Last selected position: $lastSelectedPosition")
        Log.d("SubHomeAdapter", "Is favorite: ${favoriteItems.contains(position)}")
        Log.d("SubHomeAdapter", "Has animation: ${animatedItems.contains(position)}")
        Log.d("SubHomeAdapter", "=== END DEBUG ===")
    }

    /**
     * ตรวจสอบว่ามี listener ที่ทำงานหรือไม่
     */
    fun hasActiveListeners(): Boolean {
        return true // คืนค่า true หากมี listener ทำงาน
    }
}