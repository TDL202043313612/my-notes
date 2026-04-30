package hf.inner.notebook.page.input

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.moriafly.salt.ui.SaltTheme
import hf.inner.notebook.bean.Attachment
import hf.inner.notebook.utils.formatTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputAudio(
    attachment: Attachment,
    isEdit: Boolean,
    isPlaying: Boolean,
    progress: Int = 0,
    duration: Int = 0,
    modifier: Modifier = Modifier.height(60.dp).width(200.dp),
    delete: (path: String) -> Unit = {},
    onclick: () -> Unit = {},
    onSeek: (Float) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = modifier
            .padding(if (isEdit) 6.dp else 0.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SaltTheme.colors.subText.copy(alpha = 0.1f))
            .clickable { onclick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = isPlaying,
                label = "play_pause_animation"
            ) { playingState ->
                Icon(
                    imageVector = if (playingState) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = SaltTheme.colors.highlight,
                    modifier = Modifier.size(32.dp)
                )
            }
            val sliderRange = if (duration > 0) 0f..duration.toFloat() else 0f..1f
            val currentSliderValue = if (duration > 0) progress.toFloat() else 0f
            Slider(
                value = currentSliderValue,
                onValueChange = { newValue ->
                    if (isPlaying) {
                        onSeek(newValue)
                    }
                },
                valueRange = sliderRange,
                modifier = Modifier.weight(1f),
                thumb = {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(SaltTheme.colors.highlight)
                    )
                },
                colors = SliderDefaults.colors(
//                    thumbColor = SaltTheme.colors.highlight,
                    activeTrackColor = SaltTheme.colors.highlight,
                    inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${formatTime(progress)} / ${formatTime(duration)}",
                style = MaterialTheme.typography.bodySmall,
                color = SaltTheme.colors.subText,
                fontSize = 12.sp
            )
        }
        if (isEdit) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(24.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
                    .clickable {
                        scope.launch { delete(attachment.path) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
    }
}