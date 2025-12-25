package com.pongploydev.videowatchfeature

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pongploydev.videowatchfeature.activity.ActivityVideoAutoDetail
import com.pongploydev.videowatchfeature.activity.ActivityVideoDetail
import com.pongploydev.videowatchfeature.adapter.AdapterVideo
import com.pongploydev.videowatchfeature.callbacks.CallbackCategoryDetails
import com.pongploydev.videowatchfeature.config.AppConfig
import com.pongploydev.videowatchfeature.databases.prefs.AdsPref
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.rests.RestAdapter
import com.pongploydev.videowatchfeature.utils.EqualSpacingItemDecoration
import com.pongploydev.videowatchfeature.utils.Tools
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private lateinit var searchContainer: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var clearSearchBtn: ImageView
    private lateinit var sortButton: ImageView
    private lateinit var refreshButton: ImageView
    private lateinit var categoryContainer: LinearLayout
    private lateinit var categoryAll: TextView
    private lateinit var categoryCartoon: TextView
    private lateinit var categoryMusic: TextView
    private lateinit var categoryLearning: TextView
    private lateinit var categoryStory: TextView

    // ตัวแปรสำหรับ Interstitial Ad
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var interstitialLoadAttempt = 0
    private val MAX_INTERSTITIAL_LOAD_ATTEMPTS = 5

    private var adapterVideo: AdapterVideo? = null
    private var callbackCall: Call<CallbackCategoryDetails?>? = null

    private var postTotal = 0
    private var failedPage = 0

    private lateinit var sharedPref: SharedPref
    private lateinit var adsPref: AdsPref
    private lateinit var tools: Tools
    private var activityContext: Activity? = null

    // ตัวแปรสำหรับจัดการ ActionBar
    private var isActionBarHidden = false

    // ตัวแปรสำหรับนับรอบการแสดง Interstitial Ad
    private var videoClickCount = 0
    private val INTERSTITIAL_AD_FREQUENCY = 3 // แสดงโฆษณาทุก 3 ครั้ง

    // ตัวแปรเก็บวิดีโอปัจจุบัน
    private var currentVideo: Video? = null

    // ตัวแปรสำหรับเช็คการ initialize AdMob
    private var isAdMobInitialized = false

    // ตัวแปรสำหรับการค้นหา
    private var searchQuery = ""
    private var searchHandler: Handler? = null
    private val SEARCH_DELAY = 500L // ดีเลย์ 500ms
    private var currentCategory = "all" // หมวดหมู่ปัจจุบัน
    private var isSearching = false
    private var originalVideos = mutableListOf<Video>() // เก็บข้อมูลเดิมไว้กรอง

    // ตัวแปรสำหรับแอปครอบครัว
    private var isFamiliesCompliant = false

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
        setupSearch()
        setupCategories()

        adsPref.isTestMode = false
        adsPref.isFamiliesApp = true

        // ซ่อน ActionBar เมื่อ Fragment ถูกสร้าง
        hideActionBar()

        // ตรวจสอบและโหลดโฆษณา
        initializeAds()

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
            lytNoSearchResults = findViewById(R.id.lyt_no_search_results)
            txtNoItemMessage = findViewById(R.id.no_item_message)
            txtFailedMessage = findViewById(R.id.failed_message)
            txtNoSearchMessage = findViewById(R.id.txt_no_search_message)
            txtSearchSuggest = findViewById(R.id.txt_search_suggest)
            btnRetry = findViewById(R.id.failed_retry)
            btnClearSearch = findViewById(R.id.btn_clear_search)

            // ประกาศตัวแปรสำหรับ Header และค้นหา
            searchContainer = findViewById(R.id.search_container)
            searchInput = findViewById(R.id.search_input)
            searchIcon = findViewById(R.id.search_icon)
            clearSearchBtn = findViewById(R.id.clear_search_btn)
            sortButton = findViewById(R.id.sort_button)
            refreshButton = findViewById(R.id.refresh_button)
            categoryContainer = findViewById(R.id.category_container)
            categoryAll = findViewById(R.id.category_all)
            categoryCartoon = findViewById(R.id.category_cartoon)
            categoryMusic = findViewById(R.id.category_music)
            categoryLearning = findViewById(R.id.category_learning)
            categoryStory = findViewById(R.id.category_story)
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

    /**
     * ตั้งค่าเริ่มต้นสำหรับแอปครอบครัว
     */
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
        // Adjust padding for compact view
        if (sharedPref.getVideoViewType() == Constant.VIDEO_LIST_COMPACT) {
            val spacing = resources.getDimensionPixelOffset(R.dimen.spacing_small)
            recyclerView.setPadding(0, spacing, 0, spacing)
        }

        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL).apply {
            gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            setItemPrefetchEnabled(true)
        }
        recyclerView.layoutManager = layoutManager

        // เพิ่มเพื่อลดการกระพริบ
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = null // ปิด animation ชั่วคราวเพื่อทดสอบ
        recyclerView.isDrawingCacheEnabled = true
        recyclerView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        // Initialize adapter
        adapterVideo = AdapterVideo(activityContext!!, recyclerView, mutableListOf()).apply {
            setOnItemClickListener { _, video, position ->
                Log.d("VideoClick", "Video clicked at position: $position")
                openVideoActivity(video)
            }

            setOnItemOverflowClickListener { _, video, position ->
                // Handle overflow menu click if needed
                Log.d("OverflowClick", "Overflow clicked for video: ${video?.id}")
            }

            setOnLoadMoreListener { currentPage ->
                if (!isSearching) {
                    setLoadMore(currentPage)
                } else {
                    adapterVideo?.setLoaded()
                }
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
            // รีโหลด Interstitial Ad
            loadInterstitialAd()
        }

        btnRetry.setOnClickListener {
            requestAction(1)
            loadInterstitialAd()
        }

        btnClearSearch.setOnClickListener {
            clearSearch()
        }

        refreshButton.setOnClickListener {
            refreshAll()
        }
    }

    private fun setupSearch() {
        searchHandler = Handler(Looper.getMainLooper())

        // ตั้งค่า TextWatcher สำหรับ real-time search
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // ไม่ต้องทำอะไร
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // แสดง/ซ่อนปุ่มล้าง
                clearSearchBtn.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE

                // ล้าง handler ก่อนหน้า
                searchHandler?.removeCallbacksAndMessages(null)

                // ดีเลย์การค้นหาเพื่อป้องกันการค้นหาบ่อยเกินไป
                searchHandler?.postDelayed({
                    val query = s.toString().trim()
                    if (query != searchQuery) {
                        searchQuery = query
                        performSearch(query)
                    }
                }, SEARCH_DELAY)
            }

            override fun afterTextChanged(s: Editable?) {
                // ไม่ต้องทำอะไร
            }
        })

        // ตั้งค่าปุ่มล้าง
        clearSearchBtn.setOnClickListener {
            clearSearch()
        }

        // ตั้งค่าปุ่มเรียงลำดับ
        sortButton.setOnClickListener {
            showSortMenu()
        }

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
        // ตั้งค่า OnClickListener สำหรับแต่ละหมวดหมู่
        categoryAll.setOnClickListener { selectCategory("all") }
        categoryCartoon.setOnClickListener { selectCategory("cartoon") }
        categoryMusic.setOnClickListener { selectCategory("music") }
        categoryLearning.setOnClickListener { selectCategory("learning") }
        categoryStory.setOnClickListener { selectCategory("story") }

        // เริ่มต้นเลือกหมวดหมู่ทั้งหมด
        selectCategory("all")
    }

    private fun selectCategory(category: String) {
        currentCategory = category

        // รีเซ็ตสไตล์ทั้งหมด
        val categories = listOf(categoryAll, categoryCartoon, categoryMusic, categoryLearning, categoryStory)
        categories.forEach {
            it.setBackgroundResource(R.drawable.bg_category_unselected)
        }

        // ตั้งค่าสไตล์สำหรับหมวดหมู่ที่เลือก
        when (category) {
            "all" -> categoryAll.setBackgroundResource(R.drawable.bg_category_selected)
            "cartoon" -> categoryCartoon.setBackgroundResource(R.drawable.bg_category_selected)
            "music" -> categoryMusic.setBackgroundResource(R.drawable.bg_category_selected)
            "learning" -> categoryLearning.setBackgroundResource(R.drawable.bg_category_selected)
            "story" -> categoryStory.setBackgroundResource(R.drawable.bg_category_selected)
        }

        // กรองวิดีโอตามหมวดหมู่
        filterVideosByCategory()
    }

    private fun filterVideosByCategory() {
        if (originalVideos.isEmpty()) return

        if (currentCategory == "all") {
            // แสดงทั้งหมด
            displaySearchResults(originalVideos)
        } else {
            // กรองตามหมวดหมู่
            val filteredVideos = originalVideos.filter { video ->
                video.category_name?.contains(currentCategory, ignoreCase = true) == true ||
                        video.video_title?.contains(currentCategory, ignoreCase = true) == true ||
                        video.video_description?.contains(currentCategory, ignoreCase = true) == true
            }
            displaySearchResults(filteredVideos.toMutableList())
        }
    }

    private fun performSearch(query: String) {
        Log.d("Search", "Searching for: '$query' in category: $currentCategory")

        if (query.isEmpty() && currentCategory == "all") {
            // แสดงวิดีโอทั้งหมด
            isSearching = false
            showSearchResults(false)
            requestAction(1)
        } else {
            // กำลังค้นหา
            isSearching = true
            showSearchResults(true)

            // กรองวิดีโอจากข้อมูลที่มี
            filterVideos(query)
        }
    }

    private fun filterVideos(query: String) {
        if (originalVideos.isEmpty()) {
            // ยังไม่มีข้อมูล ให้โหลดก่อน
            requestAction(1)
            return
        }

        swipeProgress(true)

        // กรองวิดีโอตามคำค้นหาและหมวดหมู่
        val filteredVideos = originalVideos.filter { video ->
            val matchesSearch = query.isEmpty() ||
                    video.video_title?.contains(query, ignoreCase = true) == true ||
                    video.video_description?.contains(query, ignoreCase = true) == true

            val matchesCategory = currentCategory == "all" ||
                    video.category_name?.contains(currentCategory, ignoreCase = true) == true

            matchesSearch && matchesCategory
        }

        // แสดงผลลัพธ์
        Handler(Looper.getMainLooper()).postDelayed({
            swipeProgress(false)
            displaySearchResults(filteredVideos.toMutableList())

            // แสดงข้อความถ้าไม่พบผลลัพธ์
            if (filteredVideos.isEmpty()) {
                showNoSearchResults(query)
            } else {
                hideNoSearchResults()
            }
        }, 300) // ดีเลย์เล็กน้อยเพื่อให้ดู smooth
    }

    private fun displaySearchResults(videos: MutableList<Video>) {
        adapterVideo?.resetListData()
        adapterVideo?.insertData(videos)

        // อัพเดทจำนวนทั้งหมด
        postTotal = videos.size

        Log.d("Search", "Displaying ${videos.size} search results")
    }

    private fun showNoSearchResults(query: String) {
        val message = if (query.isNotEmpty()) {
            "ไม่พบวิดีโอสำหรับ \"$query\""
        } else {
            "ไม่พบวิดีโอในหมวดหมู่นี้"
        }

        txtNoSearchMessage.text = message
        txtSearchSuggest.text = if (query.isNotEmpty()) {
            "ลองค้นหาด้วยคำอื่นดูสิ!"
        } else {
            "ลองเลือกหมวดหมู่อื่นดูสิ!"
        }

        lytNoSearchResults.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        lytNoItem.visibility = View.GONE
        lytFailed.visibility = View.GONE
    }

    private fun hideNoSearchResults() {
        lytNoSearchResults.visibility = View.GONE
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
        isSearching = false
        hideKeyboard()
        selectCategory("all")
        hideNoSearchResults()
        requestAction(1)
    }

    private fun refreshAll() {
        clearSearch()
        requestAction(1)
        loadInterstitialAd()
        Toast.makeText(activityContext, "กำลังรีเฟรชข้อมูล...", Toast.LENGTH_SHORT).show()
    }

    private fun showSortMenu() {
        val popupMenu = PopupMenu(activityContext, sortButton)
        popupMenu.menuInflater.inflate(R.menu.menu_video_sort, popupMenu.menu)

        // ติ๊กเลือกเมนูปัจจุบัน
        when (sharedPref.getCurrentSortHome()) {
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
        val inputMethodManager = activityContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }

    private fun showKeyboard() {
        val inputMethodManager = activityContext?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
                        Handler(Looper.getMainLooper()).postDelayed({
                            if (isAdded && !isDetached) {
                                loadInterstitialAd()
                            }
                        }, 1000) // รอ 1 วินาทีให้ SDK พร้อม
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

    /**
     * ตั้งค่า RequestConfiguration สำหรับแอปครอบครัว
     */
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
                builder.setMaxAdContentRating(
                    RequestConfiguration.MAX_AD_CONTENT_RATING_G
                )

                Log.d("FamiliesConfig", "✅ Families configuration applied")
            }

            // เพิ่ม test devices สำหรับ debugging
            if (adsPref.isTestMode) {
                builder.setTestDeviceIds(listOf(
                    AdRequest.DEVICE_ID_EMULATOR
                ))
                Log.d("FamiliesConfig", "Test mode enabled")
            }

            // ตั้งค่า RequestConfiguration
            MobileAds.setRequestConfiguration(builder.build())

            Log.d("FamiliesConfig", "RequestConfiguration set successfully")

        } catch (e: Exception) {
            Log.e("FamiliesConfig", "❌ Error setting up RequestConfiguration: ${e.message}")
        }
    }

    /**
     * บันทึก initialization status สำหรับ debugging
     */
    private fun logInitializationStatus(initializationStatus: InitializationStatus) {
        Log.d("FamiliesAds", "=== Adapter Initialization Status ===")

        initializationStatus.adapterStatusMap.forEach { (adapterClass, status) ->
            Log.d("FamiliesAds", "$adapterClass: ${status.initializationState}")
        }

        Log.d("FamiliesAds", "======================================")
    }

    /**
     * ตรวจสอบความสอดคล้องกับ Families Policy
     */
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

    /**
     * ตรวจสอบว่า Ad Unit ID ถูกต้องสำหรับ Families
     */
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

        Log.d("InterstitialAd", "📊 Video click count: $videoClickCount / $INTERSTITIAL_AD_FREQUENCY")

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

                interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        Log.d("InterstitialAd", "✅ Ad dismissed")
                        interstitialAd = null

                        // สำหรับแอปครอบครัว: บันทึกเหตุการณ์
                        if (adsPref.isFamiliesApp) {
                            recordAdEvent("interstitial_dismissed", "families_safe")
                        }

                        // โหลดโฆษณาใหม่สำหรับครั้งถัดไป
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadInterstitialAd()
                        }, 1000)

                        // เรียก callback
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        Log.e("InterstitialAd", "❌ Ad failed to show: ${adError.message}")
                        interstitialAd = null

                        // สำหรับแอปครอบครัว: บันทึกเหตุการณ์
                        if (adsPref.isFamiliesApp) {
                            recordAdEvent("interstitial_failed", "code_${adError.code}")
                        }

                        // โหลดโฆษณาใหม่
                        Handler(Looper.getMainLooper()).postDelayed({
                            loadInterstitialAd()
                        }, 3000)

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
            Log.d("InterstitialAd", "⚠️ Skipping interstitial load - already loading or max attempts reached")
            return
        }

        activityContext?.let { context ->
            isInterstitialLoading = true
            interstitialLoadAttempt++

            Log.d("InterstitialAd", "🔄 Loading interstitial ad (attempt $interstitialLoadAttempt/$MAX_INTERSTITIAL_LOAD_ATTEMPTS)")

            val adRequestBuilder = AdRequest.Builder()

            // สำหรับแอปครอบครัว: ใช้ RequestConfiguration ที่ตั้งค่าไว้แล้ว
            // ไม่ต้องเพิ่ม tagForChildDirectedTreatment ใน AdRequest อีก

            // เพิ่ม test device IDs สำหรับ Debug
            if (adsPref.isTestMode) {
                //adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                Log.d("InterstitialAd", "Test device added")
            }

            val adRequest = adRequestBuilder.build()

            val adUnitId = getInterstitialAdUnitId()
            Log.d("InterstitialAd", "📝 Using ad unit ID: $adUnitId")

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
                        Log.e("InterstitialAd", "❌ Interstitial ad failed to load: ${loadAdError.message}")
                        Log.e("InterstitialAd", "Error code: ${loadAdError.code}")
                        Log.e("InterstitialAd", "Error domain: ${loadAdError.domain}")

                        // สำหรับแอปครอบครัว: บันทึกเหตุการณ์
                        if (adsPref.isFamiliesApp) {
                            recordAdEvent("interstitial_load_failed", "code_${loadAdError.code}")
                        }

                        interstitialAd = null
                        isInterstitialLoading = false

                        // สำหรับแอปครอบครัว: error code 3 (NO_FILL) เป็นปกติ
                        if (loadAdError.code == 3 && adsPref.isFamiliesApp) {
                            Log.d("InterstitialAd", "No interstitial inventory for Families app - this is normal")
                        }

                        // ลองใหม่หลังจาก delay
                        if (interstitialLoadAttempt < MAX_INTERSTITIAL_LOAD_ATTEMPTS) {
                            val retryDelay = interstitialLoadAttempt * 3000L // เพิ่ม delay ตามจำนวนครั้ง
                            Log.d("InterstitialAd", "⏳ Retrying in ${retryDelay/1000} seconds...")

                            Handler(Looper.getMainLooper()).postDelayed({
                                loadInterstitialAd()
                            }, retryDelay)
                        } else {
                            Log.e("InterstitialAd", "💥 Max load attempts reached for interstitial ad")
                        }
                    }
                }
            )
        }
    }

    /**
     * ดึง Interstitial Ad Unit ID
     */
    private fun getInterstitialAdUnitId(): String {
        return if (adsPref.isTestMode) {
            // Test ID สำหรับ Debug
            "ca-app-pub-3940256099942544/1033173712" // Test interstitial ID
        } else {
            // Real Ad Unit ID สำหรับ Production
            // ควรใช้จาก AdsPref หรือ config
            "ca-app-pub-3279793802315716/8581063137"
        }
    }

    // ==================== Helper Functions for Families ====================

    /**
     * บันทึกเหตุการณ์โฆษณา
     */
    private fun recordAdEvent(eventName: String, eventValue: String) {
        val timestamp = System.currentTimeMillis()
        val logMessage = "[$timestamp] FamiliesAdEvent: $eventName = $eventValue"

        if (adsPref.isTestMode) {
            Log.d("AdEvents", logMessage)
        }
    }

    /**
     * บันทึกการโต้ตอบที่ปลอดภัยสำหรับครอบครัว
     */
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
            showInterstitialAd {
                openVideoDirectly(video)
            }
        } else {
            Log.d("VideoOpen", "⏭️ Skipping interstitial ad, opening video directly")
            openVideoDirectly(video)
        }
    }

    private fun openVideoDirectly(video: Video) {
        try {
            val intent = createVideoIntent(video.video_type).apply {
                putExtra(Constant.EXTRA_OBJC, video)
            }
            startActivity(intent)
            Log.d("VideoOpen", "✅ Video activity started successfully")
        } catch (e: Exception) {
            Log.e("VideoOpen", "💥 Error opening video: ${e.message}")
            e.printStackTrace()
            showToast("ไม่สามารถเปิดวิดีโอได้")
        }
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
        // บันทึกข้อมูลดั้งเดิม
        originalVideos.clear()
        originalVideos.addAll(videos.filterNotNull())

        // แสดงผล
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
                call: Call<CallbackCategoryDetails?>,
                response: Response<CallbackCategoryDetails?>
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
        hideNoSearchResults()

        if (pageNo == 1) {
            swipeProgress(true)
        } else {
            adapterVideo?.setLoading()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            requestListPostApi(pageNo)
        }, Constant.DELAY_TIME)
    }

    private fun refreshVideos() {
        callbackCall?.cancel()
        adapterVideo?.resetListData()
        requestAction(1)
    }

    private fun showFailedView(show: Boolean, message: String?) {
        lytFailed.isVisible = show
        recyclerView.isVisible = !show
        lytNoSearchResults.isVisible = false
        txtFailedMessage.text = message ?: getString(R.string.failed_text)
    }

    private fun showNoItemView(show: Boolean) {
        lytNoItem.isVisible = show
        recyclerView.isVisible = !show
        lytNoSearchResults.isVisible = false
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

        // ซ่อน ActionBar เมื่อกลับมาที่ Fragment นี้
        hideActionBar()

        // สำหรับแอปครอบครัว: ตรวจสอบ compliance ใหม่
        if (adsPref.isFamiliesApp && isAdMobInitialized) {
            Handler(Looper.getMainLooper()).postDelayed({
                checkFamiliesCompliance()
            }, 1000)
        }
    }

    override fun onPause() {
        super.onPause()

        // ซ่อน keyboard
        hideKeyboard()
    }

    override fun onDestroy() {
        super.onDestroy()
        swipeProgress(false)
        callbackCall?.takeIf { it.isExecuted }?.cancel()
        shimmerFrameLayout.stopShimmer()

        // Clean up handlers
        searchHandler?.removeCallbacksAndMessages(null)
    }

    override fun onDetach() {
        super.onDetach()
        activityContext = null
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