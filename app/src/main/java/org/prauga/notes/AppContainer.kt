// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.prauga.notes.core.data.EntryDatabase
import org.prauga.notes.core.data.EntryRepository

class AppContainer(context: Context) {
    val applicationScope = CoroutineScope(SupervisorJob())
    val preferences = NotesPreferences(context)
    val defaultCollectionName: String = context.getString(R.string.default_collection)
    val repository: EntryRepository by lazy {
        EntryRepository(EntryDatabase.create(context, "notes.db"))
    }
}
