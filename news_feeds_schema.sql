-- ====================================================================
-- SUPABASE POSTGRESQL SCHEMA FOR KIDS NEWS FEEDS (kids_news_feeds)
-- ระบบฟีดข่าวสารและการเรียนรู้สำหรับเด็กและเยาวชน (Kids News & Learning)
-- ====================================================================

-- 1. สร้างตาราง kids_news_feeds
CREATE TABLE IF NOT EXISTS public.kids_news_feeds (
    id TEXT PRIMARY KEY,                                      -- รหัสฟีด เช่น 'thaipbs_home', 'voa_thai'
    name TEXT NOT NULL,                                       -- ชื่อแท็บที่แสดงในแอป เช่น 'ข่าวรวมเด็ก (Thai PBS)'
    icon TEXT,                                                -- อีโมจิหรือไอคอน เช่น '📰', '🔬', '⛅'
    feed_url TEXT NOT NULL,                                   -- ลิงก์ RSS XML ของฟีด
    provider TEXT NOT NULL,                                   -- สำนักข่าว เช่น 'Thai PBS', 'VOA Thai', 'TMD', 'Google News'
    category TEXT NOT NULL DEFAULT 'general',                 -- หมวดหมู่ เช่น 'science', 'nature', 'health', 'art'
    description TEXT,                                         -- คำอธิบายฟีดสั้นๆ
    is_kids_filter_required BOOLEAN NOT NULL DEFAULT false,   -- ต้องกรองคำเฉพาะเรื่องเด็กหรือไม่ (สำหรับข่าวรวม)
    sort_order INT NOT NULL DEFAULT 0,                        -- ลำดับการแสดงแท็บบนหน้าจอ
    is_active BOOLEAN NOT NULL DEFAULT true,                  -- เปิด/ปิด การแสดงฟีดนี้จากฝั่ง Server
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- เพิ่มคำอธิบายตารางและคอลัมน์ (Documentation in Database)
COMMENT ON TABLE public.kids_news_feeds IS 'ตารางจัดการหมวดหมู่และลิงก์ RSS ข่าวสารและการเรียนรู้สำหรับเด็ก';
COMMENT ON COLUMN public.kids_news_feeds.id IS 'รหัสเฉพาะของฟีด (Unique Key)';
COMMENT ON COLUMN public.kids_news_feeds.name IS 'ชื่อหมวดหมู่ที่แสดงบนแท็บแอป';
COMMENT ON COLUMN public.kids_news_feeds.feed_url IS 'URL ฟีด RSS แบบ XML';
COMMENT ON COLUMN public.kids_news_feeds.is_kids_filter_required IS 'เปิดใช้ตัวกรองเรื่องเด็กอัตโนมัติ (Positive Filter)';
COMMENT ON COLUMN public.kids_news_feeds.is_active IS 'สวิตช์เปิด/ปิดฟีดทางไกล';

-- 2. สร้างตารางบุ๊กมาร์กข่าวที่เด็กๆ บันทึกไว้ (kids_saved_news)
CREATE TABLE IF NOT EXISTS public.kids_saved_news (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    article_id TEXT NOT NULL,
    title TEXT NOT NULL,
    link TEXT NOT NULL,
    image_url TEXT,
    source TEXT,
    saved_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT unique_user_saved_article UNIQUE (user_id, article_id)
);

-- Index สำหรับค้นหาบุ๊กมาร์กตาม User
CREATE INDEX IF NOT EXISTS idx_kids_saved_news_user ON public.kids_saved_news(user_id);
CREATE INDEX IF NOT EXISTS idx_kids_news_feeds_active_sort ON public.kids_news_feeds(is_active, sort_order);

-- 3. Trigger อัปเดต updated_at อัตโนมัติเมื่อมีการแก้ไขข้อมูลฟีด
CREATE OR REPLACE FUNCTION update_kids_news_feeds_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_set_kids_news_feeds_updated_at ON public.kids_news_feeds;
CREATE TRIGGER trigger_set_kids_news_feeds_updated_at
    BEFORE UPDATE ON public.kids_news_feeds
    FOR EACH ROW
    EXECUTE FUNCTION update_kids_news_feeds_updated_at();

-- 4. ตั้งค่าสิทธิ์ความปลอดภัย (Row Level Security - RLS)
ALTER TABLE public.kids_news_feeds ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.kids_saved_news ENABLE ROW LEVEL SECURITY;

-- นโยบาย kids_news_feeds: อนุญาตให้ทุกคน (รวมถึงผู้ใช้ Guest/Anon) อ่านข้อมูลฟีดได้
DROP POLICY IF EXISTS "Public can view active news feeds" ON public.kids_news_feeds;
CREATE POLICY "Public can view active news feeds"
    ON public.kids_news_feeds
    FOR SELECT
    USING (is_active = true);

-- ผู้ดูแลระบบ (Service Role) สามารถ เพิ่ม/ลบ/แก้ไข ฟีดได้
DROP POLICY IF EXISTS "Service role full access on news feeds" ON public.kids_news_feeds;
CREATE POLICY "Service role full access on news feeds"
    ON public.kids_news_feeds
    FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- นโยบาย kids_saved_news: เด็ก/ผู้ใช้ สามารถอ่านและบันทึกเฉพาะข่าวของตัวเองได้
DROP POLICY IF EXISTS "Users can view own saved news" ON public.kids_saved_news;
CREATE POLICY "Users can view own saved news"
    ON public.kids_saved_news
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can insert own saved news" ON public.kids_saved_news;
CREATE POLICY "Users can insert own saved news"
    ON public.kids_saved_news
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can delete own saved news" ON public.kids_saved_news;
CREATE POLICY "Users can delete own saved news"
    ON public.kids_saved_news
    FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);

-- ====================================================================
-- 5. ข้อมูลตั้งต้น (SEED DATA): รวมฟีดทั้ง 21 หมวดหมู่คุณภาพสำหรับเด็ก (นิทาน วิดีโอ ศิลปะ และสาระการเรียนรู้)
-- ====================================================================
INSERT INTO public.kids_news_feeds (
    id, name, icon, feed_url, provider, category, description, is_kids_filter_required, sort_order, is_active
) VALUES
(
    'thaipbs_kidshour',
    'Kids Hour ชั่วโมงนิทาน',
    '🎧',
    'https://podcast.thaipbs.or.th/program-rss.php?id=66',
    'Thai PBS Podcast',
    'podcast',
    'นิทานภาษาไทยแสนสนุก เสริมสร้างจินตนาการและการฟังคำศัพท์สำหรับเด็ก 3-10 ปี',
    false,
    1,
    true
),
(
    'thaipbs_sunshine',
    'พระอาทิตย์ยิ้มแฉ่ง นิทานเด็ก',
    '☀️',
    'https://podcast.thaipbs.or.th/program-rss.php?id=75',
    'Thai PBS Podcast',
    'podcast',
    'นิทานสนุก ๆ นิทานลอยฟ้า สาระเรื่องใกล้ตัว และห้องเรียนธรรมชาติสำหรับเด็ก',
    false,
    2,
    true
),
(
    'thaipbs_moral_tales',
    'นิทานสุภาษิต สอนใจเด็ก',
    '📖',
    'https://podcast.thaipbs.or.th/program-rss.php?id=120',
    'Thai PBS Podcast',
    'podcast',
    'เรียนรู้สำนวน สุภาษิตไทย และคติสอนใจผ่านเรื่องราวสนุกสนานของเจ้าจ้อยและเพื่อน ๆ',
    false,
    3,
    true
),
(
    'thaipbs_listen_sound',
    'ลองฟัง... นั่นเสียงอะไร',
    '👂',
    'https://podcast.thaipbs.or.th/program-rss.php?id=110',
    'Thai PBS Podcast',
    'podcast',
    'เกมฝึกทักษะการฟังและแยกแยะเสียงรอบตัว เสริมสร้างสมาธิและการเรียนรู้สำหรับเด็กปฐมวัย',
    false,
    4,
    true
),
(
    'thaipbs_kids_youtube',
    'Thai PBS Kids วิดีโอเด็ก',
    '🎬',
    'https://www.youtube.com/feeds/videos.xml?channel_id=UCkBuvFq-7IcZdIwfG8zrLyg',
    'Thai PBS Kids (YouTube)',
    'video',
    'วิดีโอสร้างสรรค์ การทดลอง นิทาน และการเรียนรู้แสนสนุกสำหรับเด็กและครอบครัว',
    false,
    5,
    true
),
(
    'thaipbs_youtube_main',
    'Thai PBS วิดีโอรวมเด็ก',
    '📺',
    'https://www.youtube.com/feeds/videos.xml?channel_id=UC5TOFhyb_LxL2VG_Zenhpzw',
    'Thai PBS (YouTube)',
    'video',
    'รายการเด็ก นิทาน ละครเด็ก และการเรียนรู้ คัดเฉพาะเนื้อหาที่เหมาะสมสำหรับเด็ก',
    true,
    6,
    true
),
(
    'thaipbs_kids_programs',
    'รายการเด็ก Thai PBS Kids',
    '🌟',
    'https://kids.thaipbs.or.th/programs',
    'Thai PBS Kids',
    'video',
    'แหล่งรวมรายการเด็กสร้างสรรค์ เช่น บ้านนักวิทยาศาสตร์น้อย, Kid Rangers, สอนศิลป์',
    false,
    7,
    true
),
(
    'thaipbs_talk_about_kids',
    'คุยกับลูก Talk About Kids',
    '👨‍👩‍👧',
    'https://www.thaipbs.or.th/program/TalkAboutKids/videos/all',
    'Thai PBS',
    'video',
    'รายการวิดีโอพัฒนาการเด็ก สมอง EF และความรู้สร้างเสริมสายสัมพันธ์ครอบครัว',
    false,
    8,
    true
),
(
    'google_arts',
    'ศิลปะ ดนตรี และสร้างสรรค์',
    '🎨',
    'https://news.google.com/rss/search?q=%E0%B8%A8%E0%B8%B4%E0%B8%A5%E0%B8%9B%E0%B8%B0%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81+OR+%E0%B8%94%E0%B8%99%E0%B8%95%E0%B8%A3%E0%B8%B5%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81+OR+%E0%B8%81%E0%B8%B4%E0%B8%88%E0%B8%81%E0%B8%A3%E0%B8%A3%E0%B8%A1%E0%B8%AA%E0%B8%A3%E0%B9%89%E0%B8%B2%E0%B8%87%E0%B8%AA%E0%B8%A3%E0%B8%A3%E0%B8%84%E0%B9%8C+OR+%E0%B8%9B%E0%B8%A3%E0%B8%B0%E0%B8%81%E0%B8%A7%E0%B8%94%E0%B8%A7%E0%B8%B2%E0%B8%94%E0%B8%A0%E0%B8%B2%E0%B8%9E%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81&hl=th&gl=TH&ceid=TH:th',
    'Google News',
    'arts',
    'การประกวดวาดภาพ ดนตรีเด็ก นิทาน และกิจกรรมเวิร์กช็อปสร้างสรรค์',
    false,
    9,
    true
),
(
    'google_books',
    'หนังสือ การอ่าน และนิทาน',
    '📚',
    'https://news.google.com/rss/search?q=%E0%B8%AB%E0%B8%99%E0%B8%B1%E0%B8%87%E0%B8%AA%E0%B8%B7%E0%B8%AD%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81+OR+%E0%B8%AA%E0%B9%88%E0%B8%87%E0%B9%80%E0%B8%AA%E0%B8%A3%E0%B8%B4%E0%B8%A1%E0%B8%81%E0%B8%B2%E0%B8%A3%E0%B8%AD%E0%B9%88%E0%B8%B2%E0%B8%99+OR+%E0%B8%AB%E0%B9%89%E0%B8%AD%E0%B8%87%E0%B8%AA%E0%B8%A1%E0%B8%B8%E0%B8%94%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81+OR+%E0%B8%AB%E0%B8%99%E0%B8%B1%E0%B8%87%E0%B8%AA%E0%B8%B7%E0%B8%AD%E0%B8%99%E0%B8%B4%E0%B8%97%E0%B8%B2%E0%B8%99&hl=th&gl=TH&ceid=TH:th',
    'Google News',
    'books',
    'หนังสือภาพ หนังสือนิทาน กิจกรรมส่งเสริมรักการอ่าน และห้องสมุดเด็ก',
    false,
    10,
    true
),
(
    'thaipbs_environment',
    'สัตว์น่ารักและธรรมชาติ',
    '🌿',
    'https://news.thaipbs.or.th/rss/news/environment',
    'Thai PBS',
    'nature',
    'สัตว์ป่า สัตว์เลี้ยง ธรรมชาติ ป่าไม้ และการอนุรักษ์สิ่งแวดล้อม',
    true,
    11,
    true
),
(
    'thaipbs_tech',
    'วิทย์และอวกาศสำหรับเด็ก',
    '🚀',
    'https://news.thaipbs.or.th/rss/news/tech',
    'Thai PBS',
    'space',
    'ดาราศาสตร์ อวกาศ การสำรวจดวงจันทร์ เทคโนโลยีปัญญาประดิษฐ์',
    true,
    12,
    true
),
(
    'google_health',
    'สุขภาพและการดูแลตัวเอง',
    '🍎',
    'https://news.google.com/rss/search?q=%E0%B8%AA%E0%B8%B8%E0%B8%82%E0%B8%A0%E0%B8%B2%E0%B8%9E%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81+OR+%E0%B9%82%E0%B8%A0%E0%B8%8A%E0%B8%99%E0%B8%B2%E0%B8%81%E0%B8%B2%E0%B8%A3%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81+OR+%E0%B8%AD%E0%B8%AD%E0%B8%81%E0%B8%81%E0%B8%B3%E0%B8%A5%E0%B8%B1%E0%B8%87%E0%B8%81%E0%B8%B2%E0%B8%A2%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81+OR+%E0%B9%81%E0%B8%9B%E0%B8%A3%E0%B8%87%E0%B8%9F%E0%B8%B1%E0%B8%99%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81&hl=th&gl=TH&ceid=TH:th',
    'Google News',
    'health',
    'โภชนาการอาหาร การออกกำลังกาย และการดูแลสุขภาพสำหรับเด็ก',
    false,
    13,
    true
),
(
    'google_youth',
    'กิจกรรมเด็กและโรงเรียน',
    '🎒',
    'https://news.google.com/rss/search?q=%E0%B9%80%E0%B8%A2%E0%B8%B2%E0%B8%A7%E0%B8%8A%E0%B8%99+OR+%E0%B8%81%E0%B8%B4%E0%B8%88%E0%B8%81%E0%B8%A3%E0%B8%A3%E0%B8%A1%E0%B9%82%E0%B8%A3%E0%B8%87%E0%B9%80%E0%B8%A3%E0%B8%B5%E0%B8%A2%E0%B8%99+OR+%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81%E0%B9%84%E0%B8%97%E0%B8%A2%E0%B8%AA%E0%B8%A3%E0%B9%89%E0%B8%B2%E0%B8%87%E0%B8%8A%E0%B8%B7%E0%B9%88%E0%B8%AD%E0%B9%80%E0%B8%AA%E0%B8%B5%E0%B8%A2%E0%B8%87+OR+%E0%B9%82%E0%B8%84%E0%B8%A3%E0%B8%87%E0%B8%87%E0%B8%B2%E0%B8%99%E0%B8%99%E0%B8%B1%E0%B8%81%E0%B9%80%E0%B8%A3%E0%B8%B5%E0%B8%A2%E0%B8%99&hl=th&gl=TH&ceid=TH:th',
    'Google News',
    'school',
    'ผลงานเด็กไทยสร้างชื่อเสียง การแข่งขันวิชาการ และกิจกรรมโรงเรียน',
    false,
    14,
    true
),
(
    'google_agriculture',
    'เกษตร อาหาร และธรรมชาติ',
    '🌾',
    'https://news.google.com/rss/search?q=%E0%B9%80%E0%B8%81%E0%B8%A9%E0%B8%95%E0%B8%A3%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81+OR+%E0%B8%AD%E0%B8%B2%E0%B8%AB%E0%B8%B2%E0%B8%A3%E0%B8%9B%E0%B8%A5%E0%B8%AD%E0%B8%94%E0%B8%A0%E0%B8%B1%E0%B8%A2+OR+%E0%B8%9B%E0%B8%A5%E0%B8%BParse%E0%B8%9C%E0%B8%B1%E0%B8%81%E0%B9%80%E0%B8%94%E0%B9%87%E0%B8%81+OR+%E0%B8%8A%E0%B8%B8%E0%B8%A1%E0%B8%8A%E0%B8%99%E0%B8%AA%E0%B8%A3%E0%B9%89%E0%B8%B2%E0%B8%87%E0%B8%AA%E0%B8%A3%E0%B8%A3%E0%B8%84%E0%B9%8C&hl=th&gl=TH&ceid=TH:th',
    'Google News',
    'agriculture',
    'เกษตรกรรมพอเพียง อาหารปลอดภัย ฟาร์มผัก และภูมิปัญญาชุมชน',
    false,
    15,
    true
),
(
    'thaipbs_kids_safety',
    'เด็กรู้สู้ภัย ทักษะชีวิต',
    '🛡️',
    'https://podcast.thaipbs.or.th/program-rss.php?id=83',
    'Thai PBS Podcast',
    'podcast',
    'สอนทักษะชีวิต การป้องกันตนเองจากอุบัติเหตุ ภัยรอบตัว และการดูแลสุขภาพให้ปลอดภัย',
    false,
    16,
    true
),
(
    'tmd_weather',
    'พยากรณ์อากาศและท้องฟ้า',
    '⛅',
    'https://www.tmd.go.th/api/xml/warning-news',
    'กรมอุตุนิยมวิทยา',
    'weather',
    'รายงานสภาพอากาศ การเปลี่ยนแปลงทางธรรมชาติ และข้อมูลอุตุนิยมวิทยา',
    false,
    17,
    true
),
(
    'nriis_research',
    'สิ่งประดิษฐ์และนวัตกรรม',
    '💡',
    'https://nriis.go.th/rss.aspx',
    'วช. (NRIIS)',
    'research',
    'ผลงานวิจัย โครงงานวิทยาศาสตร์ และสิ่งประดิษฐ์นวัตกรรมฝีมือคนไทย',
    true,
    18,
    true
),
(
    'voa_thai',
    'วิทย์และเทคโนโลยีเด็ก (VOA)',
    '🔬',
    'https://www.voathai.com/api/zbkyql-vomx-tpeqmui',
    'VOA Thai',
    'science',
    'ข่าววิทยาศาสตร์ นวัตกรรม เทคโนโลยี และการค้นพบระดับโลกสำหรับเยาวชน',
    true,
    19,
    true
),
(
    'thaipbs_home',
    'ข่าวรอบตัวเด็ก (Thai PBS)',
    '📰',
    'https://news.thaipbs.or.th/rss/news/home',
    'Thai PBS',
    'general',
    'ข่าวสารเด่นประจำวัน กรองเฉพาะเรื่องเด็ก วิทยาศาสตร์ ธรรมชาติ และการเรียนรู้',
    true,
    20,
    true
),
(
    'sanook_tech',
    'ไอทีและการเรียนรู้ดิจิทัล',
    '📱',
    'http://rssfeeds.sanook.com/rss/feeds/sanook/hitech.news.xml',
    'Sanook Hitech',
    'technology',
    'ข่าวไอที เทคโนโลยี แก็ดเจ็ต และอุปกรณ์ดิจิทัลสำหรับยุคใหม่',
    true,
    21,
    true
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    icon = EXCLUDED.icon,
    feed_url = EXCLUDED.feed_url,
    provider = EXCLUDED.provider,
    category = EXCLUDED.category,
    description = EXCLUDED.description,
    is_kids_filter_required = EXCLUDED.is_kids_filter_required,
    sort_order = EXCLUDED.sort_order,
    is_active = EXCLUDED.is_active,
    updated_at = NOW();
