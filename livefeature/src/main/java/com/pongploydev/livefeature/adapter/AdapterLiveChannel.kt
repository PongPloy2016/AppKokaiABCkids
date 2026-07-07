package com.pongploydev.livefeature.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pongploydev.livefeature.R
import com.pongploydev.livefeature.databinding.ItemLiveChannelBinding
import com.pongploydev.livefeature.model.DltvChannel

class AdapterLiveChannel(
    private val channels: List<DltvChannel>,
    private val onItemClick: (DltvChannel) -> Unit
) : RecyclerView.Adapter<AdapterLiveChannel.ViewHolder>() {

    inner class ViewHolder(val binding: ItemLiveChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(channel: DltvChannel) {
            binding.tvChannelName.text = channel.displayName
            binding.tvChannelGrade.text = channel.grade
            binding.tvChannelShortDesc.text = channel.shortDescription

            // Load Channel Logo
            Glide.with(binding.root.context)
                .load(channel.logo)
                .placeholder(R.drawable.ic_live)
                .into(binding.imgChannelLogo)

            // Pulse animation for live dot
            val anim = AlphaAnimation(1.0f, 0.2f).apply {
                duration = 800
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            }
            binding.liveDot.startAnimation(anim)

            binding.root.setOnClickListener {
                onItemClick(channel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLiveChannelBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(channels[position])
    }

    override fun getItemCount(): Int = channels.size
}
