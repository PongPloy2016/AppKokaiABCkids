package com.pongploydev.priyankvex.paintapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.activity.learning.FullScreenActivity
import com.pongploydev.priyankvex.paintapp.model.LearningDataModel

/**
 * Adapter สำหรับแสดงข้อมูลการเรียนรู้ในรูปแบบ Grid/List
 * @property context Context ของแอปพลิเคชัน
 * @property learningDataList รายการข้อมูลการเรียนรู้
 * @property categoryPosition ตำแหน่งของหมวดหมู่
 * @property displayType ประเภทการแสดงผล
 */
class SubHomeAdapter(
    private val context: Context,
    private val learningDataList: List<LearningDataModel>,
    private val categoryPosition: Int,
    private val displayType: Int
) : RecyclerView.Adapter<SubHomeAdapter.ViewHolder>() {

    // ตัวแปรเพื่อป้องกันการเรียก Animation ซ้ำ
    private val animatedItems = mutableSetOf<Int>()

    /**
     * ViewHolder สำหรับเก็บ reference ไปยัง View
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cvCardSubHome)
        val imageView: ImageView = view.findViewById(R.id.imgSubHome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_sub_list_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val learningData = learningDataList[position]

        // โหลดรูปภาพด้วย Glide
        Glide.with(context)
            .load(learningData.image)
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.home_jobs) // ควรเพิ่มรูป placeholder
                    .error(R.drawable.error_message_icon) // ควรเพิ่มรูปแสดง error
            )
            .into(holder.imageView)

        // เพิ่ม Animation เฉพาะเมื่อแสดงครั้งแรก
        if (!animatedItems.contains(position)) {
            holder.cardView.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.bubble_anim)
            )
            animatedItems.add(position)
        }

        // ตั้งค่า Click Listener
        holder.cardView.setOnClickListener {
            openFullScreenActivity(position)
        }

        // เพิ่มการจัดการ Long Click (ถ้าต้องการ)
        holder.cardView.setOnLongClickListener {
            // จัดการ Long Click ตรงนี้
            true
        }
    }

    override fun getItemCount(): Int = learningDataList.size

    /**
     * เปิดหน้าจอแบบเต็มหน้าจอ
     */
    private fun openFullScreenActivity(position: Int) {
        val learningData = learningDataList[position]

        val intent = Intent(context, FullScreenActivity::class.java).apply {
            putExtra("categoryPosition", categoryPosition)
            putExtra("selectedPosition", position)
            // ส่งข้อมูลที่จำเป็นเท่านั้น
            putExtra("imageUrl", learningData.image)
            // เพิ่มฟิลด์อื่นๆ ตามที่ต้องการใช้งานใน FullScreenActivity
        }
        context.startActivity(intent)
    }


}