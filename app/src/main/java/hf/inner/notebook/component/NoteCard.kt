package hf.inner.notebook.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import hf.inner.notebook.R
import hf.inner.notebook.bean.NoteShowBean
import dev.tdl.compose.markdowntext.MarkdownText
import hf.inner.notebook.page.home.clickable
import hf.inner.notebook.page.router.Screen
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import hf.inner.notebook.utils.toTime

enum class NoteCardFrom {
    SEARCH, TAG_DETAIL, COMMON,
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NoteCard(
    noteShowBean: NoteShowBean,
    navHostController: NavHostController,
    from: NoteCardFrom = NoteCardFrom.COMMON,
    isCanNavigate: Boolean = true,
    onCommentClick: ((NoteShowBean) -> Unit)? = null,
    isChatBubble: Boolean = false // 是否为批注卡片
) {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    var isExpanded by rememberSaveable { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    val note = noteShowBean.note
    val memosViewModel = LocalMemosViewModel.current

    val cardShape = if (isChatBubble) {
        RoundedCornerShape(topStart = 2.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        CardDefaults.shape
    }
    val cardModifier = if (isChatBubble) {
        Modifier
            .fillMaxWidth(0.85f)
            .padding(vertical = 2.dp)
            // 防止卡片内部元素溢出边界
            .clip(cardShape)
    } else {
        Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(cardShape)
    }
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = if (note.isPinned && isCanNavigate) {
                Color(0xFFBDE9C2)
            } else {
                SaltTheme.colors.subBackground
            }
        ),
        modifier = cardModifier
            .combinedClickable(
                enabled = isCanNavigate,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {
                    if (note.parentNoteId != null) {
                        navHostController.navigate(route = Screen.CommentList(note.parentNoteId!!))
                    } else {
                        navHostController.navigate(route = Screen.InputDetail(note.noteId))
                    }
                },
                onLongClick = {
                    openBottomSheet = true
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            MarkdownText(
                markdown = note.content,
                maxLines = if (isExpanded) 8 else Int.MAX_VALUE,
                style = SaltTheme.textStyles.paragraph.copy(fontSize = 15.sp, lineHeight = 24.sp),
                onTagClick = {
                    if (from == NoteCardFrom.COMMON && isCanNavigate) {
                        navHostController.navigate(Screen.TagDetail(it))
                    }
                }
            )
            if (note.content.length > 200) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    modifier = Modifier.clickable{ isExpanded = !isExpanded },
                    color = Color.Blue.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    text = if(isExpanded) stringResource(R.string.read_more) else stringResource(R.string.collapse),
                )
            }
            if (note.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
//                ImageCard(note = note, navHostController)
                AttachmentCard(
                    note = note,
                    navHostController = navHostController,
                )
            }
            if (noteShowBean.parentNote != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = noteShowBean.parentNote.content,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        style = SaltTheme.textStyles.paragraph.copy(
                            fontSize = 13.sp,
                            color = SaltTheme.colors.subText
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier.padding(start = 2.dp),
                text = note.createTime.toTime(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
    ActionBottomSheet(
        navHostController = navHostController,
        noteShowBean = noteShowBean,
        show = openBottomSheet,
        onCommentClick = onCommentClick
    ) {
        openBottomSheet = false
    }
}