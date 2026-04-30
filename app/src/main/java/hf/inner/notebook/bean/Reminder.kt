package hf.inner.notebook.bean

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = arrayOf("note_id"),
            childColumns = arrayOf("noteId"),
            onDelete = ForeignKey.CASCADE,
            ),
    ]
)
@Serializable
@Parcelize
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(index = true) val noteId: Long,
    val name: String,
    val date: Long,
) : Parcelable {
}
