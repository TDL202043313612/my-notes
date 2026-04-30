package hf.inner.notebook.page.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastSumBy
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.material.color.DynamicColors
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState
import hf.inner.notebook.R
import hf.inner.notebook.component.ItemPopup
import hf.inner.notebook.page.viewmodel.LocalMemosState
import hf.inner.notebook.page.viewmodel.LocalTags
import hf.inner.notebook.page.data.DataManagerViewModel
import hf.inner.notebook.page.main.MainActivity
import hf.inner.notebook.page.router.Screen
import hf.inner.notebook.utils.SettingsPreferences
import hf.inner.notebook.utils.lunchIo
import hf.inner.notebook.utils.toYYMMDD
import hf.inner.notebook.utils.toast
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsPage(navController: NavHostController) {
    val languageMode by SettingsPreferences.languageMode.collectAsState(SettingsPreferences.LanguageMode.SYSTEM)

    key(languageMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SaltTheme.colors.background)
                .statusBarsPadding()
        ) {
            Spacer(Modifier.height(12.dp))
            Column {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp),
                    text = stringResource(R.string.settings),
                    style = SaltTheme.textStyles.main.copy(fontSize = 24.sp)
                )
            }
            Spacer(Modifier.height(12.dp))
            SettingsPreferenceScreen(navController, languageMode)
        }
    }
}

@Composable
private fun SettingsHeadLayout() {
    val noteState = LocalMemosState.current
    val memos by lazy(noteState::notes)
    val tagList = LocalTags.current
    Row {
        val modifier = Modifier.weight(1f)
        BoxText(
            modifier, memos.size.toString(), stringResource(R.string.all_note)
        )
        BoxText(
            modifier, memos.fastSumBy { it.note.noteTitle?.length ?: (0 + it.note.content.length) }.toString(), stringResource(R.string
                .characters)
        )
        BoxText(
            modifier, memos.map { it.note.createTime.toYYMMDD() }.toSet().size.toString(), stringResource(R.string
                .days)
        )
        BoxText(
            modifier, tagList.size.toString(), stringResource(R.string.tag)
        )
    }
}

@Composable
private fun BoxText(modifier: Modifier, title: String, desc: String) {
    Column(
        modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = SaltTheme.textStyles.main
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = desc,
            style = SaltTheme.textStyles.sub,
        )
    }
}
data class SettingsBean(val title: Int, val imageVector: ImageVector, val onClick: () -> Unit)

@OptIn(UnstableSaltApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SettingsPreferenceScreen(
    navController: NavHostController,
    languageMode: SettingsPreferences.LanguageMode
) {
    val dataViewModel = hiltViewModel<DataManagerViewModel>()
    val context = LocalContext.current
    val dynamicColor by SettingsPreferences.dynamicColor.collectAsState(false)
    val scope = rememberCoroutineScope()
    val themeModePopupMenuState = rememberPopupState()
    val languageModePopupMenuState = rememberPopupState()
    val themeMode by SettingsPreferences.themeMode.collectAsState(SettingsPreferences.ThemeMode.SYSTEM)
    val settingsViewModel = hiltViewModel<SettingsViewModel>()
    val biometricAuthState by settingsViewModel.biometricAuthState.collectAsState()
    val successMsg = stringResource(R.string.execute_success)
    val settingList = listOf(
        SettingsBean(R.string.random_walk, Icons.Outlined.Explore) { navController.navigate(Screen.RandomWalk)},
        SettingsBean(R.string.gallery, Icons.Outlined.Photo) { navController.navigate(Screen.Gallery) }
    )
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = {
            item(content = {
                HeatContent()
            })

            item(content = {
                SettingsHeadLayout()
            })

            item {
                RoundedColumn{
                    ItemTitle(text = stringResource(R.string.user_interface))
                    if (DynamicColors.isDynamicColorAvailable()) {
                        ItemSwitcher(
                            state = dynamicColor,
                            onChange = { checked ->
                                scope.launch {
                                    SettingsPreferences.changeDynamicColor(checked)
                                }
                            },
                            text = stringResource(R.string.dynamic_color_switcher_text),
                            sub = stringResource(R.string.dynamic_color_switcher_sub),
                            iconPainter = painterResource(id = R.drawable.color),
                            iconPaddingValues = PaddingValues(all = 1.7.dp),
                            iconColor = SaltTheme.colors.text
                        )
                    }
                    ItemPopup(
                        state = themeModePopupMenuState,
                        iconPainter = painterResource(id = R.drawable.app_theme),
                        iconPaddingValues = PaddingValues(all = 1.8.dp),
                        iconColor = SaltTheme.colors.text,
                        text = stringResource(R.string.theme_mode_switcher_text),
                        selectedItem = stringResource(id = themeMode.resId),
                        popupWidth = 140
                    ) {
                        val options = SettingsPreferences.ThemeMode.entries.map { stringResource(id = it.resId) }
                        var selectedIndex by remember { mutableIntStateOf(SettingsPreferences.ThemeMode.entries.indexOf(themeMode)) }
                        options.forEachIndexed { index, label ->
                            PopupMenuItem(
                                onClick = {
                                    selectedIndex = index
                                    scope.launch {
                                        SettingsPreferences.changeThemeMode(SettingsPreferences.ThemeMode
                                            .entries[index])
                                    }
                                    themeModePopupMenuState.dismiss()
                                },
                                selected = selectedIndex == index,
                                text = label,
                                iconColor = SaltTheme.colors.text
                            )
                        }
                    }
                    ItemPopup(
                        state = languageModePopupMenuState,
                        iconPainter = rememberVectorPainter(Icons.Outlined.Translate),
                        iconPaddingValues = PaddingValues(all = 1.8.dp),
                        iconColor = SaltTheme.colors.text,
                        text = stringResource(R.string.language_mode_switcher_text),
                        selectedItem = stringResource(id = languageMode.resId),
                        popupWidth = 140
                    ) {
                        val options = SettingsPreferences.LanguageMode.entries.map { stringResource(id = it.resId) }
                        var selectedIndex by remember { mutableIntStateOf(SettingsPreferences.LanguageMode.entries.indexOf
                            (languageMode)) }
                        options.forEachIndexed { index, label ->
                            PopupMenuItem(
                                onClick = {
                                    selectedIndex = index
                                    val selectedMode = SettingsPreferences.LanguageMode.entries[index]
                                    scope.launch {
                                        SettingsPreferences.changeLanguageMode(selectedMode)
                                    }
                                    languageModePopupMenuState.dismiss()
                                },
                                selected = selectedIndex == index,
                                text = label,
                                iconColor = SaltTheme.colors.text
                            )
                        }
                    }

                }
            }
            item {
                RoundedColumn{
                    ItemTitle(text = stringResource(R.string.safe))
                    ItemSwitcher(
                        state = biometricAuthState,
                        iconPainter = rememberVectorPainter(Icons.Outlined.Fingerprint),
                        iconColor = SaltTheme.colors.text,
                        onChange = {
                            settingsViewModel.showBiometricPrompt(context as MainActivity)
                        },
                        text = stringResource(R.string.biometric)
                    )
                    Item(
                        onClick = {
                            navController.navigate(Screen.DataManager)
                        },
                        text = stringResource(R.string.local_data_manager),
                        iconPainter = painterResource(R.drawable.ic_database)
                    )
                    settingList.forEach { item ->
                        Item(
                            onClick = {
                                item.onClick()
                            },
                            text = stringResource(item.title),
                            iconPainter = rememberVectorPainter(item.imageVector)
                        )
                    }
                    Item(
                        onClick = {
                            lunchIo {
                                dataViewModel.fixTag()
                                toast(successMsg)
                            }
                        },
                        text = stringResource(R.string.tag_fix),
                        iconPainter = rememberVectorPainter(Icons.Outlined.Label),
                    )
                }
            }
        }
    )
}

