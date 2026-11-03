// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.core.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

sealed interface EntryTime {
    data class Date(val date: LocalDate) : EntryTime

    data class DateTime(val dateTime: LocalDateTime, val zone: ZoneId?) : EntryTime
}
