package com.pongploydev.priyankvex.paintapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.model.ItemObjectAndroidGridView

class CustomAdapterAndroidGridView(
    private val context: Context,
    private val items: List<ItemObjectAndroidGridView>
) : RecyclerView.Adapter<CustomAdapterAndroidGridView.ItemViewHolder>() {

    /**
     * ตัวแปรสำหรับสลับ View Type
     * Activity/Fragment จะเป็นคนสั่ง set ค่านี้
     * และสั่ง notifyDataSetChanged() เอง
     */
    var isGridView: Boolean = true

    // Kotlin-style click listener (ใช้ Lambda)
    private var onItemClickListener: ((Int, ItemObjectAndroidGridView) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int, ItemObjectAndroidGridView) -> Unit) {
        onItemClickListener = listener
    }

    /**
     * สร้างค่าคงที่สำหรับ View Type
     */
    companion object {
        private const val GRID_VIEW_TYPE = 1
        private const val LIST_VIEW_TYPE = 2
    }

    /**
     * บอก Adapter ว่าจะใช้ View Type ไหน โดยอิงจาก isGridView
     */
    override fun getItemViewType(position: Int): Int {
        return if (isGridView) GRID_VIEW_TYPE else LIST_VIEW_TYPE
    }

    /**
     * สร้าง ViewHolder โดยแยกตาม View Type ที่ได้รับมา
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View

        if (viewType == GRID_VIEW_TYPE) {
            // ถ้าเป็น Grid ให้ใช้ layout 'grid_view_item.xml'
            // ⚠️ ต้องแน่ใจว่าคุณมีไฟล์ layout นี้
            view = inflater.inflate(R.layout.card_item_grid, parent, false)
        } else {
            // ถ้าเป็น List ให้ใช้ layout 'list_view_item.xml'
            // ⚠️ ต้องแน่ใจว่าคุณมีไฟล์ layout นี้
            view = inflater.inflate(R.layout.card_list_start, parent, false)
        }

        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    /**
     * ViewHolder ที่ใช้ ViewBinding (หรือ findViewById)
     * เพื่อเชื่อมโยง View และจัดการการ Bind ข้อมูล
     */
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // ⚠️ ต้องแน่ใจว่า ID เหล่านี้ตรงกับในไฟล์ .xml ของคุณ
        private val itemImage: ImageView = itemView.findViewById(R.id.imgThumbnail)
        private val itemText: TextView = itemView.findViewById(R.id.item_title)

        init {
            // ตั้งค่า click listener ครั้งเดียวที่นี่
            itemView.setOnClickListener {
                val position = adapterPosition
                // ตรวจสอบว่า item ยังคงอยู่
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(position, items[position])
                }
            }
        }

        fun bind(item: ItemObjectAndroidGridView) {
            itemText.text = item.title
            // item.photoId คือ Int ที่เป็น Resource ID (เช่น R.drawable.card_one)
            itemImage.setImageResource(item.imageResId)

            // (ถ้า item.photoId เป็น String URL คุณจะต้องใช้ Glide หรือ Picasso แทน)
        }
    }
}