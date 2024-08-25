package com.example.tiktok.viewmodel

import android.media.MediaCodecList
import android.media.MediaFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.liveData
import androidx.paging.map
import com.example.tiktok.model.getVideoUrl
import com.example.tiktok.repository.HomeFeedRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class HomeFeedViewModel @Inject constructor(private val homeFeedRepository: HomeFeedRepo) : ViewModel() {

    private val supportsH265 = isCodecSupported(MediaFormat.MIMETYPE_VIDEO_HEVC)
    private val supportsH264 = isCodecSupported(MediaFormat.MIMETYPE_VIDEO_AVC)

    val homeFeedDataList = getHomeFeedData().cachedIn(viewModelScope)

    private fun getHomeFeedData() = Pager(
        config = PagingConfig(pageSize = 40),
        pagingSourceFactory = { homeFeedRepository }
    ).liveData.map { pagingData ->
        pagingData.map { videoItem ->
            videoItem.copy(media = videoItem.media.copy(
                key = videoItem.media.getVideoUrl(supportsH265, supportsH264) ?: videoItem.media.key
            ))
        }
    }

    private fun isCodecSupported(codecName: String): Boolean {
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        for (codecInfo in codecList.codecInfos) {
            if (codecInfo.isEncoder) continue
            for (type in codecInfo.supportedTypes) {
                if (type.equals(codecName, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }
}