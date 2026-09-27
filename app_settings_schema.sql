-- ====================================================================
-- SUPABASE POSTGRESQL SCHEMA FOR APP SETTINGS (app_settings)
-- แอป: ฝึกเขียน อ่าน ก ไก่-ABC ระบายสี (Kids Learn & Play)
-- ====================================================================

-- 1. สร้างตาราง app_settings
CREATE TABLE IF NOT EXISTS public.app_settings (
    id INT PRIMARY KEY DEFAULT 1,
    
    -- ---------------------------------------------------------------
    -- [1] การควบคุมเวอร์ชันและการอัปเดต (Version & Update Control)
    -- ---------------------------------------------------------------
    app_version_name TEXT NOT NULL DEFAULT '3.0.3',
    app_version_code INT NOT NULL DEFAULT 47,
    min_supported_version_code INT NOT NULL DEFAULT 45,
    force_update BOOLEAN NOT NULL DEFAULT false,
    update_title TEXT DEFAULT 'มีเวอร์ชันใหม่พร้อมใช้งาน 🎉',
    update_message TEXT DEFAULT 'กรุณาอัปเดตแอปเพื่อรับบทเรียนและเกมใหม่ๆ พร้อมประสิทธิภาพที่ดียิ่งขึ้น',
    play_store_url TEXT DEFAULT 'https://play.google.com/store/apps/details?id=com.pongploydev.priyankvex.paintapp',

    -- ---------------------------------------------------------------
    -- [2] ระบบสนับสนุนและสมัครสมาชิก VIP (Monetization & Subscriptions)
    -- ---------------------------------------------------------------
    is_subscription_enabled BOOLEAN NOT NULL DEFAULT true,
    sub_monthly_id TEXT NOT NULL DEFAULT 'sub_monthly',
    sub_monthly_price NUMERIC(10, 2) NOT NULL DEFAULT 29.00,
    sub_monthly_bonus_stars INT NOT NULL DEFAULT 20,

    sub_yearly_id TEXT NOT NULL DEFAULT 'sub_yearly',
    sub_yearly_price NUMERIC(10, 2) NOT NULL DEFAULT 199.00,
    sub_yearly_bonus_stars INT NOT NULL DEFAULT 100,

    sub_lifetime_id TEXT NOT NULL DEFAULT 'sub_lifetime',
    sub_lifetime_price NUMERIC(10, 2) NOT NULL DEFAULT 399.00,
    sub_lifetime_bonus_stars INT NOT NULL DEFAULT 300,

    vip_stars_multiplier NUMERIC(3, 1) NOT NULL DEFAULT 2.0,

    -- ---------------------------------------------------------------
    -- [3] การควบคุมโฆษณา AdMob ทางไกล (Remote AdMob Controls)
    -- ---------------------------------------------------------------
    is_ads_enabled BOOLEAN NOT NULL DEFAULT true,
    is_banner_ad_enabled BOOLEAN NOT NULL DEFAULT true,
    is_interstitial_ad_enabled BOOLEAN NOT NULL DEFAULT true,
    ad_interstitial_interval INT NOT NULL DEFAULT 3, -- แสดงโฆษณาทุกๆ กี่เกม/กิจกรรม
    admob_app_id TEXT DEFAULT 'ca-app-pub-3279793802315716~2607584711',
    admob_ad_unit_id TEXT DEFAULT 'ca-app-pub-3279793802315716/8581063137',

    -- ---------------------------------------------------------------
    -- [4] การเปิด/ปิดฟีเจอร์ภายในแอป (Feature Flags)
    -- ---------------------------------------------------------------
    is_game_hub_enabled BOOLEAN NOT NULL DEFAULT true,
    is_video_hub_enabled BOOLEAN NOT NULL DEFAULT true,
    is_sound_effects_enabled BOOLEAN NOT NULL DEFAULT true,
    is_ai_feature_enabled BOOLEAN NOT NULL DEFAULT false, -- ปิดหมวด AI ตามนโยบายเดิม

    -- ---------------------------------------------------------------
    -- [5] โหมดปิดปรับปรุงและประกาศแจ้งเตือน (Maintenance & Announcements)
    -- ---------------------------------------------------------------
    is_maintenance_mode BOOLEAN NOT NULL DEFAULT false,
    maintenance_message TEXT DEFAULT 'แอปกำลังปิดปรับปรุงเซิร์ฟเวอร์ชั่วคราว ขออภัยในความไม่สะดวกครับ',
    is_announcement_active BOOLEAN NOT NULL DEFAULT false,
    announcement_text TEXT DEFAULT 'ยินดีต้อนรับสู่อัปเดตใหม่! เพิ่ม 10 เกมการศึกษาและระบบสมาชิก VIP 🌟',

    -- ---------------------------------------------------------------
    -- [6] ข้อมูลการติดต่อและนโยบาย (Contact & Legal Information)
    -- ---------------------------------------------------------------
    contact_email TEXT DEFAULT 'pongploydev@gmail.com',
    privacy_policy_url TEXT DEFAULT 'https://pongploydev.github.io/privacy-policy/',
    terms_of_service_url TEXT DEFAULT 'https://pongploydev.github.io/terms/',
    line_official_url TEXT DEFAULT '',
    facebook_fanpage_url TEXT DEFAULT '',

    -- ---------------------------------------------------------------
    -- [7] เวลาที่อัปเดต (Timestamps)
    -- ---------------------------------------------------------------
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),

    -- บังคับให้มีแถวการตั้งค่าเพียงแถวเดียว (Single-Row Pattern)
    CONSTRAINT check_single_row CHECK (id = 1)
);

-- 2. สร้าง Trigger อัปเดต updated_at อัตโนมัติ
CREATE OR REPLACE FUNCTION update_app_settings_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_app_settings_updated_at ON public.app_settings;
CREATE TRIGGER trg_app_settings_updated_at
    BEFORE UPDATE ON public.app_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_app_settings_updated_at();

-- 3. เปิดใช้งาน Row Level Security (RLS)
ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;

-- 4. กำหนด Policies ความปลอดภัย
-- อนุญาตให้แอปอ่านค่าการตั้งค่าได้โดยไม่ต้องล็อกอิน (Public Read Access)
DROP POLICY IF EXISTS "Allow public read access to app_settings" ON public.app_settings;
CREATE POLICY "Allow public read access to app_settings"
    ON public.app_settings FOR SELECT
    USING (true);

-- อนุญาตให้อัปเดตเฉพาะ Service Role / ผู้ดูแลระบบ (Admin)
DROP POLICY IF EXISTS "Allow admin update app_settings" ON public.app_settings;
CREATE POLICY "Allow admin update app_settings"
    ON public.app_settings FOR ALL
    USING (auth.role() = 'service_role')
    WITH CHECK (auth.role() = 'service_role');

-- ====================================================================
-- 5. ข้อมูลตั้งต้น (Initial Default Data)
-- ====================================================================
INSERT INTO public.app_settings (
    id,
    app_version_name,
    app_version_code,
    min_supported_version_code,
    force_update,
    update_title,
    update_message,
    play_store_url,
    is_subscription_enabled,
    sub_monthly_id,
    sub_monthly_price,
    sub_monthly_bonus_stars,
    sub_yearly_id,
    sub_yearly_price,
    sub_yearly_bonus_stars,
    sub_lifetime_id,
    sub_lifetime_price,
    sub_lifetime_bonus_stars,
    vip_stars_multiplier,
    is_ads_enabled,
    is_banner_ad_enabled,
    is_interstitial_ad_enabled,
    ad_interstitial_interval,
    admob_app_id,
    admob_ad_unit_id,
    is_game_hub_enabled,
    is_video_hub_enabled,
    is_sound_effects_enabled,
    is_ai_feature_enabled,
    is_maintenance_mode,
    maintenance_message,
    is_announcement_active,
    announcement_text,
    contact_email,
    privacy_policy_url,
    terms_of_service_url
) VALUES (
    1,
    '3.0.3',
    47,
    45,
    false,
    'มีเวอร์ชันใหม่พร้อมใช้งาน 🎉',
    'กรุณาอัปเดตแอปเพื่อรับบทเรียนและเกมใหม่ๆ พร้อมประสิทธิภาพที่ดียิ่งขึ้น',
    'https://play.google.com/store/apps/details?id=com.pongploydev.priyankvex.paintapp',
    true,
    'sub_monthly',
    29.00,
    20,
    'sub_yearly',
    199.00,
    100,
    'sub_lifetime',
    399.00,
    300,
    2.0,
    true,
    true,
    true,
    3,
    'ca-app-pub-3279793802315716~2607584711',
    'ca-app-pub-3279793802315716/8581063137',
    true,
    true,
    true,
    false,
    false,
    'แอปกำลังปิดปรับปรุงเซิร์ฟเวอร์ชั่วคราว ขออภัยในความไม่สะดวกครับ',
    true,
    'ยินดีต้อนรับสู่อัปเดตใหม่! เพิ่ม 10 เกมการศึกษาและระบบสมาชิก VIP 👑',
    'pongploydev@gmail.com',
    'https://pongploydev.github.io/privacy-policy/',
    'https://pongploydev.github.io/terms/'
)
ON CONFLICT (id) DO UPDATE SET
    app_version_name = EXCLUDED.app_version_name,
    app_version_code = EXCLUDED.app_version_code,
    sub_monthly_price = EXCLUDED.sub_monthly_price,
    sub_yearly_price = EXCLUDED.sub_yearly_price,
    sub_lifetime_price = EXCLUDED.sub_lifetime_price,
    updated_at = NOW();
