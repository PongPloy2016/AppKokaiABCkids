package com.pongploydev.videowatchfeaturesupabase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    // Internal State
    private var originalVideos = mutableListOf<Video>()
    var postTotal = 0
    var currentCategory = "all"
    var searchQuery = ""

    private val getVideoListUseCase: GetVideoListUseCase

    init {
        val apiInterfaceV1 = RestAdapter.createAPIV1(application, sharedPref.baseUrl)
        val videoListRepo = VideoListRepositoryImpl(apiInterfaceV1)
        getVideoListUseCase = GetVideoListUseCase(videoListRepo)

        _isSupabaseApi.value = sharedPref.isUseSupabaseApi

        // โหลดข้อมูลวิดีโอเริ่มต้นเพื่อให้เด็กๆ เข้าใช้งานได้ทันทีแม้ยังโหลดเครือข่ายไม่เสร็จ
        originalVideos.addAll(getFallbackKidVideos())
        postTotal = originalVideos.size
        filterVideos("", "all")
    }

    fun setApiSource(useSupabase: Boolean) {
        sharedPref.isUseSupabaseApi = useSupabase
        _isSupabaseApi.value = useSupabase
        originalVideos.clear()
        fetchVideos(1, isRefresh = true)
    }

    fun toggleApiSource(): Boolean {
        val next = !(_isSupabaseApi.value ?: true)
        setApiSource(next)
        return next
    }

    fun fetchVideos(pageNo: Int, isRefresh: Boolean = false) {
        // If loading first page or refreshing, set loading state
        if (pageNo == 1 && (originalVideos.isEmpty() || isRefresh)) {
            _isLoading.value = true
        }

        val cacheControl = if (isRefresh) "no-cache" else null
        val useSupabase = _isSupabaseApi.value ?: sharedPref.isUseSupabaseApi

        viewModelScope.launch {
            try {
                val result = getVideoListUseCase(cacheControl, "latest", 10, pageNo, useSupabase)


                if (pageNo == 1) {
                    _isLoading.value = false
                }

                result.onSuccess { response ->
                    postTotal = response.countTotal
                    val posts = response.posts

                    if (posts.isNotEmpty()) {
                        if (pageNo == 1) {
                            originalVideos.clear()
                            originalVideos.addAll(posts)
                        } else {
                            originalVideos.addAll(posts)
                        }
                    }

                    // Apply filters if any
                    filterVideos(searchQuery, currentCategory)
                    _errorMessage.value = null // Clear error
                }.onFailure { exception ->
                    // ถ้าโหลดจากเซิร์ฟเวอร์ไม่ได้ ให้ใช้วิดีโอเริ่มต้นและไม่แสดงหน้า Error
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
            }
        }
    }

    fun filterVideos(query: String, category: String) {
        searchQuery = query
        currentCategory = category

        if (originalVideos.isEmpty()) {
            _videoList.value = emptyList()
            return
        }

        // Use the Video model's robust filtering logic
        val filtered = originalVideos.filter {
            it.matchesFilters(query, category, false, 0f)
        }

        _videoList.value = filtered
    }

    fun resetData() {
        originalVideos.clear()
        originalVideos.addAll(getFallbackKidVideos())
        _videoList.value = originalVideos
    }

    private fun getFallbackKidVideos(): List<Video> {
        val list = mutableListOf<Video>()

        // 1. เพลง ก ไก่
        val v1 = Video().apply {
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
            tags = "#กไก่ #ภาษาไทย #การศึกษา"
            total_views = 120500
        }
        list.add(v1)

        // 2. ABC Phonics
        val v2 = Video().apply {
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
        }
        list.add(v2)

        // 3. Math Magic - Numbers Song 1 to 10
        val v3 = Video().apply {
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
            tags = "#math #numbers #kids"
            total_views = 48200
        }
        list.add(v3)

        // 4. The Wheels on the Bus
        val v4 = Video().apply {
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
        }
        list.add(v4)

        // 5. Shape Song
        val v5 = Video().apply {
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
        }
        list.add(v5)

        // 6. เรียนรู้พยัญชนะและสระ ภาษาไทย ป.1
        val v6 = Video().apply {
            id = 102
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
        }
        list.add(v6)

        return list
    }
}
