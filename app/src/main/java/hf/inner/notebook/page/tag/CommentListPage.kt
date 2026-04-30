package hf.inner.notebook.page.tag

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import hf.inner.notebook.R
import hf.inner.notebook.bean.Note
import hf.inner.notebook.component.NoteCard
import hf.inner.notebook.component.NoteCardFrom
import hf.inner.notebook.page.router.debouncedPopBackStack
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel

@OptIn(UnstableSaltApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentListPage(parentNoteId: Long, navController: NavHostController) {
    val noteViewModel = LocalMemosViewModel.current
    // 获取父笔记的所有评论
    val commentList by noteViewModel.getCommentsByParentId(parentNoteId).collectAsState(emptyList())
    val parentNote by noteViewModel.getNoteShowBeanByIdFlow(parentNoteId).collectAsState(null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SaltTheme.colors.background)
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        TitleBar(
            onBack = {
                navController.debouncedPopBackStack()
            },
            text = stringResource(R.string.comment)
        )
        LazyColumn {
            parentNote?.let { parentNote ->
                item {
                    Text(
                        text = "原笔记",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                        color = SaltTheme.colors.subText,
                        fontSize = 13.sp
                    )
                    // 显示被批注的原卡片
                    NoteCard(
                        noteShowBean = parentNote,
                        navHostController = navController,
                        from = NoteCardFrom.TAG_DETAIL,
                        isCanNavigate = false
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f).height(0.5.dp).background(SaltTheme.colors.subText.copy
                            (alpha = 0.2f)))
                        Text(
                            text = "${stringResource(R.string.total_comments, commentList.size)}",
                            color = SaltTheme.colors.subText,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.weight(1f).height(0.5.dp).background(SaltTheme.colors.subText.copy(alpha = 0.2f)))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(count = commentList.size, key = { commentList[it].note.noteId }) { index ->
                val comment = commentList[index]
                // 显示批注的内容卡片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    NoteCard(
                        noteShowBean = comment.copy(parentNote = null), // 这里设为null，防止NoteCard内部再次显示引用框
                        navHostController = navController,
                        from = NoteCardFrom.TAG_DETAIL,
                        isCanNavigate = false,
                        isChatBubble = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}