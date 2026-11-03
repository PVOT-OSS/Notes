// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.notes.core.ui.markdown

import org.junit.Assert.assertEquals
import org.junit.Test
import org.prauga.notes.core.ui.markdown.MarkdownBlock as B
import org.prauga.notes.core.ui.markdown.MarkdownSpan as S

class MarkdownParserTest {
    @Test
    fun parsesBlocks() {
        val source =
            """
            # Title
            First line
            second line

            - item
              - nested
            1. one
            - [ ] todo
            - [x] done
            > quoted
            > more
            ---
            ```
            code *not italic*
            ```
            """
                .trimIndent()
        assertEquals(
            listOf(
                B.Heading(1, "Title"),
                B.Paragraph("First line\nsecond line"),
                B.Bullet("item", 0),
                B.Bullet("nested", 1),
                B.Numbered("1", "one", 0),
                B.Task(false, "todo", 7, 0),
                B.Task(true, "done", 8, 0),
                B.Quote("quoted\nmore"),
                B.Rule,
                B.Code("code *not italic*"),
            ),
            MarkdownParser.parse(source),
        )
    }

    @Test
    fun unclosedCodeFenceKeepsItsText() {
        assertEquals(listOf(B.Code("a\nb")), MarkdownParser.parse("```\na\nb"))
    }

    @Test
    fun parsesInlineStyles() {
        assertEquals(
            listOf(
                S.Bold(listOf(S.Text("bold"))),
                S.Text(" and "),
                S.Italic(listOf(S.Text("it"))),
                S.Text(" "),
                S.Strike(listOf(S.Text("gone"))),
                S.Text(" "),
                S.Code("x*y"),
                S.Text(" "),
                S.Link("site", "https://example.org"),
            ),
            MarkdownParser.parseInline(
                "**bold** and _it_ ~~gone~~ `x*y` [site](https://example.org)"
            ),
        )
    }

    @Test
    fun leavesLoneMarkersAndSnakeCaseAlone() {
        assertEquals(
            listOf(S.Text("2 * 3 = 6 and snake_case_name")),
            MarkdownParser.parseInline("2 * 3 = 6 and snake_case_name"),
        )
        assertEquals(listOf(S.Text("a *b")), MarkdownParser.parseInline("a *b"))
        assertEquals(listOf(S.Text("*literal*")), MarkdownParser.parseInline("\\*literal\\*"))
    }

    @Test
    fun linksBareUrls() {
        assertEquals(
            listOf(
                S.Text("see "),
                S.Link("https://example.org/a", "https://example.org/a"),
                S.Text("."),
            ),
            MarkdownParser.parseInline("see https://example.org/a."),
        )
    }

    @Test
    fun togglesTasks() {
        val source = "list\n- [ ] milk\n  * [x] eggs"
        assertEquals("list\n- [x] milk\n  * [x] eggs", MarkdownParser.toggleTask(source, 1))
        assertEquals("list\n- [ ] milk\n  * [ ] eggs", MarkdownParser.toggleTask(source, 2))
        assertEquals(source, MarkdownParser.toggleTask(source, 0))
        assertEquals(source, MarkdownParser.toggleTask(source, 9))
    }
}
