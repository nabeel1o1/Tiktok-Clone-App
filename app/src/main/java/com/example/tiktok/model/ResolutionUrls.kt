package com.example.tiktok.model

import com.google.gson.annotations.SerializedName

data class ResolutionUrls(
    @SerializedName("1080p")
    val p1080: String,
    @SerializedName("720p")
    val p720: String,
    @SerializedName("480p")
    val p480: String
) {
    fun getBestResolution(): String {
        return when {
            p480.isNotEmpty() -> p480 //Setting the priority to 480p as per the requirement of assessment task
            p1080.isNotEmpty() -> p1080
            else -> p720
        }
    }
}