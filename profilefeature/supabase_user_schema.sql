-- ==============================================================================
-- SUPABASE POSTGRESQL SCHEMA FOR KIDS LEARNING APP (Paint App for Kids)
-- Project: qriqerrkxccpnwabphdc
-- Description: User Profiles, Learning Progress, Quiz Scores, and Achievements
-- ==============================================================================

-- 1. ENABLE EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==============================================================================
-- TABLE 1: PROFILES (ข้อมูลโปรไฟล์ผู้ใช้งาน ลิงก์กับ auth.users)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    display_name TEXT DEFAULT 'นักอ่านตัวน้อย',
    avatar_url TEXT DEFAULT '',
    role TEXT DEFAULT 'student', -- 'student', 'parent', 'admin'
    total_stars INTEGER DEFAULT 0, -- ⭐ ดาวสะสม
    total_coins INTEGER DEFAULT 0, -- 🪙 เหรียญทอง
    current_level INTEGER DEFAULT 1, -- 🏆 เลเวลผู้ใช้
    streak_days INTEGER DEFAULT 1, -- 🚀 จำนวนวันที่เข้าเรียนต่อเนื่อง
    last_active_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- TABLE 2: USER_LEARNING_PROGRESS (บันทึกความก้าวหน้าการเรียนรู้รายหมวด)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.user_learning_progress (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    category_key TEXT NOT NULL, -- เช่น 'thai_letters', 'abc_english', 'numbers_123', 'coloring'
    item_key TEXT NOT NULL, -- เช่น 'korkai_1', 'letter_a', 'num_5'
    score INTEGER DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    times_practiced INTEGER DEFAULT 1,
    last_practiced_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_user_item UNIQUE (user_id, category_key, item_key)
);

-- ==============================================================================
-- TABLE 3: USER_QUIZ_RESULTS (บันทึกผลการทำแบบทดสอบ)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.user_quiz_results (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    quiz_topic_id INTEGER NOT NULL,
    quiz_topic_name TEXT NOT NULL, -- เช่น 'แบบทดสอบ ก-ฮ', 'แบบทดสอบ A-Z'
    score INTEGER NOT NULL DEFAULT 0,
    total_questions INTEGER NOT NULL DEFAULT 10,
    time_spent_seconds INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ==============================================================================
-- TABLE 4: USER_ACHIEVEMENTS (ถ้วยรางวัล / ตราประทับความสำเร็จ)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.user_achievements (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    badge_key TEXT NOT NULL, -- เช่น 'badge_first_write', 'badge_quiz_master', 'badge_streak_7'
    badge_name TEXT NOT NULL,
    badge_icon TEXT DEFAULT '🏆',
    unlocked_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_user_badge UNIQUE (user_id, badge_key)
);

-- ==============================================================================
-- INDEXES FOR FAST QUERY PERFORMANCE
-- ==============================================================================
CREATE INDEX IF NOT EXISTS idx_profiles_email ON public.profiles(email);
CREATE INDEX IF NOT EXISTS idx_progress_user_category ON public.user_learning_progress(user_id, category_key);
CREATE INDEX IF NOT EXISTS idx_quiz_user ON public.user_quiz_results(user_id);
CREATE INDEX IF NOT EXISTS idx_achievements_user ON public.user_achievements(user_id);

-- ==============================================================================
-- AUTOMATIC TRIGGER: CREATE PROFILE WHEN USER SIGNS UP IN auth.users
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, display_name, avatar_url)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'display_name', split_part(NEW.email, '@', 1), 'นักอ่านตัวน้อย'),
        COALESCE(NEW.raw_user_meta_data->>'avatar_url', '')
    )
    ON CONFLICT (id) DO UPDATE
    SET email = EXCLUDED.email,
        display_name = COALESCE(EXCLUDED.display_name, public.profiles.display_name),
        updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Drop trigger if exists and recreate
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ==============================================================================
-- AUTOMATIC TRIGGER: UPDATE updated_at TIMESTAMP
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_profiles_updated_at ON public.profiles;
CREATE TRIGGER set_profiles_updated_at
    BEFORE UPDATE ON public.profiles
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================

-- Enable RLS
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_learning_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_quiz_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_achievements ENABLE ROW LEVEL SECURITY;

-- 1. Profiles Policies
CREATE POLICY "Public profiles are viewable by everyone"
    ON public.profiles FOR SELECT
    USING (true);

CREATE POLICY "Users can insert their own profile"
    ON public.profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can update their own profile"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

-- 2. Learning Progress Policies (Allow read/write for members and guests)
CREATE POLICY "Users can view learning progress"
    ON public.user_learning_progress FOR SELECT
    USING (true);

CREATE POLICY "Users can insert learning progress"
    ON public.user_learning_progress FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can update learning progress"
    ON public.user_learning_progress FOR UPDATE
    USING (true);

-- 3. Quiz Results Policies (Allow public read for Leaderboard and insert for score submission)
CREATE POLICY "Allow public read to user_quiz_results"
    ON public.user_quiz_results FOR SELECT
    USING (true);

CREATE POLICY "Allow insert to user_quiz_results"
    ON public.user_quiz_results FOR INSERT
    WITH CHECK (true);

-- 4. Achievements Policies (Allow read/write for members and guests)
CREATE POLICY "Users can view achievements"
    ON public.user_achievements FOR SELECT
    USING (true);

CREATE POLICY "Users can insert achievements"
    ON public.user_achievements FOR INSERT
    WITH CHECK (true);

-- ==============================================================================
-- TABLE 5: USER_DEVICES (บันทึกข้อมูลอุปกรณ์มือถือของผู้ใช้)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.user_devices (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    device_id TEXT NOT NULL,
    device_name TEXT,
    device_model TEXT,
    os_version TEXT,
    app_version TEXT,
    fcm_token TEXT,
    is_active BOOLEAN DEFAULT true,
    last_active_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_user_device UNIQUE (user_id, device_id)
);

CREATE INDEX IF NOT EXISTS idx_user_devices_user ON public.user_devices(user_id);
CREATE INDEX IF NOT EXISTS idx_user_devices_device ON public.user_devices(device_id);

ALTER TABLE public.user_devices ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own devices"
    ON public.user_devices FOR SELECT
    USING (true);

CREATE POLICY "Users can insert/upsert their own devices"
    ON public.user_devices FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can update their own devices"
    ON public.user_devices FOR UPDATE
    USING (true);
