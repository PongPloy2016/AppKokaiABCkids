package com.pongploydev.livefeature.model

import java.io.Serializable

data class DltvChannel(
    val id: Int,
    val code: String,
    val channelNumber: Int,
    val name: String,
    val displayName: String,
    val title: String,
    val level: String,
    val grade: String,
    val logo: String,
    val stream: String,
    val description: String,
    val shortDescription: String,
    val status: String,
    val isLive: Boolean,
    val slug: String? = null,
    val pageUrl: String? = null,
    val scheduleUrl: String? = null,
    val vodUrl: String? = null,
    val thumbnail: String? = null,
    val language: String? = null,
    val resolution: String? = null,
    val streamType: String? = null,
    val videoFormat: String? = null,
    val category: String? = null,
    val country: String? = null,
    val provider: String? = null,
    val organization: String? = null,
    val updatedAt: String? = null
) : Serializable
