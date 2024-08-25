package com.example.tiktok.model

data class Media(
    val h264: ResolutionUrls?,
    val h265: ResolutionUrls?,
    val key: String,
    val thumbnailKey: String,
    val transcoded: Boolean
)

fun Media.getVideoUrl(supportsH265: Boolean, supportsH264: Boolean): String? {
    return when {
        supportsH265 -> h265?.getBestResolution()
        supportsH264 -> h264?.getBestResolution()
        else -> ""
    }
}
