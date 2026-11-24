package com.kirakit.gallery.textarea

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kirakit.components.textarea.KiraTextarea
import com.kirakit.components.textarea.KiraTextareaSize
import com.kirakit.components.textarea.KiraTextareaSubmitOn
import com.kirakit.components.textarea.KiraTextareaValidationMode
import com.kirakit.gallery.ExampleNote
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@Composable
internal fun TextareaPlayground() {
    val gap = KiraTheme.spacing.md
    val colors = KiraTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic & Sizes")
        GalleryExample("Basic Textarea") {
            var basic by remember { mutableStateOf("") }
            KiraTextarea(
                value = basic,
                onValueChange = { basic = it },
                placeholder = "Type your message here...",
            )
        }

        GalleryExample("Size Variants (Sm, Default, Lg, Xl)") {
            var sm by remember { mutableStateOf("") }
            var def by remember { mutableStateOf("") }
            var lg by remember { mutableStateOf("") }
            var xl by remember { mutableStateOf("") }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraTextarea(value = sm, onValueChange = { sm = it }, label = "Small", size = KiraTextareaSize.Sm, placeholder = "Small textarea...")
                KiraTextarea(value = def, onValueChange = { def = it }, label = "Default", size = KiraTextareaSize.Default, placeholder = "Default textarea...")
                KiraTextarea(value = lg, onValueChange = { lg = it }, label = "Large", size = KiraTextareaSize.Lg, placeholder = "Large textarea...")
                KiraTextarea(value = xl, onValueChange = { xl = it }, label = "Extra Large", size = KiraTextareaSize.Xl, placeholder = "Extra large textarea...")
            }
        }

        SectionHeading("Character Counter & Auto-Size")
        GalleryExample("Current/Max & Remaining Modes") {
            var counterText by remember { mutableStateOf("") }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                KiraTextarea(
                    value = counterText,
                    onValueChange = { counterText = it },
                    label = "Show Current/Max (soft limit — warns at 80%, shakes over 200)",
                    maxLength = 200,
                    showCount = true,
                    warningThreshold = 0.8f,
                    placeholder = "Type something...",
                )
                KiraTextarea(
                    value = counterText,
                    onValueChange = { counterText = it },
                    label = "Show Remaining",
                    maxLength = 200,
                    showCount = true,
                    showRemaining = true,
                    warningThreshold = 0.8f,
                    placeholder = "Type something...",
                )
            }
        }

        GalleryExample("Auto-Size, Fixed Height & Min/Max Rows") {
            var autoVal by remember { mutableStateOf("") }
            var fixedVal by remember { mutableStateOf("") }
            var boundedVal by remember { mutableStateOf("") }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraTextarea(
                    value = autoVal,
                    onValueChange = { autoVal = it },
                    label = "Auto-size (default)",
                    autoSize = true,
                    placeholder = "This textarea auto-sizes based on content...",
                )
                KiraTextarea(
                    value = fixedVal,
                    onValueChange = { fixedVal = it },
                    label = "Fixed height (4 rows)",
                    autoSize = false,
                    minRows = 4,
                    maxRows = 4,
                    placeholder = "This textarea has a fixed height...",
                )
                KiraTextarea(
                    value = boundedVal,
                    onValueChange = { boundedVal = it },
                    label = "With min/max rows (2–6)",
                    minRows = 2,
                    maxRows = 6,
                    placeholder = "Min 2 rows, max 6 rows...",
                )
            }
        }

        SectionHeading("KiraTextarea: Submit, Clear & Validation")
        GalleryExample("Comment Composer (Cmd/Ctrl+Enter, Clearable & Counter)") {
            var comment by remember { mutableStateOf("") }
            var chatText by remember { mutableStateOf("") }
            var messages by remember { mutableStateOf<List<String>>(emptyList()) }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraTextarea(
                    value = comment,
                    onValueChange = { comment = it },
                    label = "Comment",
                    description = "Press Cmd/Ctrl + Enter to submit.",
                    maxLength = 280,
                    showCount = true,
                    clearable = true,
                    submitOn = KiraTextareaSubmitOn.ModEnter,
                    onSubmit = { text ->
                        messages = messages + text
                        comment = ""
                    },
                    placeholder = "Share your thoughts...",
                )

                KiraTextarea(
                    value = chatText,
                    onValueChange = { chatText = it },
                    label = "Chat-style (Enter submits, Shift+Enter newline)",
                    clearable = true,
                    submitOn = KiraTextareaSubmitOn.Enter,
                    onSubmit = { text ->
                        messages = messages + text
                        chatText = ""
                    },
                    placeholder = "Press Enter to send...",
                )

                if (messages.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        messages.forEach { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(KiraTheme.radius.shapeMd)
                                    .background(colors.muted.copy(alpha = 0.4f), KiraTheme.radius.shapeMd)
                                    .border(1.dp, colors.border, KiraTheme.radius.shapeMd)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                BasicText(msg, style = KiraTheme.typography.bodySmall.copy(color = colors.foreground))
                            }
                        }
                    }
                }
            }
        }

        GalleryExample("Inline Validation & Disabled / Read-Only States") {
            var username by remember { mutableStateOf("") }
            var feedback by remember { mutableStateOf("") }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KiraTextarea(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username (validates on blur)",
                    placeholder = "3-20 characters, letters and numbers only",
                    validationMode = KiraTextareaValidationMode.OnBlur,
                    validate = { v ->
                        when {
                            v.length < 3 -> "Must be at least 3 characters."
                            v.length > 20 -> "Must be at most 20 characters."
                            !v.matches(Regex("^[a-zA-Z0-9]+$")) -> "Letters and numbers only."
                            else -> null
                        }
                    },
                )

                KiraTextarea(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = "Feedback",
                    required = true,
                    description = "Please provide detailed feedback. Maximum 100 characters.",
                    maxLength = 100,
                    showCount = true,
                    error = if (feedback.length > 80) "Keep feedback concise (under 80 chars)." else null,
                    placeholder = "Over 80 characters shows error state...",
                )

                KiraTextarea(
                    value = "You cannot edit this content.",
                    onValueChange = {},
                    label = "Disabled",
                    enabled = false,
                )

                KiraTextarea(
                    value = "This content is read-only but can be selected and copied.",
                    onValueChange = {},
                    label = "Read-only",
                    readOnly = true,
                )
                ExampleNote("All textareas share KiraInput's token border, dark-mode input fill, and focus ring.")
            }
        }
    }
}
