package com.example.eatstoday.core.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Ảnh được lưu trong bộ nhớ riêng của ứng dụng để vẫn đọc được sau khi khởi động lại. */
object ImageFiles {
    private fun newFile(context: Context): File {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        return File(dir, "${UUID.randomUUID()}.jpg")
    }

    fun newPhotoPath(context: Context): String = newFile(context).absolutePath

    fun uriForPath(context: Context, path: String): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(path))

    suspend fun copyToApp(context: Context, source: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val target = newFile(context)
            val input = context.contentResolver.openInputStream(source) ?: return@runCatching null
            input.use { stream -> target.outputStream().use { stream.copyTo(it) } }
            target.absolutePath
        }.getOrNull()
    }
}
