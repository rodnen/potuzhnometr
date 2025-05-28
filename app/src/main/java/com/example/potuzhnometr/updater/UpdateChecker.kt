package com.example.potuzhnometr.updater

import android.content.Context
import android.util.Log
import com.example.potuzhnometr.R
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class UpdateChecker(
    private val context: Context,
    private val onUpdateAvailable: (String) -> Unit,
    private val onUpToDate: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private val client = OkHttpClient()
    private val versionUrl = "https://api.github.com/repos/rodnen/potuzhnometr/releases/latest"

    fun check() {
        val request = Request.Builder().url(versionUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UpdateChecker", "Failed to fetch release", e)
                onError("Не вдалося перевірити оновлення")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body == null) {
                    onError("Порожня відповідь")
                    return
                }

                try {
                    val release = Gson().fromJson(body, GitHubRelease::class.java)
                    val latestVersion = release.tagName.trimStart('v')
                    val currentVersion = context.packageManager
                        .getPackageInfo(context.packageName, 0).versionName.toString()

                    if (isNewerVersion(latestVersion, currentVersion)) {
                        onUpdateAvailable(latestVersion)
                    } else {
                        onUpToDate(context.getString(R.string.upd_def_msg))
                    }
                } catch (e: Exception) {
                    Log.e("UpdateChecker", "Parse error: $body", e)
                    onError("Помилка при розборі версії")
                }
            }
        })
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".")
        val currentParts = current.split(".")

        val maxLength = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLength) {
            val latestPart = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
            val currentPart = currentParts.getOrNull(i)?.toIntOrNull() ?: 0

            if (latestPart > currentPart) return true
            if (latestPart < currentPart) return false
        }
        return false
    }
}