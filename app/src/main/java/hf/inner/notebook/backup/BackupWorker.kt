package hf.inner.notebook.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import hf.inner.notebook.utils.BackUp
import hf.inner.notebook.utils.SharedPreferencesUtils
import hf.inner.notebook.utils.backUpFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BackupWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    @Inject
    lateinit var syncManager: SyncManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // 目录树
        SharedPreferencesUtils.localBackupUri.first()?.let {
            val uri = Uri.parse(it)
            // DocumentFile.fromTreeUri: 把一串抽象的“目录 Uri（树状 Uri）”转换成一个具体的、可以执行读写操作的“文件夹对象”。
            // requireNotNull: 非空校验函数
            val folder = requireNotNull(DocumentFile.fromTreeUri(context, uri))
            val file = requireNotNull(folder.createFile("application/zip", "Auto".plus(backUpFileName)))
            BackUp.exportEncrypted(context, file.uri)
            Result.success()
        } ?: Result.failure()
    }
}