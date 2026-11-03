// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.core.model

enum class Classification(val value: String) {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE"),
    CONFIDENTIAL("CONFIDENTIAL");

    companion object {
        fun fromValue(value: String): Classification? =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
    }
}
