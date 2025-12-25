package com.pongploydev.priyankvex.paintapp.activity.learning

import android.content.Context
import android.media.ExifInterface
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.adapter.SubHomeAdapter
import com.pongploydev.priyankvex.paintapp.model.LearningDataModel

class SubActivity : AppCompatActivity() {
    var context: Context? = null
    var llAdView: RelativeLayout? = null
    var llAdViewFacebook: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)
        //getSupportActionBar().hide();
        context = this
        initDefine()
        llAdView = findViewById<RelativeLayout?>(R.id.llAdView)
        llAdViewFacebook = findViewById<LinearLayout?>(R.id.llAdViewFacebook)
        //        Utils.loadBannerAd(this,llAdView,llAdViewFacebook);
    }


    var txtTitleSubHome: TextView? = null
    var position: Int = 0
    var TYPE: Int = 0
    private fun initDefine() {
        rvSubHome = findViewById<RecyclerView>(R.id.rvSubHome)
        txtTitleSubHome = findViewById<TextView>(R.id.txtTitleSubHome)
        val intent = getIntent()
        position = intent.getIntExtra("categoryPosition", 0)
        val category = intent.getStringExtra("Category")
        TYPE = intent.getIntExtra("Type", 0)
        txtTitleSubHome!!.setText(category)
        prepareDataForLearning(position)
    }


    var learningDataModelArrayList: ArrayList<LearningDataModel?>? = null

    fun prepareDataForLearning(i2: Int) {
        if (i2 == 0) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_a,
                    "A for Apple",
                    "Apple"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_b,
                    "B for Ball",
                    "Ball"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_c,
                    "C for Cat",
                    "Cat"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_d,
                    "D for Dog",
                    "Dog"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_e,
                    "E for Elephant",
                    "Elephant"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_f,
                    "F for Fish",
                    "Fish"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_g,
                    "G for Goat",
                    "Goat"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_h,
                    "H for Horse",
                    "Horse"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_i,
                    "I for Ice cream",
                    "Ice cream"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_j,
                    "J for Joker",
                    "Joker"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_k,
                    "K for Kite",
                    "Kite"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_l,
                    "L for Lion",
                    "Lion"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_m,
                    "M for Monkey",
                    "Monkey"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_n,
                    "N for Nest",
                    "Nest"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_o,
                    "O for Orange",
                    "Orange"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_p,
                    "P for Parrot",
                    "Parrot"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_q,
                    "Q for Queen",
                    "Queen"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_r,
                    "R for Rabbit",
                    "Rabbit"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_s,
                    "S for Sun",
                    "Sun"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_t,
                    "T for Train",
                    "Train"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_u,
                    "U for Umbrella",
                    "Umbrella"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_v,
                    "V for Violin",
                    "Violin"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_w,
                    "W for Watch",
                    "Watch"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_x,
                    "X for Xylophone",
                    "Xylophone"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_y,
                    "Y for Yak",
                    "Yak"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.words_z,
                    "Z for Zebra",
                    "Zebra"
                )
            )
        } else if (i2 == 1) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.zero_0, "Zero", "Zero"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.one_1, "One", "One"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.two_2, "Two", "Two"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.three_3,
                    "Three",
                    "Three"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.four_4, "Four", "Four"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.five_5, "Five", "Five"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.six_6, "Six", "Six"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.seven_7,
                    "Seven",
                    "Seven"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.eight_8,
                    "Eight",
                    "Eight"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.nine_9, "Nine", "Nine"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.ten_10, "Ten", "Ten"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.eleven_11,
                    "Eleven",
                    "Eleven"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.twelve_12,
                    "Twelve",
                    "Twelve"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.thirteen_13,
                    "Thirteen",
                    "Thirteen"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.fourteen_14,
                    "Fourteen",
                    "Fourteen"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.fifteen_15,
                    "Fifteen",
                    "Fifteen"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.sixteen_16,
                    "Sixteen",
                    "Sixteen"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.seventeen_17,
                    "Seventeen",
                    "Seventeen"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.eighteen_18,
                    "Eighteen",
                    "Eighteen"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.nineteen_19,
                    "Nineteen",
                    "Nineteen"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.twenty_20,
                    "Twenty",
                    "Twenty"
                )
            )
        } else if (i2 == 2) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.green, "Green", "Green"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.pink, "Pink", "Pink"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.red, "Red", "Red"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.black, "Black", "Black"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.aqua, "Aqua", "Aqua"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.blue, "Blue", "Blue"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.brown, "Brown", "Brown"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.slate, "Slate", "Slate"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.violet,
                    "Violet",
                    "Violet"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.white, "White", "White"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.yellow,
                    "Yellow",
                    "Yellow"
                )
            )
        } else if (i2 == 3) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.circle,
                    "Circle",
                    "Circle"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.square,
                    "Square",
                    "Square"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.decagon,
                    "Decagon",
                    "Decagon"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.ellipse,
                    "Ellipse",
                    "Ellipse"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.hexagon,
                    "Hexagon",
                    "Hexagon"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.triangle,
                    "Triangle",
                    "Triangle"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.octagon,
                    "Octagon",
                    "Octagon"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.parallelogram,
                    "Parallelogram",
                    "Parallelogram"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.pentagon,
                    "Pentagon",
                    "Pentagon"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.rectangle,
                    "Rectangle",
                    "Rectangle"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.rhombus,
                    "Rhombus",
                    "Rhombus"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.right_triangle,
                    "Right Triangle",
                    "Right Triangle"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.trapezoid,
                    "Trapezoid",
                    "Trapezoid"
                )
            )
        } else if (i2 == 4) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.bear, "Bear", "Bear"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.bison, "Bison", "Bison"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.black_leopard,
                    "Black Leopard",
                    "Black Leopard"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cheetah,
                    "Cheetah",
                    "Cheetah"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.chimpanzee,
                    "Chimpanzee",
                    "Chimpanzee"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.chipmuck,
                    "Chipmuck",
                    "Chipmuck"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cougar,
                    "Cougar",
                    "Cougar"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.deer, "Deer", "Deer"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.elephant,
                    "Elephant",
                    "Elephant"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.fox, "Fox", "Fox"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.giraffe,
                    "Giraffe",
                    "Giraffe"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.gorilla,
                    "Gorilla",
                    "Gorilla"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.hedgehog,
                    "Hedgehog",
                    "Hedgehog"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.hippopotamus,
                    "Hippopotamus",
                    "Hippopotamus"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.hyena, "Hyena", "Hyena"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.jackal,
                    "Jackal",
                    "Jackal"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.jaguar,
                    "Jaguar",
                    "Jaguar"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.kangaroo,
                    "Kangaroo",
                    "Kangaroo"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.koala_bear,
                    "Koala Bear",
                    "Koala Bear"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.lion, "Lion", "Lion"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.meerkat,
                    "Meerkat",
                    "Meerkat"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.mongoose,
                    "Mongoose",
                    "Mongoose"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.monkey,
                    "Monkey",
                    "Monkey"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.opossum,
                    "Opossum",
                    "Opossum"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.otter, "Otter", "Otter"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.panda, "Panda", "Panda"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.polar_bear,
                    "Polar Bear",
                    "Polar Bear"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.porcupine,
                    "Porcupine",
                    "Porcupine"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.raccoon,
                    "Raccoon",
                    "Raccoon"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.red_panda,
                    "Red Panda",
                    "Red Panda"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.rhinoceros,
                    "Rhinoceros",
                    "Rhinoceros"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.scimitar_oryx,
                    "Scimitar Oryx",
                    "Scimitar Oryx"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.squirrel,
                    "Squirrel",
                    "Squirrel"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.tiger, "Tiger", "Tiger"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.wolf, "Wolf", "Wolf"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.wombat,
                    "Wombat",
                    "Wombat"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.zebra, "Zebra", "Zebra"))
        } else if (i2 == 5) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.canary,
                    "Canary",
                    "Canary"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.crow, "Crow", "Crow"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.dove, "Dove", "Dove"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.duck, "Duck", "Duck"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.eagle, "Eagle", "Eagle"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.hoopoe,
                    "Hoopoe",
                    "Hoopoe"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.hornbill,
                    "Hornbill",
                    "Hornbill"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.kingfisher,
                    "Kingfisher",
                    "Kingfisher"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.kite, "Kite", "Kite"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.lapwing,
                    "Lapwing",
                    "Lapwing"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.mynah, "Mynah", "Mynah"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.nightingale,
                    "Nightingale",
                    "Nightingale"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.owl, "Owl", "Owl"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.parrot,
                    "Parrot",
                    "Parrot"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.peacock,
                    "Peacock",
                    "Peacock"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.peahen,
                    "Peahen",
                    "Peahen"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.pheasant,
                    "Pheasant",
                    "Pheasant"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.pigeon,
                    "Pigeon",
                    "Pigeon"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.puffin,
                    "Puffin",
                    "Puffin"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.quail, "Quail", "Quail"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.robin, "Robin", "Robin"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.sparrow,
                    "Sparrow",
                    "Sparrow"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.swallow,
                    "Swallow",
                    "Swallow"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.toucan,
                    "Toucan",
                    "Toucan"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.vulture,
                    "Vulture",
                    "Vulture"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.wagtail,
                    "Wagtail",
                    "Wagtail"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.waxwing,
                    "Waxwing",
                    "Waxwing"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.woodpecker,
                    "Woodpecker",
                    "Woodpecker"
                )
            )
        } else if (i2 == 7) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.apple, "Apple", "Apple"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.apricot,
                    "Apricot",
                    "Apricot"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.avocado,
                    "Avocado",
                    "Avocado"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.blackberry,
                    "Blackberry",
                    "Blackberry"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.blackcurrant,
                    "Blackcurrant",
                    "Blackcurrant"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.blueberry,
                    "Blueberry",
                    "Blueberry"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cherry,
                    "Cherry",
                    "Cherry"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.coconut,
                    "Coconut",
                    "Coconut"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.fig, "Fig", "Fig"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.grape, "Grape", "Grape"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.kiwi, "Kiwi", "Kiwi"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.lemon, "Lemon", "Lemon"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.lime, "Lime", "Lime"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.lychee,
                    "Lychee",
                    "Lychee"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.mango, "Mango", "Mango"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.nectarine,
                    "Nectarine",
                    "Nectarine"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.orange,
                    "Orange",
                    "Orange"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.papaya,
                    "Papaya",
                    "Papaya"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.passion,
                    "Passion",
                    "Passion"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.peach, "Peach", "Peach"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.pear, "Pear", "Pear"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.pineapple,
                    "Pineapple",
                    "Pineapple"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.plum, "Plum", "Plum"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.quince,
                    "Quince",
                    "Quince"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.raspberry,
                    "Raspberry",
                    "Raspberry"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.strawberry,
                    "Strawberry",
                    "Strawberry"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.watermelon,
                    "Watermelon",
                    "Watermelon"
                )
            )
        } else if (i2 == 6) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.arum_lily,
                    "Arum Lily",
                    "Arum Lily"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.aster, "Aster", "Aster"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.bird_of_paradise,
                    "Bird Of Paradise",
                    "Bird Of Paradise"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.bougainvillea,
                    "Bougainvillea",
                    "Bougainvillea"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.buttercup,
                    "Buttercup",
                    "Buttercup"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.calendula,
                    "Calendula",
                    "Calendula"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.canna, "Canna", "Canna"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cockscomb,
                    "Cockscomb",
                    "Cockscomb"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.daffodils,
                    "Daffodils",
                    "Daffodils"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.dahlia,
                    "Dahlia",
                    "Dahlia"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.daisy, "Daisy", "Daisy"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.dianthus,
                    "Dianthus",
                    "Dianthus"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.gladiolus,
                    "Gladiolus",
                    "Gladiolus"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.hibiscus,
                    "Hibiscus",
                    "Hibiscus"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.jasmine,
                    "Jasmine",
                    "Jasmine"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.lavender,
                    "Lavender",
                    "Lavender"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.lilac, "Lilac", "Lilac"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.lily, "Lily", "Lily"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.lotus, "Lotus", "Lotus"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.morning_glory,
                    "Morning Glory",
                    "Morning Glory"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.nerium_oleander,
                    "Nerium Oleander",
                    "Nerium Oleander"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.orchids,
                    "Orchids",
                    "Orchids"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.peony, "Peony", "Peony"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.periwinkle,
                    "Periwinkle",
                    "Periwinkle"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.petunia,
                    "Petunia",
                    "Petunia"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.poppy, "Poppy", "Poppy"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.purple_mallow,
                    "Purple Mallow",
                    "Purple Mallow"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.rose, "Rose", "Rose"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.sunflower,
                    "Sunflower",
                    "Sunflower"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.tulips,
                    "Tulips",
                    "Tulips"
                )
            )
        } else if (i2 == 8) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.january,
                    "January",
                    "January"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.february,
                    "February",
                    "February"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.march, "March", "March"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.april, "April", "April"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.may, "May", "May"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.june, "June", "June"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.july, "July", "July"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.august,
                    "August",
                    "August"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.september,
                    "September",
                    "September"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.october,
                    "October",
                    "October"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.november,
                    "November",
                    "November"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.december,
                    "December",
                    "December"
                )
            )
        } else if (i2 == 9) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.asparagus,
                    "Asparagus",
                    "Asparagus"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.broccoli,
                    "Broccoli",
                    "Broccoli"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.brussels_sprouts,
                    "Brussels Sprouts",
                    "Brussels Sprouts"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.carrot,
                    "Carrot",
                    "Carrot"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cauliflower,
                    "Cauliflower",
                    "Cauliflower"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cucumber,
                    "Cucumber",
                    "Cucumber"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.eggplant,
                    "Eggplant",
                    "Eggplant"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.garlic,
                    "Garlic",
                    "Garlic"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.lettuce,
                    "Lettuce",
                    "Lettuce"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.mint, "Mint", "Mint"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.mushroom,
                    "Mushroom",
                    "Mushroom"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.okra, "Okra", "Okra"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.onion, "Onion", "Onion"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.peaspeas,
                    "Peaspeas",
                    "Peaspeas"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.potato,
                    "Potato",
                    "Potato"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.radish,
                    "Radish",
                    "Radish"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.red_cabbage,
                    "Red Cabbage",
                    "Red Cabbage"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.spinach,
                    "Spinach",
                    "Spinach"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.squash,
                    "Squash",
                    "Squash"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.string_beans,
                    "String Beans",
                    "String Beans"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.tomato,
                    "Tomato",
                    "Tomato"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.turnip,
                    "Turnip",
                    "Turnip"
                )
            )
        } else if (i2 == 10) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.ankle, "Ankle", "Ankle"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.arm, "Arm", "Arm"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.chest, "Chest", "Chest"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.ear, "Ear", "Ear"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.elbow, "Elbow", "Elbow"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.eye, "Eye", "Eye"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.fingers,
                    "Fingers",
                    "Fingers"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.foot, "Foot", "Foot"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.hair, "Hair", "Hair"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.knee, "Knee", "Knee"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.leg, "Leg", "Leg"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.lips, "Lips", "Lips"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.mouth, "Mouth", "Mouth"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.neck, "Neck", "Neck"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.nose, "Nose", "Nose"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.shoulder,
                    "Shoulder",
                    "Shoulder"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.stomach,
                    "Stomach",
                    "Stomach"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.thigh, "Thigh", "Thigh"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.thumb, "Thumb", "Thumb"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.toe, "Toe", "Toe"))
        } else if (i2 == 11) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.shirt, "Shirt", "Shirt"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.shoes, "Shoes", "Shoes"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.pyjamas,
                    "Pyjamas",
                    "Pyjamas"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.sock, "Sock", "Sock"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.gloves,
                    "Gloves",
                    "Gloves"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.skirt, "Skirt", "Skirt"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.slipper,
                    "Slipper",
                    "Slipper"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.sweater,
                    "Sweater",
                    "Sweater"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.bathrobe,
                    "Bathrobe",
                    "Bathrobe"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.jeans, "Jeans", "Jeans"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.boot, "Boot", "Boot"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.dress, "Dress", "Dress"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.overalls,
                    "Overalls",
                    "Overalls"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.shorts,
                    "Shorts",
                    "Shorts"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.jacket,
                    "Jacket",
                    "Jacket"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.scarf, "Scarf", "Scarf"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.belt, "Belt", "Belt"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.hat, "Hat", "Hat"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.glasses,
                    "Glasses",
                    "Glasses"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.suit, "Suit", "Suit"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.veterinarian,
                    "Veterinarian",
                    "Veterinarian"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.tailor,
                    "Tailor",
                    "Tailor"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.waiter,
                    "Waiter",
                    "Waiter"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.welder,
                    "Welder",
                    "Welder"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.traffic_warden,
                    "Traffic Warden",
                    "Traffic Warden"
                )
            )
        } else if (i2 == 12) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.argentina,
                    "Argentina",
                    "Argentina"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.austria,
                    "Austria",
                    "Austria"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.belgium,
                    "Belgium",
                    "Belgium"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.brazil,
                    "Brazil",
                    "Brazil"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cambodia,
                    "Cambodia",
                    "Cambodia"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.canada,
                    "Canada",
                    "Canada"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.croatia,
                    "Croatia",
                    "Croatia"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.cuba, "Cuba", "Cuba"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.denmark,
                    "Denmark",
                    "Denmark"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.england,
                    "England",
                    "England"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.france,
                    "France",
                    "France"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.germany,
                    "Germany",
                    "Germany"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.greece,
                    "Greece",
                    "Greece"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.indian,
                    "Indian",
                    "Indian"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.indonesia,
                    "Indonesia",
                    "Indonesia"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.italy, "Italy", "Italy"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.japan, "Japan", "Japan"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.laos, "Laos", "Laos"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.malaysia,
                    "Malaysia",
                    "Malaysia"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.mexico,
                    "Mexico",
                    "Mexico"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.myanmar,
                    "Myanmar",
                    "Myanmar"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.netherlands,
                    "Netherlands",
                    "Netherlands"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.pakistan,
                    "Pakistan",
                    "Pakistan"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.philippine,
                    "Philippine",
                    "Philippine"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.portugal,
                    "Portugal",
                    "Portugal"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.russia,
                    "Russia",
                    "Russia"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.saudi_arabia,
                    "Saudi Arabia",
                    "Saudi Arabia"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.singapore,
                    "Singapore",
                    "Singapore"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.south_korea,
                    "South Korea",
                    "South Korea"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.spain, "Spain", "Spain"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.sweden,
                    "Sweden",
                    "Sweden"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.thailand,
                    "Thailand",
                    "Thailand"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.united_states,
                    "United States",
                    "United States"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.vietnam,
                    "Vietnam",
                    "Vietnam"
                )
            )
        } else if (i2 == 13) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.pizza, "Pizza", "Pizza"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.biscuits,
                    "Biscuits",
                    "Biscuits"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.chip, "Chip", "Chip"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.cake, "Cake", "Cake"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.noodles,
                    "Noodles",
                    "Noodles"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.water, "Water", "Water"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.sandwich,
                    "Sandwich",
                    "Sandwich"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.ice_cream,
                    "Ice Cream",
                    "Ice Cream"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.beer, "Beer", "Beer"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.hamburger,
                    "Hamburger",
                    "Hamburger"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.tea, "Tea", "Tea"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.ham, "Ham", "Ham"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.yogurt,
                    "Yogurt",
                    "Yogurt"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.chocolate,
                    "Chocolate",
                    "Chocolate"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.rice, "Rice", "Rice"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.soda, "Soda", "Soda"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.juice, "Juice", "Juice"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.coffee,
                    "Coffee",
                    "Coffee"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.bread, "Bread", "Bread"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.soup, "Soup", "Soup"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.butter,
                    "Butter",
                    "Butter"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cheese,
                    "Cheese",
                    "Cheese"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.milk, "Milk", "Milk"))
        } else if (i2 == 14) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.arrow, "Arrow", "Arrow"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.circle,
                    "Circle",
                    "Circle"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.cone, "Cone", "Cone"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.crescent,
                    "Crescent",
                    "Crescent"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.cube, "Cube", "Cube"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cuboid,
                    "Cuboid",
                    "Cuboid"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cylinder,
                    "Cylinder",
                    "Cylinder"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.diamond,
                    "Diamond",
                    "Diamond"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.heart, "Heart", "Heart"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.hexagon,
                    "Hexagon",
                    "Hexagon"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.oval, "Oval", "Oval"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.parallelogram,
                    "Parallelogram",
                    "Parallelogram"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.pentagon,
                    "Pentagon",
                    "Pentagon"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.polygon,
                    "Polygon",
                    "Polygon"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.pyramid,
                    "Pyramid",
                    "Pyramid"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.rectangle,
                    "Rectangle",
                    "Rectangle"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.sphere,
                    "Sphere",
                    "Sphere"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.star, "Star", "Star"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.trapezoid,
                    "Trapezoid",
                    "Trapezoid"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.triangle,
                    "Triangle",
                    "Triangle"
                )
            )
        } else if (i2 == 15) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.bookcase,
                    "Bookcase",
                    "Bookcase"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.chair, "Chair", "Chair"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.newspaper,
                    "Newspaper",
                    "Newspaper"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.sofasofa, "Sofa", "Sofa"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.picture,
                    "Picture",
                    "Picture"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.watch, "Watch", "Watch"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.brush, "Brush", "Brush"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.television,
                    "Television",
                    "Television"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.table, "Table", "Table"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.coin, "Coin", "Coin"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.phone, "Phone", "Phone"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.bar_stool,
                    "Bar Stool",
                    "Bar Stool"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.laptop,
                    "Laptop",
                    "Laptop"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.mirror,
                    "Mirror",
                    "Mirror"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.scissors,
                    "Scissors",
                    "Scissors"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.umbrella,
                    "Umbrella",
                    "Umbrella"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.clock, "Clock", "Clock"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.bucket,
                    "Bucket",
                    "Bucket"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.cup, "Cup", "Cup"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.key, "Key", "Key"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.door, "Door", "Door"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.glass, "Glass", "Glass"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.armchair,
                    "Armchair",
                    "Armchair"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.window,
                    "Window",
                    "Window"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.knife, "Knife", "Knife"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.wallet,
                    "Wallet",
                    "Wallet"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.bottle,
                    "Bottle",
                    "Bottle"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.mobile_phone,
                    "Mobile Phone",
                    "Mobile Phone"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.bed, "Bed", "Bed"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.lock, "Lock", "Lock"))
        } else if (i2 == 16) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.accountant,
                    "Accountant",
                    "Accountant"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.architect,
                    "Architect",
                    "Architect"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.astronomer,
                    "Astronomer",
                    "Astronomer"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.author,
                    "Author",
                    "Author"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.baker, "Baker", "Baker"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.bricklayer,
                    "Bricklayer",
                    "Bricklayer"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.butcher,
                    "Butcher",
                    "Butcher"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.carpenter,
                    "Carpenter",
                    "Carpenter"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.chef, "Chef", "Chef"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cleaner,
                    "Cleaner",
                    "Cleaner"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.dentist,
                    "Dentist",
                    "Dentist"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.doctor,
                    "Doctor",
                    "Doctor"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.driver,
                    "Driver",
                    "Driver"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.dustman,
                    "Dustman",
                    "Dustman"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.electrician,
                    "Electrician",
                    "Electrician"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.engineer,
                    "Engineer",
                    "Engineer"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.farmer,
                    "Farmer",
                    "Farmer"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.firefighter,
                    "Firefighter",
                    "Firefighter"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.florist,
                    "Florist",
                    "Florist"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.gardener,
                    "Gardener",
                    "Gardener"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.hairdresser,
                    "Hairdresser",
                    "Hairdresser"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.journalist,
                    "Journalist",
                    "Journalist"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.judge, "Judge", "Judge"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.lawyer,
                    "Lawyer",
                    "Lawyer"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.lecturer,
                    "Lecturer",
                    "Lecturer"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.librarian,
                    "Librarian",
                    "Librarian"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.lifeguard,
                    "Lifeguard",
                    "Lifeguard"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.mechanics,
                    "Mechanics",
                    "Mechanics"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.model,
                    ExifInterface.TAG_MODEL,
                    ExifInterface.TAG_MODEL
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.nurse, "Nurse", "Nurse"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.optician,
                    "Optician",
                    "Optician"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.painter,
                    "Painter",
                    "Painter"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.pharmacist,
                    "Pharmacist",
                    "Pharmacist"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.photographer,
                    "Photographer",
                    "Photographer"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.pilot, "Pilot", "Pilot"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.plumber,
                    "Plumber",
                    "Plumber"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.receptionist,
                    "Receptionist",
                    "Receptionist"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.scientist,
                    "Scientist",
                    "Scientist"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.soldier,
                    "Soldier",
                    "Soldier"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.student,
                    "Student",
                    "Student"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.tailor,
                    "Tailor",
                    "Tailor"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.traffic_warden,
                    "Traffic Warden",
                    "Traffic Warden"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.veterinarian,
                    "Veterinarian",
                    "Veterinarian"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.waiter,
                    "Waiter",
                    "Waiter"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.welder,
                    "Welder",
                    "Welder"
                )
            )
        } else if (i2 == 17) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.board, "Board", "Board"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.book, "Book", "Book"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.chair, "Chair", "Chair"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.compass,
                    "Compass",
                    "Compass"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.computer,
                    "Computer",
                    "Computer"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.desk, "Desk", "Desk"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.dictionary,
                    "Dictionary",
                    "Dictionary"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.eraser,
                    "Eraser",
                    "Eraser"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.globe, "Globe", "Globe"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.map, "Map", "Map"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.notebook,
                    "Notebook",
                    "Notebook"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.pen, "Pen", "Pen"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.pencil,
                    "Pencil",
                    "Pencil"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.ruler, "Ruler", "Ruler"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.school_bag,
                    "School bag",
                    "School bag"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.teacher,
                    "Teacher",
                    "Teacher"
                )
            )
        } else if (i2 == 18) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.chess, "Chess", "Chess"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.windsurfing,
                    "Windsurfing",
                    "Windsurfing"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.bowling,
                    "Bowling",
                    "Bowling"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.karate,
                    "Karate",
                    "Karate"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.ice_skating,
                    "Ice Skating",
                    "Ice Skating"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.table_tennis,
                    "Table Tennis",
                    "Table Tennis"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.badminton,
                    "Badminton",
                    "Badminton"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.swimming,
                    "Swimming",
                    "Swimming"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.football,
                    "Football",
                    "Football"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.hockey,
                    "Hockey",
                    "Hockey"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.equestrian,
                    "Equestrian",
                    "Equestrian"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.cycling,
                    "Cycling",
                    "Cycling"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.diving,
                    "Diving",
                    "Diving"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.judo, "Judo", "Judo"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.golf, "Golf", "Golf"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.baseball,
                    "Baseball",
                    "Baseball"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.volleyball,
                    "Volleyball",
                    "Volleyball"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.surfing,
                    "Surfing",
                    "Surfing"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.skateboarding,
                    "Skateboarding",
                    "Skateboarding"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.skiing,
                    "Skiing",
                    "Skiing"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.archery,
                    "Archery",
                    "Archery"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.canoeing,
                    "Canoeing",
                    "Canoeing"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.running,
                    "Running",
                    "Running"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.billiards,
                    "Billiards",
                    "Billiards"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.fencing,
                    "Fencing",
                    "Fencing"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.tennis,
                    "Tennis",
                    "Tennis"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.basketball,
                    "Basketball",
                    "Basketball"
                )
            )
        } else if (i2 == 19) {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.ambulance,
                    "Ambulance",
                    "Ambulance"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.bike, "Bike", "Bike"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.boat, "Boat", "Boat"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.bus, "Bus", "Bus"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.car, "Car", "Car"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.container_truck,
                    "Container Truck",
                    "Container Truck"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.fire_truck,
                    "Fire Truck",
                    "Fire Truck"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.helicopter,
                    "Helicopter",
                    "Helicopter"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.motorbike,
                    "Motorbike",
                    "Motorbike"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.plane, "Plane", "Plane"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.police_car,
                    "Police Car",
                    "Police Car"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.ship, "Ship", "Ship"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.subway,
                    "Subway",
                    "Subway"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.train, "Train", "Train"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.truck, "Truck", "Truck"))
        } else {
            learningDataModelArrayList = ArrayList<LearningDataModel?>()
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.a,
                    "A for Apple",
                    "Apple"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.b, "B for Ball", "Ball"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.c, "C for Cat", "Cat"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.d, "D for Dog", "Dog"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.e,
                    "E for Elephant",
                    "Elephant"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.f, "F for Fish", "Fish"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.g, "G for Goat", "Goat"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.h,
                    "H for Horse",
                    "Horse"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.i,
                    "I for Ice cream",
                    "Ice cream"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.j,
                    "J for Joker",
                    "Joker"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.k, "K for Kite", "Kite"))
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.l, "L for Lion", "Lion"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.m,
                    "M for Monkey",
                    "Monkey"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.n, "N for Nest", "Nest"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.o,
                    "O for Orange",
                    "Orange"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.p,
                    "P for Parrot",
                    "Parrot"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.q,
                    "Q for Queen",
                    "Queen"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.r,
                    "R for Rabbit",
                    "Rabbit"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.s, "S for Sun", "Sun"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.t,
                    "T for Train",
                    "Train"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.u,
                    "U for Umbrella",
                    "Umbrella"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.v,
                    "V for Violin",
                    "Violin"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.w,
                    "W for Watch",
                    "Watch"
                )
            )
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.x,
                    "X for Xylophone",
                    "Xylophone"
                )
            )
            learningDataModelArrayList!!.add(LearningDataModel(R.drawable.y, "Y for Yak", "Yak"))
            learningDataModelArrayList!!.add(
                LearningDataModel(
                    R.drawable.z,
                    "Z for Zebra",
                    "Zebra"
                )
            )
        }
        setRvAdapter()
    }


    var subHomeAdapter: SubHomeAdapter? = null
    var rvSubHome: RecyclerView? = null

    private fun setRvAdapter() {
        val gridLayoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        rvSubHome!!.setLayoutManager(gridLayoutManager)
        subHomeAdapter = SubHomeAdapter(context!!,
            learningDataModelArrayList as List<LearningDataModel>, position, TYPE)
        rvSubHome!!.setAdapter(subHomeAdapter)
    }

    fun onClickBack(view: View?) {
        finish()
    }
}
