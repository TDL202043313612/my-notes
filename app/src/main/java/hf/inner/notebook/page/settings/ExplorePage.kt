package hf.inner.notebook.page.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import dev.tdl.compose.markdowntext.MarkdownText
import hf.inner.notebook.R
import hf.inner.notebook.bean.Note
import hf.inner.notebook.component.AttachmentCard
import hf.inner.notebook.component.DraggableCard
import hf.inner.notebook.component.EmptyComponent
import hf.inner.notebook.component.ImageCard
import hf.inner.notebook.page.viewmodel.LocalMemosState
import hf.inner.notebook.page.router.Screen
import hf.inner.notebook.page.router.debouncedPopBackStack
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import hf.inner.notebook.utils.toTime

@OptIn(UnstableSaltApi::class)
@Composable
fun ExplorePage(
    navHostController: NavHostController
) {
    val noteState = LocalMemosState.current
    // shuffled: 随机打乱 copy: 克隆
    val shuffledList = noteState.notes.shuffled().map { it.note }.map { it.copy() }.take(20).toMutableList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SaltTheme.colors.background)
            .padding(top = 30.dp)
    ) {
        TitleBar(
            onBack = {
                navHostController.debouncedPopBackStack()
            },
            text = stringResource(R.string.random_walk)
        )
        ExploreList(memos = shuffledList, navHostController, onItemClick = { index ->
            navHostController.navigate(Screen.InputDetail(shuffledList[index].noteId))
        })
    }
}

@Composable
fun ExploreList(
    memos: MutableList<Note>,
    navHostController: NavHostController,
    onItemClick: (index: Int) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val cardHeight = screenHeight - 200.dp
    val listEmpty = remember { mutableStateOf(false) }
    if (listEmpty.value) {
        EmptyComponent()
    }
    memos.forEachIndexed { index, note ->
        DraggableCard(
            item = note,
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .padding(
                    top = 16.dp + (index + 2).dp,
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            onSwiped = { swipeResult, note ->
                if (memos.isNotEmpty()) {
                    memos.remove(note)
                    if (memos.isEmpty()) {
                        listEmpty.value = true
                    }
                }
            },
            onClick = { onItemClick(index) }
        ) {
            ExploreMemoCard(note, navHostController)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExploreMemoCard(
    note: Note,
    navHostController: NavHostController
) {
    val memosViewModel = LocalMemosViewModel.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(color = SaltTheme.colors.subBackground)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        MarkdownText(
            markdown = note.content,
            style = SaltTheme.textStyles.paragraph.copy(fontSize = 15.sp, lineHeight = 24.sp),
            onTagClick = {
                navHostController.navigate(Screen.TagDetail(it))
            }
        )
        Spacer(Modifier.height(12.dp))
        if (note.attachments.isNotEmpty()) {
//            ImageCard(note, navHostController)
            AttachmentCard(
                note = note,
                navHostController = navHostController,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.padding(start = 2.dp),
            text = note.createTime.toTime(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.outline
        )
    }
}
