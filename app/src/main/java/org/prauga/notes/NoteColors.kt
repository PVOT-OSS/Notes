// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

enum class NoteColor(val argb: Int, @StringRes val label: Int) {
    RED(0xFFEF5350.toInt(), R.string.color_red),
    ORANGE(0xFFFFA726.toInt(), R.string.color_orange),
    YELLOW(0xFFFFEE58.toInt(), R.string.color_yellow),
    GREEN(0xFF66BB6A.toInt(), R.string.color_green),
    TEAL(0xFF26A69A.toInt(), R.string.color_teal),
    BLUE(0xFF42A5F5.toInt(), R.string.color_blue),
    PURPLE(0xFFAB47BC.toInt(), R.string.color_purple),
    PINK(0xFFEC407A.toInt(), R.string.color_pink),
}

@Composable
fun noteContainerColor(argb: Int): Color =
    Color(argb).copy(alpha = 0.3f).compositeOver(MaterialTheme.colorScheme.surface)

@Composable
fun NoteColorRow(selected: Int?, onSelect: (Int?) -> Unit, modifier: Modifier = Modifier) {
    val outline = MaterialTheme.colorScheme.outline
    val ring = MaterialTheme.colorScheme.onSurface
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            val label = stringResource(R.string.color_none)
            Swatch(selected == null, ring, label, { onSelect(null) }) {
                Box(
                    Modifier.size(32.dp).border(BorderStroke(1.dp, outline), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        null,
                        Modifier.size(16.dp),
                        tint = outline,
                    )
                }
            }
        }
        items(NoteColor.entries) { color ->
            val label = stringResource(color.label)
            Swatch(selected == color.argb, ring, label, { onSelect(color.argb) }) {
                Box(Modifier.size(32.dp).background(Color(color.argb), CircleShape))
            }
        }
    }
}

@Composable
private fun Swatch(
    selected: Boolean,
    ring: Color,
    label: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier.size(40.dp)
                .border(BorderStroke(2.dp, if (selected) ring else Color.Transparent), CircleShape)
                .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
                .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
