package com.pongploydev.videowatchfeaturesupabase.data.repository

import android.net.Uri
import android.text.Html
import android.util.Log
import com.pongploydev.videowatchfeaturesupabase.config.AppConfig
import com.pongploydev.videowatchfeaturesupabase.data.remote.SupabaseClient
import com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem
import com.pongploydev.videowatchfeaturesupabase.model.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

/**
 * Repository สำหรับจัดการดึงแท็บแหล่งวิดีโอเด็กจาก Supabase (kids_video_feeds)
 * และดาวน์โหลดรายการวิดีโอจาก RSS / Atom / YouTube feeds
 */
object VideoFeedRepository {

    private const val TAG = "VideoFeedRepository"
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 15_000
    private const val CACHE_TTL_MS = 5 * 60 * 1000L // 5 นาที

    private class CachedVideos(val fetchedAt: Long, val videos: List<Video>)

    private val feedCache = ConcurrentHashMap<String, CachedVideos>()
    private var cachedFeedsList: List<KidsVideoFeedItem>? = null
    private var feedsListFetchedAt: Long = 0L

    /**
     * ดึงรายการแท็บวิดีโอจากตาราง public.kids_video_feeds บน Supabase
     */
    suspend fun getFeeds(force: Boolean = false): List<KidsVideoFeedItem> {
        if (!force && cachedFeedsList != null && System.currentTimeMillis() - feedsListFetchedAt < CACHE_TTL_MS) {
            return cachedFeedsList!!
        }

        return withContext(Dispatchers.IO) {
            try {
                val response = SupabaseClient.getService().getKidsVideoFeeds()
                if (response.isSuccessful) {
                    val body = response.body().orEmpty()
                    Log.d(TAG, "✅ Loaded ${body.size} feeds from Supabase kids_video_feeds")
                    cachedFeedsList = body
                    feedsListFetchedAt = System.currentTimeMillis()
                    return@withContext body
                }
                Log.w(TAG, "Supabase kids_video_feeds returned HTTP ${response.code()}")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching kids_video_feeds from Supabase: ${e.message}")
            }
            cachedFeedsList ?: emptyList()
        }
    }

    /**
     * ดึงวิดีโอจาก feed_url ของแท็บที่เลือก (ใช้ URL จาก Supabase โดยตรง)
     */
    suspend fun loadFeedVideos(feed: KidsVideoFeedItem, force: Boolean = false): List<Video> {
        val cacheKey = "${feed.id}_${feed.feedUrl}"
        val cached = feedCache[cacheKey]
        if (!force && cached != null && System.currentTimeMillis() - cached.fetchedAt < CACHE_TTL_MS) {
            return cached.videos
        }

        return withContext(Dispatchers.IO) {
            val videos = try {
                downloadFeedVideos(feed)
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading feed ${feed.id} (${feed.feedUrl}): ${e.message}")
                emptyList()
            }

            if (videos.isNotEmpty()) {
                feedCache[cacheKey] = CachedVideos(System.currentTimeMillis(), videos)
            } else {
                feedCache.remove(cacheKey)
            }
            videos
        }
    }

    private fun sanitizeFeedUrl(rawUrl: String): String {
        val url = rawUrl.trim()
        if (url.isEmpty()) return ""

        // ถ้าเป็น URL ของ YouTube RSS หรือมี query string ที่ถูกต้องอยู่แล้ว ให้ใช้ตรงๆ จาก Supabase
        if (url.contains("youtube.com/feeds/videos.xml", ignoreCase = true)) {
            return url
        }

        val qIdx = url.indexOf('?')
        if (qIdx != -1) {
            val base = url.substring(0, qIdx)
            val queryStr = url.substring(qIdx + 1)
            val pairs = queryStr.split("&")
            val cleanPairs = pairs.map { pair ->
                val eq = pair.indexOf('=')
                if (eq != -1) {
                    val k = pair.substring(0, eq)
                    val v = pair.substring(eq + 1)
                    val decoded = try { java.net.URLDecoder.decode(v, "UTF-8") } catch (_: Exception) { v }
                    "$k=" + URLEncoder.encode(decoded, "UTF-8")
                } else {
                    pair
                }
            }
            return "$base?${cleanPairs.joinToString("&")}"
        }
        return url
    }

    private fun downloadFeedVideos(feed: KidsVideoFeedItem): List<Video> {
        val cleanUrl = sanitizeFeedUrl(feed.feedUrl)
        if (cleanUrl.isEmpty()) {
            Log.w(TAG, "Feed ${feed.id} has empty feed_url")
            return emptyList()
        }

        // รองรับ YouTube Data API v3 ทั้งจาก URL API โดยตรง หรือแปลงจาก Google News site:youtube.com query
        if (cleanUrl.contains("googleapis.com/youtube/v3/", ignoreCase = true) ||
            (cleanUrl.contains("news.google.com/rss/search", ignoreCase = true) &&
                cleanUrl.contains("youtube.com", ignoreCase = true))
        ) {
            val ytApiVideos = tryLoadFromYouTubeDataApi(cleanUrl, feed)
            if (ytApiVideos.isNotEmpty()) {
                return ytApiVideos
            }
            // ถ้ายังไม่ได้เปิดใช้ YouTube Data API v3 และเป็นลิงก์ Google News RSS ให้ fallback ไปโหลด RSS เดิมต่อ
            if (cleanUrl.contains("googleapis.com/youtube/v3/", ignoreCase = true)) {
                return emptyList()
            }
        }

        val connection = URL(cleanUrl).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.instanceFollowRedirects = true
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) PaintApp-KidsVideo")
            connection.setRequestProperty("Accept", "application/atom+xml, application/rss+xml, application/xml, text/xml, */*")

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "Feed URL $cleanUrl returned HTTP $responseCode")
                return emptyList()
            }

            return connection.inputStream.use { parseXmlToVideos(it, feed) }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseXmlToVideos(inputStream: InputStream, feed: KidsVideoFeedItem): List<Video> {
        val result = mutableListOf<Video>()
        val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = true }
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var inItem = false
        var currentTitle = ""
        var currentLink = ""
        var currentVideoId = ""
        var currentDescription = ""
        var currentThumbnail = ""
        var currentPublished = ""
        var itemCounter = 0

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name?.lowercase(Locale.ROOT) ?: ""

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName == "item" || tagName == "entry") {
                        inItem = true
                        currentTitle = ""
                        currentLink = ""
                        currentVideoId = ""
                        currentDescription = ""
                        currentThumbnail = ""
                        currentPublished = ""
                    } else if (inItem) {
                        when (tagName) {
                            "videoid", "yt:videoid" -> {
                                currentVideoId = parser.nextText().trim()
                            }
                            "id", "guid" -> {
                                if (currentVideoId.isEmpty()) {
                                    val idText = try { parser.nextText().trim() } catch (_: Exception) { "" }
                                    if (idText.startsWith("yt:video:")) {
                                        currentVideoId = idText.removePrefix("yt:video:").trim()
                                    }
                                }
                            }
                            "title" -> {
                                if (currentTitle.isEmpty()) {
                                    currentTitle = parser.nextText().trim()
                                }
                            }
                            "link" -> {
                                val href = parser.getAttributeValue(null, "href")?.trim().orEmpty()
                                if (href.isNotEmpty()) {
                                    currentLink = href
                                } else {
                                    val txt = try { parser.nextText().trim() } catch (_: Exception) { "" }
                                    if (txt.isNotEmpty() && currentLink.isEmpty()) {
                                        currentLink = txt
                                    }
                                }
                            }
                            "description", "media:description" -> {
                                if (currentDescription.isEmpty()) {
                                    val desc = try { parser.nextText() } catch (_: Exception) { "" }
                                    currentDescription = Html.fromHtml(desc).toString().trim()
                                }
                            }
                            "thumbnail", "media:thumbnail" -> {
                                val thumbUrl = parser.getAttributeValue(null, "url")?.trim()
                                if (!thumbUrl.isNullOrEmpty() && currentThumbnail.isEmpty()) {
                                    currentThumbnail = thumbUrl
                                }
                            }
                            "published", "pubdate", "updated" -> {
                                if (currentPublished.isEmpty()) {
                                    currentPublished = try { parser.nextText().trim() } catch (_: Exception) { "" }
                                }
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName == "item" || tagName == "entry") {
                        inItem = false

                        // สกัด YouTube ID ถ้ายังไม่ได้จากแท็ก yt:videoid
                        if (currentVideoId.isEmpty() && currentLink.isNotEmpty()) {
                            currentVideoId = extractYouTubeId(currentLink)
                        }

                        // ถ้ามีชื่อ " - YouTube" ติดมา ตัดออกเพื่อความสวยงาม
                        val cleanTitle = currentTitle.replace(" - YouTube", "").trim()

                        if (cleanTitle.isNotEmpty()) {
                            // กรองคำที่ไม่เหมาะสมเฉพาะเมื่อแท็บนั้นเปิด is_kids_filter_required ใน Supabase
                            if (feed.isKidsFilterRequired && !isTitleKidsSafe(cleanTitle)) {
                                eventType = parser.next()
                                continue
                            }

                            val vId = if (currentVideoId.isNotEmpty()) currentVideoId else "vid_${Math.abs((cleanTitle + currentLink).hashCode())}"
                            val thumb = when {
                                currentThumbnail.isNotEmpty() -> currentThumbnail
                                currentVideoId.isNotEmpty() -> "https://img.youtube.com/vi/$currentVideoId/hqdefault.jpg"
                                else -> ""
                            }

                            val video = Video().apply {
                                id = 1000 + itemCounter++
                                vid = "FEED_$vId"
                                video_title = cleanTitle
                                video_url = if (currentVideoId.isNotEmpty()) "https://www.youtube.com/watch?v=$currentVideoId" else currentLink
                                video_id = currentVideoId
                                video_thumbnail = thumb
                                video_duration = "05:20"
                                video_description = currentDescription.ifEmpty { "รายการวิดีโอคุณภาพสำหรับเด็กและครอบครัว จาก ${feed.provider.ifEmpty { feed.name }}" }
                                video_type = if (currentVideoId.isNotEmpty()) "youtube" else "rss"
                                category_name = feed.name
                                tags = "#${feed.id} #${feed.name.replace(" ", "")} #เด็กและครอบครัว"
                                age_rating = "เด็กและครอบครัว"
                                date_time = currentPublished
                                total_views = 45000L + (Math.abs(cleanTitle.hashCode()) % 80000)
                                rating = 4.9f
                                isNew = true
                            }
                            result.add(video)
                            if (result.size >= 100) {
                                break
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return result.distinctBy { if (it.video_id.isNotEmpty()) it.video_id else it.video_title }
    }

    private fun isTitleKidsSafe(title: String): Boolean {
        val lower = title.lowercase(Locale.ROOT)
        val badWords = listOf("horror", "เกม", "game", "แฉ", "ดราม่า", "ฆ่า", "ตาย", "ดับ", "สงคราม", "scarlet heart", "ep.", "terrible")
        if (badWords.any { lower.contains(it) }) return false
        val goodWords = listOf("นิทาน", "เด็ก", "อ่าน", "ก-ฮ", "ก ไก่", "สระ", "เขียน", "อนุบาล", "การ์ตูน", "ลูกหมู", "พินอคคิโอ", "สอน", "เรียนรู้", "เพลง", "ปฐมวัย", "หลับสบาย", "นิทานก่อนนอน", "สัตว์")
        return goodWords.any { lower.contains(it) }
    }

    private fun tryLoadFromYouTubeDataApi(sourceUrl: String, feed: KidsVideoFeedItem): List<Video> {
        val apiKey = AppConfig.YOUTUBE_DATA_API_KEY
        if (apiKey.isBlank()) return emptyList()

        val requestUrl = try {
            if (sourceUrl.contains("googleapis.com/youtube/v3/", ignoreCase = true)) {
                if (sourceUrl.contains("key=", ignoreCase = true)) sourceUrl
                else if (sourceUrl.contains("?")) "$sourceUrl&key=$apiKey"
                else "$sourceUrl?key=$apiKey"
            } else {
                val uri = Uri.parse(sourceUrl)
                val rawQ = uri.getQueryParameter("q").orEmpty()
                val cleanedQ = rawQ
                    .replace(Regex("site:youtube\\.com", RegexOption.IGNORE_CASE), "")
                    .replace("(", "")
                    .replace(")", "")
                    .replace(Regex("\\s+OR\\s+", RegexOption.IGNORE_CASE), "|")
                    .trim()
                if (cleanedQ.isEmpty()) return emptyList()
                val encodedQ = URLEncoder.encode(cleanedQ, "UTF-8")
                "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&safeSearch=strict&relevanceLanguage=th&regionCode=TH&maxResults=30&q=$encodedQ&key=$apiKey"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error building YouTube API URL: ${e.message}")
            return emptyList()
        }

        val connection = URL(requestUrl).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")

            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                val errBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                Log.w(TAG, "YouTube Data API v3 returned HTTP $code: $errBody")
                return emptyList()
            }

            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            parseYouTubeApiJson(jsonText, feed)
        } catch (e: Exception) {
            Log.e(TAG, "YouTube Data API v3 request failed: ${e.message}")
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseYouTubeApiJson(jsonText: String, feed: KidsVideoFeedItem): List<Video> {
        val result = mutableListOf<Video>()
        val root = JSONObject(jsonText)
        val items = root.optJSONArray("items") ?: return emptyList()

        for (i in 0 until items.length()) {
            val itemObj = items.optJSONObject(i) ?: continue
            val idObj = itemObj.optJSONObject("id")
            val videoId = idObj?.optString("videoId")?.trim().orEmpty()
            if (videoId.isEmpty()) continue

            val snippet = itemObj.optJSONObject("snippet") ?: continue
            val rawTitle = snippet.optString("title").trim()
            val cleanTitle = Html.fromHtml(rawTitle).toString().trim()
            if (cleanTitle.isEmpty()) continue

            if (feed.isKidsFilterRequired && !isTitleKidsSafe(cleanTitle)) {
                continue
            }

            val desc = Html.fromHtml(snippet.optString("description").trim()).toString().trim()
            val publishedAt = snippet.optString("publishedAt").trim()
            val channelTitle = snippet.optString("channelTitle").trim()

            val thumbs = snippet.optJSONObject("thumbnails")
            val thumbUrl = thumbs?.optJSONObject("high")?.optString("url")
                ?.takeIf { it.isNotBlank() }
                ?: thumbs?.optJSONObject("medium")?.optString("url")?.takeIf { it.isNotBlank() }
                ?: thumbs?.optJSONObject("default")?.optString("url")?.takeIf { it.isNotBlank() }
                ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

            result.add(
                Video().apply {
                    id = 2000 + i
                    vid = "FEED_$videoId"
                    video_title = cleanTitle
                    video_url = "https://www.youtube.com/watch?v=$videoId"
                    video_id = videoId
                    video_thumbnail = thumbUrl
                    video_duration = "YouTube"
                    video_description = desc.ifEmpty {
                        "วิดีโอสำหรับเด็กจาก $channelTitle (${feed.name})"
                    }
                    video_type = "youtube"
                    category_name = feed.name
                    tags = "#${feed.id} #${feed.name.replace(" ", "")} #เด็กและครอบครัว"
                    age_rating = "เด็กและครอบครัว"
                    date_time = publishedAt
                    total_views = 45000L + (Math.abs(cleanTitle.hashCode()) % 80000)
                    rating = 4.9f
                    isNew = true
                }
            )
        }
        return result.distinctBy { it.video_id }
    }

    fun extractYouTubeId(url: String): String {
        if (url.isEmpty()) return ""
        val pattern = ".*(?:youtu\\.be/|v/|u/\\w/|embed/|shorts/|watch\\?v=|watch\\?.*?&v=)([^#&?]*).*"
        val compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
        val matcher = compiledPattern.matcher(url)
        if (matcher.matches()) {
            val id = matcher.group(1)
            if (!id.isNullOrEmpty() && id.length == 11) return id
        }
        return ""
    }
}
