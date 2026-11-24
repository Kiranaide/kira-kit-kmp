package com.kirakit.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.kirakit.components.card.KiraCard
import com.kirakit.components.card.KiraCardSize
import com.kirakit.theme.KiraTheme
import com.kirakit.theme.spacing
import com.kirakit.theme.typography

@Composable
internal fun GalleryExample(title: String, content: @Composable () -> Unit) {
    KiraCard(Modifier.fillMaxWidth(), size = KiraCardSize.Sm) {
        Column(verticalArrangement = Arrangement.spacedBy(KiraTheme.spacing.md)) {
            BasicText(
                title, Modifier.semantics { heading() },
                style = KiraTheme.typography.heading3.copy(color = KiraTheme.colors.cardForeground),
            )
            content()
        }
    }
}

@Composable
internal fun ExampleNote(text: String) {
    BasicText(text, style = KiraTheme.typography.bodySmall.copy(color = KiraTheme.colors.mutedForeground))
}
