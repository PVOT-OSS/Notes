// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes

import android.content.Context
import org.prauga.notes.list.NoteLayout

class NotesPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("notes", Context.MODE_PRIVATE)

    var layout: NoteLayout
        get() =
            prefs.getString(KEY_LAYOUT, null)?.let {
                runCatching { NoteLayout.valueOf(it) }.getOrNull()
            } ?: NoteLayout.GRID
        set(value) = prefs.edit().putString(KEY_LAYOUT, value.name).apply()

    private companion object {
        const val KEY_LAYOUT = "layout"
    }
}
