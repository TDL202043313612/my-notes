package hf.inner.notebook.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object Constant {
    val USER_AGREEMENT = "https://www.freeprivacypolicy.com/live/77fb28fd-1c21-4a6c-8c8b-1464c314d629"

    val PRIVACY_POLICY = "https://www.freeprivacypolicy.com/live/07870fcd-c545-4b1c-9490-4d6de2d8bb5c"

    val PHOTO_EXTENSIONS = arrayOf(".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif", ".apng", ".avif", ".gif")

    val VIDE_EXTENSIONS = arrayOf(".mp4", ".mkv", ".webm", ".avi", ".3gp", ".mov", ".m4v", ".3gpp")

    val AUDIO_EXTENSIONS = arrayOf(".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac")

    fun startUserAgreeUrl(context: Context) {
        val uri: Uri = Uri.parse(USER_AGREEMENT)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        context.startActivity(intent)
    }

    fun startPrivacyUrl(context: Context) {
        val uri: Uri = Uri.parse(PRIVACY_POLICY)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent)
    }
}