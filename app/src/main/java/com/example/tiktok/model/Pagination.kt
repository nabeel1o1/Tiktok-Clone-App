package com.example.tiktok.model

data class Pagination(
    val currentPage: Int,
    val hasNextPage: Boolean,
    val itemsPerPage: Int
)