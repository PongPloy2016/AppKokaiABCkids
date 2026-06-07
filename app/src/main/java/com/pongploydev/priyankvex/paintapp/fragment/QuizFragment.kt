package com.pongploydev.priyankvex.paintapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.activity.QuizSessionActivity
import com.pongploydev.priyankvex.paintapp.adapter.QuizTopic
import com.pongploydev.priyankvex.paintapp.adapter.QuizTopicAdapter
import com.pongploydev.priyankvex.paintapp.databinding.FragmentQuizListBinding

class QuizFragment : Fragment() {

    private var _binding: FragmentQuizListBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val topics =
                listOf(
                        QuizTopic(
                                1,
                                "ภาษาไทย ก-ฮ",
                                "ทดสอบความรู้พยัญชนะไทย 44 ตัว",
                                R.drawable.card_one,
                                R.color.color_red,
                                R.drawable.quiz_card_gradient_red
                        ),
                        QuizTopic(
                                2,
                                "English A-Z",
                                "Test your English Alphabet knowledge",
                                R.drawable.card_two,
                                R.color.color_blue,
                                R.drawable.quiz_card_gradient_blue
                        ),
                        QuizTopic(
                                3,
                                "ตัวเลข 0-9",
                                "ทดสอบการนับเลขและจำตัวเลข",
                                R.drawable.card_three,
                                R.color.color_green,
                                R.drawable.quiz_card_gradient_green
                        ),
                        QuizTopic(
                                4,
                                "English Words",
                                "คำศัพท์ภาษาอังกฤษ A for Apple, B for Ball",
                                R.drawable.card_one,
                                R.color.color_pink,
                                R.drawable.quiz_card_gradient_pink
                        ),
                        QuizTopic(
                                5,
                                "คำศัพท์อังกฤษ-ไทย",
                                "ทายคำศัพท์จากรูปเป็นภาษาไทย",
                                R.drawable.card_two,
                                R.color.color_orange,
                                R.drawable.quiz_card_gradient_orange
                        ),
                        QuizTopic(
                                6,
                                "ตัวเลขเป็นคำ",
                                "Zero, One, Two... ทดสอบตัวเลขภาษาอังกฤษ",
                                R.drawable.card_three,
                                R.color.color_purple,
                                R.drawable.quiz_card_gradient_purple
                        ),
                        QuizTopic(
                                7,
                                "สัตว์ Animals 🐾",
                                "ทายชื่อสัตว์จากรูปภาพ",
                                R.drawable.home_animal,
                                R.color.color_blue,
                                R.drawable.quiz_card_gradient_teal
                        ),
                        QuizTopic(
                                8,
                                "ผลไม้ Fruits 🍎",
                                "ทายชื่อผลไม้ภาษาอังกฤษ",
                                R.drawable.home_fruits,
                                R.color.color_red,
                                R.drawable.quiz_card_gradient_coral
                        ),
                        QuizTopic(
                                9,
                                "สี Colors 🎨",
                                "ทดสอบชื่อสีภาษาอังกฤษ",
                                R.drawable.home_color,
                                R.color.color_purple,
                                R.drawable.quiz_card_gradient_indigo
                        ),
                        QuizTopic(
                                10,
                                "ผัก Vegetables 🥦",
                                "ทายชื่อผักภาษาอังกฤษ",
                                R.drawable.home_vegetable,
                                R.color.color_green,
                                R.drawable.quiz_card_gradient_emerald
                        ),
                        QuizTopic(
                                11,
                                "กีฬา Sports ⚽",
                                "ทายชื่อกีฬาจากรูปภาพ",
                                R.drawable.home_sports,
                                R.color.color_pink,
                                R.drawable.quiz_card_gradient_sunset
                        ),
                        QuizTopic(
                                12,
                                "ยานพาหนะ Vehicles 🚗",
                                "ทายชื่อยานพาหนะภาษาอังกฤษ",
                                R.drawable.home_vehicle,
                                R.color.color_orange,
                                R.drawable.quiz_card_gradient_golden
                        )
                )

        binding.rvTopics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopics.adapter = QuizTopicAdapter(topics) { topic -> startQuizSession(topic) }
    }

    private fun startQuizSession(topic: QuizTopic) {
        val intent =
                Intent(requireContext(), QuizSessionActivity::class.java).apply {
                    putExtra("EXTRA_TOPIC_ID", topic.id)
                    putExtra("EXTRA_TOPIC_TITLE", topic.title)
                }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
