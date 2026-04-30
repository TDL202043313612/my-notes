package hf.inner.notebook.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import hf.inner.notebook.bean.Note
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.bean.NoteTagCrossRef
import hf.inner.notebook.bean.Reminder
import hf.inner.notebook.bean.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagNoteDao {

    @RawQuery(
        observedEntities = [
            Note::class,
            Tag::class,
            Reminder::class,
            NoteTagCrossRef::class
        ]
    )
    fun rawGetQueryFlow(query: SimpleSQLiteQuery): Flow<List<NoteShowBean>>

    fun getAll(sortTime: String, order: String): Flow<List<NoteShowBean>> {
        // @Query不能动态替换列名和ASC/DESC
        return rawGetQueryFlow(
            SimpleSQLiteQuery("SELECT * FROM Note order by $sortTime $order")
        )
    }
    @Transaction
    @Query("SELECT * FROM Note WHERE create_time BETWEEN :startTime AND :endTime AND is_deleted = 0 ORDER BY " +
            "create_time DESC")
    fun getNotesByCreateTimeRange(startTime: Long, endTime: Long): Flow<List<NoteShowBean>>

    @Query("SELECT DISTINCT strftime('%Y', datetime(create_time/1000, 'unixepoch')) AS year FROM Note ORDER BY year " +
            "DESC")
    suspend fun getAllDistinctYears(): List<String>

    @Transaction
    @Query("SELECT * FROM Note WHERE strftime('%Y', datetime(create_time/1000, 'unixepoch')) = :year")
    fun getNotesByYear(year: String): Flow<List<NoteShowBean>>

    @Transaction
    @Query("""
        SELECT * FROM Note 
        INNER JOIN NoteTagCrossRef ON Note.note_id = NoteTagCrossRef.note_id
        WHERE NoteTagCrossRef.tag = :tagName
        ORDER BY Note.update_time DESC
    """)
    fun getNoteListWithByTag(tagName: String): Flow<List<NoteShowBean>>

    @Transaction
    @Query("SELECT * FROM Note order by update_time desc")
    fun getAllNoteWithTagList(): List<NoteShowBean>
    @Transaction
    @Query("SELECT * FROM Note WHERE note_id = :noteId")
    fun getNoteShowBeanById(noteId: Long): NoteShowBean?

    @Transaction
    @Query("SELECT * FROM Note WHERE note_id = :noteId")
    fun getNoteShowBeanByIdFlow(noteId: Long): Flow<NoteShowBean?>

    @Transaction
    @Query("SELECT * FROM Note WHERE date(create_time/1000, 'unixepoch') = :selectedDate")
    fun getNoteShowOnDate(selectedDate: String): List<NoteShowBean>

    @Query("SELECT COUNT(*) FROM Note nt JOIN NoteTagCrossRef ncr on nt.note_id=ncr.note_id WHERE ncr.tag=:tagName")
    fun countNoteListWithByTag(tagName: String): Int

    @Transaction
    @Query("SELECT * FROM Note WHERE parent_note_id = :parentNoteId ORDER BY create_time DESC")
    fun getCommentsByParentId(parentNoteId: Long): Flow<List<NoteShowBean>>
}