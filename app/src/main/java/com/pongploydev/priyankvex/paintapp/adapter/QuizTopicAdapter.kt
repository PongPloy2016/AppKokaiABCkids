package com.pongploydev.priyankvex.paintapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pongploydev.priyankvex.paintapp.R

data class QuizTopic(
        val id: Int,
        val title: String,
        val description: String,
        val iconRes: Int,
        val colorRes: Int,
        val gradientRes: Int = 0
)

class QuizTopicAdapter(
        private val topics: List<QuizTopic>,
        private val onTopicClick: (QuizTopic) -> Unit
) : RecyclerView.Adapter<QuizTopicAdapter.TopicViewHolder>() {

    class TopicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardTopic: com.google.android.material.card.MaterialCardView =
                view.findViewById(R.id.cardTopic)
        val layoutBackground: LinearLayout = view.findViewById(R.id.layoutBackground)
        val ivTopicIcon: ImageView = view.findViewById(R.id.ivTopicIcon)
        val tvTopicTitle: TextView = view.findViewById(R.id.tvTopicTitle)
        val tvTopicDescription: TextView = view.findViewById(R.id.tvTopicDescription)
        val tvBadgeNumber: TextView = view.findViewById(R.id.tvBadgeNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_quiz_topic, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]
        holder.tvTopicTitle.text = topic.title
        holder.tvTopicDescription.text = topic.description
        holder.ivTopicIcon.setImageResource(topic.iconRes)

        // Badge number
        holder.tvBadgeNumber.text = "${position + 1}"

        // Use gradient drawable if provided, otherwise fall back to flat color
        if (topic.gradientRes != 0) {
            holder.layoutBackground.background =
                    ContextCompat.getDrawable(holder.itemView.context, topic.gradientRes)
        } else {
            val color = ContextCompat.getColor(holder.itemView.context, topic.colorRes)
            holder.layoutBackground.setBackgroundColor(color)
        }

        // Ripple click effect
        holder.cardTopic.setOnClickListener { onTopicClick(topic) }
    }

    override fun getItemCount() = topics.size
}
