package com.pulse.app.ui.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EMOJI_LIST = listOf(
    "💧", "🏃", "📖", "🧘", "💪", "🎯", "✍️", "🎨",
    "🎵", "🌱", "💤", "🍎", "💊", "🧹", "📝", "🎓",
    "☀️", "🚴", "🏊", "⏰", "🧠", "❤️", "🙏", "😊",
    "🔥", "⭐", "🌙", "🍳", "🥗", "🚶", "💻", "📱",
    "🎸", "📚", "🧪", "🏋️", "🤸", "🛌", "🚿", "🦷"
)

@Composable
fun EmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(EMOJI_LIST) { emoji ->
            val isSelected = emoji == selectedEmoji
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onEmojiSelected(emoji) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = emoji,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}
