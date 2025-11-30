package com.pongploydev.videowatchfeature

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.pongploydev.videowatchfeature.activity.ActivityVideoAutoDetail
import com.pongploydev.videowatchfeature.activity.ActivityVideoDetail
import com.pongploydev.videowatchfeature.adapter.AdapterVideo
import com.pongploydev.videowatchfeature.callbacks.CallbackCategoryDetails
import com.pongploydev.videowatchfeature.config.AppConfig
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.rests.RestAdapter
import com.pongploydev.videowatchfeature.utils.EqualSpacingItemDecoration
import com.pongploydev.videowatchfeature.utils.Tools
import retrofit2.Call
import retrofit2.Callback

class VideoListFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var lytNoItem: View
    private lateinit var lytFailed: View
    private lateinit var txtNoItemMessage: TextView
    private lateinit var txtFailedMessage: TextView
    private lateinit var btnRetry: View

    private var adapterVideo: AdapterVideo? = null
    private var callbackCall: retrofit2.Call<CallbackCategoryDetails?>? = null

    private var postTotal = 0
    private var failedPage = 0

    private lateinit var sharedPref: SharedPref
    private lateinit var tools: Tools
    private var activityContext: Activity? = null

    // ตัวแปรสำหรับจัดการ ActionBar
    private var isActionBarHidden = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityContext = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_video_list, container, false)
        initViews()
        setupComponents()
        setupRecyclerView()
        setupListeners()

        // ซ่อน ActionBar เมื่อ Fragment ถูกสร้าง
        hideActionBar()

        if (AppConfig.ALLOW_APP_ACCESSED_USING_VPN) {
            requestAction(1)
        } else {
            requestAction(1)
        }

        return rootView
    }

    private fun initViews() {
        rootView?.apply {
            recyclerView = findViewById(R.id.recyclerView)
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_home)
            shimmerFrameLayout = findViewById(R.id.shimmer_view_container)
            lytNoItem = findViewById(R.id.lyt_no_item_home)
            lytFailed = findViewById(R.id.lyt_failed_home)
            txtNoItemMessage = findViewById(R.id.no_item_message)
            txtFailedMessage = findViewById(R.id.failed_message)
            btnRetry = findViewById(R.id.failed_retry)
        }
    }

    private fun setupComponents() {
        activityContext?.let {
            tools = Tools(it)
            sharedPref = SharedPref(it)
            sharedPref.setDefaultSortHome()
        }

        setHasOptionsMenu(true)
        setupSwipeRefreshLayout()
    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(
            R.color.color_light_primary,
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light
        )
    }

    private fun setupRecyclerView() {
        // Adjust padding for compact view
        if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
            val spacing = resources.getDimensionPixelOffset(R.dimen.spacing_small)
            recyclerView.setPadding(0, spacing, 0, spacing)
        }

        recyclerView.addItemDecoration(EqualSpacingItemDecoration(0))
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        // Initialize adapter
        adapterVideo = AdapterVideo(activityContext, recyclerView, mutableListOf()).apply {
            setOnItemClickListener { _, video, position ->
                Log.d("VideoClick", "Video clicked at position: $position")
                openVideoActivity(video)
            }

            setOnItemOverflowClickListener { _, video, position ->
                // Handle overflow menu click if needed
                Log.d("OverflowClick", "Overflow clicked for video: ${video?.id}")
            }

            setOnLoadMoreListener { currentPage ->
                setLoadMore(currentPage)
            }
        }

        recyclerView.adapter = adapterVideo

        // เพิ่ม Scroll Listener เพื่อซ่อน/แสดง ActionBar เมื่อ scroll
        setupScrollListener()
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var scrolledDistance = 0
            private var controlsVisible = true

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // ซ่อน/แสดง ActionBar เมื่อ scroll ขึ้นลง
                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                    hideActionBar()
                    controlsVisible = false
                    scrolledDistance = 0
                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                    showActionBar()
                    controlsVisible = true
                    scrolledDistance = 0
                }

                if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                    scrolledDistance += dy
                }
            }
        })
    }

    private fun setupListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            callbackCall?.takeIf { it.isExecuted }?.cancel()
            adapterVideo?.resetListData()
            requestAction(1)
        }

        btnRetry.setOnClickListener {
            requestAction(1)
        }
    }

    // ฟังก์ชันซ่อน ActionBar
    private fun hideActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        isActionBarHidden = true
    }

    // ฟังก์ชันแสดง ActionBar
    private fun showActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.show()
        isActionBarHidden = false
    }

    // ฟังก์ชันสลับสถานะ ActionBar
    private fun toggleActionBar() {
        if (isActionBarHidden) {
            showActionBar()
        } else {
            hideActionBar()
        }
    }

    private fun openVideoActivity(video: Video?) {
        if (video == null) {
            showToast("ข้อมูลวิดีโอไม่พร้อม")
            return
        }

        val intent = createVideoIntent(video.video_type).apply {
            putExtra(Constant.EXTRA_OBJC, video)
        }

        showInterstitialAdIfNeeded()
        startActivity(intent)
    }

    private fun createVideoIntent(videoType: String?): Intent {
        return when (videoType?.lowercase()) {
            "youtube" -> Intent(activityContext, ActivityVideoAutoDetail::class.java)
            else -> Intent(activityContext, ActivityVideoDetail::class.java)
        }
    }

    private fun showInterstitialAdIfNeeded() {
        try {
            (activityContext as? MainActivity)?.apply {
                // Show ads implementation
            }
        } catch (e: Exception) {
            Log.w("Ads", "Failed to show ads: ${e.message}")
        }
    }

    private fun setLoadMore(currentPage: Int) {
        Log.d("Pagination", "Loading more data for page: $currentPage")
        val totalItemsBeforeAds = (adapterVideo?.itemCount ?: 0) - currentPage

        if (postTotal > totalItemsBeforeAds && currentPage != 0) {
            requestAction(currentPage + 1)
        } else {
            adapterVideo?.setLoaded()
        }
    }

    private fun displayApiResult(videos: List<Video?>) {
        adapterVideo?.insertData(videos)
        swipeProgress(false)

        if (videos.isEmpty()) {
            showNoItemView(true)
        }
    }

    private fun requestListPostApi(pageNo: Int) {
        val apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl())

        val sortType = when (sharedPref.getCurrentSortHome()) {
            0 -> Constant.MOST_POPULAR
            1 -> Constant.ADDED_OLDEST
            2 -> Constant.ADDED_NEWEST
            else -> Constant.ADDED_NEWEST
        }

        callbackCall = apiInterface.getCategoryVideos(
            6,
            pageNo,
            AppConfig.LOAD_MORE,
            sortType,
            AppConfig.REST_API_KEY
        )

        callbackCall?.enqueue(object : Callback<CallbackCategoryDetails?> {
            override fun onResponse(
                call: retrofit2.Call<CallbackCategoryDetails?>,
                response: retrofit2.Response<CallbackCategoryDetails?>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val categoryDetails = response.body()!!
                    postTotal = categoryDetails.count_total

                    Log.d("API_SUCCESS", "Loaded ${categoryDetails.posts.size} videos")
                    displayApiResult(categoryDetails.posts)
                } else {
                    Log.e("API_ERROR", "Code: ${response.code()}, Message: ${response.message()}")
                    onFailRequest(pageNo)
                }
            }

            override fun onFailure(
                call: Call<CallbackCategoryDetails?>,
                t: Throwable
            ) {
                if (!call.isCanceled) {
                    onFailRequest(pageNo)
                }
                Log.e("API_FAILURE", "Request failed: ${t.message}", t)
            }


        })
    }

    private fun onFailRequest(pageNo: Int) {
        failedPage = pageNo
        adapterVideo?.setLoaded()
        swipeProgress(false)

        val errorMessage = if (Tools.isConnect(activityContext)) {
            getString(R.string.failed_text)
        } else {
            getString(R.string.no_internet)
        }
        showFailedView(true, errorMessage)
    }

    private fun requestAction(pageNo: Int) {
        showFailedView(false, "")
        showNoItemView(false)

        if (pageNo == 1) {
            swipeProgress(true)
        } else {
            adapterVideo?.setLoading()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            requestListPostApi(pageNo)
        }, Constant.DELAY_TIME)
    }

    private fun showFailedView(show: Boolean, message: String?) {
        lytFailed.isVisible = show
        recyclerView.isVisible = !show
        txtFailedMessage.text = message ?: getString(R.string.failed_text)
    }

    private fun showNoItemView(show: Boolean) {
        lytNoItem.isVisible = show
        recyclerView.isVisible = !show
        txtNoItemMessage.setText(R.string.msg_no_item)
    }

    private fun swipeProgress(show: Boolean) {
        if (!show) {
            swipeRefreshLayout.isRefreshing = false
            shimmerFrameLayout.visibility = View.GONE
            shimmerFrameLayout.stopShimmer()
            return
        }

        swipeRefreshLayout.post {
            swipeRefreshLayout.isRefreshing = true
            shimmerFrameLayout.visibility = View.VISIBLE
            shimmerFrameLayout.startShimmer()
        }
    }

//    private fun initShimmerLayout() {
//        val lytShimmerDefault = rootView?.findViewById<View>(R.id.lyt_shimmer_default)
//        val lytShimmerCompact = rootView?.findViewById<View>(R.id.lyt_shimmer_compact)
//
//        val isCompactView = sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT
//        lytShimmerDefault?.isVisible = !isCompactView
//        lytShimmerCompact?.isVisible = isCompactView
//    }

    private fun showToast(message: String) {
        Toast.makeText(activityContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // ซ่อน ActionBar เมื่อกลับมาที่ Fragment นี้
        hideActionBar()
    }

    override fun onPause() {
        super.onPause()
        // แสดง ActionBar เมื่อออกจาก Fragment นี้ (optional)
        // showActionBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        swipeProgress(false)
        callbackCall?.takeIf { it.isExecuted }?.cancel()
        shimmerFrameLayout.stopShimmer()
    }



    companion object {
        const val ARG_ID_CAT = "id_cat"

        // ค่าคงที่สำหรับการซ่อน/แสดง ActionBar
        private const val HIDE_THRESHOLD = 20

        fun newInstance(): VideoListFragment {
            return VideoListFragment().apply {
                arguments = Bundle().apply {
                    // Add arguments if needed
                }
            }
        }
    }
}