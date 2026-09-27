# 🎨 Kids Learn & Play: Thai Alphabet, Tracing, Coloring & Video Watch App
**(แอปฝึกคัดลายมือ ท่อง ก-ฮ, ระบายสี และสื่อวิดีโอการเรียนรู้สำหรับเด็ก)**

แอปพลิเคชันเพื่อการศึกษาสำหรับเด็กปฐมวัยและเด็กเล็ก รวมการเรียนรู้ภาษาไทย ภาษาอังกฤษ การฝึกเขียนคัดลายมือตามเส้นประ การระบายสีภาพสัตว์/คำศัพท์ และคลังวิดีโอเพื่อการเรียนรู้ปลอดภัยไว้ในแอปเดียว ใช้งานง่าย สีสันสดใส และออกแบบตามหลัก Child-Friendly UX/UI

---

## ✨ ฟีเจอร์หลัก (Key Features)

### 🇹🇭 1. ห้องเรียนภาษาไทยครบวงจร (Thai Learning Hub)
- **พยัญชนะไทย 44 ตัว (ก-ฮ)**: แสดงการ์ดตัวอักษรสวยงาม พร้อมเสียงอ่านออกเสียงภาษาไทยชัดเจน
- **สระและวรรณยุกต์**: สระเดี่ยว สระประสม วรรณยุกต์ ๔ รูป ๕ เสียง
- **อักษร ๓ หมู่ (ไตรยางศ์)**: จัดกลุ่มอักษรกลาง อักษรสูง อักษรต่ำ พร้อมบทท่องจำ
- **ตัวเลขไทย (๑ - ๑٠)**: ฝึกนับและจดจำตัวเลขไทย

### ✏️ 2. ระบบฝึกเขียนคัดลายมือตามรอยประ (Kid Tracing & Writing Canvas)
- **ลากเส้นตามเส้นประอัจฉริยะ**: ระบบสกัดเส้นเวกเตอร์ตัวอักษรแบบ Real-time พร้อมแสดงเส้นประตรงกลาง (Dotted Centerline)
- **ตัวช่วยบอกทิศทาง**: มีตัวเลขลำดับเส้น (1, 2, 3...) และลูกศรบอกทิศทางการลากเส้นที่ถูกต้อง
- **กระดานสมุดคัดลายมือ**: ลายเส้นบรรทัดสมุดน่ารักสำหรับเด็ก
- **เปลี่ยนตัวอักษรต่อเนื่อง (Next / Prev)**: มีปุ่มลอยข้างจอและปุ่มควบคุมด้านล่างเพื่อเลื่อนไปยังตัวอักษรถัดไปหรือก่อนหน้าได้อย่างราบรื่น

### 🎨 3. โหมดระบายสีภาพสัตว์และคำศัพท์ (Animal & Vocabulary Coloring)
- **กดค้างที่การ์ดเพื่อไประบายสี (Long-Press to Color)**: เมื่อกดค้างที่การ์ดรูปสัตว์ (Animals & Sounds), ผลไม้ (Fruits), ยานพาหนะ (Vehicles), อวัยวะ (Body Parts) หรือสีประจำวัน จะเปิดกระดานลงสีภาพสัตว์ตัวนั้นทันที
- **ชุดสีเทียนสดใส (Crayon Box)**: มีสีเทียน 6 สีสดใส พร้อมยางลบและระบบปรับขนาดเส้น
- **ระบบตรวจจับการลงสี**: ตรวจจับพื้นที่การระบายสีและสะสมดาวรางวัล

### ⭐ 4. ระบบรางวัลและเสียงเชียร์ (Gamification & Celebration)
- **สะสมดาว 3 ดวง (⭐ ⭐ ⭐)**: แถบสถานะสะสมดาวพร้อมเอฟเฟกต์เสียงและอนิเมชัน
- **กล่องยินดีต้อนรับ (Celebration Dialog)**: เมื่อเขียนหรือระบายสีสำเร็จ จะมีเสียงฉลองความสำเร็จ พร้อมปุ่ม **"ถัดไป ➡️"** เพื่อเปลี่ยนไปทำข้อถัดไปทันที

### 🔊 5. ระบบช่วยอ่านและมินิเกม (Auto-Read & Mini Quiz)
- **โหมดอ่านอัตโนมัติ (Auto-Read)**: เล่นเสียงอ่านต่อเนื่องทีละตัวอย่างเป็นจังหวะ
- **มินิเกมสุ่มทายเสียง (Audio Quiz)**: ฟังเสียงแล้วแตะเลือกการ์ดที่ถูกต้องเพื่อสะสมคะแนน

### 📺 6. คลังวิดีโอสื่อการเรียนรู้สำหรับเด็ก (Kids Video Watch Hub - Supabase)
- **ระบบคัดกรองเนื้อหาสำหรับเด็ก (Kid Educational Filtering)**: ดึงข้อมูลวิดีโอการเรียนรู้จาก **Supabase API** โดยเน้นเนื้อหาที่ปลอดภัยและเสริมสร้างพัฒนาการเด็ก
- **Smart Hybrid Video Player**: 
  - เล่นสื่อผ่าน **Custom WebView Player** ที่มีระบบ Auto-Play และซ่อน UI ที่ไม่จำเป็น
  - รองรับการสลับไปเล่นบน **ExoPlayer (Progressive Media)** โดยอัตโนมัติหากเป็นลิงก์ไฟล์วิดีโอโดยตรง (Direct MP4/Media Stream)
  - ระบบข้าม Cookie Challenge และตั้งค่า Referer Header อัตโนมัติ (เช่น TeraBox Link Handling)
- **Child-Friendly Video Detail**: หน้าจอเล่นวิดีโอออกแบบด้วยไอคอนสีสดใส พร้อมหมวดหมู่รีแอคชัน (Reaction Chips & Storybook Cards)

### 🎮 8. ศูนย์รวม 10 เกมการศึกษาสำหรับเด็ก (Kids Game Hub)
- **10 เกมเสริมพัฒนาการครบวงจร** ออกแบบให้เล่นง่าย สนุก และได้ความรู้:
  1. **เกมจับคู่เงาตัวอักษร (Shadow Matching)**: ลากวางตัวอักษรให้ตรงกับเงา
  2. **เกมเรียงลำดับ ก-ฮ (Alphabet Order)**: วางตัวอักษรตามลำดับพยัญชนะไทย
  3. **เกมต่อจิ๊กซอว์ภาพ (Jigsaw Puzzle)**: ต่อภาพสัตว์และตัวอักษรฝึกมิติสัมพันธ์
  4. **เกมฟังเสียงเลือกภาพ (Sound Quiz)**: ฟังการออกเสียงแล้วเลือกภาพที่ถูกต้อง
  5. **เกมการ์ดจับคู่ความจำ (Memory Cards)**: พลิกการ์ดจับคู่ฝึกสมาธิและความจำ
  6. **เกมนับเลขแสนสนุก (Number Counting)**: นับจำนวนสิ่งของและเลือกตัวเลข
  7. **เกมผสมสระคำศัพท์ (Vowel Builder)**: ฝึกผสมพยัญชนะกับสระพื้นฐาน
  8. **เกมผจญภัยเขาวงกต (Alphabet Maze)**: ลากเส้นผ่านเขาวงกตเพื่อเก็บตัวอักษร
  9. **เกมทายเสียงสัตว์และยานพาหนะ (Audio Match)**: ฟังเสียงร้องหรือเครื่องยนต์แล้วทาย
  10. **เกมระบายสีตามตัวเลข (Color by Number)**: เติมสีตามตัวเลขฝึกสมาธิและความคิดสร้างสรรค์
- เข้าถึงได้ง่ายผ่าน **Side Navigation Drawer** และแถบทางลัดหน้าแรก

### 👑 9. ระบบสนับสนุนสมาชิก VIP (Google Play Subscriptions v8.0.0)
- **เชื่อมต่อ Google Play Billing Library 8.0.0**: อัปเดตตามมาตรฐานล่าสุดของ Google Play รองรับ `QueryProductDetailsResult` และความปลอดภัยระดับสูง
- **3 แพ็กเกจสนับสนุนครอบคลุม**:
  - **รายเดือน (`sub_monthly`)**: ฿29 / เดือน รับโบนัส 20 ดาว ⭐
  - **รายปี (`sub_yearly`)**: ฿199 / ปี เฉลี่ยเพียง ฿16/เดือน รับโบนัส 100 ดาว ⭐ (ยอดนิยม)
  - **ตลอดชีพ (`sub_lifetime`)**: ฿399 ครั้งเดียวจบ สิทธิ VIP ถาวรตลอดชีพ รับโบนัส 300 ดาว ⭐
- **สิทธิพิเศษสำหรับสมาชิก VIP (100% Ad-Free & Bonuses)**:
  - 🚫 **ปิดโฆษณาทุกรูปแบบ 100%**: ปิดทั้ง Banner ด้านล่าง และ Interstitial Ads คั่นระหว่างหน้าโดยอัตโนมัติ
  - ⭐ **โบนัสดาวสะสม x2**: ทำกิจกรรม ทำควิซ และเล่นเกมได้ดาวเร็วขึ้น 2 เท่า
  - 🎮 **ปลดล็อกเนื้อหาครบทุกหมวดหมู่และ 10 มินิเกม**
  - 👑 **ป้ายมงกุฎเกียรติยศ VIP**: แสดงบนหน้าโปรไฟล์และผู้นำ
  - ↺ **กู้คืนการซื้ออัตโนมัติ (Restore Purchases)**: รองรับการกู้คืนสิทธิ์สมาชิกข้ามเครื่องได้ทันที
  - ⚙️ **Deep Link จัดการการสมัคร**: เปิดหน้าจัดการหรือยกเลิกการสมัครใน Google Play Store ได้ตามข้อกำหนดสากล
  - 🛠️ **Sandbox / Test Mode**: มีระบบทดสอบสำหรับนักพัฒนาและ QA ใช้งานได้ทันทีแม้ยังไม่ได้ Publish ใน Play Console
- **เอกสารคู่มืออย่างละเอียด**: ดูเพิ่มเติมได้ใน [`GOOGLE_PLAY_SUBSCRIPTIONS_GUIDE.md`](GOOGLE_PLAY_SUBSCRIPTIONS_GUIDE.md)

### 📊 10. ระบบคะแนน เลเวล และโปรไฟล์เชื่อมโยงแบบ Real-time (Score & Sync Engine)
- **การเชื่อมโยงข้อมูลแบบ 2 ทาง**: คะแนนดาว (Stars) และระดับเลเวล (Level) ในหน้าหลัก (Main Screen) และหน้าโปรไฟล์ (Profile Screen) อ้างอิงและอัปเดตสอดคล้องกันแบบ Real-time
- **Supabase Cloud & Local Sync**: เก็บข้อมูลปลอดภัยทั้งใน Local Preferences และซิงค์ขึ้น Supabase Backend อัตโนมัติ

---

## 🛠️ สถาปัตยกรรมและเทคโนโลยี (Tech Stack)

### Modular Architecture
- `:app` - แอปพลิเคชันหลัก, Navigation Drawer, Game Hub, และ UI รวมฟีเจอร์
- `:profilefeature` - หน้าโปรไฟล์เด็ก, ระบบ Google Play Billing Manager (`PlayBillingManager`), หน้าต่างเลือกแพ็กเกจสมาชิก (`SubscriptionDialog`), และ `SupabaseDataManager`
- `:videowatchfeatureSupabase` - โมดูลเล่นวิดีโอเด็ก เชื่อมต่อ Supabase API, Custom Auto-Play WebView, ExoPlayer, และระบบกรองโฆษณา VIP (`InterstitialAdManager`)
- `:videowatchfeature` - โมดูลจัดการรายการวิดีโอพื้นฐาน (Legacy)
- `:kidswebviewfeature` - โมดูล WebView ปลอดภัยสำหรับเด็ก พัฒนาด้วย Jetpack Compose
- `:livefeature` - โมดูลรองรับเนื้อหาถ่ายทอดสด
- `:custom-ui` - รวม Custom Views เช่น `KidTracingCanvasView`
- `:core` - ยูทิลิตี้และโค้ดส่วนกลาง

### Core Technologies
- **Languages**: Kotlin 100%, Java
- **UI Framework**: Jetpack Compose (BOM 2024.06.00), XML Layouts, Custom Canvas Engine (`KidTracingCanvasView`), Material 3
- **Google Play In-App Billing**: Play Billing Library v8.0.0 (API ล่าสุดสำหรับ Subscriptions & In-App Purchases)
- **Backend & Database**: Supabase REST API (PostgREST integration) + Local SharedPreferences Cache
- **Video & Media**: ExoPlayer (Media3), Custom Android `WebView` with JS Auto-Trigger & Referer handling
- **Monetization & Ads**: Google AdMob SDK 23.1.0 พร้อมระบบตรวจสอบปิดโฆษณา 100% สำหรับสมาชิก VIP
- **Graphics & Vector**: Android `Canvas`, `PathMeasure`, `Paint.getTextPath()`, Vector Drawables
- **Audio & Speech**: `MediaPlayer`, Android `TextToSpeech` (Thai / English)
- **Animations**: `YoYo Animations`, Jetpack Compose Transition APIs
- **Build System**: Gradle 8.12+, Android SDK 35, Java 11

---

## 🚀 การติดตั้งและรันโปรเจกต์ (Getting Started)

1. Clone repository:
```bash
git clone https://github.com/PongPloy2016/Practice_Writing_Reading-_Coloring_Thai_Alphabet_App.git
```
2. เปิดโปรเจกต์ด้วย **Android Studio (Ladybug หรือใหม่กว่า)**
3. Sync Gradle และ Build โปรเจกต์:
```bash
./gradlew assembleDebug
```

---

## 📢 การตั้งค่าโฆษณา AdMob (AdMob & Third-Party Mediation Setup)

### 📋 ข้อมูลรหัสโฆษณา AdMob (AdMob Credentials)
- **ชื่อแอปในระบบ AdMob**: ฝึกเขียน อ่าน ก ไก่ABC-ระบายสี
- **รหัสแอป AdMob (AdMob App ID)**: `ca-app-pub-3279793802315716~2607584711`
- **รหัสหน่วยโฆษณา AdMob (Ad Unit ID)**: `ca-app-pub-3279793802315716/8581063137`
- **การยืนยันสิทธิ์แอป (app-ads.txt)**: `google.com, pub-3279793802315716, DIRECT, f08c47fec0942fa0`

### 🛠️ วิธีป้อนรายละเอียดหน่วยโฆษณา AdMob ในแพลตฟอร์มสื่อกลางของบุคคลที่สาม (Third-Party Mediation Platform Setup)

ถัดจากนั้น ให้ป้อนรายละเอียด AdMob ในแพลตฟอร์มสื่อกลางของบุคคลที่สาม
1. **ลงชื่อเข้าใช้บัญชีในแพลตฟอร์มสื่อกลางของบุคคลที่สาม**: (เช่น AppLovin MAX, Unity LevelPlay, ironSource, Meta ฯลฯ)
2. **ใช้รายละเอียดต่อไปนี้เพื่อจับคู่หน่วยโฆษณา AdMob ในแพลตฟอร์มสื่อกลาง**:
   - **รหัสแอป AdMob**:
     `ca-app-pub-3279793802315716~2607584711`
   - **รหัสหน่วยโฆษณา AdMob**:
     `ca-app-pub-3279793802315716/8581063137`
3. **บันทึกการตั้งค่า**: กดบันทึกข้อมูลเพื่อเปิดใช้งานการรับส่งโฆษณาผ่านแพลตฟอร์มสื่อกลาง

