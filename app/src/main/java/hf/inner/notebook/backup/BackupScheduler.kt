package hf.inner.notebook.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BackupScheduler {
    companion object {
        private const val BACKUP_WORK_TAG = "backup_work"

        fun scheduleDailyBackup(context: Context) {
            // 定义约束：仅在 Wi-Fi 连接且充电时执行
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .build()
            // 周期性请求
            val workRequest = PeriodicWorkRequestBuilder<BackupWorker>(3, TimeUnit.DAYS)
                .addTag(BACKUP_WORK_TAG)
                .build()

            // 提交唯一任务
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                BACKUP_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP, // 如果系统中已经存在一个带有 BACKUP_WORK_TAG 的任务，则保留旧任务，忽略这次新的请求。
                workRequest
            )
        }

        fun cancelDailyBackup(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(BACKUP_WORK_TAG)
        }
    }
}