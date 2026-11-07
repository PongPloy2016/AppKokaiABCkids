package com.pongploydev.videowatchfeaturesupabase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pongploydev.videowatchfeaturesupabase.activity.ActivityVideoAutoDetail
import com.pongploydev.videowatchfeaturesupabase.activity.ActivityVideoDetail
import com.pongploydev.videowatchfeaturesupabase.adapter.AdapterVideo
import com.pongploydev.videowatchfeaturesupabase.config.AppConfig
import com.pongploydev.videowatchfeaturesupabase.databases.prefs.AdsPref
import com.pongploydev.videowatchfeaturesupabase.databases.prefs.SharedPref
import com.pongploydev.videowatchfeaturesupabase.model.Video
import com.pongploydev.videowatchfeaturesupabase.utils.Tools
import com.pongploydev.videowatchfeaturesupabase.viewmodel.FavoritesViewModel
import com.pongploydev.videowatchfeaturesupabase.viewmodel.VideoListViewModel

class VideoListFragment : Fragment() {

    private var rootView: View? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var lytNoItem: View
    private lateinit var lytFailed: View
    private lateinit var lytNoSearchResults: LinearLayout
    private lateinit var txtNoItemMessage: TextView
    private lateinit var txtFailedMessage: TextView
    private lateinit var txtNoSearchMessage: TextView
    private lateinit var txtSearchSuggest: TextView
    private lateinit var btnRetry: View
    private lateinit var btnClearSearch: Button

    // ตัวแปรสำหรับ Header และค้นหา
    private lateinit var searchContainer: MaterialCardView
    private lateinit var searchInput: EditText
    private lateinit var searchIcon: ImageView
    // private lateinit var clearSearchBtn: ImageView
    // private lateinit var sortButton: ImageView
    // private lateinit var refreshButton: ImageView
    // private lateinit var favoriteButton: ImageView
    // private lateinit var switchViewButton: ImageView
    private lateinit var categoryContainer: LinearLayout
    private lateinit var categoryAll: MaterialCardView
    private lateinit var categoryLearning: MaterialCardView
    private lateinit var categoryAbc: MaterialCardView
    private lateinit var categoryThaipbsKids: MaterialCardView
    private lateinit var categoryThaipbsMain: MaterialCardView
    private lateinit var categoryPrograms: MaterialCardView
    private lateinit var categoryTalk: MaterialCardView
    private lateinit var categoryStory: MaterialCardView
    private lateinit var txtCategoryAll: TextView
    private lateinit var txtCategoryLearning: TextView
    private lateinit var txtCategoryAbc: TextView
    private lateinit var txtCategoryThaipbsKids: TextView
    private lateinit var txtCategoryThaipbsMain: TextView
    private lateinit var txtCategoryPrograms: TextView
    private lateinit var txtCategoryTalk: TextView
    private lateinit var txtCategoryStory: TextView
    private var btnFavoritePage: View? = null
    private var btnFavoriteQuick: ImageView? = null
    private var containerThaiPbsKids: FrameLayout? = null
    private var btnLoadMoreHome: View? = null
    private var nestedScrollViewHome: androidx.core.widget.NestedScrollView? = null
    private val dynamicCategoryViews = mutableMapOf<String, Pair<MaterialCardView, TextView>>()


    // ตัวแปรสำหรับ Interstitial Ad
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var interstitialLoadAttempt = 0
    private val MAX_INTERSTITIAL_LOAD_ATTEMPTS = 5

    private var adapterVideo: AdapterVideo? = null

    private lateinit var sharedPref: SharedPref
    private lateinit var adsPref: AdsPref
    private lateinit var tools: Tools
    private var activityContext: Activity? = null

    // ตัวแปรสำหรับจัดการ ActionBar
    private var isActionBarHidden = false

    // ตัวแปรสำหรับนับรอบการแสดง Interstitial Ad
    private var videoClickCount = 0
    private val INTERSTITIAL_AD_FREQUENCY = 1 // แสดงโฆษณาทุก 1 ครั้ง (เพื่อทดสอบ)

    // ตัวแปรเก็บวิดีโอปัจจุบัน
    private var currentVideo: Video? = null

    // ตัวแปรสำหรับเช็คการ initialize AdMob
    private var isAdMobInitialized = false

    // ตัวแปรสำหรับการค้นหา
    private var searchQuery = ""
    private var searchHandler: Handler? = null
    private val SEARCH_DELAY = 500L // ดีเลย์ 500ms

    // ตัวแปรสำหรับแอปครอบครัว
    private var isFamiliesCompliant = false

    // ViewModel
    private lateinit var viewModel: VideoListViewModel
    private lateinit var favoritesViewModel: FavoritesViewModel

    // Inline YouTube Preview Variables
    private var activeYouTubePlayerView: YouTubePlayerView? = null
    private var activeViewHolder: AdapterVideo.OriginalViewHolder? = null
    private var activeYouTubePlayer: YouTubePlayer? = null
    private var isPreviewMuted = true

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

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(VideoListViewModel::class.java)
        favoritesViewModel = ViewModelProvider(this).get(FavoritesViewModel::class.java)

        initViews()
        setupComponents()
        setupRecyclerView()
        setupListeners()
        setupSearch()
        setupCategories()
        setupObservers()
        updateSwitchViewButtonIcon()

        adsPref.isTestMode = false
        adsPref.interstitialAdId = "ca-app-pub-3279793802315716/8581063137"
        adsPref.interstitialPostDetailId = "ca-app-pub-3279793802315716/8581063137"
        adsPref.isFamiliesApp = true
        adsPref.isAdEnabled = true
        adsPref.isInterstitialAdEnabled = true
        adsPref.setPlacement(
            isBannerHome = true,
            isBannerPostDetails = true,
            isBannerCategoryDetails = true,
            isBannerSearch = true,
            isInterstitialPostList = true,
            isInterstitialPostDetails = true,
            isNativeAdPostList = false,
            isNativeAdPostDetails = false,
            isNativeAdExitDialog = false,
            isAppOpenAdOnStart = false,
            isAppOpenAdOnResume = false
        )

        // ซ่อน ActionBar เมื่อ Fragment ถูกสร้าง
        hideActionBar()

        // ตรวจสอบและโหลดโฆษณา
        initializeAds()

        swipeProgress(true)
        if (sharedPref.isUseSupabaseApi) {
            requestAction(1)
        } else {
            ensureCookieChallengeSolved {
                requestAction(1)
            }
        }

        return rootView
    }

    private fun setupObservers() {
        viewModel.videoList.observe(viewLifecycleOwner) { videos ->
            if (videos != null) {
                displaySearchResults(videos.toMutableList())
                adapterVideo?.setLoaded()
                val hasMore = viewModel.postTotal > videos.size
                btnLoadMoreHome?.visibility = if (hasMore && containerThaiPbsKids?.visibility != View.VISIBLE) View.VISIBLE else View.GONE
                showNoItemView(
                    videos.isEmpty() &&
                            viewModel.isLoading.value != true &&
                            viewModel.searchQuery.isEmpty() &&
                            viewModel.currentCategory == "all"
                )
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading != null) {
                swipeProgress(isLoading)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showFailedView(true, error)
            }
        }

        viewModel.videoFeeds.observe(viewLifecycleOwner) { feeds ->
            if (!feeds.isNullOrEmpty()) {
                updateCategoryTabsFromFeeds(feeds)
            }
        }
    }

    private fun requestAction(pageNo: Int, isRefresh: Boolean = false) {
        showFailedView(false, "")
        showNoItemView(false)
        hideNoSearchResults()

        viewModel.fetchVideos(pageNo, isRefresh)
    }

    private fun refreshVideos() {
        if (sharedPref.isUseSupabaseApi) {
            requestAction(1, true)
        } else {
            ensureCookieChallengeSolved {
                requestAction(1, true)
            }
        }
    }

    private fun initViews() {
        rootView?.apply {
            recyclerView = findViewById(R.id.recyclerView)
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_home)
            shimmerFrameLayout = findViewById(R.id.shimmer_view_container)
            lytNoItem = findViewById(R.id.lyt_no_item_home)
            lytFailed = findViewById(R.id.lyt_failed_home)
            // lytNoSearchResults = findViewById(R.id.lyt_no_search_results)
            txtNoItemMessage = findViewById(R.id.no_item_message)
            txtFailedMessage = findViewById(R.id.failed_message)
            // txtNoSearchMessage = findViewById(R.id.txt_no_search_message)
            // txtSearchSuggest = findViewById(R.id.txt_search_suggest)
            btnRetry = findViewById(R.id.failed_retry)
            // btnClearSearch = findViewById(R.id.btn_clear_search)

            // ประกาศตัวแปรสำหรับ Header และค้นหา
            searchContainer = findViewById(R.id.search_container)
            searchInput = findViewById(R.id.search_input)
            // searchIcon = findViewById(R.id.search_icon)
            // clearSearchBtn = findViewById(R.id.clear_search_btn)
            // sortButton = findViewById(R.id.sort_button)
            // refreshButton = findViewById(R.id.refresh_button)
            // favoriteButton = findViewById(R.id.favorite_button)
            // switchViewButton = findViewById(R.id.switch_view_button)
            categoryContainer = findViewById(R.id.category_container)
            categoryAll = findViewById(R.id.category_all)
            categoryLearning = findViewById(R.id.category_learning)
            categoryAbc = findViewById(R.id.category_abc)
            categoryThaipbsKids = findViewById(R.id.category_thaipbs_kids)
            categoryThaipbsMain = findViewById(R.id.category_thaipbs_main)
            categoryPrograms = findViewById(R.id.category_programs)
            categoryTalk = findViewById(R.id.category_talk)
            categoryStory = findViewById(R.id.category_story)
            txtCategoryAll = findViewById(R.id.txt_category_all)
            txtCategoryLearning = findViewById(R.id.txt_category_learning)
            txtCategoryAbc = findViewById(R.id.txt_category_abc)
            txtCategoryThaipbsKids = findViewById(R.id.txt_category_thaipbs_kids)
            txtCategoryThaipbsMain = findViewById(R.id.txt_category_thaipbs_main)
            txtCategoryPrograms = findViewById(R.id.txt_category_programs)
            txtCategoryTalk = findViewById(R.id.txt_category_talk)
            txtCategoryStory = findViewById(R.id.txt_category_story)
            btnFavoritePage = findViewById(R.id.btn_favorite_page)
            btnFavoriteQuick = findViewById(R.id.btn_favorite_quick)
            containerThaiPbsKids = findViewById(R.id.container_thai_pbs_kids)
            btnLoadMoreHome = findViewById(R.id.btn_load_more_home)
            nestedScrollViewHome = findViewById(R.id.nested_scroll_view_home)
        }
    }



    private fun setupComponents() {
        activityContext?.let {
            tools = Tools(it)
            sharedPref = SharedPref(it)
            adsPref = AdsPref(it)
            sharedPref.setDefaultSortHome()

            // ตั้งค่าเริ่มต้นสำหรับแอปครอบครัว
            if (adsPref.isFamiliesApp) {
                setupFamiliesDefaults()
            }
        }

        setHasOptionsMenu(true)
        setupSwipeRefreshLayout()
    }

    /** ตั้งค่าเริ่มต้นสำหรับแอปครอบครัว */
    private fun setupFamiliesDefaults() {
        // สำหรับแอปครอบครัว: ปิด Native Ad
        if (adsPref.isNativeAdPostList) {
            Log.w("FamiliesSetup", "Native ads disabled for Families app")
            adsPref.isNativeAdPostList = false
        }

        // สำหรับแอปครอบครัว: ตั้งค่า Content Rating เป็น G
        if (adsPref.adContentRating != "G") {
            adsPref.adContentRating = "G"
            Log.d("FamiliesSetup", "Ad content rating set to G")
        }

        Log.d("FamiliesSetup", "✅ Families app configuration loaded")
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
        // เพิ่มเพื่อลดการกระพริบ
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null // ปิด animation ชั่วคราวเพื่อทดสอบ
        recyclerView.isDrawingCacheEnabled = true
        recyclerView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        
        setupRecyclerViewLayoutManager()
        setupRecyclerViewAdapter()

        // เพิ่ม Scroll Listener เพื่อซ่อน/แสดง ActionBar เมื่อ scroll
        setupScrollListener()
    }

    private fun setupRecyclerViewLayoutManager() {
        val viewType = sharedPref.videoViewType
        if (viewType == Constant.VIDEO_LIST_COMPACT) {
            val spacing = resources.getDimensionPixelOffset(R.dimen.spacing_small)
            recyclerView.setPadding(0, spacing, 0, spacing)
            recyclerView.layoutManager = LinearLayoutManager(activityContext)
        } else {
            // ปรับเป็น List (span 1) แทน Grid (span 2) ตามคำขอของผู้ใช้
            val spacing = resources.getDimensionPixelOffset(R.dimen.spacing_medium)
            recyclerView.setPadding(spacing, spacing, spacing, spacing)
            recyclerView.layoutManager = LinearLayoutManager(activityContext)
        }
    }

    private fun setupRecyclerViewAdapter() {
        val videos = viewModel.videoList.value ?: emptyList()
        adapterVideo = AdapterVideo(activityContext!!, recyclerView, videos.toMutableList()).apply {
            setOnItemClickListener { _, video, position ->
                Log.d("VideoClick", "Video clicked at position: $position")
                stopActivePreview()
                openVideoActivity(video)
            }

            setOnItemOverflowClickListener { _, video, position ->
                Log.d("OverflowClick", "Overflow clicked for video: ${video?.id}")
            }

            setOnItemLongClickListener { _, video, position, holder ->
                if (video?.video_type?.lowercase() == "youtube") {
                    showYouTubeInlinePreview(video, holder)
                } else {
                    showToast("เฉพาะวิดีโอ YouTube เท่านั้นที่รองรับการพรีวิว")
                }
            }

            setOnLoadMoreListener { _ ->
                val loadedItems = adapterVideo?.videoCount ?: (adapterVideo?.itemCount ?: 0)
                if (viewModel.postTotal > loadedItems) {
                    adapterVideo?.setLoading()
                    viewModel.loadNextPage()
                } else {
                    adapterVideo?.setLoaded()
                }
            }
        }
        recyclerView.adapter = adapterVideo
    }

    private fun updateSwitchViewButtonIcon() {
        // Functionality removed for new UI
    }

    private fun toggleViewType() {
        val currentType = sharedPref.videoViewType
        val newType = if (currentType == Constant.VIDEO_LIST_COMPACT) {
            Constant.VIDEO_LIST_DEFAULT
        } else {
            Constant.VIDEO_LIST_COMPACT
        }
        sharedPref.updateVideoViewType(newType)

        setupRecyclerViewLayoutManager()
        updateSwitchViewButtonIcon()
        setupRecyclerViewAdapter()
    }

    private fun setupScrollListener() {
        recyclerView.addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    private var scrolledDistance = 0
                    private var controlsVisible = true

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        // Stop YouTube preview when scrolling
                        if (dy != 0) {
                            stopActivePreview()
                        }
                    }
                }
        )
    }

    private fun setupListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            if (containerThaiPbsKids?.visibility == View.VISIBLE) {
                val frag = childFragmentManager.findFragmentById(R.id.container_thai_pbs_kids) as? com.pongploydev.videowatchfeaturesupabase.fragment.ThaiPbsKidsFragment
                frag?.loadFeedData(force = true)
                swipeProgress(false)
            } else if (sharedPref.isUseSupabaseApi) {
                requestAction(1, true)
            } else {
                ensureCookieChallengeSolved {
                    requestAction(1, true)
                }
            }
            // รีโหลด Interstitial Ad
            loadInterstitialAd()
        }

        btnRetry.setOnClickListener {
            swipeProgress(true)
            if (sharedPref.isUseSupabaseApi) {
                requestAction(1, true)
            } else {
                ensureCookieChallengeSolved {
                    requestAction(1, true)
                }
            }
            loadInterstitialAd()
        }

        btnLoadMoreHome?.setOnClickListener {
            adapterVideo?.setLoading()
            viewModel.loadNextPage()
        }

        nestedScrollViewHome?.setOnScrollChangeListener(
            androidx.core.widget.NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                stopActivePreview()
                if (scrollY >= (v.getChildAt(0).measuredHeight - v.measuredHeight - 350)) {
                    val loadedItems = adapterVideo?.videoCount ?: (adapterVideo?.itemCount ?: 0)
                    if (viewModel.postTotal > loadedItems && !viewModel.isFetchingVideos) {
                        adapterVideo?.setLoading()
                        viewModel.loadNextPage()
                    }
                }
            }
        )
    }

    fun openFavoriteList() {
        val favoriteFragment = FavoriteListFragment.newInstance()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, favoriteFragment, "FavoriteListFragment")
            .addToBackStack("FavoriteListFragment")
            .commit()
    }

    private fun setupSearch() {
        searchHandler = Handler(Looper.getMainLooper())

        // ตั้งค่า TextWatcher สำหรับ real-time search
        searchInput.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {
                        // ไม่ต้องทำอะไร
                    }

                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {
                        // ล้าง handler ก่อนหน้า
                        searchHandler?.removeCallbacksAndMessages(null)

                        // ดีเลย์การค้นหาเพื่อป้องกันการค้นหาบ่อยเกินไป
                        searchHandler?.postDelayed(
                                {
                                    val query = s.toString().trim()
                                    if (query != searchQuery) {
                                        searchQuery = query
                                        performSearch(query)
                                    }
                                },
                                SEARCH_DELAY
                        )
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // ไม่ต้องทำอะไร
                    }
                }
        )

        // ตั้งค่าปุ่มเรียงลำดับ
        rootView?.findViewById<View>(R.id.btn_sort_new)?.setOnClickListener { showSortMenu() }

        // ตั้งค่าเมื่อกด search บน keyboard
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString().trim()
                searchQuery = query
                performSearch(query)
                // ซ่อน keyboard
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        // ตั้งค่าเมื่อคลิกที่ช่องค้นหา
        searchContainer.setOnClickListener {
            searchInput.requestFocus()
            showKeyboard()
        }
    }

    private fun setupCategories() {
        categoryAll.setOnClickListener { selectCategory("all") }
        btnFavoritePage?.setOnClickListener { openFavoriteList() }
        btnFavoriteQuick?.setOnClickListener { openFavoriteList() }
        categoryLearning.setOnClickListener { selectCategory("learning") }
        categoryAbc.setOnClickListener { selectCategory("abc") }
        categoryThaipbsKids.setOnClickListener { selectCategory("thaipbs_kids_youtube") }
        categoryThaipbsMain.setOnClickListener { selectCategory("thaipbs_youtube_main") }
        categoryPrograms.setOnClickListener { selectCategory("thaipbs_kids_programs") }
        categoryTalk.setOnClickListener { selectCategory("thaipbs_talk_about_kids") }
        categoryStory.setOnClickListener { selectCategory("story_feed") }

        // ซ่อนแท็บเก่าที่ไม่ได้อยู่ในตาราง Supabase kids_video_feeds เริ่มต้นก่อน
        categoryThaipbsKids.visibility = View.GONE
        categoryThaipbsMain.visibility = View.GONE
        categoryTalk.visibility = View.GONE
        categoryStory.visibility = View.GONE

        updateCategoryTabsFromFeeds(com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem.DEFAULT_FEEDS)
        updateCategoryUI("all")
    }

    private fun isRssFeedCategory(category: String): Boolean {
        val nonFeedCategories = setOf("all", "learning", "math", "abc", "english")
        return category.lowercase() !in nonFeedCategories
    }

    private fun selectCategory(category: String) {
        updateCategoryUI(category)
        if (isRssFeedCategory(category)) {
            btnLoadMoreHome?.visibility = View.GONE
            containerThaiPbsKids?.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            lytNoItem.visibility = View.GONE
            lytFailed.visibility = View.GONE
            shimmerFrameLayout.visibility = View.GONE

            var frag = childFragmentManager.findFragmentById(R.id.container_thai_pbs_kids) as? com.pongploydev.videowatchfeaturesupabase.fragment.ThaiPbsKidsFragment
            if (frag == null) {
                frag = com.pongploydev.videowatchfeaturesupabase.fragment.ThaiPbsKidsFragment.newInstance(category)
                childFragmentManager.beginTransaction()
                    .replace(R.id.container_thai_pbs_kids, frag)
                    .commitNowAllowingStateLoss()
            } else {
                frag.loadFeedById(category, force = false)
            }
        } else {
            containerThaiPbsKids?.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            viewModel.filterVideos(viewModel.searchQuery, category)
        }
    }

    private fun updateCategoryTabsFromFeeds(feeds: List<com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem>) {
        val context = activityContext ?: return
        val activeFeedIds = feeds.map { it.id }.toSet()

        categoryThaipbsKids.visibility = if ("thaipbs_kids_youtube" in activeFeedIds) View.VISIBLE else View.GONE
        categoryThaipbsMain.visibility = if ("thaipbs_youtube_main" in activeFeedIds) View.VISIBLE else View.GONE
        categoryPrograms.visibility = if ("thaipbs_kids_programs" in activeFeedIds) View.VISIBLE else View.GONE
        categoryTalk.visibility = if ("thaipbs_talk_about_kids" in activeFeedIds) View.VISIBLE else View.GONE
        categoryStory.visibility = if ("story_feed" in activeFeedIds) View.VISIBLE else View.GONE

        val sortedFeeds = feeds.sortedBy { it.sortOrder }
        for (feed in sortedFeeds) {
            val label = if (!feed.icon.isNullOrEmpty()) "${feed.icon} ${feed.name}" else feed.name
            when (feed.id) {
                "thaipbs_kids_youtube" -> {
                    txtCategoryThaipbsKids.text = label
                    categoryThaipbsKids.setOnClickListener { selectCategory(feed.id) }
                }
                "thaipbs_youtube_main" -> {
                    txtCategoryThaipbsMain.text = label
                    categoryThaipbsMain.setOnClickListener { selectCategory(feed.id) }
                }
                "thaipbs_kids_programs" -> {
                    txtCategoryPrograms.text = label
                    categoryPrograms.setOnClickListener { selectCategory(feed.id) }
                }
                "thaipbs_talk_about_kids" -> {
                    txtCategoryTalk.text = label
                    categoryTalk.setOnClickListener { selectCategory(feed.id) }
                }
                "story_feed" -> {
                    txtCategoryStory.text = label
                    categoryStory.setOnClickListener { selectCategory(feed.id) }
                }
                else -> {
                    // แท็บใหม่ที่เพิ่มจาก Supabase ตาราง kids_video_feeds
                    if (!dynamicCategoryViews.containsKey(feed.id)) {
                        createDynamicCategoryChip(context, feed)
                    } else {
                        dynamicCategoryViews[feed.id]?.second?.text = label
                    }
                }
            }
        }
    }

    private fun createDynamicCategoryChip(
        context: Context,
        feed: com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem
    ) {
        val density = resources.displayMetrics.density
        val card = MaterialCardView(context).apply {
            radius = 20 * density
            cardElevation = 0f
            strokeWidth = (1 * density).toInt()
            setStrokeColor(Color.parseColor("#E0E0E0"))
            setCardBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (8 * density).toInt()
            }
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((14 * density).toInt(), (10 * density).toInt(), (14 * density).toInt(), (10 * density).toInt())
        }

        val textView = TextView(context).apply {
            text = if (!feed.icon.isNullOrEmpty()) "${feed.icon} ${feed.name}" else feed.name
            setTextColor(Color.parseColor("#2D3047"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        layout.addView(textView)
        card.addView(layout)

        card.setOnClickListener {
            selectCategory(feed.id)
        }

        val sortBtn = rootView?.findViewById<View>(R.id.btn_sort_new)
        val sortIndex = if (sortBtn != null) categoryContainer.indexOfChild(sortBtn) else -1
        if (sortIndex >= 0) {
            categoryContainer.addView(card, sortIndex)
        } else {
            categoryContainer.addView(card)
        }
        dynamicCategoryViews[feed.id] = Pair(card, textView)
    }

    private fun updateCategoryCard(
        card: MaterialCardView,
        text: TextView,
        isSelected: Boolean,
        pinkColor: Int,
        whiteColor: Int,
        darkTextColor: Int,
        strokeColor: Int,
        strokeWidthPx: Int
    ) {
        if (isSelected) {
            card.setCardBackgroundColor(pinkColor)
            card.strokeWidth = 0
            text.setTextColor(whiteColor)
        } else {
            card.setCardBackgroundColor(whiteColor)
            card.setStrokeColor(strokeColor)
            card.strokeWidth = strokeWidthPx
            text.setTextColor(darkTextColor)
        }
    }

    private fun updateCategoryUI(selectedCategory: String) {
        try {
            val pinkColor = Color.parseColor("#FF6B8E")
            val whiteColor = Color.parseColor("#FFFFFF")
            val darkTextColor = Color.parseColor("#2D3047")
            val strokeColor = Color.parseColor("#E0E0E0")
            val strokeWidthPx = (1 * resources.displayMetrics.density).toInt()

            val isThaipbsKidsSelected = selectedCategory == "thaipbs_kids" || selectedCategory == "thaipbs_kids_youtube"
            val isProgramsSelected = selectedCategory == "programs" || selectedCategory == "thaipbs_kids_programs" || selectedCategory == "ฝึกอ่านและเขียน"

            updateCategoryCard(categoryAll, txtCategoryAll, selectedCategory == "all", pinkColor, whiteColor, darkTextColor, strokeColor, strokeWidthPx)
            updateCategoryCard(categoryLearning, txtCategoryLearning, selectedCategory == "learning" || selectedCategory == "math", pinkColor, whiteColor, darkTextColor, strokeColor, strokeWidthPx)
            updateCategoryCard(categoryAbc, txtCategoryAbc, selectedCategory == "abc" || selectedCategory == "english", pinkColor, whiteColor, darkTextColor, strokeColor, strokeWidthPx)
            updateCategoryCard(categoryThaipbsKids, txtCategoryThaipbsKids, isThaipbsKidsSelected, pinkColor, whiteColor, darkTextColor, strokeColor, strokeWidthPx)
            updateCategoryCard(categoryThaipbsMain, txtCategoryThaipbsMain, selectedCategory == "thaipbs_main" || selectedCategory == "thaipbs_youtube_main", pinkColor, whiteColor, darkTextColor, strokeColor, strokeWidthPx)
            updateCategoryCard(categoryPrograms, txtCategoryPrograms, isProgramsSelected, pinkColor, whiteColor, darkTextColor, strokeColor, strokeWidthPx)
            updateCategoryCard(categoryTalk, txtCategoryTalk, selectedCategory == "talk" || selectedCategory == "thaipbs_talk_about_kids", pinkColor, whiteColor, darkTextColor, strokeColor, strokeWidthPx)
            updateCategoryCard(categoryStory, txtCategoryStory, selectedCategory == "story" || selectedCategory == "story_feed", pinkColor, whiteColor, darkTextColor, strokeColor, strokeWidthPx)

            for ((id, pair) in dynamicCategoryViews) {
                updateCategoryCard(pair.first, pair.second, selectedCategory == id, pinkColor, whiteColor, darkTextColor, strokeColor, strokeWidthPx)
            }
        } catch (e: Exception) {
            Log.e("VideoListFragment", "Error updating category UI", e)
        }
    }

    private fun showSettingsDialog() {
        val context = activityContext ?: return
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_video_settings, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val txtTitle = dialogView.findViewById<TextView>(R.id.txt_dialog_api_title)
        val switchApi = dialogView.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_api_source)
        val btnClose = dialogView.findViewById<Button>(R.id.btn_dialog_close)

        val currentIsSupabase = viewModel.isSupabaseApi.value ?: sharedPref.isUseSupabaseApi
        switchApi.isChecked = currentIsSupabase
        txtTitle.text = if (currentIsSupabase) "⚡ Supabase API" else "🌐 Legacy API"

        switchApi.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setApiSource(isChecked)
            txtTitle.text = if (isChecked) "⚡ Supabase API" else "🌐 Legacy API"
            val msg = if (isChecked) "⚡ สลับไปใช้ Supabase API" else "🌐 สลับไปใช้ Legacy API"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun performSearch(query: String) {
        viewModel.filterVideos(query, viewModel.currentCategory)
    }

    private fun displaySearchResults(videos: List<Video>) {
        adapterVideo?.updateData(videos)
        Log.d("Search", "Updating ${videos.size} search results using DiffUtil")
    }

    private fun showNoSearchResults(query: String) {
        val message =
                if (query.isNotEmpty()) {
                    "ไม่พบวิดีโอสำหรับ \"$query\""
                } else {
                    "ไม่พบวิดีโอในหมวดหมู่นี้"
                }

        if (::txtNoSearchMessage.isInitialized) {
            txtNoSearchMessage.text = message
        }
        if (::txtSearchSuggest.isInitialized) {
            txtSearchSuggest.text =
                    if (query.isNotEmpty()) {
                        "ลองค้นหาด้วยคำอื่นดูสิ!"
                    } else {
                        "ลองเลือกหมวดหมู่อื่นดูสิ!"
                    }
        }

        if (::lytNoSearchResults.isInitialized) {
            lytNoSearchResults.visibility = View.VISIBLE
        }
        recyclerView.visibility = View.GONE
        lytNoItem.visibility = View.GONE
        lytFailed.visibility = View.GONE
    }

    private fun hideNoSearchResults() {
        if (::lytNoSearchResults.isInitialized) {
            lytNoSearchResults.visibility = View.GONE
        }
        recyclerView.visibility = View.VISIBLE
    }

    private fun showSearchResults(show: Boolean) {
        if (show) {
            recyclerView.visibility = View.VISIBLE
            lytNoItem.visibility = View.GONE
            lytFailed.visibility = View.GONE
        }
    }

    private fun clearSearch() {
        searchInput.text.clear()
        searchQuery = ""
        hideKeyboard()
        selectCategory("all")
        hideNoSearchResults()
        if (sharedPref.isUseSupabaseApi) {
            requestAction(1)
        } else {
            ensureCookieChallengeSolved {
                requestAction(1)
            }
        }
    }

    private fun refreshAll() {
        searchInput.text.clear()
        searchQuery = ""
        hideKeyboard()
        selectCategory("all")
        hideNoSearchResults()
        swipeProgress(true)
        if (sharedPref.isUseSupabaseApi) {
            requestAction(1, true)
        } else {
            ensureCookieChallengeSolved {
                requestAction(1, true)
            }
        }
        loadInterstitialAd()
        Toast.makeText(activityContext, "กำลังรีเฟรชข้อมูล...", Toast.LENGTH_SHORT).show()
    }

    private fun showSortMenu() {
        val sortBtn = rootView?.findViewById<View>(R.id.btn_sort_new) ?: return
        val popupMenu = PopupMenu(activityContext, sortBtn)
        popupMenu.menuInflater.inflate(R.menu.menu_video_sort, popupMenu.menu)

        // ติ๊กเลือกเมนูปัจจุบัน
        when (sharedPref.currentSortHome) {
            0 -> popupMenu.menu.findItem(R.id.menu_sort_popular)?.isChecked = true
            1 -> popupMenu.menu.findItem(R.id.menu_sort_oldest)?.isChecked = true
            2 -> popupMenu.menu.findItem(R.id.menu_sort_newest)?.isChecked = true
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_sort_popular -> {
                    refreshVideos()
                    true
                }
                R.id.menu_sort_newest -> {
                    refreshVideos()
                    true
                }
                R.id.menu_sort_oldest -> {
                    refreshVideos()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun hideKeyboard() {
        val inputMethodManager =
                activityContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as
                        InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }

    private fun showKeyboard() {
        val inputMethodManager =
                activityContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as
                        InputMethodManager
        searchInput.requestFocus()
        inputMethodManager.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
    }

    // ==================== AdMob Initialization for Families ====================

    private fun initializeAds() {
        try {
            activityContext?.let { context ->
                if (!isAdMobInitialized) {
                    // ตั้งค่า RequestConfiguration สำหรับแอปครอบครัว
                    setupFamiliesRequestConfiguration()

                    Log.d("FamiliesAds", "🔧 Initializing Mobile Ads SDK for Families app...")
                    Log.d("FamiliesAds", "Configuration:")
                    Log.d("FamiliesAds", "  - Is Families App: ${adsPref.isFamiliesApp}")
                    Log.d("FamiliesAds", "  - Content Rating: ${adsPref.adContentRating}")
                    Log.d("FamiliesAds", "  - Test Mode: ${adsPref.isTestMode}")

                    MobileAds.initialize(context) { initializationStatus ->
                        Log.d("FamiliesAds", "✅ Mobile Ads SDK initialized successfully")

                        // บันทึกสถานะการ initialize
                        isAdMobInitialized = true

                        // ตรวจสอบ adapter status
                        logInitializationStatus(initializationStatus)

                        // ตรวจสอบ compliance สำหรับแอปครอบครัว
                        if (adsPref.isFamiliesApp) {
                            checkFamiliesCompliance()
                        }

                        // โหลด Interstitial Ad หลังจาก SDK พร้อม
                        Handler(Looper.getMainLooper())
                                .postDelayed(
                                        {
                                            if (isAdded && !isDetached) {
                                                loadInterstitialAd()
                                            }
                                        },
                                        1000
                                ) // รอ 1 วินาทีให้ SDK พร้อม
                    }
                } else {
                    Log.d("FamiliesAds", "🔄 Mobile Ads SDK already initialized")

                    // ถ้า initialize แล้ว ให้โหลดโฆษณา
                    if (isAdded && !isDetached) {
                        loadInterstitialAd()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FamiliesAds", "❌ Error initializing AdMob: ${e.message}")
            e.printStackTrace()
        }
    }

    /** ตั้งค่า RequestConfiguration สำหรับแอปครอบครัว */
    private fun setupFamiliesRequestConfiguration() {
        try {
            val builder = RequestConfiguration.Builder()

            if (adsPref.isFamiliesApp) {
                // สำหรับแอปครอบครัว (Designed for Families)
                Log.d("FamiliesConfig", "Applying Families-specific configuration")

                // 1. ตั้งค่าสำหรับเด็กอายุต่ำกว่า 13 ปี
                builder.setTagForUnderAgeOfConsent(
                        RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
                )

                // 2. ตั้งค่า child directed treatment
                builder.setTagForChildDirectedTreatment(
                        RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
                )

                // 3. ตั้งค่าระดับเนื้อหาสูงสุดเป็น G (สำหรับทุกวัย)
                builder.setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)

                Log.d("FamiliesConfig", "✅ Families configuration applied")
            }

            // เพิ่ม test devices สำหรับ debugging
            if (adsPref.isTestMode) {
                builder.setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                Log.d("FamiliesConfig", "Test mode enabled")
            }

            // ตั้งค่า RequestConfiguration
            MobileAds.setRequestConfiguration(builder.build())

            Log.d("FamiliesConfig", "RequestConfiguration set successfully")
        } catch (e: Exception) {
            Log.e("FamiliesConfig", "❌ Error setting up RequestConfiguration: ${e.message}")
        }
    }

    /** บันทึก initialization status สำหรับ debugging */
    private fun logInitializationStatus(initializationStatus: InitializationStatus) {
        Log.d("FamiliesAds", "=== Adapter Initialization Status ===")

        initializationStatus.adapterStatusMap.forEach { (adapterClass, status) ->
            Log.d("FamiliesAds", "$adapterClass: ${status.initializationState}")
        }

        Log.d("FamiliesAds", "======================================")
    }

    /** ตรวจสอบความสอดคล้องกับ Families Policy */
    private fun checkFamiliesCompliance() {
        if (!adsPref.isFamiliesApp) return

        Log.d("FamiliesCompliance", "=== Families Policy Compliance Check ===")

        val issues = mutableListOf<String>()

        // 1. ตรวจสอบ content rating
        if (adsPref.adContentRating != "G") {
            issues.add("Ad content rating should be G (currently: ${adsPref.adContentRating})")
            adsPref.adContentRating = "G"
            Log.d("FamiliesCompliance", "✅ Fixed: Set ad content rating to G")
        }

        // 2. ตรวจสอบประเภทโฆษณา (ปิด Native Ad สำหรับแอปครอบครัว)
        //        if (adsPref.isNativeAdPostList) {
        //            issues.add("Native ads should be disabled for Families app")
        //            adsPref.isNativeAdPostList = false
        //            Log.d("FamiliesCompliance", "✅ Fixed: Disabled native ads")
        //        }

        // 3. ตรวจสอบ Ad Unit ID สำหรับ Interstitial
        val interstitialAdUnitId = getInterstitialAdUnitId()
        if (!isValidFamiliesAdUnit(interstitialAdUnitId)) {
            issues.add("Interstitial ad unit may not be approved for Families program")
            Log.w("FamiliesCompliance", "Check interstitial ad unit in AdMob console")
        }

        // แสดงผลการตรวจสอบ
        if (issues.isNotEmpty()) {
            Log.w("FamiliesCompliance", "⚠️ Found ${issues.size} compliance issues:")
            issues.forEachIndexed { index, issue ->
                Log.w("FamiliesCompliance", "${index + 1}. $issue")
            }
        } else {
            isFamiliesCompliant = true
            Log.d("FamiliesCompliance", "✅ All Families policy requirements are met")
        }

        Log.d("FamiliesCompliance", "=========================================")
    }

    /** ตรวจสอบว่า Ad Unit ID ถูกต้องสำหรับ Families */
    private fun isValidFamiliesAdUnit(adUnitId: String): Boolean {
        if (adUnitId.isEmpty()) {
            return false
        }

        // Ad Unit ID ต้องเริ่มต้นด้วย ca-app-pub-
        val isValidFormat = adUnitId.startsWith("ca-app-pub-")

        // สำหรับโหมดทดสอบ อนุญาตให้ใช้ test ID ได้
        if (adsPref.isTestMode) {
            return true
        }

        return isValidFormat
    }

    // ==================== Interstitial Ad Functions for Families ====================

    private fun shouldShowInterstitialAd(): Boolean {
        // ตรวจสอบว่าเปิดใช้งาน Interstitial Ad หรือไม่
        if (!adsPref.isInterstitialAdEnabled || !adsPref.isInterstitialPostList) {
            Log.d("InterstitialAd", "⚠️ Interstitial ads are disabled in settings")
            return false
        }

        // สำหรับแอปครอบครัว: ตรวจสอบ compliance
        if (adsPref.isFamiliesApp && !isFamiliesCompliant) {
            Log.d("InterstitialAd", "⚠️ App not Families-compliant, skipping interstitial")
            return false
        }

        // เพิ่มจำนวนครั้งที่คลิกวิดีโอ
        videoClickCount++

        Log.d(
                "InterstitialAd",
                "📊 Video click count: $videoClickCount / $INTERSTITIAL_AD_FREQUENCY"
        )

        // ตรวจสอบว่าครบรอบที่ต้องแสดงโฆษณาหรือยัง
        val shouldShow = videoClickCount >= INTERSTITIAL_AD_FREQUENCY

        // รีเซ็ต counter ถ้าควรแสดงโฆษณา
        if (shouldShow) {
            videoClickCount = 0
            Log.d("InterstitialAd", "🔄 Reset video click counter")
        }

        // ตรวจสอบว่าโฆษณาพร้อมแสดง
        val isAdReady = interstitialAd != null

        Log.d("InterstitialAd", "📦 Interstitial ad ready: $isAdReady")

        return shouldShow && isAdReady
    }

    private fun showInterstitialAd(onAdDismissed: () -> Unit) {
        activityContext?.let { context ->
            if (interstitialAd != null) {
                Log.d("InterstitialAd", "🚀 Preparing to show interstitial ad")

                interstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d("InterstitialAd", "✅ Ad dismissed")
                                interstitialAd = null

                                // สำหรับแอปครอบครัว: บันทึกเหตุการณ์
                                if (adsPref.isFamiliesApp) {
                                    recordAdEvent("interstitial_dismissed", "families_safe")
                                }

                                // โหลดโฆษณาใหม่สำหรับครั้งถัดไป
                                Handler(Looper.getMainLooper())
                                        .postDelayed({ loadInterstitialAd() }, 1000)

                                // เรียก callback
                                onAdDismissed()
                            }

                            override fun onAdFailedToShowFullScreenContent(
                                    adError: com.google.android.gms.ads.AdError
                            ) {
                                Log.e("InterstitialAd", "❌ Ad failed to show: ${adError.message}")
                                interstitialAd = null

                                // สำหรับแอปครอบครัว: บันทึกเหตุการณ์
                                if (adsPref.isFamiliesApp) {
                                    recordAdEvent("interstitial_failed", "code_${adError.code}")
                                }

                                // โหลดโฆษณาใหม่
                                Handler(Looper.getMainLooper())
                                        .postDelayed({ loadInterstitialAd() }, 3000)

                                // เรียก callback เพื่อดำเนินการต่อ
                                onAdDismissed()
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d("InterstitialAd", "👀 Ad showed full screen content")

                                // สำหรับแอปครอบครัว: บันทึกเหตุการณ์
                                if (adsPref.isFamiliesApp) {
                                    recordAdEvent("interstitial_shown", "families")
                                }
                            }

                            override fun onAdClicked() {
                                Log.d("InterstitialAd", "👆 Ad clicked")

                                // สำหรับแอปครอบครัว: บันทึกการโต้ตอบ
                                if (adsPref.isFamiliesApp) {
                                    recordFamilySafeInteraction()
                                }
                            }

                            override fun onAdImpression() {
                                Log.d("InterstitialAd", "📊 Ad impression recorded")
                            }
                        }

                try {
                    interstitialAd?.show(context)
                    Log.d("InterstitialAd", "🎬 Showing interstitial ad")
                } catch (e: Exception) {
                    Log.e("InterstitialAd", "💥 Error showing ad: ${e.message}")
                    e.printStackTrace()
                    onAdDismissed()
                }
            } else {
                Log.d("InterstitialAd", "⚠️ Interstitial ad not ready, continuing without ad")
                onAdDismissed()

                // พยายามโหลดโฆษณาใหม่
                loadInterstitialAd()
            }
        }
    }

    private fun loadInterstitialAd() {
        // ตรวจสอบว่าควรโหลดโฆษณาหรือไม่
        if (!adsPref.isInterstitialAdEnabled || !adsPref.isInterstitialPostList) {
            Log.d("InterstitialAd", "⚠️ Interstitial ads are disabled in settings")
            return
        }

        // สำหรับแอปครอบครัว: ตรวจสอบ compliance
        if (adsPref.isFamiliesApp && !isFamiliesCompliant) {
            Log.d("InterstitialAd", "⚠️ App not Families-compliant, skipping interstitial")
            return
        }

        if (isInterstitialLoading || interstitialLoadAttempt >= MAX_INTERSTITIAL_LOAD_ATTEMPTS) {
            Log.d(
                    "InterstitialAd",
                    "⚠️ Skipping interstitial load - already loading or max attempts reached"
            )
            return
        }

        activityContext?.let { context ->
            isInterstitialLoading = true
            interstitialLoadAttempt++

            Log.d(
                    "InterstitialAd",
                    "🔄 Loading interstitial ad (attempt $interstitialLoadAttempt/$MAX_INTERSTITIAL_LOAD_ATTEMPTS)"
            )

            val adRequestBuilder = AdRequest.Builder()

            // สำหรับแอปครอบครัว: ใช้ RequestConfiguration ที่ตั้งค่าไว้แล้ว
            // ไม่ต้องเพิ่ม tagForChildDirectedTreatment ใน AdRequest อีก

            // เพิ่ม test device IDs สำหรับ Debug
            if (adsPref.isTestMode) {
                // adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                Log.d("InterstitialAd", "Test device added")
            }

            val adRequest = adRequestBuilder.build()

            val adUnitId = getInterstitialAdUnitId()
            Log.d("InterstitialAd", "📝 Using ad unit ID: $adUnitId")
            
            // แจ้งเตือน ID ที่ใช้ (เฉพาะโหมด Debug)
            if (AppConfig.DEBUG) {
                activityContext?.let {
                    Toast.makeText(it, "Loading Ad: $adUnitId", Toast.LENGTH_SHORT).show()
                }
            }

            InterstitialAd.load(
                    context,
                    adUnitId,
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            Log.d("InterstitialAd", "✅ Interstitial ad loaded successfully")

                            // สำหรับแอปครอบครัว: บันทึกเหตุการณ์
                            if (adsPref.isFamiliesApp) {
                                recordAdEvent("interstitial_loaded", "families_safe")
                            }

                            interstitialAd = ad
                            isInterstitialLoading = false
                            interstitialLoadAttempt = 0

                            // แสดง debug info
                            Log.d("InterstitialAd", "📊 Interstitial ad is now ready for display")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.e(
                                    "InterstitialAd",
                                    "❌ Interstitial ad failed to load: ${loadAdError.message}"
                            )
                            Log.e("InterstitialAd", "Error code: ${loadAdError.code}")
                            Log.e("InterstitialAd", "Error domain: ${loadAdError.domain}")
                            
                            // Toast เพื่อช่วย Debug (ลบออกได้ภายหลัง)
                            if (adsPref.isTestMode || AppConfig.DEBUG) {
                                activityContext?.let {
                                    Toast.makeText(it, "Ad Error: ${loadAdError.code} - ${loadAdError.message}", Toast.LENGTH_LONG).show()
                                }
                            }

                            interstitialAd = null
                            isInterstitialLoading = false

                            // สำหรับแอปครอบครัว: error code 3 (NO_FILL) เป็นปกติ
                            if (loadAdError.code == 3 && adsPref.isFamiliesApp) {
                                Log.d(
                                        "InterstitialAd",
                                        "No interstitial inventory for Families app - this is normal"
                                )
                            }

                            // ลองใหม่หลังจาก delay
                            if (interstitialLoadAttempt < MAX_INTERSTITIAL_LOAD_ATTEMPTS) {
                                val retryDelay =
                                        interstitialLoadAttempt * 3000L // เพิ่ม delay ตามจำนวนครั้ง
                                Log.d(
                                        "InterstitialAd",
                                        "⏳ Retrying in ${retryDelay/1000} seconds..."
                                )

                                Handler(Looper.getMainLooper())
                                        .postDelayed({ loadInterstitialAd() }, retryDelay)
                            } else {
                                Log.e(
                                        "InterstitialAd",
                                        "💥 Max load attempts reached for interstitial ad"
                                )
                            }
                        }
                    }
            )
        }
    }

    /** ดึง Interstitial Ad Unit ID */
    private fun getInterstitialAdUnitId(): String {
        return AppConfig.getInterstitialAdUnitId()
    }

    // ==================== Helper Functions for Families ====================

    /** บันทึกเหตุการณ์โฆษณา */
    private fun recordAdEvent(eventName: String, eventValue: String) {
        val timestamp = System.currentTimeMillis()
        val logMessage = "[$timestamp] FamiliesAdEvent: $eventName = $eventValue"

        if (adsPref.isTestMode) {
            Log.d("AdEvents", logMessage)
        }
    }

    /** บันทึกการโต้ตอบที่ปลอดภัยสำหรับครอบครัว */
    private fun recordFamilySafeInteraction() {
        Log.d("FamiliesSafety", "📝 Recording family-safe ad interaction")
        recordAdEvent("family_interaction", "safe_click")
    }

    // ==================== Video Opening Logic ====================

    private fun openVideoActivity(video: Video?) {
        if (video == null) {
            showToast("ข้อมูลวิดีโอไม่พร้อม")
            return
        }

        // บันทึกวิดีโอปัจจุบัน
        currentVideo = video

        Log.d("VideoOpen", "🎬 Opening video: ${video.video_title}")

        // ตรวจสอบและแสดงโฆษณา
        if (shouldShowInterstitialAd()) {
            Log.d("VideoOpen", "📢 Showing interstitial ad before video")
            showInterstitialAd { openVideoDirectly(video) }
        } else {
            Log.d("VideoOpen", "⏭️ Skipping interstitial ad, opening video directly")
            openVideoDirectly(video)
        }
    }

    private fun isFeedVideo(video: Video): Boolean {
        val vid = video.vid.orEmpty()
        val tags = video.tags.orEmpty()
        val url = video.video_url.orEmpty()
        val cat = video.category_name.orEmpty()
        return vid.startsWith("FEED_") ||
                vid.startsWith("TPBS_") ||
                tags.contains("#feed", ignoreCase = true) ||
                tags.contains("thaipbs", ignoreCase = true) ||
                tags.contains("ฝึกอ่าน", ignoreCase = true) ||
                cat.contains("ฝึกอ่าน", ignoreCase = true) ||
                cat.contains("Thai PBS", ignoreCase = true) ||
                cat.contains("วิดีโอรวม", ignoreCase = true) ||
                cat.contains("คุยกับลูก", ignoreCase = true) ||
                url.contains("news.google", ignoreCase = true) ||
                url.contains(".xml", ignoreCase = true) ||
                url.contains("rss", ignoreCase = true)
    }

    private fun openVideoDirectly(video: Video) {
        try {
            val intent = if (isFeedVideo(video)) {
                Log.d("VideoOpen", "📖 Opening dedicated RSS/XML Screen (RssFeedDetailActivity) for: ${video.video_title}")
                Intent(activityContext, com.pongploydev.videowatchfeaturesupabase.activity.RssFeedDetailActivity::class.java).apply {
                    putExtra(Constant.KEY_VID, video.vid)
                    putExtra(Constant.EXTRA_OBJC, video)
                }
            } else {
                createVideoIntent(video.video_type).apply {
                    putExtra(Constant.KEY_VID, video.vid)
                    putExtra(Constant.EXTRA_OBJC, video)
                }
            }
            startActivity(intent)
            Log.d("VideoOpen", "✅ Video activity started successfully")
        } catch (e: Exception) {
            Log.e("VideoOpen", "💥 Error opening video: ${e.message}")
            e.printStackTrace()
            showToast("ไม่สามารถเปิดวิดีโอได้")
        }
    }

    private fun showYouTubeInlinePreview(video: Video, holder: AdapterVideo.OriginalViewHolder) {
        val context = activityContext ?: return
        
        // Stop and clean up previous preview if any
        stopActivePreview()

        activeViewHolder = holder
        
        // Create or reuse YouTubePlayerView
        val playerView = YouTubePlayerView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        activeYouTubePlayerView = playerView
        
        // Hide thumbnail and show player container
        holder.lytYouTubeContainer.visibility = View.VISIBLE
        holder.imgPlay.visibility = View.GONE
        holder.lytYouTubeContainer.removeAllViews()
        holder.lytYouTubeContainer.addView(playerView)

        // Add Mute/Unmute Button
        val muteButton = ImageButton(context).apply {
            val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, resources.displayMetrics).toInt()
            layoutParams = FrameLayout.LayoutParams(size, size).apply {
                gravity = Gravity.TOP or Gravity.END
                val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, resources.displayMetrics).toInt()
                setMargins(margin, margin, margin, margin)
            }
            // Use a semi-transparent dark circle background
            val shape = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(android.graphics.Color.parseColor("#80000000")) // 50% transparent black
            }
            background = shape
            setPadding(12, 12, 12, 12)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            
            // Set icon color to white for contrast on dark background
            setImageResource(if (isPreviewMuted) R.drawable.ic_volume_off else R.drawable.ic_volume_up)
            setColorFilter(android.graphics.Color.WHITE)
            
            setOnClickListener {
                isPreviewMuted = !isPreviewMuted
                android.util.Log.d("YouTubePreview", "Mute toggled: $isPreviewMuted")
                if (isPreviewMuted) {
                    activeYouTubePlayer?.mute()
                    setImageResource(R.drawable.ic_volume_off)
                } else {
                    activeYouTubePlayer?.unMute()
                    setImageResource(R.drawable.ic_volume_up)
                }
            }
        }
        holder.lytYouTubeContainer.addView(muteButton)
        
        lifecycle.addObserver(playerView)
        
        playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                activeYouTubePlayer = youTubePlayer
                youTubePlayer.loadVideo(video.video_id, 0f)
                // Use a small delay to ensure player is ready for sound commands
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (isPreviewMuted) {
                        youTubePlayer.mute()
                    } else {
                        youTubePlayer.unMute()
                    }
                }, 600)
            }
        })
    }

    private fun stopActivePreview() {
        if (activeYouTubePlayerView == null && activeViewHolder == null) return

        activeYouTubePlayerView?.let { playerView ->
            playerView.release()
            lifecycle.removeObserver(playerView)
        }
        
        activeViewHolder?.let { holder ->
            holder.lytYouTubeContainer.removeAllViews()
            holder.lytYouTubeContainer.visibility = View.GONE
            holder.imgPlay.visibility = View.VISIBLE
        }
        
        activeYouTubePlayerView = null
        activeViewHolder = null
        activeYouTubePlayer = null
    }

    private fun showYouTubePreview(video: Video) {
        // Obsolete, can be removed or kept if needed. For now let's keep it to avoid compile errors if called elsewhere.
    }

    private fun createVideoIntent(videoType: String?): Intent {
        return when (videoType?.lowercase()) {
            "youtube" -> Intent(activityContext, ActivityVideoAutoDetail::class.java)
            else -> Intent(activityContext, ActivityVideoDetail::class.java)
        }
    }

    // ==================== Existing Functions ====================

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

    private fun showFailedView(show: Boolean, message: String?) {
        lytFailed.isVisible = show
        recyclerView.isVisible = !show
        if (::lytNoSearchResults.isInitialized) {
            lytNoSearchResults.isVisible = false
        }
        txtFailedMessage.text = message ?: getString(R.string.failed_text)
    }

    private fun showNoItemView(show: Boolean) {
        lytNoItem.isVisible = show
        recyclerView.isVisible = !show
        if (::lytNoSearchResults.isInitialized) {
            lytNoSearchResults.isVisible = false
        }
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

    private fun showToast(message: String) {
        Toast.makeText(activityContext, message, Toast.LENGTH_SHORT).show()
    }

    // ==================== Lifecycle Methods ====================

    override fun onResume() {
        super.onResume()

        // สำหรับแอปครอบครัว: ตรวจสอบ compliance ใหม่
        if (adsPref.isFamiliesApp && isAdMobInitialized) {
            Handler(Looper.getMainLooper()).postDelayed({ checkFamiliesCompliance() }, 1000)
        }
    }

    override fun onPause() {
        super.onPause()
        stopActivePreview()
        // ซ่อน keyboard
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        swipeProgress(false)
        shimmerFrameLayout.stopShimmer()

        // Clean up handlers
        searchHandler?.removeCallbacksAndMessages(null)
    }

    override fun onDetach() {
        super.onDetach()
        activityContext = null
    }

    private fun ensureCookieChallengeSolved(onComplete: () -> Unit) {
        val baseUrl = sharedPref.baseUrl
        if (baseUrl.contains("onrender.com")) {
            // Render doesn't need cookie challenge
            onComplete()
            return
        }
        val cookie = android.webkit.CookieManager.getInstance().getCookie(baseUrl)
        if (cookie != null && cookie.contains("__test")) {
            android.util.Log.d("CookieChallenge", "Cookie already exists, proceeding directly")
            onComplete()
        } else {
            solveCookieChallenge(onComplete)
        }
    }

    private fun solveCookieChallenge(onComplete: () -> Unit) {
        val context = activityContext ?: return
        val baseUrl = sharedPref.baseUrl
        // Load the base URL instead of API to solve challenge faster
        val challengeUrl = baseUrl
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            val webView = android.webkit.WebView(context)
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            // Use same User-Agent as Retrofit
            webView.settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            
            webView.webViewClient = object : android.webkit.WebViewClient() {
                override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Check every 500ms if cookie is set, up to 5 seconds
                    var checkCount = 0
                    val checkRunnable = object : Runnable {
                        override fun run() {
                            val cookie = android.webkit.CookieManager.getInstance().getCookie(url)
                            if (cookie != null && cookie.contains("__test")) {
                                android.util.Log.d("CookieChallenge", "Successfully obtained __test cookie!")
                                onComplete()
                                try { webView.destroy() } catch (e: Exception) {}
                            } else if (checkCount < 10) {
                                checkCount++
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this, 500)
                            } else {
                                android.util.Log.e("CookieChallenge", "Timed out waiting for cookie")
                                onComplete() // Proceed anyway
                                try { webView.destroy() } catch (e: Exception) {}
                            }
                        }
                    }
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(checkRunnable, 500)
                }

                override fun onReceivedError(
                    view: android.webkit.WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    android.util.Log.e("CookieChallenge", "WebView error: $description")
                    onComplete() // Proceed anyway to avoid being stuck
                }
            }
            android.util.Log.d("CookieChallenge", "Starting WebView to load $challengeUrl to solve challenge")
            webView.loadUrl(challengeUrl)
        }
    }

    companion object {
        const val ARG_ID_CAT = "id_cat"

        // ค่าคงที่สำหรับการซ่อน/แสดง ActionBar
        private const val HIDE_THRESHOLD = 60

        fun newInstance(): VideoListFragment {
            return VideoListFragment().apply {
                arguments =
                        Bundle().apply {
                            // Add arguments if needed
                        }
            }
        }
    }
}
