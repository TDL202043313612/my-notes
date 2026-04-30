package hf.inner.notebook.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class ExportMarkDownContract(val name: String): ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(
        context: Context,
        input: Unit,
    ): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            // MIME 类型为 Markdown
            type = "text/markdown"
            putExtra(Intent.EXTRA_TITLE, "$name.md")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }

}


object RestoreNotesContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }

}
object ChoseFolderContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        // 选文件夹: 系统授权你的App访问则会个文件夹及其内部所有的内容【无限次创建、删除、修改文件的权力】
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }
}

object ExportNotesJsonContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "IdeaMemo.json")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }

}

object ExportTextContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "IdeaMemo.text")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }

}

object ExportHtmlContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
            putExtra(Intent.EXTRA_TITLE, "IdeaMemoHtml.zip")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }

}

object ImportHtmlZipContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
        }    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }

}

