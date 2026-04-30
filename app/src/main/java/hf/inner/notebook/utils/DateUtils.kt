package hf.inner.notebook.utils

import androidx.compose.material3.DatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toTime(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH)
    return format.format(dateTime)
}

fun Long.toMM(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("MM", Locale.ENGLISH)
    return format.format(dateTime)
}

fun Long.toYYMMDD(): String {
    val dateTime = Date(this)
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return format.format(dateTime)
}
fun formatTime(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}