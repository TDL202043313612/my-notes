package hf.inner.notebook.state

import hf.inner.notebook.bean.NoteShowBean

data class NoteState (
    val notes: List<NoteShowBean> = emptyList(),
    val title: String = "",
    val content: String = "",
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val editingNote: NoteShowBean? = null,
)