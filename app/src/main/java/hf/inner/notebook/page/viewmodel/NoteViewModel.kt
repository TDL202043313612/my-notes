package hf.inner.notebook.page.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hf.inner.notebook.App
import hf.inner.notebook.bean.Note
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.bean.Tag
import hf.inner.notebook.db.repo.TagNoteRepo
import hf.inner.notebook.page.settings.Level
import hf.inner.notebook.state.NoteState
import hf.inner.notebook.utils.AudioPlayer
import hf.inner.notebook.utils.SharedPreferencesUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class SortTime {
    UPDATE_TIME_DESC, UPDATE_TIME_ASC, CREATE_TIME_DESC, CREATE_TIME_ASC
}
@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val tagNoteRepo: TagNoteRepo,
    private val audioPlayer: AudioPlayer
) : ViewModel() {
    private var progressJob: Job? = null
    var currentlyPlayingPath by mutableStateOf<String?>(null)
        private set
    var currentAudioDuration by mutableStateOf(0)
        private set
    var currentAudioProgress by mutableStateOf(0)
        private set
    var selectedDate by mutableStateOf(LocalDate.now())

    val sortTime = SharedPreferencesUtils.sortTime

    private val _notes: StateFlow<List<NoteShowBean>> = sortTime.flatMapLatest { newSortTime ->
        tagNoteRepo.queryAllMemosFlow(newSortTime.name)
    }.stateIn( // 从冷流(Flow)变为热流(StateFlow)
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )

    private val _state = MutableStateFlow(NoteState())

    @RequiresApi(Build.VERSION_CODES.O)
    val state = combine(_state, _notes) { state, notes ->
        // 收藏的笔记排在前面
        val sortedNotes = notes.sortedWith(
            compareByDescending<NoteShowBean> { it.note.isPinned }
                .thenByDescending { it.note.isCollected }
        )
        val filteredNotes = if (state.searchQuery.isBlank()) {
            sortedNotes
        } else {
            sortedNotes.filter { it.doesMatchSearchQuery(state.searchQuery) }
        }
        getLocalDateMap(notes)
        state.copy(notes = filteredNotes)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        NoteState()
    )

    fun toggleAudio(path: String) {
        if (currentlyPlayingPath == path) {
            audioPlayer.stop()
            currentlyPlayingPath = null
            stopProgressUpdate()
        } else {
            currentlyPlayingPath = path
            audioPlayer.playFile(path) {
                if (currentlyPlayingPath == path) {
                    currentlyPlayingPath = null
                    stopProgressUpdate()
                    currentAudioProgress = 0
                }
            }
            startProgressUpdate()
        }
    }

    fun seekAudio(position: Float) {
        val posInt = position.toInt()
        currentAudioProgress = posInt
        audioPlayer.seekTo(posInt)
    }
    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            delay(100)
            currentAudioDuration = audioPlayer.getDuration()
            while (isActive && currentlyPlayingPath != null) {
                currentAudioProgress = audioPlayer.getCurrentPosition()
                delay(50)
            }
        }
    }
    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotesOnSelectedDate(selectedDate: LocalDate): List<NoteShowBean> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = selectedDate.format(formatter)
        return tagNoteRepo.getNotesOnSelectedDate(formattedDate)
    }

    fun insertOrUpdate(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            tagNoteRepo.insertOrUpdate(note)
        }
    }

    /**
     * 置顶笔记
     */
    fun updatePinStatus(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            tagNoteRepo.updatePinStatus(note.noteId, !note.isPinned)
        }
    }
    fun getNoteListByTagFlow(tagName: String): Flow<List<NoteShowBean>> = tagNoteRepo.getNoteListWithByTag(tagName)
        suspend fun getAllDistinctYears(): List<String> = withContext(Dispatchers.IO) {
        tagNoteRepo.getAllDistinctYears()
    }
    fun getNoteShowBeanById(noteId: Long): NoteShowBean? = tagNoteRepo.getNoteShowBeanById(noteId)

    fun getNoteShowBeanByIdFlow(noteId: Long): Flow<NoteShowBean?> = tagNoteRepo.getNoteShowBeanByIdFlow(noteId)

    fun getCommentsByParentId(parentNoteId: Long): Flow<List<NoteShowBean>> = tagNoteRepo.getCommentsByParentId(parentNoteId)

    fun getNotesByYear(year: String): Flow<List<NoteShowBean>> = tagNoteRepo.getNotesByYear(year)
    fun getNotesByCreateTimeRange(startTime: Long, endTime: Long): Flow<List<NoteShowBean>> = tagNoteRepo
        .getNotesByCreateTimeRange(startTime, endTime)
    val tags: StateFlow<List<Tag>> = tagNoteRepo.queryAllTagFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        emptyList()
    )
    val levelMemosMap = mutableStateMapOf<LocalDate, Level>()

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getLocalDateMap(notes: List<NoteShowBean>) = withContext(Dispatchers.IO) {
        val sortTime = SharedPreferencesUtils.sortTime.first()
        val map: MutableMap<LocalDate, Int> = mutableMapOf()

        notes.forEach {
            val showTime = if (sortTime == SortTime.UPDATE_TIME_DESC || sortTime == SortTime.UPDATE_TIME_ASC)
                it.note.updateTime else it.note.createTime
            // toLocalDate 2026-04-09
            val localDate = Instant.ofEpochMilli(showTime).atZone(ZoneId.systemDefault()).toLocalDate()
            map[localDate] = map.getOrElse(localDate) { 0 } + 1
        }
        levelMemosMap.clear()
        levelMemosMap.putAll(convertToLevelMap(map))
    }

    private fun convertToLevelMap(inputMap: Map<LocalDate, Int>): Map<LocalDate, Level> {
        // mapValues: key不变，更改value后返回一个新的map
        return inputMap.mapValues { (_, value) ->
            when(value) {
                in 0 until 1 -> Level.Zero
                in 1 until 3 -> Level.One
                in 3 until 5 -> Level.Three
                in 5 until 8 -> Level.Four
                else -> Level.Four
            }
        }
    }

    suspend fun deleteNote(card: Note, tags: List<Tag>) = withContext(Dispatchers.IO) {
        tagNoteRepo.deleteNote(card, tags)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()

        // 退出应用时清理所有的临时拍照缓存
        File(App.instance.cacheDir, "capture_picture").apply {
            if (exists()) {
                deleteRecursively()
            }
        }
    }

}

val LocalMemosViewModel = compositionLocalOf<NoteViewModel> { error("Not Found") }

val LocalMemosState = compositionLocalOf<NoteState> { error("Not Found") }
val LocalTags = compositionLocalOf<List<Tag>> { error("Not Found") }