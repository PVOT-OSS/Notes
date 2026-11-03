// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.core.model

enum class Status(val value: String) {
    DRAFT("DRAFT"),
    FINAL("FINAL"),
    CANCELLED("CANCELLED"),
    NEEDS_ACTION("NEEDS-ACTION"),
    IN_PROCESS("IN-PROCESS"),
    COMPLETED("COMPLETED");

    companion object {
        fun fromValue(value: String): Status? =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
    }
}
