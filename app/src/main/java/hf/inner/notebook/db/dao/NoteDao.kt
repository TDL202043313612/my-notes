package hf.inner.notebook.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import hf.inner.notebook.bean.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(list: List<Note>)

    @Delete
    fun delete(note: Note)

    @Update
    fun update(note: Note)

    @Transaction
    @Query("SELECT * FROM Note order by is_pinned desc, update_time desc")
    fun queryAll(): Flow<List<Note>>

    @Transaction
    @Query("SELECT * FROM Note order by is_pinned desc, update_time desc")
    fun queryAllData(): List<Note>

    @Query("select * from Note where note_id =:id")
    fun queryById(id: Int): Note

    @Query("select count(*) from Note")
    fun getCount(): Int

    @Query("delete from Note")
    fun deleteAll()

    @Query("UPDATE Note SET is_pinned = :isPinned WHERE note_id = :noteId")
    suspend fun updatePinStatus(noteId: Long, isPinned: Boolean)
}