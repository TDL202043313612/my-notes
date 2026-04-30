package hf.inner.notebook.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Base64.encodeToString
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import dalvik.system.ZipPathValidator
import hf.inner.notebook.App
import hf.inner.notebook.AppEntryPoint
import hf.inner.notebook.bean.Attachment
import hf.inner.notebook.bean.Note
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.bean.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.zeroturnaround.zip.ZipUtil
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.Buffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class ExportItem(val dir: String, val file: File)

object BackUp {
    private const val MY_KEY = "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6"
    suspend fun exportEncrypted(context: Context, uri: Uri) = suspendCoroutine { continuation ->
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            // 创建加密对象+AES加密算法
            val cipher = Cipher.getInstance("AES")
            // 把自己的密钥字符串转换成AES能用的密钥
            val secretKey = SecretKeySpec(MY_KEY.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            // CipherOutputStream加密 -》ZipOutputStream打包zip
            ZipOutputStream(BufferedOutputStream(CipherOutputStream(outputStream, cipher))).use { zipOut ->
                try {
                    val files = listOf(
                        ExportItem("", File(context.dataDir.path + "/databases")), // 数据库
                        ExportItem("", context.filesDir), // 私有文件
                        ExportItem("external/", context.getExternalFilesDir(null)!!) // 外部私有文件
                    )
                    for (i in files.indices) {
                        val item = files[i]
                        appendFile(zipOut, item.dir, item.file)
                    }
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val fileName = cursor.getStringValue(OpenableColumns.DISPLAY_NAME)
                            continuation.resume(fileName)
                        }
                    }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }

        }
    }

    suspend fun restoreFromEncryptedZip(context: Context, uri: Uri, isEncrypt: Boolean) = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= 34) {
            // 在Android 14 中引入了ZIP 文件安全校验机制。这里直接关闭安全校验机制
            ZipPathValidator.clearCallback()
        }
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val stream = if (isEncrypt) {
                val cipher = Cipher.getInstance("AES")
                val secretKey = SecretKeySpec(MY_KEY.toByteArray(), "AES")
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
                CipherInputStream(inputStream, cipher)
            } else {
                inputStream
            }
            val destFile = File(context.cacheDir, "decrypted_restore")
            if (!destFile.exists()) {
                destFile.mkdirs()
            }
            val zipStream = ZipInputStream(BufferedInputStream(stream))
            var entry = zipStream.nextEntry
            while (entry != null) {
                val entryFile = File(destFile, entry.name)
                entryFile.parentFile?.mkdirs()

                if (!entry.isDirectory) {
                    FileOutputStream(entryFile).use { outputStream ->
                        zipStream.copyTo(outputStream)
                    }
                }
                entry = zipStream.nextEntry
            }

            if (File(destFile.path + "/databases").exists()) {
                File(destFile.path + "/databases").copyRecursively(File(context.dataDir.path + "/databases"), true)
            }
            if (File(destFile.path + "/files").exists()) {
                File(destFile.path + "/files").copyRecursively(context.filesDir, true)
            }
            if (File(destFile.path + "/external/files").exists()) {
                File(destFile.path + "/external/files").copyRecursively(context.getExternalFilesDir(null)!!, true)
            }
            // 层级删除临时文件
            destFile.deleteRecursively()
        }
    }

    suspend fun exportJson(list: List<NoteShowBean>, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        // 转换为 JSON 格式
        val json = Json.encodeToString(list.toSet())
        // runCatching 异常处理函数,成功Result.success()，失败Result.failure(exception)
        val result = runCatching {
            BufferedOutputStream(App.instance.contentResolver.openOutputStream(uri)).use { outputStream: BufferedOutputStream ->
                outputStream.write(json.toByteArray())
            }
        }
        result
    }

    suspend fun exportTXTFile(list: List<NoteShowBean>, uri: Uri) = withContext(Dispatchers.IO) {
        App.instance.contentResolver.openOutputStream(uri)?.use { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use {  out ->
                list.forEach { noteShowBean ->
                    out.append("${noteShowBean.note.createTime.toTime()}\n")
                    if (!noteShowBean.note.locationInfo.isNullOrEmpty()) {
                        out.append(noteShowBean.note.locationInfo.plus("\n"))
                    }
                    out.append(noteShowBean.note.content.plus("").plus("\n\n"))
                }
            }
        }
    }
    private fun appendFile(out: ZipOutputStream, dir: String, file: File) {
        if (file.isDirectory) {
            val files = file.listFiles()  ?: return
            for (childFile in files) {
                appendFile(out, "${dir}${file.name}/", childFile)
            }
        } else {
            // 开始写文件-写入文件内容-结束写文件
            val entry = ZipEntry("$dir${file.name}")
            entry.size = file.length()
            entry.time = file.lastModified()
            out.putNextEntry(entry)
            FileInputStream(file).use { inputStream ->
                inputStream.copyTo(out)
            }
            out.closeEntry()
        }
    }

    fun exportMarkDownFile(list: List<NoteShowBean>, uri: Uri) {
        App.instance.contentResolver.openOutputStream(uri)?.let { outputStream ->
            BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                list.forEach { noteShowBean ->
                    writer.append("### ${noteShowBean.note.createTime.toTime()}\n")
                    if (!noteShowBean.note.locationInfo.isNullOrEmpty()) {
                        writer.append(noteShowBean.note.locationInfo.plus("\n"))
                    }
                    writer.append(noteShowBean.note.content.plus(""))
                    writer.append("\n\n")
                }
            }
        }
    }

    suspend fun export(context: Context, uri: Uri): String = suspendCoroutine { continuation ->
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            ZipOutputStream(outputStream).use { out ->
                try {
                    val files = listOf(
                        ExportItem("", File(context.dataDir.path + "/databases")),
                        ExportItem("", context.filesDir),
                        ExportItem("external/", context.getExternalFilesDir(null)!!)
                    )
                    for (i in files.indices) {
                        val item = files[i]
                        appendFile(out, item.dir, item.file)
                    }
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val fileName = cursor.getStringValue(OpenableColumns.DISPLAY_NAME)
                            continuation.resume(fileName)
                        }
                    }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    suspend fun restoreFromSd(uri: Uri) = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= 34) {
            ZipPathValidator.clearCallback()
        }
        val context = App.instance
        App.instance.contentResolver.openInputStream(uri)?.use { stream ->
            val destFile = File(context.cacheDir, "restore")
            // 这里zip解压用的第三方库
            ZipUtil.unpack(stream, destFile)
            if(File(destFile.path + "/databases").exists()) {
                File(destFile.path + "/databases").copyRecursively(File(context.dataDir.path + "/databases"), true)
            }
            if (File(destFile.path + "/files").exists()) {
                // restore local storage
                File(destFile.path + "/files").copyRecursively(context.filesDir, true)
            }
            if (File(destFile.path + "/external/files").exists()) {
                // restore external files
                File(destFile.path + "/external/files").copyRecursively(context.getExternalFilesDir(null)!!, true)
            }
            destFile.deleteRecursively()
        }
    }

    suspend fun importFromHtmlZip(context: Context, uri: Uri): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= 34) {
                    ZipPathValidator.clearCallback()
                }
                val tempDir = File(context.cacheDir, "html_import_${System.currentTimeMillis()}")
                tempDir.mkdirs()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipUtil.unpack(inputStream, tempDir)
                }
                val htmlFile = findFirstHtmlFileAndPrintAll(tempDir) ?: return@withContext Result.failure(
                    Exception("HTML file not found in zip")
                )
                val htmlContent = htmlFile.readText()
                val document = Jsoup.parse(htmlContent)
                val memoElements = document.select(".memo")

                val notes = mutableListOf<Pair<Note, List<Tag>>>()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                memoElements.forEach { memoElement ->
                    try {
                        val timeElement = memoElement.select(".time").first()
                        val contentElement = memoElement.select(".content").first()
                        val filesElement = memoElement.select(".files").first()
                        val tagsAttr = memoElement.attr("data-tags")
                        val timeStr = timeElement?.text() ?: ""
                        val contentText = contentElement?.select("p")?.joinToString("\n") {
                            it.text()
                        } ?: ""
                        val createTime = try {
                            dateFormat.parse(timeStr)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                        val attachments = mutableListOf<Attachment>()
                        filesElement?.select("img")?.forEach { imgElement ->
                            val imgSrc = imgElement.attr("data-filepath")
                            // 使用相对于 HTML 文件的路径来寻找图片
                            val imgFile = File(htmlFile.parentFile, imgSrc)
                            if (imgFile.exists()) {
                                val destDir = File(
                                    context.getExternalFilesDir(null),
                                    "file/${
                                        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(
                                            Date(createTime)
                                        )
                                    }"
                                )
                                destDir.mkdirs()
                                val destFile = File(destDir, imgFile.name)
                                imgFile.copyTo(destFile, overwrite = true)
                                attachments.add(
                                    Attachment(
                                        path = destFile.absolutePath,
                                        type = Attachment.Type.IMAGE
                                    )
                                )
                            }
                        }
                        filesElement?.select("audio")?.forEach { audioElement ->
                            val imgSrc = audioElement.attr("data-filepath")
                            val imgFile = File(htmlFile.parentFile, imgSrc)
                            if (imgFile.exists()) {
                                val destDir = File(
                                    context.getExternalFilesDir(null),
                                    "file/${
                                        SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(
                                            Date(createTime)
                                        )
                                    }"
                                )
                                destDir.mkdirs()
                                val destFile = File(destDir, imgFile.name)
                                imgFile.copyTo(destFile, overwrite = true)
                                attachments.add(
                                    Attachment(
                                        path = destFile.absolutePath,
                                        type = Attachment.Type.AUDIO
                                    )
                                )
                            }
                        }
                        val tagList = tagsAttr.split(" ")
                            .filter { it.startsWith("#") }
                            // removePrefix: 如果字符串是以某个特定的前缀开头的，就把这个前缀删掉；
                            // 如果不是，就什么都不做，原样返回。
                            .map { it.removePrefix("#") }
                            .filter { it.isNotBlank() }
                            .map { Tag(it) }
                        val note = Note(
                            noteTitle = null,
                            content = contentText,
                            createTime = createTime,
                            updateTime = createTime,
                            attachments = attachments
                        )
                        notes.add(note to tagList)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val entryPoint = EntryPointAccessors.fromApplication(context, AppEntryPoint::class.java)
                val tagNoteRepo = entryPoint.tagNoteRepo()

                var importedCount = 0
                notes.forEach { (note, tagList) ->
                    try {
                        tagNoteRepo.insertOrUpdate(note)
                        importedCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                tempDir.deleteRecursively()
                Result.success(importedCount)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }

    suspend fun exportHtmlZip(list: List<NoteShowBean>, uri: Uri) = withContext(Dispatchers.IO) {
        val context = App.instance
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                try {
                    val htmlContent = generateHtml(list)
                    val htmlEntry = ZipEntry("IdeaMemo.html")
                    zipOut.putNextEntry(htmlEntry)
                    zipOut.write(htmlContent.toByteArray())
                    zipOut.closeEntry()

                    // 导出图片附件
                    val createdDirs = mutableSetOf<String>()
                    list.forEach { noteShowBean ->
                        val dateDirName = SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.ENGLISH
                        ).format(Date(noteShowBean.note.createTime))
                        noteShowBean.note.attachments.forEach { attachment ->
                            val file = File(attachment.path)
                            if (file.exists()) {
                                val noteIdStr = noteShowBean.note.noteId.toString()
                                val dirPaths = listOf(
                                    "file/",
                                    "file/$dateDirName/",
                                    "file/$dateDirName/$noteIdStr/"
                                )
                                for (dirPath in dirPaths) {
                                    if (createdDirs.add(dirPath)) {
                                        val dirEntry = ZipEntry(dirPath)
                                        zipOut.putNextEntry(dirEntry)
                                        zipOut.closeEntry() // 目录不需要写入数据， 直接close
                                    }
                                }

                                val entryPath = "file/$dateDirName/$noteIdStr/${file.name}"
                                val entry = ZipEntry(entryPath)
                                zipOut.putNextEntry(entry)
                                FileInputStream(file).use { inputStream ->
                                    inputStream.copyTo(zipOut)
                                }
                                zipOut.closeEntry()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun generateHtml(list: List<NoteShowBean>): String {
        val userName = "@Lucky"
        val exportDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())
        val count = list.size

        val dateOptions = list.map {
            SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(Date(it.note.createTime))
        }.distinct().sortedDescending().joinToString("") {
            "<option value=\"$it\">$it</option>"
        }

        // 生成下拉菜单的标签，同时过滤掉空标签
        val tags = list.flatMap { it.tagList }
            .map { it.tag }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .joinToString("") {
                "<option value=\"$it\">$it</option>"
            }

        val memosHtml = list.joinToString("") { noteShowBean ->
            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date(noteShowBean.note.createTime))
            val dateMonth = SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(Date(noteShowBean.note.createTime))
            val dateDirName = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date(noteShowBean.note.createTime))
            val tagRegex = Regex("""(#[\u4e00-\u9fa5a-zA-Z]+\d{0,100})""")
            // 正文段落 (行内的 #标签 已经天然包含在这里了)
            val contentHtml = noteShowBean.note.content.split("\n").joinToString("") { line ->
                val highlightedLine = line.replace(tagRegex, "<span class=\"tag-highlight\">$1</span>")
                "<p>$highlightedLine</p>"
            }

            // 提取有效标签，仅用于注入到 data-tags 属性中供 JavaScript 筛选使用，不在页面上显示
            val filterTagsStr = noteShowBean.tagList.joinToString(" ") { "#${it.tag}" }

            // 图片附件【这个方法手机浏览器无法显示图片】
//            val filesHtml = noteShowBean.note.attachments.filter { it.type == Attachment.Type.IMAGE }
//                .joinToString("") { attachment ->
//                    val fileName = File(attachment.path).name
//                    "<img src=\"file/$dateDirName/${noteShowBean.note.noteId}/$fileName\" alt=\"memo image\" loading=\"lazy\" />"
//                }
            // 图片附件 (Base64 + 自动压缩方案，专治手机端打不开)
//            val filesHtml = noteShowBean.note.attachments.filter { it.type == Attachment.Type.IMAGE }
//                .joinToString("") { attachment ->
//                    try {
//                        val file = File(attachment.path)
//                        if (file.exists()) {
//                            // 把图片从物理文件变成一段极小的Base64文本
//                            // 1. 获取图片原始宽高，但不把整张图片加载进内存（防止导出时 OOM）
//                            val options = BitmapFactory.Options()
//                            options.inJustDecodeBounds = true
//                            BitmapFactory.decodeFile(file.absolutePath, options)
//                            // 2. 计算缩放比例，假设我们限制网页图片最大宽度为 1080 像素
//                            // inSampleSize: 缩放倍数
//                            var inSampleSize = 1
//                            val reqWidth = 1080
//                            val reqHeight = 1080
//                            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
//                                val halfHeight = options.outHeight / 2
//                                val halfWidth = options.outWidth / 2
//                                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
//                                    inSampleSize *= 2
//                                }
//                            }
//                            // 3. 按照计算好的比例，真正把图片加载进内存
//                            options.inJustDecodeBounds = false
//                            options.inSampleSize = inSampleSize
//                            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
//                            // 4. 将 Bitmap 压缩为 JPEG 格式，画质设定为 70%（网页显示足够了，体积大幅减小）
//                            val outputStream = ByteArrayOutputStream()
//                            bitmap?.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
//                            val bytes = outputStream.toByteArray()
//                            // 5. 释放 Bitmap 内存
//                            bitmap?.recycle()
//                            // 6. 转成 Base64
//                            val base64String = encodeToString(bytes, Base64.NO_WRAP)
//                            val filePath = "file/$dateDirName/${noteShowBean.note.noteId}/${file.name}"
//                            // 生成标签 (因为前面压缩成了 JPEG，所以 mimeType 统一写 image/jpeg)
//                            "<img src=\"data:image/jpeg;base64,$base64String\" data-filepath=\"$filePath\" alt=\"memo " +
//                                    "image\"" +
//                                    " " +
//                                    "loading=\"lazy\" />"
//                        } else {
//                            "<p style='color:red; font-size:12px;'>[图片已丢失]</p>"
//                        }
//                    } catch (e: Exception) {
//                        "<p style='color:red; font-size:12px;'>[图片加载异常: ${e.message}]</p>"
//                    }
//                }
            val attachmentsHtml = noteShowBean.note.attachments.joinToString("") { attachment ->
                try {
                    val file = File(attachment.path)
                    val filePath = "file/$dateDirName/${noteShowBean.note.noteId}/${file.name}"
                    if (file.exists()) {
                        when(attachment.type) {
                            Attachment.Type.IMAGE -> {
                                // ====== 原有的图片处理逻辑 ======
                                val options = BitmapFactory.Options()
                                options.inJustDecodeBounds = true
                                BitmapFactory.decodeFile(file.absolutePath, options)

                                var inSampleSize = 1
                                val reqWidth = 1080
                                val reqHeight = 1080
                                if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                                    val halfHeight = options.outHeight / 2
                                    val halfWidth = options.outWidth / 2
                                    while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                                        inSampleSize *= 2
                                    }
                                }
                                options.inJustDecodeBounds = false
                                options.inSampleSize = inSampleSize
                                val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

                                val outputStream = ByteArrayOutputStream()
                                bitmap?.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                                val bytes = outputStream.toByteArray()
                                bitmap?.recycle()

                                val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                                // 生成标签 (因为前面压缩成了 JPEG，所以 mimeType 统一写 image/jpeg)
                                "<img src=\"data:image/jpeg;base64,$base64String\" data-filepath=\"$filePath\" alt=\"memo " +
                                        "image\"" +
                                        " " +
                                        "loading=\"lazy\" />"
                            }

                            Attachment.Type.AUDIO -> {
                                // 处理音频
                                val bytes = file.readBytes()
                                val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                                """
                                    <div class="audio-player">
                                        <audio controls controlsList="nodownload" preload="metadata" 
                                        data-filepath="$filePath">
                                            <source src="data:audio/mp4;base64,$base64String" type="audio/mp4" >
                                            您的浏览器不支持音频播放。
                                        </audio>
                                    </div>
                                """.trimIndent()
                            }
                            else -> ""
                        }
                    } else {
                        "<p style='color:red; font-size:12px'>[附件已丢失]</p>"
                    }
                } catch (e: Exception) {
                    "<p style='color:red; font-size:12px;'>[附件加载异常: ${e.message}]</p>"
                }
            }
            """
            <div class="memo" data-date="$dateMonth" data-tags="$filterTagsStr">
              <div class="time">$timeStr</div>
              <div class="content">
                $contentHtml
              </div>
              <div class="files">
                $attachmentsHtml
              </div>
            </div>
        """.trimIndent()
        }

        return """
<!DOCTYPE html>
<html lang="zh-CN">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IdeaMemo · 笔记导出</title>
    <style type="text/css">
      :root {
        --bg-color: #f4f5f7;
        --card-bg: #ffffff;
        --text-main: #2d3748;
        --text-muted: #a0aec0;
        --primary: #38b2ac;
        --primary-hover: #319795;
      }

      * { margin: 0; padding: 0; box-sizing: border-box; }
      body {
        background: var(--bg-color);
        font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        color: var(--text-main);
        line-height: 1.6;
        -webkit-font-smoothing: antialiased;
      }

      header { max-width: 680px; margin: 0 auto; padding: 40px 20px 20px; }
      header .top { display: flex; flex-direction: column; gap: 20px; }
      @media (min-width: 600px) {
        header .top { flex-direction: row; justify-content: space-between; align-items: flex-end; }
      }

      header .user .name { font-size: 24px; font-weight: 700; letter-spacing: -0.5px; }
      header .user .date { font-size: 14px; color: var(--text-muted); margin-top: 4px; }

      header .filter { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
      /* 使用传统的 margin-right 来制造右侧间距（兼容所有浏览器） */
      header .filter select {
        margin-right: 16px;
      }

      /* 针对手机小屏幕的适配：如果屏幕太窄导致按钮被挤到下一行，给它们加一点底部间距 */
      @media (max-width: 600px) {
        header .filter select,
        header .filter button {
          margin-bottom: 12px;
        }
      }
      select {
        -webkit-appearance: none;
        -moz-appearance: none;
        appearance: none;
        background-color: #fff;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        padding: 8px 36px 8px 16px;
        font-size: 14px;
        color: var(--text-main);
        cursor: pointer;
        outline: none;
        box-shadow: 0 1px 2px rgba(0,0,0,0.05);
        background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23a0aec0' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
        background-repeat: no-repeat;
        background-position: right 10px center;
        background-size: 16px;
        transition: all 0.2s;
      }
      select::-ms-expand {
        display: none;
      }
      select:hover { border-color: #cbd5e0; }
      select:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(56, 178, 172, 0.2); }

      button.filter-btn {
        padding: 8px 20px;
        background: var(--primary);
        border-radius: 8px;
        color: #fff;
        font-size: 14px;
        font-weight: 500;
        border: none;
        cursor: pointer;
        transition: background 0.2s;
        box-shadow: 0 2px 4px rgba(56, 178, 172, 0.2);
      }
      button.filter-btn:hover { background: var(--primary-hover); }

      .memos { max-width: 680px; margin: 0 auto; padding: 0 20px 60px; }

      .memo {
        background: var(--card-bg);
        padding: 24px;
        border-radius: 16px;
        margin-bottom: 24px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.02), 0 1px 3px rgba(0, 0, 0, 0.04);
        transition: transform 0.2s ease, box-shadow 0.2s ease;
        word-wrap: break-word;
      }
      .memo:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 15px rgba(0, 0, 0, 0.04), 0 4px 6px rgba(0, 0, 0, 0.02);
      }

      .memo .time { color: var(--text-muted); font-size: 13px; font-weight: 500; margin-bottom: 12px; }
      .memo .content { color: var(--text-main); font-size: 15px; }
      .memo .content p { min-height: 24px; margin-bottom: 8px; }
      .tag-highlight {
        color: var(--primary-hover);
        background: var(--tag-bg);
        padding: 2px 6px;
        border-radius: 6px;
        font-weight: 600;
        margin: 0 2px;
        font-size: 0.95em;
        text-decoration: none;
        display: inline-block;
        line-height: 1.2;
      }
      .memo .content p:last-child { margin-bottom: 0; }
      .memo .content ul, .memo .content ol { padding-inline-start: 24px; margin: 8px 0; }

      .memo .files img {
        max-width: 100%;
        border: 1px solid #edf2f7;
        border-radius: 8px;
        margin-top: 16px;
        display: block;
      }
      .memo .files .audio-player {
        margin-top: 16px;
        width: 100%;
      }
      .memo .files audio {
        width: 100%;
        outline: none;
        border-radius: 22px;
        background-color: #f8fafc;
      }
      audio::-webkit-media-controls-enclosure {
        border-radius: 22px;
      }
      audio::-webkit-media-controls-panel {
        background-color: #f8fafc;
      }
    </style>
  </head>
  <body>
    <header>
      <div class="top">
        <div class="user">
          <div class="name">$userName</div>
          <div class="date">于 $exportDate 导出 $count 条 MEMO</div>
        </div>
        <div class="filter">
          <select id="date">
            <option value="">全部年月</option>
            $dateOptions
          </select>
          <select id="tag">
            <option value="">全部标签</option>
            $tags
          </select>
          <button class="filter-btn">筛选</button>
        </div>
      </div>
    </header>

    <div class="memos">
      $memosHtml
    </div>

    <script>
      const filterBtn = document.querySelector(".filter-btn");

      filterBtn.addEventListener("click", () => {
        const dateVal = document.querySelector("#date").value;
        const tagVal = document.querySelector("#tag").value;
        const memos = document.querySelectorAll(".memo");

        memos.forEach((memo) => {
          const memoDate = memo.getAttribute("data-date") || "";
          const memoTags = memo.getAttribute("data-tags") || "";
          const contentText = memo.querySelector(".content").innerText;

          const isDateMatch = !dateVal || memoDate === dateVal;
          // 模糊匹配：无论标签是在 data-tags 里，还是直接写在了正文 contentText 里，都能搜出来
          const isTagMatch = !tagVal || memoTags.includes(tagVal) || contentText.includes(tagVal);

          if (isDateMatch && isTagMatch) {
            memo.style.display = "block";
            memo.style.opacity = "0";
            setTimeout(() => memo.style.opacity = "1", 50);
            memo.style.transition = "opacity 0.3s ease";
          } else {
            memo.style.display = "none";
          }
        });
      });
    </script>
  </body>
</html>
    """.trimIndent()
    }



    fun findFirstHtmlFileAndPrintAll(dir: File): File? {
        if (!dir.exists() || !dir.isDirectory) {
            Log.i("BackUp","目录不存在或不是文件夹: ${dir.absolutePath}")
            return null
        }
        var result: File? = null

        fun dfs(current: File) {
            if (result != null) return
            Log.i("BackUp","访问: ${current.absolutePath}")
            if (current.isFile) {
                if (current.name.endsWith(".html", ignoreCase = true)) {
                    result = current
                    return
                }
            } else if (current.isDirectory) {
                current.listFiles()?.forEach { file ->
                    dfs(file)
                    if (result != null) return
                }
            }
        }
        dfs(dir)
        if (result != null) {
            Log.i("BackUp","找到HTML文件: ${result!!.absolutePath}")
        } else {
            Log.i("BackUp","未找到HTML文件")
        }
        return result
    }
}