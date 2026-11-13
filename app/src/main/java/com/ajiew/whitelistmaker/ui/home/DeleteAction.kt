package com.ajiew.whitelistmaker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun DeleteActionBackgroundPreview() {
    Box(
        modifier = Modifier
            .size(400.dp, 120.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(color = Color(96, 20, 16))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        DeleteActionBackgroundTextIcon()
    }
}

@Composable
internal  fun DeleteActionBackground(
    revealed: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        if (revealed) {
            DeleteActionBackgroundTextIcon()
        }
    }
}

@Composable
private fun DeleteActionBackgroundTextIcon() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "左\n滑\n删\n除",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFEAB8B8),
        )
        Icon(
            imageVector = Icons.Outlined.Delete,
            modifier = Modifier.size(32.dp),
            contentDescription = "删除",
            tint = Color(0xFFEAB8B8),
        )
    }
}