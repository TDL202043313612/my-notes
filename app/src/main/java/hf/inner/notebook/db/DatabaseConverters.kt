package hf.inner.notebook.db

import androidx.room.TypeConverter
import hf.inner.notebook.bean.Attachment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DatabaseConverters {
    @TypeConverter
    fun jsonFromAttachments(attachments: List<Attachment>): String = Json.encodeToString(attachments)

    @TypeConverter
    fun attachmentsFromJson(json: String): List<Attachment> = Json.decodeFromString(json)
}