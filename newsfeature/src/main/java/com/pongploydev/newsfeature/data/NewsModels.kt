package com.pongploydev.newsfeature.data

import java.net.URLEncoder

/**
 * ข้อมูลฟีดข่าวสารสำหรับเด็ก (ดึงจาก Supabase ตาราง kids_news_feeds แบบ Realtime)
 */
data class NewsFeedItem(
    val id: String,
    val name: String,
    val icon: String? = null,
    val feedUrl: String,
    val provider: String = "",
    val category: String = "general",
    val description: String? = null,
    val isKidsFilterRequired: Boolean = false,
    val sortOrder: Int = 0
) {
    val displayLabel: String
        get() = if (!icon.isNullOrBlank()) "$icon $name" else name

    companion object {
        val DEFAULT_FEEDS = listOf(
            // 1. หมวดนิทานและเรื่องเล่าสำหรับเด็ก (หมวดหลักเริ่มต้นของเด็ก)
            NewsFeedItem(
                id = "thaipbs_kidshour",
                name = "Kids Hour ชั่วโมงนิทาน",
                icon = "🎧",
                feedUrl = "https://podcast.thaipbs.or.th/program-rss.php?id=66",
                provider = "Thai PBS Podcast",
                category = "podcast",
                description = "นิทานภาษาไทยแสนสนุก เสริมสร้างจินตนาการและการฟังคำศัพท์สำหรับเด็ก 3-10 ปี",
                sortOrder = 1
            ),
            NewsFeedItem(
                id = "thaipbs_sunshine",
                name = "พระอาทิตย์ยิ้มแฉ่ง นิทานเด็ก",
                icon = "☀️",
                feedUrl = "https://podcast.thaipbs.or.th/program-rss.php?id=75",
                provider = "Thai PBS Podcast",
                category = "podcast",
                description = "นิทานสนุก ๆ นิทานลอยฟ้า สาระเรื่องใกล้ตัว และห้องเรียนธรรมชาติสำหรับเด็ก",
                sortOrder = 2
            ),
            NewsFeedItem(
                id = "thaipbs_moral_tales",
                name = "นิทานสุภาษิต สอนใจเด็ก",
                icon = "📖",
                feedUrl = "https://podcast.thaipbs.or.th/program-rss.php?id=120",
                provider = "Thai PBS Podcast",
                category = "podcast",
                description = "เรียนรู้สำนวน สุภาษิตไทย และคติสอนใจผ่านเรื่องราวสนุกสนานของเจ้าจ้อยและเพื่อน ๆ",
                sortOrder = 3
            ),
            NewsFeedItem(
                id = "thaipbs_listen_sound",
                name = "ลองฟัง... นั่นเสียงอะไร",
                icon = "👂",
                feedUrl = "https://podcast.thaipbs.or.th/program-rss.php?id=110",
                provider = "Thai PBS Podcast",
                category = "podcast",
                description = "เกมฝึกทักษะการฟังและแยกแยะเสียงรอบตัว เสริมสร้างสมาธิและการเรียนรู้สำหรับเด็กปฐมวัย",
                sortOrder = 4
            ),

            // 2. หมวดวิดีโอเด็กและการเรียนรู้สร้างสรรค์ (Video)
            NewsFeedItem(
                id = "thaipbs_kids_youtube",
                name = "Thai PBS Kids วิดีโอเด็ก",
                icon = "🎬",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=UCkBuvFq-7IcZdIwfG8zrLyg",
                provider = "Thai PBS Kids (YouTube)",
                category = "video",
                description = "วิดีโอสร้างสรรค์ การทดลอง นิทาน และการเรียนรู้แสนสนุกสำหรับเด็กและครอบครัว",
                isKidsFilterRequired = false,
                sortOrder = 5
            ),
            NewsFeedItem(
                id = "thaipbs_youtube_main",
                name = "Thai PBS วิดีโอรวมเด็ก",
                icon = "📺",
                feedUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=UC5TOFhyb_LxL2VG_Zenhpzw",
                provider = "Thai PBS (YouTube)",
                category = "video",
                description = "รายการเด็ก นิทาน ละครเด็ก และการเรียนรู้ คัดเฉพาะเนื้อหาที่เหมาะสมสำหรับเด็ก",
                isKidsFilterRequired = true,
                sortOrder = 6
            ),
            NewsFeedItem(
                id = "thaipbs_kids_programs",
                name = "รายการเด็ก Thai PBS Kids",
                icon = "🌟",
                feedUrl = "https://kids.thaipbs.or.th/programs",
                provider = "Thai PBS Kids",
                category = "video",
                description = "แหล่งรวมรายการเด็กสร้างสรรค์ เช่น บ้านนักวิทยาศาสตร์น้อย, Kid Rangers, สอนศิลป์",
                isKidsFilterRequired = false,
                sortOrder = 7
            ),
            NewsFeedItem(
                id = "thaipbs_talk_about_kids",
                name = "คุยกับลูก Talk About Kids",
                icon = "👨‍👩‍👧",
                feedUrl = "https://www.thaipbs.or.th/program/TalkAboutKids/videos/all",
                provider = "Thai PBS",
                category = "video",
                description = "รายการวิดีโอพัฒนาการเด็ก สมอง EF และความรู้สร้างเสริมสายสัมพันธ์ครอบครัว",
                isKidsFilterRequired = false,
                sortOrder = 8
            ),

            // 3. ศิลปะ ดนตรี และการอ่าน (ตรงกับการวาดเขียนและระบายสี)
            NewsFeedItem(
                id = "google_arts",
                name = "ศิลปะ ดนตรี และสร้างสรรค์",
                icon = "🎨",
                feedUrl = "https://news.google.com/rss/search?q=" +
                    URLEncoder.encode("ศิลปะเด็ก OR ดนตรีเด็ก OR กิจกรรมสร้างสรรค์ OR ประกวดวาดภาพเด็ก", "UTF-8") +
                    "&hl=th&gl=TH&ceid=TH:th",
                provider = "Google News",
                category = "arts",
                description = "การประกวดวาดภาพ ดนตรีเด็ก นิทาน และกิจกรรมเวิร์กช็อปสร้างสรรค์",
                sortOrder = 9
            ),
            NewsFeedItem(
                id = "google_books",
                name = "หนังสือ การอ่าน และนิทาน",
                icon = "📚",
                feedUrl = "https://news.google.com/rss/search?q=" +
                    URLEncoder.encode("หนังสือเด็ก OR ส่งเสริมการอ่าน OR ห้องสมุดเด็ก OR หนังสือนิทาน", "UTF-8") +
                    "&hl=th&gl=TH&ceid=TH:th",
                provider = "Google News",
                category = "books",
                description = "หนังสือภาพ หนังสือนิทาน กิจกรรมส่งเสริมรักการอ่าน และห้องสมุดเด็ก",
                sortOrder = 10
            ),

            // 4. สัตว์น่ารัก ธรรมชาติ และวิทยาศาสตร์เด็ก
            NewsFeedItem(
                id = "thaipbs_environment",
                name = "สัตว์น่ารักและธรรมชาติ",
                icon = "🌿",
                feedUrl = "https://news.thaipbs.or.th/rss/news/environment",
                provider = "Thai PBS",
                category = "nature",
                description = "สัตว์ป่า สัตว์เลี้ยง ธรรมชาติ ป่าไม้ และการอนุรักษ์สิ่งแวดล้อม",
                isKidsFilterRequired = true,
                sortOrder = 11
            ),
            NewsFeedItem(
                id = "thaipbs_tech",
                name = "วิทย์และอวกาศสำหรับเด็ก",
                icon = "🚀",
                feedUrl = "https://news.thaipbs.or.th/rss/news/tech",
                provider = "Thai PBS",
                category = "space",
                description = "ดาราศาสตร์ อวกาศ การสำรวจดวงจันทร์ เทคโนโลยีปัญญาประดิษฐ์",
                isKidsFilterRequired = true,
                sortOrder = 12
            ),
            NewsFeedItem(
                id = "google_health",
                name = "สุขภาพและการดูแลตัวเอง",
                icon = "🍎",
                feedUrl = "https://news.google.com/rss/search?q=" +
                    URLEncoder.encode("สุขภาพเด็ก OR โภชนาการเด็ก OR ออกกำลังกายเด็ก OR แปรงฟันเด็ก", "UTF-8") +
                    "&hl=th&gl=TH&ceid=TH:th",
                provider = "Google News",
                category = "health",
                description = "โภชนาการอาหาร การออกกำลังกาย และการดูแลสุขภาพสำหรับเด็ก",
                sortOrder = 13
            ),
            NewsFeedItem(
                id = "google_youth",
                name = "กิจกรรมเด็กและโรงเรียน",
                icon = "🎒",
                feedUrl = "https://news.google.com/rss/search?q=" +
                    URLEncoder.encode("เยาวชน OR กิจกรรมโรงเรียน OR เด็กไทยสร้างชื่อเสียง OR โครงงานนักเรียน", "UTF-8") +
                    "&hl=th&gl=TH&ceid=TH:th",
                provider = "Google News",
                category = "school",
                description = "ผลงานเด็กไทยสร้างชื่อเสียง การแข่งขันวิชาการ และกิจกรรมโรงเรียน",
                sortOrder = 14
            ),
            NewsFeedItem(
                id = "google_agriculture",
                name = "เกษตร อาหาร และธรรมชาติ",
                icon = "🌾",
                feedUrl = "https://news.google.com/rss/search?q=" +
                    URLEncoder.encode("เกษตรเด็ก OR อาหารปลอดภัย OR ปลูกผักเด็ก OR ชุมชนสร้างสรรค์", "UTF-8") +
                    "&hl=th&gl=TH&ceid=TH:th",
                provider = "Google News",
                category = "agriculture",
                description = "เกษตรกรรมพอเพียง อาหารปลอดภัย ฟาร์มผัก และภูมิปัญญาชุมชน",
                sortOrder = 15
            ),
            NewsFeedItem(
                id = "thaipbs_kids_safety",
                name = "เด็กรู้สู้ภัย ทักษะชีวิต",
                icon = "🛡️",
                feedUrl = "https://podcast.thaipbs.or.th/program-rss.php?id=83",
                provider = "Thai PBS Podcast",
                category = "podcast",
                description = "สอนทักษะชีวิต การป้องกันตนเองจากอุบัติเหตุ ภัยรอบตัว และการดูแลสุขภาพให้ปลอดภัย",
                sortOrder = 16
            ),
            NewsFeedItem(
                id = "tmd_weather",
                name = "พยากรณ์อากาศและท้องฟ้า",
                icon = "⛅",
                feedUrl = "https://www.tmd.go.th/api/xml/warning-news",
                provider = "กรมอุตุนิยมวิทยา",
                category = "weather",
                description = "รายงานสภาพอากาศ การเปลี่ยนแปลงทางธรรมชาติ และข้อมูลอุตุนิยมวิทยา",
                sortOrder = 17
            ),
            NewsFeedItem(
                id = "nriis_research",
                name = "สิ่งประดิษฐ์และนวัตกรรม",
                icon = "💡",
                feedUrl = "https://nriis.go.th/rss.aspx",
                provider = "วช. (NRIIS)",
                category = "research",
                description = "ผลงานวิจัย โครงงานวิทยาศาสตร์ และสิ่งประดิษฐ์นวัตกรรมฝีมือคนไทย",
                isKidsFilterRequired = true,
                sortOrder = 18
            ),

            // 5. แหล่งสาระข่าวทั่วไป (เปิดระบบกรองเด็ก 100%)
            NewsFeedItem(
                id = "voa_thai",
                name = "วิทย์และเทคโนโลยีเด็ก (VOA)",
                icon = "🔬",
                feedUrl = "https://www.voathai.com/api/zbkyql-vomx-tpeqmui",
                provider = "VOA Thai",
                category = "science",
                description = "ข่าววิทยาศาสตร์ นวัตกรรม เทคโนโลยี และการค้นพบระดับโลกสำหรับเยาวชน",
                isKidsFilterRequired = true,
                sortOrder = 19
            ),
            NewsFeedItem(
                id = "thaipbs_home",
                name = "ข่าวรอบตัวเด็ก (Thai PBS)",
                icon = "📰",
                feedUrl = "https://news.thaipbs.or.th/rss/news/home",
                provider = "Thai PBS",
                category = "general",
                description = "ข่าวสารเด่นประจำวัน กรองเฉพาะเรื่องเด็ก วิทยาศาสตร์ ธรรมชาติ และการเรียนรู้",
                isKidsFilterRequired = true,
                sortOrder = 20
            ),
            NewsFeedItem(
                id = "sanook_tech",
                name = "ไอทีและการเรียนรู้ดิจิทัล",
                icon = "📱",
                feedUrl = "http://rssfeeds.sanook.com/rss/feeds/sanook/hitech.news.xml",
                provider = "Sanook Hitech",
                category = "technology",
                description = "ข่าวไอที เทคโนโลยี แก็ดเจ็ต และอุปกรณ์ดิจิทัลสำหรับยุคใหม่",
                isKidsFilterRequired = true,
                sortOrder = 21
            )
        )
    }
}

/** Typealias เพื่อความเข้ากันได้ */
typealias NewsFeed = NewsFeedItem

data class NewsArticle(
    val id: String,
    val title: String,
    val link: String,
    /** เนื้อหาย่อ */
    val summary: String,
    /** เนื้อข่าวแยกเป็นย่อหน้า (plain text) */
    val paragraphs: List<String>,
    /** หมวดหมู่ข่าว หรือ สำนักข่าวต้นทาง */
    val category: String,
    /** URL รูปภาพข่าว */
    val imageUrl: String?,
    /** เวลาเผยแพร่ (epoch millis) หรือ 0 ถ้าอ่านไม่ได้ */
    val publishedAtMillis: Long,
    /** ลิงก์วิดีโอ (ถ้ามี) */
    val videoUrl: String? = null,
    /** ลิงก์เสียงนิทาน / พอดแคสต์ (ถ้ามี) */
    val audioUrl: String? = null
)

data class FeedState(
    val loading: Boolean = false,
    val items: List<NewsArticle> = emptyList(),
    val error: String? = null
)
