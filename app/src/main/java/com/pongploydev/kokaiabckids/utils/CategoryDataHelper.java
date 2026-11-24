package com.pongploydev.kokaiabckids.utils;


import com.pongploydev.kokaiabckids.model.LearningCategory;
import com.pongploydev.kokaiabckids.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryDataHelper {

    public static List<LearningCategory> getAllCategories() {
        String[] titles = {
                "ตัวอักษร", "ตัวเลข", "สี", "รูปทรง", "สัตว์", "นก", "ดอกไม้", "ผลไม้", "เดือน", "ผัก",
                "ส่วนของร่างกาย", "เสื้อผ้า", "ประเทศ", "อาหาร", "เรขาคณิต", "บ้าน", "อาชีพ", "โรงเรียน", "กีฬา", "ยานพาหนะ",
                "พยัญชนะ ก-ฮ", "สระภาษาไทย", "วันทั้ง 7", "ฝึกสะกดคำ"
        };
        // **สำคัญ:** R.drawable.* เหล่านี้ต้องมีอยู่จริงใน folder res/drawable ของคุณ
        int[] drawables = {
                R.drawable.ic_svg_alphabet_abc_two, R.drawable.home_number, R.drawable.home_color, R.drawable.home_shape,
                R.drawable.home_animal, R.drawable.home_birds, R.drawable.home_flower, R.drawable.home_fruits,
                R.drawable.home_month, R.drawable.home_vegetable, R.drawable.home_body_parts, R.drawable.home_clothes,
                R.drawable.home_country, R.drawable.home_food, R.drawable.home_geometry, R.drawable.home_house,
                R.drawable.home_jobs, R.drawable.home_school, R.drawable.home_sports, R.drawable.home_vehicle,
                R.drawable.kids_thai_3d, R.drawable.thai_vowel_1, R.drawable.home_day, R.drawable.book
        };

        List<LearningCategory> categories = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            categories.add(new LearningCategory(i, titles[i], drawables[i]));
        }
        return categories;
    }

    public static String getTitleForType(int type) {
        if (type == 1) {
            return "เรียนรู้คำศัพท์เด็กปฐมวัย";
        } else if (type == 2) {
            return "แบบทดสอบดูแล้วเลือก";
        } else if (type == 3) {
            return "ฟังเสียงแล้วทายคำ";
        }
        return "กิจกรรมการเรียนรู้";
    }

    public static String getCategoryTitle(int categoryPosition) {
        List<LearningCategory> list = getAllCategories();
        if (categoryPosition >= 0 && categoryPosition < list.size()) {
            return list.get(categoryPosition).getTitle();
        }
        return "คำศัพท์น่ารู้";
    }
}