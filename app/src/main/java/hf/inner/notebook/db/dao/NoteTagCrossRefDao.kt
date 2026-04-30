package hf.inner.notebook.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import hf.inner.notebook.bean.NoteTagCrossRef

@Dao
interface NoteTagCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNoteTagCrossRef(entity: NoteTagCrossRef)

    @Delete
    fun deleteCrossRef(entity: NoteTagCrossRef)
}