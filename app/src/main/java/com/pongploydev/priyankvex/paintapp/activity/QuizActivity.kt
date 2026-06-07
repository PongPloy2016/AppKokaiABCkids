package com.pongploydev.priyankvex.paintapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.pongploydev.priyankvex.paintapp.R
import com.pongploydev.priyankvex.paintapp.adapter.QuizTopic
import com.pongploydev.priyankvex.paintapp.adapter.QuizTopicAdapter
import com.pongploydev.priyankvex.paintapp.databinding.ActivityQuizListBinding

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "เลือกแบบทดสอบ"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        val topics =
                listOf(
                        QuizTopic(
                                1,
                                "ภาษาไทย ก-ฮ",
                                "ทดสอบความรู้พยัญชนะไทย 44 ตัว",
                                R.drawable.card_one,
                                R.color.color_red
                        ),
                        QuizTopic(
                                2,
                                "English A-Z",
                                "Test your English Alphabet knowledge",
                                R.drawable.card_two,
                                R.color.color_blue
                        ),
                        QuizTopic(
                                3,
                                "ตัวเลข 0-9",
                                "ทดสอบการนับเลขและจำตัวเลข",
                                R.drawable.card_three,
                                R.color.color_green
                        ),
                        QuizTopic(
                                4,
                                "English Words",
                                "คำศัพท์ภาษาอังกฤษ A for Apple, B for Ball",
                                R.drawable.card_one,
                                R.color.color_pink
                        ),
                        QuizTopic(
                                5,
                                "คำศัพท์อังกฤษ-ไทย",
                                "ทายคำศัพท์จากรูปเป็นภาษาไทย",
                                R.drawable.card_two,
                                R.color.color_orange
                        ),
                        QuizTopic(
                                6,
                                "ตัวเลขเป็นคำ",
                                "Zero, One, Two... ทดสอบตัวเลขภาษาอังกฤษ",
                                R.drawable.card_three,
                                R.color.color_purple
                        )
                )

        binding.rvTopics.layoutManager = LinearLayoutManager(this)
        binding.rvTopics.adapter = QuizTopicAdapter(topics) { topic -> startQuizSession(topic) }
    }

    private fun startQuizSession(topic: QuizTopic) {
        val intent =
                Intent(this, QuizSessionActivity::class.java).apply {
                    putExtra("EXTRA_TOPIC_ID", topic.id)
                    putExtra("EXTRA_TOPIC_TITLE", topic.title)
                }
        startActivity(intent)
    }
}
