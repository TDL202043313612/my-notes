package hf.inner.notebook.backup

import android.content.Context
import android.util.Log
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import hf.inner.notebook.R
import hf.inner.notebook.backup.model.DavData
import hf.inner.notebook.utils.SharedPreferencesUtils
import hf.inner.notebook.utils.str
import hf.inner.notebook.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SyncManager(
    private val context: Context
) {

    private suspend fun getSardine(): OkHttpSardine {
        val sardine = OkHttpSardine()
        sardine.setCredentials(SharedPreferencesUtils.davUserName.first(), SharedPreferencesUtils.davPassword.first(), true)
        return sardine
    }
    suspend fun checkConnection(url: String, account: String, pwd: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val sardine = OkHttpSardine()
        sardine.setCredentials(account, pwd, true)
        return@withContext try {
            sardine.exists(url)
            SharedPreferencesUtils.updateDavServerUrl(url)
            SharedPreferencesUtils.updateDavUserName(account)
            SharedPreferencesUtils.updateDavPassword(pwd)
            SharedPreferencesUtils.updateDavLoginSuccess(true)
            Pair(true, R.string.webdav_config_success.str)

        } catch (e: SardineException) {
            e.printStackTrace()
            SharedPreferencesUtils.clearDavConfig()
            Pair(false, e.message.toString())
        }
    }

    suspend fun downloadFileByPath(webPath: String, localDir: String): String ? = withContext(Dispatchers.IO) {
        try {
            val davServerUrl = SharedPreferencesUtils.davServerUrl.first()
            val sardine = getSardine()
            val fileName = webPath.substringAfterLast("/")
            val localPath = File(localDir, fileName).path
            Log.i("wutao", "downloadFileByPath: $davServerUrl$webPath")
            sardine.get(davServerUrl + webPath).use { inputStream ->
                FileOutputStream(localPath).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        outputStream.flush()
                    }
                }
            }
            localPath
        } catch (e: Exception) {
            toast(e.message.toString())
            e.printStackTrace()
            null
        }
    }
    suspend fun uploadFile(fileName: String?, fileDir: String, localFile: File?): String = withContext(Dispatchers.IO) {
        try {
            val sardine = getSardine()
            val serverUrl = SharedPreferencesUtils.davServerUrl.first()
            ensureDirectoryExists(sardine, serverUrl + fileDir)
            val url = "$serverUrl$fileDir/$fileName"
            if (sardine.exists(url)) {
                sardine.delete(url)
            }
            sardine.put(url, localFile, "application/x-www-form-urlencoded")
            "Success：$fileDir/$fileName"
        }catch (e: Exception) {
            e.printStackTrace()
            e.message.toString()
        }
    }

    suspend fun listAllFile(dir: String?): List<DavData> = withContext(Dispatchers.IO) {
        try {
            val sardine = getSardine()
            val davServerUrl = SharedPreferencesUtils.davServerUrl.first()
            val resources = sardine.list(davServerUrl + dir) // 如果是目录一定别忘记在后面加上一个斜杠
            val davData: MutableList<DavData> = ArrayList()
            for (i in resources) {
                davData.add(DavData(i))
            }
            davData
        }catch (e: Exception) {
            e.printStackTrace()
            toast(e.message.toString())
            emptyList()
        }
    }

    private fun ensureDirectoryExists(sardine: OkHttpSardine, dirUrl: String) {
        if (!sardine.exists(dirUrl)) {
            sardine.createDirectory(dirUrl)
        }
    }
}
