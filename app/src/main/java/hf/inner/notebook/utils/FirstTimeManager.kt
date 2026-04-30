package hf.inner.notebook.utils

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import hf.inner.notebook.App
import hf.inner.notebook.R
import hf.inner.notebook.bean.Note
import hf.inner.notebook.db.repo.TagNoteRepo
import hf.inner.notebook.page.home.clickable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class FirstTimeManager @Inject constructor() {
    @Inject
    lateinit var tagNoteRepo: TagNoteRepo

    fun generateIntroduceNoteList() {
        CoroutineScope(Dispatchers.IO).launch {
            if (!SettingsPreferences.firstLaunch.first() || tagNoteRepo.queryAllNoteList().isNotEmpty()) {
                return@launch
            }
            if (App.instance.isSystemLanguageEnglish()) {
                generateEnglishIntroduceNoteList()
            } else {
                generateChineseIntroduceNoteList()
            }
        }
    }

    private fun generateEnglishIntroduceNoteList() {
        val functionNote = Note(
            content = "#Life \nLess is more.",
        )
        tagNoteRepo.insertOrUpdate(functionNote)
    }

    private fun generateChineseIntroduceNoteList() {
        val functionNote = Note(
            content = "#灵感 \n生活不止眼前的苟且 还有诗和远方。",
        )
        tagNoteRepo.insertOrUpdate(functionNote)
    }
}

@Composable
fun FirstTimeWarmDialog(block: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        containerColor = SaltTheme.colors.subBackground,
        onDismissRequest = { },
        title = { Text(stringResource(R.string.welcome), color = SaltTheme.colors.text) },
        text = {
            Column {
                Text(stringResource(R.string.warm_reminder_desc), color = SaltTheme.colors.text)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.browse_tos_tips_service),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.clickable {
                        Constant.startUserAgreeUrl(context)
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    stringResource(R.string.browse_tos_tips_privacy),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.outline,
                    ),
                    modifier = Modifier.clickable {
                        Constant.startPrivacyUrl(context)
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                block()
            }) {
                Text(stringResource(id = R.string.agree))
            }
        },
        dismissButton = {
            Button(onClick = {
                if (context is Activity) {
                    context.finish()
                }
            }) {
                Text(stringResource(id = R.string.exit))
            }
        }
    )
}