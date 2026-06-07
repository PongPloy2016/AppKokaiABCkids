package com.pongploydev.priyankvex.paintapp.confixdata


import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.model.LearningDataModel

/**
 * Data Manager สำหรับจัดการข้อมูลการเรียนรู้
 */
object DataManager {

    /**
     * ดึงข้อมูลตามหมวดหมู่
     */
    fun getDataByCategory(categoryId: Int): List<LearningDataModel> {
        return when (categoryId) {
            0 -> getAlphabetData()
            1 -> getNumberData()
            2 -> getColorData()
            3 -> getShapeData()
            4 -> getAnimalData()
            5 -> getBirdData()
            6 -> getFlowerData()
            7 -> getFruitData()
            8 -> getMonthData()
            9 -> getVegetableData()
            10 -> getBodyPartData()
            11 -> getClothingData()
            12 -> getCountryData()
            13 -> getFoodData()
            14 -> getGeometryData()
            15 -> getHouseholdData()
            16 -> getJobData()
            17 -> getSchoolData()
            18 -> getSportData()
            19 -> getVehicleData()
            else -> getDefaultAlphabetData()
        }
    }

    /**
     * ข้อมูลตัวอักษร A-Z
     */
    private fun getAlphabetData(): List<LearningDataModel> {
        return listOf(
            LearningDataModel(R.drawable.words_a, "A for Apple", "Apple"),
            LearningDataModel(R.drawable.words_b, "B for Ball", "Ball"),
            LearningDataModel(R.drawable.words_c, "C for Cat", "Cat"),
            LearningDataModel(R.drawable.words_d, "D for Dog", "Dog"),
            LearningDataModel(R.drawable.words_e, "E for Elephant", "Elephant"),
            LearningDataModel(R.drawable.words_f, "F for Fish", "Fish"),
            LearningDataModel(R.drawable.words_g, "G for Goat", "Goat"),
            LearningDataModel(R.drawable.words_h, "H for Horse", "Horse"),
            LearningDataModel(R.drawable.words_i, "I for Ice cream", "Ice cream"),
            LearningDataModel(R.drawable.words_j, "J for Joker", "Joker"),
            LearningDataModel(R.drawable.words_k, "K for Kite", "Kite"),
            LearningDataModel(R.drawable.words_l, "L for Lion", "Lion"),
            LearningDataModel(R.drawable.words_m, "M for Monkey", "Monkey"),
            LearningDataModel(R.drawable.words_n, "N for Nest", "Nest"),
            LearningDataModel(R.drawable.words_o, "O for Orange", "Orange"),
            LearningDataModel(R.drawable.words_p, "P for Parrot", "Parrot"),
            LearningDataModel(R.drawable.words_q, "Q for Queen", "Queen"),
            LearningDataModel(R.drawable.words_r, "R for Rabbit", "Rabbit"),
            LearningDataModel(R.drawable.words_s, "S for Sun", "Sun"),
            LearningDataModel(R.drawable.words_t, "T for Train", "Train"),
            LearningDataModel(R.drawable.words_u, "U for Umbrella", "Umbrella"),
            LearningDataModel(R.drawable.words_v, "V for Violin", "Violin"),
            LearningDataModel(R.drawable.words_w, "W for Watch", "Watch"),
            LearningDataModel(R.drawable.words_x, "X for Xylophone", "Xylophone"),
            LearningDataModel(R.drawable.words_y, "Y for Yak", "Yak"),
            LearningDataModel(R.drawable.words_z, "Z for Zebra", "Zebra")
        )
    }

    /**
     * ข้อมูลตัวเลข 0-20
     */
    private fun getNumberData(): List<LearningDataModel> {
        return listOf(
            LearningDataModel(R.drawable.zero_0, "Zero", "Zero"),
            LearningDataModel(R.drawable.one_1, "One", "One"),
            LearningDataModel(R.drawable.two_2, "Two", "Two"),
            LearningDataModel(R.drawable.three_3, "Three", "Three"),
            LearningDataModel(R.drawable.four_4, "Four", "Four"),
            LearningDataModel(R.drawable.five_5, "Five", "Five"),
            LearningDataModel(R.drawable.six_6, "Six", "Six"),
            LearningDataModel(R.drawable.seven_7, "Seven", "Seven"),
            LearningDataModel(R.drawable.eight_8, "Eight", "Eight"),
            LearningDataModel(R.drawable.nine_9, "Nine", "Nine"),
            LearningDataModel(R.drawable.ten_10, "Ten", "Ten"),
            LearningDataModel(R.drawable.eleven_11, "Eleven", "Eleven"),
            LearningDataModel(R.drawable.twelve_12, "Twelve", "Twelve"),
            LearningDataModel(R.drawable.thirteen_13, "Thirteen", "Thirteen"),
            LearningDataModel(R.drawable.fourteen_14, "Fourteen", "Fourteen"),
            LearningDataModel(R.drawable.fifteen_15, "Fifteen", "Fifteen"),
            LearningDataModel(R.drawable.sixteen_16, "Sixteen", "Sixteen"),
            LearningDataModel(R.drawable.seventeen_17, "Seventeen", "Seventeen"),
            LearningDataModel(R.drawable.eighteen_18, "Eighteen", "Eighteen"),
            LearningDataModel(R.drawable.nineteen_19, "Nineteen", "Nineteen"),
            LearningDataModel(R.drawable.twenty_20, "Twenty", "Twenty")
        )
    }

    /**
     * ข้อมูลสี
     */
    private fun getColorData(): List<LearningDataModel> {
        return listOf(
            LearningDataModel(R.drawable.green, "Green", "Green"),
            LearningDataModel(R.drawable.pink, "Pink", "Pink"),
            LearningDataModel(R.drawable.red, "Red", "Red"),
            LearningDataModel(R.drawable.black, "Black", "Black"),
            LearningDataModel(R.drawable.aqua, "Aqua", "Aqua"),
            LearningDataModel(R.drawable.blue, "Blue", "Blue"),
            LearningDataModel(R.drawable.brown, "Brown", "Brown"),
            LearningDataModel(R.drawable.slate, "Slate", "Slate"),
            LearningDataModel(R.drawable.violet, "Violet", "Violet"),
            LearningDataModel(R.drawable.white, "White", "White"),
            LearningDataModel(R.drawable.yellow, "Yellow", "Yellow")
        )
    }

    /**
     * ข้อมูลรูปทรง
     */
    private fun getShapeData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลสัตว์
     */
    private fun getAnimalData(): List<LearningDataModel> {
        return listOf(
            LearningDataModel(R.drawable.bear, "Bear", "Bear"),
            LearningDataModel(R.drawable.bison, "Bison", "Bison"),
            LearningDataModel(R.drawable.black_leopard, "Black Leopard", "Black Leopard"),
            LearningDataModel(R.drawable.cheetah, "Cheetah", "Cheetah"),
            LearningDataModel(R.drawable.chimpanzee, "Chimpanzee", "Chimpanzee"),
            LearningDataModel(R.drawable.chipmuck, "Chipmuck", "Chipmuck"),
            LearningDataModel(R.drawable.cougar, "Cougar", "Cougar"),
            LearningDataModel(R.drawable.deer, "Deer", "Deer"),
            LearningDataModel(R.drawable.elephant, "Elephant", "Elephant"),
            LearningDataModel(R.drawable.fox, "Fox", "Fox"),
            LearningDataModel(R.drawable.giraffe, "Giraffe", "Giraffe"),
            LearningDataModel(R.drawable.gorilla, "Giraffe", "Gorilla"),
            LearningDataModel(R.drawable.hedgehog, "Hedgehog", "Hedgehog"),
            LearningDataModel(R.drawable.hippopotamus, "Hippopotamus", "Hippopotamus"),
            LearningDataModel(R.drawable.hyena, "Hyena", "Hyena"),
            LearningDataModel(R.drawable.jackal, "Jackal", "Jackal"),
            LearningDataModel(R.drawable.jaguar, "Jaguar", "Jaguar"),
            LearningDataModel(R.drawable.kangaroo, "Kangaroo", "Kangaroo"),
            LearningDataModel(R.drawable.koala_bear, "Koala Bear", "Koala Bear"),
            LearningDataModel(R.drawable.lion, "Lion", "Lion"),
            LearningDataModel(R.drawable.meerkat, "Meerkat", "Meerkat"),
            LearningDataModel(R.drawable.mongoose, "Mongoose", "Mongoose"),
            LearningDataModel(R.drawable.monkey, "Monkey", "Monkey"),
            LearningDataModel(R.drawable.opossum, "Opossum", "Opossum"),
            LearningDataModel(R.drawable.otter, "Otter", "Otter"),
            LearningDataModel(R.drawable.panda, "Panda", "Panda"),
            LearningDataModel(R.drawable.polar_bear, "Polar Bear", "Polar Bear"),
            LearningDataModel(R.drawable.porcupine, "Porcupine", "Porcupine"),
            LearningDataModel(R.drawable.raccoon, "Raccoon", "Raccoon"),
            LearningDataModel(R.drawable.red_panda, "Red Panda", "Red Panda"),
            LearningDataModel(R.drawable.rhinoceros, "Rhinoceros", "Rhinoceros"),
            LearningDataModel(R.drawable.scimitar_oryx, "Scimitar Oryx", "Scimitar Oryx"),
            LearningDataModel(R.drawable.squirrel, "Squirrel", "Squirrel"),
            LearningDataModel(R.drawable.tiger, "Tiger", "Tiger"),
            LearningDataModel(R.drawable.wolf, "Wolf", "Wolf"),
            LearningDataModel(R.drawable.wombat, "Wombat", "Wombat"),
            LearningDataModel(R.drawable.zebra, "Zebra", "Zebra")
        )
    }

    /**
     * ข้อมูลนก
     */
    private fun getBirdData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลดอกไม้
     */
    private fun getFlowerData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลผลไม้
     */
    private fun getFruitData(): List<LearningDataModel> {
        return listOf(
            LearningDataModel(R.drawable.apple, "Apple", "Apple"),
            LearningDataModel(R.drawable.apricot, "Apricot", "Apricot"),
            LearningDataModel(R.drawable.avocado, "Avocado", "Avocado"),
            LearningDataModel(R.drawable.blackberry, "Blackberry", "Blackberry"),
            LearningDataModel(R.drawable.blackcurrant, "Blackcurrant", "Blackcurrant"),
            LearningDataModel(R.drawable.blueberry, "Blueberry", "Blueberry"),
            LearningDataModel(R.drawable.cherry, "Cherry", "Cherry"),
            LearningDataModel(R.drawable.coconut, "Coconut", "Coconut"),
            LearningDataModel(R.drawable.fig, "Fig", "Fig"),
            LearningDataModel(R.drawable.grape, "Grape", "Grape"),
            LearningDataModel(R.drawable.kiwi, "Kiwi", "Kiwi"),
            LearningDataModel(R.drawable.lemon, "Lemon", "Lemon"),
            LearningDataModel(R.drawable.lime, "Lime", "Lime"),
            LearningDataModel(R.drawable.lychee, "Lychee", "Lychee"),
            LearningDataModel(R.drawable.mango, "Mango", "Mango"),
            LearningDataModel(R.drawable.nectarine, "Nectarine", "Nectarine"),
            LearningDataModel(R.drawable.orange, "Orange", "Orange"),
            LearningDataModel(R.drawable.papaya, "Papaya", "Papaya"),
            LearningDataModel(R.drawable.passion, "Passion", "Passion"),
            LearningDataModel(R.drawable.peach, "Peach", "Peach"),
            LearningDataModel(R.drawable.pear, "Pear", "Pear"),
            LearningDataModel(R.drawable.pineapple, "Pineapple", "Pineapple"),
            LearningDataModel(R.drawable.plum, "Plum", "Plum"),
            LearningDataModel(R.drawable.quince, "Quince", "Quince"),
            LearningDataModel(R.drawable.raspberry, "Raspberry", "Raspberry"),
            LearningDataModel(R.drawable.strawberry, "Strawberry", "Strawberry"),
            LearningDataModel(R.drawable.watermelon, "Watermelon", "Watermelon")
        )
    }

    /**
     * ข้อมูลเดือน
     */
    private fun getMonthData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลผัก
     */
    private fun getVegetableData(): List<LearningDataModel> {
        return listOf(
            LearningDataModel(R.drawable.asparagus, "Asparagus", "Asparagus"),
            LearningDataModel(R.drawable.broccoli, "Broccoli", "Broccoli"),
            LearningDataModel(R.drawable.brussels_sprouts, "Brussels Sprouts", "Brussels Sprouts"),
            LearningDataModel(R.drawable.carrot, "Carrot", "Carrot"),
            LearningDataModel(R.drawable.cauliflower, "Cauliflower", "Cauliflower"),
            LearningDataModel(R.drawable.cucumber, "Cucumber", "Cucumber"),
            LearningDataModel(R.drawable.eggplant, "Eggplant", "Eggplant"),
            LearningDataModel(R.drawable.garlic, "Garlic", "Garlic"),
            LearningDataModel(R.drawable.lettuce, "Lettuce", "Lettuce"),
            LearningDataModel(R.drawable.mint, "Mint", "Mint"),
            LearningDataModel(R.drawable.mushroom, "Mushroom", "Mushroom"),
            LearningDataModel(R.drawable.okra, "Okra", "Okra"),
            LearningDataModel(R.drawable.onion, "Onion", "Onion"),
            LearningDataModel(R.drawable.peaspeas, "Peaspeas", "Peaspeas"),
            LearningDataModel(R.drawable.potato, "Potato", "Potato"),
            LearningDataModel(R.drawable.radish, "Radish", "Radish"),
            LearningDataModel(R.drawable.red_cabbage, "Red Cabbage", "Red Cabbage"),
            LearningDataModel(R.drawable.spinach, "Spinach", "Spinach"),
            LearningDataModel(R.drawable.squash, "Squash", "Squash"),
            LearningDataModel(R.drawable.string_beans, "String Beans", "String Beans"),
            LearningDataModel(R.drawable.tomato, "Tomato", "Tomato"),
            LearningDataModel(R.drawable.turnip, "Turnip", "Turnip")
        )
    }

    /**
     * ข้อมูลอวัยวะร่างกาย
     */
    private fun getBodyPartData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลเสื้อผ้า
     */
    private fun getClothingData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลประเทศ
     */
    private fun getCountryData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลอาหาร
     */
    private fun getFoodData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลรูปทรงเรขาคณิต
     */
    private fun getGeometryData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลของใช้ในบ้าน
     */
    private fun getHouseholdData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลอาชีพ
     */
    private fun getJobData(): List<LearningDataModel> {
        return listOf(
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
            LearningDataModel(R.drawable.model, "Model", "Model"),
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

    /**
     * ข้อมูลโรงเรียน
     */
    private fun getSchoolData(): List<LearningDataModel> {
        return listOf(
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

    /**
     * ข้อมูลกีฬา
     */
    private fun getSportData(): List<LearningDataModel> {
        return listOf(
            LearningDataModel(R.drawable.chess, "Chess", "Chess"),
            LearningDataModel(R.drawable.windsurfing, "Windsurfing", "Windsurfing"),
            LearningDataModel(R.drawable.bowling, "Bowling", "Bowling"),
            LearningDataModel(R.drawable.karate, "Karate", "Karate"),
            LearningDataModel(R.drawable.ice_skating, "Ice Skating", "Ice Skating"),
            LearningDataModel(R.drawable.table_tennis, "Table Tennis", "Table Tennis"),
            LearningDataModel(R.drawable.badminton, "Badminton", "Badminton"),
            LearningDataModel(R.drawable.swimming, "Swimming", "Swimming"),
            LearningDataModel(R.drawable.football, "Football", "Football"),
            LearningDataModel(R.drawable.hockey, "Hockey", "Hockey"),
            LearningDataModel(R.drawable.equestrian, "Equestrian", "Equestrian"),
            LearningDataModel(R.drawable.cycling, "Cycling", "Cycling"),
            LearningDataModel(R.drawable.diving, "Diving", "Diving"),
            LearningDataModel(R.drawable.judo, "Judo", "Judo"),
            LearningDataModel(R.drawable.golf, "Golf", "Golf"),
            LearningDataModel(R.drawable.baseball, "Baseball", "Baseball"),
            LearningDataModel(R.drawable.volleyball, "Volleyball", "Volleyball"),
            LearningDataModel(R.drawable.surfing, "Surfing", "Surfing"),
            LearningDataModel(R.drawable.skateboarding, "Skateboarding", "Skateboarding"),
            LearningDataModel(R.drawable.skiing, "Skiing", "Skiing"),
            LearningDataModel(R.drawable.archery, "Archery", "Archery"),
            LearningDataModel(R.drawable.canoeing, "Canoeing", "Canoeing"),
            LearningDataModel(R.drawable.running, "Running", "Running"),
            LearningDataModel(R.drawable.billiards, "Billiards", "Billiards"),
            LearningDataModel(R.drawable.fencing, "Fencing", "Fencing"),
            LearningDataModel(R.drawable.tennis, "Tennis", "Tennis"),
            LearningDataModel(R.drawable.basketball, "Basketball", "Basketball")
        )
    }

    /**
     * ข้อมูลยานพาหนะ
     */
    private fun getVehicleData(): List<LearningDataModel> {
        return listOf(
            LearningDataModel(R.drawable.ambulance, "Ambulance", "Ambulance"),
            LearningDataModel(R.drawable.bike, "Bike", "Bike"),
            LearningDataModel(R.drawable.boat, "Boat", "Boat"),
            LearningDataModel(R.drawable.bus, "Bus", "Bus"),
            LearningDataModel(R.drawable.car, "Car", "Car"),
            LearningDataModel(R.drawable.container_truck, "Container Truck", "Container Truck"),
            LearningDataModel(R.drawable.fire_truck, "Fire Truck", "Fire Truck"),
            LearningDataModel(R.drawable.helicopter, "Helicopter", "Helicopter"),
            LearningDataModel(R.drawable.motorbike, "Motorbike", "Motorbike"),
            LearningDataModel(R.drawable.plane, "Plane", "Plane"),
            LearningDataModel(R.drawable.police_car, "Police Car", "Police Car"),
            LearningDataModel(R.drawable.ship, "Ship", "Ship"),
            LearningDataModel(R.drawable.subway, "Subway", "Subway"),
            LearningDataModel(R.drawable.train, "Train", "Train"),
            LearningDataModel(R.drawable.truck, "Truck", "Truck")
        )
    }

    /**
     * ข้อมูล default (ตัวอักษร)
     */
    private fun getDefaultAlphabetData(): List<LearningDataModel> {
        return listOf(
            LearningDataModel(R.drawable.a, "A for Apple", "Apple"),
            LearningDataModel(R.drawable.b, "B for Ball", "Ball"),
            LearningDataModel(R.drawable.c, "C for Cat", "Cat"),
            LearningDataModel(R.drawable.d, "D for Dog", "Dog"),
            LearningDataModel(R.drawable.e, "E for Elephant", "Elephant"),
            LearningDataModel(R.drawable.f, "F for Fish", "Fish"),
            LearningDataModel(R.drawable.g, "G for Goat", "Goat"),
            LearningDataModel(R.drawable.h, "H for Horse", "Horse"),
            LearningDataModel(R.drawable.i, "I for Ice cream", "Ice cream"),
            LearningDataModel(R.drawable.j, "J for Joker", "Joker"),
            LearningDataModel(R.drawable.k, "K for Kite", "Kite"),
            LearningDataModel(R.drawable.l, "L for Lion", "Lion"),
            LearningDataModel(R.drawable.m, "M for Monkey", "Monkey"),
            LearningDataModel(R.drawable.n, "N for Nest", "Nest"),
            LearningDataModel(R.drawable.o, "O for Orange", "Orange"),
            LearningDataModel(R.drawable.p, "P for Parrot", "Parrot"),
            LearningDataModel(R.drawable.q, "Q for Queen", "Queen"),
            LearningDataModel(R.drawable.r, "R for Rabbit", "Rabbit"),
            LearningDataModel(R.drawable.s, "S for Sun", "Sun"),
            LearningDataModel(R.drawable.t, "T for Train", "Train"),
            LearningDataModel(R.drawable.u, "U for Umbrella", "Umbrella"),
            LearningDataModel(R.drawable.v, "V for Violin", "Violin"),
            LearningDataModel(R.drawable.w, "W for Watch", "Watch"),
            LearningDataModel(R.drawable.x, "X for Xylophone", "Xylophone"),
            LearningDataModel(R.drawable.y, "Y for Yak", "Yak"),
            LearningDataModel(R.drawable.z, "Z for Zebra", "Zebra")
        )
    }
}