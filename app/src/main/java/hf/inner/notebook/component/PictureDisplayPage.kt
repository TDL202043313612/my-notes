package hf.inner.notebook.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.moriafly.salt.ui.Text
import hf.inner.notebook.page.router.debouncedPopBackStack
import hf.inner.notebook.utils.BlurTransformation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

@Composable
fun PictureDisplayPage(
    pathList: List<String>, index: Int, timestamps: List<Long>,
    navController: NavHostController
){
    val pageState = rememberPagerState(pageCount = { pathList.size }, initialPage = index )
    val dateFormat = SimpleDateFormat("yyy-MM-dd HH:mm", Locale.getDefault())
    Box {
        HorizontalPager(state = pageState) { page ->
            DetailContent(
                imgUri = pathList[page],
                requestImage = {
                    Image(pathList[page])
                }
            )
        }
        IconButton(onClick = { navController.debouncedPopBackStack() }, modifier = Modifier.padding(start = 12.dp, top = 24.dp, end = 0.dp, bottom = 0.dp)) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = Color(0xFF000000)
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 24.dp, end = 12.dp)
                .align(Alignment.TopEnd)
                .wrapContentSize()
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "${pageState.currentPage + 1}/${pathList.size}",
                color = Color(0xFFFFFFFF),
                fontSize = 16.sp,
            )
        }
        Box(
            modifier = Modifier
                .padding(bottom = 24.dp, start = 12.dp)
                .align(Alignment.BottomStart)
                .wrapContentSize()
        ) {
            val safeIndex = min(pageState.currentPage, timestamps.size - 1)
            val timestamp = if (timestamps.isNotEmpty()) timestamps[safeIndex] else System.currentTimeMillis()
            Text(
                text = dateFormat.format(Date(timestamp)),
                color = Color(0xFFFFFFFF),
                fontSize = 16.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun DetailContent(
    imgUri: String?,
    modifier: Modifier = Modifier,
    requestImage: @Composable () -> Unit
) {
    Surface {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imgUri)
                .transformations(
                    BlurTransformation(
                        LocalContext.current,
                        25f,
                        5f
                    )
                )
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = modifier
                .fillMaxWidth(),
            contentScale = ContentScale.Crop // 裁剪并填满整个宽度
        )
        requestImage()
    }
}

@Composable
private fun Image(imgUrl: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imgUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit,

    )
}