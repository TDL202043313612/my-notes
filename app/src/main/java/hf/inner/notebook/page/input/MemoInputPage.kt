package hf.inner.notebook.page.input

import android.Manifest
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.moriafly.salt.ui.SaltTheme
import dev.tdl.compose.markdowntext.MarkdownText
import hf.inner.notebook.R
import hf.inner.notebook.bean.Attachment
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.bean.Tag
import hf.inner.notebook.component.PIconButton
import hf.inner.notebook.page.viewmodel.LocalMemosState
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import hf.inner.notebook.page.viewmodel.LocalTags
import hf.inner.notebook.page.router.LocalRootNavController
import hf.inner.notebook.page.router.Screen
import hf.inner.notebook.page.router.debouncedPopBackStack
import hf.inner.notebook.utils.handlePickFiles
import hf.inner.notebook.utils.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.collections.forEach
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MemoInputPage(
    memoId: Long,
    memoInputViewModel: MemoInputViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val noteState = LocalMemosState.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val scope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }
    val tagList = LocalTags.current.filterNot { it.isCityTag }
    var tagMenuExpanded by remember { mutableStateOf(false) }
    val navController = LocalRootNavController.current
    val memosViewModel = LocalMemosViewModel.current
    var isEditMode by remember { mutableStateOf(false) }
    val memo = remember { noteState.notes.find { it.note.noteId == memoId } }
    // stateSaver = TextFieldValue.Saver: 就是给了它一本**“说明书”**，告诉系统：“遇到 TextFieldValue 时，请按照官方提供的这个专门的 Saver 规则去拆解和恢复它。
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(memo?.note?.content ?: "", TextRange(memo?.note?.content?.length ?: 0, )))
    }
    var photoImageUri by remember { mutableStateOf<Uri?>(null) }

    val localDate = Instant.ofEpochMilli(memo?.note?.createTime ?: System.currentTimeMillis()).atZone(ZoneId
        .systemDefault()).toLocalDate()
    val focusManager = LocalFocusManager.current

    var isRecording by rememberSaveable { mutableStateOf(false) }
    var duration by remember { mutableStateOf(Duration.ZERO) }
    val infiniteTransition = rememberInfiniteTransition(label = "recording_dot")
    var tagSearchQuery by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            duration = 0.seconds
            while (true) {
                delay(1000L)
                duration += 1.seconds
            }
        }
    }
    val dotAlpha = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            tween(2000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "recording_dot_alpha"
    )

    fun uploadImage(uri: Uri) = coroutineScope.launch {
        handlePickFiles(setOf(uri)) {
            memoInputViewModel.uploadAttachments.addAll(it)
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            memoInputViewModel.startRecording()
            isRecording = true
            focusRequester.freeFocus()
        } else {
            toast(context.getString(R.string.need_microphone_permission))
        }
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoImageUri?.let { uploadImage(it) }
        }
    }
    val pickMultipleMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { uris ->
        scope.launch {
            handlePickFiles(uris.toSet()) {
                memoInputViewModel.uploadAttachments.addAll(it)
            }
        }
    }
    fun submit() = coroutineScope.launch {
        focusRequester.freeFocus()
        focusManager.clearFocus()
        memo?.note?.apply {
            this.content = text.text
            this.updateTime = System.currentTimeMillis()
            this.attachments = memoInputViewModel.uploadAttachments
            memosViewModel.insertOrUpdate(this)
        }
        navController.debouncedPopBackStack()
    }
    @Composable
    fun TagButton(tagList: List<Tag>) {
        // 更具搜索内容过滤标签
        val filteredTags = remember(tagList, tagSearchQuery) {
            if (tagSearchQuery == null) {
                tagList
            } else {
                tagList.filter { it.tag.contains(tagSearchQuery!!, ignoreCase = true) }
            }
        }
        // 提取文本替换逻辑为单独的函数，避免代码重复
        fun insertTagText(tagContent: String) {
            val newText = text.text.replaceRange(text.selection.min, text.selection.max, tagContent)
            val newSelection = TextRange(text.selection.min + tagContent.length)
            text = text.copy(newText, newSelection)
        }
        fun replaceTagText(tagContent: String) {
            val cursorPos = text.selection.start
            val textBeforeCursor = text.text.substring(0, cursorPos)
            val lastHashIndex = textBeforeCursor.lastIndexOf("#")
            if (lastHashIndex != -1) {
                val cleanTag = tagContent.removePrefix("#")
                val replacement = "#$cleanTag"
                val newText = text.text.replaceRange(lastHashIndex, cursorPos, replacement)
                val newSelection = TextRange(lastHashIndex + replacement.length)
                text = text.copy(newText, newSelection)
            }
        }
        // 根据tagList是否为空选择不同的图标
        val tagIcon = if (tagList.isEmpty()) Icons.Filled.Tag else Icons.Outlined.Tag

        PIconButton(
            imageVector = tagIcon,
            contentDescription = stringResource(R.string.tag)
        ) {
            // 光标当前位置
            val cursorPos = text.selection.start
            val textBeforeCursor = text.text.substring(0, cursorPos)
            val lastHashIndex = textBeforeCursor.lastIndexOf("#")
            val fragment = if (lastHashIndex != -1) textBeforeCursor.substring(lastHashIndex + 1) else null
            if (fragment != null && !fragment.contains(" ") && !fragment.contains("\n")) {
                tagSearchQuery = fragment
                tagMenuExpanded = !tagMenuExpanded
            } else {
                insertTagText("#")
                tagSearchQuery = ""
                tagMenuExpanded = tagList.isNotEmpty()
            }
        }

        if (filteredTags.isNotEmpty() && tagMenuExpanded) {
            Box {
                DropdownMenu(
                    modifier = Modifier
                        .wrapContentHeight()
                        .heightIn(max = 400.dp),
                    expanded = tagMenuExpanded,
                    onDismissRequest = {
                        tagSearchQuery = null
                        tagMenuExpanded = false
                    },
                    // 这个弹窗不抢占焦点，它只是一个安安静静悬浮在表面的图层
                    properties = PopupProperties(focusable = false)
                ) {
                    tagList.forEach { tag ->
                        DropdownMenuItem(
                            text = { Text(tag.tag) },
                            onClick = {
                                if (tagSearchQuery != null) {
                                    replaceTagText(tag.tag)
                                } else {
                                    val cleanTag = tag.tag.removePrefix("#")
                                    insertTagText("#$cleanTag")
                                }
                                tagMenuExpanded = false
                                tagSearchQuery = null
                            }
                        )
                    }
                }
            }
        }

    }
    Scaffold(modifier = Modifier.imePadding(),
        topBar = {
            InputActionBar(
                localDate = localDate,
                isEditMode = isEditMode,
                memo = memo,
                navBack = {
                    if (isEditMode) {
                        focusManager.clearFocus()
                        focusRequester.freeFocus()
                    }
                    navController.debouncedPopBackStack()
                }
            ) {
                isEditMode = it
                if (it) {
                    scope.launch {
                        delay(300)
                        focusRequester.requestFocus()
                    }
                } else {
                    focusRequester.freeFocus()
                }
            }
        },
        bottomBar = {
            if (isEditMode) {
                BottomAppBar(containerColor = SaltTheme.colors.background) {
                    AnimatedContent(
                        targetState = isRecording,
                        label = "recording_toolbar_animation"
                    ) { targetIsRecording ->
                        if(targetIsRecording) {
                            // 录音控制台 UI
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .imePadding()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                //  取消按钮
                                PIconButton(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.cancel),
                                    tint = MaterialTheme.colorScheme.error
                                ) {
                                    memoInputViewModel.cancelRecording()
                                    isRecording = false
                                }
                                // 中间的闪烁灯 + 计时器
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .graphicsLayer {
                                                scaleX = dotAlpha.value
                                                scaleY = dotAlpha.value
                                            }
                                            .clip(CircleShape)
                                            .background(Color.Red)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        modifier = Modifier.alignByBaseline(),
                                        text = duration.toComponents { minutes, seconds, _ ->
                                            val min = minutes.toString().padStart(2, '0')
                                            val sec = seconds.toString().padStart(2, '0')
                                            "$min:$sec"
                                        }
                                    )
                                }
                                PIconButton(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = stringResource(R.string.finish),
                                    tint = SaltTheme.colors.highlight
                                ) {
                                    memoInputViewModel.finishRecording()
                                    isRecording = false
                                }
                            }
                        } else {
                            // 工具栏 UI
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // 它确保当软键盘弹出时，这行操作按钮会被键盘顶起来，而不会被键盘遮住
                                    .imePadding()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TagButton(tagList)
                                PIconButton(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = stringResource(R.string.add_image)
                                ) {
                                    pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                                PIconButton(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = stringResource(R.string.take_photo)
                                ) {
                                    // TODO 拍摄的照片并没有压缩
                                    try {
                                        val imagesFolder = File(context.cacheDir, "capture_picture")
                                        if (!imagesFolder.exists()) {
                                            imagesFolder.mkdirs()
                                        }
                                        // 在指定的目录下，自动生成一个名字唯一、绝不重复的临时文件
                                        val file = File.createTempFile("capture_picture_", ".jpg", imagesFolder)
                                        // 把真实的路径转换为虚拟路径
                                        val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
                                        photoImageUri = uri
                                        takePhoto.launch(uri)
                                    } catch (e: ActivityNotFoundException) {
                                        toast(e.localizedMessage ?: "Unable to take picture.")
                                    }
                                }
                                PIconButton(
                                    imageVector = Icons.Outlined.MicNone,
                                    contentDescription = stringResource(R.string.record_audio)
                                ) {
                                    // 申请权限
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                PIconButton(
                                    imageVector = Icons.Outlined.Send,
                                    contentDescription = stringResource(R.string.send),
                                ) {
                                    submit()
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                }
            }
        }, snackbarHost = {
            SnackbarHost(hostState = snackbarState)
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxHeight()
                .background(SaltTheme.colors.background)
        ) {
            val customTextFieldColors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = SaltTheme.colors.text,
                unfocusedTextColor = SaltTheme.colors.text
            )
            if (isEditMode) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = SaltTheme.textStyles.paragraph,
                    value = text,
                    colors = customTextFieldColors,
                    onValueChange = { it: TextFieldValue ->
                        text = it
                        val cursorPos = it.selection.start
                        val textBeforeCursor = it.text.substring(0, cursorPos)
                        val lastHashIndex = textBeforeCursor.lastIndexOf("#")
                        if (lastHashIndex != -1) {
                            val fragment = textBeforeCursor.substring(lastHashIndex + 1)
                            if (fragment.contains(" ") || fragment.contains("\n")) {
                                tagMenuExpanded = false
                                tagSearchQuery = null
                            } else {
                                tagSearchQuery = fragment
                                // 检查是否有匹配项
                                val hasMatch = tagList.any { it.tag.contains(fragment, ignoreCase = true) }
                                tagMenuExpanded = hasMatch
                            }
                        } else {
                            tagMenuExpanded = false
                            tagSearchQuery = null
                        }
                    },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    MarkdownText(
                        markdown = text.text,
                        style = SaltTheme.textStyles.paragraph,
                        isTextSelectable = true
                    )
                }
            }
            if (memoInputViewModel.uploadAttachments.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .height(120.dp)
                        .padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(
                        items = memoInputViewModel.uploadAttachments.toList(),
//                            key = { index, attachment ->  attachment.path }
                    ) { index, attachment ->
                        when(attachment.type) {
                            Attachment.Type.IMAGE -> {
                                InputImage(attachment = attachment, isEdit = isEditMode, delete = { path ->
                                    memoInputViewModel.deleteResource(path)
                                }) {
                                    val onlyImages = memoInputViewModel.uploadAttachments.filter { it.type == Attachment.Type.IMAGE }
                                    val realImageIndex = onlyImages.indexOf(attachment)
                                    if (realImageIndex != -1) {
                                        navController.navigate(route = Screen.PictureDisplay(
                                            pathList = onlyImages.map { it.path },
                                            curIndex = realImageIndex,
                                            timestamps = listOf(System.currentTimeMillis())
                                        ))
                                    }

                                }
                            }
                            Attachment.Type.AUDIO -> {
                                val isCurrentlyPlaying = memoInputViewModel.currentlyPlayingPath == attachment.path
                                InputAudio(
                                    attachment = attachment,
                                    isEdit = isEditMode,
                                    isPlaying = isCurrentlyPlaying,
                                    progress = if (isCurrentlyPlaying) memoInputViewModel.currentAudioProgress else 0,
                                    duration = if (isCurrentlyPlaying) memoInputViewModel.currentAudioDuration else 0,
                                    delete = { path -> memoInputViewModel.deleteResource(path) },
                                    onSeek = { position -> memoInputViewModel.seekAudio(position) },
                                    onclick = {
                                        memoInputViewModel.toggleAudio(attachment.path)
                                    }
                                )
                            }
                            else -> { }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        when {
            memo != null -> {
                memo.note.attachments.let { resourceList ->
                    memoInputViewModel.uploadAttachments.apply {
                        clear()
                        addAll(resourceList)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun InputActionBar(
    localDate: LocalDate,
    memo: NoteShowBean?,
    navBack: () -> Unit,
    isEditMode: Boolean,
    isEditModeClick: (Boolean) -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SaltTheme.colors.background),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = localDate.dayOfMonth.toString(),
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold).copy(color = SaltTheme.colors.text)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(text = localDate.dayOfWeek.getDisplayName(
                            TextStyle.SHORT, Locale.getDefault()
                        ), style = MaterialTheme.typography.bodySmall.copy(color = SaltTheme.colors.text), fontSize = 11.sp
                    )
                    Row {
                        Text(
                            text = localDate.year.toString() + "/" + localDate.month.getDisplayName(
                                TextStyle.SHORT, Locale.getDefault()
                            ), style = MaterialTheme.typography.bodySmall.copy(color = SaltTheme.colors.text), fontSize = 10.sp
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                navBack()
            }) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = SaltTheme.colors.text
                )
            }
        },
        actions = {
            if (isEditMode) {
                IconButton(onClick = {
                    isEditModeClick.invoke(false)
                }) {
                    Icon(Icons.Outlined.RemoveRedEye, contentDescription = stringResource(R.string.preview), tint = SaltTheme.colors.text)
                }
            } else {
                IconButton(onClick = {
                    isEditModeClick.invoke(true)
                }) {
                    Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit), tint = SaltTheme.colors.text)
                }
            }
        }
    )
}