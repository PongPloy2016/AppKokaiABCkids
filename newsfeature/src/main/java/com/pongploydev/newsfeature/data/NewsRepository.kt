package com.pongploydev.newsfeature.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * จัดการข้อมูลฟีดข่าว เชื่อมต่อกับ Supabase ตาราง kids_news_feeds แบบ Realtime
 * พร้อมระบบ Cache และ Fallback ไปที่ค่าเริ่มต้นเมื่อออฟไลน์
 */
object NewsRepository {

    const val SUPABASE_URL = "https://qriqerrkxccpnwabphdc.supabase.co"
    const val SUPABASE_ANON_KEY = "sb_publishable_4pbBttafKQoR_Bj78vtARA_eDMkRe66"

    private const val CACHE_TTL_MS = 5 * 60 * 1000L
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 15_000

    private class Cached(val fetchedAt: Long, val items: List<NewsArticle>)

    private val cache = ConcurrentHashMap<String, Cached>()
    private var cachedFeeds: List<NewsFeedItem>? = null
    private var feedsFetchedAt: Long = 0L

    fun cached(feed: NewsFeedItem): List<NewsArticle>? = cache[feed.id]?.items

    fun isFresh(feed: NewsFeedItem): Boolean {
        val entry = cache[feed.id] ?: return false
        return System.currentTimeMillis() - entry.fetchedAt < CACHE_TTL_MS
    }

    /**
     * ดึงรายการฟีดจากตาราง kids_news_feeds บน Supabase
     * หากออฟไลน์หรือดึงไม่ได้ จะ fallback ไปที่ NewsFeedItem.DEFAULT_FEEDS อัตโนมัติ
     */
    suspend fun getFeeds(force: Boolean = false): List<NewsFeedItem> {
        if (!force && cachedFeeds != null && System.currentTimeMillis() - feedsFetchedAt < CACHE_TTL_MS) {
            return cachedFeeds!!
        }

        return withContext(Dispatchers.IO) {
            try {
                val feeds = fetchFeedsFromSupabase()
                if (feeds.isNotEmpty()) {
                    cachedFeeds = feeds
                    feedsFetchedAt = System.currentTimeMillis()
                    feeds
                } else {
                    cachedFeeds ?: NewsFeedItem.DEFAULT_FEEDS
                }
            } catch (_: Exception) {
                cachedFeeds ?: NewsFeedItem.DEFAULT_FEEDS
            }
        }
    }

    private fun fetchFeedsFromSupabase(): List<NewsFeedItem> {
        val result = mutableListOf<NewsFeedItem>()
        // 1. ดึงฟีดข่าวสาร นิทาน และบทความ จาก kids_news_feeds
        result.addAll(fetchFromTable("kids_news_feeds"))
        // 2. ดึงฟีดวิดีโอเด็ก จาก kids_video_feeds (ถ้าสร้างไว้ใน Supabase)
        result.addAll(fetchFromTable("kids_video_feeds"))
        return result.distinctBy { it.id }.sortedBy { it.sortOrder }
    }

    private fun fetchFromTable(tableName: String): List<NewsFeedItem> {
        val endpoint = "$SUPABASE_URL/rest/v1/$tableName?select=*&is_active=eq.true&order=sort_order.asc"
        val connection = try {
            URL(endpoint).openConnection() as HttpURLConnection
        } catch (_: Exception) {
            return emptyList()
        }
        try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.setRequestProperty("apikey", SUPABASE_ANON_KEY)
            connection.setRequestProperty("Authorization", "Bearer $SUPABASE_ANON_KEY")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "PaintApp-Android")

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return emptyList()
            }

            val jsonString = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val result = ArrayList<NewsFeedItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id")
                val name = obj.optString("name")
                val feedUrl = obj.optString("feed_url")
                if (id.isNotEmpty() && feedUrl.isNotEmpty()) {
                    result.add(
                        NewsFeedItem(
                            id = id,
                            name = name,
                            icon = obj.optString("icon", "").takeIf { it.isNotEmpty() },
                            feedUrl = feedUrl,
                            provider = obj.optString("provider", ""),
                            category = obj.optString("category", if (tableName.contains("video")) "video" else "general"),
                            description = obj.optString("description", "").takeIf { it.isNotEmpty() },
                            isKidsFilterRequired = obj.optBoolean("is_kids_filter_required", false),
                            sortOrder = obj.optInt("sort_order", i + 1)
                        )
                    )
                }
            }
            return result
        } catch (_: Exception) {
            return emptyList()
        } finally {
            connection.disconnect()
        }
    }

    suspend fun load(feed: NewsFeedItem, force: Boolean = false): Result<List<NewsArticle>> {
        if (!force && isFresh(feed)) {
            val items = cache[feed.id]?.items
            if (items != null) return Result.success(items)
        }
        return withContext(Dispatchers.IO) {
            try {
                val items = download(feed)
                cache[feed.id] = Cached(System.currentTimeMillis(), items)
                Result.success(items)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Result.failure(e)
            }
        }
    }

    private fun download(feed: NewsFeedItem): List<NewsArticle> {
        var urlStr = feed.feedUrl

        // แปลง URL รูปแบบเก่า user=ThaiPBSKids และ user=ThaiPBS ให้เป็น channel_id อัตโนมัติ เพื่อป้องกันข้อผิดพลาด 404 จาก YouTube
        if (urlStr.contains("user=ThaiPBSKids", ignoreCase = true)) {
            urlStr = "https://www.youtube.com/feeds/videos.xml?channel_id=UCkBuvFq-7IcZdIwfG8zrLyg"
        } else if (urlStr.contains("user=ThaiPBS", ignoreCase = true)) {
            urlStr = "https://www.youtube.com/feeds/videos.xml?channel_id=UC5TOFhyb_LxL2VG_Zenhpzw"
        }

        // กรณีเป็นหน้าเว็บแหล่งรวมรายการเด็กที่ไม่ได้เป็น XML โดยตรง ให้ดึงรายการวิดีโอเด็กที่คัดสรรแล้ว
        val isRssOrXml = urlStr.contains(".xml", ignoreCase = true) ||
                urlStr.contains("rss", ignoreCase = true) ||
                urlStr.contains("feeds/videos", ignoreCase = true)

        if (!isRssOrXml) {
            if (urlStr.contains("kids.thaipbs.or.th/programs", ignoreCase = true)) {
                return getCuratedThaiPbsKidsPrograms()
            }
            if (urlStr.contains("TalkAboutKids", ignoreCase = true)) {
                return getCuratedTalkAboutKidsVideos()
            }
        }

        // จัดการ Google News RSS ที่มีตัวอักษรไทย (Encode query อัตโนมัติเพื่อไม่ให้เจอ HTTP 400)
        if (urlStr.contains("news.google.com/rss/search", ignoreCase = true) && !urlStr.contains("%")) {
            try {
                val uri = android.net.Uri.parse(urlStr)
                val q = uri.getQueryParameter("q") ?: ""
                val hl = uri.getQueryParameter("hl") ?: "th"
                val gl = uri.getQueryParameter("gl") ?: "TH"
                val ceid = uri.getQueryParameter("ceid") ?: "TH:th"
                val encodedQ = java.net.URLEncoder.encode(q, "UTF-8")
                urlStr = "https://news.google.com/rss/search?q=$encodedQ&hl=$hl&gl=$gl&ceid=$ceid"
            } catch (_: Exception) {
            }
        }

        val connection = URL(urlStr).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android) PaintApp-News")
            connection.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, */*")
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP $code")
            }
            return connection.inputStream.use { RssParser.parse(it, filterForKids = feed.isKidsFilterRequired) }
        } finally {
            connection.disconnect()
        }
    }

    private fun getCuratedThaiPbsKidsPrograms(): List<NewsArticle> {
        val now = System.currentTimeMillis()
        return listOf(
            NewsArticle(
                id = "thaipbs_prog_little_scientist",
                title = "บ้านนักวิทยาศาสตร์น้อย 🧪 ชวนเด็กๆ ค้นคว้าและทดลอง",
                link = "https://kids.thaipbs.or.th/programs",
                summary = "ส่งเสริมกระบวนการเรียนรู้ทางวิทยาศาสตร์ผ่านการทดลองและการคิดเชิงตรรกะสำหรับเด็กปฐมวัย สนุกและทำตามได้ง่าย",
                paragraphs = listOf(
                    "โครงการบ้านนักวิทยาศาสตร์น้อย ประเทศไทย มุ่งเน้นการสร้างเจตคติที่ดีและทักษะพื้นฐานทางวิทยาศาสตร์",
                    "เด็กๆ จะได้เรียนรู้เรื่องแรง ลม น้ำ และธรรมชาติรอบตัวผ่านกิจกรรมการทดลองที่ปลอดภัยและน่าตื่นเต้น"
                ),
                category = "วิดีโอเด็ก",
                imageUrl = "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=800&auto=format&fit=crop&q=80",
                publishedAtMillis = now - 3600000L * 2,
                videoUrl = "https://www.youtube.com/results?search_query=บ้านนักวิทยาศาสตร์น้อย+ThaiPBSKids"
            ),
            NewsArticle(
                id = "thaipbs_prog_kid_rangers",
                title = "Kid Rangers ปฏิบัติการเด็กช่างคิด 🎒",
                link = "https://kids.thaipbs.or.th/programs",
                summary = "ภารกิจสุดท้าทาย ชวนเหล่าเรนเจอร์ตัวน้อยออกสำรวจธรรมชาติ สัตว์แปลก และสิ่งประดิษฐ์รอบตัว",
                paragraphs = listOf(
                    "Kid Rangers รายการที่จะพาเด็กๆ ออกนอกห้องเรียนไปผจญภัยในโลกกว้าง",
                    "ค้นหาคำตอบทางวิทยาศาสตร์และสิ่งแวดล้อมด้วยการลงมือทำจริงอย่างสนุกสนาน"
                ),
                category = "วิดีโอเด็ก",
                imageUrl = "https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?w=800&auto=format&fit=crop&q=80",
                publishedAtMillis = now - 3600000L * 5,
                videoUrl = "https://www.youtube.com/results?search_query=Kid+Rangers+ThaiPBSKids"
            ),
            NewsArticle(
                id = "thaipbs_prog_art_journey",
                title = "สอนศิลป์ Journey จินตนาการสร้างสรรค์ 🎨",
                link = "https://kids.thaipbs.or.th/programs",
                summary = "ปลดปล่อยพลังสร้างสรรค์ผ่านการวาดภาพ ระบายสี และประดิษฐ์ของเล่นชิ้นเดียวในโลก",
                paragraphs = listOf(
                    "สอนศิลป์ Journey นำโดยพี่โจและผองเพื่อนที่จะมาชวนเด็กๆ ฝึกทักษะศิลปะง่ายๆ",
                    "ใช้วัสดุเหลือใช้รอบบ้านมาสร้างสรรค์ผลงานศิลปะสุดน่ารัก เสริมสร้างสมาธิและกล้ามเนื้อมัดเล็ก"
                ),
                category = "วิดีโอเด็ก",
                imageUrl = "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=800&auto=format&fit=crop&q=80",
                publishedAtMillis = now - 3600000L * 8,
                videoUrl = "https://www.youtube.com/results?search_query=สอนศิลป์+ThaiPBSKids"
            ),
            NewsArticle(
                id = "thaipbs_prog_fun_namnom",
                title = "ขบวนการ Fun น้ำนม ร่างกายสดใสแข็งแรง 🥛",
                link = "https://kids.thaipbs.or.th/programs",
                summary = "เสริมสร้างพัฒนาการรอบด้าน สุขภาพแข็งแรง และจังหวะดนตรีผ่านการเคลื่อนไหวแสนสนุก",
                paragraphs = listOf(
                    "รายการสำหรับเด็กปฐมวัยที่ผสานเสียงเพลง นิทาน และการออกกำลังกายยามเช้า",
                    "ช่วยส่งเสริมพัฒนาการทั้งทางด้านร่างกาย อารมณ์ และสังคมให้เติบโตอย่างสมวัย"
                ),
                category = "วิดีโอเด็ก",
                imageUrl = "https://images.unsplash.com/photo-1485546246426-74dc88dec4d9?w=800&auto=format&fit=crop&q=80",
                publishedAtMillis = now - 3600000L * 12,
                videoUrl = "https://www.youtube.com/results?search_query=ขบวนการ+Fun+น้ำนม+ThaiPBSKids"
            ),
            NewsArticle(
                id = "thaipbs_prog_mor_khao_mor_kaeng",
                title = "หม้อข้าวหม้อแกง อาหารปลอดภัยและภูมิปัญญา 🍲",
                link = "https://kids.thaipbs.or.th/programs",
                summary = "พาเด็กๆ ไปทำความรู้จักวัตถุดิบชุมชน ผักพื้นบ้าน และทำเมนูอร่อยดีต่อสุขภาพ",
                paragraphs = listOf(
                    "การเรียนรู้ผ่านการทำอาหาร เชื่อมโยงเด็กๆ สู่ธรรมชาติและวิถีชีวิตชุมชน",
                    "เรียนรู้ความปลอดภัยในครัว คุณค่าทางโภชนาการ และการแบ่งปันความสุขบนโต๊ะอาหาร"
                ),
                category = "วิดีโอเด็ก",
                imageUrl = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&auto=format&fit=crop&q=80",
                publishedAtMillis = now - 3600000L * 16,
                videoUrl = "https://www.youtube.com/results?search_query=หม้อข้าวหม้อแกง+ThaiPBS"
            )
        )
    }

    private fun getCuratedTalkAboutKidsVideos(): List<NewsArticle> {
        val now = System.currentTimeMillis()
        return listOf(
            NewsArticle(
                id = "thaipbs_talk_ef_brain",
                title = "คุยกับลูก: เข้าใจพัฒนาการสมอง EF และการจัดการอารมณ์ 🧠",
                link = "https://www.thaipbs.or.th/program/TalkAboutKids/videos/all",
                summary = "การฝึกทักษะสมอง Executive Functions (EF) ช่วยให้เด็กคิด ยืดหยุ่น และจัดการอารมณ์ได้อย่างมีประสิทธิภาพ",
                paragraphs = listOf(
                    "Talk About Kids ชวนทำความเข้าใจธรรมชาติการทำงานของสมองเด็กในแต่ละช่วงวัย",
                    "แนวทางการสื่อสารเชิงบวกที่จะช่วยให้ลูกกล้าคิด กล้าตัดสินใจ และมีวุฒิภาวะทางอารมณ์"
                ),
                category = "วิดีโอครอบครัว",
                imageUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=800&auto=format&fit=crop&q=80",
                publishedAtMillis = now - 3600000L * 3,
                videoUrl = "https://www.thaipbs.or.th/program/TalkAboutKids/videos/all"
            ),
            NewsArticle(
                id = "thaipbs_talk_screen_time",
                title = "คุยกับลูก: เทคนิคสร้างสมาธิและปรับสมดุลการใช้หน้าจอ 📱",
                link = "https://www.thaipbs.or.th/program/TalkAboutKids/videos/all",
                summary = "วิธีจัดระเบียบเวลาหน้าจออย่างสร้างสรรค์ ชวนเด็กหากิจกรรมทางเลือกที่สนุกและพัฒนาสมาธิ",
                paragraphs = listOf(
                    "การติดจออาจส่งผลต่อการนอนและสมาธิ แต่เทคโนโลยียังเป็นเครื่องมือการเรียนรู้ที่ดีถ้าใช้อย่างถูกวิธี",
                    "เคล็ดลับจากผู้เชี่ยวชาญในการตั้งกติการ่วมกันในบ้านอย่างเป็นมิตรและได้ผลจริง"
                ),
                category = "วิดีโอครอบครัว",
                imageUrl = "https://images.unsplash.com/photo-1516627145497-ae6968895b74?w=800&auto=format&fit=crop&q=80",
                publishedAtMillis = now - 3600000L * 6,
                videoUrl = "https://www.thaipbs.or.th/program/TalkAboutKids/videos/all"
            ),
            NewsArticle(
                id = "thaipbs_talk_free_play",
                title = "คุยกับลูก: พลังแห่งการเล่นอิสระ (Free Play) ปลูกฝังความคิดสร้างสรรค์ 🧸",
                link = "https://www.thaipbs.or.th/program/TalkAboutKids/videos/all",
                summary = "ให้พื้นที่และเวลาแก่เด็กได้เล่นตามจินตนาการ เสริมทักษะการแก้ปัญหาและการเข้าสังคม",
                paragraphs = listOf(
                    "การเล่นคือหน้าที่สำคัญที่สุดของวัยเด็ก การเปิดโอกาสให้เด็กคิดค้นกติกาการเล่นเองช่วยสร้างความมั่นใจ",
                    "พ่อแม่สามารถเป็นผู้สังเกตการณ์และผู้สนับสนุนที่ดีเพื่อความปลอดภัยและความสุขของลูก"
                ),
                category = "วิดีโอครอบครัว",
                imageUrl = "https://images.unsplash.com/photo-1472162072942-cd5147eb3902?w=800&auto=format&fit=crop&q=80",
                publishedAtMillis = now - 3600000L * 10,
                videoUrl = "https://www.thaipbs.or.th/program/TalkAboutKids/videos/all"
            ),
            NewsArticle(
                id = "thaipbs_talk_nutrition",
                title = "คุยกับลูก: สุขภาวะและโภชนาการที่เหมาะสมสำหรับเด็กวัยเจริญเติบโต 🥦",
                link = "https://www.thaipbs.or.th/program/TalkAboutKids/videos/all",
                summary = "เทคนิคจัดจานอาหารให้หลากหลาย ถูกหลักโภชนาการ และชวนให้เด็กกินผักผลไม้อย่างมีความสุข",
                paragraphs = listOf(
                    "สารอาหารครบ 5 หมู่คือพื้นฐานของการเจริญเติบโตและการทำงานของสมอง",
                    "สร้างทัศนคติที่ดีต่อการรับประทานอาหารโดยไม่บังคับ พร้อมเคล็ดลับชวนลูกเข้าครัวช่วยทำอาหาร"
                ),
                category = "วิดีโอครอบครัว",
                imageUrl = "https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=800&auto=format&fit=crop&q=80",
                publishedAtMillis = now - 3600000L * 15,
                videoUrl = "https://www.thaipbs.or.th/program/TalkAboutKids/videos/all"
            )
        )
    }
}
