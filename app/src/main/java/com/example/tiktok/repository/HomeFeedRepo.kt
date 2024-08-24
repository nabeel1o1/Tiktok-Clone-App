package com.example.tiktok.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tiktok.interfaces.TiktokApiService
import com.example.tiktok.model.Data
import javax.inject.Inject

class HomeFeedRepo @Inject constructor(private val tiktokApi: TiktokApiService) :
    PagingSource<Int, Data>() {

    override fun getRefreshKey(state: PagingState<Int, Data>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Data> {
        return try {
            val position = params.key ?: 1
            val response = tiktokApi.getHomeFeed(page = position)
            LoadResult.Page(
                data = response.data,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (response.pagination.hasNextPage) position + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}