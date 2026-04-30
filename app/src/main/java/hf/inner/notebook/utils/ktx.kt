package hf.inner.notebook.utils

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import hf.inner.notebook.App
import hf.inner.notebook.R
import hf.inner.notebook.bean.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("Range")
fun Cursor.getStringValue(key: String): String = getString(getColumnIndex(key)) ?: ""
val Int.str: String
    get() = App.instance.getString(this)

fun toast(text: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(App.instance, text, Toast.LENGTH_SHORT).show()
    }
}
fun Date.formatName(): String {
    return SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH).format(this)
}

val backUpFileName:String
    get() = "Idea" + Date().formatName() + ".zip"

fun copy(note: Note) {
    val text = if (note.noteTitle.isNullOrEmpty()) {
        note.content
    } else {
        (note.noteTitle ?: "") + "\n\n" + note.content
    }

    // 获取剪贴板管理器
    val cm: ClipboardManager = App.instance.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(null, text))
    toast(R.string.execute_success.str)
}
/**
 * 用于处理宽屏设备布局
 */
fun isWideScreen(context: Context): Boolean {
    val configuration = context.resources.configuration
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    return screenWidthDp > screenHeightDp
}

fun Context.isSystemLanguageEnglish(): Boolean {
    val systemLanguage = Locale.getDefault().language
    return systemLanguage.startsWith("en")
}