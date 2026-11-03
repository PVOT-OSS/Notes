// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.notes.core.data.CollectionCount
import org.prauga.notes.core.data.EntryRepository
import org.prauga.notes.core.model.Component
import org.prauga.notes.core.model.EntryCollection

class CollectionsViewModel(private val repository: EntryRepository) : ViewModel() {
    val collections: StateFlow<List<CollectionCount>> =
        repository
            .observeCollectionCounts(Component.VJOURNAL)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(id: Long, name: String, color: Int?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.saveCollection(EntryCollection(id = id, name = trimmed, color = color))
        }
    }

    fun delete(id: Long) {
        if (collections.value.size <= 1) return
        viewModelScope.launch { repository.deleteCollection(id) }
    }
}
