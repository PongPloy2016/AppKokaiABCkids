package com.pongploydev.kokaiabckids.model

import androidx.compose.ui.graphics.Color
import com.pongploydev.kokaiabckids.R

/**
 * Data Model สำหรับการ์ดตัวอักษร ก-ฮ, สระ, ตัวเลข, สัตว์, ผักผลไม้, อักษร 3 หมู่, ๗ วัน ๗ สี, ยานพาหนะ, ร่างกาย, สะกดคำ และ A-Z
 */
data class AlphabetCardItem(
    val id: Int,
    val letter: String,
    val name: String,
    val rhyme: String,
    val imageRes: Int,
    val posterCardRes: Int = 0,
    val soundResName: String = "",
    val audioRawRes: Int = 0,
    val isObsolete: Boolean = false,
    val cardColor: Color = Color(0xFFFF6B8B),
    val groupTag: String = "",
    val groupMnemonic: String = ""
)

object ThaiAlphabetRepository {

    val cardColors = listOf(
        Color(0xFFFF6B8B), // Candy Pink
        Color(0xFFFF9F68), // Peach Coral
        Color(0xFFFFC436), // Sunny Yellow
        Color(0xFF2EC4B6), // Mint Emerald
        Color(0xFF00B4D8), // Sky Teal
        Color(0xFF9D4EDD), // Grape Purple
        Color(0xFF3A86FF), // Ocean Blue
        Color(0xFFEF476F)  // Watermelon Pink
    )

    /**
     * ดึงรายการพยัญชนะไทย 44 ตัว ก - ฮ พร้อมไฟล์เสียง soundk1 - soundk44 และรูปภาพโปสเตอร์
     */
    fun getThaiAlphabetList(): List<AlphabetCardItem> {
        val items = listOf(
            Triple("ก", "ก ไก่", "ก เอ๋ย ก ไก่") to R.drawable.k_one,
            Triple("ข", "ข ไข่", "ข ไข่ในเล้า") to R.drawable.k_two,
            Triple("ฃ", "ฃ ขวด", "ฃ ขวดของเรา") to R.drawable.k_three,
            Triple("ค", "ค ควาย", "ค ควายเข้านา") to R.drawable.k_four,
            Triple("ฅ", "ฅ คน", "ฅ คนขึงขัง") to R.drawable.k_five,
            Triple("ฆ", "ฆ ระฆัง", "ฆ ระฆังข้างฝา") to R.drawable.k_six,
            Triple("ง", "ง งู", "ง งูใจกล้า") to R.drawable.k_seven,
            Triple("จ", "จ จาน", "จ จานใช้ดี") to R.drawable.k_eight,
            Triple("ฉ", "ฉ ฉิ่ง", "ฉ ฉิ่งตีดัง") to R.drawable.k_nine,
            Triple("ช", "ช ช้าง", "ช ช้างวิ่งหนี") to R.drawable.k_ten,

            Triple("ซ", "ซ โซ่", "ซ โซ่ล่ามที") to R.drawable.k_eleven,
            Triple("ฌ", "ฌ เฌอ", "ฌ เฌอคู่กัน") to R.drawable.k_twelve,
            Triple("ญ", "ญ หญิง", "ญ หญิงโสภา") to R.drawable.k_thirteen,
            Triple("ฎ", "ฎ ชฎา", "ฎ ชฎาสวมพลัน") to R.drawable.k_fourteen,
            Triple("ฏ", "ฏ ปฏัก", "ฏ ปฏักหุนหัน") to R.drawable.k_fifteen,
            Triple("ฐ", "ฐ ฐาน", "ฐ ฐานเข้ามารอง") to R.drawable.k_sixteen,
            Triple("ฑ", "ฑ มณโฑ", "ฑ มณโฑหน้าขาว") to R.drawable.k_seventeen,
            Triple("ฒ", "ฒ ผู้เฒ่า", "ฒ ผู้เฒ่าเดินย่อง") to R.drawable.k_eighteen,
            Triple("ณ", "ณ เณร", "ณ เณรไม่มอง") to R.drawable.k_nineteen,
            Triple("ด", "ด เด็ก", "ด เด็กต้องนิมนต์") to R.drawable.k_twenty,

            Triple("ต", "ต เต่า", "ต เต่าหลังตุง") to R.drawable.k_twenty_one,
            Triple("ถ", "ถ ถุง", "ถ ถุงแบกขน") to R.drawable.k_twenty_two,
            Triple("ท", "ท ทหาร", "ท ทหารอดทน") to R.drawable.k_twenty_three,
            Triple("ธ", "ธ ธง", "ธ ธงคนนิยม") to R.drawable.k_twenty_four,
            Triple("น", "น หนู", "น หนูขวักไขว่") to R.drawable.k_twenty_five,
            Triple("บ", "บ ใบไม้", "บ ใบไม้ทับถม") to R.drawable.k_twenty_six,
            Triple("ป", "ป ปลา", "ป ปลาตากลม") to R.drawable.k_twenty_seven,
            Triple("ผ", "ผ ผึ้ง", "ผ ผึ้งทำรัง") to R.drawable.k_twenty_eight,
            Triple("ฝ", "ฝ ฝา", "ฝ ฝาทนทาน") to R.drawable.k_twenty_nine,
            Triple("พ", "พ พาน", "พ พานวางตั้ง") to R.drawable.k_thirty,

            Triple("ฟ", "ฟ ฟัน", "ฟ ฟันสะอาดจัง") to R.drawable.k_thirty_one,
            Triple("ภ", "ภ สำเภา", "ภ สำเภาเข้าใบ") to R.drawable.k_thirty_two,
            Triple("ม", "ม ม้า", "ม ม้าคึกคัก") to R.drawable.k_thirty_three,
            Triple("ย", "ย ยักษ์", "ย ยักษ์เขี้ยวใหญ่") to R.drawable.k_thirty_four,
            Triple("ร", "ร เรือ", "ร เรือพายไป") to R.drawable.k_thirty_five,
            Triple("ล", "ล ลิง", "ล ลิงไต่ราว") to R.drawable.k_thirty_six,
            Triple("ว", "ว แหวน", "ว แหวนลงยา") to R.drawable.k_thirty_seven,
            Triple("ศ", "ศ ศาลา", "ศ ศาลาเงียบเหงา") to R.drawable.k_thirty_eight,
            Triple("ษ", "ษ ฤๅษี", "ษ ฤๅษีหนวดยาว") to R.drawable.k_thirty_nine,
            Triple("ส", "ส เสือ", "ส เสือดางคะนอง") to R.drawable.k_forty,

            Triple("ห", "ห หีบ", "ห หีบใส่ผ้า") to R.drawable.k_forty_one,
            Triple("ฬ", "ฬ จุฬา", "ฬ จุฬาท่าผยอง") to R.drawable.k_forty_two,
            Triple("อ", "อ อ่าง", "อ อ่างเนืองนอง") to R.drawable.k_forty_three,
            Triple("ฮ", "ฮ นกฮูก", "ฮ นกฮูกตาโต") to R.drawable.k_forty_four
        )

        val posterCardResList = listOf(
            R.drawable.thai_card_1, R.drawable.thai_card_2, R.drawable.thai_card_3, R.drawable.thai_card_4,
            R.drawable.thai_card_5, R.drawable.thai_card_6, R.drawable.thai_card_7, R.drawable.thai_card_8,
            R.drawable.thai_card_9, R.drawable.thai_card_10, R.drawable.thai_card_11, R.drawable.thai_card_12,
            R.drawable.thai_card_13, R.drawable.thai_card_14, R.drawable.thai_card_15, R.drawable.thai_card_16,
            R.drawable.thai_card_17, R.drawable.thai_card_18, R.drawable.thai_card_19, R.drawable.thai_card_20,
            R.drawable.thai_card_21, R.drawable.thai_card_22, R.drawable.thai_card_23, R.drawable.thai_card_24,
            R.drawable.thai_card_25, R.drawable.thai_card_26, R.drawable.thai_card_27, R.drawable.thai_card_28,
            R.drawable.thai_card_29, R.drawable.thai_card_30, R.drawable.thai_card_31, R.drawable.thai_card_32,
            R.drawable.thai_card_33, R.drawable.thai_card_34, R.drawable.thai_card_35, R.drawable.thai_card_36,
            R.drawable.thai_card_37, R.drawable.thai_card_38, R.drawable.thai_card_39, R.drawable.thai_card_40,
            R.drawable.thai_card_41, R.drawable.thai_card_42, R.drawable.thai_card_43, R.drawable.thai_card_44
        )

        val thaiSounds = listOf(
            R.raw.soundk1, R.raw.soundk2, R.raw.soundk3, R.raw.soundk4, R.raw.soundk5,
            R.raw.soundk6, R.raw.soundk7, R.raw.soundk8, R.raw.soundk9, R.raw.soundk10,
            R.raw.soundk11, R.raw.soundk12, R.raw.soundk13, R.raw.soundk14, R.raw.soundk15,
            R.raw.soundk16, R.raw.soundk17, R.raw.soundk18, R.raw.soundk19, R.raw.soundk20,
            R.raw.soundk21, R.raw.soundk22, R.raw.soundk23, R.raw.soundk24, R.raw.soundk25,
            R.raw.soundk26, R.raw.soundk27, R.raw.soundk28, R.raw.soundk29, R.raw.soundk30,
            R.raw.soundk31, R.raw.soundk32, R.raw.soundk33, R.raw.soundk34, R.raw.soundk35,
            R.raw.soundk36, R.raw.soundk37, R.raw.soundk38, R.raw.soundk39, R.raw.soundk40,
            R.raw.soundk41, R.raw.soundk42, R.raw.soundk43, R.raw.soundk44
        )

        return items.mapIndexed { index, pair ->
            val (letterInfo, drawableRes) = pair
            val (letter, name, rhyme) = letterInfo
            AlphabetCardItem(
                id = index,
                letter = letter,
                name = name,
                rhyme = rhyme,
                imageRes = drawableRes,
                posterCardRes = posterCardResList.getOrElse(index) { drawableRes },
                soundResName = "soundk${index + 1}",
                audioRawRes = thaiSounds.getOrElse(index) { 0 },
                isObsolete = (letter == "ฃ" || letter == "ฅ"),
                cardColor = cardColors[index % cardColors.size]
            )
        }
    }

    /**
     * ดึงรายการสระไทยและวรรณยุกต์ 36 ตัว (32 สระ + 4 วรรณยุกต์)
     */
    fun getThaiVowelList(): List<AlphabetCardItem> {
        val vowelItems = listOf(
            Triple("-ะ", "สระอะ", "สระอะ มะละกอ"),
            Triple("-า", "สระอา", "สระอา กา"),
            Triple("-ิ", "สระอิ", "สระอิ ขิง"),
            Triple("-ี", "สระอี", "สระอี สี"),
            Triple("-ึ", "สระอึ", "สระอึ อึ่ง"),
            Triple("-ื", "สระอือ", "สระอือ มือ"),
            Triple("-ุ", "สระอุ", "สระอุ น้ำพุ"),
            Triple("-ู", "สระอู", "สระอู ปลาทู"),
            Triple("เ-ะ", "สระเอะ", "สระเอะ เตะ"),
            Triple("เ-", "สระเอ", "สระเอ เท"),
            Triple("แ-ะ", "สระแอะ", "สระแอะ แกะ"),
            Triple("แ-", "สระแอ", "สระแอ กุญแจ"),
            Triple("โ-ะ", "สระโอะ", "สระโอะ โต๊ะ"),
            Triple("โ-", "สระโอ", "สระโอ แตงโม"),
            Triple("เ-าะ", "สระเอาะ", "สระเอาะ เงาะ"),
            Triple("-อ", "สระออ", "สระออ ดินสอ"),
            Triple("เ-อะ", "สระเออะ", "สระเออะ เลอะเทอะ"),
            Triple("เ-อ", "สระเออ", "สระเออ เธอ"),
            Triple("เ-ียะ", "สระเอียะ", "สระเอียะ เกี๊ยะ"),
            Triple("เ-ีย", "สระเอีย", "สระเอีย เปีย"),
            Triple("เ-ือะ", "สระเอือะ", "สระเอือะ"),
            Triple("เ-ือ", "สระเอือ", "สระเอือ เรือ"),
            Triple("-ัวะ", "สระอัวะ", "สระอัวะ ผัวะ"),
            Triple("-ัว", "สระอัว", "สระอัว วัว"),
            Triple("-ำ", "สระอำ", "สระอำ รำ"),
            Triple("ใ-", "สระไอไม้ม้วน", "สระไอไม้ม้วน ใยแมงมุม"),
            Triple("ไ-", "สระไอไม้มลาย", "สระไอไม้มลาย ไฟ"),
            Triple("เ-า", "สระเอา", "สระเอา เตา"),
            Triple("ฤ", "ตัว ฤ", "ตัว ฤ ฤดู"),
            Triple("ฤๅ", "ตัว ฤๅ", "ตัว ฤๅ ฤๅษี"),
            Triple("ฦ", "ตัว ฦ", "ตัว ฦ ปัจจุบันเลิกใช้แล้ว"),
            Triple("ฦๅ", "ตัว ฦๅ", "ตัว ฦๅ ปัจจุบันเลิกใช้แล้ว"),
            Triple("่", "ไม้เอก", "ไม้เอก ไก่"),
            Triple("้", "ไม้โท", "ไม้โท บ้าน"),
            Triple("๊", "ไม้ตรี", "ไม้ตรี แก๊ส"),
            Triple("๋", "ไม้จัตวา", "ไม้จัตวา กระเป๋า")
        )

        val vowelCards = listOf(
            R.drawable.thai_vowel_1, R.drawable.thai_vowel_2, R.drawable.thai_vowel_3, R.drawable.thai_vowel_4,
            R.drawable.thai_vowel_5, R.drawable.thai_vowel_6, R.drawable.thai_vowel_7, R.drawable.thai_vowel_8,
            R.drawable.thai_vowel_9, R.drawable.thai_vowel_10, R.drawable.thai_vowel_11, R.drawable.thai_vowel_12,
            R.drawable.thai_vowel_13, R.drawable.thai_vowel_14, R.drawable.thai_vowel_15, R.drawable.thai_vowel_16,
            R.drawable.thai_vowel_17, R.drawable.thai_vowel_18, R.drawable.thai_vowel_19, R.drawable.thai_vowel_20,
            R.drawable.thai_vowel_21, R.drawable.thai_vowel_22, R.drawable.thai_vowel_23, R.drawable.thai_vowel_24,
            R.drawable.thai_vowel_25, R.drawable.thai_vowel_26, R.drawable.thai_vowel_27, R.drawable.thai_vowel_28,
            R.drawable.thai_vowel_29, R.drawable.thai_vowel_30, R.drawable.thai_vowel_31, R.drawable.thai_vowel_32,
            R.drawable.thai_vowel_33, R.drawable.thai_vowel_34, R.drawable.thai_vowel_35, R.drawable.thai_vowel_36
        )

        return vowelItems.mapIndexed { index, triple ->
            val (symbol, name, rhyme) = triple
            val cardRes = vowelCards.getOrElse(index) { 0 }
            AlphabetCardItem(
                id = 100 + index,
                letter = symbol,
                name = name,
                rhyme = rhyme,
                imageRes = cardRes,
                posterCardRes = cardRes,
                soundResName = "",
                audioRawRes = 0,
                isObsolete = (symbol == "ฦ" || symbol == "ฦๅ"),
                cardColor = cardColors[index % cardColors.size]
            )
        }
    }

    /**
     * ดึงรายการตัวเลขไทย ๑ - ๑๐ (และ ๐) สำหรับหมวดภาษาไทย
     */
    fun getNumbersList(): List<AlphabetCardItem> {
        val numberData = listOf(
            Tuple4("๑", "1", "หนึ่ง (1)", "๑ หนึ่ง 1", R.drawable.m1, R.raw.q1),
            Tuple4("๒", "2", "สอง (2)", "๒ สอง 2", R.drawable.m2, R.raw.q2),
            Tuple4("๓", "3", "สาม (3)", "๓ สาม 3", R.drawable.m3, R.raw.q3),
            Tuple4("๔", "4", "สี่ (4)", "๔ สี่ 4", R.drawable.m4, R.raw.q4),
            Tuple4("๕", "5", "ห้า (5)", "๕ ห้า 5", R.drawable.m5, R.raw.q5),
            Tuple4("๖", "6", "หก (6)", "๖ หก 6", R.drawable.m6, R.raw.q6),
            Tuple4("๗", "7", "เจ็ด (7)", "๗ เจ็ด 7", R.drawable.m7, R.raw.q7),
            Tuple4("๘", "8", "แปด (8)", "๘ แปด 8", R.drawable.m8, R.raw.q8),
            Tuple4("๙", "9", "เก้า (9)", "๙ เก้า 9", R.drawable.m9, R.raw.q9),
            Tuple4("๑๐", "10", "สิบ (10)", "๑๐ สิบ 10", R.drawable.ten_10, 0),
            Tuple4("๐", "0", "ศูนย์ (0)", "๐ ศูนย์ 0", R.drawable.m0, R.raw.q0)
        )

        return numberData.mapIndexed { index, item ->
            AlphabetCardItem(
                id = 200 + index,
                letter = "${item.thai} (${item.arabic})",
                name = item.name,
                rhyme = item.rhyme,
                imageRes = item.imageRes,
                posterCardRes = item.imageRes,
                soundResName = if (item.soundRes != 0) "q${index + 1}" else "",
                audioRawRes = item.soundRes,
                isObsolete = false,
                cardColor = cardColors[index % cardColors.size]
            )
        }
    }

    /**
     * ดึงรายการตัวเลขอารบิกและภาษาอังกฤษ 1 - 10 (และ 0) สำหรับ English Kids Hub
     */
    fun getEnglishNumbersList(): List<AlphabetCardItem> {
        val engNumData = listOf(
            Tuple4("1", "One", "1 • One", "1 One", R.drawable.n1, R.raw.one),
            Tuple4("2", "Two", "2 • Two", "2 Two", R.drawable.n2, R.raw.two),
            Tuple4("3", "Three", "3 • Three", "3 Three", R.drawable.n3, R.raw.three),
            Tuple4("4", "Four", "4 • Four", "4 Four", R.drawable.n4, R.raw.four),
            Tuple4("5", "Five", "5 • Five", "5 Five", R.drawable.n5, R.raw.five),
            Tuple4("6", "Six", "6 • Six", "6 Six", R.drawable.n6, R.raw.six),
            Tuple4("7", "Seven", "7 • Seven", "7 Seven", R.drawable.n7, R.raw.seven),
            Tuple4("8", "Eight", "8 • Eight", "8 Eight", R.drawable.n8, R.raw.eigth),
            Tuple4("9", "Nine", "9 • Nine", "9 Nine", R.drawable.n9, R.raw.nine),
            Tuple4("10", "Ten", "10 • Ten", "10 Ten", R.drawable.ten_10, 0),
            Tuple4("0", "Zero", "0 • Zero", "0 Zero", R.drawable.n0, R.raw.zero)
        )

        return engNumData.mapIndexed { index, item ->
            AlphabetCardItem(
                id = 250 + index,
                letter = item.thai,
                name = item.name,
                rhyme = item.rhyme,
                imageRes = item.imageRes,
                posterCardRes = item.imageRes,
                soundResName = "",
                audioRawRes = item.soundRes,
                isObsolete = false,
                cardColor = cardColors[index % cardColors.size]
            )
        }
    }

    /**
     * 🎨 1. ดึงรายการ ๗ วัน ๗ สี และแม่สีน่ารู้ (Colors & 7 Days)
     */
    fun getColorsAndDaysList(): List<AlphabetCardItem> {
        val list = listOf(
            ColorDayItem("วันจันทร์", "สีเหลือง (Yellow)", "วันจันทร์ สีเหลือง สดใส", R.drawable.yellow, Color(0xFFFBC02D)),
            ColorDayItem("วันอังคาร", "สีชมพู (Pink)", "วันอังคาร สีชมพู หวานแหวว", R.drawable.pink, Color(0xFFE91E63)),
            ColorDayItem("วันพุธ", "สีเขียว (Green)", "วันพุธ สีเขียว ชื่นใจ", R.drawable.green, Color(0xFF4CAF50)),
            ColorDayItem("วันพฤหัสบดี", "สีส้ม (Orange)", "วันพฤหัสบดี สีส้ม ร่าเริง", R.drawable.orange, Color(0xFFFF9800)),
            ColorDayItem("วันศุกร์", "สีฟ้า (Blue)", "วันศุกร์ สีฟ้า สบายตา", R.drawable.blue, Color(0xFF03A9F4)),
            ColorDayItem("วันเสาร์", "สีม่วง (Purple)", "วันเสาร์ สีม่วง สวยงาม", R.drawable.violet, Color(0xFF9C27B0)),
            ColorDayItem("วันอาทิตย์", "สีแดง (Red)", "วันอาทิตย์ สีแดง แรงฤทธิ์", R.drawable.red, Color(0xFFF44336)),
            ColorDayItem("สีขาว", "White", "สีขาว บริสุทธิ์ สะอาด", R.drawable.white, Color(0xFF9E9E9E)),
            ColorDayItem("สีดำ", "Black", "สีดำ ลึกลับ มืดมิด", R.drawable.black, Color(0xFF212121)),
            ColorDayItem("สีน้ำตาล", "Brown", "สีน้ำตาล อบอุ่น", R.drawable.brown, Color(0xFF795548)),
            ColorDayItem("สีฟ้าอมเขียว", "Aqua", "สีฟ้าอมเขียว สดชื่นสดใส", R.drawable.aqua, Color(0xFF00BCD4))
        )

        return list.mapIndexed { index, item ->
            AlphabetCardItem(
                id = 800 + index,
                letter = item.dayOrColor,
                name = item.colorDesc,
                rhyme = item.rhyme,
                imageRes = item.imageRes,
                posterCardRes = item.imageRes,
                soundResName = "",
                audioRawRes = 0,
                isObsolete = false,
                cardColor = item.cardColor
            )
        }
    }

    /**
     * 🚗 2. ดึงรายการยานพาหนะและการเดินทาง (Vehicles & Transport)
     */
    fun getVehiclesList(): List<AlphabetCardItem> {
        val list = listOf(
            VehicleInfo("รถยนต์", "Car", "ขับขี่ปลอดภัยบนถนน 🚗", "รถยนต์ คาร์ ขับขี่บนถนน", R.drawable.car),
            VehicleInfo("รถไฟ", "Train", "แล่นบนราง ปู๊นๆ 🚆", "รถไฟ เทรน ปู๊นๆ", R.drawable.train),
            VehicleInfo("เครื่องบิน", "Airplane", "บินบนท้องฟ้าสูง ✈️", "เครื่องบิน แอร์เพลน บินบนฟ้า", R.drawable.plane),
            VehicleInfo("รถพยาบาล", "Ambulance", "ช่วยผู้ป่วย วี้หว่อ 🚑", "รถพยาบาล แอมบูแลนซ์ วี้หว่อ", R.drawable.ambulance),
            VehicleInfo("รถดับเพลิง", "Fire Truck", "ฉีดน้ำดับเพลิง 🚒", "รถดับเพลิง ไฟเออร์ทรัค", R.drawable.fire_truck),
            VehicleInfo("รถตำรวจ", "Police Car", "ดูแลความปลอดภัย 🚓", "รถตำรวจ โปลิศคาร์", R.drawable.police_car),
            VehicleInfo("รถประจำทาง", "Bus", "รถเมล์โดยสาร 🚌", "รถประจำทาง บัส รถเมล์", R.drawable.bus),
            VehicleInfo("เรือ", "Boat", "ลอยแล่นในแม่น้ำ ⛵", "เรือ โบ๊ต ลอยในน้ำ", R.drawable.boat),
            VehicleInfo("เฮลิคอปเตอร์", "Helicopter", "ใบพัดหมุนบนฟ้า 🚁", "เฮลิคอปเตอร์ ใบพัดหมุน", R.drawable.helicopter),
            VehicleInfo("รถจักรยาน", "Bicycle", "ปั่นออกกำลังกาย 🚲", "รถจักรยาน ไบซิเคิล", R.drawable.bike),
            VehicleInfo("มอเตอร์ไซค์", "Motorcycle", "รถจักรยานยนต์ 🏍️", "มอเตอร์ไซค์ รถมอเตอร์ไซค์", R.drawable.motorbike),
            VehicleInfo("รถบรรทุก", "Truck", "บรรทุกสิ่งของหนัก 🚚", "รถบรรทุก ทรัค", R.drawable.truck),
            VehicleInfo("เรือเดินสมุทร", "Ship", "เรือลำใหญ่ข้ามทะเล 🚢", "เรือเดินสมุทร ชิป ลำใหญ่", R.drawable.ship),
            VehicleInfo("รถไฟฟ้าใต้ดิน", "Subway", "เดินทางรวดเร็วใต้ดิน 🚇", "รถไฟฟ้าใต้ดิน ซับเวย์", R.drawable.subway)
        )

        return list.mapIndexed { index, item ->
            AlphabetCardItem(
                id = 900 + index,
                letter = item.thaiName,
                name = "${item.thaiName} • ${item.engName}",
                rhyme = item.rhyme,
                imageRes = item.imageRes,
                posterCardRes = item.imageRes,
                soundResName = "",
                audioRawRes = 0,
                isObsolete = false,
                cardColor = cardColors[index % cardColors.size]
            )
        }
    }

    /**
     * ✋ 3. ดึงรายการร่างกายของฉัน (My Body Parts)
     */
    fun getBodyPartsList(): List<AlphabetCardItem> {
        val list = listOf(
            BodyPartInfo("ตา", "Eye", "ดวงตาไว้มองเห็น 👁️", "ตา อาย ดวงตามองเห็น", R.drawable.eye),
            BodyPartInfo("หู", "Ear", "ใบหูไว้ฟังเสียง 👂", "หู เอียร์ หูฟังเสียง", R.drawable.ear),
            BodyPartInfo("จมูก", "Nose", "จมูกไว้ดมกลิ่น 👃", "จมูก โนส จมูกดมกลิ่น", R.drawable.nose),
            BodyPartInfo("ปาก", "Mouth", "ปากไว้พูดและทานอาหาร 👄", "ปาก เมาธ์ ปากพูด", R.drawable.mouth),
            BodyPartInfo("ริมฝีปาก", "Lips", "ริมฝีปากสวยงาม 💋", "ริมฝีปาก ลิปส์", R.drawable.lips),
            BodyPartInfo("มือและนิ้วมือ", "Fingers", "นิ้วมือหยิบจับสิ่งของ 🖐️", "นิ้วมือ ฟิงเกอร์ส", R.drawable.fingers),
            BodyPartInfo("แขน", "Arm", "แขนแข็งแรง 💪", "แขน อาร์ม แขนแข็งแรง", R.drawable.arm),
            BodyPartInfo("ขา", "Leg", "ขาช่วยพยุงร่างกาย 🦵", "ขา เล็ก ขาก้าวเดิน", R.drawable.leg),
            BodyPartInfo("เท้า", "Foot", "เท้าก้าวเดิน วิ่ง 🦶", "เท้า ฟุต เท้าก้าวเดิน", R.drawable.foot),
            BodyPartInfo("ผม", "Hair", "เส้นผมบนศีรษะ 💇", "ผม แฮร์ เส้นผม", R.drawable.hair),
            BodyPartInfo("หัวไหล่", "Shoulder", "หัวไหล่ทั้งสองข้าง", "ไหล่ โชลเดอร์", R.drawable.shoulder),
            BodyPartInfo("หัวเข่า", "Knee", "หัวเข่างอได้", "หัวเข่า นี", R.drawable.knee),
            BodyPartInfo("ข้อศอก", "Elbow", "ข้อศอกพับแขน", "ข้อศอก เอลโบว์", R.drawable.elbow),
            BodyPartInfo("คอ", "Neck", "ลำคอตั้งตรง", "คอ เน็ค ลำคอ", R.drawable.neck),
            BodyPartInfo("ข้อเท้า", "Ankle", "ข้อเท้าหมุนได้", "ข้อเท้า แองเคิล", R.drawable.ankle)
        )

        return list.mapIndexed { index, item ->
            AlphabetCardItem(
                id = 1000 + index,
                letter = item.thaiName,
                name = "${item.thaiName} • ${item.engName}",
                rhyme = item.rhyme,
                imageRes = item.imageRes,
                posterCardRes = item.imageRes,
                soundResName = "",
                audioRawRes = 0,
                isObsolete = false,
                cardColor = cardColors[index % cardColors.size]
            )
        }
    }

    /**
     * 🗣️ 4. ดึงรายการแจกลูกสะกดคำ (Phonics & Spelling)
     */
    fun getSpellingList(): List<AlphabetCardItem> {
        val list = listOf(
            SpellingInfo("กา", "ก + า = กา", "กอ - อา - กา กาตัวดำ", R.drawable.k_one),
            SpellingInfo("ตา", "ต + า = ตา", "ตอ - อา - ตา ดวงตาของเรา", R.drawable.eye),
            SpellingInfo("ขา", "ข + า = ขา", "ขอ - อา - ขา ขาก้าวเดิน", R.drawable.leg),
            SpellingInfo("ยา", "ย + า = ยา", "ยอ - อา - ยา ยารักษาโรค", R.drawable.pharmacist),
            SpellingInfo("มา", "ม + า = มา", "มอ - อา - มา เดินมาหา", R.drawable.k_thirty_three),
            SpellingInfo("ฝา", "ฝ + า = ฝา", "ฝอ - อา - ฝา ฝาหม้อ", R.drawable.k_twenty_nine),
            SpellingInfo("ปา", "ป + า = ปา", "ปอ - อา - ปา ปาลูกบอล", R.drawable.b_ball),
            SpellingInfo("ปู", "ป + ู = ปู", "ปอ - อู - ปู ปูทะเล", R.drawable.k_twenty_seven),
            SpellingInfo("งู", "ง + ู = งู", "งอ - อู - งู งูมีพิษ", R.drawable.k_seven),
            SpellingInfo("ดู", "ด + ู = ดู", "ดอ - อู - ดู ดูทีวี", R.drawable.television),
            SpellingInfo("หู", "ห + ู = หู", "หอ - อู - หู หูไว้ฟัง", R.drawable.ear),
            SpellingInfo("ถู", "ถ + ู = ถู", "ถอ - อู - ถู ถูพื้นสะอาด", R.drawable.cleaner),
            SpellingInfo("ตี", "ต + ี = ตี", "ตอ - อี - ตี ตีกลอง", R.drawable.k_twenty_one),
            SpellingInfo("มี", "ม + ี = มี", "มอ - อี - มี ฉันมีเหรียญ", R.drawable.coin),
            SpellingInfo("ดี", "ด + ี = ดี", "ดอ - อี - ดี เด็กดีน่ารัก", R.drawable.k_twenty)
        )

        return list.mapIndexed { index, item ->
            AlphabetCardItem(
                id = 1100 + index,
                letter = item.word,
                name = item.formula,
                rhyme = item.spellingRhyme,
                imageRes = item.imageRes,
                posterCardRes = item.imageRes,
                soundResName = "",
                audioRawRes = 0,
                isObsolete = false,
                cardColor = cardColors[index % cardColors.size]
            )
        }
    }

    /**
     * ดึงรายการสัตว์โลกน่ารัก 20 ชนิด พร้อมเสียงร้องและคำศัพท์ 2 ภาษา
     */
    fun getAnimalsList(): List<AlphabetCardItem> {
        val animalData = listOf(
            AnimalInfo("ช้าง", "Elephant", "เสียงร้อง แป๋นๆ 🐘", "Elephant ช้าง เสียงร้อง แป๋นๆ", R.drawable.elephant),
            AnimalInfo("สุนัข", "Dog", "เสียงร้อง โฮ่งๆ 🐶", "Dog สุนัข เสียงร้อง โฮ่งๆ", R.drawable.d_dog),
            AnimalInfo("แมว", "Cat", "เสียงร้อง เหมียวๆ 🐱", "Cat แมว เสียงร้อง เหมียวๆ", R.drawable.c_cat),
            AnimalInfo("เป็ด", "Duck", "เสียงร้อง ก้าบๆ 🦆", "Duck เป็ด เสียงร้อง ก้าบๆ", R.drawable.duck),
            AnimalInfo("ม้า", "Horse", "เสียงร้อง ฮี้ๆ 🐴", "Horse ม้า เสียงร้อง ฮี้ๆ", R.drawable.h_horse),
            AnimalInfo("ลิง", "Monkey", "เสียงร้อง เจี๊ยกๆ 🐵", "Monkey ลิง เสียงร้อง เจี๊ยกๆ", R.drawable.monkey),
            AnimalInfo("เสือ", "Tiger", "เสียงคำราม โฮกๆ 🐯", "Tiger เสือ เสียงร้อง โฮกๆ", R.drawable.tiger),
            AnimalInfo("สิงโต", "Lion", "เจ้าป่าคำราม โฮกๆ 🦁", "Lion สิงโต เสียงร้อง โฮกๆ", R.drawable.lion),
            AnimalInfo("กระต่าย", "Rabbit", "กระโดดดุ๊กดิ๊ก 🐰", "Rabbit กระต่าย กระโดด ดุ๊กดิ๊ก", R.drawable.r_rabbit),
            AnimalInfo("หมี", "Bear", "หมีตัวใหญ่ใจดี 🐻", "Bear หมี ตัวใหญ่", R.drawable.bear),
            AnimalInfo("แพนด้า", "Panda", "แพนด้ากินไผ่ 🐼", "Panda แพนด้า น่ารัก", R.drawable.panda),
            AnimalInfo("จิงโจ้", "Kangaroo", "กระโดดไกล มีกระเป๋า 🦘", "Kangaroo จิงโจ้ กระโดดไกล", R.drawable.kangaroo),
            AnimalInfo("ยีราฟ", "Giraffe", "ยีราฟคอยาว 🦒", "Giraffe ยีราฟ คอยาว", R.drawable.giraffe),
            AnimalInfo("ม้าลาย", "Zebra", "ม้าลาย ขาวดำ 🦓", "Zebra ม้าลาย ลายขาวดำ", R.drawable.zebra),
            AnimalInfo("กวาง", "Deer", "กวางเขาสวยงาม 🦌", "Deer กวาง เขาสวย", R.drawable.deer),
            AnimalInfo("นกฮูก", "Owl", "เสียงร้อง ฮูกๆ 🦉", "Owl นกฮูก เสียงร้อง ฮูกๆ", R.drawable.owl),
            AnimalInfo("แพะ", "Goat", "เสียงร้อง แบ๊ะๆ 🐐", "Goat แพะ เสียงร้อง แบ๊ะๆ", R.drawable.g_goat),
            AnimalInfo("หมาป่า", "Wolf", "เสียงหอน บรู๊วว 🐺", "Wolf หมาป่า หอน บรู๊ว", R.drawable.wolf),
            AnimalInfo("สุนัขจิ้งจอก", "Fox", "จิ้งจอกเจ้าเล่ห์ 🦊", "Fox สุนัขจิ้งจอก", R.drawable.fox),
            AnimalInfo("นกยูง", "Peacock", "นกยูงรำแพน 🦚", "Peacock นกยูง รำแพนสวยงาม", R.drawable.peacock)
        )

        return animalData.mapIndexed { index, animal ->
            AlphabetCardItem(
                id = 300 + index,
                letter = animal.engName,
                name = "${animal.engName} • ${animal.thaiName}",
                rhyme = animal.rhyme,
                imageRes = animal.imageRes,
                posterCardRes = animal.imageRes,
                soundResName = "",
                audioRawRes = 0,
                isObsolete = false,
                cardColor = cardColors[index % cardColors.size]
            )
        }
    }

    /**
     * ดึงรายการผักและผลไม้น่ารู้ 18 ชนิด (Fruits & Vegetables)
     */
    fun getFruitsList(): List<AlphabetCardItem> {
        val fruitData = listOf(
            FruitInfo("แตงโม", "Watermelon", "หวานฉ่ำ ชื่นใจ 🍉", "Watermelon แตงโม หวานฉ่ำ", R.drawable.watermelon),
            FruitInfo("สตรอว์เบอร์รี", "Strawberry", "สีแดงสด รสเปรี้ยวหวาน 🍓", "Strawberry สตรอว์เบอร์รี สีแดงสด", R.drawable.strawberry),
            FruitInfo("มะม่วง", "Mango", "ผลไม้ไทย รสหวานหอม 🥭", "Mango มะม่วง รสหวานหอม", R.drawable.mango),
            FruitInfo("แอปเปิ้ล", "Apple", "กรอบอร่อย มีประโยชน์ 🍎", "Apple แอปเปิ้ล กรอบอร่อย", R.drawable.apple),
            FruitInfo("ส้ม", "Orange", "วิตามินซีสูง สดชื่น 🍊", "Orange ส้ม วิตามินซีสูง", R.drawable.orange),
            FruitInfo("องุ่น", "Grape", "พวงองุ่น รสหวานอร่อย 🍇", "Grape องุ่น รสหวานอร่อย", R.drawable.grape),
            FruitInfo("มะละกอ", "Papaya", "เนื้อส้มหวาน มีประโยชน์ 🍈", "Papaya มะละกอ ผลสุกสีส้ม", R.drawable.papaya),
            FruitInfo("สับปะรด", "Pineapple", "ผลไม้ฉ่ำ รสเปรี้ยวหวาน 🍍", "Pineapple สับปะรด เปรี้ยวหวาน", R.drawable.pineapple),
            FruitInfo("มะพร้าว", "Coconut", "น้ำมะพร้าว หวานชื่นใจ 🥥", "Coconut มะพร้าว น้ำหวานหอม", R.drawable.coconut),
            FruitInfo("เลมอน", "Lemon", "ผลสีเหลือง รสเปรี้ยวสดชื่น 🍋", "Lemon เลมอน รสเปรี้ยว", R.drawable.lemon),
            FruitInfo("แครอท", "Carrot", "ผักสีส้ม บำรุงสายตา 🥕", "Carrot แครอท บำรุงสายตา", R.drawable.carrot),
            FruitInfo("มะเขือเทศ", "Tomato", "ผิวแดงสดใส วิตามินสูง 🍅", "Tomato มะเขือเทศ วิตามินสูง", R.drawable.tomato),
            FruitInfo("มะเขือม่วง", "Eggplant", "ผักสีม่วง อร่อยมีประโยชน์ 🍆", "Eggplant มะเขือม่วง ผักมีประโยชน์", R.drawable.eggplant),
            FruitInfo("ผักโขม", "Spinach", "เพิ่มพลังแข็งแรง 🥬", "Spinach ผักโขม เพิ่มพลังแข็งแรง", R.drawable.spinach),
            FruitInfo("หัวหอม", "Onion", "สมุนไพรคู่ครัว 🧅", "Onion หัวหอม สมุนไพรคู่ครัว", R.drawable.onion),
            FruitInfo("เสาวรส", "Passion Fruit", "รสเปรี้ยวหวาน กลิ่นหอม 🍹", "Passion Fruit เสาวรส", R.drawable.passion),
            FruitInfo("แบล็กเคอร์แรนต์", "Blackcurrant", "ผลไม้ตระกูลเบอร์รี 🫐", "Blackcurrant แบล็กเคอร์แรนต์", R.drawable.blackcurrant),
            FruitInfo("เนคทารีน", "Nectarine", "รสหวาน กรอบอร่อย 🍑", "Nectarine เนคทารีน หวานกรอบ", R.drawable.nectarine)
        )

        return fruitData.mapIndexed { index, fruit ->
            AlphabetCardItem(
                id = 400 + index,
                letter = fruit.engName,
                name = "${fruit.engName} • ${fruit.thaiName}",
                rhyme = fruit.rhyme,
                imageRes = fruit.imageRes,
                posterCardRes = fruit.imageRes,
                soundResName = "",
                audioRawRes = 0,
                isObsolete = false,
                cardColor = cardColors[index % cardColors.size]
            )
        }
    }

    /**
     * ดึงรายการอักษร 3 หมู่ (ไตรยางศ์: อักษรกลาง 9, อักษรสูง 11, อักษรต่ำ 24)
     */
    fun getToneGroupsList(): List<AlphabetCardItem> {
        val middleGroup = listOf(
            TupleMnemonic("ก", "ก ไก่", "ไก่", R.drawable.k_one, R.raw.soundk1),
            TupleMnemonic("จ", "จ จาน", "จิก", R.drawable.k_eight, R.raw.soundk8),
            TupleMnemonic("ด", "ด เด็ก", "เด็ก", R.drawable.k_twenty, R.raw.soundk20),
            TupleMnemonic("ต", "ต เต่า", "ตาย", R.drawable.k_twenty_one, R.raw.soundk21),
            TupleMnemonic("ฎ", "ฎ ชฎา", "เด็ก (ชฎา)", R.drawable.k_fourteen, R.raw.soundk14),
            TupleMnemonic("ฏ", "ฏ ปฏัก", "ตาย (ปฏัก)", R.drawable.k_fifteen, R.raw.soundk15),
            TupleMnemonic("บ", "บ ใบไม้", "บน", R.drawable.k_twenty_six, R.raw.soundk26),
            TupleMnemonic("ป", "ป ปลา", "ปาก", R.drawable.k_twenty_seven, R.raw.soundk27),
            TupleMnemonic("อ", "อ อ่าง", "โอ่ง", R.drawable.k_forty_three, R.raw.soundk43)
        )

        val highGroup = listOf(
            TupleMnemonic("ผ", "ผ ผึ้ง", "ผี", R.drawable.k_twenty_eight, R.raw.soundk28),
            TupleMnemonic("ฝ", "ฝ ฝา", "ฝาก", R.drawable.k_twenty_nine, R.raw.soundk29),
            TupleMnemonic("ถ", "ถ ถุง", "ถุง", R.drawable.k_twenty_two, R.raw.soundk22),
            TupleMnemonic("ข", "ข ไข่", "ข้าว", R.drawable.k_two, R.raw.soundk2),
            TupleMnemonic("ส", "ส เสือ", "สาร", R.drawable.k_forty, R.raw.soundk40),
            TupleMnemonic("ห", "ห หีบ", "ให้", R.drawable.k_forty_one, R.raw.soundk41),
            TupleMnemonic("ฉ", "ฉ ฉิ่ง", "ฉัน", R.drawable.k_nine, R.raw.soundk9),
            TupleMnemonic("ศ", "ศ ศาลา", "ศึกษา", R.drawable.k_thirty_eight, R.raw.soundk38),
            TupleMnemonic("ษ", "ษ ฤๅษี", "ษา", R.drawable.k_thirty_nine, R.raw.soundk39),
            TupleMnemonic("ฐ", "ฐ ฐาน", "ฐาน", R.drawable.k_sixteen, R.raw.soundk16),
            TupleMnemonic("ฃ", "ฃ ขวด", "ขวด", R.drawable.k_three, R.raw.soundk3)
        )

        val lowGroup = listOf(
            TupleMnemonic("ค", "ค ควาย", "พ่อค้า", R.drawable.k_four, R.raw.soundk4),
            TupleMnemonic("ฅ", "ฅ คน", "คน", R.drawable.k_five, R.raw.soundk5),
            TupleMnemonic("ฆ", "ฆ ระฆัง", "ระฆัง", R.drawable.k_six, R.raw.soundk6),
            TupleMnemonic("ง", "ง งู", "งูใหญ่", R.drawable.k_seven, R.raw.soundk7),
            TupleMnemonic("ช", "ช ช้าง", "ช้าง", R.drawable.k_ten, R.raw.soundk10),
            TupleMnemonic("ซ", "ซ โซ่", "ซื้อ", R.drawable.k_eleven, R.raw.soundk11),
            TupleMnemonic("ฌ", "ฌ เฌอ", "เฌอ", R.drawable.k_twelve, R.raw.soundk12),
            TupleMnemonic("ญ", "ญ หญิง", "หญิง", R.drawable.k_thirteen, R.raw.soundk13),
            TupleMnemonic("ฑ", "ฑ มณโฑ", "มณโฑ", R.drawable.k_seventeen, R.raw.soundk17),
            TupleMnemonic("ฒ", "ฒ ผู้เฒ่า", "เฒ่า", R.drawable.k_eighteen, R.raw.soundk18),
            TupleMnemonic("ณ", "ณ เณร", "ณ", R.drawable.k_nineteen, R.raw.soundk19),
            TupleMnemonic("ท", "ท ทหาร", "ทอง", R.drawable.k_twenty_three, R.raw.soundk23),
            TupleMnemonic("ธ", "ธ ธง", "ธง", R.drawable.k_twenty_four, R.raw.soundk24),
            TupleMnemonic("น", "น หนู", "นอน", R.drawable.k_twenty_five, R.raw.soundk25),
            TupleMnemonic("พ", "พ พาน", "พ่อ", R.drawable.k_thirty, R.raw.soundk30),
            TupleMnemonic("ฟ", "ฟ ฟัน", "ฟัน", R.drawable.k_thirty_one, R.raw.soundk31),
            TupleMnemonic("ภ", "ภ สำเภา", "สำเภา", R.drawable.k_thirty_two, R.raw.soundk32),
            TupleMnemonic("ม", "ม ม้า", "โมฬี", R.drawable.k_thirty_three, R.raw.soundk33),
            TupleMnemonic("ย", "ย ยักษ์", "อยู่", R.drawable.k_thirty_four, R.raw.soundk34),
            TupleMnemonic("ร", "ร เรือ", "ริม", R.drawable.k_thirty_five, R.raw.soundk35),
            TupleMnemonic("ล", "ล ลิง", "โลก", R.drawable.k_thirty_six, R.raw.soundk36),
            TupleMnemonic("ว", "ว แหวน", "วัด", R.drawable.k_thirty_seven, R.raw.soundk37),
            TupleMnemonic("ฬ", "ฬ จุฬา", "จุฬา", R.drawable.k_forty_two, R.raw.soundk42),
            TupleMnemonic("ฮ", "ฮ นกฮูก", "ฮ่อ", R.drawable.k_forty_four, R.raw.soundk44)
        )

        val result = mutableListOf<AlphabetCardItem>()

        middleGroup.forEachIndexed { index, item ->
            result.add(
                AlphabetCardItem(
                    id = 500 + index,
                    letter = item.letter,
                    name = "${item.name} (${item.mnemonic})",
                    rhyme = "${item.name} อักษรกลาง คำช่วยจำ ${item.mnemonic}",
                    imageRes = item.imageRes,
                    posterCardRes = item.imageRes,
                    soundResName = "",
                    audioRawRes = item.soundRes,
                    cardColor = Color(0xFF00C853),
                    groupTag = "อักษรกลาง (๙ ตัว)",
                    groupMnemonic = "ไก่ จิก เด็ก ตาย บน ปาก โอ่ง"
                )
            )
        }

        highGroup.forEachIndexed { index, item ->
            result.add(
                AlphabetCardItem(
                    id = 600 + index,
                    letter = item.letter,
                    name = "${item.name} (${item.mnemonic})",
                    rhyme = "${item.name} อักษรสูง คำช่วยจำ ${item.mnemonic}",
                    imageRes = item.imageRes,
                    posterCardRes = item.imageRes,
                    soundResName = "",
                    audioRawRes = item.soundRes,
                    cardColor = Color(0xFFFF9100),
                    groupTag = "อักษรสูง (๑๑ ตัว)",
                    groupMnemonic = "ผี ฝาก ถุง ข้าว สาร ให้ ฉัน"
                )
            )
        }

        lowGroup.forEachIndexed { index, item ->
            result.add(
                AlphabetCardItem(
                    id = 700 + index,
                    letter = item.letter,
                    name = "${item.name} (${item.mnemonic})",
                    rhyme = "${item.name} อักษรต่ำ คำช่วยจำ ${item.mnemonic}",
                    imageRes = item.imageRes,
                    posterCardRes = item.imageRes,
                    soundResName = "",
                    audioRawRes = item.soundRes,
                    cardColor = Color(0xFF2979FF),
                    groupTag = "อักษรต่ำ (๒๔ ตัว)",
                    groupMnemonic = "งูใหญ่ นอนอยู่ ณ ริมวัดโมฬีโลก"
                )
            )
        }

        return result
    }

    /**
     * ดึงรายการตัวอักษรภาษาอังกฤษ 26 ตัว A - Z พร้อมไฟล์เสียง sounda - soundz
     */
    fun getEnglishAlphabetList(): List<AlphabetCardItem> {
        val engData = listOf(
            Triple("A", "Apple", "A for Apple") to R.drawable.words_a,
            Triple("B", "Ball", "B for Ball") to R.drawable.words_b,
            Triple("C", "Cat", "C for Cat") to R.drawable.words_c,
            Triple("D", "Dog", "D for Dog") to R.drawable.words_d,
            Triple("E", "Elephant", "E for Elephant") to R.drawable.words_e,
            Triple("F", "Fish", "F for Fish") to R.drawable.words_f,
            Triple("G", "Goat", "G for Goat") to R.drawable.words_g,
            Triple("H", "Horse", "H for Horse") to R.drawable.words_h,
            Triple("I", "Ice cream", "I for Ice cream") to R.drawable.words_i,
            Triple("J", "Joker", "J for Joker") to R.drawable.words_j,
            Triple("K", "Kite", "K for Kite") to R.drawable.words_k,
            Triple("L", "Lion", "L for Lion") to R.drawable.words_l,
            Triple("M", "Monkey", "M for Monkey") to R.drawable.words_m,
            Triple("N", "Nest", "N for Nest") to R.drawable.words_n,
            Triple("O", "Orange", "O for Orange") to R.drawable.words_o,
            Triple("P", "Parrot", "P for Parrot") to R.drawable.words_p,
            Triple("Q", "Queen", "Q for Queen") to R.drawable.words_q,
            Triple("R", "Rabbit", "R for Rabbit") to R.drawable.words_r,
            Triple("S", "Sun", "S for Sun") to R.drawable.words_s,
            Triple("T", "Train", "T for Train") to R.drawable.words_t,
            Triple("U", "Umbrella", "U for Umbrella") to R.drawable.words_u,
            Triple("V", "Violin", "V for Violin") to R.drawable.words_v,
            Triple("W", "Watch", "W for Watch") to R.drawable.words_w,
            Triple("X", "Xylophone", "X for Xylophone") to R.drawable.words_x,
            Triple("Y", "Yak", "Y for Yak") to R.drawable.words_y,
            Triple("Z", "Zebra", "Z for Zebra") to R.drawable.words_z
        )

        val engSounds = listOf(
            R.raw.sounda, R.raw.soundb, R.raw.soundc, R.raw.soundd, R.raw.sounde,
            R.raw.soundf, R.raw.soundg, R.raw.soundh, R.raw.soundi, R.raw.soundj,
            R.raw.soundk, R.raw.soundl, R.raw.soundm, R.raw.soundn, R.raw.soundo,
            R.raw.soundp, R.raw.soundq, R.raw.soundr, R.raw.sounds, R.raw.soundt,
            R.raw.soundu, R.raw.soundv, R.raw.soundw, R.raw.soundx, R.raw.soundy,
            R.raw.soundz
        )

        return engData.mapIndexed { index, pair ->
            val (info, drawableRes) = pair
            val (letter, name, rhyme) = info
            AlphabetCardItem(
                id = index,
                letter = letter,
                name = name,
                rhyme = rhyme,
                imageRes = drawableRes,
                posterCardRes = drawableRes,
                soundResName = "sound${letter.lowercase()}",
                audioRawRes = engSounds.getOrElse(index) { 0 },
                isObsolete = false,
                cardColor = cardColors[index % cardColors.size]
            )
        }
    }
}

private data class Tuple4(
    val thai: String,
    val arabic: String,
    val name: String,
    val rhyme: String,
    val imageRes: Int,
    val soundRes: Int
)

private data class ColorDayItem(
    val dayOrColor: String,
    val colorDesc: String,
    val rhyme: String,
    val imageRes: Int,
    val cardColor: Color
)

private data class VehicleInfo(
    val thaiName: String,
    val engName: String,
    val desc: String,
    val rhyme: String,
    val imageRes: Int
)

private data class BodyPartInfo(
    val thaiName: String,
    val engName: String,
    val desc: String,
    val rhyme: String,
    val imageRes: Int
)

private data class SpellingInfo(
    val word: String,
    val formula: String,
    val spellingRhyme: String,
    val imageRes: Int
)

private data class AnimalInfo(
    val thaiName: String,
    val engName: String,
    val soundDesc: String,
    val rhyme: String,
    val imageRes: Int
)

private data class FruitInfo(
    val thaiName: String,
    val engName: String,
    val desc: String,
    val rhyme: String,
    val imageRes: Int
)

private data class TupleMnemonic(
    val letter: String,
    val name: String,
    val mnemonic: String,
    val imageRes: Int,
    val soundRes: Int
)
