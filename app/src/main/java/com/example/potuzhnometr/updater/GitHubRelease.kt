package com.example.potuzhnometr.updater

import com.google.gson.annotations.SerializedName

data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String,
    val name: String,
    val assets: List<Asset>
)

data class Asset(
    val name: String,
    @SerializedName("browser_download_url") val browserDownloadUrl: String
)