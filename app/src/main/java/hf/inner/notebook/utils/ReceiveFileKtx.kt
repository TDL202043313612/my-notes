package hf.inner.notebook.utils

import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import hf.inner.notebook.App
import hf.inner.notebook.bean.Attachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.tdl.luban.Luban
import java.io.File

suspend fun handlePickFiles(
    uris: Set<Uri>, callback: (list: List<Attachment>) -> Unit
) {
    val items = mutableListOf<Attachment>()

    withContext(Dispatchers.IO) {
        uris.forEach { uri ->
            val context = App.instance
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                // OpenableColumns.DISPLAY_NAME：查询文件名
                var fileName = cursor.getStringValue(OpenableColumns.DISPLAY_NAME)
                val type = context.contentResolver.getType(uri) ?: ""
                var extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type)
                if (extension.isNullOrEmpty()) {
                    extension = fileName.getFilenameExtension()
                }
                if (extension.isNotEmpty()) {
                    fileName = fileName.getFilenameWithoutExtension() + "." + extension
                }
                cursor.close()
                val fileType: Attachment.Type
                try {
                    val dir = when {
                        fileName.isVideoFast() -> {
                            fileType = Attachment.Type.VIDEO
                            Environment.DIRECTORY_MOVIES
                        }
                        fileName.isImageFast() -> {
                            fileType = Attachment.Type.IMAGE
                            Environment.DIRECTORY_PICTURES
                        }
                        fileName.isAudioFast() -> {
                            fileType = Attachment.Type.AUDIO
                            Environment.DIRECTORY_MUSIC
                        }
                        else -> {
                            fileType = Attachment.Type.FILE
                            Environment.DIRECTORY_DOCUMENTS
                        }
                    }
                    val dst = context.getExternalFilesDir(dir)!!.path + "/$fileName"
                    val dstFile = File(dst)
                    val finalPath = if (dstFile.exists()) {
                        val newPath = dstFile.newPath()
                        copyFile(context, uri, newPath)
                        newPath
                    } else {
                        copyFile(context, uri, dst)
                        dst
                    }
                    // 图片压缩
                    Luban.with(context)
                        .setTargetDir(context.getExternalFilesDir(dir)!!.path)  // 设置压缩后的文件存到哪
                        .load(finalPath) // 载入刚才从系统读取并保存好的原图文件
                        .get() // 同步执行压缩，返回压缩后的文件列表 List<File>
                        .forEach {
                            if (it.exists() && it.path != finalPath) {
                                File(finalPath).delete()
                            }
                            items.add(Attachment(path = it.path, fileName = it.name, description = it.name, type = fileType))
                        }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
        callback(items)
    }
}