package com.ajiew.whitelistmaker.ui.whitelist

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun WhitelistDeleteConfirmationDialogPreview() {
    WhitelistDeleteConfirmationDialog(
        packageName = "com.example.reader",
        onDismiss = { },
        onConfirm = { },
    )
}

@Composable
internal fun WhitelistDeleteConfirmationDialog(
    packageName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("从白名单删除？") },
        text = {
            Text("确认删除包名 $packageName？此操作无法撤销。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "删除",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
