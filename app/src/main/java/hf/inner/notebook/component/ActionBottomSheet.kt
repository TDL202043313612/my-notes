package hf.inner.notebook.component

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.moriafly.salt.ui.SaltTheme
import hf.inner.notebook.R
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import hf.inner.notebook.page.router.Screen
import hf.inner.notebook.utils.BackUp
import hf.inner.notebook.utils.ExportMarkDownContract
import hf.inner.notebook.utils.copy
import hf.inner.notebook.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionBottomSheet(
    navHostController: NavHostController,
    noteShowBean: NoteShowBean,
    show: Boolean,
    onCommentClick: ((NoteShowBean) -> Unit)? = null,
    onDismissRequest: () -> Unit
) {

    val viewModel = LocalMemosViewModel.current
    val skipPartiallyExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    val fileName = if (noteShowBean.note.content.length > 4) noteShowBean.note.content.take(4) else noteShowBean.note.content
    val successMsg = stringResource(R.string.execute_success)
    val exportMarkDownLauncher = rememberLauncherForActivityResult(ExportMarkDownContract(fileName)) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            BackUp.exportMarkDownFile(list = arrayListOf(noteShowBean), uri)
            toast(successMsg)
        }
    }

    if (show) {
        ModalBottomSheet(
            containerColor = SaltTheme.colors.popup,
            onDismissRequest = onDismissRequest,
            sheetState = bottomSheetState,
        ) {
            Row(
                Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Absolute.Center
            ) {
                LazyColumn {
                    item {
                        TextButton(onClick = {
                            onCommentClick?.invoke(noteShowBean)
                            onDismissRequest()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(R.string.comment), style = SaltTheme.textStyles.paragraph)
                        }
                    }
                    item {
                        TextButton(onClick = {
                            viewModel.updatePinStatus(noteShowBean.note)
                            onDismissRequest()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = if (noteShowBean.note.isPinned) stringResource(R.string.cancel_top_note) else stringResource(R.string.top_note),
                                style = SaltTheme.textStyles.paragraph)
                        }
                    }
                    item {
                        TextButton(onClick = {
                            copy(noteShowBean.note)
                            onDismissRequest()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(R.string.copy), style = SaltTheme.textStyles.paragraph)
                        }
                    }
                    item {
                        TextButton(onClick = {
                            navHostController.navigate(Screen.Share(noteShowBean.note.noteId))
                            onDismissRequest()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(R.string.share), style = SaltTheme.textStyles.paragraph)
                        }
                    }

                    item {
                        TextButton(onClick = {
                            scope.launch {
                                viewModel.deleteNote(noteShowBean.note, noteShowBean.tagList)
                                onDismissRequest()
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(R.string.delete), style = SaltTheme.textStyles.paragraph)
                        }
                    }
                    item {
                        TextButton(onClick = {
                            scope.launch {
                                scope.launch(Dispatchers.IO) {
                                    exportMarkDownLauncher.launch(Unit)
                                }
                                onDismissRequest()
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = stringResource(R.string.export), style = SaltTheme.textStyles.paragraph)
                        }
                    }
                }
            }
        }
    }
}