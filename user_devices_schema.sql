-- ====================================================================
-- SUPABASE POSTGRESQL SCHEMA FOR USER DEVICES (user_devices)
-- ====================================================================

-- 1. Create user_devices Table (user_id is UUID referencing public.profiles(id))
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
    
    -- Ensure unique device registration per user
    CONSTRAINT unique_user_device UNIQUE (user_id, device_id)
);

-- 2. Create Indexes for High Performance Querying
CREATE INDEX IF NOT EXISTS idx_user_devices_user_id ON public.user_devices(user_id);
CREATE INDEX IF NOT EXISTS idx_user_devices_device_id ON public.user_devices(device_id);

-- 3. Automatic updated_at Trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_user_devices_updated_at ON public.user_devices;
CREATE TRIGGER set_user_devices_updated_at
    BEFORE UPDATE ON public.user_devices
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 4. Enable Row Level Security (RLS)
ALTER TABLE public.user_devices ENABLE ROW LEVEL SECURITY;

-- 5. Row Level Security Policies
CREATE POLICY "Allow public read access to user_devices"
    ON public.user_devices FOR SELECT
    USING (true);

CREATE POLICY "Allow insert/upsert to user_devices"
    ON public.user_devices FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Allow update to user_devices"
    ON public.user_devices FOR UPDATE
    USING (true);

CREATE POLICY "Allow delete on user_devices"
    ON public.user_devices FOR DELETE
    USING (true);
