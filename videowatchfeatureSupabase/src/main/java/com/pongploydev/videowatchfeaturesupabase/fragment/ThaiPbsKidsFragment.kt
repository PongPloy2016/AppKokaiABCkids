package com.pongploydev.videowatchfeaturesupabase.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.pongploydev.videowatchfeaturesupabase.Constant
import com.pongploydev.videowatchfeaturesupabase.R
import com.pongploydev.videowatchfeaturesupabase.activity.RssFeedDetailActivity
import com.pongploydev.videowatchfeaturesupabase.adapter.AdapterVideo
import com.pongploydev.videowatchfeaturesupabase.data.repository.VideoFeedRepository
import com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem
import com.pongploydev.videowatchfeaturesupabase.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment เฉพาะสำหรับหมวดหมู่ "🌟 Thai PBS Kids" (thaipbs_kids_programs)
 * โหลดเนื้อหาจาก RSS / XML feed จาก Supabase ตาราง kids_video_feeds
 * พร้อมหน้าจออ่านนิทานและเครื่องเล่นวิดีโอเด็กโดยเฉพาะ
 */
class ThaiPbsKidsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var lytNoItem: View
    private lateinit var lytFailed: View
    private lateinit var btnLoadMore: View
    private lateinit var txtBannerIcon: TextView
    private lateinit var txtBannerTitle: TextView
    private lateinit var txtBannerDescription: TextView

    private var adapterVideo: AdapterVideo? = null
    private val videoList = mutableListOf<Video>()
    private val allVideos = mutableListOf<Video>()
    private val PAGE_SIZE = 10

    private var currentFeed: KidsVideoFeedItem? = null

    companion object {
        private const val TAG = "ThaiPbsKidsFragment"
        private const val DEFAULT_FEED_ID = "thaipbs_kids_programs"

        fun newInstance(): ThaiPbsKidsFragment {
            return ThaiPbsKidsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_thai_pbs_kids, container, false)
        initViews(view)
        setupRecyclerView()
        setupListeners()
        loadFeedData(force = false)
        return view
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container)
        lytNoItem = view.findViewById(R.id.lyt_no_item)
        lytFailed = view.findViewById(R.id.lyt_failed)
        btnLoadMore = view.findViewById(R.id.btn_load_more)
        txtBannerIcon = view.findViewById(R.id.txt_banner_icon)
        txtBannerTitle = view.findViewById(R.id.txt_banner_title)
        txtBannerDescription = view.findViewById(R.id.txt_banner_description)
    }

    private fun setupRecyclerView() {
        val context = requireContext()
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapterVideo = AdapterVideo(context, recyclerView, videoList).apply {
            setOnItemClickListener { _, video, _ ->
                if (video != null) {
                    openDetailActivity(video)
                }
            }
        }
        recyclerView.adapter = adapterVideo
    }

    private fun setupListeners() {
        btnLoadMore.setOnClickListener {
            loadNextBatch()
        }

        lytFailed.findViewById<View>(R.id.failed_retry)?.setOnClickListener {
            loadFeedData(force = true)
        }
    }

    private fun loadNextBatch() {
        val currentSize = videoList.size
        if (currentSize >= allVideos.size) {
            btnLoadMore.visibility = View.GONE
            return
        }
        val nextSize = minOf(currentSize + PAGE_SIZE, allVideos.size)
        val nextItems = allVideos.subList(currentSize, nextSize)
        videoList.addAll(nextItems)
        adapterVideo?.notifyItemRangeInserted(currentSize, nextItems.size)
        btnLoadMore.visibility = if (videoList.size < allVideos.size) View.VISIBLE else View.GONE
    }

    fun loadFeedData(force: Boolean = false) {
        viewLifecycleOwner.lifecycleScope.launch {
            setLoading(true)
            try {
                // 1. ดึงข้อมูลฟีดจาก Supabase kids_video_feeds
                val feeds = VideoFeedRepository.getFeeds(force)
                val targetFeed = feeds.firstOrNull {
                    it.id == DEFAULT_FEED_ID ||
                            (it.name.contains("Thai PBS", ignoreCase = true) && it.icon?.contains("🌟") == true) ||
                            it.name.contains("Thai PBS Kids", ignoreCase = true)
                } ?: KidsVideoFeedItem(
                    id = DEFAULT_FEED_ID,
                    name = "Thai PBS Kids",
                    icon = "🌟",
                    feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=UCkBuvFq-7IcZdIwfG8zrLyg",
                    provider = "Thai PBS Kids",
                    category = "video",
                    description = "แหล่งรวมรายการเด็กสร้างสรรค์ เช่น บ้านนักวิทยาศาสตร์น้อย, Kid Rangers, สอนศิลป์, หม้อข้าวหม้อแกง"
                )

                currentFeed = targetFeed
                updateBanner(targetFeed)

                // 2. โหลดรายการวิดีโอจาก feed_url
                val videos = withContext(Dispatchers.IO) {
                    VideoFeedRepository.loadFeedVideos(targetFeed, force)
                }

                if (videos.isNotEmpty()) {
                    allVideos.clear()
                    allVideos.addAll(videos)
                    videoList.clear()
                    videoList.addAll(allVideos.take(PAGE_SIZE))
                    adapterVideo?.notifyDataSetChanged()
                    recyclerView.visibility = View.VISIBLE
                    btnLoadMore.visibility = if (allVideos.size > videoList.size) View.VISIBLE else View.GONE
                    lytNoItem.visibility = View.GONE
                    lytFailed.visibility = View.GONE
                } else {
                    if (videoList.isEmpty()) {
                        btnLoadMore.visibility = View.GONE
                        lytNoItem.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading Thai PBS Kids feed: ${e.message}")
                if (videoList.isEmpty()) {
                    // Fallback to curated videos
                    val fallback = VideoFeedRepository.getFallbackVideosForFeed(
                        currentFeed ?: KidsVideoFeedItem(id = DEFAULT_FEED_ID, name = "Thai PBS Kids")
                    )
                    allVideos.clear()
                    allVideos.addAll(fallback)
                    videoList.clear()
                    videoList.addAll(allVideos.take(PAGE_SIZE))
                    adapterVideo?.notifyDataSetChanged()
                    btnLoadMore.visibility = if (allVideos.size > videoList.size) View.VISIBLE else View.GONE
                    recyclerView.visibility = View.VISIBLE
                    lytFailed.visibility = View.GONE
                }
            } finally {
                setLoading(false)
            }
        }
    }

    private fun updateBanner(feed: KidsVideoFeedItem) {
        txtBannerIcon.text = feed.icon.ifNullOrEmpty("🌟")
        txtBannerTitle.text = feed.name.ifNullOrEmpty("Thai PBS Kids")
        txtBannerDescription.text = feed.description.ifNullOrEmpty("แหล่งรวมรายการเด็กสร้างสรรค์ เช่น บ้านนักวิทยาศาสตร์น้อย, Kid Rangers, สอนศิลป์, หม้อข้าวหม้อแกง")
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading && videoList.isEmpty()) {
            shimmerFrameLayout.visibility = View.VISIBLE
            shimmerFrameLayout.startShimmer()
            recyclerView.visibility = View.GONE
            btnLoadMore.visibility = View.GONE
        } else {
            shimmerFrameLayout.visibility = View.GONE
            shimmerFrameLayout.stopShimmer()
        }
    }

    private fun openDetailActivity(video: Video) {
        try {
            val intent = Intent(requireContext(), RssFeedDetailActivity::class.java).apply {
                putExtra(Constant.KEY_VID, video.vid)
                putExtra(Constant.EXTRA_OBJC, video)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening RssFeedDetailActivity: ${e.message}")
        }
    }

    private fun String?.ifNullOrEmpty(default: String): String {
        return if (this.isNullOrBlank()) default else this
    }
}

