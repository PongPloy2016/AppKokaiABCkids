package com.pongploydev.newsfeature.data

import android.text.Html
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern
import kotlin.math.abs

/**
 * Parser สำหรับ RSS ข่าว (รองรับ Thai PBS, VOA Thai, TMD กรมอุตุฯ, NRIIS วิจัย, Sanook, Google News)
 */
object RssParser {

    private val BANGKOK = TimeZone.getTimeZone("Asia/Bangkok")
    private val GMT = TimeZone.getTimeZone("GMT")
    private const val RELATED_MARKER = "อ่านข่าว"
    private val IMG_SRC_PATTERN = Pattern.compile("""src=["'](https?://[^"']+)["']""", Pattern.CASE_INSENSITIVE)
    private val TMD_IMG_PATTERN = Pattern.compile("""\[image\](.*?)\[/image\]""", Pattern.CASE_INSENSITIVE)

    // คัดกรองข่าวผู้ใหญ่ การเมือง ธุรกิจ อาชญากรรม และเนื้อหาที่ไม่เหมาะสม เพื่อให้ฟีดปลอดภัยสำหรับเด็ก 100%
    private val UNSAFE_AND_ADULT_KEYWORDS = listOf(
        // ความรุนแรง / อาชญากรรม / สงคราม
        "เสียชีวิต", "ศพ", "ฆ่า", "ยิงกัน", "ดับคา", "สังหาร", "ข่มขืน", "ชำเรา", "ยาเสพติด", "อาชญากรรม",
        "คดีฆ่า", "สงคราม", "ขีปนาวุธ", "กองทัพ", "ระเบิด", "ผู้ก่อการร้าย", "อาวุธ", "ยิงถล่ม", "สังหารหมู่",

        // การเมือง / รัฐบาล / ข่าวผู้ใหญ่
        "การเมือง", "รัฐบาล", "นายกรัฐมนตรี", "นายกฯ", "ครม", "พรรค", "เลือกตั้ง", "สภาผู้แทน", "วุฒิสภา",
        "ส.ส.", "ส.ว.", "ทำเนียบขาว", "ทำเนียบ", "นโยบายการเงิน", "นโยบายต่างประเทศ", "กกต", "ยุบสภา",
        "ศาลรัฐธรรมนูญ", "ประท้วง", "ม็อบ", "รัฐประหาร", "ทรัมป์", "ไบเดน", "ปูติน", "มัสก์", "อีลอน",
        "รัสเซีย", "ยูเครน", "อิสราเอล", "กาซา", "สงครามการค้า",

        // เศรษฐกิจผู้ใหญ่ / หุ้น / ตลาดเงิน
        "ตลาดหุ้น", "หุ้น", "ดอกเบี้ย", "เงินเฟ้อ", "คริปโต", "บิตคอยน์", "ล้มละลาย", "หนี้สิน", "ภาษี",
        "ผู้ผลิตรถ", "ค่ายรถยนต์", "วอลล์สตรีท", "นักลงทุน", "ฟองสบู่", "เศรษฐกิจชะลอ",

        // อุบัติเหตุร้ายแรง
        "ชนสนั่น", "รถคว่ำ", "จมน้ำดับ", "บาดเจ็บสาหัส", "ไฟคลอก", "รถชนดับ", "ตกตึก",

        // ดราม่า / ข่าวฉาวผู้ใหญ่
        "เตียงหัก", "เลิกรา", "แฟนเก่า", "แฉ", "ดราม่า", "ไฮโซ", "ดาราสาว", "เซ็กซี่", "นุ่งสั้น", "บิกินี่",
        "ส่วย", "บ่อน", "พนัน", "ฟอกเงิน", "สแกมเมอร์", "หลอกโอนเงิน", "แชร์ลูกโซ่"
    )

    // คำที่ใช้กรองสำหรับข่าวทั่วไป ให้เน้นเฉพาะเรื่องเด็กและการเรียนรู้
    private val KIDS_POSITIVE_TOPICS = listOf(
        "สัตว์", "วิทยาศาสตร์", "สิ่งแวดล้อม", "การศึกษา", "กีฬา", "ธรรมชาติ",
        "เด็ก", "เยาวชน", "นวัตกรรม", "อวกาศ", "สุขภาพ", "โรงเรียน", "เรียนรู้",
        "ศิลปะ", "วัฒนธรรม", "เทคโนโลยี", "อาหาร", "ปัญญาประดิษฐ์", "AI", "ดาราศาสตร์",
        "เกษตร", "อ่าน", "หนังสือ", "วิจัย", "จักรวาล", "พยากรณ์อากาศ",
        "นิทาน", "สุภาษิต", "พอดแคสต์", "เสียง", "เล่าเรื่อง", "เรื่องเล่า",
        "ไดโนเสาร์", "ของเล่น", "การทดลอง", "วาดภาพ", "ระบายสี", "ดนตรี", "นักเรียน"
    )

    // คลังภาพประกอบการศึกษาคุณภาพสูง สำหรับข่าวที่ไม่มีภาพแนบมา
    private val THEME_IMAGES = mapOf(
        "video" to listOf(
            "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1578374173705-969cbe6f2d6b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1507838153414-b4b713384a76?auto=format&fit=crop&w=800&q=80"
        ),
        "podcast" to listOf(
            "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1532012164546-f432f2e37b73?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1578374173705-969cbe6f2d6b?auto=format&fit=crop&w=800&q=80"
        ),
        "health" to listOf(
            "https://images.unsplash.com/photo-1498837167922-ddd27525d352?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1505751172876-fa1923c5c528?auto=format&fit=crop&w=800&q=80"
        ),
        "arts" to listOf(
            "https://images.unsplash.com/photo-1513364776144-60967b0f800f?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1507838153414-b4b713384a76?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1502086223501-7ea6ecd79368?auto=format&fit=crop&w=800&q=80"
        ),
        "books" to listOf(
            "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=800&q=80"
        ),
        "school" to listOf(
            "https://images.unsplash.com/photo-1577896851231-70ef18881754?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1509062522246-3755977927d7?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=800&q=80"
        ),
        "agriculture" to listOf(
            "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1464226184884-fa280b87c399?auto=format&fit=crop&w=800&q=80"
        ),
        "weather" to listOf(
            "https://images.unsplash.com/photo-1534088568595-a066f410bcda?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1515694346937-94d85e41e6f0?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1504608524841-42fe6f032b4b?auto=format&fit=crop&w=800&q=80"
        ),
        "general" to listOf(
            "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?auto=format&fit=crop&w=800&q=80"
        )
    )

    fun parse(stream: InputStream, filterForKids: Boolean = false): List<NewsArticle> {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        val dateFormats = listOf(
            SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", Locale.US).apply { timeZone = GMT },
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US).apply { timeZone = GMT },
            SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US).apply { timeZone = GMT },
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).apply { timeZone = GMT },
            SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.US).apply { timeZone = BANGKOK },
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US).apply { timeZone = BANGKOK },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = GMT },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
            SimpleDateFormat("d/M/yyyy HH:mm:ss", Locale.US).apply { timeZone = BANGKOK },
            SimpleDateFormat("d/M/yyyy H:m:s", Locale.US).apply { timeZone = BANGKOK }
        )

        val articles = ArrayList<NewsArticle>()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && (parser.name.equals("item", ignoreCase = true) || parser.name.equals("entry", ignoreCase = true))) {
                readItem(parser, dateFormats, filterForKids)?.let { articles.add(it) }
            }
            event = parser.next()
        }
        return articles.distinctBy { it.id }
    }

    private fun readItem(
        parser: XmlPullParser,
        dateFormats: List<SimpleDateFormat>,
        filterForKids: Boolean
    ): NewsArticle? {
        var id = ""
        var title = ""
        var link = ""
        var description = ""
        var category = ""
        var source = ""
        var imageUrl: String? = null
        var pubDate = ""
        var comments = ""
        var guid = ""
        var videoUrl = ""
        var audioUrl: String? = null

        while (true) {
            val event = parser.next()
            if (event == XmlPullParser.END_DOCUMENT) break
            if (event == XmlPullParser.END_TAG && (parser.name.equals("item", ignoreCase = true) || parser.name.equals("entry", ignoreCase = true))) break
            if (event != XmlPullParser.START_TAG) continue

            when (parser.name.lowercase(Locale.ROOT)) {
                "media:group" -> {
                    // ไม่ข้าม media:group เพื่อให้เข้าถึง media:thumbnail และ media:description ของ YouTube
                }
                "id" -> {
                    val idText = try { parser.nextText().trim() } catch (_: Exception) { "" }
                    if (id.isEmpty()) id = idText
                }
                "yt:videoid" -> {
                    val vId = try { parser.nextText().trim() } catch (_: Exception) { "" }
                    if (vId.isNotEmpty()) {
                        id = vId
                        if (videoUrl.isEmpty()) videoUrl = "https://www.youtube.com/watch?v=$vId"
                        if (link.isEmpty()) link = "https://www.youtube.com/watch?v=$vId"
                    }
                }
                "title", "media:title" -> {
                    val t = try { parser.nextText().trim() } catch (_: Exception) { "" }
                    if (t.isNotEmpty() && title.isEmpty()) title = t
                }
                "link" -> {
                    val href = parser.getAttributeValue(null, "href")?.trim().orEmpty()
                    val txt = try { parser.nextText().trim() } catch (_: Exception) { "" }
                    val finalLink = href.ifEmpty { txt }
                    if (finalLink.isNotEmpty() && link.isEmpty()) {
                        link = finalLink
                    }
                    if (finalLink.contains("youtube.com/watch") && videoUrl.isEmpty()) {
                        videoUrl = finalLink
                    }
                }
                "description", "media:description", "content" -> {
                    val desc = try { parser.nextText() } catch (_: Exception) { "" }
                    if (desc.isNotEmpty() && description.isEmpty()) {
                        description = desc
                    }
                }
                "category" -> category = try { parser.nextText().trim() } catch (_: Exception) { "" }
                "source" -> source = try { parser.nextText().trim() } catch (_: Exception) { "" }
                "pubdate", "published", "updated" -> {
                    val d = try { parser.nextText().trim() } catch (_: Exception) { "" }
                    if (d.isNotEmpty() && pubDate.isEmpty()) pubDate = d
                }
                "comments" -> comments = try { parser.nextText() } catch (_: Exception) { "" }
                "guid" -> guid = try { parser.nextText().trim() } catch (_: Exception) { "" }
                "video_url" -> videoUrl = try { parser.nextText().trim() } catch (_: Exception) { "" }
                "image", "itunes:image" -> {
                    val attrHref = parser.getAttributeValue(null, "href")?.trim()
                    val imgText = try { parser.nextText().trim() } catch (_: Exception) { "" }
                    val foundImage = attrHref?.takeIf { it.isNotEmpty() } ?: imgText.takeIf { it.isNotEmpty() }
                    if (!foundImage.isNullOrEmpty() && imageUrl == null) {
                        imageUrl = foundImage
                    }
                }
                "media:thumbnail", "thumbnail" -> {
                    val thumbUrl = parser.getAttributeValue(null, "url")?.trim()
                    if (!thumbUrl.isNullOrEmpty() && imageUrl == null) {
                        imageUrl = thumbUrl
                    }
                }
                "itunes:summary" -> {
                    val itunesSum = try { parser.nextText().trim() } catch (_: Exception) { "" }
                    if (itunesSum.isNotEmpty() && (comments.isEmpty() || description.isEmpty())) {
                        comments = itunesSum
                        if (description.isEmpty()) description = itunesSum
                    }
                }
                "enclosure" -> {
                    val attrUrl = parser.getAttributeValue(null, "url")?.trim().orEmpty()
                    val attrType = parser.getAttributeValue(null, "type")?.trim().orEmpty().lowercase(Locale.ROOT)
                    val textUrl = try { parser.nextText().trim() } catch (_: Exception) { "" }
                    val finalUrl = attrUrl.ifEmpty { textUrl }
                    if (finalUrl.isNotEmpty()) {
                        val isAudio = attrType.startsWith("audio/") ||
                            finalUrl.endsWith(".mp3", ignoreCase = true) ||
                            finalUrl.endsWith(".wav", ignoreCase = true) ||
                            finalUrl.endsWith(".m4a", ignoreCase = true) ||
                            finalUrl.endsWith(".aac", ignoreCase = true) ||
                            finalUrl.endsWith(".ogg", ignoreCase = true)
                        val isImage = attrType.startsWith("image/") ||
                            finalUrl.endsWith(".jpg", ignoreCase = true) ||
                            finalUrl.endsWith(".jpeg", ignoreCase = true) ||
                            finalUrl.endsWith(".png", ignoreCase = true) ||
                            finalUrl.endsWith(".webp", ignoreCase = true)

                        if (isAudio) {
                            if (audioUrl == null) audioUrl = finalUrl
                        } else if (isImage) {
                            if (imageUrl == null) imageUrl = finalUrl
                        } else if (imageUrl == null) {
                            imageUrl = finalUrl
                        }
                    }
                }
                else -> skip(parser)
            }
        }

        var plainTitle = htmlToPlain(title)
        if (plainTitle.isEmpty()) return null
        val fullContent = "$plainTitle $description $category"

        // 1. ตรวจสอบคำต้องห้ามสำหรับเด็ก (ทั้งในหัวข้อและเนื้อหา)
        if (UNSAFE_AND_ADULT_KEYWORDS.any { fullContent.contains(it) }) return null

        // 2. ถ้าต้องกรองสำหรับเด็ก (filterForKids) ให้ผ่านเฉพาะเรื่องที่เกี่ยวกับเด็ก/การเรียนรู้
        if (filterForKids) {
            val matchesKidsTopic = KIDS_POSITIVE_TOPICS.any { fullContent.contains(it) }
            if (!matchesKidsTopic) return null
        }

        // ตัดชื่อสำนักข่าวที่ Google News ต่อท้ายออก
        if (source.isNotEmpty() && plainTitle.endsWith(" - $source")) {
            plainTitle = plainTitle.removeSuffix(" - $source").trim()
        }

        val paragraphs = extractParagraphs(description)
        val summary = htmlToPlain(comments).ifEmpty {
            paragraphs.firstOrNull() ?: htmlToPlain(description)
        }
        val articleLink = link.ifEmpty { guid }
        val finalCategory = category.ifEmpty { source.ifEmpty { if (videoUrl.isNotEmpty()) "วิดีโอเด็ก" else "ข่าวสารเด็ก" } }

        // ดึงภาพจาก description หรือ TMD tag ถ้าไม่มี enclosure
        if (imageUrl.isNullOrBlank()) {
            val tmdMatcher = TMD_IMG_PATTERN.matcher(description)
            if (tmdMatcher.find()) {
                val path = tmdMatcher.group(1).trim()
                imageUrl = "https://www.tmd.go.th/media/$path"
            } else {
                val matcher = IMG_SRC_PATTERN.matcher(description)
                if (matcher.find()) {
                    imageUrl = matcher.group(1)
                }
            }
        }

        // หากยังไม่มีภาพ กำหนดภาพประกอบคุณภาพสูงตามธีมหัวข้อ
        if (imageUrl.isNullOrBlank()) {
            imageUrl = pickThemedImage(plainTitle, finalCategory)
        }

        return NewsArticle(
            id = id.ifEmpty { articleLink.ifEmpty { plainTitle } },
            title = plainTitle,
            link = articleLink,
            summary = summary,
            paragraphs = paragraphs,
            category = finalCategory,
            imageUrl = imageUrl?.takeIf { it.startsWith("http") },
            publishedAtMillis = parseDate(pubDate, dateFormats),
            videoUrl = videoUrl.takeIf { it.startsWith("http") },
            audioUrl = audioUrl?.takeIf { it.startsWith("http") }
        )
    }

    private fun pickThemedImage(title: String, category: String): String {
        val text = "$title $category"
        val list = when {
            text.contains("วิดีโอ") || text.contains("youtube") || text.contains("คลิป") ->
                THEME_IMAGES["video"]
            text.contains("นิทาน") || text.contains("สุภาษิต") || text.contains("เสียง") || text.contains("podcast") || text.contains("เรื่องเล่า") ->
                THEME_IMAGES["podcast"]
            text.contains("อากาศ") || text.contains("ฝน") || text.contains("พายุ") || text.contains("อุตุนิยมวิทยา") ->
                THEME_IMAGES["weather"]
            text.contains("สุขภาพ") || text.contains("โภชนาการ") || text.contains("ออกกำลังกาย") || text.contains("วัคซีน") || text.contains("แพทย์") ->
                THEME_IMAGES["health"]
            text.contains("ศิลปะ") || text.contains("ดนตรี") || text.contains("สร้างสรรค์") || text.contains("วาด") || text.contains("เพลง") ->
                THEME_IMAGES["arts"]
            text.contains("หนังสือ") || text.contains("อ่าน") || text.contains("ห้องสมุด") || text.contains("วรรณกรรม") ->
                THEME_IMAGES["books"]
            text.contains("เยาวชน") || text.contains("โรงเรียน") || text.contains("นักเรียน") || text.contains("เด็กไทย") ->
                THEME_IMAGES["school"]
            text.contains("เกษตร") || text.contains("อาหาร") || text.contains("ชุมชน") || text.contains("ผัก") ->
                THEME_IMAGES["agriculture"]
            else -> THEME_IMAGES["general"]
        } ?: THEME_IMAGES["general"]!!

        val index = abs(title.hashCode()) % list.size
        return list[index]
    }

    private fun parseDate(value: String, formats: List<SimpleDateFormat>): Long {
        if (value.isEmpty()) return 0L
        for (format in formats) {
            try {
                val date = format.parse(value) ?: continue
                val cal = Calendar.getInstance().apply { time = date }
                // ถ้าเป็น พ.ศ. (เกินปี 2400) ให้แปลงเป็น ค.ศ.
                if (cal.get(Calendar.YEAR) > 2400) {
                    cal.add(Calendar.YEAR, -543)
                }
                return cal.timeInMillis
            } catch (_: Exception) {
            }
        }
        return 0L
    }

    private fun extractParagraphs(html: String): List<String> {
        if (html.isBlank()) return emptyList()
        val result = ArrayList<String>()
        for (chunk in html.split(Regex("(?i)</p>|<br\\s*/?>"))) {
            val text = htmlToPlain(chunk)
            if (text.isEmpty()) continue
            if (text.startsWith(RELATED_MARKER)) break
            // ข้าม tag [image]...[/image] ของ TMD
            if (text.startsWith("[image]")) continue
            result.add(text)
        }
        return result
    }

    @Suppress("DEPRECATION")
    private fun htmlToPlain(html: String): String {
        if (html.isEmpty()) return ""
        val spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(html)
        }
        return spanned.toString()
            .replace('\uFFFC', ' ')
            .replace('\u00A0', ' ')
            .trim()
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) return
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }
}
