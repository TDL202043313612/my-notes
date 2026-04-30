package hf.inner.notebook.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import hf.inner.notebook.page.home.clickable

@Composable
fun LoadingComponent(visible: Boolean) {
    val interceptClicks = remember { mutableStateOf(visible) }
    DisposableEffect(interceptClicks.value) {
        onDispose {
            interceptClicks.value = false
        }
    }
    val modifier = if (visible) {
        Modifier
            .fillMaxSize()
            .clickable(enabled = interceptClicks.value) { }
    } else {
        Modifier
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (visible) {

        }
    }
}