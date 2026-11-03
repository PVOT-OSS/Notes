// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.list

import org.junit.Assert.assertEquals
import org.junit.Test

class ListControlsTest {
    @Test
    fun keepsSelectionsThatStillExist() {
        val controls = ListControls(collection = 2, category = "work")
        assertEquals(controls, controls.dropMissing(setOf(1, 2), setOf("work", "home")))
    }

    @Test
    fun dropsADeletedCollectionAndAVanishedTag() {
        val controls = ListControls(collection = 2, category = "work", query = "eggs")
        assertEquals(
            ListControls(collection = null, category = null, query = "eggs"),
            controls.dropMissing(setOf(1), setOf("home")),
        )
    }
}
