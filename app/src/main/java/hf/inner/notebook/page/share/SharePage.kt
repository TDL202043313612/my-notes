package hf.inner.notebook.page.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import dev.tdl.compose.markdowntext.MarkdownText
import hf.inner.notebook.R
import hf.inner.notebook.bean.Note
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.component.RYScaffold
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import hf.inner.notebook.utils.toTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun SharePage(
    noteId: Long,
    navController: NavHostController
){
    val context = LocalContext.current
    val noteViewModel = LocalMemosViewModel.current
    val noteShowBean = remember { mutableStateOf<NoteShowBean?>(null) }
    val captureView = remember { mutableStateOf<View?>(null) }
    val totalImages = remember { mutableStateOf(0) }
    val imagesLoaded = remember { mutableStateOf(true) } // 跟踪图片是否加载完成
    val loadedImages = remember { mutableStateOf(0) }
    val isCapturing = remember { mutableStateOf(false) } // 添加截图状态
    val bitmap = remember { mutableStateOf<Bitmap?>(null) } // 改为状态变量
    val scrollState = remember { ScrollState(0) }

    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val queriedNote = noteViewModel.getNoteShowBeanById(noteId)
            noteShowBean.value = queriedNote
            totalImages.value = queriedNote?.note?.attachments?.size ?: 0
            if (totalImages.value == 0) {
                imagesLoaded.value = true
            } else {
                imagesLoaded.value = false
                loadedImages.value = 0
            }
        }
    }

    // 当图片加载数量达到总数时，标记所有图片加载完成
    LaunchedEffect(loadedImages.value, totalImages.value) {
        if (loadedImages.value >= totalImages.value && totalImages.value > 0) {
            imagesLoaded.value = true
            captureView.value?.let { view->
                isCapturing.value = true
                bitmap.value = captureFullView(view, context)
                isCapturing.value = false
            }
        }
    }
    // 处理没有图片的情况
    LaunchedEffect(imagesLoaded.value, noteShowBean.value) {
        if (imagesLoaded.value && totalImages.value == 0 && noteShowBean.value !=null && bitmap.value == null &&
            !isCapturing.value) {
            captureView.value?.let { view ->
                isCapturing.value = true
                bitmap.value = captureFullView(view, context)
                isCapturing.value = false
            }
        }
    }

    RYScaffold(
        title = stringResource(R.string.share),
        navController = navController,
        actions = {
            IconButton(
                onClick = {
                if (!imagesLoaded.value || isCapturing.value) {
                    // 图片还在加载中或者正在截图，
                    return@IconButton
                }

                // 如果bitmap为空，尝试重新生成
                if (bitmap.value == null) {
                    captureView.value?.let { view ->
                        isCapturing.value = true
                        bitmap.value = captureFullView(view, context)
                        isCapturing.value = false
                    }
                }

                bitmap.value?.let {
                    shareImage(context, saveBitmapToFile(context, it))
                }
            },
            content = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Send,
                    tint = SaltTheme.colors.text,
                    contentDescription = stringResource(R.string.share)
                )
            }
            )
        }
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState)
        ) {
            AndroidView(
                factory = {
                    ComposeView(it).apply {
                        // 设置布局参数，确保宽度匹配屏幕
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        setContent {
                            noteShowBean.value?.let { noteBean ->
                                Column(Modifier.background(SaltTheme.colors.background)) {
                                    Spacer(Modifier.height(20.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SaltTheme.colors.popup),
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            val note = noteBean.note
                                            Text(
                                                modifier = Modifier.padding(start = 2.dp),
                                                text = note.createTime.toTime(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            MarkdownText(
                                                markdown = note.content,
                                                style = SaltTheme.textStyles.paragraph.copy(fontSize = 15.sp,
                                                    lineHeight = 24.sp),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            if (note.attachments.isNotEmpty()) {
                                                Spacer(Modifier.height(8.dp))
                                                CustomImageCard(
                                                    note = note,
                                                    onImageLoaded = {
                                                        loadedImages.value++
                                                    }
                                                )
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier
                                                .fillMaxWidth()) {
                                                Text(
                                                    text = "By IdeaMemo.T",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily =
                                                        FontFamily.Cursive),
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(20.dp)) // 增加内部底部间距
                                }
                            }
                        }
                    }
                }, modifier = Modifier.fillMaxWidth(), update = { view ->
                    captureView.value = view
                })
            Spacer(modifier = Modifier.height(120.dp)) // 增加外部底部间距，确保所有内容都能完整显示
        }
    }
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap): Uri {
    val imagesFolder = File(context.cacheDir, "shared_images")
    if (!imagesFolder.exists()) {
        imagesFolder.mkdirs()
    }

    val file = File(imagesFolder, "${System.currentTimeMillis()}.png")
    val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }
    // 返回虚拟路径
    return uri
}


fun shareImage(context: Context, imageUri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, imageUri)
        // // 授予临时读取权限
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Image"))
}

@Composable
fun CustomImageCard(note: Note, onImageLoaded: () -> Unit) {
    val context = LocalContext.current
    if (note.attachments.size == 1) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(note.attachments[0].path)
                .bitmapConfig(Bitmap.Config.ARGB_8888) // 强制使用软件位图
                .allowHardware(false) // 禁用硬件位图
                .allowRgb565(false) // 禁用RGB565格式
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(160.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            onSuccess = { onImageLoaded() }
        )
    } else {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            note.attachments.forEachIndexed { index, attachment ->
                val path: String = attachment.path
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(path)
                        .bitmapConfig(Bitmap.Config.ARGB_8888) // 强制使用软件位图
                        .allowHardware(false) // 禁用硬件位图
                        .allowRgb565(false) // 禁用RGB565格式
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    onSuccess = { onImageLoaded() }
                )
            }
        }
    }
}

fun captureFullView(view: View, context: Context): Bitmap? {
    // 获取屏幕宽度，减去左右边距
    val screenWidth = getScreenWidth(context)
    val contentWidth = screenWidth - 0

    // 使用屏幕宽度作为测量宽度，确保内容在测量时就会正确换行
    view.measure(
        // View.MeasureSpec.EXACTLY 精准
        View.MeasureSpec.makeMeasureSpec(contentWidth, View.MeasureSpec.EXACTLY),
        // View.MeasureSpec.UNSPECIFIED 没有限制
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    // 把view铺开
    view.layout(0, 0, view.measuredWidth, view.measuredHeight)

    try {
        // 白纸
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        // 画笔对准白纸
        val canvas = Canvas(bitmap)

        // 绘制View内容【命令模特“自己把自己画出来”！】
        view.draw(canvas)

        // 确保返回的是软件位图
        return if (bitmap.config == Bitmap.Config.ARGB_8888) {
            // 如果是硬件位图，转换为软件位图
            val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            softwareBitmap
        } else {
            bitmap
        }
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        // 如果绘制失败，尝试创建一个空的软件位图
        return try {
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        } catch (e: OutOfMemoryError) {
            // 如果内存不足，尝试创建一个更小的位图
            Bitmap.createBitmap(
                (view.measuredWidth / 2).coerceAtLeast(1),
                (view.measuredHeight / 2).coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
        }
    }
}

fun getScreenWidth(context: Context): Int {
    return context.resources.displayMetrics.widthPixels
}