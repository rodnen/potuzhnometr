package com.example.potuzhnometr.updater

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.potuzhnometr.R
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class UpdateChecker(
    private val context: Context,
    private val onUpdateAvailable: (version: String, downloadUrl: String) -> Unit,
    private val onUpToDate: (String) -> Unit,
    private val onProgress: (Int) -> Unit,
    private val onSuccess: () -> Unit,
    private val onError: (String) -> Unit,
) {
    private lateinit var apkUrl : String
    private lateinit var latestVersion : String
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
                    val currentVersion = context.packageManager
                        .getPackageInfo(context.packageName, 0).versionName.toString()

                    latestVersion = release.tagName.trimStart('v')

                    apkUrl = release.assets.firstOrNull { it.name.endsWith(".apk") }?.browserDownloadUrl.toString()
                    if (apkUrl == "null" || apkUrl == "") {
                        onError("Не знайдено .apk файл у релізі")
                        return
                    }

                    if (isNewerVersion(latestVersion, currentVersion)) {
                        onUpdateAvailable(latestVersion, apkUrl)
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

    fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true
        } else {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermission(activity: Activity) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), 1001)
            }
        }
    }

    @SuppressLint("Range")
    fun startDownload() {
        if (!(::apkUrl.isInitialized && ::latestVersion.isInitialized)) {
            Log.e("UpdateChecker", "Download failed")
            onError("Немає посилання на завантаження оновлення")
            return
        }

        val request = Request.Builder().url(apkUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UpdateChecker", "Download failed", e)
                onError("Помилка при завантаженні оновлення")
            }

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError("Помилка відповіді при завантаженні: ${response.code}")
                    return
                }

                try {
                    val fileName = "potuzhnometr-v$latestVersion.apk"
                    val inputStream = response.body?.byteStream()
                    val totalBytes = response.body?.contentLength() ?: -1L

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        saveFileToMediaStore(inputStream, fileName, totalBytes, onProgress, onSuccess)
                    } else {
                        saveFileToExternalStorage(inputStream, fileName, totalBytes, onProgress, onSuccess)
                    }

                } catch (e: Exception) {
                    Log.e("UpdateChecker", "Write error", e)
                    onError("Помилка при записі оновлення")
                }
            }
        })
    }

    private fun saveFileToExternalStorage(
        inputStream: InputStream?,
        fileName: String,
        totalBytes: Long,
        onProgress: (Int) -> Unit,
        onSuccess: () -> Unit
    ) {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val apkFile = File(downloadsDir, fileName)
            val outputStream = FileOutputStream(apkFile)

            copyStreamWithProgress(inputStream, outputStream, totalBytes, onProgress)

            MediaScannerConnection.scanFile(
                context,
                arrayOf(apkFile.absolutePath),
                arrayOf("application/vnd.android.package-archive"),
                null
            )

            onSuccess()
        } catch (e: Exception) {
            Log.e("UpdateChecker", "Save error (legacy)", e)
            onError("Помилка при збереженні оновлення (старий Android)")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFileToMediaStore(
        inputStream: InputStream?,
        fileName: String,
        totalBytes: Long,
        onProgress: (Int) -> Unit,
        onSuccess: () -> Unit
    ) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/vnd.android.package-archive")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val fileUri = resolver.insert(collection, contentValues)

        if (fileUri == null) {
            onError("Не вдалося створити файл для збереження .apk")
            return
        }

        resolver.openOutputStream(fileUri).use { outputStream ->
            copyStreamWithProgress(inputStream, outputStream, totalBytes, onProgress)
        }

        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(fileUri, contentValues, null, null)

        onSuccess()
    }

    private fun copyStreamWithProgress(
        input: InputStream?,
        output: OutputStream?,
        totalBytes: Long,
        onProgress: (Int) -> Unit
    ) {
        if (input == null || output == null) return

        val buffer = ByteArray(8192)
        var bytesCopied: Long = 0
        var lastProgress = -1

        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
            bytesCopied += read

            if (totalBytes > 0) {
                val progress = (bytesCopied * 100 / totalBytes).toInt()
                if (progress != lastProgress) {
                    lastProgress = progress
                    onProgress(progress)
                }
            }
        }

        output.flush()
    }
}
