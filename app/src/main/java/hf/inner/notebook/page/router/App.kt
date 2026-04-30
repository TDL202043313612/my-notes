package hf.inner.notebook.page.router

import android.appwidget.AppWidgetManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.darkSaltColors
import com.moriafly.salt.ui.lightSaltColors
import com.moriafly.salt.ui.saltColorsByColorScheme
import com.moriafly.salt.ui.saltConfigs
import hf.inner.notebook.component.PictureDisplayPage
import hf.inner.notebook.page.data.DataManagerPage
import hf.inner.notebook.page.input.MemoInputPage
import hf.inner.notebook.page.main.MainScreen
import hf.inner.notebook.page.search.SearchPage
import hf.inner.notebook.page.settings.ExplorePage
import hf.inner.notebook.page.settings.GalleryPage
import hf.inner.notebook.page.share.SharePage
import hf.inner.notebook.page.tag.CommentListPage
import hf.inner.notebook.page.tag.DateRangePage
import hf.inner.notebook.page.tag.TagDetailPage
import hf.inner.notebook.page.tag.TagListPage
import hf.inner.notebook.page.tag.YearDetailPage
import hf.inner.notebook.utils.SettingsPreferences


val LocalRootNavController = compositionLocalOf<NavHostController> { error("Not find") }


fun NavHostController.debouncedPopBackStack() {
    val currentRoute = this.currentBackStackEntry?.destination?.route
    val previousRoute = this.previousBackStackEntry?.destination?.route
    if (currentRoute != null && previousRoute != null) {
        this.popBackStack()
    } else {
        Log.w("Navigation", "Attempted to pop empty back stack")
    }
}

@OptIn(UnstableSaltApi::class)
@Composable
fun App() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val themeModeState by SettingsPreferences.themeMode.collectAsState(SettingsPreferences.ThemeMode.SYSTEM)
    val dynamicColor by SettingsPreferences.dynamicColor.collectAsState(false)
    val darkTheme = when(themeModeState) {
        SettingsPreferences.ThemeMode.SYSTEM -> isSystemInDarkTheme()
        SettingsPreferences.ThemeMode.DARK -> true
        else -> false
    }

    val colors = when(themeModeState) {
        SettingsPreferences.ThemeMode.LIGHT -> if (dynamicColor) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            saltColorsByColorScheme(
                //它从用户的手机壁纸中提取主色调、强调色等颜色种子（Color Seeds），
                // 并根据这些种子生成一套完整的 Material 3 颜色矩阵（包含 primary, onSecondary, surface 等几十种颜色）
                dynamicLightColorScheme(context)
            )
        } else {
            // VERSION.SDK_INT < S
            lightSaltColors()
        } else {
            // dynamicColor = false
            lightSaltColors()
        }
        SettingsPreferences.ThemeMode.DARK -> if (dynamicColor) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            saltColorsByColorScheme(
                dynamicDarkColorScheme(context)
            )
        } else {
            // VERSION.SDK_INT < S
            darkSaltColors()
        } else {
            // dynamicColor = false
            darkSaltColors()
        }
        SettingsPreferences.ThemeMode.SYSTEM -> {
            if (isSystemInDarkTheme())
                if (dynamicColor) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    saltColorsByColorScheme(
                        dynamicDarkColorScheme(context)
                    )
                } else {
                    // VERSION.SDK_INT < S
                    darkSaltColors()
                } else darkSaltColors()
            else
                if (dynamicColor) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    saltColorsByColorScheme(
                        dynamicLightColorScheme(context)
                    )
                } else {
                    // VERSION.SDK_INT < S
                    lightSaltColors()
                } else lightSaltColors()
        }
    }
//    CompositionLocalProvider(LocalRootNavController provides navController) {
//        SaltTheme(
//            colors = colors,
//            configs = saltConfigs(isDarkTheme = darkTheme)
//        ) {
//            NavHostContainer(navController = navController)
//        }
//    }

    SaltTheme(
        colors = colors,
        configs = saltConfigs(isDarkTheme = darkTheme)
    ) {
        CompositionLocalProvider(
            LocalRootNavController provides navController,
            LocalIndication provides ripple()
        ) {
            NavHostContainer(navController = navController)
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavHostContainer(
    navController: NavHostController,
) {
    NavHost(
        navController,
        startDestination = Screen.Main
    ) {
        composable<Screen.Main> {
            MainScreen(navController = navController)
        }
        composable<Screen.CommentList> { navBackStackEntry ->
            val args = navBackStackEntry.toRoute<Screen.CommentList>()
            CommentListPage(args.parentNoteId, navController)
        }
        composable<Screen.Explore> {
            ExplorePage(navController)
        }
        composable<Screen.InputDetail> { navBackStackEntry ->
            val arg = navBackStackEntry.toRoute<Screen.InputDetail>()
            MemoInputPage(arg.id)
        }
        composable<Screen.TagList> {
            TagListPage(navController)
        }
        composable<Screen.YearDetail> { navBackStackEntry ->
            val args = navBackStackEntry.toRoute<Screen.YearDetail>()
            YearDetailPage(args.year, navController)
        }
        composable<Screen.TagDetail> { navBackStackEntry ->
            val args = navBackStackEntry.toRoute<Screen.TagDetail>()
            TagDetailPage(tag = args.tag, navController = navController)
        }
        composable<Screen.Search> {
            SearchPage(navController = navController)
        }
        composable<Screen.PictureDisplay> { navBackStackEntry ->
            val args = navBackStackEntry.toRoute<Screen.PictureDisplay>()
            PictureDisplayPage(pathList = args.pathList, index = args.curIndex, timestamps = args.timestamps,
                navController = navController)
        }
        composable<Screen.DataManager> { navBackStackEntry ->
            DataManagerPage(navController)
        }

        composable<Screen.RandomWalk> { navBackStackEntry ->
            ExplorePage(navController)
        }
        composable<Screen.Gallery> { navBackStackEntry ->
            GalleryPage(navController)
        }
        composable<Screen.DateRangePage> { navBackStackEntry ->
            val args = navBackStackEntry.toRoute<Screen.DateRangePage>()
            DateRangePage(startTime = args.startTime, endTime = args.endTime, navController = navController)
        }
        composable<Screen.Share> { navBackStackEntry ->
            val args = navBackStackEntry.toRoute<Screen.Share>()
            SharePage(args.id, navController)
        }
    }
}