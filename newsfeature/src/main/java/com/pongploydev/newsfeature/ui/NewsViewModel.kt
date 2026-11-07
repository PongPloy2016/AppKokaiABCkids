package com.pongploydev.newsfeature.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pongploydev.newsfeature.data.FeedState
import com.pongploydev.newsfeature.data.NewsFeedItem
import com.pongploydev.newsfeature.data.NewsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {

    var feeds = mutableStateListOf<NewsFeedItem>().apply {
        addAll(NewsFeedItem.DEFAULT_FEEDS)
    }
        private set

    var selectedFeed by mutableStateOf(NewsFeedItem.DEFAULT_FEEDS.first())
        private set

    private val states = mutableStateMapOf<String, FeedState>()
    private val jobs = HashMap<String, Job>()

    init {
        // โหลดฟีดแรกทันทีด้วย Default Feeds
        load(selectedFeed)
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
            }
        }
    }

    /** ผู้ใช้กดรีเฟรช: ดึงข้อมูลข่าวและฟีดใหม่ทันที */
    fun refresh() {
        syncFeedsFromSupabase(force = true)
        load(selectedFeed, force = true)
    }

    /** เรียกตอนกลับมาที่หน้านี้: โหลดใหม่เฉพาะเมื่อ cache เก่าแล้ว */
    fun refreshIfStale() {
        syncFeedsFromSupabase(force = false)
        load(selectedFeed, force = false)
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
