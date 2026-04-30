package hf.inner.notebook.page.home

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import com.moriafly.salt.ui.SaltTheme
import hf.inner.notebook.R
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.component.EmptyComponent
import hf.inner.notebook.component.NoteCard
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalenderPage(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val noteViewModel = LocalMemosViewModel.current
//    var currentLocalDate by remember { mutableStateOf(LocalDate.now()) }
    var currentLocalDate = noteViewModel.selectedDate
    val currentMonth = remember { YearMonth.now() }
    // 是往回倒推 500 个月（大概 41 年）
    val startMonth = remember { currentMonth.minusMonths(500) }
    val endMonth = remember { currentMonth.plusMonths(500) }
    val daysOfWeek = remember { daysOfWeek() }
    val today = remember { LocalDate.now() }
    val filterList = remember { mutableStateListOf<NoteShowBean>() }


    val calendarState: CalendarState = rememberCalendarState(
        startMonth = startMonth, // 日历的绝对起点
        endMonth = endMonth, // 日历的绝对终点
        firstVisibleMonth = currentMonth, // 翻开日历的默认页
        firstDayOfWeek = daysOfWeek.first() // 星期的排版规则：星期几 开头
    )
    LaunchedEffect(currentLocalDate) {
        CoroutineScope(Dispatchers.IO).launch {
            filterList.clear()
            filterList.addAll(noteViewModel.getNotesOnSelectedDate(currentLocalDate))
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            // 它会自动给你的组件顶部，加上一个等同于手机“状态栏”（显示时间、电量、信号的那一栏）高度的内边距（Padding）。
            .statusBarsPadding()
    ) {
        // 顶部标题栏
        IndexTopBar(currentLocalDate, navigationToToday = {
//            currentLocalDate = LocalDate.now()
            val today = LocalDate.now()
            noteViewModel.selectedDate = today
            scope.launch {
                calendarState.animateScrollToMonth(YearMonth.from(today))
            }
        })

        LazyColumn {
            // 吸顶头部: 当用户往上滑动列表时，不要把日历滑出屏幕，而是让它粘在屏幕顶部
            stickyHeader {
                Column {
                    HorizontalCalendar(
                        modifier = Modifier
                            .background(SaltTheme.colors.background),
                        state = calendarState,
                        dayContent = { day ->
                            // day 这天是否有写笔记
                            val hasScheme = noteViewModel.levelMemosMap.containsKey(day.date)
                            Day(day, today, hasScheme = hasScheme, isSelected = currentLocalDate == day.date) { calendarDay ->
//                                currentLocalDate = calendarDay.date
                                noteViewModel.selectedDate = calendarDay.date
                            }
                        },
                        monthHeader = {
                            MonthHeader(daysOfWeek)
                        }

                    )
                }
            }
            if (filterList.isEmpty()) {
                item {
                    EmptyComponent(
                        Modifier
                            .fillMaxWidth()
                            .height(height = 300.dp)
                    )
                }
            } else {
                items(count = filterList.size, key = { it }) { index ->
                    NoteCard(noteShowBean = filterList[index], navController)
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexTopBar(
    date: LocalDate, navigationToToday: () -> Unit, modifier: Modifier = Modifier
) {
    // 日期
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SaltTheme.colors.background),
        modifier = modifier.fillMaxWidth(),
        title = {
            Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = date.month.getDisplayName(
                        TextStyle.SHORT, Locale.getDefault()
                    ) + date.dayOfMonth + stringResource(R.string.day), style = SaltTheme.textStyles.main.copy(fontSize = 24.sp)
                        .copy(fontWeight = FontWeight.Bold)
                )
                Column {
                    Text(
                        text = date.year.toString(), style = SaltTheme.textStyles.main.copy(fontSize = 12.sp).copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = date.dayOfWeek.getDisplayName(
                            TextStyle.SHORT, Locale.getDefault()
                        ),
                        style = SaltTheme.textStyles.main.copy(fontSize = 12.sp).copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {
                navigationToToday()
            }) {
                Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday, contentDescription = "Today", tint = SaltTheme.colors.text
                    )
                    Text(
                        modifier = modifier.padding(top = 4.dp),
                        text = LocalDate.now().dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(color = SaltTheme.colors.text)
                    )
                }
            }
        }
    )
}