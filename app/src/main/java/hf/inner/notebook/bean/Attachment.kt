package hf.inner.notebook.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Attachment(
    val type: Type = Type.IMAGE,
    val path: String = "",
    val description: String = "",
    val fileName: String = ""
) : Parcelable {
    enum class Type { AUDIO, IMAGE, VIDEO, GENERIC, FILE }

    fun isMedia(): Boolean {
        return type == Type.IMAGE || type == Type.VIDEO
    }

    fun isEmpty() = path.isEmpty() && description.isEmpty() && fileName.isEmpty()
}
