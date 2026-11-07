-- อัปเดตตาราง public.kids_video_feeds ให้ใช้ YouTube Official Playlist RSS (playlist_id)
-- เพื่อความแม่นยำ 100% ตรงตามหมวดหมู่ มี <yt:videoId>, <media:thumbnail>, <media:description> ครบถ้วน และไม่ซ้ำกัน
INSERT INTO public.kids_video_feeds
  (id, name, icon, feed_url, provider, category, description, is_kids_filter_required, sort_order, is_active)
VALUES
  ('thaipbs_kids_programs', 'Thai PBS Kids', '🌟',
   'https://www.youtube.com/feeds/videos.xml?playlist_id=PL78EDk_9jZAH5Nw5Tly6tGP46Mn_qYQtI',
   'Thai PBS Kids', 'video', 'แหล่งรวมรายการเด็กสร้างสรรค์ เช่น บ้านนักวิทยาศาสตร์น้อย, Kid Rangers, สอนศิลป์, หม้อข้าวหม้อแกง', false, 7, true),
  ('kids_cartoon_feed', 'การ์ตูนเด็ก', '🎨',
   'https://www.youtube.com/feeds/videos.xml?playlist_id=PL78EDk_9jZAEnJzU6CIacF5pk2R8t8PbO',
   'Thai PBS Kids Animation', 'video', 'การ์ตูนและแอนิเมชันสำหรับเด็ก', false, 10, true),
  ('kids_song_feed', 'เพลงเด็ก', '🎵',
   'https://www.youtube.com/feeds/videos.xml?playlist_id=PLRkCAAfpKzZRTxTyR3zo-IdITnIRnlv3V',
   'KidsMeSong', 'video', 'เพลงเด็ก เพลงท่องศัพท์ เพลงประกอบท่าทาง', false, 11, true),
  ('kids_science_feed', 'วิทยาศาสตร์น้อย', '🔬',
   'https://www.youtube.com/feeds/videos.xml?playlist_id=PL78EDk_9jZAG0CS1_nSeW4qpwmOTXnATy',
   'บ้านนักวิทยาศาสตร์น้อย', 'video', 'การทดลองและความรู้วิทยาศาสตร์สำหรับเด็ก', false, 12, true),
  ('kids_bedtime_story_feed', 'นิทานก่อนนอน', '🌙',
   'https://www.youtube.com/feeds/videos.xml?playlist_id=PLYSzQdnk6q9ZrhdSXFx2Wz9m3gv3L2nOj',
   'นิทานอีสปก่อนนอน', 'video', 'นิทานก่อนนอนสำหรับเด็ก', false, 13, true)
ON CONFLICT (id) DO UPDATE SET
  name = EXCLUDED.name, icon = EXCLUDED.icon, feed_url = EXCLUDED.feed_url,
  provider = EXCLUDED.provider, category = EXCLUDED.category,
  description = EXCLUDED.description,
  is_kids_filter_required = EXCLUDED.is_kids_filter_required,
  sort_order = EXCLUDED.sort_order, is_active = EXCLUDED.is_active,
  updated_at = NOW();

