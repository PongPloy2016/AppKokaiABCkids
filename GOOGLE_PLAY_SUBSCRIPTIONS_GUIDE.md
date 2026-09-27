# คู่มือการใช้งานและตั้งค่าระบบ Google Play Billing Subscriptions (เวอร์ชัน 8.0.0)

เอกสารนี้รวบรวมรายละเอียด สถาปัตยกรรมโค้ด ขั้นตอนการตั้งค่าบน **Google Play Console** และการแก้ปัญหา (Troubleshooting) สำหรับระบบสมัครสมาชิก (Subscriptions) และการชำระเงินในแอปตามมาตรฐานทางการของ Google Play

---

## 📌 สารบัญ
1. [ภาพรวมสถาปัตยกรรมและไฟล์ที่เกี่ยวข้อง](#1-ภาพรวมสถาปัตยกรรมและไฟล์ที่เกี่ยวข้อง)
2. [รายการแพ็กเกจสินค้า (Product IDs)](#2-รายการแพ็กเกจสินค้า-product-ids)
3. [ขั้นตอนการตั้งค่าบน Google Play Console](#3-ขั้นตอนการตั้งค่าบน-google-play-console)
4. [การตั้งค่าผู้ทดสอบ (License Testing & Internal Testing)](#4-การตั้งค่าผู้ทดสอบ-license-testing--internal-testing)
5. [ฟีเจอร์สิทธิพิเศษ VIP และการปิดโฆษณา 100%](#5-ฟีเจอร์สิทธิพิเศษ-vip-และการปิดโฆษณา-100)
6. [การแก้ปัญหาที่พบบ่อย (Troubleshooting)](#6-การแก้ปัญหาที่พบบ่อย-troubleshooting)

---

## 1. ภาพรวมสถาปัตยกรรมและไฟล์ที่เกี่ยวข้อง

ระบบถูกพัฒนาขึ้นโดยใช้ **Google Play Billing Library v8.0.0** (เวอร์ชันล่าสุดที่รองรับ API ใหม่ `QueryProductDetailsResult`):

| ไฟล์ | หน้าที่และความรับผิดชอบ |
| :--- | :--- |
| [`PlayBillingManager.kt`](profilefeature/src/main/java/com/pongploydev/profilefeature/billing/PlayBillingManager.kt) | **ศูนย์กลางควบคุม Billing 8.0.0** ทั้งหมด เชื่อมต่อ BillingClient, ดึงข้อมูลราคาแบบ Real-time, เรียกหน้าต่างชำระเงิน Google Play, ยืนยันสิทธิ์ (`AcknowledgePurchase`), กู้คืนการซื้ออัตโนมัติ (`queryPurchasesAsync`) และ Deep Link จัดการการสมัคร |
| [`SubscriptionDialog.kt`](profilefeature/src/main/java/com/pongploydev/profilefeature/compose/SubscriptionDialog.kt) | **UI หน้าต่างเลือกแพ็กเกจสมาชิก VIP (Jetpack Compose)** แสดงราคาจริงที่ดึงจาก Google Play, แสดงสถานะสมาชิกปัจจุบัน, ปุ่มกู้คืนการซื้อ, และมีระบบ Sandbox Test Fallback สำหรับการทดสอบในโหมด Debug |
| [`SupabaseDataManager.kt`](profilefeature/src/main/java/com/pongploydev/profilefeature/data/SupabaseDataManager.kt) | จัดเก็บสถานะ VIP (`isVipSubscriber`), Tier ปัจจุบัน และบันทึกคะแนนดาวโบนัส ⭐ ลงใน Local Cache และ Cloud Database |
| [`InterstitialAdManager.kt`](videowatchfeatureSupabase/src/main/java/com/pongploydev/videowatchfeaturesupabase/utils/ads/InterstitialAdManager.kt) | ตรวจสอบสถานะ VIP ก่อนโหลดและแสดงโฆษณาคั่นระหว่างหน้า หากเป็นสมาชิก VIP จะข้ามโฆษณา 100% ทันที |
| [`MainActivity.kt`](app/src/main/java/com/pongploydev/priyankvex/paintapp/activity/MainActivity.kt) | เริ่มเชื่อมต่อ `PlayBillingManager` ทันทีเมื่อเปิดแอป และซ่อนแบนเนอร์โฆษณาเมื่อผู้ใช้เป็น VIP |
| [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) | ประกาศสิทธิ์ `<uses-permission android:name="com.android.vending.BILLING" />` |
| [`proguard-rules.pro`](app/proguard-rules.pro) | กำหนดกฎ Proguard Keep Rules ป้องกันการตัดคลาส BillingClient และ Data Model |

---

## 2. รายการแพ็กเกจสินค้า (Product IDs)

ในโค้ดได้กำหนดรหัสสินค้าไว้ 3 รายการ ซึ่งต้องตั้งค่าใน Google Play Console ให้ตรงกันทุกตัวอักษร:

| รายการ | รหัสสินค้า (Product ID) | ชนิดสินค้า (Type) | ราคาที่ตั้ง | สิทธิพิเศษ |
| :---: | :---: | :---: | :---: | :--- |
| **1. รายเดือน** | `sub_monthly` | การสมัครรับข้อมูล (Subscription) | ฿29 / เดือน | ปิดโฆษณารายเดือน + โบนัส 20 ดาว ⭐ |
| **2. รายปี** | `sub_yearly` | การสมัครรับข้อมูล (Subscription) | ฿199 / ปี | ปิดโฆษณาตลอดปี + โบนัส 100 ดาว ⭐ |
| **3. ตลอดชีพ** | `sub_lifetime` | ผลิตภัณฑ์แบบเรียกเก็บเงินครั้งเดียว (In-app Product) | ฿399 (ครั้งเดียว) | VIP ตลอดชีพ ปิดโฆษณาถาวร + โบนัส 300 ดาว ⭐ |

---

## 3. ขั้นตอนการตั้งค่าบน Google Play Console

### ก. การตั้งค่าโปรไฟล์ผู้ขาย (Merchant Profile)
1. ไปที่เมนู **การตั้งค่า (Settings)** -> **การชำระเงิน (Payments profile)**
2. ระบุชื่อธุรกิจ (เช่น `PongPloyDev`), หมวดสินค้า (เลือก *Digital goods / Digital content*), อีเมลสนับสนุน และชื่อใบแจ้งยอดบัตร (เช่น `PONGPLOY`)

### ข. การสร้างแพ็กเกจรายเดือน (`sub_monthly`) และรายปี (`sub_yearly`)
1. เข้า **Google Play Console** -> เลือกแอป -> เมนูด้านซ้าย: **สร้างรายได้ด้วย Play** -> **การสมัครรับข้อมูล (Subscriptions)**
2. กด **สร้างการสมัครใช้บริการ (Create subscription)**
3. กรอกรหัสสินค้า: `sub_monthly`
4. กรอกชื่อ: `ผู้สนับสนุนตัวน้อย (รายเดือน)`
5. เมื่อสร้างแล้ว เลื่อนลงมาที่ **"แพ็กเกจหลักและข้อเสนอ" (Base plans and offers)**:
   * กด **เพิ่มแพ็กเกจหลัก (Add base plan)**
   * รหัสแพ็กเกจ: `monthly-plan`
   * ประเภท: **ต่ออายุอัตโนมัติ (Auto-renewing)**
   * รอบการเรียกเก็บเงิน: **1 เดือน**
   * ราคา: **29 บาท (THB)**
   * **⚠️ สำคัญมาก:** กดปุ่ม **"เปิดใช้งาน" (Activate)** ที่มุมขวาบน เพื่อให้สถานะเปลี่ยนจาก "ฉบับร่าง" เป็น **"ใช้งานอยู่" (Active สีเขียว)**
6. ทำขั้นตอนเดียวกันสำหรับแพ็กเกจรายปี:
   * รหัสสินค้า: `sub_yearly`
   * ชื่อ: `สมาชิก VIP นักอ่าน (รายปี)`
   * Base Plan: `yearly-plan`, รอบ **1 ปี**, ราคา **199 บาท**, กด **เปิดใช้งาน (Activate)**

### ค. การสร้างแพ็กเกจตลอดชีพ (`sub_lifetime`)
1. เมนูด้านซ้าย: **สร้างรายได้ด้วย Play** -> **ผลิตภัณฑ์** -> **ผลิตภัณฑ์แบบเรียกเก็บเงินครั้งเดียว (In-app products)**
2. กด **สร้างผลิตภัณฑ์ (Create product)**
3. กรอกรหัสสินค้า: `sub_lifetime`
4. กรอกชื่อ: `ครอบครัวซูเปอร์คิดส์ (VIP ตลอดชีพ)`
5. กำหนดราคา: **399 บาท (THB)**
6. กด **บันทึก (Save)** และกด **"เปิดใช้งาน" (Activate)**

---

## 4. การตั้งค่าผู้ทดสอบ (License Testing & Internal Testing)

เพื่อให้สามารถทดสอบกดซื้อได้ฟรีโดยไม่ต้องตัดเงินจริง และไม่ติดเงื่อนไขความปลอดภัยของ Google:

### 1. ใส่เมลใน License Testing (ทดสอบฟรีไม่เสียเงิน)
1. เข้า Play Console -> เมนูด้านซ้ายล่าง: **การตั้งค่า (Settings)** -> **การทดสอบใบอนุญาต (License Testing)**
2. พิมพ์ **Gmail ของเครื่องทดสอบ** ลงในช่อง
3. เลือกรหัสตอบกลับเป็น **`RESPOND_NORMALLY`**
4. กด **บันทึก (Save)**

### 2. นำแอปขึ้นแทร็ก "การทดสอบภายใน" (Internal Testing)
1. Build ไฟล์ **App Bundle (`.aab`)** จาก Android Studio
2. ไปที่ Play Console -> เมนู **การทดสอบภายใน (Internal testing)** -> สร้างรุ่นใหม่และอัปโหลดไฟล์ `.aab`
3. ไปที่แท็บ **ผู้ทดสอบ (Testers)** -> คัดลอกลิงก์การเข้าร่วมการทดสอบ
4. เปิดลิงก์นั้นบนมือถือเครื่องทดสอบ แล้วกด **"เข้าร่วมโครงการทดสอบ" (Become a tester)**
5. ดาวน์โหลดหรืออัปเดตแอปผ่าน **Google Play Store โดยตรง** (ต้องไม่รันตรงจาก Android Studio เพื่อให้ Signature ตรงกับ Store)

---

## 5. ฟีเจอร์สิทธิพิเศษ VIP และการปิดโฆษณา 100%

เมื่อผู้ใช้ทำการสมัครสมาชิกสำเร็จ:
1. **Google Play Flow**: `PlayBillingManager` จะตรวจจับสถานะ `PURCHASED` และเรียก `acknowledgePurchase()` ยืนยันสิทธิ์ภายในทันที (ป้องกัน Google คืนเงินอัตโนมัติใน 3 วัน)
2. **Entitlement Activation**:
   * อัปเดต `SupabaseDataManager.setSubscription()`
   * เพิ่มคะแนนโบนัส ⭐ ตามระดับแพ็กเกจ
   * อัปเดตเลเวลทันที
3. **100% No Ads**:
   * โฆษณาคั่นระหว่างหน้า (`InterstitialAdManager`) จะตรวจพบ `isVipSubscriber() == true` และข้ามการแสดงโฆษณาตลอดเวลา
   * แบนเนอร์โฆษณาด้านล่างจะถูกตั้งเป็น `View.GONE` ซ่อนไม่ให้รบกวนเด็กและผู้ปกครอง
4. **Subscription Management**:
   * ผู้ใช้สามารถกดปุ่ม **"จัดการ ⚙️"** เพื่อเปิดหน้าจัดการต่ออายุ/ยกเลิกใน Google Play Store ได้ตลอดเวลาตามข้อกำหนดสากลของ Google

---

## 6. การแก้ปัญหาที่พบบ่อย (Troubleshooting)

| ข้อความ / อาการ | สาเหตุ | วิธีแก้ไข |
| :--- | :--- | :--- |
| **"แอปพลิเคชันเวอร์ชันนี้ไม่ได้ถูกกำหนดค่าไว้สำหรับการเรียกเก็บเงินผ่านทาง Google Play"** | 1. แอปในมือถือถูก Run ตรงจาก Android Studio (Debug Keystore)<br>2. บัญชียังไม่ได้กดรับสิทธิ์เข้าร่วมการทดสอบในลิงก์ Web | 1. ถอนการติดตั้งแอปเดิมในมือถือ<br>2. กดลิงก์ทดสอบภายในแล้วกด **"ดาวน์โหลดใน Google Play"** ติดตั้งจาก Store โดยตรง<br>3. เช็คว่าในแอป Play Store สลับไปใช้ Gmail บัญชีเดียวกับที่เพิ่มเป็นผู้ทดสอบ |
| **"ทางเราเกิดข้อผิดพลาดบางอย่าง โปรดลองอีกครั้ง"** | 1. Base Plan ใน Google Play Console ยังเป็นสถานะ "ฉบับร่าง" (Draft)<br>2. ข้อมูลสินค้าเพิ่งสร้างเสร็จ เซิร์ฟเวอร์กำลังซิงค์ (ต้องรอประมาณ 15-30 นาที) | 1. ไปที่ Play Console -> Subscriptions -> ดูใน Base Plans แล้วกด **"เปิดใช้งาน" (Activate)** ให้เป็นสีเขียว<br>2. ในมือถือไปที่ Settings -> Apps -> Google Play Store -> กด **"ล้างแคช" (Clear Cache)** |
| **"ยังไม่พบข้อมูลแพ็กเกจจาก Google Play Console (sub_monthly)"** | ยังไม่ได้สร้าง Product ID ใน Play Console หรือชื่อตัวพิมพ์เล็ก/ใหญ่ไม่ตรงกัน | ตรวจสอบว่าใน Play Console ตั้งรหัสเป็น `sub_monthly`, `sub_yearly`, `sub_lifetime` เป็นตัวพิมพ์เล็กตรงกันเป๊ะๆ |
| **ต้องการทดสอบ UI หรือฟังก์ชัน VIP ทันทีโดยไม่ต้องรอ Play Console** | เหมาะสำหรับการพัฒนาและ QA | เมื่อระบบแสดง Dialog ทดสอบ ให้กดปุ่มสีเขียว **"เปิดใช้งานทดสอบ (Test Mode)"** สิทธิ VIP จะทำงานทันทีครบทุกอย่าง |
