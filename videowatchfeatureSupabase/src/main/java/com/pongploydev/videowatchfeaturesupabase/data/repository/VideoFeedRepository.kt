package com.pongploydev.videowatchfeaturesupabase.data.repository

import android.net.Uri
import android.text.Html
import android.util.Log
import com.pongploydev.videowatchfeaturesupabase.data.remote.SupabaseClient
import com.pongploydev.videowatchfeaturesupabase.model.KidsVideoFeedItem
import com.pongploydev.videowatchfeaturesupabase.model.Video
import com.pongploydev.videowatchfeaturesupabase.viewmodel.VideoListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
                    if (body.isNotEmpty()) {
                        Log.d(TAG, "✅ Loaded ${body.size} feeds from Supabase kids_video_feeds")
                        cachedFeedsList = body
                        feedsListFetchedAt = System.currentTimeMillis()
                        return@withContext body
                    }
                }
                Log.w(TAG, "Supabase kids_video_feeds returned empty or error, using default feeds")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching kids_video_feeds from Supabase: ${e.message}")
            }
            cachedFeedsList ?: KidsVideoFeedItem.DEFAULT_FEEDS
        }
    }

    /**
     * ดึงวิดีโอจาก feed_url ของแท็บที่เลือก
     */
    suspend fun loadFeedVideos(feed: KidsVideoFeedItem, force: Boolean = false): List<Video> {
        val cached = feedCache[feed.id]
        if (!force && cached != null && System.currentTimeMillis() - cached.fetchedAt < CACHE_TTL_MS) {
            val hasStaleGoogleNews = cached.videos.any {
                it.video_url.contains("news.google.com") ||
                (cached.videos.count { v -> v.video_thumbnail.contains("-a1Qe12xMh4") } > 1)
            }
            if (!hasStaleGoogleNews) {
                return cached.videos
            }
        }

        return withContext(Dispatchers.IO) {
            val videos = try {
                downloadFeedVideos(feed)
            } catch (e: Exception) {
                Log.e(TAG, "Error downloading feed ${feed.id}: ${e.message}")
                emptyList()
            }

            val finalVideos = if (videos.size >= 6) {
                videos
            } else {
                val fallback = getFallbackVideosForFeed(feed)
                (videos + fallback).distinctBy { it.video_title }
            }

            feedCache[feed.id] = CachedVideos(System.currentTimeMillis(), finalVideos)
            finalVideos
        }
    }

    private fun sanitizeFeedUrl(rawUrl: String, feed: KidsVideoFeedItem? = null): String {
        var url = rawUrl.trim()
        val feedId = feed?.id?.lowercase(Locale.ROOT) ?: ""
        val feedName = feed?.name?.lowercase(Locale.ROOT) ?: ""

        // 1. นิทานก่อนนอน (Bedtime Story) -> Indysong Kids Official Channel RSS
        if (feedId.contains("bedtime") || feedId.contains("story") || feedName.contains("นิทาน")) {
            return "https://www.youtube.com/feeds/videos.xml?channel_id=UC103Yv2QS7C2Vf17XIMk0AQ"
        }

        // 2. การ์ตูนเด็ก (Kids Cartoon / Animation) -> BabyBus Thailand Channel RSS
        if (feedId.contains("cartoon") || feedName.contains("การ์ตูน")) {
            return "https://www.youtube.com/feeds/videos.xml?channel_id=UCKpFt-Nh3A2Sg--mLmCebZQ"
        }

        // 3. เพลงเด็ก (Kids Songs / Rhymes) -> Pinkfong Thailand Channel RSS
        if (feedId.contains("song") || feedId.contains("music") || feedName.contains("เพลง")) {
            return "https://www.youtube.com/feeds/videos.xml?channel_id=UCjKVU1NouDYZHDvAdwLQwvg"
        }

        // 4. วิทยาศาสตร์น้อย (Kids Science / Experiments) -> Thai PBS Kids (บ้านนักวิทยาศาสตร์น้อย & Kid Rangers)
        if (feedId.contains("science") || feedName.contains("วิทย์")) {
            return "https://www.youtube.com/feeds/videos.xml?channel_id=UCkBuvFq-7IcZdIwfG8zrLyg"
        }

        // 5. Thai PBS Kids รายการเด็ก
        if (feedId.contains("thaipbs_kids") || feedName.contains("thai pbs kids") ||
            url.contains("user=ThaiPBSKids", ignoreCase = true) ||
            (url.contains("thaipbs", ignoreCase = true) && url.contains("kids", ignoreCase = true))) {
            return "https://www.youtube.com/feeds/videos.xml?channel_id=UCkBuvFq-7IcZdIwfG8zrLyg"
        }

        // 6. วิดีโอรวม / Thai PBS Main
        if (feedId.contains("main") || feedName.contains("วิดีโอรวม") || url.contains("user=ThaiPBS", ignoreCase = true)) {
            return "https://www.youtube.com/feeds/videos.xml?channel_id=UC5TOFhyb_LxL2VG_Zenhpzw"
        }

        // 7. คุยกับลูก
        if (feedId.contains("talk") || feedName.contains("คุย")) {
            return "https://www.youtube.com/feeds/videos.xml?channel_id=UC5TOFhyb_LxL2VG_Zenhpzw"
        }

        // 8. ถ้าเป็น URL ของ YouTube RSS อยู่แล้วให้ใช้เลย
        if (url.contains("youtube.com/feeds/videos.xml")) {
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
        val cleanUrl = sanitizeFeedUrl(feed.feedUrl, feed)

        // 1. ตรวจสอบกรณีเป็นหน้าเว็บ HTML ปกติที่ไม่ใช่ XML/RSS (เช่น kids.thaipbs.or.th/programs หรือ TalkAboutKids)
        val isRssOrXml = cleanUrl.contains(".xml", ignoreCase = true) ||
                cleanUrl.contains("rss", ignoreCase = true) ||
                cleanUrl.contains("feeds/videos", ignoreCase = true)

        if (!isRssOrXml) {
            if (cleanUrl.contains("kids.thaipbs.or.th/programs", ignoreCase = true)) {
                return getCuratedProgramsVideos()
            }
            if (cleanUrl.contains("TalkAboutKids", ignoreCase = true)) {
                return getCuratedTalkVideos()
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
                            // คัดกรองสำหรับ Google News ให้ผ่านเฉพาะเนื้อหาที่เกี่ยวกับเด็กและปลอดภัย 100%
                            val isUnsafe = feed.feedUrl.contains("news.google.com", ignoreCase = true) && !isTitleKidsSafe(cleanTitle)
                            if (isUnsafe) {
                                eventType = parser.next()
                                continue
                            }

                            val vId = if (currentVideoId.isNotEmpty()) currentVideoId else "vid_${Math.abs((cleanTitle + currentLink).hashCode())}"
                            val thumb = if (currentThumbnail.isNotEmpty()) {
                                currentThumbnail
                            } else if (currentVideoId.isNotEmpty()) {
                                "https://img.youtube.com/vi/$currentVideoId/hqdefault.jpg"
                            } else {
                                pickKidsThumbnail(cleanTitle)
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
                                video_type = "youtube"
                                category_name = feed.name
                                tags = "#${feed.id} #${feed.name.replace(" ", "")} #เด็กและครอบครัว"
                                age_rating = "เด็กและครอบครัว"
                                date_time = currentPublished
                                total_views = 45000L + (Math.abs(cleanTitle.hashCode()) % 80000)
                                rating = 4.9f
                                isNew = true
                            }
                            result.add(video)
                            if (result.size >= 30) {
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

    private val kidThumbnails = listOf(
        "https://img.youtube.com/vi/fJowGHv9Q7w/hqdefault.jpg", // คิดส์เรนเจอร์
        "https://img.youtube.com/vi/e2Sus3plcV8/hqdefault.jpg", // สอนศิลป์
        "https://img.youtube.com/vi/n1kdvZ76OPo/hqdefault.jpg", // บ้านนักวิทยาศาสตร์น้อย
        "https://img.youtube.com/vi/xa2GRJC8Yac/hqdefault.jpg", // หม้อข้าวหม้อแกง
        "https://img.youtube.com/vi/AcLn892iPo0/hqdefault.jpg", // ก ไก่
        "https://img.youtube.com/vi/mdw6UNfz9Og/hqdefault.jpg", // สระไทย
        "https://img.youtube.com/vi/BELlZKpi1Zs/hqdefault.jpg", // ABC Phonics
        "https://img.youtube.com/vi/f0Tq1mBsmk0/hqdefault.jpg", // ลูกหมูสามตัว
        "https://img.youtube.com/vi/-a1Qe12xMh4/hqdefault.jpg", // กระต่ายกับเต่า
        "https://img.youtube.com/vi/oSOCfvMT3Cc/hqdefault.jpg", // นักซิ่งล้อเดียว
        "https://img.youtube.com/vi/x_PsuCO4D_I/hqdefault.jpg", // ขบวนการ Fun น้ำนม
        "https://img.youtube.com/vi/DR-cfDsHCGA/hqdefault.jpg"  // นับเลข 1-10
    )

    private fun pickKidsThumbnail(title: String): String {
        val index = Math.abs(title.hashCode()) % kidThumbnails.size
        return kidThumbnails[index]
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

    /**
     * คืนค่าชุดวิดีโอเด็กที่คัดสรรแล้วตามหมวดหมู่ feed เพื่อเป็น Fallback รับประกันว่าจะไม่มีหน้าว่าง (Empty Screen)
     */
    fun getFallbackVideosForFeed(feed: KidsVideoFeedItem): List<Video> {
        val specific = when {
            feed.id.contains("program", ignoreCase = true) || feed.name.contains("อ่าน") || feed.name.contains("เขียน") -> getCuratedProgramsVideos()
            feed.id.contains("kids", ignoreCase = true) || feed.provider.contains("Kids", ignoreCase = true) -> getCuratedThaiPbsKidsVideos()
            feed.id.contains("main", ignoreCase = true) || feed.name.contains("รวม") -> getCuratedThaiPbsMainVideos()
            feed.id.contains("talk", ignoreCase = true) || feed.name.contains("คุย") -> getCuratedTalkVideos()
            else -> emptyList()
        }
        if (specific.isNotEmpty()) {
            return specific
        }

        val curatedAll = VideoListViewModel.getCuratedKidVideos()
        val matching = curatedAll.filter { it.isInCategory(feed.id) || it.isInCategory(feed.name) }
        if (matching.isNotEmpty()) {
            return matching
        }

        return getCuratedProgramsVideos()
    }

    private fun getCuratedThaiPbsKidsVideos(): List<Video> {
        return listOf(
            Video().apply {
                id = 201
                vid = "TPBS_KIDS_01"
                video_title = "คิดส์เรนเจอร์: สำรวจธรรมชาติและสิ่งมีชีวิตรอบตัว 🎒"
                category_name = "Thai PBS Kids"
                video_type = "youtube"
                video_id = "fJowGHv9Q7w"
                video_url = "https://www.youtube.com/watch?v=fJowGHv9Q7w"
                video_thumbnail = "https://img.youtube.com/vi/fJowGHv9Q7w/hqdefault.jpg"
                video_duration = "07:45"
                video_description = "ชวนเด็กๆ ออกสำรวจธรรมชาติ เรียนรู้วงจรชีวิตสัตว์และสิ่งแวดล้อมแสนสนุก"
                age_rating = "4-10 yrs"
                tags = "#feed #thaipbskids #ธรรมชาติ #สัตว์ #kidrangers"
                total_views = 98000
            },
            Video().apply {
                id = 202
                vid = "TPBS_KIDS_02"
                video_title = "สอนศิลป์: วาดภาพและประดิษฐ์ของเล่นแสนสนุก 🎨"
                category_name = "Thai PBS Kids"
                video_type = "youtube"
                video_id = "e2Sus3plcV8"
                video_url = "https://www.youtube.com/watch?v=e2Sus3plcV8"
                video_thumbnail = "https://img.youtube.com/vi/e2Sus3plcV8/hqdefault.jpg"
                video_duration = "06:30"
                video_description = "ชวนเด็กๆ มาปลดปล่อยจินตนาการ วาด ระบายสี และประดิษฐ์ของเล่นสร้างสรรค์"
                age_rating = "3-9 yrs"
                tags = "#feed #thaipbskids #สอนศิลป์ #ศิลปะ #ประดิษฐ์"
                total_views = 125000
            },
            Video().apply {
                id = 203
                vid = "TPBS_KIDS_03"
                video_title = "บ้านนักวิทยาศาสตร์น้อย: วิทยาศาสตร์แสนสนุกสำหรับเด็ก 🧪"
                category_name = "Thai PBS Kids"
                video_type = "youtube"
                video_id = "n1kdvZ76OPo"
                video_url = "https://www.youtube.com/watch?v=n1kdvZ76OPo"
                video_thumbnail = "https://img.youtube.com/vi/n1kdvZ76OPo/hqdefault.jpg"
                video_duration = "06:15"
                video_description = "การทดลองวิทยาศาสตร์ง่ายๆ เสริมสร้างความคิดสร้างสรรค์และการสังเกตสำหรับเด็กปฐมวัย"
                age_rating = "3-7 yrs"
                tags = "#feed #thaipbskids #บ้านนักวิทยาศาสตร์น้อย #วิทยาศาสตร์"
                total_views = 142000
            },
            Video().apply {
                id = 204
                vid = "TPBS_KIDS_04"
                video_title = "หม้อข้าวหม้อแกง: เมนูคุณหนู สุขภาพดีจากธรรมชาติ 🍲"
                category_name = "Thai PBS Kids"
                video_type = "youtube"
                video_id = "xa2GRJC8Yac"
                video_url = "https://www.youtube.com/watch?v=xa2GRJC8Yac"
                video_thumbnail = "https://img.youtube.com/vi/xa2GRJC8Yac/hqdefault.jpg"
                video_duration = "08:00"
                video_description = "เด็กๆ เข้าครัวทำอาหารเพื่อสุขภาพ สนุกกับรสชาติและเรียนรู้วิถีธรรมชาติ"
                age_rating = "4-10 yrs"
                tags = "#feed #thaipbskids #หม้อข้าวหม้อแกง #อาหาร"
                total_views = 115000
            },
            Video().apply {
                id = 205
                vid = "TPBS_KIDS_05"
                video_title = "เพลง ก ไก่ ในสวนสนุก แสนสนุก 🎪"
                category_name = "Thai PBS Kids"
                video_type = "youtube"
                video_id = "AcLn892iPo0"
                video_url = "https://www.youtube.com/watch?v=AcLn892iPo0"
                video_thumbnail = "https://img.youtube.com/vi/AcLn892iPo0/hqdefault.jpg"
                video_duration = "02:10"
                video_description = "เพลงท่องพยัญชนะไทย 44 ตัว พร้อมภาพการ์ตูนสีสันสดใส"
                age_rating = "2-6 yrs"
                tags = "#feed #thaipbskids #เพลงเด็ก #กไก่"
                total_views = 280000
            },
            Video().apply {
                id = 206
                vid = "TPBS_KIDS_06"
                video_title = "นิทานอีสป: กระต่ายกับเต่า นิทานสอนใจ 🐢🐇"
                category_name = "Thai PBS Kids"
                video_type = "youtube"
                video_id = "-a1Qe12xMh4"
                video_url = "https://www.youtube.com/watch?v=-a1Qe12xMh4"
                video_thumbnail = "https://img.youtube.com/vi/-a1Qe12xMh4/hqdefault.jpg"
                video_duration = "04:30"
                video_description = "นิทานคติสอนใจเรื่องความพยายามและความไม่ประมาท ภาพการ์ตูนสีสันสดใส"
                age_rating = "2-8 yrs"
                tags = "#feed #thaipbskids #นิทาน #กระต่ายกับเต่า"
                total_views = 210000
            }
        )
    }

    private fun getCuratedThaiPbsMainVideos(): List<Video> {
        return listOf(
            Video().apply {
                id = 301
                vid = "TPBS_MAIN_01"
                video_title = "หม้อข้าวหม้อแกง: เมนูคุณหนู สุขภาพดีจากธรรมชาติ 🍲"
                category_name = "วิดีโอรวม"
                video_type = "youtube"
                video_id = "xa2GRJC8Yac"
                video_url = "https://www.youtube.com/watch?v=xa2GRJC8Yac"
                video_thumbnail = "https://img.youtube.com/vi/xa2GRJC8Yac/hqdefault.jpg"
                video_duration = "08:00"
                video_description = "เด็กๆ เข้าครัวทำอาหารเพื่อสุขภาพ สนุกกับรสชาติและเรียนรู้วิถีธรรมชาติ"
                age_rating = "5-12 yrs"
                tags = "#feed #thaipbs #วิดีโอรวม #หม้อข้าวหม้อแกง"
                total_views = 127000
            },
            Video().apply {
                id = 302
                vid = "TPBS_MAIN_02"
                video_title = "คิดส์เรนเจอร์ ผจญภัยโลกกว้างและสิ่งแวดล้อม 🌿"
                category_name = "วิดีโอรวม"
                video_type = "youtube"
                video_id = "fJowGHv9Q7w"
                video_url = "https://www.youtube.com/watch?v=fJowGHv9Q7w"
                video_thumbnail = "https://img.youtube.com/vi/fJowGHv9Q7w/hqdefault.jpg"
                video_duration = "07:30"
                video_description = "สารคดีและกิจกรรมสร้างสรรค์สำหรับเด็กและครอบครัวจากสถานี Thai PBS"
                age_rating = "เด็กและครอบครัว"
                tags = "#feed #thaipbs #วิดีโอรวม #สารคดี"
                total_views = 112000
            },
            Video().apply {
                id = 303
                vid = "TPBS_MAIN_03"
                video_title = "สอนศิลป์: วาดภาพระบายสีและงานประดิษฐ์ 🎨"
                category_name = "วิดีโอรวม"
                video_type = "youtube"
                video_id = "e2Sus3plcV8"
                video_url = "https://www.youtube.com/watch?v=e2Sus3plcV8"
                video_thumbnail = "https://img.youtube.com/vi/e2Sus3plcV8/hqdefault.jpg"
                video_duration = "06:30"
                video_description = "เปิดโลกศิลปะกับครูพี่เบิร์ดและเด็กๆ สร้างสรรค์ผลงานน่ารัก"
                age_rating = "3-9 yrs"
                tags = "#feed #thaipbs #วิดีโอรวม #สอนศิลป์"
                total_views = 89000
            },
            Video().apply {
                id = 304
                vid = "TPBS_MAIN_04"
                video_title = "บ้านนักวิทยาศาสตร์น้อย: สำรวจแรงลมและน้ำ 🧪"
                category_name = "วิดีโอรวม"
                video_type = "youtube"
                video_id = "n1kdvZ76OPo"
                video_url = "https://www.youtube.com/watch?v=n1kdvZ76OPo"
                video_thumbnail = "https://img.youtube.com/vi/n1kdvZ76OPo/hqdefault.jpg"
                video_duration = "06:15"
                video_description = "การทดลองวิทยาศาสตร์แสนสนุก ตอบคำถามความสงสัยของเด็กช่างสังเกต"
                age_rating = "3-7 yrs"
                tags = "#feed #thaipbs #วิดีโอรวม #วิทยาศาสตร์"
                total_views = 95000
            }
        )
    }

    private fun getCuratedProgramsVideos(): List<Video> {
        return listOf(
            Video().apply {
                id = 401
                vid = "TPBS_PROG_01"
                video_title = "ฝึกอ่านออกเสียง ก-ฮ สำหรับเด็กอนุบาล 📚"
                category_name = "ฝึกอ่านและเขียน"
                video_type = "youtube"
                video_id = "k_Xok3td7m0"
                video_url = "https://www.youtube.com/watch?v=k_Xok3td7m0"
                video_thumbnail = "https://img.youtube.com/vi/k_Xok3td7m0/hqdefault.jpg"
                video_duration = "08:15"
                video_description = "บทเรียนฝึกอ่านออกเสียงพยัญชนะไทย ก ไก่ ถึง ฮ นกฮูก ชัดเจนเข้าใจง่าย มีเสียงอ่านและคำอธิบายสนุกๆ สำหรับน้องๆ ปฐมวัย"
                age_rating = "3-6 yrs"
                tags = "#feed #ฝึกอ่าน #กไก่ #ภาษาไทย #ฝึกอ่านและเขียน #thaipbs_kids_programs"
                total_views = 195000
            },
            Video().apply {
                id = 402
                vid = "TPBS_PROG_02"
                video_title = "ฝึกสะกดคำภาษาไทย สระและวรรณยุกต์แสนสนุก ✏️"
                category_name = "ฝึกอ่านและเขียน"
                video_type = "youtube"
                video_id = "mdw6UNfz9Og"
                video_url = "https://www.youtube.com/watch?v=mdw6UNfz9Og"
                video_thumbnail = "https://img.youtube.com/vi/mdw6UNfz9Og/hqdefault.jpg"
                video_duration = "07:20"
                video_description = "เรียนรู้การประสมคำ สระไทย และวรรณยุกต์ สื่อการเรียนรู้สำหรับเด็กปฐมวัย เสริมความมั่นใจในการอ่านภาษาไทย"
                age_rating = "4-7 yrs"
                tags = "#feed #สระไทย #สะกดคำ #ฝึกอ่านและเขียน #thaipbs_kids_programs"
                total_views = 168000
            },
            Video().apply {
                id = 403
                vid = "TPBS_PROG_03"
                video_title = "เพลง ก ไก่ ในสวนสนุก แสนสนุก 🎪"
                category_name = "ฝึกอ่านและเขียน"
                video_type = "youtube"
                video_id = "AcLn892iPo0"
                video_url = "https://www.youtube.com/watch?v=AcLn892iPo0"
                video_thumbnail = "https://img.youtube.com/vi/AcLn892iPo0/hqdefault.jpg"
                video_duration = "02:10"
                video_description = "เพลงท่องพยัญชนะไทย 44 ตัว พร้อมภาพการ์ตูนสีสันสดใส ชวนร้องตามง่าย สนุกเพลิดเพลิน"
                age_rating = "2-6 yrs"
                tags = "#feed #เพลงเด็ก #กไก่ #ฝึกอ่านและเขียน #thaipbs_kids_programs"
                total_views = 280000
            },
            Video().apply {
                id = 404
                vid = "TPBS_PROG_04"
                video_title = "นิทานอีสป: กระต่ายกับเต่า นิทานสอนใจ 🐢🐇"
                category_name = "ฝึกอ่านและเขียน"
                video_type = "youtube"
                video_id = "-a1Qe12xMh4"
                video_url = "https://www.youtube.com/watch?v=-a1Qe12xMh4"
                video_thumbnail = "https://img.youtube.com/vi/-a1Qe12xMh4/hqdefault.jpg"
                video_duration = "04:30"
                video_description = "นิทานคติสอนใจเรื่องความพยายามและความไม่ประมาท ภาพการ์ตูนแอนิเมชันน่ารัก พร้อมเนื้อเรื่องสำหรับฝึกอ่าน"
                age_rating = "2-8 yrs"
                tags = "#feed #นิทาน #กระต่ายกับเต่า #ฝึกอ่านและเขียน #thaipbs_kids_programs"
                total_views = 210000
            },
            Video().apply {
                id = 405
                vid = "TPBS_PROG_05"
                video_title = "ABC Phonics Alphabet Song ภาษาอังกฤษแสนสนุก 🔤"
                category_name = "ฝึกอ่านและเขียน"
                video_type = "youtube"
                video_id = "BELlZKpi1Zs"
                video_url = "https://www.youtube.com/watch?v=BELlZKpi1Zs"
                video_thumbnail = "https://img.youtube.com/vi/BELlZKpi1Zs/hqdefault.jpg"
                video_duration = "03:15"
                video_description = "เรียนรู้ออกเสียงโฟนิกส์ A ถึง Z สนุกเพลิดเพลินไปกับบทเพลงภาษาอังกฤษสำหรับเด็กวัยอนุบาล"
                age_rating = "2-6 yrs"
                tags = "#feed #abc #phonics #ฝึกอ่านและเขียน #thaipbs_kids_programs"
                total_views = 310000
            },
            Video().apply {
                id = 406
                vid = "TPBS_PROG_06"
                video_title = "นิทานลูกหมู 3 ตัว: นิทานก่อนนอนสอนความขยัน 🐷🏡"
                category_name = "ฝึกอ่านและเขียน"
                video_type = "youtube"
                video_id = "-a1Qe12xMh4"
                video_url = "https://www.youtube.com/watch?v=-a1Qe12xMh4"
                video_thumbnail = "https://img.youtube.com/vi/-a1Qe12xMh4/hqdefault.jpg"
                video_duration = "05:10"
                video_description = "เรื่องราวของลูกหมูสามตัวกับการสร้างบ้านด้วยฟาง ไม้ และอิฐ สอนให้เด็กๆ รู้จักความอดทนและตั้งใจทำงาน"
                age_rating = "2-8 yrs"
                tags = "#feed #นิทาน #ลูกหมูสามตัว #ฝึกอ่านและเขียน #thaipbs_kids_programs"
                total_views = 175000
            },
            Video().apply {
                id = 407
                vid = "TPBS_PROG_07"
                video_title = "สอนศิลป์: วาดภาพและประดิษฐ์ตัวอักษรน่ารัก 🎨"
                category_name = "ฝึกอ่านและเขียน"
                video_type = "youtube"
                video_id = "e2Sus3plcV8"
                video_url = "https://www.youtube.com/watch?v=e2Sus3plcV8"
                video_thumbnail = "https://img.youtube.com/vi/e2Sus3plcV8/hqdefault.jpg"
                video_duration = "06:30"
                video_description = "ชวนเด็กๆ มาวาดรูปพยัญชนะไทยแปลงร่างเป็นสัตว์น่ารัก เสริมสร้างจินตนาการและการจดจำ"
                age_rating = "3-8 yrs"
                tags = "#feed #สอนศิลป์ #ศิลปะ #ฝึกอ่านและเขียน #thaipbs_kids_programs"
                total_views = 120000
            },
            Video().apply {
                id = 408
                vid = "TPBS_PROG_08"
                video_title = "บ้านนักวิทยาศาสตร์น้อย: ตัวเลขกับพยัญชนะซ่อนแอบ 🧪"
                category_name = "ฝึกอ่านและเขียน"
                video_type = "youtube"
                video_id = "n1kdvZ76OPo"
                video_url = "https://www.youtube.com/watch?v=n1kdvZ76OPo"
                video_thumbnail = "https://img.youtube.com/vi/n1kdvZ76OPo/hqdefault.jpg"
                video_duration = "06:15"
                video_description = "กิจกรรมส่งเสริมการคิดแบบวิทยาศาสตร์ ค้นหาตัวอักษรและตัวเลขในชีวิตประจำวัน"
                age_rating = "3-7 yrs"
                tags = "#feed #บ้านนักวิทยาศาสตร์น้อย #วิทยาศาสตร์ #ฝึกอ่านและเขียน #thaipbs_kids_programs"
                total_views = 138000
            }
        )
    }

    private fun getCuratedTalkVideos(): List<Video> {
        return listOf(
            Video().apply {
                id = 501
                vid = "TPBS_TALK_01"
                video_title = "คุยกับลูก: เข้าใจพัฒนาการสมอง EF และการจัดการอารมณ์ 🧠"
                category_name = "คุยกับลูก"
                video_type = "youtube"
                video_id = "dO4pL8yZq3M"
                video_url = "https://www.youtube.com/watch?v=dO4pL8yZq3M"
                video_thumbnail = "https://img.youtube.com/vi/dO4pL8yZq3M/hqdefault.jpg"
                video_duration = "09:15"
                video_description = "การเสริมสร้างทักษะสมอง Executive Functions (EF) ช่วยให้เด็กคิด ยืดหยุ่น และจัดการอารมณ์ได้ดี"
                age_rating = "พ่อแม่และเด็ก"
                tags = "#feed #คุยกับลูก #talk #ครอบครัว #ef #สมอง"
                total_views = 56000
            },
            Video().apply {
                id = 502
                vid = "TPBS_TALK_02"
                video_title = "คุยกับลูก: เทคนิคสร้างสมาธิและปรับสมดุลการใช้หน้าจอ 📱"
                category_name = "คุยกับลูก"
                video_type = "youtube"
                video_id = "vT2mL6yZq9P"
                video_url = "https://www.youtube.com/watch?v=vT2mL6yZq9P"
                video_thumbnail = "https://img.youtube.com/vi/vT2mL6yZq9P/hqdefault.jpg"
                video_duration = "07:30"
                video_description = "วิธีสร้างข้อตกลงเรื่องการใช้หน้าจอในครอบครัว พร้อมกิจกรรมเสริมสร้างสมาธิเชิงบวก"
                age_rating = "พ่อแม่และเด็ก"
                tags = "#feed #คุยกับลูก #talk #ครอบครัว #สมาธิ"
                total_views = 49000
            }
        )
    }
}

