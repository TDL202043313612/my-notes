package hf.inner.notebook.page.data

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.FormatColorText
import androidx.compose.material.icons.outlined.Javascript
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemEdit
import com.moriafly.salt.ui.ItemEditPassword
import com.moriafly.salt.ui.ItemOutHalfSpacer
import com.moriafly.salt.ui.ItemOutSpacer
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TextButton
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.dialog.BasicDialog
import hf.inner.notebook.App
import hf.inner.notebook.R
import hf.inner.notebook.backup.model.DavData
import hf.inner.notebook.component.ConfirmDialog
import hf.inner.notebook.component.LoadingComponent
import hf.inner.notebook.component.RYDialog
import hf.inner.notebook.page.viewmodel.LocalMemosState
import hf.inner.notebook.page.home.clickable
import hf.inner.notebook.page.router.debouncedPopBackStack
import hf.inner.notebook.page.settings.SettingsBean
import hf.inner.notebook.utils.BackUp
import hf.inner.notebook.utils.ChoseFolderContract
import hf.inner.notebook.utils.ExportHtmlContract
import hf.inner.notebook.utils.ExportMarkDownContract
import hf.inner.notebook.utils.ExportNotesJsonContract
import hf.inner.notebook.utils.ExportTextContract
import hf.inner.notebook.utils.ImportHtmlZipContract
import hf.inner.notebook.utils.RestoreNotesContract
import hf.inner.notebook.utils.SharedPreferencesUtils
import hf.inner.notebook.utils.backUpFileName
import hf.inner.notebook.utils.lunchMain
import hf.inner.notebook.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(UnstableSaltApi::class)
@Composable
fun DataManagerPage(
    navController: NavHostController,
    viewModel: DataManagerViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val noteState = LocalMemosState.current
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current as ComponentActivity
    val snackbarState = remember { SnackbarHostState() }
    var isShowRestartDialog by remember { mutableStateOf(false) }
    var showChoseFolderDialog by remember { mutableStateOf(false) }
    var webInputDialog: Boolean by remember { mutableStateOf(false) }
    val autoBackSwitchState = SharedPreferencesUtils.localAutoBackup.collectAsState(false)
    val jianGuoCloudSwitchState = SharedPreferencesUtils.davLoginSuccess.collectAsState(false)
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val webDavList = remember { mutableListOf<DavData>() }
    val isLogin = viewModel.isLogin
    val successMsg = stringResource(R.string.execute_success)
    fun navToWebdavConfigPage() {
//        navController.navigate(Screen.DataCloudConfig)
    }

    fun exportToWebdav() {
        if (isLogin) {
            navToWebdavConfigPage()
            return
        }
        scope.launch {
            isLoading = true
            val resultStr = viewModel.exportToWebdav(context)
            isLoading = false
            snackbarState.showSnackbar(resultStr)
        }
    }

    fun restoreForWebdav() {
        if (isLogin) {
            navToWebdavConfigPage()
            return
        }
        scope.launch {
            isLoading = true
            val list = viewModel.restoreForWebdav()
            webDavList.clear()
            webDavList.addAll(list)
            isLoading = false
            openBottomSheet = true
        }
    }

    val encryptedExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument()
    ) { uri ->
        if (uri != null){
            scope.launch {
                isLoading = true
                withContext(Dispatchers.IO) {
                    Log.d("BackUp", BackUp.exportEncrypted(context, uri))
                }
                isLoading = false
                snackbarState.showSnackbar(successMsg)
            }
        }
    }

    val restoreEncryptFromSdLauncher = rememberLauncherForActivityResult(RestoreNotesContract) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isLoading = true
            BackUp.restoreFromEncryptedZip(context, uri, true)
            isLoading = false
            isShowRestartDialog = true
        }
    }

    val exportNotesJsonLauncher = rememberLauncherForActivityResult(ExportNotesJsonContract) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                BackUp.exportJson(noteState.notes, uri)
            }
            snackbarState.showSnackbar(successMsg)
        }
    }

    val exportTxtLauncher = rememberLauncherForActivityResult(ExportTextContract) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            withContext(Dispatchers.IO) {
                BackUp.exportTXTFile(noteState.notes, uri)
            }
            snackbarState.showSnackbar(successMsg)
        }
    }
    val exportMarkDownLauncher = rememberLauncherForActivityResult(ExportMarkDownContract("IdeaMemo")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch(Dispatchers.IO) {
            BackUp.exportMarkDownFile(noteState.notes, uri)
            withContext(Dispatchers.Main) {
                snackbarState.showSnackbar(successMsg)
            }
        }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isLoading = true
                withContext(Dispatchers.IO) {
                    BackUp.export(context, uri)
                }
                isLoading = false
                snackbarState.showSnackbar(successMsg)
            }
        }
    }
    val restoreNoEncryLauncher = rememberLauncherForActivityResult(RestoreNotesContract) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        lunchMain {
            isLoading = true
            BackUp.restoreFromSd(uri)
            isLoading = false
            isShowRestartDialog = true
        }
    }
    val choseFolderLauncher = rememberLauncherForActivityResult(ChoseFolderContract) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            SharedPreferencesUtils.updateLocalBackUri(uri.toString())
            SharedPreferencesUtils.updateLocalAutoBackup(true)
        }
    }

    val exportHtmlLauncher = rememberLauncherForActivityResult(ExportHtmlContract) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isLoading = true
            withContext(Dispatchers.IO) {
                BackUp.exportHtmlZip(noteState.notes, uri)
            }
            isLoading = false
        }
    }

    val importHtmlZipLauncher = rememberLauncherForActivityResult(ImportHtmlZipContract) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isLoading = true
            val result = BackUp.importFromHtmlZip(context, uri)
            isLoading = false
            result.onSuccess { count ->
                snackbarState.showSnackbar(context.getString(R.string.import_notes_count, count))
            }
        }
    }
    val localDataList = listOf(
        SettingsBean(R.string.data_backup, Icons.Outlined.PrivacyTip) {
            encryptedExportLauncher.launch(backUpFileName)
        },
        SettingsBean(R.string.data_restore, Icons.Outlined.Restore) {
            restoreEncryptFromSdLauncher.launch(Unit)
        },
        SettingsBean(R.string.data_backup_no_encr, Icons.Outlined.SaveAlt) {
            exportLauncher.launch("IdeaMNoEncrypt.zip")
        },
        SettingsBean(R.string.data_restore_no_encr, Icons.Outlined.FileUpload) {
            restoreNoEncryLauncher.launch(Unit)
        },
        SettingsBean(R.string.json_export, Icons.Outlined.Javascript) {
            exportNotesJsonLauncher.launch(Unit)
        },
        SettingsBean(R.string.txt_export, Icons.Outlined.TextFields) {
            exportTxtLauncher.launch(Unit)
        },
        SettingsBean(R.string.mk_export, Icons.Outlined.FormatColorText) {
            exportMarkDownLauncher.launch(Unit)
        },
        SettingsBean(R.string.html_export, Icons.Outlined.FileDownload) {
            exportHtmlLauncher.launch(Unit)
        },
        SettingsBean(R.string.html_import, Icons.Outlined.FileUpload) {
            importHtmlZipLauncher.launch(Unit)
        },
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SaltTheme.colors.background)
    ) {
        Spacer(Modifier.height(30.dp))
        TitleBar(onBack = {
            navController.debouncedPopBackStack()
        },
            text = stringResource(R.string.local_data_manager)
        )
        RoundedColumn {
            localDataList.forEach { it ->
                Item(
                    onClick = {
                        it.onClick()
                    },
                    text = stringResource(it.title),
                    iconPainter = rememberVectorPainter(it.imageVector)
                )
            }
            val localBackUri = SharedPreferencesUtils.localBackupUri.collectAsState("")
            ItemSwitcher(
                state = autoBackSwitchState.value,
                onChange = {
                    if (localBackUri.value.isNullOrEmpty()) {
                        showChoseFolderDialog = true
                    } else {
                        val isChecked = autoBackSwitchState.value
                        if (isChecked) {
                            scope.launch {
                                SharedPreferencesUtils.updateLocalAutoBackup(false)
                                SharedPreferencesUtils.updateLocalBackUri(null)
                            }
                        }
                    }
                },
                iconColor = SaltTheme.colors.highlight,
                text = stringResource(R.string.title_local_auto_backup)
            )
        }
        if (webInputDialog) {
            AccountInputDialog(
                onDismissRequest = {
                    webInputDialog = false
                },
                onConfirm = {
                    webInputDialog = false
                    scope.launch {
                        SharedPreferencesUtils.updateDavLoginSuccess(true)
                    }
                },
            )
        }

        RoundedColumn {
            ItemSwitcher(
                text = stringResource(R.string.webdav_auth),
                state = jianGuoCloudSwitchState.value,
                onChange = {
                    if (it) {
                        webInputDialog = true
                    } else {
                        scope.launch {
                            SharedPreferencesUtils.clearDavConfig()
                        }
                    }
                }
            )
            if (jianGuoCloudSwitchState.value) {
                Item(text = stringResource(R.string.webdav_backup), onClick = {
                    exportToWebdav()
                })
                Item(text = stringResource(R.string.webdav_restore), onClick = {
                    restoreForWebdav()
                })
            }
        }
    }
    LoadingComponent(isLoading)
    ChoseFolderDialog(
        visible = showChoseFolderDialog,
        onDismissRequest = {
            showChoseFolderDialog = false
        }, onConfirmRequest = {
            choseFolderLauncher.launch(Unit)
            showChoseFolderDialog = false
        }
    )

    ConfirmDialog(
        isShowRestartDialog,
        title = stringResource(R.string.restart),
        content = stringResource(R.string.app_restored),
        onDismissRequest = {
            isShowRestartDialog = false
        }, onConfirmRequest = {
           isShowRestartDialog = false
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage(context.packageName)
            val componentName = intent!!.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            context.startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }
    )
    WebRestoreBottomSheet(show = openBottomSheet, webDavList, onDismissRequest = {
            openBottomSheet = false
        }, onConfirmRequest = { davData ->
            scope.launch {
                openBottomSheet = false
                isLoading = true
                val resultPath = viewModel.downloadFileByPath(davData)
                if (!resultPath.isNullOrEmpty()) {
                    val uri = FileProvider.getUriForFile(context, context.packageName+".provider", File(resultPath))
                    BackUp.restoreFromEncryptedZip(App.instance, uri, true)
                    isLoading = false
                    isShowRestartDialog = true
                }
            }
        }
    )

}

@OptIn(UnstableSaltApi::class)
@Composable
fun AccountInputDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    properties: DialogProperties = DialogProperties()
) {
    val scope = rememberCoroutineScope()
    BasicDialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        ItemOutSpacer()
        ItemOutHalfSpacer()
        ItemTitle(text = stringResource(R.string.webdav_config))

        val serverUrl = SharedPreferencesUtils.davServerUrl.collectAsState("")
        val userName = SharedPreferencesUtils.davUserName.collectAsState(null)
        val password = SharedPreferencesUtils.davPassword.collectAsState(null)
        val dataManagerViewMode: DataManagerViewModel = hiltViewModel()
        ItemEdit(
            text = serverUrl.value ?: "",
            onChange = {
                scope.launch {
                    SharedPreferencesUtils.updateDavServerUrl(it)
                }
            },
            hint = stringResource(R.string.server_url)
        )

        ItemEdit(
            text = userName.value ?: "",
            onChange = {
                scope.launch {
                    SharedPreferencesUtils.updateDavUserName(it)
                }
            },
            hint = stringResource(R.string.username)
        )

        ItemEditPassword(
            text = password.value ?: "",
            onChange = {
                scope.launch {
                    SharedPreferencesUtils.updateDavPassword(it)
                }
            },
            hint = stringResource(R.string.password)
        )
        ItemOutHalfSpacer()
        Row(
            modifier = Modifier.padding(horizontal = SaltTheme.dimens.outerHorizontalPadding)
        ) {
            TextButton(
                onClick = {
                    onDismissRequest()
                },
                modifier = Modifier
                    .weight(1f),
                text = stringResource(R.string.cancel),
                textColor = SaltTheme.colors.subText,
                backgroundColor = SaltTheme.colors.subBackground
            )
            Spacer(modifier = Modifier.width(SaltTheme.dimens.contentPadding))
            TextButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val pair = dataManagerViewMode.checkConnection(serverUrl.value!!, userName.value!!, password
                            .value!!)
                        withContext(Dispatchers.Main) {
                            toast(pair.second)
                            scope.launch {
                                SharedPreferencesUtils.updateDavLoginSuccess(pair.first)
                                if (pair.first) {
                                    onConfirm()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f),
                text = stringResource(R.string.submit),
            )
        }
        ItemOutSpacer()
    }
}


@Composable
fun ChoseFolderDialog(visible: Boolean, onDismissRequest: () -> Unit, onConfirmRequest: () -> Unit) {
    RYDialog(
        visible = visible,
        properties = DialogProperties(),
        title = {
            Text(text = stringResource(R.string.choose_folder))
        },
        text = {
            Text(text = stringResource(R.string.notes_will_be))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmRequest()
                }
            ) {
                Text(stringResource(R.string.choose))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebRestoreBottomSheet(show: Boolean, list: List<DavData>, onDismissRequest: () -> Unit, onConfirmRequest: (data: DavData) -> Unit) {
    if (show) {
        ModalBottomSheet(onDismissRequest = onDismissRequest) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                LazyRow {
                    items(list.size) { index ->
                        ListItem(headlineContent = { Text(list[index].displayName) }, modifier = Modifier.clickable {
                            onConfirmRequest(list[index])
                        })
                    }
                }
            }
        }
    }
}


