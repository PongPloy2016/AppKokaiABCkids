Spark
เบต้า
แชทใหม่
ค้นหาแชท
วิดีโอ
คลัง
Notebook ใหม่
GoGoApp Travel Interface and Profile Management
Untitled notebook
Notebook ทั้งหมด
Spring Boot Project Creation Solutions
แปลงแอป ก-ฮ เป็น Jetpack Compose
ยอดหนี้ กยศ. ไม่ตรงกัน ชี้แจงสาเหตุ
โอนบ้านที่ดินให้ลูก: ประเด็นกฎหมาย
แก้ชื่อแอปหายบนหน้าจอหลัก
แจ้งเคลมประกันรอยขูดฟุตบาท
ถอดแบต MacBook 2020 ใช้งานได้ไหม
รหัสส่วนลด Hostinger เพิ่มเติม
USB Format สำหรับ Mac และ Windows
แก้ปัญหา ISDone.dll / Unarc.dll error code -7
แก้ไขภาพ เพิ่มพวงมาลัยมะลิ
แก้ไขภาพ: เพิ่มพวงมาลัยมะลิ
ลบคนในภาพวัด
วิดีโอเหตุการณ์กราดยิงต่างประเทศ
คลิปวิดีโอกราดยิงในสำนวนคดี
แก้ปัญหา MOHW.exe รันไม่ได้
ระบุชื่อเพจคลินิกเสริมความงาม
แก้ปัญหา BIOS/LEGACY BOOT OF UEFI-ONLY MEDIA
ระบุผักเรดโอ๊คและคำแนะนำ
แหล่งข่าว ยูทูบเบอร์เสียชีวิต จอร์เจีย
ถ้าหากว่า on the phone จะพาที่ไหนกับที่ไหนดี
ร้านบุฟเฟ่ต์พัทยา ไม่รองรับคนละครึ่ง
ขอโค้ดเว็บไซต์ Trip.com
โค้ด React Trip Planner App
หาโค้ดส่วนลด TeraBox และช่องทาง
การสนทนากับ Gemini
ช่วยทำเป็น file .md # Practice_Writing_Reading-_Coloring_Thai_Alphabet_App

Practice_Writing_Reading _Coloring_Thai_Alphabet_App



แผนงานปรับปรุงหน้า "ฝึกเขียน ก ไก่ - ABC" เป็น Jetpack Compose (Kotlin) ตามดีไซน์โปสเตอร์การ์ตูน

สรุปภาพรวม (Overview)

เปลี่ยนหน้าจอแสดงรายการตัวอักษรสำหรับฝึกเขียน/อ่าน จากเดิมที่เป็น XML Layout (activity_menu.xml, MenuThaiLettersActivity.java, MenuActivityAZ.java) ที่แสดงการ์ด 2 ท่อน (บนขาว ล่างสี) แบบเก่า ให้เป็น Jetpack Compose (Kotlin) ทั้งหมด โดยจัดดีไซน์ใหม่ให้เป็นการ์ดโค้งมนสีสันสดใส (Candy / Pastel Colorful Cards) ตามแบบโปสเตอร์ฝึกอ่าน ก-ฮ ในภาพตัวอย่าง:



การ์ดตัวอักษรสีสันสดใส (Poster Card Grid):

พื้นหลังการ์ดแต่ละใบเป็นสีสันสดใสโทนเด็ก (ชมพู, ส้มพีช, เหลืองทอง, เขียวมิ้นต์, ฟ้าคราม, ม่วงพาสเทล ฯลฯ สลับสีสวยงาม)

ตัวอักษรตัวหนาขนาดใหญ่ สีขาวคมชัด พร้อมรูปภาพประกอบการ์ตูนของตัวอักษรนั้นๆ (เช่น ไก่, ไข่, ขวด, ควาย...)

คำอ่าน/กลอนคล้องจองภาษาไทยครบถ้วนใต้ตัวอักษร (เช่น "ก เอ๋ย ก ไก่", "ข ไข่ในเล้า", "ฃ ขวดของเรา", ... จนถึง "ฮ นกฮูกตาโต")

เครื่องหมายกำกับตัวอักษร ฃ และ ฅ พร้อมการ์ดแจ้งเตือนด้านล่างตามรูปต้นฉบับ: "พยัญชนะ ฃ และ ฅ ในปัจจุบันนี้ไม่ได้ใช้แล้ว"

ระบบ Auto-Read (อ่านออกเสียงอัตโนมัติ):

แถบควบคุม Auto Read ดีไซน์โมเดิร์นโค้งมน พร้อมปุ่ม Start / Pause / Stop

แถบ Progress Bar แสดงความคืบหน้า (เช่น 1/44, 2/44...)

ไฮไลท์การ์ดที่กำลังอ่านอยู่ด้วย Animation ขยายและมีเอฟเฟกต์เรืองแสง พร้อม Auto Scroll เลื่อนตามอัตโนมัติ

รองรับทั้งไฟล์เสียงจริง (R.raw.k1 - k44) และ fallback ไปยัง TextToSpeech ภาษาไทย (th-TH)

รองรับทั้ง ก-ฮ (Thai) และ A-Z (English):

มีแท็บสลับโหมด ภาษาไทย ก-ฮ (44 ตัว) และ English A-Z (26 ตัว)

เชื่อมต่อไปยังหน้าฝึกเขียน ThaiLettersActivity / FullScreenActivity ได้อย่างราบรื่น 100%

User Review Required

IMPORTANT



หน้า MenuThaiLettersActivity จะถูกแปลงเป็น Kotlin + Jetpack Compose โดยยังคง Intent Parameter ที่เรียกเข้าและส่งออกไปยัง ThaiLettersActivity ไว้อย่างครบถ้วน ทำให้การทำงานเดิมไม่สะดุด

การ์ดจะใช้รูปภาพการ์ตูนและข้อความคล้องจอง ก-ฮ ครบ 44 ตัวตามภาพโปสเตอร์ตัวอย่าง

Proposed Changes

1. Data Model & Repository (Kotlin)

[NEW] ThaiAlphabetData.kt

สร้าง Data Class AlphabetItem(id, letter, name, rhyme, imageRes, audioRes, isObsolete, bgColor)

รวบรวมข้อมูลครบ 44 ตัวอักษร ก-ฮ พร้อมกลอนคล้องจองและคู่สีพาสเทลสดใส 7 เฉดสี

รวบรวมข้อมูลตัวอักษรภาษาอังกฤษ A-Z ครบ 26 ตัว (A for Apple, B for Ball...)

2. Compose UI Screens & Components

[NEW] AlphabetMenuComposeScreen.kt

Header Section: แบนเนอร์หัวข้อสไตล์โปสเตอร์ "แบบฝึกอ่านและเขียนพยัญชนะภาษาไทย" พร้อมไอคอนน่ารัก

Tab Selector: สลับระหว่าง "ภาษาไทย (ก-ฮ)" และ "English (A-Z)"

Auto-Read Control Bar: แถบแก้วมนเงา (Glassmorphism / Rounded Card) มีปุ่ม Start/Stop, ไอคอนคลื่นเสียง, หลอด Progress

Alphabet Grid: LazyVerticalGrid ปรับจำนวนคอลัมน์อัตโนมัติ (3-4 คอลัมน์บนมือถือ, 6 คอลัมน์บนแท็บเล็ต/แนวนอนเหมือนโปสเตอร์)

Alphabet Card Item:

การ์ดมน RoundedCornerShape(16.dp) สีสันตามธีม

ตัวอักษรพยัญชนะเด่นชัด ตัวหนา สีขาว

รูปภาพประกอบการ์ตูนตรงกลาง

คำอ่านกลอน ก เอ๋ย ก ไก่ / Rhyme ชัดเจน

เอฟเฟกต์การกด (Bounce Scale Animation), ปุ่มรูปลำโพงฟังเสียง, แตะการ์ดเพื่อเปิดหน้าฝึกเขียน

Footer Section: กล่องข้อความแจ้งเตือนสีขาว-ฟ้า "พยัญชนะ ฃ และ ฅ ในปัจจุบันนี้ไม่ได้ใช้แล้ว" ตามรูปโปสเตอร์เป๊ะๆ

3. Activity Integration

[MODIFY] MenuThaiLettersActivity.kt (แทนที่ MenuThaiLettersActivity.java)

แปลงเป็น Kotlin ComponentActivity / AppCompatActivity ที่ใช้ setContent { AlphabetMenuComposeScreen(...) }

จัดการ TextToSpeech, MediaPlayer, Auto-Read Handler ในระดับ Lifecycle

นำทางไปยัง ThaiLettersActivity เมื่อเด็กๆ แตะการ์ดตัวอักษร

[MODIFY] MenuActivityAZ.java / MainComposeScreen.kt

ปรับปรุงให้สามารถเปิด MenuThaiLettersActivity หรือส่ง Intent Mode (MODE_THAI / MODE_ENGLISH) ได้อย่างไร้รอยต่อ

Verification Plan

Manual Verification

เปิดหน้า "ฝึกเขียน ก - ฮ" จากเมนูหลัก และตรวจดู UI ใหม่ที่เป็น Jetpack Compose

ตรวจสอบสีสัน การจัดวางการ์ด ตัวหนังสือภาษาไทย และคำกลอน ก-ฮ ครบทั้ง 44 ตัว

ทดสอบกดปุ่ม "Start" บนแถบ Auto Read ตรวจสอบว่ามีเสียงอ่านและเลื่อนหน้าจออัตโนมัติ

ทดสอบแตะที่ตัวการ์ดเพื่อเข้าไปหน้าฝึกเขียน ThaiLettersActivity ว่าส่งค่า index และชื่อตัวอักษรได้ถูกต้อง

ทดสอบสลับแท็บไปยังโหมด A-Z และทดลองฟังเสียง A-Z

Python
md_content = """# Practice_Writing_Reading-_Coloring_Thai_Alphabet_App

## แผนงานปรับปรุงหน้า "ฝึกเขียน ก ไก่ - ABC" เป็น Jetpack Compose (Kotlin) ตามดีไซน์โปสเตอร์การ์ตูน

---

### สรุปภาพรวม (Overview)
เปลี่ยนหน้าจอแสดงรายการตัวอักษรสำหรับฝึกเขียน/อ่าน จากเดิมที่เป็น XML Layout (`activity_menu.xml`, `MenuThaiLettersActivity.java`, `MenuActivityAZ.java`) ที่แสดงการ์ด 2 ท่อน (บนขาว ล่างสี) แบบเก่า ให้เป็น **Jetpack Compose (Kotlin)** ทั้งหมด โดยจัดดีไซน์ใหม่ให้เป็นการ์ดโค้งมนสีสันสดใส (*Candy / Pastel Colorful Cards*) ตามแบบโปสเตอร์ฝึกอ่าน ก-ฮ ในภาพตัวอย่าง:

* **การ์ดตัวอักษรสีสันสดใส (Poster Card Grid):**
  * พื้นหลังการ์ดแต่ละใบเป็นสีสันสดใสโทนเด็ก (ชมพู, ส้มพีช, เหลืองทอง, เขียวมิ้นต์, ฟ้าคราม, ม่วงพาสเทล ฯลฯ สลับสีสวยงาม)
  * ตัวอักษรตัวหนาขนาดใหญ่ สีขาวคมชัด พร้อมรูปภาพประกอบการ์ตูนของตัวอักษรนั้นๆ (เช่น ไก่, ไข่, ขวด, ควาย...)
  * คำอ่าน/กลอนคล้องจองภาษาไทยครบถ้วนใต้ตัวอักษร (เช่น *"ก เอ๋ย ก ไก่"*, *"ข ไข่ในเล้า"*, *"ฃ ขวดของเรา"*, ... จนถึง *"ฮ นกฮูกตาโต"*)
  * เครื่องหมายกำกับตัวอักษร **ฃ** และ **ฅ** พร้อมการ์ดแจ้งเตือนด้านล่างตามรูปต้นฉบับ: *"พยัญชนะ ฃ และ ฅ ในปัจจุบันนี้ไม่ได้ใช้แล้ว"*
* **ระบบ Auto-Read (อ่านออกเสียงอัตโนมัติ):**
  * แถบควบคุม Auto Read ดีไซน์โมเดิร์นโค้งมน พร้อมปุ่ม Start / Pause / Stop
  * แถบ Progress Bar แสดงความคืบหน้า (เช่น 1/44, 2/44...)
  * ไฮไลท์การ์ดที่กำลังอ่านอยู่ด้วย Animation ขยายและมีเอฟเฟกต์เรืองแสง พร้อม Auto Scroll เลื่อนตามอัตโนมัติ
  * รองรับทั้งไฟล์เสียงจริง (`R.raw.k1` - `k44`) และ fallback ไปยัง TextToSpeech ภาษาไทย (`th-TH`)
* **รองรับทั้ง ก-ฮ (Thai) และ A-Z (English):**
  * มีแท็บสลับโหมด ภาษาไทย ก-ฮ (44 ตัว) และ English A-Z (26 ตัว)
  * เชื่อมต่อไปยังหน้าฝึกเขียน `ThaiLettersActivity` / `FullScreenActivity` ได้อย่างราบรื่น 100%

---

### User Review Required
> [!IMPORTANT]
> * หน้า `MenuThaiLettersActivity` จะถูกแปลงเป็น Kotlin + Jetpack Compose โดยยังคง Intent Parameter ที่เรียกเข้าและส่งออกไปยัง `ThaiLettersActivity` ไว้อย่างครบถ้วน ทำให้การทำงานเดิมไม่สะดุด
> * การ์ดจะใช้รูปภาพการ์ตูนและข้อความคล้องจอง ก-ฮ ครบ 44 ตัวตามภาพโปสเตอร์ตัวอย่าง

---

### Proposed Changes

#### 1. Data Model & Repository (Kotlin)
* **`[NEW] ThaiAlphabetData.kt`**
  * สร้าง Data Class `AlphabetItem(id, letter, name, rhyme, imageRes, audioRes, isObsolete, bgColor)`
  * รวบรวมข้อมูลครบ 44 ตัวอักษร ก-ฮ พร้อมกลอนคล้องจองและคู่สีพาสเทลสดใส 7 เฉดสี
  * รวบรวมข้อมูลตัวอักษรภาษาอังกฤษ A-Z ครบ 26 ตัว (*A for Apple, B for Ball...*)

#### 2. Compose UI Screens & Components
* **`[NEW] AlphabetMenuComposeScreen.kt`**
  * **Header Section:** แบนเนอร์หัวข้อสไตล์โปสเตอร์ *"แบบฝึกอ่านและเขียนพยัญชนะภาษาไทย"* พร้อมไอคอนน่ารัก
  * **Tab Selector:** สลับระหว่าง *"ภาษาไทย (ก-ฮ)"* และ *"English (A-Z)"*
  * **Auto-Read Control Bar:** แถบแก้วมนเงา (Glassmorphism / Rounded Card) มีปุ่ม Start/Stop, ไอคอนคลื่นเสียง, หลอด Progress
  * **Alphabet Grid:** `LazyVerticalGrid` ปรับจำนวนคอลัมน์อัตโนมัติ (3-4 คอลัมน์บนมือถือ, 6 คอลัมน์บนแท็บเล็ต/แนวนอนเหมือนโปสเตอร์)
  * **Alphabet Card Item:**
    * การ์ดมน `RoundedCornerShape(16.dp)` สีสันตามธีม
    * ตัวอักษรพยัญชนะเด่นชัด ตัวหนา สีขาว
    * รูปภาพประกอบการ์ตูนตรงกลาง
    * คำอ่านกลอน ก เอ๋ย ก ไก่ / Rhyme ชัดเจน
    * เอฟเฟกต์การกด (Bounce Scale Animation), ปุ่มรูปลำโพงฟังเสียง, แตะการ์ดเพื่อเปิดหน้าฝึกเขียน
  * **Footer Section:** กล่องข้อความแจ้งเตือนสีขาว-ฟ้า *"พยัญชนะ ฃ และ ฅ ในปัจจุบันนี้ไม่ได้ใช้แล้ว"* ตามรูปโปสเตอร์เป๊ะๆ

#### 3. Activity Integration
* **`[MODIFY] MenuThaiLettersActivity.kt`** *(แทนที่ MenuThaiLettersActivity.java)*
  * แปลงเป็น Kotlin `ComponentActivity` / `AppCompatActivity` ที่ใช้ `setContent { AlphabetMenuComposeScreen(...) }`
  * จัดการ TextToSpeech, MediaPlayer, Auto-Read Handler ในระดับ Lifecycle
  * นำทางไปยัง `ThaiLettersActivity` เมื่อเด็กๆ แตะการ์ดตัวอักษร
* **`[MODIFY] MenuActivityAZ.java / MainComposeScreen.kt`**
  * ปรับปรุงให้สามารถเปิด `MenuThaiLettersActivity` หรือส่ง Intent Mode (`MODE_THAI` / `MODE_ENGLISH`) ได้อย่างไร้รอยต่อ

---

### Verification Plan

#### Manual Verification
1. **ตรวจสอบ UI ใหม่:** เปิดหน้า *"ฝึกเขียน ก - ฮ"* จากเมนูหลัก และตรวจดู UI ใหม่ที่เป็น Jetpack Compose
2. **ความถูกต้องของเนื้อหา:** ตรวจสอบสีสัน การจัดวางการ์ด ตัวหนังสือภาษาไทย และคำกลอน ก-ฮ ครบทั้ง 44 ตัว
3. **ระบบ Auto Read:** ทดสอบกดปุ่ม *"Start"* บนแถบ Auto Read ตรวจสอบว่ามีเสียงอ่านและเลื่อนหน้าจออัตโนมัติ
4. **การเชื่อมต่อหน้าฝึกเขียน:** ทดสอบแตะที่ตัวการ์ดเพื่อเข้าไปหน้าฝึกเขียน `ThaiLettersActivity` ว่าส่งค่า index และชื่อตัวอักษรได้ถูกต้อง
5. **โหมดภาษาอังกฤษ:** ทดสอบสลับแท็บไปยังโหมด A-Z และทดลองฟังเสียง A-Z
"""

filename = "Practice_Writing_Reading-_Coloring_Thai_Alphabet_App.md"
with open(filename, "w", encoding="utf-8") as f:
    f.write(md_content.strip() + "\n")

print(f"File created successfully: {filename}")

เอาต์พุตของโค้ด
File created successfully: Practice_Writing_Reading-_Coloring_Thai_Alphabet_App.md

Your Markdown (.md) file is ready

ไอคอน MD
Practice_Writing_Reading-_Coloring_Thai_Alphabet_App
 MD 
เปิดอยู่
สร้างไฟล์ Practice_Writing_Reading-_Coloring_Thai_Alphabet_App.md เรียบร้อยแล้ว โดยจัดรูปแบบโครงสร้างเอกสาร Markdown ให้สวยงาม ครบถ้วนตามหัวข้อ สรุปภาพรวม แผนงาน Compose UI, Data Model, Activity Integration และ Verification Plan ทั้งหมดครับ




Gemini เป็น AI และอาจทำผิดพลาดได้

กำลังวิเคราะห์
# Practice_Writing_Reading-_Coloring_Thai_Alphabet_App

## แผนงานปรับปรุงหน้า "ฝึกเขียน ก ไก่ - ABC" เป็น Jetpack Compose (Kotlin) ตามดีไซน์โปสเตอร์การ์ตูน

---

### สรุปภาพรวม (Overview)
เปลี่ยนหน้าจอแสดงรายการตัวอักษรสำหรับฝึกเขียน/อ่าน จากเดิมที่เป็น XML Layout (`activity_menu.xml`, `MenuThaiLettersActivity.java`, `MenuActivityAZ.java`) ที่แสดงการ์ด 2 ท่อน (บนขาว ล่างสี) แบบเก่า ให้เป็น **Jetpack Compose (Kotlin)** ทั้งหมด โดยจัดดีไซน์ใหม่ให้เป็นการ์ดโค้งมนสีสันสดใส (*Candy / Pastel Colorful Cards*) ตามแบบโปสเตอร์ฝึกอ่าน ก-ฮ ในภาพตัวอย่าง:

* **การ์ดตัวอักษรสีสันสดใส (Poster Card Grid):**
  * พื้นหลังการ์ดแต่ละใบเป็นสีสันสดใสโทนเด็ก (ชมพู, ส้มพีช, เหลืองทอง, เขียวมิ้นต์, ฟ้าคราม, ม่วงพาสเทล ฯลฯ สลับสีสวยงาม)
  * ตัวอักษรตัวหนาขนาดใหญ่ สีขาวคมชัด พร้อมรูปภาพประกอบการ์ตูนของตัวอักษรนั้นๆ (เช่น ไก่, ไข่, ขวด, ควาย...)
  * คำอ่าน/กลอนคล้องจองภาษาไทยครบถ้วนใต้ตัวอักษร (เช่น *"ก เอ๋ย ก ไก่"*, *"ข ไข่ในเล้า"*, *"ฃ ขวดของเรา"*, ... จนถึง *"ฮ นกฮูกตาโต"*)
  * เครื่องหมายกำกับตัวอักษร **ฃ** และ **ฅ** พร้อมการ์ดแจ้งเตือนด้านล่างตามรูปต้นฉบับ: *"พยัญชนะ ฃ และ ฅ ในปัจจุบันนี้ไม่ได้ใช้แล้ว"*
* **ระบบ Auto-Read (อ่านออกเสียงอัตโนมัติ):**
  * แถบควบคุม Auto Read ดีไซน์โมเดิร์นโค้งมน พร้อมปุ่ม Start / Pause / Stop
  * แถบ Progress Bar แสดงความคืบหน้า (เช่น 1/44, 2/44...)
  * ไฮไลท์การ์ดที่กำลังอ่านอยู่ด้วย Animation ขยายและมีเอฟเฟกต์เรืองแสง พร้อม Auto Scroll เลื่อนตามอัตโนมัติ
  * รองรับทั้งไฟล์เสียงจริง (`R.raw.k1` - `k44`) และ fallback ไปยัง TextToSpeech ภาษาไทย (`th-TH`)
* **รองรับทั้ง ก-ฮ (Thai) และ A-Z (English):**
  * มีแท็บสลับโหมด ภาษาไทย ก-ฮ (44 ตัว) และ English A-Z (26 ตัว)
  * เชื่อมต่อไปยังหน้าฝึกเขียน `ThaiLettersActivity` / `FullScreenActivity` ได้อย่างราบรื่น 100%

---

### User Review Required
> [!IMPORTANT]
> * หน้า `MenuThaiLettersActivity` จะถูกแปลงเป็น Kotlin + Jetpack Compose โดยยังคง Intent Parameter ที่เรียกเข้าและส่งออกไปยัง `ThaiLettersActivity` ไว้อย่างครบถ้วน ทำให้การทำงานเดิมไม่สะดุด
> * การ์ดจะใช้รูปภาพการ์ตูนและข้อความคล้องจอง ก-ฮ ครบ 44 ตัวตามภาพโปสเตอร์ตัวอย่าง

---

### Proposed Changes

#### 1. Data Model & Repository (Kotlin)
* **`[NEW] ThaiAlphabetData.kt`**
  * สร้าง Data Class `AlphabetItem(id, letter, name, rhyme, imageRes, audioRes, isObsolete, bgColor)`
  * รวบรวมข้อมูลครบ 44 ตัวอักษร ก-ฮ พร้อมกลอนคล้องจองและคู่สีพาสเทลสดใส 7 เฉดสี
  * รวบรวมข้อมูลตัวอักษรภาษาอังกฤษ A-Z ครบ 26 ตัว (*A for Apple, B for Ball...*)

#### 2. Compose UI Screens & Components
* **`[NEW] AlphabetMenuComposeScreen.kt`**
  * **Header Section:** แบนเนอร์หัวข้อสไตล์โปสเตอร์ *"แบบฝึกอ่านและเขียนพยัญชนะภาษาไทย"* พร้อมไอคอนน่ารัก
  * **Tab Selector:** สลับระหว่าง *"ภาษาไทย (ก-ฮ)"* และ *"English (A-Z)"*
  * **Auto-Read Control Bar:** แถบแก้วมนเงา (Glassmorphism / Rounded Card) มีปุ่ม Start/Stop, ไอคอนคลื่นเสียง, หลอด Progress
  * **Alphabet Grid:** `LazyVerticalGrid` ปรับจำนวนคอลัมน์อัตโนมัติ (3-4 คอลัมน์บนมือถือ, 6 คอลัมน์บนแท็บเล็ต/แนวนอนเหมือนโปสเตอร์)
  * **Alphabet Card Item:**
    * การ์ดมน `RoundedCornerShape(16.dp)` สีสันตามธีม
    * ตัวอักษรพยัญชนะเด่นชัด ตัวหนา สีขาว
    * รูปภาพประกอบการ์ตูนตรงกลาง
    * คำอ่านกลอน ก เอ๋ย ก ไก่ / Rhyme ชัดเจน
    * เอฟเฟกต์การกด (Bounce Scale Animation), ปุ่มรูปลำโพงฟังเสียง, แตะการ์ดเพื่อเปิดหน้าฝึกเขียน
  * **Footer Section:** กล่องข้อความแจ้งเตือนสีขาว-ฟ้า *"พยัญชนะ ฃ และ ฅ ในปัจจุบันนี้ไม่ได้ใช้แล้ว"* ตามรูปโปสเตอร์เป๊ะๆ

#### 3. Activity Integration
* **`[MODIFY] MenuThaiLettersActivity.kt`** *(แทนที่ MenuThaiLettersActivity.java)*
  * แปลงเป็น Kotlin `ComponentActivity` / `AppCompatActivity` ที่ใช้ `setContent { AlphabetMenuComposeScreen(...) }`
  * จัดการ TextToSpeech, MediaPlayer, Auto-Read Handler ในระดับ Lifecycle
  * นำทางไปยัง `ThaiLettersActivity` เมื่อเด็กๆ แตะการ์ดตัวอักษร
* **`[MODIFY] MenuActivityAZ.java / MainComposeScreen.kt`**
  * ปรับปรุงให้สามารถเปิด `MenuThaiLettersActivity` หรือส่ง Intent Mode (`MODE_THAI` / `MODE_ENGLISH`) ได้อย่างไร้รอยต่อ

---

### Verification Plan

#### Manual Verification
1. **ตรวจสอบ UI ใหม่:** เปิดหน้า *"ฝึกเขียน ก - ฮ"* จากเมนูหลัก และตรวจดู UI ใหม่ที่เป็น Jetpack Compose
2. **ความถูกต้องของเนื้อหา:** ตรวจสอบสีสัน การจัดวางการ์ด ตัวหนังสือภาษาไทย และคำกลอน ก-ฮ ครบทั้ง 44 ตัว
3. **ระบบ Auto Read:** ทดสอบกดปุ่ม *"Start"* บนแถบ Auto Read ตรวจสอบว่ามีเสียงอ่านและเลื่อนหน้าจออัตโนมัติ
4. **การเชื่อมต่อหน้าฝึกเขียน:** ทดสอบแตะที่ตัวการ์ดเพื่อเข้าไปหน้าฝึกเขียน `ThaiLettersActivity` ว่าส่งค่า index และชื่อตัวอักษรได้ถูกต้อง
5. **โหมดภาษาอังกฤษ:** ทดสอบสลับแท็บไปยังโหมด A-Z และทดลองฟังเสียง A-Z
