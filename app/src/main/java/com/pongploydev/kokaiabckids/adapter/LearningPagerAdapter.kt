package com.pongploydev.kokaiabckids.adapter




import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.pongploydev.kokaiabckids.R
import com.pongploydev.kokaiabckids.model.LearningDataModel

class LearningPagerAdapter(
    private val context: Context,
    private val dataList: List<LearningDataModel>
) : PagerAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = dataList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    @SuppressLint("MissingInflatedId")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = inflater.inflate(R.layout.viewpager_layout, container, false)
        val imageView = itemView.findViewById<ImageView>(R.id.cellImgViewPager)

        // Load image with Glide
        Glide.with(context)
            .load(dataList[position].image)
            .apply(RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.iconpngs)
                .error(R.drawable.eraser))
            .into(imageView)

        // Add fun animation
        YoYo.with(Techniques.Tada)
            .duration(1200)
            .repeat(1)
            .playOn(imageView)

        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }
}