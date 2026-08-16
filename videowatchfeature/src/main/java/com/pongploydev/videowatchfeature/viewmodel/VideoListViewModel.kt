package com.pongploydev.videowatchfeature.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pongploydev.videowatchfeature.data.repository.VideoListRepositoryImpl
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.domain.usecase.GetVideoListUseCase
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.rests.RestAdapter
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

        // โหลดข้อมูลวิดีโอเริ่มต้นเพื่อให้เด็กๆ เข้าใช้งานได้ทันทีแม้ยังโหลดเครือข่ายไม่เสร็จ
        originalVideos.addAll(getFallbackKidVideos())
        postTotal = originalVideos.size
        filterVideos("", "all")
    }

    fun fetchVideos(pageNo: Int, isRefresh: Boolean = false) {
        // If loading first page or refreshing, set loading state
        if (pageNo == 1 && originalVideos.isEmpty()) {
            _isLoading.value = true
        }

        val cacheControl = if (isRefresh) "no-cache" else null

        viewModelScope.launch {
            try {
                val result = getVideoListUseCase(cacheControl, "latest", 10, pageNo)

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

        // 1. Driving In My Car
        val v1 = Video().apply {
            id = 1
            video_title = "Driving In My Car 🚗"
            category_name = "Spark Learning"
            video_type = "youtube"
            video_id = "BradJ9L_lQI"
            video_url = "https://www.youtube.com/watch?v=BradJ9L_lQI"
            video_duration = "00:51"
            video_description = "Spark Clip: learn vehicle words and movement"
            age_rating = "2-6 yrs"
            tags = "#supersimplesongs #vehicles"
            total_views = 25400
        }
        list.add(v1)

        // 2. Math Magic - Numbers Song 1 to 10
        val v2 = Video().apply {
            id = 2
            video_title = "Count Numbers 1 to 10 🔢"
            category_name = "Math Magic"
            video_type = "youtube"
            video_id = "DR-cfDsHCGA"
            video_url = "https://www.youtube.com/watch?v=DR-cfDsHCGA"
            video_duration = "01:45"
            video_description = "Learn counting 1 to 10 with fun animated characters"
            age_rating = "2-5 yrs"
            tags = "#math #numbers #kids"
            total_views = 48200
        }
        list.add(v2)

        // 3. ABC Phonics Song
        val v3 = Video().apply {
            id = 3
            video_title = "ABC Phonics Alphabet Song 🔤"
            category_name = "ABC"
            video_type = "youtube"
            video_id = "BELlZKpi1Zs"
            video_url = "https://www.youtube.com/watch?v=BELlZKpi1Zs"
            video_duration = "03:15"
            video_description = "A for Apple, B for Ball - Alphabet phonics for kids"
            age_rating = "2-6 yrs"
            tags = "#abc #phonics #english"
            total_views = 98000
        }
        list.add(v3)

        // 4. เพลง ก ไก่ ชวนสนุก
        val v4 = Video().apply {
            id = 4
            video_title = "เพลง ก ไก่ ในสวนแสนสนุก 🇹🇭"
            category_name = "Spark Learning"
            video_type = "youtube"
            video_id = "8pM12JtJv_4"
            video_url = "https://www.youtube.com/watch?v=8pM12JtJv_4"
            video_duration = "02:10"
            video_description = "ฝึกท่องพยัญชนะไทย ก ไก่ ถึง ฮ นกฮูก พร้อมภาพสดใส"
            age_rating = "3-7 yrs"
            tags = "#ภาษาไทย #กไก่ #การศึกษา"
            total_views = 120500
        }
        list.add(v4)

        // 5. The Wheels on the Bus
        val v5 = Video().apply {
            id = 5
            video_title = "The Wheels On The Bus 🚌"
            category_name = "Spark Learning"
            video_type = "youtube"
            video_id = "e_04ZrNroTo"
            video_url = "https://www.youtube.com/watch?v=e_04ZrNroTo"
            video_duration = "02:24"
            video_description = "Sing along and dance with Buster the bus"
            age_rating = "1-5 yrs"
            tags = "#nurseryrhymes #songs"
            total_views = 310000
        }
        list.add(v5)

        // 6. Shape Song
        val v6 = Video().apply {
            id = 6
            video_title = "Shapes Are All Around Us 🔺🟦🟡"
            category_name = "Math Magic"
            video_type = "youtube"
            video_id = "OEbRDtCAFdU"
            video_url = "https://www.youtube.com/watch?v=OEbRDtCAFdU"
            video_duration = "01:58"
            video_description = "Circle, Square, Triangle shape discovery"
            age_rating = "2-6 yrs"
            tags = "#shapes #math #learning"
            total_views = 76500
        }
        list.add(v6)

        return list
    }
}
