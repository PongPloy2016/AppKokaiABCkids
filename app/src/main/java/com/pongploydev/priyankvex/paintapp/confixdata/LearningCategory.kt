package com.pongploydev.priyankvex.paintapp.confixdata

import android.media.ExifInterface
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.model.LearningDataModel


enum class LearningCategory(
    val title: String,
    val data: List<LearningDataModel>
) {
    ALPHABET("Alphabet", AlphabetData.getData()),
    NUMBERS("Numbers", NumberData.getData()),
    COLORS("Colors", ColorData.getData()),
    SHAPES("Shapes", ShapeData.getData()),
    ANIMALS("Animals", AnimalData.getData()),
    BIRDS("Birds", BirdData.getData()),
    FLOWERS("Flowers", FlowerData.getData()),
    FRUITS("Fruits", FruitData.getData()),
    MONTHS("Months", MonthData.getData()),
    VEGETABLES("Vegetables", VegetableData.getData()),
    BODY_PARTS("Body Parts", BodyPartData.getData()),
    CLOTHES("Clothes", ClothesData.getData()),
    COUNTRIES("Countries", CountryData.getData()),
    FOODS("Foods", FoodData.getData()),
    GEOMETRY("Geometry", GeometryData.getData()),
    HOUSE_ITEMS("House Items", HouseItemData.getData()),
    PROFESSIONS("Professions", ProfessionData.getData()),
    SCHOOL_ITEMS("School Items", SchoolItemData.getData()),
    SPORTS("Sports", SportData.getData()),
    VEHICLES("Vehicles", VehicleData.getData())
}

object AlphabetData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.words_a, "A for Apple ลูกแอปเปิล", "แอปเปิล"),
        LearningDataModel(R.drawable.words_b, "B for Bird ลูกนก", "นก"),
        LearningDataModel(R.drawable.words_c, "C for Cat ลูกแมว", "แมว"),
        LearningDataModel(R.drawable.words_d, "D for Dog ลูกสุนัข", "สุนัข"),
        LearningDataModel(R.drawable.words_e, "E for Egg ไข่", "ไข่"),
        LearningDataModel(R.drawable.words_f, "F for Fish น้องปลา", "ปลา"),
        LearningDataModel(R.drawable.words_g, "G for Giraffe น้องยีราฟ", "แพะ"),
        LearningDataModel(R.drawable.words_h, "H for Hat หมวก", "หมวก"),
        LearningDataModel(R.drawable.words_i, "I for Ice cream ไอศกรีม", "ไอศกรีม"),
        LearningDataModel(R.drawable.words_j, "J for Jam แยม", "แยม"),
        LearningDataModel(R.drawable.words_k, "K for Key กุญแจ", "กุญแจ"),
        LearningDataModel(R.drawable.words_l, "L for Leaf ใบไม้", "ใบไม้"),
        LearningDataModel(R.drawable.words_m, "M for Mushroom เห็ด", "เห็ด"),
        LearningDataModel(R.drawable.words_n, "N for Nestling รังนก", "รังนก"),
        LearningDataModel(R.drawable.words_o, "O for Owl เจ้านกฮูก", "นกฮูก"),
        LearningDataModel(R.drawable.words_p, "P for Pencil ดินสอ", "เขียนดินสอ"),
        LearningDataModel(R.drawable.words_q, "Q for Queen พระราชินี", "พระราชินี"),
        LearningDataModel(R.drawable.words_r, "R for Ring แหวน", "แหวน"),
        LearningDataModel(R.drawable.words_s, "S for Sun ดวงอาทิตย์", "ดวงอาทิตย์"),
        LearningDataModel(R.drawable.words_t, "T for Turtle น้องเต่า", "น้องเต่า"),
        LearningDataModel(R.drawable.words_u, "U for Umbrella ร่ม", "ร่ม"),
        LearningDataModel(R.drawable.words_v, "V for Vase แจกัน", "แจกัน"),
        LearningDataModel(R.drawable.words_w, "W for Whale น้องปลาวาฬ", "ปลาวาฬ"),
        LearningDataModel(R.drawable.words_x, "X for Xylophone ระนาด", "ระนาด"),
        LearningDataModel(R.drawable.words_y, "Y for Yo yo  โยโย่", "โยโย่"),
        LearningDataModel(R.drawable.words_z, "Z for Zebra น้องม้าลาย", "ม้าลาย")
    )

}


object NumberData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.zero_0, "ศูนย์", "ศูนย์"),
        LearningDataModel(R.drawable.one_1, "หนึ่ง", "หนึ่ง"),
        LearningDataModel(R.drawable.two_2, "สอง", "สอง"),
        LearningDataModel(R.drawable.three_3, "สาม", "สาม"),
        LearningDataModel(R.drawable.four_4, "สี่", "สี่"),
        LearningDataModel(R.drawable.five_5, "ห้า", "ห้า"),
        LearningDataModel(R.drawable.six_6, "หก", "หก"),
        LearningDataModel(R.drawable.seven_7, "เจ็ด", "เจ็ด"),
        LearningDataModel(R.drawable.eight_8, "แปด", "แปด"),
        LearningDataModel(R.drawable.nine_9, "เก้า", "เก้า"),
        LearningDataModel(R.drawable.ten_10, "สิบ", "สิบ"),
        LearningDataModel(R.drawable.eleven_11, "สิบเอ็ด", "สิบเอ็ด"),
        LearningDataModel(R.drawable.twelve_12, "สิบสอง", "สิบสอง"),
        LearningDataModel(R.drawable.thirteen_13, "สิบสาม", "สิบสาม"),
        LearningDataModel(R.drawable.fourteen_14, "สิบสี่", "สิบสี่"),
        LearningDataModel(R.drawable.fifteen_15, "สิบห้า", "สิบห้า"),
        LearningDataModel(R.drawable.sixteen_16, "สิบหก", "สิบหก"),
        LearningDataModel(R.drawable.seventeen_17, "สิบเจ็ด", "สิบเจ็ด"),
        LearningDataModel(R.drawable.eighteen_18, "สิบแปด", "สิบแปด"),
        LearningDataModel(R.drawable.nineteen_19, "สิบเก้า", "สิบเก้า"),
        LearningDataModel(R.drawable.twenty_20, "ยี่สิบ", "ยี่สิบ")
    )
}


object ColorData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.green, "เขียว", "เขียว"),
        LearningDataModel(R.drawable.pink, "สีชมพู", "สีชมพู"),
        LearningDataModel(R.drawable.red, "สีแดง", "สีแดง"),
        LearningDataModel(R.drawable.black, "สีดำ", "สีดำ"),
        LearningDataModel(R.drawable.aqua, "สีฟ้า", "สีฟ้า"),
        LearningDataModel(R.drawable.blue, "สีน้ำเงิน", "สีน้ำเงิน"),
        LearningDataModel(R.drawable.brown, "สีน้ำตาล", "สีน้ำตาล"),
        LearningDataModel(R.drawable.slate, "สีเทา", "สีเทา"),
        LearningDataModel(R.drawable.violet, "สีม่วง", "สีม่วง"),
        LearningDataModel(R.drawable.white, "สีขาว", "สีขาว"),
        LearningDataModel(R.drawable.yellow, "สีเหลือง", "สีเหลือง")
    )
}



object ShapeData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.circle, "Circle", "Circle"),
        LearningDataModel(R.drawable.square, "Square", "Square"),
        LearningDataModel(R.drawable.decagon, "Decagon", "Decagon"),
        LearningDataModel(R.drawable.ellipse, "Ellipse", "Ellipse"),
        LearningDataModel(R.drawable.hexagon, "Hexagon", "Hexagon"),
        LearningDataModel(R.drawable.triangle, "Triangle", "Triangle"),
        LearningDataModel(R.drawable.octagon, "Octagon", "Octagon"),
        LearningDataModel(R.drawable.parallelogram, "Parallelogram", "Parallelogram"),
        LearningDataModel(R.drawable.pentagon, "Pentagon", "Pentagon"),
        LearningDataModel(R.drawable.rectangle, "Rectangle", "Rectangle"),
        LearningDataModel(R.drawable.rhombus, "Rhombus", "Rhombus"),
        LearningDataModel(R.drawable.right_triangle, "Right Triangle", "Right Triangle"),
        LearningDataModel(R.drawable.trapezoid, "Trapezoid", "Trapezoid")
    )
}



object AnimalData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.bear, "หมี", "หมี"),
        LearningDataModel(R.drawable.bison, "ไบซัน", "ไบซัน"),
        LearningDataModel(R.drawable.black_leopard, "เสือดำ", "เสือดำ"),
        LearningDataModel(R.drawable.cheetah, "เสือชีตาห์", "เสือชีตาห์"),
        LearningDataModel(R.drawable.chimpanzee, "ชิมแปนซี", "ชิมแปนซี"),
        LearningDataModel(R.drawable.chipmuck, "ชิปมังก์", "ชิปมังก์"),
        LearningDataModel(R.drawable.cougar, "สิงโตภูเขา", "สิงโตภูเขา"),
        LearningDataModel(R.drawable.deer, "กวาง", "กวาง"),
        LearningDataModel(R.drawable.elephant, "ช้าง", "ช้าง"),
        LearningDataModel(R.drawable.fox, "สุนัขจิ้งจอก", "สุนัขจิ้งจอก"),
        LearningDataModel(R.drawable.giraffe, "ยีราฟ", "ยีราฟ"),
        LearningDataModel(R.drawable.gorilla, "กอริลลา", "กอริลลา"),
        LearningDataModel(R.drawable.hedgehog, "เม่น", "เม่น"),
        LearningDataModel(R.drawable.hippopotamus, "ฮิปโปโปเตมัส", "ฮิปโปโปเตมัส"),
        LearningDataModel(R.drawable.hyena, "ไฮยีนา", "ไฮยีนา"),
        LearningDataModel(R.drawable.jackal, "หมาจิ้งจอก", "หมาจิ้งจอก"),
        LearningDataModel(R.drawable.jaguar, "เสือจากัวร์", "เสือจากัวร์"),
        LearningDataModel(R.drawable.kangaroo, "จิงโจ้", "จิงโจ้"),
        LearningDataModel(R.drawable.koala_bear, "โคอาลา", "โคอาลา"),
        LearningDataModel(R.drawable.lion, "สิงโต", "สิงโต"),
        LearningDataModel(R.drawable.meerkat, "เมียร์แคท", "เมียร์แคท"),
        LearningDataModel(R.drawable.mongoose, "พังพอน", "พังพอน"),
        LearningDataModel(R.drawable.monkey, "ลิง", "ลิง"),
        LearningDataModel(R.drawable.opossum, "โอพอสซัม", "โอพอสซัม"),
        LearningDataModel(R.drawable.otter, "นาก", "นาก"),
        LearningDataModel(R.drawable.panda, "แพนด้า", "แพนด้า"),
        LearningDataModel(R.drawable.polar_bear, "หมีขั้วโลก", "หมีขั้วโลก"),
        LearningDataModel(R.drawable.porcupine, "เม่นหนาม", "เม่นหนาม"),
        LearningDataModel(R.drawable.raccoon, "แรคคูน", "แรคคูน"),
        LearningDataModel(R.drawable.red_panda, "แพนด้าแดง", "แพนด้าแดง"),
        LearningDataModel(R.drawable.rhinoceros, "แรด", "แรด"),
        LearningDataModel(R.drawable.scimitar_oryx, "ออริกซ์", "ออริกซ์"),
        LearningDataModel(R.drawable.squirrel, "กระรอก", "กระรอก"),
        LearningDataModel(R.drawable.tiger, "เสือ", "เสือ"),
        LearningDataModel(R.drawable.wolf, "หมาป่า", "หมาป่า"),
        LearningDataModel(R.drawable.wombat, "วอมแบท", "วอมแบท"),
        LearningDataModel(R.drawable.zebra, "ม้าลาย", "ม้าลาย")
    )
}


object BirdData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.canary, "Canary", "Canary"),
        LearningDataModel(R.drawable.crow, "Crow", "Crow"),
        LearningDataModel(R.drawable.dove, "Dove", "Dove"),
        LearningDataModel(R.drawable.duck, "Duck", "Duck"),
        LearningDataModel(R.drawable.eagle, "Eagle", "Eagle"),
        LearningDataModel(R.drawable.hoopoe, "Hoopoe", "Hoopoe"),
        LearningDataModel(R.drawable.hornbill, "Hornbill", "Hornbill"),
        LearningDataModel(R.drawable.kingfisher, "Kingfisher", "Kingfisher"),
        LearningDataModel(R.drawable.kite, "Kite", "Kite"),
        LearningDataModel(R.drawable.lapwing, "Lapwing", "Lapwing"),
        LearningDataModel(R.drawable.mynah, "Mynah", "Mynah"),
        LearningDataModel(R.drawable.nightingale, "Nightingale", "Nightingale"),
        LearningDataModel(R.drawable.owl, "Owl", "Owl"),
        LearningDataModel(R.drawable.parrot, "Parrot", "Parrot"),
        LearningDataModel(R.drawable.peacock, "Peacock", "Peacock"),
        LearningDataModel(R.drawable.peahen, "Peahen", "Peahen"),
        LearningDataModel(R.drawable.pheasant, "Pheasant", "Pheasant"),
        LearningDataModel(R.drawable.pigeon, "Pigeon", "Pigeon"),
        LearningDataModel(R.drawable.puffin, "Puffin", "Puffin"),
        LearningDataModel(R.drawable.quail, "Quail", "Quail"),
        LearningDataModel(R.drawable.robin, "Robin", "Robin"),
        LearningDataModel(R.drawable.sparrow, "Sparrow", "Sparrow"),
        LearningDataModel(R.drawable.swallow, "Swallow", "Swallow"),
        LearningDataModel(R.drawable.toucan, "Toucan", "Toucan"),
        LearningDataModel(R.drawable.vulture, "Vulture", "Vulture"),
        LearningDataModel(R.drawable.wagtail, "Wagtail", "Wagtail"),
        LearningDataModel(R.drawable.waxwing, "Waxwing", "Waxwing"),
        LearningDataModel(R.drawable.woodpecker, "Woodpecker", "Woodpecker")
    )
}



object FlowerData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.arum_lily, "Arum Lily", "Arum Lily"),
        LearningDataModel(R.drawable.aster, "Aster", "Aster"),
        LearningDataModel(R.drawable.bird_of_paradise, "Bird Of Paradise", "Bird Of Paradise"),
        LearningDataModel(R.drawable.bougainvillea, "Bougainvillea", "Bougainvillea"),
        LearningDataModel(R.drawable.buttercup, "Buttercup", "Buttercup"),
        LearningDataModel(R.drawable.calendula, "Calendula", "Calendula"),
        LearningDataModel(R.drawable.canna, "Canna", "Canna"),
        LearningDataModel(R.drawable.cockscomb, "Cockscomb", "Cockscomb"),
        LearningDataModel(R.drawable.daffodils, "Daffodils", "Daffodils"),
        LearningDataModel(R.drawable.dahlia, "Dahlia", "Dahlia"),
        LearningDataModel(R.drawable.daisy, "Daisy", "Daisy"),
        LearningDataModel(R.drawable.dianthus, "Dianthus", "Dianthus"),
        LearningDataModel(R.drawable.gladiolus, "Gladiolus", "Gladiolus"),
        LearningDataModel(R.drawable.hibiscus, "Hibiscus", "Hibiscus"),
        LearningDataModel(R.drawable.jasmine, "Jasmine", "Jasmine"),
        LearningDataModel(R.drawable.lavender, "Lavender", "Lavender"),
        LearningDataModel(R.drawable.lilac, "Lilac", "Lilac"),
        LearningDataModel(R.drawable.lily, "Lily", "Lily"),
        LearningDataModel(R.drawable.lotus, "Lotus", "Lotus"),
        LearningDataModel(R.drawable.morning_glory, "Morning Glory", "Morning Glory"),
        LearningDataModel(R.drawable.nerium_oleander, "Nerium Oleander", "Nerium Oleander"),
        LearningDataModel(R.drawable.orchids, "Orchids", "Orchids"),
        LearningDataModel(R.drawable.peony, "Peony", "Peony"),
        LearningDataModel(R.drawable.periwinkle, "Periwinkle", "Periwinkle"),
        LearningDataModel(R.drawable.petunia, "Petunia", "Petunia"),
        LearningDataModel(R.drawable.poppy, "Poppy", "Poppy"),
        LearningDataModel(R.drawable.purple_mallow, "Purple Mallow", "Purple Mallow"),
        LearningDataModel(R.drawable.rose, "Rose", "Rose"),
        LearningDataModel(R.drawable.sunflower, "Sunflower", "Sunflower"),
        LearningDataModel(R.drawable.tulips, "Tulips", "Tulips")
    )
}



object FruitData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.apple, "แอปเปิ้ล", "แอปเปิ้ล"),
        LearningDataModel(R.drawable.apricot, "แอปริคอท", "แอปริคอท"),
        LearningDataModel(R.drawable.avocado, "อะโวคาโด", "อะโวคาโด"),
        LearningDataModel(R.drawable.blackberry, "แบล็คเบอร์รี่", "แบล็คเบอร์รี่"),
        LearningDataModel(R.drawable.blackcurrant, "แบล็คเคอแรนท์", "แบล็คเคอแรนท์"),
        LearningDataModel(R.drawable.blueberry, "บลูเบอร์รี่", "บลูเบอร์รี่"),
        LearningDataModel(R.drawable.cherry, "เชอร์รี่", "เชอร์รี่"),
        LearningDataModel(R.drawable.coconut, "มะพร้าว", "มะพร้าว"),
        LearningDataModel(R.drawable.fig, "มะเดื่อ", "มะเดื่อ"),
        LearningDataModel(R.drawable.grape, "องุ่น", "องุ่น"),
        LearningDataModel(R.drawable.kiwi, "กีวี่", "กีวี่"),
        LearningDataModel(R.drawable.lemon, "มะนาว", "มะนาว"),
        LearningDataModel(R.drawable.lime, "มะนาวเขียว", "มะนาวเขียว"),
        LearningDataModel(R.drawable.lychee, "ลิ้นจี่", "ลิ้นจี่"),
        LearningDataModel(R.drawable.mango, "มะม่วง", "มะม่วง"),
        LearningDataModel(R.drawable.nectarine, "เนคทารีน", "เนคทารีน"),
        LearningDataModel(R.drawable.orange, "ส้ม", "ส้ม"),
        LearningDataModel(R.drawable.papaya, "มะละกอ", "มะละกอ"),
        LearningDataModel(R.drawable.passion, "เสาวรส", "เสาวรส"),
        LearningDataModel(R.drawable.peach, "พีช", "พีช"),
        LearningDataModel(R.drawable.pear, "สาลี่", "สาลี่"),
        LearningDataModel(R.drawable.pineapple, "สับปะรด", "สับปะรด"),
        LearningDataModel(R.drawable.plum, "พลัม", "พลัม"),
        LearningDataModel(R.drawable.quince, "ควินซ์", "ควินซ์"),
        LearningDataModel(R.drawable.raspberry, "ราสเบอร์รี่", "ราสเบอร์รี่"),
        LearningDataModel(R.drawable.strawberry, "สตรอว์เบอร์รี่", "สตรอว์เบอร์รี่"),
        LearningDataModel(R.drawable.watermelon, "แตงโม", "แตงโม")
    )
}



object MonthData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.january, "January", "January"),
        LearningDataModel(R.drawable.february, "February", "February"),
        LearningDataModel(R.drawable.march, "March", "March"),
        LearningDataModel(R.drawable.april, "April", "April"),
        LearningDataModel(R.drawable.may, "May", "May"),
        LearningDataModel(R.drawable.june, "June", "June"),
        LearningDataModel(R.drawable.july, "July", "July"),
        LearningDataModel(R.drawable.august, "August", "August"),
        LearningDataModel(R.drawable.september, "September", "September"),
        LearningDataModel(R.drawable.october, "October", "October"),
        LearningDataModel(R.drawable.november, "November", "November"),
        LearningDataModel(R.drawable.december, "December", "December")
    )
}


object VegetableData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.asparagus, "หน่อไม้ฝรั่ง", "หน่อไม้ฝรั่ง"),
        LearningDataModel(R.drawable.broccoli, "บร็อกโคลี", "บร็อกโคลี"),
        LearningDataModel(R.drawable.brussels_sprouts, "กะหล่ำดาว", "กะหล่ำดาว"),
        LearningDataModel(R.drawable.carrot, "แครอท", "แครอท"),
        LearningDataModel(R.drawable.cauliflower, "ดอกกะหล่ำ", "ดอกกะหล่ำ"),
        LearningDataModel(R.drawable.cucumber, "แตงกวา", "แตงกวา"),
        LearningDataModel(R.drawable.eggplant, "มะเขือ", "มะเขือ"),
        LearningDataModel(R.drawable.garlic, "กระเทียม", "กระเทียม"),
        LearningDataModel(R.drawable.lettuce, "ผักกาดหอม", "ผักกาดหอม"),
        LearningDataModel(R.drawable.mint, "สะระแหน่", "สะระแหน่"),
        LearningDataModel(R.drawable.mushroom, "เห็ด", "เห็ด"),
        LearningDataModel(R.drawable.okra, "กระเจี๊ยบ", "กระเจี๊ยบ"),
        LearningDataModel(R.drawable.onion, "หัวหอม", "หัวหอม"),
        LearningDataModel(R.drawable.peaspeas, "ถั่วลันเตา", "ถั่วลันเตา"),
        LearningDataModel(R.drawable.potato, "มันฝรั่ง", "มันฝรั่ง"),
        LearningDataModel(R.drawable.radish, "หัวไชเท้า", "หัวไชเท้า"),
        LearningDataModel(R.drawable.red_cabbage, "กะหล่ำม่วง", "กะหล่ำม่วง"),
        LearningDataModel(R.drawable.spinach, "ผักขม", "ผักขม"),
        LearningDataModel(R.drawable.squash, "สควอช", "สควอช"),
        LearningDataModel(R.drawable.string_beans, "ถั่วฝักยาว", "ถั่วฝักยาว"),
        LearningDataModel(R.drawable.tomato, "มะเขือเทศ", "มะเขือเทศ"),
        LearningDataModel(R.drawable.turnip, "หัวผักกาด", "หัวผักกาด")
    )
}



object BodyPartData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.ankle, "Ankle", "Ankle"),
        LearningDataModel(R.drawable.arm, "Arm", "Arm"),
        LearningDataModel(R.drawable.chest, "Chest", "Chest"),
        LearningDataModel(R.drawable.ear, "Ear", "Ear"),
        LearningDataModel(R.drawable.elbow, "Elbow", "Elbow"),
        LearningDataModel(R.drawable.eye, "Eye", "Eye"),
        LearningDataModel(R.drawable.fingers, "Fingers", "Fingers"),
        LearningDataModel(R.drawable.foot, "Foot", "Foot"),
        LearningDataModel(R.drawable.hair, "Hair", "Hair"),
        LearningDataModel(R.drawable.knee, "Knee", "Knee"),
        LearningDataModel(R.drawable.leg, "Leg", "Leg"),
        LearningDataModel(R.drawable.lips, "Lips", "Lips"),
        LearningDataModel(R.drawable.mouth, "Mouth", "Mouth"),
        LearningDataModel(R.drawable.neck, "Neck", "Neck"),
        LearningDataModel(R.drawable.nose, "Nose", "Nose"),
        LearningDataModel(R.drawable.shoulder, "Shoulder", "Shoulder"),
        LearningDataModel(R.drawable.stomach, "Stomach", "Stomach"),
        LearningDataModel(R.drawable.thigh, "Thigh", "Thigh"),
        LearningDataModel(R.drawable.thumb, "Thumb", "Thumb"),
        LearningDataModel(R.drawable.toe, "Toe", "Toe")
    )
}


object ClothesData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.shirt, "Shirt", "Shirt"),
        LearningDataModel(R.drawable.shoes, "Shoes", "Shoes"),
        LearningDataModel(R.drawable.pyjamas, "Pyjamas", "Pyjamas"),
        LearningDataModel(R.drawable.sock, "Sock", "Sock"),
        LearningDataModel(R.drawable.gloves, "Gloves", "Gloves"),
        LearningDataModel(R.drawable.skirt, "Skirt", "Skirt"),
        LearningDataModel(R.drawable.slipper, "Slipper", "Slipper"),
        LearningDataModel(R.drawable.sweater, "Sweater", "Sweater"),
        LearningDataModel(R.drawable.bathrobe, "Bathrobe", "Bathrobe"),
        LearningDataModel(R.drawable.jeans, "Jeans", "Jeans"),
        LearningDataModel(R.drawable.boot, "Boot", "Boot"),
        LearningDataModel(R.drawable.dress, "Dress", "Dress"),
        LearningDataModel(R.drawable.overalls, "Overalls", "Overalls"),
        LearningDataModel(R.drawable.shorts, "Shorts", "Shorts"),
        LearningDataModel(R.drawable.jacket, "Jacket", "Jacket"),
        LearningDataModel(R.drawable.scarf, "Scarf", "Scarf"),
        LearningDataModel(R.drawable.belt, "Belt", "Belt"),
        LearningDataModel(R.drawable.hat, "Hat", "Hat"),
        LearningDataModel(R.drawable.glasses, "Glasses", "Glasses"),
        LearningDataModel(R.drawable.suit, "Suit", "Suit"),
        LearningDataModel(R.drawable.veterinarian, "Veterinarian", "Veterinarian"),
        LearningDataModel(R.drawable.tailor, "Tailor", "Tailor"),
        LearningDataModel(R.drawable.waiter, "Waiter", "Waiter"),
        LearningDataModel(R.drawable.welder, "Welder", "Welder"),
        LearningDataModel(R.drawable.traffic_warden, "Traffic Warden", "Traffic Warden")
    )
}



object CountryData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.argentina, "Argentina", "Argentina"),
        LearningDataModel(R.drawable.austria, "Austria", "Austria"),
        LearningDataModel(R.drawable.belgium, "Belgium", "Belgium"),
        LearningDataModel(R.drawable.brazil, "Brazil", "Brazil"),
        LearningDataModel(R.drawable.cambodia, "Cambodia", "Cambodia"),
        LearningDataModel(R.drawable.canada, "Canada", "Canada"),
        LearningDataModel(R.drawable.croatia, "Croatia", "Croatia"),
        LearningDataModel(R.drawable.cuba, "Cuba", "Cuba"),
        LearningDataModel(R.drawable.denmark, "Denmark", "Denmark"),
        LearningDataModel(R.drawable.england, "England", "England"),
        LearningDataModel(R.drawable.france, "France", "France"),
        LearningDataModel(R.drawable.germany, "Germany", "Germany"),
        LearningDataModel(R.drawable.greece, "Greece", "Greece"),
        LearningDataModel(R.drawable.indian, "Indian", "Indian"),
        LearningDataModel(R.drawable.indonesia, "Indonesia", "Indonesia"),
        LearningDataModel(R.drawable.italy, "Italy", "Italy"),
        LearningDataModel(R.drawable.japan, "Japan", "Japan"),
        LearningDataModel(R.drawable.laos, "Laos", "Laos"),
        LearningDataModel(R.drawable.malaysia, "Malaysia", "Malaysia"),
        LearningDataModel(R.drawable.mexico, "Mexico", "Mexico"),
        LearningDataModel(R.drawable.myanmar, "Myanmar", "Myanmar"),
        LearningDataModel(R.drawable.netherlands, "Netherlands", "Netherlands"),
        LearningDataModel(R.drawable.pakistan, "Pakistan", "Pakistan"),
        LearningDataModel(R.drawable.philippine, "Philippine", "Philippine"),
        LearningDataModel(R.drawable.portugal, "Portugal", "Portugal"),
        LearningDataModel(R.drawable.russia, "Russia", "Russia"),
        LearningDataModel(R.drawable.saudi_arabia, "Saudi Arabia", "Saudi Arabia"),
        LearningDataModel(R.drawable.singapore, "Singapore", "Singapore"),
        LearningDataModel(R.drawable.south_korea, "South Korea", "South Korea"),
        LearningDataModel(R.drawable.spain, "Spain", "Spain"),
        LearningDataModel(R.drawable.sweden, "Sweden", "Sweden"),
        LearningDataModel(R.drawable.thailand, "Thailand", "Thailand"),
        LearningDataModel(R.drawable.united_states, "United States", "United States"),
        LearningDataModel(R.drawable.vietnam, "Vietnam", "Vietnam")
    )
}



object FoodData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.pizza, "Pizza", "Pizza"),
        LearningDataModel(R.drawable.biscuits, "Biscuits", "Biscuits"),
        LearningDataModel(R.drawable.chip, "Chip", "Chip"),
        LearningDataModel(R.drawable.cake, "Cake", "Cake"),
        LearningDataModel(R.drawable.noodles, "Noodles", "Noodles"),
        LearningDataModel(R.drawable.water, "Water", "Water"),
        LearningDataModel(R.drawable.sandwich, "Sandwich", "Sandwich"),
        LearningDataModel(R.drawable.ice_cream, "Ice Cream", "Ice Cream"),
        LearningDataModel(R.drawable.beer, "Beer", "Beer"),
        LearningDataModel(R.drawable.hamburger, "Hamburger", "Hamburger"),
        LearningDataModel(R.drawable.tea, "Tea", "Tea"),
        LearningDataModel(R.drawable.ham, "Ham", "Ham"),
        LearningDataModel(R.drawable.yogurt, "Yogurt", "Yogurt"),
        LearningDataModel(R.drawable.chocolate, "Chocolate", "Chocolate"),
        LearningDataModel(R.drawable.rice, "Rice", "Rice"),
        LearningDataModel(R.drawable.soda, "Soda", "Soda"),
        LearningDataModel(R.drawable.juice, "Juice", "Juice"),
        LearningDataModel(R.drawable.coffee, "Coffee", "Coffee"),
        LearningDataModel(R.drawable.bread, "Bread", "Bread"),
        LearningDataModel(R.drawable.soup, "Soup", "Soup"),
        LearningDataModel(R.drawable.butter, "Butter", "Butter"),
        LearningDataModel(R.drawable.cheese, "Cheese", "Cheese"),
        LearningDataModel(R.drawable.milk, "Milk", "Milk")
    )
}


object GeometryData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.arrow, "Arrow", "Arrow"),
        LearningDataModel(R.drawable.circle, "Circle", "Circle"),
        LearningDataModel(R.drawable.cone, "Cone", "Cone"),
        LearningDataModel(R.drawable.crescent, "Crescent", "Crescent"),
        LearningDataModel(R.drawable.cube, "Cube", "Cube"),
        LearningDataModel(R.drawable.cuboid, "Cuboid", "Cuboid"),
        LearningDataModel(R.drawable.cylinder, "Cylinder", "Cylinder"),
        LearningDataModel(R.drawable.diamond, "Diamond", "Diamond"),
        LearningDataModel(R.drawable.heart, "Heart", "Heart"),
        LearningDataModel(R.drawable.hexagon, "Hexagon", "Hexagon"),
        LearningDataModel(R.drawable.oval, "Oval", "Oval"),
        LearningDataModel(R.drawable.parallelogram, "Parallelogram", "Parallelogram"),
        LearningDataModel(R.drawable.pentagon, "Pentagon", "Pentagon"),
        LearningDataModel(R.drawable.polygon, "Polygon", "Polygon"),
        LearningDataModel(R.drawable.pyramid, "Pyramid", "Pyramid"),
        LearningDataModel(R.drawable.rectangle, "Rectangle", "Rectangle"),
        LearningDataModel(R.drawable.sphere, "Sphere", "Sphere"),
        LearningDataModel(R.drawable.star, "Star", "Star"),
        LearningDataModel(R.drawable.trapezoid, "Trapezoid", "Trapezoid"),
        LearningDataModel(R.drawable.triangle, "Triangle", "Triangle")
    )
}



object HouseItemData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.bookcase, "Bookcase", "Bookcase"),
        LearningDataModel(R.drawable.chair, "Chair", "Chair"),
        LearningDataModel(R.drawable.newspaper, "Newspaper", "Newspaper"),
        LearningDataModel(R.drawable.sofasofa, "Sofa", "Sofa"),
        LearningDataModel(R.drawable.picture, "Picture", "Picture"),
        LearningDataModel(R.drawable.watch, "Watch", "Watch"),
        LearningDataModel(R.drawable.brush, "Brush", "Brush"),
        LearningDataModel(R.drawable.television, "Television", "Television"),
        LearningDataModel(R.drawable.table, "Table", "Table"),
        LearningDataModel(R.drawable.coin, "Coin", "Coin"),
        LearningDataModel(R.drawable.phone, "Phone", "Phone"),
        LearningDataModel(R.drawable.bar_stool, "Bar Stool", "Bar Stool"),
        LearningDataModel(R.drawable.laptop, "Laptop", "Laptop"),
        LearningDataModel(R.drawable.mirror, "Mirror", "Mirror"),
        LearningDataModel(R.drawable.scissors, "Scissors", "Scissors"),
        LearningDataModel(R.drawable.umbrella, "Umbrella", "Umbrella"),
        LearningDataModel(R.drawable.clock, "Clock", "Clock"),
        LearningDataModel(R.drawable.bucket, "Bucket", "Bucket"),
        LearningDataModel(R.drawable.cup, "Cup", "Cup"),
        LearningDataModel(R.drawable.key, "Key", "Key"),
        LearningDataModel(R.drawable.door, "Door", "Door"),
        LearningDataModel(R.drawable.glass, "Glass", "Glass"),
        LearningDataModel(R.drawable.armchair, "Armchair", "Armchair"),
        LearningDataModel(R.drawable.window, "Window", "Window"),
        LearningDataModel(R.drawable.knife, "Knife", "Knife"),
        LearningDataModel(R.drawable.wallet, "Wallet", "Wallet"),
        LearningDataModel(R.drawable.bottle, "Bottle", "Bottle"),
        LearningDataModel(R.drawable.mobile_phone, "Mobile Phone", "Mobile Phone"),
        LearningDataModel(R.drawable.bed, "Bed", "Bed"),
        LearningDataModel(R.drawable.lock, "Lock", "Lock")
    )
}



object ProfessionData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.accountant, "Accountant", "Accountant"),
        LearningDataModel(R.drawable.architect, "Architect", "Architect"),
        LearningDataModel(R.drawable.astronomer, "Astronomer", "Astronomer"),
        LearningDataModel(R.drawable.author, "Author", "Author"),
        LearningDataModel(R.drawable.baker, "Baker", "Baker"),
        LearningDataModel(R.drawable.bricklayer, "Bricklayer", "Bricklayer"),
        LearningDataModel(R.drawable.butcher, "Butcher", "Butcher"),
        LearningDataModel(R.drawable.carpenter, "Carpenter", "Carpenter"),
        LearningDataModel(R.drawable.chef, "Chef", "Chef"),
        LearningDataModel(R.drawable.cleaner, "Cleaner", "Cleaner"),
        LearningDataModel(R.drawable.dentist, "Dentist", "Dentist"),
        LearningDataModel(R.drawable.doctor, "Doctor", "Doctor"),
        LearningDataModel(R.drawable.driver, "Driver", "Driver"),
        LearningDataModel(R.drawable.dustman, "Dustman", "Dustman"),
        LearningDataModel(R.drawable.electrician, "Electrician", "Electrician"),
        LearningDataModel(R.drawable.engineer, "Engineer", "Engineer"),
        LearningDataModel(R.drawable.farmer, "Farmer", "Farmer"),
        LearningDataModel(R.drawable.firefighter, "Firefighter", "Firefighter"),
        LearningDataModel(R.drawable.florist, "Florist", "Florist"),
        LearningDataModel(R.drawable.gardener, "Gardener", "Gardener"),
        LearningDataModel(R.drawable.hairdresser, "Hairdresser", "Hairdresser"),
        LearningDataModel(R.drawable.journalist, "Journalist", "Journalist"),
        LearningDataModel(R.drawable.judge, "Judge", "Judge"),
        LearningDataModel(R.drawable.lawyer, "Lawyer", "Lawyer"),
        LearningDataModel(R.drawable.lecturer, "Lecturer", "Lecturer"),
        LearningDataModel(R.drawable.librarian, "Librarian", "Librarian"),
        LearningDataModel(R.drawable.lifeguard, "Lifeguard", "Lifeguard"),
        LearningDataModel(R.drawable.mechanics, "Mechanics", "Mechanics"),
        LearningDataModel(R.drawable.model, ExifInterface.TAG_MODEL, ExifInterface.TAG_MODEL),
        LearningDataModel(R.drawable.nurse, "Nurse", "Nurse"),
        LearningDataModel(R.drawable.optician, "Optician", "Optician"),
        LearningDataModel(R.drawable.painter, "Painter", "Painter"),
        LearningDataModel(R.drawable.pharmacist, "Pharmacist", "Pharmacist"),
        LearningDataModel(R.drawable.photographer, "Photographer", "Photographer"),
        LearningDataModel(R.drawable.pilot, "Pilot", "Pilot"),
        LearningDataModel(R.drawable.plumber, "Plumber", "Plumber"),
        LearningDataModel(R.drawable.receptionist, "Receptionist", "Receptionist"),
        LearningDataModel(R.drawable.scientist, "Scientist", "Scientist"),
        LearningDataModel(R.drawable.soldier, "Soldier", "Soldier"),
        LearningDataModel(R.drawable.student, "Student", "Student"),
        LearningDataModel(R.drawable.tailor, "Tailor", "Tailor"),
        LearningDataModel(R.drawable.traffic_warden, "Traffic Warden", "Traffic Warden"),
        LearningDataModel(R.drawable.veterinarian, "Veterinarian", "Veterinarian"),
        LearningDataModel(R.drawable.waiter, "Waiter", "Waiter"),
        LearningDataModel(R.drawable.welder, "Welder", "Welder")
    )
}



object SchoolItemData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.board, "Board", "Board"),
        LearningDataModel(R.drawable.book, "Book", "Book"),
        LearningDataModel(R.drawable.chair, "Chair", "Chair"),
        LearningDataModel(R.drawable.compass, "Compass", "Compass"),
        LearningDataModel(R.drawable.computer, "Computer", "Computer"),
        LearningDataModel(R.drawable.desk, "Desk", "Desk"),
        LearningDataModel(R.drawable.dictionary, "Dictionary", "Dictionary"),
        LearningDataModel(R.drawable.eraser, "Eraser", "Eraser"),
        LearningDataModel(R.drawable.globe, "Globe", "Globe"),
        LearningDataModel(R.drawable.map, "Map", "Map"),
        LearningDataModel(R.drawable.notebook, "Notebook", "Notebook"),
        LearningDataModel(R.drawable.pen, "Pen", "Pen"),
        LearningDataModel(R.drawable.pencil, "Pencil", "Pencil"),
        LearningDataModel(R.drawable.ruler, "Ruler", "Ruler"),
        LearningDataModel(R.drawable.school_bag, "School bag", "School bag"),
        LearningDataModel(R.drawable.teacher, "Teacher", "Teacher")
    )
}


object SportData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.chess, "หมากรุก", "หมากรุก"),
        LearningDataModel(R.drawable.windsurfing, "วินด์เซิร์ฟ", "วินด์เซิร์ฟ"),
        LearningDataModel(R.drawable.bowling, "โบว์ลิ่ง", "โบว์ลิ่ง"),
        LearningDataModel(R.drawable.karate, "คาราเต้", "คาราเต้"),
        LearningDataModel(R.drawable.ice_skating, "สเก็ตน้ำแข็ง", "สเก็ตน้ำแข็ง"),
        LearningDataModel(R.drawable.table_tennis, "ปิงปอง", "ปิงปอง"),
        LearningDataModel(R.drawable.badminton, "แบดมินตัน", "แบดมินตัน"),
        LearningDataModel(R.drawable.swimming, "ว่ายน้ำ", "ว่ายน้ำ"),
        LearningDataModel(R.drawable.football, "ฟุตบอล", "ฟุตบอล"),
        LearningDataModel(R.drawable.hockey, "ฮอกกี้", "ฮอกกี้"),
        LearningDataModel(R.drawable.equestrian, "ขี่ม้า", "ขี่ม้า"),
        LearningDataModel(R.drawable.cycling, "ปั่นจักรยาน", "ปั่นจักรยาน"),
        LearningDataModel(R.drawable.diving, "ดำน้ำ", "ดำน้ำ"),
        LearningDataModel(R.drawable.judo, "ยูโด", "ยูโด"),
        LearningDataModel(R.drawable.golf, "กอล์ฟ", "กอล์ฟ"),
        LearningDataModel(R.drawable.baseball, "เบสบอล", "เบสบอล"),
        LearningDataModel(R.drawable.volleyball, "วอลเลย์บอล", "วอลเลย์บอล"),
        LearningDataModel(R.drawable.surfing, "เซิร์ฟ", "เซิร์ฟ"),
        LearningDataModel(R.drawable.skateboarding, "สเก็ตบอร์ด", "สเก็ตบอร์ด"),
        LearningDataModel(R.drawable.skiing, "สกี", "สกี"),
        LearningDataModel(R.drawable.archery, "ยิงธนู", "ยิงธนู"),
        LearningDataModel(R.drawable.canoeing, "พายเรือแคนู", "พายเรือแคนู"),
        LearningDataModel(R.drawable.running, "วิ่ง", "วิ่ง"),
        LearningDataModel(R.drawable.billiards, "บิลเลียด", "บิลเลียด"),
        LearningDataModel(R.drawable.fencing, "ฟันดาบ", "ฟันดาบ"),
        LearningDataModel(R.drawable.tennis, "เทนนิส", "เทนนิส"),
        LearningDataModel(R.drawable.basketball, "บาสเกตบอล", "บาสเกตบอล")
    )
}


object VehicleData {
    fun getData(): List<LearningDataModel> = listOf(
        LearningDataModel(R.drawable.ambulance, "รถพยาบาล", "รถพยาบาล"),
        LearningDataModel(R.drawable.bike, "จักรยาน", "จักรยาน"),
        LearningDataModel(R.drawable.boat, "เรือ", "เรือ"),
        LearningDataModel(R.drawable.bus, "รถบัส", "รถบัส"),
        LearningDataModel(R.drawable.car, "รถยนต์", "รถยนต์"),
        LearningDataModel(R.drawable.container_truck, "รถบรรทุกตู้คอนเทนเนอร์", "รถบรรทุกตู้คอนเทนเนอร์"),
        LearningDataModel(R.drawable.fire_truck, "รถดับเพลิง", "รถดับเพลิง"),
        LearningDataModel(R.drawable.helicopter, "เฮลิคอปเตอร์", "เฮลิคอปเตอร์"),
        LearningDataModel(R.drawable.motorbike, "รถมอเตอร์ไซค์", "รถมอเตอร์ไซค์"),
        LearningDataModel(R.drawable.plane, "เครื่องบิน", "เครื่องบิน"),
        LearningDataModel(R.drawable.police_car, "รถตำรวจ", "รถตำรวจ"),
        LearningDataModel(R.drawable.ship, "เรือเดินสมุทร", "เรือเดินสมุทร"),
        LearningDataModel(R.drawable.subway, "รถไฟใต้ดิน", "รถไฟใต้ดิน"),
        LearningDataModel(R.drawable.train, "รถไฟ", "รถไฟ"),
        LearningDataModel(R.drawable.truck, "รถบรรทุก", "รถบรรทุก")
    )
}