package hf.inner.notebook.page.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.db.repo.TagNoteRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val tagNoteRepo: TagNoteRepo) : ViewModel() {
    private val _query: MutableStateFlow<String> = MutableStateFlow(value = "")
    val query: StateFlow<String>
        get() = _query
    private var notes: List<NoteShowBean> = emptyList()
    private val _dataFlow: MutableStateFlow<List<NoteShowBean>> = MutableStateFlow(value = emptyList())
    val dataFlow: StateFlow<List<NoteShowBean>>
        get() = _dataFlow

    init {
        viewModelScope.launch(Dispatchers.IO) {
            notes = tagNoteRepo.queryAllNoteShowBeanList()
            if (_query.value.isNotEmpty()) {
                onSearch(_query.value)
            }
        }
    }

    private fun getSearchResults(
        searchKey: String,
    ): List<NoteShowBean> = notes.filter { note ->
        note.note.content.contains(searchKey.trim(), true) ||
                note.note.attachments.any { it.description.contains(searchKey.trim(), true) } ||
                note.tagList.any { it.tag.contains(searchKey.trim(), true) }
    }
    fun onSearch(str: String) {
        _dataFlow.value = getSearchResults(str)
    }
    fun onQuery(query: String) {
        _query.value = query
    }

    fun clearSearchQuery() {
        _query.value = ""
        _dataFlow.value = emptyList()
    }
}