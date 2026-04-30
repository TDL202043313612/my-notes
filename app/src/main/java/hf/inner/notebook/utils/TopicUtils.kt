package hf.inner.notebook.utils

import android.media.MediaCodec
import hf.inner.notebook.bean.Tag
import java.util.regex.Matcher
import java.util.regex.Pattern

object TopicUtils {
    // #开头，后面跟着汉字、字母、数字或者/
    private val inputReg = "\\#[\u4e00-\u9fa5a-zA-Z0-9/]+"
    val pattern = Pattern.compile(inputReg)

    /**
     * 支持多级标签，例如 #深圳/保安 会被拆分为
     * 1. #深圳
     * 2. #深圳/海边
     * 这样在数据库中，这条笔记会同时关联到“深圳”和“深圳/海边”两个标签。
     */
    fun getTopicListByString(text: String): List<Tag> {
        val tagSet = mutableSetOf<String>()
        val matcher: Matcher = pattern.matcher(text)
        while (matcher.find()) {
            val fullTag = matcher.group().trim()
            if (fullTag.contains("/")) {
                // 处理多级标签，如 #A/B/C -> 产生 #A, #A/B, #A/B/C
                val parts = fullTag.split("/")
                var currentPath = ""
                parts.forEachIndexed { index, part ->
                    currentPath = if (index == 0) part else "$currentPath/$part"
                    if (currentPath.isNotBlank() && currentPath != "#") {
                        tagSet.add(currentPath)
                    }
                }
            } else {
                if (fullTag.isNotBlank() && fullTag != "#") {
                    tagSet.add(fullTag)
                }
            }
        }
        return tagSet.map { Tag(it) }
    }
}