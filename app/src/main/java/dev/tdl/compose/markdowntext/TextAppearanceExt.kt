package dev.tdl.compose.markdowntext

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.ExtraBold
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.font.resolveAsTypeface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnNextLayout
import androidx.core.widget.TextViewCompat
import hf.inner.notebook.utils.TopicUtils

fun TextView.applyFontResource(@FontRes font: Int) {
    typeface = ResourcesCompat.getFont(context, font)
}
fun TextView.applyTextColor(argbColor: Int) {
    setTextColor(argbColor)
}

fun TextView.applyFontSize(textStyle: TextStyle) {
    setTextSize(TypedValue.COMPLEX_UNIT_SP, textStyle.fontSize.value)
}

fun TextView.applyLineHeight(textStyle: TextStyle) {
    if (textStyle.lineHeight.isSp) {
        TextViewCompat.setLineHeight(
            this,
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                textStyle.lineHeight.value,
                context.resources.displayMetrics
            ).toInt()
        )
    }
}
fun TextView.applyTextDecoration(textStyle: TextStyle) {
    // 是否包含删除线
    if (textStyle.textDecoration == TextDecoration.LineThrough) {
        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }
}

fun TextView.applyTextAlign(align: TextAlign) {
    // 水平对齐
    gravity = when(align) {
        TextAlign.Left, TextAlign.Start -> Gravity.START
        TextAlign.Right, TextAlign.End -> Gravity.END
        TextAlign.Center -> Gravity.CENTER_HORIZONTAL
        else -> Gravity.START
    }
    // 两端对齐
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && align == TextAlign.Justify) {
        justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
    }
}

fun TextView.applyFontStyle(fontStyle: FontStyle) {
    val type = when(fontStyle) {
        FontStyle.Italic -> Typeface.ITALIC
        FontStyle.Normal -> Typeface.NORMAL
        else -> Typeface.NORMAL
    }
    // 在保留原有字体家族的前提下，改变它的胖瘦或倾斜度。
    // setTypeface(null, Typeface.ITALIC)，系统可能会把你的自定义字体丢掉，变回默认的系统字体。再变为斜体
    setTypeface(typeface, type)
}

fun TextView.applyFontWeight(fontWeight: FontWeight) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        typeface = Typeface.create(typeface, fontWeight.weight, false)
    } else {
        val weight = when(fontWeight) {
            ExtraBold, Bold, SemiBold -> Typeface.BOLD
            else -> Typeface.NORMAL
        }
        setTypeface(typeface, weight)
    }
}
fun TextView.applyFontFamily(fontFamily: FontFamily) {
    typeface = createFontFamilyResolver(context).resolveAsTypeface(fontFamily).value
}

fun TextView.highlightTagsWithClick(onTagClick: ((String) -> Unit)?, isTextSelectable: Boolean) {
    val originalText = this.text.toString()
    val matcher = TopicUtils.pattern.matcher(originalText)
    val foundTags = mutableListOf<Pair<Int, Int>>()
    while (matcher.find()) {
        foundTags.add(Pair(matcher.start(), matcher.end()))
    }
    if (foundTags.isEmpty()) return
    val spannableString = SpannableString(originalText)
    val textColor = Color.parseColor("#4D84F7")
    for (tagRange in foundTags) {
        val startIndex = tagRange.first
        val endIndex = tagRange.second
        val tagText = originalText.substring(startIndex, endIndex)

        // tag设置颜色为蓝色
        // SPAN_EXCLUSIVE_EXCLUSIVE startIndex 之前插入文字，新文字不会变色 endIndex 之后插入文字，新文字不会变色。
        spannableString.setSpan(
            ForegroundColorSpan(textColor),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(p0: View) {
                onTagClick?.invoke(tagText)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = textColor
                ds.isUnderlineText = false
            }
        }, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    this.text = spannableString
    //
    if (!isTextSelectable) {
        this.movementMethod = LinkMovementMethod.getInstance()
        this.highlightColor = Color.TRANSPARENT
    }
}
fun TextView.enableTextOverflow() {
    // 在 TextView 还没经过系统的“测量”和“布局”之前，它是不知道自己到底有多少行（lineCount）的
    // 这段逻辑会等到 TextView 确定了自己的宽高、排好了版之后才触发。
    doOnNextLayout {
        if (maxLines != -1 && lineCount > maxLines) {
            // endOfLastLine：通过 Layout 引擎找到最后一行末尾那个字符的索引位置
            val endOfLastLine = layout.getLineEnd(maxLines -1)
            // 三个点 ... 留出位置
            val startIndex = maxOf(0, endOfLastLine - 3)
            val spannedDropLast3Chars = text.subSequence(0, startIndex) as? Spanned
            if (spannedDropLast3Chars != null) {
                val spannableBuilder = SpannableStringBuilder()
                    .append(spannedDropLast3Chars)
                    .append("…")
                text = spannableBuilder
            }
        }
    }
}