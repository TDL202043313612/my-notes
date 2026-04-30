package hf.inner.notebook.page.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.moriafly.salt.ui.SaltTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


fun Modifier.clickable(
    enabled: Boolean = true,
    showRipple: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onclick: () -> Unit
): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        // LocalIndication.current 点击”水波纹“特效
        indication = if (showRipple) LocalIndication.current else null,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = onclick
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Day(day: CalendarDay, today: LocalDate, hasScheme: Boolean, isSelected: Boolean, onclick: (CalendarDay) -> Unit) {
    val backgroundColor = if (day.date == today) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        if (isSelected) MaterialTheme.colorScheme.surfaceContainerHigh else Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1.2f) // 保持长宽比，长是宽1.2倍
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                color = backgroundColor
            )
            .clickable(
                showRipple = !hasScheme,
                onclick = { onclick(day) }
            )
    ) {
        var textColor = when(day.position) {
            DayPosition.MonthDate -> SaltTheme.colors.text //本月日期
            DayPosition.InDate,  DayPosition.OutDate -> Color.Gray // InDate: 上月日期 OutDate: 下月日期
        }

        if (day.date == today) {
            textColor = Color.White
        }
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = day.date.dayOfMonth.toString(),
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        // 这天是否有笔记
        if (hasScheme) {
            Canvas(
                modifier = Modifier
                    .size(4.dp)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
            ) {
                val radius = size.width / 2f
                drawCircle(
                    color = Color.Gray,
                    radius = radius
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = SaltTheme.colors.text,
                text = dayOfWeek.displayText(),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun DayOfWeek.displayText(uppercase: Boolean = false): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault()).let { value ->
        if (uppercase) value.uppercase(Locale.getDefault()) else value
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun YearMonth.displayText(short: Boolean = false): String {
    return "${this.month.displayText(short = short)} ${this.year}"
}

@RequiresApi(Build.VERSION_CODES.O)
fun Month.displayText(short: Boolean = true): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return  getDisplayName(style, Locale.getDefault())
}
