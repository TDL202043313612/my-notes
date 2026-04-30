package hf.inner.notebook.page.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import hf.inner.notebook.App
import hf.inner.notebook.backup.SyncManager
import hf.inner.notebook.backup.model.DavData
import hf.inner.notebook.db.repo.TagNoteRepo
import hf.inner.notebook.getAppName
import hf.inner.notebook.utils.BackUp
import hf.inner.notebook.utils.SharedPreferencesUtils
import hf.inner.notebook.utils.backUpFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DataManagerViewModel @Inject constructor(
    private val tagNoteRepo: TagNoteRepo,
    private val syncManager: SyncManager
) : ViewModel() {
    val isLogin = SharedPreferencesUtils.davLoginSuccess.asLiveData().value ?: false

    suspend fun checkConnection(url: String, account: String, pwd: String): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            syncManager.checkConnection(url, account, pwd)
        }
    suspend fun downloadFileByPath(davData: DavData): String? = withContext(Dispatchers.IO) {
        syncManager.downloadFileByPath(
            davData.path.substringAfterLast("/dav/"),
            App.instance.cacheDir.absolutePath
        )
    }

    suspend fun fixTag() = withContext(Dispatchers.IO) {
        val dataList = tagNoteRepo.queryAllNoteList()
        dataList.forEach { note ->
            tagNoteRepo.insertOrUpdate(note)
        }
        tagNoteRepo.queryAllTagList().forEach { tag ->
            val count = tagNoteRepo.countNoteListWithByTag(tag.tag)
            tag.count = count
            tagNoteRepo.updateTag(tag)
        }
    }
    suspend fun exportToWebdav(context: Context): String = withContext(Dispatchers.IO) {
        val (filename, file, _) = generateZipFile(context, backUpFileName)
        val resultStr = syncManager.uploadFile(filename, getAppName(), file)
        if (resultStr.startsWith("Success")) {
            File(filename).deleteRecursively()
        }
        resultStr
    }

    suspend fun restoreForWebdav(): List<DavData> = withContext(Dispatchers.IO) {
        val dataList = syncManager.listAllFile(getAppName() + "/").filterNotNull().filter {
            it.name.endsWith(".zip")
        }.sortedByDescending { it.name }
        dataList

    }

    private suspend fun generateZipFile(context: Context, fileName: String): Triple<String, File, Uri> =
        withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, fileName)
            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            val fileName = BackUp.exportEncrypted(context, uri)
            Triple(fileName, file, uri)
        }
}