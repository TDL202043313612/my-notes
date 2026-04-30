package hf.inner.notebook.page.main

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import java.util.Locale

@Composable
fun AdaptiveNavigationBar(
    destinations: List<NavigationBarPath>,
    currentDestination: String,
    onNavigateToDestination: (Int) -> Unit,
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    if (isWideScreen) {
        // 使用 NavigationRail 适配宽屏
        NavigationRail(modifier, containerColor = SaltTheme.colors.subBackground) {
            destinations.forEachIndexed { index, destination ->
                val selected = destination.route == currentDestination
                NavigationRailItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(index) },
                    icon = destination.icon,
                )
            }
        }
    } else {
        // 使用 NavigationBar 适配普通屏幕
        NavigationBar(Modifier.height(56.dp), containerColor = SaltTheme.colors.subBackground) {
            destinations.forEachIndexed { index, destination ->
                val selected = destination.route == currentDestination
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigateToDestination(index) },
                    icon = destination.icon,
                )
            }
        }
    }
}
enum class NavigationBarPath(
    val route: String,
    val icon: @Composable () -> Unit,
    val label: @Composable () -> Unit
) {
    AllNote(
        route = "home".capitalize(),
        icon = {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "home"
            )
        },
        label = { Text("home") }
    ),

    Calendar(
        route = "calendar".capitalize(),
        icon = {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "calendar"
            )
        },
        label = { Text("calendar") }
    ),
    Settings(
        route = "settings".capitalize(),
        icon = {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "settings"
            )
        },
        label = { Text("settings") }
    )
}

private fun String.capitalize() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }