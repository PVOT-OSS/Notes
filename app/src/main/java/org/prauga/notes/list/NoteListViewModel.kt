// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.prauga.notes.core.data.EntryFilter
import org.prauga.notes.core.data.EntryRepository
import org.prauga.notes.core.model.Component
import org.prauga.notes.core.model.Entry

enum class NoteLayout {
    GRID,
    LIST,
}

data class NoteListUiState(
    val notes: List<Entry> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val query: String = "",
    val searching: Boolean = false,
    val layout: NoteLayout = NoteLayout.GRID,
    val loading: Boolean = true,
)

private data class ListControls(
    val query: String = "",
    val searching: Boolean = false,
    val category: String? = null,
    val layout: NoteLayout = NoteLayout.GRID,
)

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModel(private val repository: EntryRepository) : ViewModel() {
    private val controls = MutableStateFlow(ListControls())

    val uiState: StateFlow<NoteListUiState> =
        combine(
                controls,
                controls.flatMapLatest {
                    repository.observeEntries(
                        Component.VJOURNAL,
                        EntryFilter(category = it.category, query = it.query),
                    )
                },
                repository.observeCategories(Component.VJOURNAL),
            ) { controls, notes, categories ->
                NoteListUiState(
                    notes = notes,
                    categories = categories.map { it.name },
                    selectedCategory = controls.category,
                    query = controls.query,
                    searching = controls.searching,
                    layout = controls.layout,
                    loading = false,
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteListUiState())

    fun setQuery(query: String) = controls.update { it.copy(query = query) }

    fun setSearching(searching: Boolean) =
        controls.update { it.copy(searching = searching, query = if (searching) it.query else "") }

    fun selectCategory(category: String?) = controls.update { it.copy(category = category) }

    fun toggleLayout() =
        controls.update {
            it.copy(layout = if (it.layout == NoteLayout.GRID) NoteLayout.LIST else NoteLayout.GRID)
        }
}
