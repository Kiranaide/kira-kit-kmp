package com.kirakit.components.modal

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kirakit.components.dialog.KiraDialog
import com.kirakit.components.dialog.KiraDialogBody
import com.kirakit.components.dialog.KiraDialogFooter
import com.kirakit.components.dialog.KiraDialogHeader
import com.kirakit.components.dialog.KiraDialogSize

/**
 * Size tiers for [KiraModal], backed by [KiraDialogSize].
 */
typealias KiraModalSize = KiraDialogSize

/**
 * Accessible centered modal dialog aligned with kira-kit-v2's Modal showcase (built on `KiraDialog`).
 */
@Composable
fun KiraModal(
    open: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    size: KiraModalSize = KiraDialogSize.Default,
    scrollable: Boolean = false,
    showCloseButton: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    paneTitle: String = "Modal",
    content: @Composable ColumnScope.() -> Unit,
) {
    KiraDialog(
        open = open,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        size = size,
        modal = true,
        scrollable = scrollable,
        showCloseButton = showCloseButton,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        paneTitle = paneTitle,
        content = content,
    )
}

/**
 * Convenience shorthand for a modal with header, body, and optional footer.
 */
@Composable
fun KiraModal(
    open: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    size: KiraModalSize = KiraDialogSize.Default,
    scrollable: Boolean = false,
    showCloseButton: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    footer: (@Composable RowScope.() -> Unit)? = null,
    body: @Composable ColumnScope.() -> Unit,
) {
    KiraDialog(
        open = open,
        onDismissRequest = onDismissRequest,
        title = title,
        modifier = modifier,
        description = description,
        size = size,
        modal = true,
        scrollable = scrollable,
        showCloseButton = showCloseButton,
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        footer = footer,
        body = body,
    )
}

@Composable
fun KiraModalHeader(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    KiraDialogHeader(title = title, modifier = modifier, description = description)
}

@Composable
fun KiraModalBody(
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    KiraDialogBody(modifier = modifier, scrollable = scrollable, content = content)
}

@Composable
fun KiraModalFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    KiraDialogFooter(modifier = modifier, content = content)
}
