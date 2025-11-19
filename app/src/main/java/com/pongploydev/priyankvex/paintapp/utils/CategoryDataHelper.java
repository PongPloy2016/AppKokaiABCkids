package com.pongploydev.priyankvex.paintapp.utils;


import com.pongploydev.priyankvex.paintapp.model.LearningCategory;
import com.pongploydev.priyankvex.paintapp.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryDataHelper {

    public static List<LearningCategory> getAllCategories() {
        String[] titles = {"ตัวอักษร", "ตัวเลข", "สี", "รูปทรง", "สัตว์", "นก", "ดอกไม้", "ผลไม้", "เดือน", "ผัก", "ส่วนของร่างกาย", "เสื้อผ้า", "ประเทศ", "อาหาร", "เรขาคณิต", "บ้าน", "อาชีพ", "โรงเรียน", "กีฬา", "ยานพาหนะ"};
        // **สำคัญ:** R.drawable.* เหล่านี้ต้องมีอยู่จริงใน folder res/drawable ของคุณ
        int[] drawables = {
                R.drawable.home_alphabet, R.drawable.home_number, R.drawable.home_color, R.drawable.home_shape,
                R.drawable.home_animal, R.drawable.home_birds, R.drawable.home_flower, R.drawable.home_fruits,
                R.drawable.home_month, R.drawable.home_vegetable, R.drawable.home_body_parts, R.drawable.home_clothes,
                R.drawable.home_country, R.drawable.home_food, R.drawable.home_geometry, R.drawable.home_house,
                R.drawable.home_jobs, R.drawable.home_school, R.drawable.home_sports, R.drawable.home_vehicle
        };

        List<LearningCategory> categories = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            categories.add(new LearningCategory(i, titles[i], drawables[i]));
        }
        return categories;
    }

    public static String getTitleForType(int type) {
        if (type == 1) {
            return "Preschool Kids Learning";
        } else if (type == 2) {
            return "Look and Choose Quiz";
        } else if (type == 3) {
            return "Listen and Guess";
        }
        return "Learning Activities";
    }
}