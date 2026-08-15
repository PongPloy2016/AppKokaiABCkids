package com.pongploydev.priyankvex.paintapp.model

import androidx.compose.ui.graphics.Color
import com.pongploydev.priyankvex.paintapp.R

/**
 * Data Model สำหรับการ์ดตัวอักษร ก-ฮ, สระ-วรรณยุกต์ และ A-Z ในรูปแบบโปสเตอร์สีสันสดใส
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
    val cardColor: Color = Color(0xFFFF6B8B)
)

object ThaiAlphabetRepository {

    // จานสีพาสเทลและสีสันสดใส 8 เฉดสีสำหรับเด็กแบบวนซ้ำ
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

        // ไฟล์เสียงพยัญชนะไทยแท้ soundk1 - soundk44
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

        // ไฟล์เสียง sounda - soundz
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
