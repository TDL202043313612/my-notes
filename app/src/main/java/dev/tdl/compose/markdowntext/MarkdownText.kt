package dev.tdl.compose.markdowntext

import android.content.Context
import android.os.Build
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import androidx.annotation.FontRes
import androidx.annotation.IdRes
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.TextViewCompat
import coil.ImageLoader
import hf.inner.notebook.page.home.clickable
import io.noties.markwon.Markwon
import java.util.regex.Pattern

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    linkColor: Color = Color.Unspecified,
    truncateOnTextOverflow: Boolean = false,
    style: TextStyle = LocalTextStyle.current,
    @IdRes viewId: Int? = null,
    @FontRes fontResource: Int? = null,
    maxLines: Int = Int.MAX_VALUE,
    isTextSelectable: Boolean = false,
    autoSizeConfig: AutoSizeConfig? = null,

    onClick: (() -> Unit)? = null,
    // markdown
    disableLinkMovementMethod: Boolean = false,
    imageLoader: ImageLoader? = null,
    linkifyMask: Int = Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS or Linkify.WEB_URLS,
    enableSoftBreakAddsNewLine: Boolean = true,
    onLinkClicked: ((String) -> Unit)? = null,
    onTextLayout: ((numLines: Int) -> Unit)? = null,
    onTagClick: ((String) -> Unit)? = null
) {
    val defaultColor: Color = LocalContentColor.current
    val context: Context = LocalContext.current
    val markdownRender: Markwon =
        remember {
            MarkdownRender.create(
                context,
                imageLoader = imageLoader,
                linkifyMask = linkifyMask,
                enableSoftBreakAddsNewLine = enableSoftBreakAddsNewLine,
                onLinkClicked
            )
        }

    val androidViewModifier = if (onClick != null) {
        Modifier
            .clickable { onClick() }
            .then(modifier)
    } else {
        modifier
    }

    AndroidView(
        modifier = androidViewModifier,
        // 一次性初始化
        factory = { factoryContext ->
            val linkTextColor = linkColor.takeOrElse { style.color.takeOrElse { defaultColor } }
            CustomTextView(factoryContext).apply {
                viewId?.let { id = it }
                fontResource?.let { font -> applyFontResource(font) }
                setMaxLines(maxLines)
                setLinkTextColor(linkTextColor.toArgb())
                setTextIsSelectable(isTextSelectable)
                if (!isTextSelectable) {
                    // 可选中文本 -》则激活链接点击探测器
                    movementMethod = LinkMovementMethod.getInstance()
                }
                if (truncateOnTextOverflow) enableTextOverflow()
                // 字体大小自动缩放
                autoSizeConfig?.let { config ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                            this,
                            config.autoSizeMinTextSize,
                            config.autoSizeMaxTextSize,
                            config.autoSizeStepGranularity,
                            config.unit
                        )
                    }
                }
            }
        },
        update = { textView ->
            with(textView) {
                applyTextColor(style.color.takeOrElse { defaultColor }.toArgb())
                applyFontSize(style)
                applyLineHeight(style)
                applyTextDecoration(style)

                with(style) {
                    applyTextAlign(textAlign)
                    fontStyle?.let { applyFontStyle(it) }
                    fontWeight?.let { applyFontWeight(it) }
                    fontFamily?.let { applyFontFamily(it) }
                }
            }
            markdownRender.setMarkdown(textView, markdown)
            if (disableLinkMovementMethod) {
                // 禁用链接逻辑
                textView.movementMethod = null
            } else if (isTextSelectable) {
                // 文字可选-》禁止使用处理触摸事件
                if (textView.movementMethod is LinkMovementMethod) {
                    textView.movementMethod = null
                    textView.setTextIsSelectable(true)
                }
            }
            if (onTextLayout != null) {
                textView.post {
                    onTextLayout(textView.lineCount)
                }
            }
            textView.maxLines = maxLines
            textView.highlightTagsWithClick(onTagClick, isTextSelectable)
        }
    )
}
//object TopicUtils {
//    private const val inputReg = "(\\#[\u4e00-\u9fa5a-zA-Z]+\\d{0,100})[\\w\\s]"
//    val pattern: Pattern = Pattern.compile(inputReg)
//
//    fun getTopicListByString(text: String): List<String> {
//        val tagList: MutableList<String> = mutableListOf()
//        val matcher = pattern.matcher(text)
//        while (matcher.find()) {
//            val tag = text.substring(matcher.start(), matcher.end()).trim { it <= ' ' }
//            tagList.add(tag)
//        }
//        return tagList
//    }
//}