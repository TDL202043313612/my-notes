package hf.inner.notebook.component

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TextButton
import hf.inner.notebook.R
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDateRangePicker(
    onDismissRequest: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    // skipPartiallyExpanded = true: 底部弹窗跳过“半展开”状态
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var startYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var startMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
    var startDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth)  }

    var endYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var endMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
    var endDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth)  }

    var selectingStartDate by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SaltTheme.colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = SaltTheme.colors.subText.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel), color = SaltTheme.colors.subText)
                }
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.date_range), style = SaltTheme.textStyles.main.copy(fontWeight =
                        FontWeight.Bold))
                }
                TextButton(
                    onClick = {
                        val start = LocalDate.of(startYear, startMonth, startDay)
                            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val end = LocalDate.of(endYear, endMonth, endDay)
                            .atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        onConfirm(start, end)
                    }
                ) {
                    androidx.compose.material3.Text(
                        stringResource(R.string.sure),
                        color = SaltTheme.colors.highlight
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SaltTheme.colors.subBackground)
                    .padding(4.dp)
            ) {
                val indicatorBias by animateFloatAsState(
                    targetValue = if (selectingStartDate) -1f else 1f,
                    animationSpec = spring(),
                    label = "indicator_bias"
                )
                Box(modifier = Modifier.matchParentSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight()
                            .align(BiasAlignment(indicatorBias, 0f))
                            .clip(RoundedCornerShape(8.dp))
                            .background(SaltTheme.colors.background)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tabModifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp)

                    Box(
                        modifier = tabModifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { selectingStartDate = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$startYear-$startMonth-$startDay",
                            style = SaltTheme.textStyles.main.copy(
                                fontSize = 14.sp,
                                fontWeight = if (selectingStartDate) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                    Box(
                        modifier = tabModifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectingStartDate = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$endYear-$endMonth-$endDay",
                            style = SaltTheme.textStyles.main.copy(
                                fontSize = 14.sp,
                                fontWeight = if (!selectingStartDate) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }

                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (selectingStartDate) {
                WheelDatePicker(
                    year = startYear,
                    month = startMonth,
                    day = startDay,
                    onYearChange = { startYear = it },
                    onMonthChange = { startMonth = it },
                    onDayChange = { startDay = it }
                )
            } else {
                WheelDatePicker(
                    year = endYear,
                    month = endMonth,
                    day = endDay,
                    onYearChange = { endYear = it },
                    onMonthChange = { endMonth = it },
                    onDayChange = { endDay = it }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WheelDatePicker(
    year: Int,
    month: Int,
    day: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
) {
    val currentYear = LocalDate.now().year
    val years = remember { (currentYear - 20 .. currentYear + 5).toList() }
    val months = remember { (1 .. 12).toList() }
    val days = remember(year, month) {
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        (1 .. daysInMonth).toList()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(1f)) {
            WheelPicker(items = years, initialItem = year, onItemSelected = onYearChange)
        }
        Box(modifier = Modifier.weight(1f)) {
            WheelPicker(items = months, initialItem = month, onItemSelected = onMonthChange)
        }
        Box(modifier = Modifier.weight(1f)) {
            WheelPicker(items = days, initialItem = day, onItemSelected = onDayChange)
        }
    }
}

@Composable
fun <T> WheelPicker(
    items: List<T>,
    initialItem: T,
    onItemSelected: (T) -> Unit,
) {
    val lazyListState = rememberLazyListState(
        // initialFirstVisibleItemIndex 第一次渲染时，那个索引的元素应该排在第一个可见的位置
        initialFirstVisibleItemIndex = items.indexOf(initialItem).coerceAtLeast(0)
    )

    LaunchedEffect(lazyListState.isScrollInProgress) {
        // 列表停止滚动
        if (!lazyListState.isScrollInProgress) {
            // 第一个可见元素
            val centerIndex = lazyListState.firstVisibleItemIndex
            if (centerIndex < items.size) {
                onItemSelected(items[centerIndex])
                // 用户松手时，中间那个项目可能并没有完美居中，可能偏上一点或者偏下一点。
                // 这行代码的作用是：强制列表以平滑的动画，把刚刚那个元素丝毫不差地挪到标准位置。
                lazyListState.animateScrollToItem(centerIndex)
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 选择框遮罩
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(SaltTheme.colors.highlight.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        )
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            // 为整个可滚动内容（Content）的边缘添加内边距【导致firstVisibleItemIndex根据contentPadding的值而移动】
            contentPadding = PaddingValues(vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val item = items[index]
                Text(
                    text = item.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        // 垂直居中
                        .wrapContentHeight(Alignment.CenterVertically),
                    // 水平居中
                    textAlign = TextAlign.Center,
                    style = SaltTheme.textStyles.main.copy(
                        fontSize = 18.sp,
                        fontWeight = if (lazyListState.firstVisibleItemIndex == index) FontWeight.Bold else
                            FontWeight.Normal,
                        color = if (lazyListState.firstVisibleItemIndex == index) SaltTheme.colors.text else
                            SaltTheme.colors.subText
                    )
                )
            }
        }
    }
}