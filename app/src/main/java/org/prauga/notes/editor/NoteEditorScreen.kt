// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.editor

import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.designsystem.components.PvotDialog
import com.prauga.pvot.designsystem.components.PvotDialogAction
import com.prauga.pvot.designsystem.components.PvotDialogActionStyle
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import org.prauga.notes.R

@Composable
fun NoteEditorScreen(viewModel: NoteEditorViewModel, onClose: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }
    val shareTitle = stringResource(R.string.share)

    PvotScreenScaffold(
        topBar = {
            PvotTopBar(title = "", onBack = onClose) {
                PvotTopBarAction(
                    icon = painterResource(R.drawable.ic_share),
                    contentDescription = shareTitle,
                    enabled = state.title.isNotBlank() || state.body.isNotBlank(),
                    onClick = {
                        val send =
                            Intent(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, viewModel.shareText())
                        if (state.title.isNotBlank())
                            send.putExtra(Intent.EXTRA_SUBJECT, state.title)
                        context.startActivity(Intent.createChooser(send, shareTitle))
                    },
                )
                PvotTopBarAction(
                    icon = painterResource(R.drawable.ic_delete),
                    contentDescription = stringResource(R.string.delete),
                    onClick = {
                        if (state.saved) confirmDelete = true else viewModel.delete(onClose)
                    },
                )
            }
        }
    ) { padding ->
        if (!state.loading) {
            EditorContent(
                state = state,
                viewModel = viewModel,
                modifier =
                    Modifier.fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding(),
                        )
                        .padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
    }

    PvotDialog(
        visible = confirmDelete,
        onDismissRequest = { confirmDelete = false },
        title = stringResource(R.string.delete_note_title),
        message = stringResource(R.string.delete_note_message),
        destructive = true,
    ) {
        PvotDialogAction(
            label = stringResource(R.string.delete),
            style = PvotDialogActionStyle.Destructive,
            onClick = {
                confirmDelete = false
                viewModel.delete(onClose)
            },
        )
        PvotDialogAction(
            label = stringResource(R.string.cancel),
            onClick = { confirmDelete = false },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditorContent(
    state: NoteEditorUiState,
    viewModel: NoteEditorViewModel,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(modifier = modifier) {
        val titleFocus = remember { FocusRequester() }
        val bodyFocus = remember { FocusRequester() }
        LaunchedEffect(Unit) { if (state.focusTitle) titleFocus.requestFocus() }
        PlainField(
            value = state.title,
            onValueChange = { value ->
                viewModel.setTitle(value.replace("\n", ""))
                if (value.contains('\n')) bodyFocus.requestFocus()
            },
            placeholder = stringResource(R.string.title),
            style = typography.headlineSmall.copy(color = colors.onSurface),
            imeAction = ImeAction.Next,
            onImeAction = { bodyFocus.requestFocus() },
            modifier = Modifier.fillMaxWidth().focusRequester(titleFocus),
        )
        Spacer(Modifier.height(12.dp))
        PlainField(
            value = state.body,
            onValueChange = viewModel::setBody,
            placeholder = stringResource(R.string.note),
            style = typography.bodyLarge.copy(color = colors.onSurface),
            modifier = Modifier.fillMaxWidth().focusRequester(bodyFocus).padding(bottom = 24.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            state.categories.forEach { category ->
                InputChip(
                    selected = false,
                    onClick = { viewModel.removeCategory(category) },
                    label = { Text("#$category") },
                    trailingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.remove_tag, category),
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }
        var newTag by rememberSaveable { mutableStateOf("") }
        PlainField(
            value = newTag,
            onValueChange = { value ->
                if (value.endsWith(',')) {
                    viewModel.addCategory(value.dropLast(1))
                    newTag = ""
                } else {
                    newTag = value
                }
            },
            placeholder = stringResource(R.string.add_tag),
            style = typography.bodyMedium.copy(color = colors.primary),
            singleLine = true,
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Done,
            onImeAction = {
                viewModel.addCategory(newTag)
                newTag = ""
            },
            modifier = Modifier.widthIn(min = 120.dp).padding(vertical = 8.dp),
        )
        state.lastModified?.let {
            Spacer(Modifier.height(8.dp))
            val now = System.currentTimeMillis()
            Text(
                text =
                    if (now - it.toEpochMilli() < DateUtils.MINUTE_IN_MILLIS) {
                        stringResource(R.string.edited_just_now)
                    } else {
                        stringResource(
                            R.string.edited_at,
                            DateUtils.getRelativeTimeSpanString(
                                it.toEpochMilli(),
                                now,
                                DateUtils.MINUTE_IN_MILLIS,
                            ),
                        )
                    },
                style = typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlainField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    style: TextStyle,
    modifier: Modifier = Modifier.fillMaxWidth(),
    singleLine: Boolean = false,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = style,
        singleLine = singleLine,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions =
            KeyboardOptions(
                capitalization = capitalization,
                autoCorrectEnabled = capitalization != KeyboardCapitalization.None,
                imeAction = imeAction,
            ),
        keyboardActions = KeyboardActions(onAny = { onImeAction() }),
        modifier = modifier,
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    style =
                        style.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                )
            }
            inner()
        },
    )
}
