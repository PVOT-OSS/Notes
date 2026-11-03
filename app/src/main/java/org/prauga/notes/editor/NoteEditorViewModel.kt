// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.prauga.notes.NoteEditor
import org.prauga.notes.core.data.EntryRepository
import org.prauga.notes.core.model.Component
import org.prauga.notes.core.model.Entry

data class NoteEditorUiState(
    val loading: Boolean = true,
    val title: String = "",
    val body: String = "",
    val categories: List<String> = emptyList(),
    val lastModified: Instant? = null,
    val saved: Boolean = false,
    val focusTitle: Boolean = false,
)

@OptIn(FlowPreview::class)
class NoteEditorViewModel(
    private val repository: EntryRepository,
    key: NoteEditor,
    defaultCollectionName: String,
    private val applicationScope: CoroutineScope,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteEditorUiState())
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private val edits = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val saveLock = Mutex()
    private var entry: Entry? = null
    private var deleted = false
    private var revised = false

    init {
        viewModelScope.launch {
            val loaded =
                (if (key.id != 0L) repository.getEntry(key.id) else null)
                    ?: repository
                        .newEntry(
                            Component.VJOURNAL,
                            repository.defaultCollection(defaultCollectionName).id,
                        )
                        .copy(summary = key.title, description = key.text)
            entry = loaded
            _uiState.value =
                NoteEditorUiState(
                    loading = false,
                    title = loaded.summary,
                    body = loaded.description,
                    categories = loaded.categories,
                    lastModified = loaded.lastModified.takeIf { loaded.id != 0L },
                    saved = loaded.id != 0L,
                    focusTitle = loaded.id == 0L && loaded.isBlank(),
                )
            if (loaded.id == 0L && !loaded.isBlank()) edits.emit(Unit)
        }
        viewModelScope.launch { edits.debounce(SAVE_DELAY_MS).collect { save() } }
    }

    fun setTitle(title: String) = edit { it.copy(title = title) }

    fun setBody(body: String) = edit { it.copy(body = body) }

    fun addCategory(category: String) {
        val name = category.trim().removePrefix("#").trim()
        if (name.isEmpty()) return
        edit { state ->
            if (state.categories.contains(name)) state
            else state.copy(categories = state.categories + name)
        }
    }

    fun removeCategory(category: String) = edit { it.copy(categories = it.categories - category) }

    fun shareText(): String =
        with(_uiState.value) { listOf(title, body).filter { it.isNotBlank() }.joinToString("\n\n") }

    fun delete(onDeleted: () -> Unit) {
        deleted = true
        val id = entry?.id ?: 0L
        applicationScope.launch { saveLock.withLock { if (id != 0L) repository.delete(id) } }
        onDeleted()
    }

    override fun onCleared() {
        if (!deleted) applicationScope.launch { save() }
    }

    private fun edit(transform: (NoteEditorUiState) -> NoteEditorUiState) {
        if (_uiState.value.loading) return
        _uiState.update(transform)
        edits.tryEmit(Unit)
    }

    private suspend fun save() =
        saveLock.withLock {
            val base = entry ?: return@withLock
            if (deleted) return@withLock
            val state = _uiState.value
            val updated =
                base.copy(
                    summary = state.title.trim(),
                    description = state.body,
                    categories = state.categories,
                )
            if (updated.id == 0L && updated.isBlank()) return@withLock
            if (updated.id != 0L && updated == base) return@withLock
            val id = repository.save(updated, newRevision = !revised)
            revised = true
            val stored = repository.getEntry(id) ?: return@withLock
            entry = stored
            _uiState.update { it.copy(lastModified = stored.lastModified, saved = true) }
        }

    private fun Entry.isBlank() = summary.isBlank() && description.isBlank() && categories.isEmpty()

    private companion object {
        const val SAVE_DELAY_MS = 500L
    }
}
