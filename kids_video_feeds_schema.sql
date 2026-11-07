-- ====================================================================
-- SUPABASE POSTGRESQL SCHEMA FOR KIDS VIDEO FEEDS (kids_video_feeds)
-- ระบบจัดการแท็บฟีดวิดีโอเพื่อการเรียนรู้สำหรับเด็กและเยาวชนโดยเฉพาะ
-- ====================================================================

-- 1. สร้างตาราง kids_video_feeds
CREATE TABLE IF NOT EXISTS public.kids_video_feeds (
    id TEXT PRIMARY KEY,                                      -- รหัสฟีด เช่น 'thaipbs_kids_youtube'
    name TEXT NOT NULL,                                       -- ชื่อแท็บที่แสดงในแอป เช่น 'Thai PBS Kids วิดีโอเด็ก'
    icon TEXT,                                                -- อีโมจิหรือไอคอน เช่น '🎬', '📺', '🌟', '👨‍👩‍👧'
    feed_url TEXT NOT NULL,                                   -- ลิงก์ XML/RSS หรือแหล่งรวมวิดีโอ
    provider TEXT NOT NULL,                                   -- สำนักข่าว/ช่องผู้ผลิต เช่น 'Thai PBS Kids (YouTube)'
    category TEXT NOT NULL DEFAULT 'video',                   -- หมวดหมู่ (ค่าเริ่มต้นคือ 'video')
    description TEXT,                                         -- คำอธิบายสาระสำคัญของวิดีโอ
    is_kids_filter_required BOOLEAN NOT NULL DEFAULT false,   -- ต้องเปิดตัวกรองเนื้อหาสำหรับเด็กหรือไม่
    sort_order INT NOT NULL DEFAULT 0,                        -- ลำดับการแสดงแท็บบนหน้าจอ
    is_active BOOLEAN NOT NULL DEFAULT true,                  -- เปิด/ปิด การแสดงฟีดนี้จากฝั่ง Server
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- เพิ่มคำอธิบายตารางและคอลัมน์ (Database Comments)
COMMENT ON TABLE public.kids_video_feeds IS 'ตารางจัดการแท็บและลิงก์ฟีดวิดีโอเพื่อการเรียนรู้สำหรับเด็กโดยเฉพาะ';
COMMENT ON COLUMN public.kids_video_feeds.id IS 'รหัสเฉพาะของฟีดวิดีโอ (Unique Key)';
COMMENT ON COLUMN public.kids_video_feeds.name IS 'ชื่อหมวดหมู่วิดีโอที่แสดงบนแท็บแอป';
COMMENT ON COLUMN public.kids_video_feeds.feed_url IS 'URL ฟีด YouTube RSS หรือหน้าแหล่งวิดีโอเด็ก';
COMMENT ON COLUMN public.kids_video_feeds.is_kids_filter_required IS 'เปิดใช้ตัวกรองเฉพาะเรื่องเด็ก (Positive Kids Filter)';
COMMENT ON COLUMN public.kids_video_feeds.is_active IS 'สวิตช์เปิด/ปิดฟีดทางไกล';

-- 2. สร้าง Indexes เพื่อความรวดเร็วในการจัดเรียงและโหลดข้อมูล
CREATE INDEX IF NOT EXISTS idx_kids_video_feeds_active_sort ON public.kids_video_feeds(is_active, sort_order);

-- 3. Trigger อัปเดต updated_at อัตโนมัติเมื่อมีการแก้ไขข้อมูลฟีด
CREATE OR REPLACE FUNCTION update_kids_video_feeds_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_set_kids_video_feeds_updated_at ON public.kids_video_feeds;
CREATE TRIGGER trigger_set_kids_video_feeds_updated_at
    BEFORE UPDATE ON public.kids_video_feeds
    FOR EACH ROW
    EXECUTE FUNCTION update_kids_video_feeds_updated_at();

-- 4. ตั้งค่าสิทธิ์ความปลอดภัย Row Level Security (RLS)
ALTER TABLE public.kids_video_feeds ENABLE ROW LEVEL SECURITY;

-- นโยบาย: อนุญาตให้ทุกคน (รวมทั้งผู้ใช้ทั่วไป/Anon) ดึงข้อมูลแท็บวิดีโอที่เปิดใช้งานได้
DROP POLICY IF EXISTS "Public can view active video feeds" ON public.kids_video_feeds;
CREATE POLICY "Public can view active video feeds"
    ON public.kids_video_feeds
    FOR SELECT
    USING (is_active = true);

-- ผู้ดูแลระบบ (Service Role) สามารถ เพิ่ม/ลบ/แก้ไข ฟีดวิดีโอได้เต็มสิทธิ์
DROP POLICY IF EXISTS "Service role full access on video feeds" ON public.kids_video_feeds;
CREATE POLICY "Service role full access on video feeds"
    ON public.kids_video_feeds
    FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

-- ====================================================================
-- 5. ข้อมูลแท็บวิดีโอเด็กและครอบครัว (SEED DATA)
-- ====================================================================
INSERT INTO public.kids_video_feeds (
    id,
    name,
    icon,
    feed_url,
    provider,
    category,
    description,
    is_kids_filter_required,
    sort_order,
    is_active
) VALUES
(
    'thaipbs_kids_youtube',
    'Thai PBS Kids วิดีโอเด็ก',
    '🎬',
    'https://www.youtube.com/feeds/videos.xml?channel_id=UCkBuvFq-7IcZdIwfG8zrLyg',
    'Thai PBS Kids (YouTube)',
    'video',
    'วิดีโอสร้างสรรค์ การทดลอง นิทาน และการเรียนรู้แสนสนุกสำหรับเด็กและครอบครัว',
    false,
    1,
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
    2,
    true
),
(
    'thaipbs_kids_programs',
    'รายการเด็ก Thai PBS Kids',
    '🌟',
    'https://kids.thaipbs.or.th/programs',
    'Thai PBS Kids',
    'video',
    'แหล่งรวมรายการเด็กสร้างสรรค์ เช่น บ้านนักวิทยาศาสตร์น้อย, Kid Rangers, สอนศิลป์, หม้อข้าวหม้อแกง',
    false,
    3,
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
    4,
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

