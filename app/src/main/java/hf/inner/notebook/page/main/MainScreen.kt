package hf.inner.notebook.page.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.moriafly.salt.ui.SaltTheme
import hf.inner.notebook.page.home.AllNotesPage
import hf.inner.notebook.page.home.CalenderPage
import hf.inner.notebook.page.settings.SettingsPage
import hf.inner.notebook.utils.isWideScreen
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavHostController) {
    var currentDestination by rememberSaveable { mutableStateOf(NavigationBarPath.AllNote.route) }
    val pagerState = rememberPagerState(initialPage = 0) { NavigationBarPath.entries.size }
    val scope = rememberCoroutineScope()

    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    var hideNavBar by rememberSaveable { mutableStateOf(false) }

    // 当configuration.orientation只改变-》调用isWideScreen函数
    val isWideScreen = remember(configuration.orientation) { configuration.screenWidthDp > configuration.screenHeightDp }

    val navigationBar: @Composable () -> Unit = {
        AdaptiveNavigationBar(
            destinations = NavigationBarPath.entries,
            currentDestination = currentDestination,
            onNavigateToDestination = {
                currentDestination = NavigationBarPath.entries[it].route
                scope.launch { pagerState.scrollToPage(it) }
            },
            isWideScreen = isWideScreen,
        )
    }

    val pagerContent: @Composable (Modifier) -> Unit = { modifier ->
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = modifier
        ) { page ->
            when(page) {
                0 -> AllNotesPage(navController = navController) { hide ->
                    hideNavBar = hide
                }
                1 -> CalenderPage(navController = navController)
                2-> SettingsPage(navController = navController)
            }
        }
    }

    if (isWideScreen) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .displayCutoutPadding()
        ) {
            if (!hideNavBar) {
                navigationBar()
            }
            pagerContent(
                Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().background(SaltTheme.colors.subBackground)) {
            pagerContent(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
            )
            if (!hideNavBar) {
                navigationBar()
            }
        }
    }
}