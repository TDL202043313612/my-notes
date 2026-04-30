package hf.inner.notebook.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun File.newName(): String {
    var index = 1
    var candidate: String
    // 去掉后缀的文件名，按控制切开
    val split = nameWithoutExtension.split(" ").toMutableList()
    val last = split.last()
    if ("""^\(\d+\)$""".toRegex().matches(last)) {
        split.removeAt(split.lastIndex)
    }
    val name = split.joinToString(" ")
    while (true) {
        candidate = if (extension.isEmpty()) "$name ($index)" else "$name ($index).$extension"
        if (!File("$parent/$candidate").exists()) {
            return candidate
        }
        index++
    }
}

fun File.newPath(): String {
    return "$parent/" + newName()
}

fun copyFile(context: Context, pathFrom: Uri, pathTo: String) {
    try {
        context.contentResolver.openInputStream(pathFrom)?.use { input ->
            FileOutputStream(pathTo).use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}