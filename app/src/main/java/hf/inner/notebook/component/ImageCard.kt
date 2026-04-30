package hf.inner.notebook.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import hf.inner.notebook.bean.Attachment
import hf.inner.notebook.bean.Note
import hf.inner.notebook.page.home.clickable
import hf.inner.notebook.page.input.InputAudio
import hf.inner.notebook.page.input.InputImage
import hf.inner.notebook.page.router.Screen
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttachmentCard(
    note: Note,
    navHostController: NavHostController?,
) {
    val memosViewModel = LocalMemosViewModel.current
    if (note.attachments.size == 1 && note.attachments[0].type == Attachment.Type.IMAGE) {
        val singleAttachment = note.attachments[0]
        InputImage(
            attachment = singleAttachment,
            isEdit = false,
            modifier = Modifier.size(160.dp),
            onclick = {
                navHostController?.navigate(Screen.PictureDisplay(
                    arrayListOf(singleAttachment.path), 0, listOf(note.createTime)
                ))
            }
        )

    } else {
        LazyRow(
            modifier = Modifier.height(90.dp).padding(end = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(items = note.attachments) { index, attachment ->
                when(attachment.type) {
                    Attachment.Type.IMAGE -> {
                        InputImage(
                            attachment = attachment,
                            isEdit = false,
                            onclick = {
                                val onlyImages = note.attachments.filter { it.type == Attachment.Type.IMAGE }
                                val realIndex = onlyImages.indexOf(attachment)
                                navHostController?.navigate(Screen.PictureDisplay(
                                    onlyImages.map { it.path }, realIndex, listOf(note.createTime)
                                ))
                            }
                        )
                    }
                    Attachment.Type.AUDIO -> {
                        val isCurrentlyPlaying = memosViewModel.currentlyPlayingPath == attachment.path
                        InputAudio(
                            attachment = attachment,
                            isEdit = false,
                            isPlaying = isCurrentlyPlaying,
                            progress = if (isCurrentlyPlaying) memosViewModel.currentAudioProgress else 0,
                            duration = if (isCurrentlyPlaying) memosViewModel.currentAudioDuration else 0,
                            onclick = { memosViewModel.toggleAudio(attachment.path) },
                            onSeek = { position -> memosViewModel.seekAudio(position) }
                        )
                    }
                    else -> { }
                }

            }
        }
    }
}

@Composable
fun ImageCard(note: Note, navHostController: NavHostController?) {
    if (note.attachments.size == 1) {
        AsyncImage(
            model = note.attachments[0].path,
            contentDescription = null,
            modifier = Modifier
                .width(160.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable{
                    navHostController?.navigate(Screen.PictureDisplay(arrayListOf(note.attachments[0].path), 0,
                        listOf(note.createTime)))
                },
            contentScale = ContentScale.Crop
        )
    } else {
        LazyRow(
            modifier = Modifier.height(90.dp).padding(end = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(
                count = note.attachments.size,
//                key = { index -> note.attachments[index].path }
            ) { index ->
                val path: String = note.attachments[index].path
                AsyncImage(
                    model = path,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        // 强行保持 1:1 的宽高比
                        .aspectRatio(1f)
                        .zIndex(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable{
                            navHostController?.navigate(Screen.PictureDisplay(note.attachments.map { it.path },
                                index, listOf(note.createTime)))
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}