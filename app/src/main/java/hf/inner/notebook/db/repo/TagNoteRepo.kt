package hf.inner.notebook.db.repo

import hf.inner.notebook.bean.Note
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.bean.NoteTagCrossRef
import hf.inner.notebook.bean.Tag
import hf.inner.notebook.db.dao.NoteDao
import hf.inner.notebook.db.dao.NoteTagCrossRefDao
import hf.inner.notebook.db.dao.TagDao
import hf.inner.notebook.db.dao.TagNoteDao
import hf.inner.notebook.page.viewmodel.SortTime
import hf.inner.notebook.utils.TopicUtils
import kotlinx.coroutines.flow.Flow

class TagNoteRepo(
    private val noteDao: NoteDao,
    private val tagNoteDao: TagNoteDao,
    private val tagDao: TagDao,
    private val noteTagCrossRef: NoteTagCrossRefDao
){
    fun updateTag(tag: Tag) {
        tagDao.update(tag)
    }
    fun queryAllNoteList(): List<Note> {
        return noteDao.queryAllData()
    }
    fun queryAllTagList(): List<Tag> {
        return tagDao.queryAllTagList().filterNot { it.tag.isBlank() }
    }

    fun deleteNote(card: Note, tags: List<Tag>) {
        noteDao.delete(card)
        tags.forEach {
            tagDao.deleteOrUpdate(it)
            noteTagCrossRef.deleteCrossRef(NoteTagCrossRef(noteId = card.noteId, tag = it.tag))
        }
    }

    suspend fun updatePinStatus(noteId: Long, isPinned: Boolean) {
        noteDao.updatePinStatus(noteId, isPinned)
    }
    fun insertOrUpdate(card: Note) {
        val tagList = TopicUtils.getTopicListByString(card.content)
        val noteId = noteDao.insert(card)
        if (tagList.isEmpty()) {
            val tempTag = Tag(tag = "")
            tagDao.insertOrUpdate(tempTag)
            noteTagCrossRef.insertNoteTagCrossRef(NoteTagCrossRef(noteId = noteId, tag = tempTag.tag))
            return
        }
        tagList.forEach { tag ->
            tagDao.insertOrUpdate(tag)
            noteTagCrossRef.insertNoteTagCrossRef(NoteTagCrossRef(noteId = noteId, tag = tag.tag))
        }
    }

    fun queryAllMemosFlow(sortTime: String): Flow<List<NoteShowBean>> {
        return when(sortTime) {
            SortTime.UPDATE_TIME_DESC.name -> {
                tagNoteDao.getAll("update_time", "desc")
            }
            SortTime.UPDATE_TIME_ASC.name -> {
                tagNoteDao.getAll("update_time", "asc")
            }
            SortTime.CREATE_TIME_DESC.name -> {
                tagNoteDao.getAll("create_time", "desc")
            }
            SortTime.CREATE_TIME_ASC.name -> {
                tagNoteDao.getAll("create_time", "asc")
            }
            else -> {
                tagNoteDao.getAll("update_time", "desc")
            }
        }
    }

    fun queryAllTagFlow(): Flow<List<Tag>> = tagDao.queryAll()

    fun getNotesByCreateTimeRange(startTime: Long, endTime: Long): Flow<List<NoteShowBean>> = tagNoteDao
        .getNotesByCreateTimeRange(startTime, endTime)

    suspend fun getAllDistinctYears(): List<String> {
        return tagNoteDao.getAllDistinctYears()
    }

    fun getNoteListWithByTag(tagName: String): Flow<List<NoteShowBean>> {
        return tagNoteDao.getNoteListWithByTag(tagName)
    }

    fun getNotesByYear(year: String): Flow<List<NoteShowBean>> = tagNoteDao.getNotesByYear(year)

    fun queryAllNoteShowBeanList(): List<NoteShowBean> = tagNoteDao.getAllNoteWithTagList()

    fun getNoteShowBeanById(noteId: Long): NoteShowBean? = tagNoteDao.getNoteShowBeanById(noteId)

    fun getNoteShowBeanByIdFlow(noteId: Long): Flow<NoteShowBean?> = tagNoteDao.getNoteShowBeanByIdFlow(noteId)

    fun getNotesOnSelectedDate(selectedDate: String): List<NoteShowBean> = tagNoteDao.getNoteShowOnDate(selectedDate)

    fun countNoteListWithByTag(tagName: String): Int {
        return tagNoteDao.countNoteListWithByTag(tagName)
    }

    fun getCommentsByParentId(parentNoteId: Long): Flow<List<NoteShowBean>> = tagNoteDao.getCommentsByParentId(parentNoteId)
}