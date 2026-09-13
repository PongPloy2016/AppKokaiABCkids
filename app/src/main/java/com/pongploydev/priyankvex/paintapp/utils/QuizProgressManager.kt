package com.pongploydev.priyankvex.paintapp.utils

import android.content.Context
import android.content.SharedPreferences

object QuizProgressManager {
    private const val PREF_NAME = "quiz_progress_prefs"
    private const val KEY_MAX_UNLOCKED_LESSON = "max_unlocked_lesson"
    private const val KEY_TOPIC_SCORE_PREFIX = "topic_score_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Returns the highest unlocked lesson topicId (1-based).
     * Default is 2 (Lesson 1 and Lesson 2 are unlocked by default).
     */
    fun getMaxUnlockedLesson(context: Context): Int {
        return getPrefs(context).getInt(KEY_MAX_UNLOCKED_LESSON, 2)
    }

    /**
     * Set max unlocked lesson.
     */
    fun setMaxUnlockedLesson(context: Context, maxLesson: Int) {
        val currentMax = getMaxUnlockedLesson(context)
        if (maxLesson > currentMax) {
            getPrefs(context).edit().putInt(KEY_MAX_UNLOCKED_LESSON, maxLesson).apply()
        }
    }

    /**
     * Returns the highest score recorded for a topic (or -1 if not completed yet).
     */
    fun getTopicHighScore(context: Context, topicId: Int): Int {
        return getPrefs(context).getInt("$KEY_TOPIC_SCORE_PREFIX$topicId", -1)
    }

    /**
     * Checks if a topic (1-based topicId) has been passed (score >= 50%, i.e., >= 10 out of 20).
     */
    fun isTopicPassed(context: Context, topicId: Int): Boolean {
        val score = getTopicHighScore(context, topicId)
        return score >= 10
    }

    /**
     * Returns whether a specific topicId (1-based) is unlocked.
     * Rules:
     * - Topic 1 and 2 are always unlocked by default.
     * - Topic N (N >= 3) is unlocked if Topic (N-1) is passed OR if stored maxUnlockedLesson >= N.
     */
    fun isTopicUnlocked(context: Context, topicId: Int): Boolean {
        if (topicId <= 2) return true
        if (topicId <= getMaxUnlockedLesson(context)) return true
        // Check if the immediate previous topic was passed
        return isTopicPassed(context, topicId - 1)
    }

    /**
     * Call when a quiz session finishes. Records highest score and unlocks next lesson if passed!
     * Passing threshold: score >= totalQuestions / 2 (e.g. >= 10 out of 20)
     */
    fun recordQuizResult(context: Context, topicId: Int, score: Int, totalQuestions: Int) {
        val prefs = getPrefs(context)
        val currentBest = getTopicHighScore(context, topicId)
        if (score > currentBest) {
            prefs.edit().putInt("$KEY_TOPIC_SCORE_PREFIX$topicId", score).apply()
        }

        // Check if user passed (score >= 50%)
        val isPassed = score >= (totalQuestions / 2)
        if (isPassed) {
            val nextLesson = topicId + 1
            setMaxUnlockedLesson(context, nextLesson)
        }
    }
}
