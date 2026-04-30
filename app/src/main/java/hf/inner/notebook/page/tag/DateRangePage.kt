package hf.inner.notebook.page.tag

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.component.NoteCard
import hf.inner.notebook.component.NoteCardFrom
import hf.inner.notebook.component.RYScaffold
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateRangePage(startTime: Long, endTime: Long, navController: NavHostController) {
    val noteViewModel = LocalMemosViewModel.current
    val filterYearList = remember { mutableStateListOf<NoteShowBean>() }

    LaunchedEffect(key1 = Unit) {
        noteViewModel.getNotesByCreateTimeRange(startTime, endTime).collect {
            filterYearList.clear()
            filterYearList.addAll(it)
        }
    }

    // 转化时间戳为指定格式的字符串
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyyMMdd") }
    val startTimeStr = Instant.ofEpochMilli(startTime).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
    val endTimeStr = Instant.ofEpochMilli(endTime).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
    val title = "$startTimeStr-$endTimeStr"

    RYScaffold(
        title = title,
        navController = navController
    ) {
        LazyColumn {
            items(count = filterYearList.size, key = { it }) { index ->
                NoteCard(noteShowBean = filterYearList[index], navHostController = navController, from = NoteCardFrom.TAG_DETAIL)
            }
            item {
                Spacer(Modifier.height(60.dp))
            }
        }
    }

}