-- RSS feed เพิ่มเติมสำหรับแท็บวิดีโอเด็ก (ทดสอบแล้วว่าตอบ HTTP 200 และมี 100 รายการต่อ feed)
INSERT INTO public.kids_video_feeds
  (id, name, icon, feed_url, provider, category, description, is_kids_filter_required, sort_order, is_active)
VALUES
  ('kids_cartoon_feed', 'การ์ตูนเด็ก', '🎨',
   'https://news.google.com/rss/search?q=การ์ตูนเด็ก+site:youtube.com&hl=th&gl=TH&ceid=TH:th',
   'Google News (YouTube)', 'video', 'การ์ตูนและแอนิเมชันสำหรับเด็ก', true, 10, true),
  ('kids_song_feed', 'เพลงเด็ก', '🎵',
   'https://news.google.com/rss/search?q=เพลงเด็ก+site:youtube.com&hl=th&gl=TH&ceid=TH:th',
   'Google News (YouTube)', 'video', 'เพลงเด็ก เพลงท่องศัพท์ เพลงประกอบท่าทาง', true, 11, true),
  ('kids_science_feed', 'วิทยาศาสตร์น้อย', '🔬',
   'https://news.google.com/rss/search?q=วิทยาศาสตร์สำหรับเด็ก+site:youtube.com&hl=th&gl=TH&ceid=TH:th',
   'Google News (YouTube)', 'video', 'การทดลองและความรู้วิทยาศาสตร์สำหรับเด็ก', true, 12, true),
  ('kids_bedtime_story_feed', 'นิทานก่อนนอน', '🌙',
   'https://news.google.com/rss/search?q=นิทานก่อนนอน+site:youtube.com&hl=th&gl=TH&ceid=TH:th',
   'Google News (YouTube)', 'video', 'นิทานก่อนนอนสำหรับเด็ก', true, 13, true)
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name, icon = EXCLUDED.icon, feed_url = EXCLUDED.feed_url,
  provider = EXCLUDED.provider, category = EXCLUDED.category,
  description = EXCLUDED.description,
  is_kids_filter_required = EXCLUDED.is_kids_filter_required,
  sort_order = EXCLUDED.sort_order, is_active = EXCLUDED.is_active,
  updated_at = NOW();

