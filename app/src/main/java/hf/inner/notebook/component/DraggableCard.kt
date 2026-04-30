package hf.inner.notebook.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import hf.inner.notebook.page.home.clickable
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max


enum class SwipeResult {
    ACCEPTED, REJECTED
}


@Composable
fun DraggableCard(
    item: Any,
    modifier: Modifier = Modifier,
    onSwiped: (Any, Any) -> Unit,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val swipeXLeft = -(screenWidth.value * 3.2).toFloat()
    val swipeXRight = (screenWidth.value * 3.2).toFloat()
    val swipeYTop = -1000f
    val swipeYBottom = 1000f
    val swipeX = remember { Animatable(0f) }
    val swipeY = remember { Animatable(0f) }
    // 更新边界
    swipeX.updateBounds(swipeXLeft, swipeXRight)
    swipeY.updateBounds(swipeYTop, swipeYBottom)
    if (abs(swipeX.value) < swipeXRight - 50f) {
        val rotationFraction = (swipeX.value / 60).coerceIn(-40f, 40f)
        Card(
            modifier = modifier
                .zIndex(if (abs(swipeX.value) > 0 || abs(swipeY.value) > 0) 1f else 0f)
                .dragContent(
                    swipeX = swipeX,
                    swipeY = swipeY,
                    maxX = swipeXRight,
                    onSwiped = { _, _ ->}
                )
                // 对比offset：物理位置没变
                .graphicsLayer(
                    translationX = swipeX.value,
                    translationY = swipeY.value,
                    rotationZ = rotationFraction,
                    shadowElevation = if (abs(swipeX.value) > 0 || abs(swipeY.value) > 0) 16f else 4f,
                    shape = RoundedCornerShape(16.dp),
                    clip = true
                )
                .clickable { onClick() }
//                .clip(RoundedCornerShape(16.dp))
        ) {
            content()
        }
    } else {
        // 右滑喜欢，左滑拒接
        val swipeResult = if (swipeX.value > 0) SwipeResult.ACCEPTED else SwipeResult.REJECTED
        onSwiped(swipeResult, item)
    }
}

fun Modifier.dragContent(
    swipeX: Animatable<Float, AnimationVector1D>,
    swipeY: Animatable<Float, AnimationVector1D>,
    maxX: Float,
    onSwiped: (Any, Any) -> Unit
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()
    pointerInput(Unit) {
        this.detectDragGestures(
            onDragCancel = {
                coroutineScope.apply {
                    launch { swipeX.animateTo(0f) }
                    launch { swipeY.animateTo(0f) }
                }
            },
            onDragEnd = {
                coroutineScope.apply {

                    if (abs(swipeX.targetValue) < abs(maxX)/4) {
                        // 滑动的距离不足边界的四分之一
                        launch { swipeX.animateTo(0f, tween(400)) }
                        launch { swipeY.animateTo(0f, tween(400)) }
                    } else {
                        launch {
                            if (swipeX.targetValue > 0) {
                                swipeX.animateTo(maxX, tween(400))
                            } else {
                                swipeX.animateTo(-maxX, tween(400))
                            }
                        }
                    }
                }
            }
        ) { change, dragAmount ->
            // 吃掉这个触摸事件，防止它向下传递给底层的列表
            change.consumePositionChange()
            coroutineScope.apply {
                // 让卡片跟随手指
                launch { swipeX.snapTo(swipeX.targetValue + dragAmount.x) }
                launch { swipeY.snapTo(swipeY.targetValue + dragAmount.y) }
            }

        }
    }
}