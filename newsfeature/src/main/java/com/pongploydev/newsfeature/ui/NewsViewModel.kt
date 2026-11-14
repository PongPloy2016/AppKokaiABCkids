package com.pongploydev.newsfeature.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pongploydev.newsfeature.data.FeedState
import com.pongploydev.newsfeature.data.NewsArticle
import com.pongploydev.newsfeature.data.NewsFeedItem
import com.pongploydev.newsfeature.data.NewsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

data class FeaturedFeedArticle(
    val feed: NewsFeedItem,
    val article: NewsArticle
)

class NewsViewModel : ViewModel() {

    var feeds = mutableStateListOf<NewsFeedItem>().apply {
        addAll(NewsFeedItem.DEFAULT_FEEDS)
    }
        private set

    var selectedFeed by mutableStateOf(NewsFeedItem.DEFAULT_FEEDS.first())
        private set

    var featuredSlides by mutableStateOf<List<FeaturedFeedArticle>>(emptyList())
        private set

    var featuredLoading by mutableStateOf(false)
        private set

    private val states = mutableStateMapOf<String, FeedState>()
    private val jobs = HashMap<String, Job>()
    private var featuredJob: Job? = null

    init {
        // โหลดฟีดแรกทันทีด้วย Default Feeds
        load(selectedFeed)
        // โหลดเรื่องเด่นของแต่ละหมวดสำหรับหน้าแรก
        loadFeaturedPerTab()
        // ดึงรายการฟีดอัปเดตล่าสุดจาก Supabase เบื้องหลัง
        syncFeedsFromSupabase()
    }

    fun stateOf(feed: NewsFeedItem): FeedState = states[feed.id] ?: FeedState(loading = true)

    fun select(feed: NewsFeedItem) {
        selectedFeed = feed
        val state = states[feed.id]
        if (state == null || (state.items.isEmpty() && !state.loading)) {
            load(feed)
        }
    }

    /** ดึงรายการฟีดล่าสุดจากตาราง kids_news_feeds บน Supabase */
    fun syncFeedsFromSupabase(force: Boolean = false) {
        viewModelScope.launch {
            val remoteFeeds = NewsRepository.getFeeds(force = force)
            if (remoteFeeds.isNotEmpty() && remoteFeeds != feeds) {
                val currentSelectedId = selectedFeed.id
                feeds.clear()
                feeds.addAll(remoteFeeds)

                // ปรับฟีดที่เลือกอยู่ให้ตรงกับรายการใหม่
                val newSelected = feeds.find { it.id == currentSelectedId } ?: feeds.first()
                if (newSelected != selectedFeed) {
                    selectedFeed = newSelected
                    load(newSelected)
                }
                loadFeaturedPerTab(force = force)
            }
        }
    }

    /** โหลดเรื่องเด่นอันดับ 1 ของแต่ละหมวด (1 เรื่องต่อ 1 tab) สำหรับสไลด์หน้าแรก */
    fun loadFeaturedPerTab(force: Boolean = false) {
        if (featuredJob?.isActive == true && !force) return
        featuredJob?.cancel()

        featuredJob = viewModelScope.launch {
            val targetFeeds = feeds.toList()
            if (targetFeeds.isEmpty()) return@launch
            featuredLoading = featuredSlides.isEmpty()

            val orderedSlides = if (force) {
                mutableListOf<FeaturedFeedArticle>()
            } else {
                featuredSlides.filter { existing -> targetFeeds.any { it.id == existing.feed.id } }.toMutableList()
            }

            fun upsertSlide(feed: NewsFeedItem, article: NewsArticle) {
                val idx = orderedSlides.indexOfFirst { it.feed.id == feed.id }
                if (idx >= 0) {
                    orderedSlides[idx] = FeaturedFeedArticle(feed, article)
                } else {
                    orderedSlides.add(FeaturedFeedArticle(feed, article))
                }
                featuredSlides = orderedSlides.toList()
                featuredLoading = false
            }

            // ใส่ข้อมูลจาก cache ทันทีที่มีอยู่แล้วตามลำดับ feed
            targetFeeds.forEach { feed ->
                val cached = if (!force && NewsRepository.isFresh(feed)) {
                    NewsRepository.cached(feed).orEmpty()
                } else {
                    states[feed.id]?.items.orEmpty()
                }
                val top = cached.firstOrNull { !it.imageUrl.isNullOrBlank() } ?: cached.firstOrNull()
                if (top != null) {
                    upsertSlide(feed, top)
                }
            }

            val semaphore = Semaphore(4)
            coroutineScope {
                targetFeeds.map { feed ->
                    async {
                        if (!force && NewsRepository.isFresh(feed)) {
                            val items = NewsRepository.cached(feed).orEmpty()
                            states[feed.id] = FeedState(items = items)
                            val top = items.firstOrNull { !it.imageUrl.isNullOrBlank() } ?: items.firstOrNull()
                            if (top != null) {
                                upsertSlide(feed, top)
                            }
                            return@async
                        }
                        semaphore.withPermit {
                            NewsRepository.load(feed, force).onSuccess { items ->
                                states[feed.id] = FeedState(items = items)
                                val top = items.firstOrNull { !it.imageUrl.isNullOrBlank() } ?: items.firstOrNull()
                                if (top != null) {
                                    upsertSlide(feed, top)
                                }
                            }
                        }
                    }
                }.awaitAll()
            }

            featuredLoading = false
        }
    }

    /** ผู้ใช้กดรีเฟรช: ดึงข้อมูลข่าวและฟีดใหม่ทันที */
    fun refresh() {
        syncFeedsFromSupabase(force = true)
        load(selectedFeed, force = true)
        loadFeaturedPerTab(force = true)
    }

    /** เรียกตอนกลับมาที่หน้านี้: โหลดใหม่เฉพาะเมื่อ cache เก่าแล้ว */
    fun refreshIfStale() {
        syncFeedsFromSupabase(force = false)
        load(selectedFeed, force = false)
        loadFeaturedPerTab(force = false)
    }

    private fun load(feed: NewsFeedItem, force: Boolean = false) {
        if (jobs[feed.id]?.isActive == true) return

        if (!force && NewsRepository.isFresh(feed)) {
            val cached = NewsRepository.cached(feed).orEmpty()
            states[feed.id] = FeedState(items = cached)
            return
        }

        val previous = states[feed.id] ?: FeedState()
        states[feed.id] = previous.copy(loading = true, error = null)
        jobs[feed.id] = viewModelScope.launch {
            NewsRepository.load(feed, force).fold(
                onSuccess = { states[feed.id] = FeedState(items = it) },
                onFailure = {
                    states[feed.id] = previous.copy(
                        loading = false,
                        error = "ไม่สามารถโหลดข่าวได้ กรุณาตรวจสอบอินเทอร์เน็ตแล้วลองอีกครั้ง"
                    )
                }
            )
        }
    }
}
