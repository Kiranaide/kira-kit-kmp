package com.kirakit.gallery.label

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kirakit.components.input.KiraInput
import com.kirakit.components.label.KiraLabel
import com.kirakit.components.label.KiraLabelGroup
import com.kirakit.components.label.KiraLabelSize
import com.kirakit.components.label.KiraLabelValidationState
import com.kirakit.gallery.GalleryExample
import com.kirakit.gallery.SectionHeading
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing

@Composable
internal fun LabelPlayground() {
    val colors = KiraTheme.colors
    val gap = KiraTheme.spacing.md

    var email by rememberSaveable { mutableStateOf("") }
    var apiKey by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("Building multiplatform apps with Kira Kit") }
    var username by rememberSaveable { mutableStateOf("kira") }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        SectionHeading("Basic, Icons & Tooltip")
        GalleryExample("Icons, Required & Info Tooltip") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    KiraLabel(
                        text = "Email Address",
                        required = true,
                        icon = { MailGlyph(colors.mutedForeground) },
                        tooltip = "We'll send a confirmation link to this address.",
                    )
                    KiraInput(value = email, onValueChange = { email = it }, placeholder = "you@example.com")
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    KiraLabel(
                        text = "API Key",
                        optional = true,
                        tooltip = "Your API key authenticates requests. Keep it secret!",
                        hint = "Press & hold info icon on mobile",
                    )
                    KiraInput(value = apiKey, onValueChange = { apiKey = it }, placeholder = "sk-...")
                }
            }
        }

        SectionHeading("Sizes & Counters")
        GalleryExample("Sm, Md, Lg & Counter Pills") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KiraLabel(text = "Small Label (12sp)", size = KiraLabelSize.Sm, hint = "compact hint")
                KiraLabel(text = "Medium Label (14sp default)", size = KiraLabelSize.Md, counter = 12)
                KiraLabel(text = "Large Label (16sp)", size = KiraLabelSize.Lg, counter = 150)
                KiraLabel(text = "Disabled Label", disabled = true, hint = "read-only field")
                KiraLabel(
                    text = "Very long label that truncates cleanly with an ellipsis when space is tight on mobile viewports",
                    truncate = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        SectionHeading("Live Character Count & Validation")
        GalleryExample("KiraLabel with count/maxCount & validationState") {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val maxBio = 60
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    KiraLabel(
                        text = "Short Bio",
                        count = bio.length,
                        maxCount = maxBio,
                        error = bio.length > maxBio,
                        description = "Shown on your public contributor profile.",
                    )
                    KiraInput(value = bio, onValueChange = { bio = it }, placeholder = "Tell us about yourself…")
                }

                val validationState = when {
                    username.length < 3 -> KiraLabelValidationState.Invalid
                    username.equals("admin", ignoreCase = true) -> KiraLabelValidationState.Validating
                    else -> KiraLabelValidationState.Valid
                }
                val validationMsg = when (validationState) {
                    KiraLabelValidationState.Invalid -> "Username must be at least 3 characters."
                    KiraLabelValidationState.Validating -> "Checking availability…"
                    KiraLabelValidationState.Valid -> "Username is available!"
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    KiraLabel(
                        text = "Username",
                        required = true,
                        validationState = validationState,
                        validationMessage = validationMsg,
                    )
                    KiraInput(value = username, onValueChange = { username = it }, placeholder = "Pick a handle")
                }

                KiraLabelGroup(
                    label = "Workspace Slug",
                    description = "Used in your organization's URL.",
                    required = true,
                ) {
                    KiraInput(value = "kira-design", onValueChange = {}, placeholder = "my-team")
                }
            }
        }
    }
}

@Composable
private fun MailGlyph(color: Color) {
    Canvas(Modifier.size(14.dp)) {
        val stroke = Stroke(width = 1.4.dp.toPx())
        drawRoundRect(
            color = color,
            topLeft = Offset(1f, size.height * 0.18f),
            size = Size(size.width - 2f, size.height * 0.64f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = stroke,
        )
        drawLine(
            color = color,
            start = Offset(2f, size.height * 0.24f),
            end = Offset(size.width / 2f, size.height * 0.56f),
            strokeWidth = stroke.width,
        )
        drawLine(
            color = color,
            start = Offset(size.width - 2f, size.height * 0.24f),
            end = Offset(size.width / 2f, size.height * 0.56f),
            strokeWidth = stroke.width,
        )
    }
}
