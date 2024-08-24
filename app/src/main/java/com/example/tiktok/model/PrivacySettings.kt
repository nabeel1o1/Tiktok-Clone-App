package com.example.tiktok.model

data class PrivacySettings(
    val allowComments: Boolean,
    val comment: String,
    val download: Boolean,
    val duet: String,
    val stitch: String,
    val view: String
)