package hf.inner.notebook.page.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.HeatMapCalendar
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapCalendarState
import com.kizitonwose.calendar.compose.heatmapcalendar.HeatMapWeek
import com.kizitonwose.calendar.compose.heatmapcalendar.rememberHeatMapCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import hf.inner.notebook.page.home.clickable
import hf.inner.notebook.page.home.displayText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

enum class Level(val color: Color) {
    Zero(Color(0xFFEBEDF0)),
    One(Color(0xFF9BE9A8)),
    Two(Color(0xFF40C463)),
    Three(Color(0xFF30A14E)),
    Four(Color(0xFF216E3A)),
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeatContent() {
    val noteViewModel = LocalMemosViewModel.current

    val endDate = remember { LocalDate.now() }
    val startDate = remember { endDate.minusMonths(12) }

    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxSize()
    ) {
        val state = rememberHeatMapCalendarState(
            startMonth = startDate.yearMonth,
            endMonth = endDate.yearMonth,
            firstVisibleMonth = endDate.yearMonth,
            firstDayOfWeek = firstDayOfWeekFromLocale()
        )
        HeatMapCalendar(
            modifier = Modifier.padding(vertical = 10.dp),
            state = state,
            contentPadding = PaddingValues(end = 6.dp),
            dayContent = { day, week ->
                Day(
                    day = day,
                    startDate = startDate,
                    endDate = endDate,
                    week = week,
                    level = noteViewModel.levelMemosMap[day.date] ?: Level.Zero,
                ) { clicked ->
                    // TODO
                }
            },
            weekHeader = { dayOfWeek ->
                WeekHeader(dayOfWeek)
            },
            monthHeader = { calendarMonth ->
                MonthHeader(calendarMonth, endDate, state)
            }
        )
        CalendarInfo(modifier = Modifier.fillMaxWidth().align(Alignment.End))
    }
}

@Composable
private fun CalendarInfo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Less", style = SaltTheme.textStyles.sub.copy(fontSize = 10.sp))
        Level.values().forEach { level ->
            LevelBox(level.color)
        }
        Text(text = "More", style = SaltTheme.textStyles.sub.copy(fontSize = 10.sp))
    }
}

private val daySize = 15.dp


// 单格
@Composable
private fun Day(
    day: CalendarDay,
    startDate: LocalDate,
    endDate: LocalDate,
    week: HeatMapWeek,
    level: Level,
    onClick: (LocalDate) -> Unit
) {
    // 一周
    val weekDates = week.days.map { it.date }
    // day不在有效的日期内-》透明
    if (day.date in startDate .. endDate) {
        LevelBox(level.color) { onClick(day.date) }
    } else if (weekDates.contains(startDate)) {
        LevelBox(Color.Transparent)
    }
}

@Composable
private fun WeekHeader(dayOfWeek: DayOfWeek) {
    Box(
        modifier = Modifier
            .height(daySize)
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = dayOfWeek.displayText(),
            modifier = Modifier.align(Alignment.Center),
            color = SaltTheme.colors.subText,
            fontSize = 8.sp
        )
    }
}

@Composable
private fun MonthHeader(
    calendarMonth: CalendarMonth,
    endDate: LocalDate,
    state: HeatMapCalendarState
) {
    val density = LocalDensity.current
    val firstFullVisibleMonth by remember {
        // 把高频变化的状态，过滤成低频变化的 UI 刷新
        derivedStateOf { getMonthWithYear(state.layoutInfo, daySize, density) }
    }

    if (calendarMonth.weekDays.first().first().date <= endDate) {
        val month = calendarMonth.yearMonth
        // 表头该怎么显示月份+年份
        val title = if (month == firstFullVisibleMonth) {
            month.displayText(short = true)
        } else {
            month.month.displayText()
        }
        Box(Modifier
            .fillMaxWidth()
            .padding(bottom = 1.dp, start = 2.dp)
        ) {
            Text(text = title, fontSize = 10.sp, color = SaltTheme.colors.subText)
        }
    }

}

private fun getMonthWithYear(
    layoutInfo: CalendarLayoutInfo,
    daySize: Dp,
    density: Density
): YearMonth? {
    // 向底层要数据看看屏幕上画了那几个月
    val visibleItemsInfo = layoutInfo.visibleMonthsInfo
    return when {
        visibleItemsInfo.isEmpty() -> null // 啥也没有
        visibleItemsInfo.count() == 1 -> visibleItemsInfo.first().month.yearMonth // 只有一个月
        else -> { // 两个或以上
            val firstItem = visibleItemsInfo.first()
            val daySizePx = with(density) { daySize.toPx() }

            if (
                // firstItem.size：物理像素px
                // 第一个月的总宽度小于3天的宽度
                firstItem.size < daySizePx * 3 ||
                // 第一个月已经被滑出屏幕左侧，并且划出去的部分超过了1天的宽度
                firstItem.offset < layoutInfo.viewportStartOffset &&
                (layoutInfo.viewportStartOffset - firstItem.offset > daySizePx)
            ) {
                // 判给第二个月
                visibleItemsInfo[1].month.yearMonth
            } else {
                // 判给第一个月
                firstItem.month.yearMonth
            }
        }
    }
}
@Composable
private fun LevelBox(color: Color, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .size(daySize)
            .padding(2.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color = color)
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            }
    )
}

