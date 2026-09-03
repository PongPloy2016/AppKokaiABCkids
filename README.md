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

### 🌐 7. ระบบ Safe Browsing สำหรับเด็ก (Kids WebView Feature)
- **Kids Web Screen**: หน้าเว็บสำหรับเด็กปลอดภัย พัฒนาด้วย **Jetpack Compose** ร่วมกับ **Fragment Architecture**

---

## 🛠️ สถาปัตยกรรมและเทคโนโลยี (Tech Stack)

### Modular Architecture
- `:app` - แอปพลิเคชันหลัก และ UI รวมฟีเจอร์
- `:videowatchfeatureSupabase` - โมดูลเล่นวิดีโอเด็ก เชื่อมต่อ Supabase API, Custom Auto-Play WebView และ ExoPlayer
- `:videowatchfeature` - โมดูลจัดการรายการวิดีโอพื้นฐาน
- `:kidswebviewfeature` - โมดูล WebView ปลอดภัยสำหรับเด็ก พัฒนาด้วย Jetpack Compose
- `:livefeature` - โมดูลรองรับเนื้อหาถ่ายทอดสด
- `:custom-ui` - รวม Custom Views เช่น `KidTracingCanvasView`
- `:core` - ยูทิลิตี้และโค้ดส่วนกลาง

### Core Technologies
- **Languages**: Kotlin 100%, Java
- **UI Framework**: Jetpack Compose, XML Layouts, Custom Canvas Engine (`KidTracingCanvasView`)
- **Backend & Database**: Supabase REST API (PostgREST integration)
- **Video & Media**: ExoPlayer (Media3), Custom Android `WebView` with JS Auto-Trigger & Referer handling
- **Graphics & Vector**: Android `Canvas`, `PathMeasure`, `Paint.getTextPath()`, Vector Drawables
- **Audio & Speech**: `MediaPlayer`, Android `TextToSpeech` (Thai / English)
- **Animations**: `YoYo Animations`, Jetpack Compose Transition APIs
- **Build System**: Gradle 8.12+, Android SDK 35

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
