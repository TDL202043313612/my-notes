package dev.tdl.compose.markdowntext

import android.content.Context
import android.text.Spannable
import android.text.style.ClickableSpan
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView

class CustomTextView(context: Context) : AppCompatTextView(context) {
    // 选词模式

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 重写onTouchEvent需要调用performClick()
        // 无障碍服务的唯一入口、setOnClickListener
        if (isTextSelectable) {
            // 在可选择模式下，首先调用super.onTouchEvent 处理系统自带的选中逻辑
            val superResult =  super.onTouchEvent(event)
            // 如果是手指抬起动作，且当前没有文字被选中，则尝试触发连接点击
            // hasSelection: 是否存在选中的文本
            if (event.action == MotionEvent.ACTION_UP && !hasSelection()){
                val link = getClickableSpans(event)
                if (link.isNotEmpty()) {
                    link[0].onClick(this)
                    return true
                }
            }
            // 当有选中文本则返回 superResult
            return superResult
        } else {
            // 非选择模式下的原有逻辑
            performClick()
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_DOWN) {
                val link = getClickableSpans(event)
                if (link.isNotEmpty()) {
                    if (event.action == MotionEvent.ACTION_UP) {
                        // 即使有多个，我们也只执行第一个被发现的点击逻辑
                        link[0].onClick(this)
                    }
                    return true
                }
            }
            return false
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private fun getClickableSpans(event: MotionEvent): Array<ClickableSpan> {
        // 触摸点的初始坐标
        var x = event.x.toInt()
        var y = event.y.toInt()

        // 减去内边距（）
        // event.x 和 event.y: 参照原点 (0,0) 是整个 TextView 控件的左上角
        // Layout(文字排版引擎): 参照原点 (0,0) 是纯文字内容的左上角
        x -= totalPaddingLeft
        y -= totalPaddingTop

        // 加上滚动偏移量
        x += scrollX
        y += scrollY

        val layout = layout
        val line = layout.getLineForVertical(y)
        val off = layout.getOffsetForHorizontal(line, x.toFloat())

        val spannable = text as Spannable
        // ClickableSpan: 这个字符是否为可点击
        return spannable.getSpans(off, off, ClickableSpan::class.java)
    }
}