package hf.inner.notebook.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hf.inner.notebook.bean.Comment
import hf.inner.notebook.bean.Note
import hf.inner.notebook.bean.NoteTagCrossRef
import hf.inner.notebook.bean.Reminder
import hf.inner.notebook.bean.Tag
import hf.inner.notebook.db.dao.NoteDao
import hf.inner.notebook.db.dao.NoteTagCrossRefDao
import hf.inner.notebook.db.dao.TagDao
import hf.inner.notebook.db.dao.TagNoteDao

@Database(
    entities = [
        Note::class, Tag::class, NoteTagCrossRef::class, Comment::class, Reminder::class
    ], version = 3, exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao
    abstract fun getTagDao(): TagDao

    abstract fun getTagNoteDao(): TagNoteDao
    abstract fun getNoteTagCrossRefDao(): NoteTagCrossRefDao
}