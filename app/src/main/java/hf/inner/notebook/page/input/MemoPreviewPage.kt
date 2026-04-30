package hf.inner.notebook.page.input

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import hf.inner.notebook.App
import hf.inner.notebook.R
import hf.inner.notebook.page.router.LocalRootNavController
import hf.inner.notebook.page.router.Screen
import hf.inner.notebook.page.router.debouncedPopBackStack
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MemoPreviewPage(memoId: Long) {
    val memosViewModel = LocalMemosViewModel.current
    val memo by remember(memoId) { memosViewModel.getNoteShowBeanByIdFlow(memoId) }.collectAsState(null)
    val navController = LocalRootNavController.current

    if (memo == null) {
        return
    }
    val localDate = Instant.ofEpochMilli(memo?.note?.createTime ?: System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SaltTheme.colors.background),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = localDate.dayOfMonth.toString(),
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold).copy(color = SaltTheme.colors.text)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = localDate.dayOfWeek.getDisplayName(
                                    TextStyle.SHORT, Locale.getDefault()
                                ),
                                style = MaterialTheme.typography.bodySmall.copy(color = SaltTheme.colors.text),
                                fontSize = 11.sp
                            )
                            Row{
                                Text(
                                    text = localDate.year.toString() + "/" + localDate.month.getDisplayName(
                                        TextStyle.SHORT, Locale.getDefault()
                                    ), style = MaterialTheme.typography.bodySmall.copy(color = SaltTheme.colors.text), fontSize = 10.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.debouncedPopBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = SaltTheme.colors.text)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Share(memoId))
                        }
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.share), tint =
                            SaltTheme.colors.text)
                    }
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.InputDetail(memoId))
                        }
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit), tint = SaltTheme.colors.text)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column() { }
    }
}