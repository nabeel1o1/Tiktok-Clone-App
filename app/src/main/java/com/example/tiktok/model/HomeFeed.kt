package com.example.tiktok.model

data class HomeFeed(
    val data: List<Data>,
    val message: String,
    val pagination: Pagination
)