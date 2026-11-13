package com.pongploydev.videowatchfeaturesupabase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pongploydev.videowatchfeaturesupabase.config.AppConfig
import com.pongploydev.videowatchfeaturesupabase.data.repository.VideoListRepositoryImpl
import com.pongploydev.videowatchfeaturesupabase.databases.prefs.SharedPref
import com.pongploydev.videowatchfeaturesupabase.domain.usecase.GetVideoListUseCase
import com.pongploydev.videowatchfeaturesupabase.model.Video
import com.pongploydev.videowatchfeaturesupabase.rests.RestAdapter
import kotlinx.coroutines.launch

class VideoListViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPref = SharedPref(application)

    // LiveData
    private val _videoList = MutableLiveData<List<Video>>()
    val videoList: LiveData<List<Video>> = _videoList

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isSupabaseApi = MutableLiveData<Boolean>()
    val isSupabaseApi: LiveData<Boolean> = _isSupabaseApi

    // รายการแท็บฟีดวิดีโอจากตาราง public.kids_video_feeds บน Supabase
    private val _videoFeeds = MutableLiveData<List<com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem>>()
    val videoFeeds: LiveData<List<com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem>> = _videoFeeds

    // แคชวิดีโอแยกตามฟีด
    private val feedVideosCache = mutableMapOf<String, List<Video>>()

    // Internal State
    private var originalVideos = mutableListOf<Video>()
    var postTotal = 0
    var currentCategory = "all"
    var searchQuery = ""

    private var currentPage = 1
    private var isFetching = false
    val isFetchingVideos: Boolean get() = isFetching

    private val getVideoListUseCase: GetVideoListUseCase

    init {
        val apiInterfaceV1 = RestAdapter.createAPIV1(application, sharedPref.baseUrl)
        val videoListRepo = VideoListRepositoryImpl(apiInterfaceV1)
        getVideoListUseCase = GetVideoListUseCase(videoListRepo)

        _isSupabaseApi.value = sharedPref.isUseSupabaseApi

        // โหลดรายการแท็บฟีดจาก Supabase
        loadVideoFeeds()

        // โหลดข้อมูลวิดีโอเริ่มต้นเพื่อให้เด็กๆ เข้าใช้งานได้ทันทีแม้ยังโหลดเครือข่ายไม่เสร็จ
        originalVideos.addAll(getFallbackKidVideos())
        postTotal = originalVideos.size
        filterVideos("", "all")
    }

    fun loadVideoFeeds(force: Boolean = false) {
        viewModelScope.launch {
            try {
                val feeds = com.pongploydev.videowatchfeaturesupabase.data.repository.VideoFeedRepository.getFeeds(force)
                _videoFeeds.value = feeds
            } catch (e: Exception) {
                _videoFeeds.value = emptyList()
            }
        }
    }

    fun setApiSource(useSupabase: Boolean) {
        sharedPref.isUseSupabaseApi = useSupabase
        _isSupabaseApi.value = useSupabase
        originalVideos.clear()
        currentPage = 1
        postTotal = 0
        fetchVideos(1, isRefresh = true)
    }

    fun toggleApiSource(): Boolean {
        val next = !(_isSupabaseApi.value ?: true)
        setApiSource(next)
        return next
    }

    fun getLoadedVideoCount(): Int = originalVideos.size

    fun loadNextPage() {
        if (isFetching) return
        if (originalVideos.size >= postTotal && postTotal > 0) return
        fetchVideos(currentPage + 1)
    }

    fun fetchVideos(pageNo: Int, isRefresh: Boolean = false) {
        if (isFetching && !isRefresh) return
        isFetching = true

        if (isRefresh) {
            feedVideosCache.clear()
            loadVideoFeeds(force = true)
        }

        // If loading first page or refreshing, set loading state
        if (pageNo == 1 && (originalVideos.isEmpty() || isRefresh)) {
            _isLoading.value = true
        }

        val cacheControl = if (isRefresh) "no-cache" else null
        val useSupabase = _isSupabaseApi.value ?: sharedPref.isUseSupabaseApi

        viewModelScope.launch {
            try {
                val fetchCount = AppConfig.LOAD_MORE
                val result = getVideoListUseCase(cacheControl, "latest", fetchCount, pageNo, useSupabase)

                if (pageNo == 1) {
                    _isLoading.value = false
                }

                result.onSuccess { response ->
                    postTotal = response.countTotal
                    val posts = response.posts

                    if (pageNo == 1) {
                        originalVideos.clear()
                        currentPage = 1
                    } else {
                        currentPage = pageNo
                    }

                    if (posts.isNotEmpty()) {
                        val existingIds = originalVideos.map { it.id }.toSet()
                        val newPosts = posts.filter { it.id !in existingIds }
                        originalVideos.addAll(newPosts)
                    }

                    // Apply filters if any
                    filterVideos(searchQuery, currentCategory)
                    _errorMessage.value = null // Clear error
                }.onFailure { exception ->
                    if (originalVideos.isEmpty()) {
                        originalVideos.addAll(getFallbackKidVideos())
                        postTotal = originalVideos.size
                        filterVideos(searchQuery, currentCategory)
                    }
                    _errorMessage.value = null
                }
            } catch (e: Exception) {
                if (originalVideos.isEmpty()) {
                    originalVideos.addAll(getFallbackKidVideos())
                    postTotal = originalVideos.size
                    filterVideos(searchQuery, currentCategory)
                }
                _errorMessage.value = null
            } finally {
                isFetching = false
            }
        }
    }

    fun filterVideos(query: String, category: String) {
        searchQuery = query
        currentCategory = category

        // 1. ตรวจสอบว่ามีแคชวิดีโอจากฟีดนี้อยู่แล้วหรือไม่
        val cachedFeedVideos = feedVideosCache[category]
        if (cachedFeedVideos != null && cachedFeedVideos.isNotEmpty()) {
            val filtered = cachedFeedVideos.filter { it.matchesSearch(query) }
            _videoList.value = filtered
            return
        }

        // 2. กรองจาก originalVideos ก่อนทันที เพื่อแสดงผลลัพธ์ทันทีไม่มีหน้าว่าง
        val initialFiltered = originalVideos.filter {
            it.matchesFilters(query, category, false, 0f)
        }
        _videoList.value = initialFiltered

        // 3. ถ้าเป็นหมวดหมู่ที่ตรงกับ Feed ให้ดึงคอนเทนต์สดจาก Feed RSS ของหมวดนั้น
        val matchedFeed = findMatchingFeed(category)
        if (matchedFeed != null) {
            loadVideosForFeed(matchedFeed, category, query)
        } else if (initialFiltered.size < 5 && originalVideos.size < postTotal && !isFetching) {
            loadNextPage()
        }
    }

    private fun findMatchingFeed(categoryKey: String): com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem? {
        val feeds = _videoFeeds.value.orEmpty()
        val keyLower = categoryKey.lowercase()

        // 1. หาตาม ID โดยตรง
        val byId = feeds.firstOrNull { it.id.equals(categoryKey, ignoreCase = true) }
        if (byId != null) return byId

        // 2. แมปหมวดหมู่ที่คลิกกับ ID หรือ Provider ของฟีด
        return when (keyLower) {
            "thaipbs_kids", "thaipbs_kids_youtube" -> {
                feeds.firstOrNull { it.id == "thaipbs_kids_youtube" || it.name.contains("Kids", ignoreCase = true) }
                    ?: feeds.firstOrNull { it.provider.contains("Thai PBS Kids", ignoreCase = true) }
                    ?: feeds.firstOrNull { it.id == "thaipbs_kids_programs" }
            }
            "thaipbs_main", "thaipbs_youtube_main", "วิดีโอรวม" -> feeds.firstOrNull { it.id == "thaipbs_youtube_main" || it.name.contains("รวม") }
            "programs", "thaipbs_kids_programs", "รายการเด็ก", "ฝึกอ่านและเขียน" -> feeds.firstOrNull { it.id == "thaipbs_kids_programs" || it.name.contains("รายการ") || it.name.contains("อ่าน") }
            "talk", "thaipbs_talk_about_kids", "คุยกับลูก" -> feeds.firstOrNull { it.id == "thaipbs_talk_about_kids" || it.name.contains("คุย") }
            "story", "story_feed", "นิทาน" -> feeds.firstOrNull { it.id == "story_feed" || it.name.contains("นิทาน") }
            else -> feeds.firstOrNull { it.name.contains(categoryKey, ignoreCase = true) || it.id.contains(keyLower) }
        }
    }

    private fun loadVideosForFeed(
        feed: com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem,
        categoryKey: String,
        query: String
    ) {
        viewModelScope.launch {
            try {
                val videos = com.pongploydev.videowatchfeaturesupabase.data.repository.VideoFeedRepository.loadFeedVideos(feed)
                if (videos.isNotEmpty()) {
                    feedVideosCache[categoryKey] = videos
                    feedVideosCache[feed.id] = videos

                    // เพิ่มเข้า originalVideos ด้วยเพื่อไม่ให้ขาดหาย
                    val existingIds = originalVideos.map { it.vid }.toSet()
                    val newVideos = videos.filter { it.vid !in existingIds }
                    originalVideos.addAll(newVideos)

                    if (currentCategory == categoryKey || currentCategory == feed.id ||
                        ((categoryKey == "thaipbs_kids" || categoryKey == "thaipbs_kids_youtube") && feed.provider.contains("Thai PBS Kids", ignoreCase = true))) {
                        val filtered = videos.filter { it.matchesSearch(searchQuery) }
                        _videoList.value = filtered
                    }
                }
            } catch (e: Exception) {
                // error fallback handled inside repository
            }
        }
    }

    fun resetData() {
        originalVideos.clear()
        currentPage = 1
        postTotal = 0
        originalVideos.addAll(getCuratedKidVideos())
        _videoList.value = originalVideos
    }

    private fun getFallbackKidVideos(): List<Video> = getCuratedKidVideos()

    companion object {
        fun getCuratedKidVideos(): List<Video> {
            val list = mutableListOf<Video>()

            // 1. เพลง ก ไก่
            list.add(Video().apply {
                id = 1
                vid = "VID001"
                video_title = "เพลง ก ไก่ ในสวนสนุก 🎪"
                category_name = "Spark Learning"
                video_type = "youtube"
                video_id = "AcLn892iPo0"
                video_url = "https://www.youtube.com/watch?v=AcLn892iPo0"
                video_thumbnail = "https://img.youtube.com/vi/AcLn892iPo0/hqdefault.jpg"
                video_duration = "02:10"
                video_description = "ฝึกท่องพยัญชนะไทย ก ไก่ ถึง ฮ นกฮูก พร้อมภาพการ์ตูนสดใส"
                age_rating = "3-7 yrs"
                tags = "#กไก่ #ภาษาไทย #การศึกษา #sparklearning"
                total_views = 120500
            })

            // 2. ABC Phonics
            list.add(Video().apply {
                id = 2
                vid = "VID002"
                video_title = "ABC Phonics Alphabet Song 🔤"
                category_name = "ABC & Phonics"
                video_type = "youtube"
                video_id = "BELlZKpi1Zs"
                video_url = "https://www.youtube.com/watch?v=BELlZKpi1Zs"
                video_thumbnail = "https://img.youtube.com/vi/BELlZKpi1Zs/hqdefault.jpg"
                video_duration = "03:15"
                video_description = "เรียนรู้ออกเสียงโฟนิกส์ A ถึง Z สนุกเข้าใจง่ายสำหรับเด็กปฐมวัย"
                age_rating = "2-6 yrs"
                tags = "#abc #phonics #english"
                total_views = 98000
            })

            // 3. Math Magic - Numbers Song 1 to 10
            list.add(Video().apply {
                id = 3
                vid = "VID003"
                video_title = "นับเลข 1 ถึง 10 แสนสนุก 🐻"
                category_name = "Math Magic"
                video_type = "youtube"
                video_id = "DR-cfDsHCGA"
                video_url = "https://www.youtube.com/watch?v=DR-cfDsHCGA"
                video_thumbnail = "https://img.youtube.com/vi/DR-cfDsHCGA/hqdefault.jpg"
                video_duration = "01:45"
                video_description = "ฝึกนับจำนวน 1-10 ทั้งภาษาไทยและอังกฤษกับพี่หมีใจดี"
                age_rating = "2-5 yrs"
                tags = "#math #numbers #learning #magic"
                total_views = 48200
            })

            // 4. The Wheels on the Bus
            list.add(Video().apply {
                id = 4
                vid = "VID004"
                video_title = "The Wheels On The Bus 🚌"
                category_name = "Spark Learning"
                video_type = "youtube"
                video_id = "e_04ZrNroTo"
                video_url = "https://www.youtube.com/watch?v=e_04ZrNroTo"
                video_thumbnail = "https://img.youtube.com/vi/e_04ZrNroTo/hqdefault.jpg"
                video_duration = "02:24"
                video_description = "ร้องเพลงและขยับร่างกายไปกับรถบัส เสริมสร้างจินตนาการ"
                age_rating = "1-5 yrs"
                tags = "#nurseryrhymes #songs"
                total_views = 310000
            })

            // 5. Shape Song
            list.add(Video().apply {
                id = 5
                vid = "VID005"
                video_title = "รูปทรงเรขาคณิตรอบตัว 🎨"
                category_name = "Math Magic"
                video_type = "youtube"
                video_id = "OEbRDtCAFdU"
                video_url = "https://www.youtube.com/watch?v=OEbRDtCAFdU"
                video_thumbnail = "https://img.youtube.com/vi/OEbRDtCAFdU/hqdefault.jpg"
                video_duration = "01:58"
                video_description = "รู้จักวงกลม สามเหลี่ยม สี่เหลี่ยม และรูปทรงต่างๆ รอบตัวเรา"
                age_rating = "2-6 yrs"
                tags = "#shapes #math #learning"
                total_views = 76500
            })

            // 6. เรียนรู้พยัญชนะและสระ ภาษาไทย ป.1
            list.add(Video().apply {
                id = 6
                vid = "VID102"
                video_title = "เรียนรู้พยัญชนะและสระ - ภาษาไทย ป.1 🇹🇭"
                category_name = "Education & Teaching"
                video_type = "youtube"
                video_id = "mDGOvJ-RFuA"
                video_url = "https://www.youtube.com/watch?v=mDGOvJ-RFuA"
                video_thumbnail = "https://img.youtube.com/vi/mDGOvJ-RFuA/hqdefault.jpg"
                video_duration = "08:12"
                video_description = "เรียนรู้พยัญชนะและสระ สื่อการเรียนการสอน ภาษาไทย ชั้นประถมศึกษาปีที่ 1"
                age_rating = "6-8 yrs"
                tags = "#ภาษาไทย #ป1 #การศึกษา"
                total_views = 85000
            })

            // 7. Thai PBS Kids: คิดส์เรนเจอร์
            list.add(Video().apply {
                id = 7
                vid = "VID104"
                video_title = "คิดส์เรนเจอร์: สำรวจธรรมชาติและสิ่งมีชีวิตรอบตัว 🎒"
                category_name = "Thai PBS Kids"
                video_type = "youtube"
                video_id = "fJowGHv9Q7w"
                video_url = "https://www.youtube.com/watch?v=fJowGHv9Q7w"
                video_thumbnail = "https://img.youtube.com/vi/fJowGHv9Q7w/hqdefault.jpg"
                video_duration = "07:45"
                video_description = "ชวนเด็กๆ ออกสำรวจธรรมชาติ เรียนรู้วงจรชีวิตสัตว์และสิ่งแวดล้อมแสนสนุก"
                age_rating = "4-10 yrs"
                tags = "#thaipbs #thaipbskids #ธรรมชาติ #สัตว์"
                total_views = 98000
            })

            // 8. บ้านนักวิทยาศาสตร์น้อย
            list.add(Video().apply {
                id = 8
                vid = "VID105"
                video_title = "บ้านนักวิทยาศาสตร์น้อย: วิทยาศาสตร์แสนสนุกสำหรับเด็ก 🧪"
                category_name = "Thai PBS Kids"
                video_type = "youtube"
                video_id = "n1kdvZ76OPo"
                video_url = "https://www.youtube.com/watch?v=n1kdvZ76OPo"
                video_thumbnail = "https://img.youtube.com/vi/n1kdvZ76OPo/hqdefault.jpg"
                video_duration = "06:15"
                video_description = "กิจกรรมส่งเสริมการคิดแบบวิทยาศาสตร์สำหรับเด็กปฐมวัยตามแนวทางบ้านนักวิทยาศาสตร์น้อย"
                age_rating = "3-7 yrs"
                tags = "#thaipbs #thaipbskids #บ้านนักวิทยาศาสตร์น้อย #วิทยาศาสตร์"
                total_views = 142000
            })

            // 9. สอนศิลป์ Journey
            list.add(Video().apply {
                id = 9
                vid = "VID106"
                video_title = "สอนศิลป์: วาดภาพและประดิษฐ์ของเล่นแสนสนุก 🎨"
                category_name = "Thai PBS Kids"
                video_type = "youtube"
                video_id = "e2Sus3plcV8"
                video_url = "https://www.youtube.com/watch?v=e2Sus3plcV8"
                video_thumbnail = "https://img.youtube.com/vi/e2Sus3plcV8/hqdefault.jpg"
                video_duration = "06:30"
                video_description = "ชวนเด็กๆ มาปลดปล่อยจินตนาการ วาด ระบายสี และประดิษฐ์ของเล่นสร้างสรรค์"
                age_rating = "3-9 yrs"
                tags = "#thaipbs #thaipbskids #สอนศิลป์ #ศิลปะ"
                total_views = 125000
            })

            // 10. ฝึกอ่านออกเสียง ก-ฮ
            list.add(Video().apply {
                id = 10
                vid = "VID107"
                video_title = "ฝึกอ่านออกเสียง ก-ฮ สำหรับเด็กอนุบาล 📚"
                category_name = "ฝึกอ่านและเขียน"
                video_type = "youtube"
                video_id = "k_Xok3td7m0"
                video_url = "https://www.youtube.com/watch?v=k_Xok3td7m0"
                video_thumbnail = "https://img.youtube.com/vi/k_Xok3td7m0/hqdefault.jpg"
                video_duration = "08:15"
                video_description = "บทเรียนฝึกอ่านออกเสียงพยัญชนะไทย ก ไก่ ถึง ฮ นกฮูก ชัดเจนเข้าใจง่าย"
                age_rating = "3-6 yrs"
                tags = "#ฝึกอ่าน #กไก่ #ภาษาไทย #ฝึกอ่านและเขียน"
                total_views = 195000
            })

            // 11. หม้อข้าวหม้อแกง (Thai PBS รวม)
            list.add(Video().apply {
                id = 11
                vid = "VID108"
                video_title = "หม้อข้าวหม้อแกง: เมนูคุณหนู สุขภาพดีจากธรรมชาติ 🍲"
                category_name = "วิดีโอรวม"
                video_type = "youtube"
                video_id = "xa2GRJC8Yac"
                video_url = "https://www.youtube.com/watch?v=xa2GRJC8Yac"
                video_thumbnail = "https://img.youtube.com/vi/xa2GRJC8Yac/hqdefault.jpg"
                video_duration = "08:00"
                video_description = "เด็กๆ เข้าครัวทำอาหารเพื่อสุขภาพ สนุกกับรสชาติและเรียนรู้วิถีธรรมชาติ"
                age_rating = "5-12 yrs"
                tags = "#thaipbs #วิดีโอรวม #หม้อข้าวหม้อแกง #อาหาร"
                total_views = 127000
            })

            // 12. คุยกับลูก Talk About Kids - สมอง EF
            list.add(Video().apply {
                id = 12
                vid = "VID109"
                video_title = "คุยกับลูก: เข้าใจพัฒนาการสมอง EF และการจัดการอารมณ์ 🧠"
                category_name = "คุยกับลูก"
                video_type = "youtube"
                video_id = "dO4pL8yZq3M"
                video_url = "https://www.youtube.com/watch?v=dO4pL8yZq3M"
                video_thumbnail = "https://img.youtube.com/vi/dO4pL8yZq3M/hqdefault.jpg"
                video_duration = "09:15"
                video_description = "การเสริมสร้างทักษะสมอง Executive Functions (EF) ช่วยให้เด็กคิด ยืดหยุ่น และจัดการอารมณ์ได้ดี"
                age_rating = "พ่อแม่และเด็ก"
                tags = "#คุยกับลูก #talk #ครอบครัว #ef #สมอง"
                total_views = 56000
            })

            // 13. คุยกับลูก Talk About Kids - ปรับสมดุลหน้าจอ
            list.add(Video().apply {
                id = 13
                vid = "VID110"
                video_title = "คุยกับลูก: เทคนิคสร้างสมาธิและปรับสมดุลการใช้หน้าจอ 📱"
                category_name = "คุยกับลูก"
                video_type = "youtube"
                video_id = "vT2mL6yZq9P"
                video_url = "https://www.youtube.com/watch?v=vT2mL6yZq9P"
                video_thumbnail = "https://img.youtube.com/vi/vT2mL6yZq9P/hqdefault.jpg"
                video_duration = "07:30"
                video_description = "วิธีสร้างข้อตกลงเรื่องการใช้หน้าจอในครอบครัว พร้อมกิจกรรมเสริมสร้างสมาธิเชิงบวก"
                age_rating = "พ่อแม่และเด็ก"
                tags = "#คุยกับลูก #talk #ครอบครัว #สมาธิ"
                total_views = 49000
            })

            // 14. นิทานอีสป: กระต่ายกับเต่า
            list.add(Video().apply {
                id = 14
                vid = "VID111"
                video_title = "นิทานอีสป: กระต่ายกับเต่า 🐢🐇"
                category_name = "นิทาน"
                video_type = "youtube"
                video_id = "-a1Qe12xMh4"
                video_url = "https://www.youtube.com/watch?v=-a1Qe12xMh4"
                video_thumbnail = "https://img.youtube.com/vi/-a1Qe12xMh4/hqdefault.jpg"
                video_duration = "04:30"
                video_description = "นิทานคติสอนใจเรื่องความพยายามและความไม่ประมาท ภาพการ์ตูนสีสันสดใส"
                age_rating = "2-8 yrs"
                tags = "#นิทาน #story #story_feed #กระต่ายกับเต่า #อีสป"
                total_views = 154000
            })

            // 15. วิดีโอรวม Thai PBS - สารคดีธรรมชาติใต้ทะเล
            list.add(Video().apply {
                id = 15
                vid = "VID112"
                video_title = "Thai PBS รวม: สารคดีเปิดโลกใต้ทะเลสีคราม 🐬"
                category_name = "วิดีโอรวม"
                video_type = "youtube"
                video_id = "DR-cfDsHCGA"
                video_url = "https://www.youtube.com/watch?v=DR-cfDsHCGA"
                video_thumbnail = "https://img.youtube.com/vi/DR-cfDsHCGA/hqdefault.jpg"
                video_duration = "10:20"
                video_description = "ตื่นตาตื่นใจกับปะการัง โลมา และสัตว์ทะเล พร้อมข้อคิดการดูแลรักษ์สิ่งแวดล้อม"
                age_rating = "เด็กและครอบครัว"
                tags = "#thaipbs #thaipbs_main #thaipbs_youtube_main #วิดีโอรวม #สารคดี"
                total_views = 98000
            })

            // 16. ฝึกอ่านและเขียน ก-ฮ ภาษาไทย
            list.add(Video().apply {
                id = 16
                vid = "VID113"
                video_title = "ฝึกอ่านและเขียน ก-ฮ สระ และสะกดคำง่ายๆ 📚"
                category_name = "รายการเด็ก"
                video_type = "youtube"
                video_id = "mDGOvJ-RFuA"
                video_url = "https://www.youtube.com/watch?v=mDGOvJ-RFuA"
                video_thumbnail = "https://img.youtube.com/vi/mDGOvJ-RFuA/hqdefault.jpg"
                video_duration = "08:12"
                video_description = "บทเรียนสนุกๆ ฝึกสะกดคำภาษาไทย ฝึกอ่านออกเสียง ก ไก่ ถึง ฮ นกฮูก"
                age_rating = "3-7 yrs"
                tags = "#thaipbs_kids_programs #programs #รายการเด็ก #ฝึกอ่านและเขียน #กไก่ #ภาษาไทย"
                total_views = 112000
            })

            return list
        }
    }
}
