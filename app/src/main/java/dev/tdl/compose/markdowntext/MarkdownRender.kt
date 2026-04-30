package dev.tdl.compose.markdowntext

import android.content.Context
import coil.ImageLoader
import coil.imageLoader
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.SoftBreakAddsNewLinePlugin
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin

internal object MarkdownRender {
    fun create(
        context: Context,
        imageLoader: ImageLoader?,
        linkifyMask: Int,
        enableSoftBreakAddsNewLine: Boolean,
        onLinkClicked: ((String) -> Unit)? = null,
    ): Markwon {
        val coilImageLoader = imageLoader ?: context.imageLoader
        return Markwon.builder(context)
            .usePlugin(HtmlPlugin.create())
            .usePlugin(CoilImagesPlugin.create(context, coilImageLoader))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(LinkifyPlugin.create(linkifyMask))
            .apply {
                if (enableSoftBreakAddsNewLine) {
                    usePlugin(SoftBreakAddsNewLinePlugin.create())
                }
            }
            .usePlugin(object : AbstractMarkwonPlugin() {
                // 拦截 Markdown 中所有超链接的点击事件，并将其交给你的 App 自己来处理
                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    onLinkClicked ?: return
                    builder.linkResolver { _, link ->
                        onLinkClicked.invoke(link)
                    }
                }
            })
            .build()


    }
}