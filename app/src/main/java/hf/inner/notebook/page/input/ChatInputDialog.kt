package hf.inner.notebook.page.input

import android.Manifest
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.moriafly.salt.ui.SaltTheme
import hf.inner.notebook.R
import hf.inner.notebook.bean.Attachment
import hf.inner.notebook.bean.Note
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.bean.Tag
import hf.inner.notebook.component.PIconButton
import hf.inner.notebook.page.viewmodel.LocalMemosViewModel
import hf.inner.notebook.page.viewmodel.LocalTags
import hf.inner.notebook.page.home.clickable
import hf.inner.notebook.page.router.LocalRootNavController
import hf.inner.notebook.page.router.Screen
import hf.inner.notebook.utils.handlePickFiles
import hf.inner.notebook.utils.toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds



@Composable
fun ChatInputDialog(
    isShow: Boolean,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    parentNote: NoteShowBean? = null,
    dismiss: () -> Unit
) {
    var bottomSheetState by rememberSaveable { mutableStateOf(false) }
    bottomSheetState = isShow
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var text: TextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current
    var tagMenuExpanded by remember { mutableStateOf(false) }
    var photoImageUri by remember { mutableStateOf<Uri?>(null) }
    val tagList = LocalTags.current.filterNot { it.isCityTag }
    val memosViewModel = LocalMemosViewModel.current
    val memoInputViewModel = hiltViewModel<MemoInputViewModel>()
    val navHostController = LocalRootNavController.current
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var duration by remember { mutableStateOf(Duration.ZERO) }
    var tagSearchQuery by remember { mutableStateOf<String?>(null) }
    val infiniteTransition = rememberInfiniteTransition(label = "recording_dot")

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            memoInputViewModel.startRecording()
            isRecording = true
            softwareKeyboardController?.hide()
            focusRequester.freeFocus()
        } else {
            toast(context.getString(R.string.need_microphone_permission))
        }
    }


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
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoImageUri?.let {
                coroutineScope.launch {
                    handlePickFiles(setOf(it)) {
                        memoInputViewModel.uploadAttachments.addAll(it)
                    }
                }
            }
        }
    }

    val pickMultipleMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(3)
    ) { uris ->
        coroutineScope.launch {
            handlePickFiles(uris.toSet()) {
                memoInputViewModel.uploadAttachments.addAll(it)
            }
        }
    }

    fun submit() = coroutineScope.launch {
        softwareKeyboardController?.hide()
        focusRequester.freeFocus()
        val content = text.text
        memosViewModel.insertOrUpdate(Note(
            content = content,
            attachments = memoInputViewModel.uploadAttachments.toList(),
            parentNoteId = parentNote?.note?.noteId
        ))
        text = TextFieldValue("")
        memoInputViewModel.uploadAttachments.clear()
        dismiss()
    }

    LaunchedEffect(bottomSheetState) {
        if (bottomSheetState) {
            delay(100)
            focusRequester.requestFocus()
            softwareKeyboardController?.show()
        } else {
            softwareKeyboardController?.hide()
        }
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
                        tagMenuExpanded = false
                        tagSearchQuery = null
                    },
                    // 这个弹窗不抢占焦点，它只是一个安安静静悬浮在表面的图层
                    properties = PopupProperties(focusable = false)
                ) {
                    filteredTags.forEach { tag ->
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

    if (isShow) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .clickable(showRipple = false) {
                    dismiss()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        color = SaltTheme.colors.background
                    )
            ) {
                if (parentNote != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(SaltTheme.colors.subBackground, RoundedCornerShape(8.dp))
                            .border(1.dp, SaltTheme.colors.subText.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = parentNote.note.content,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                            style = SaltTheme.textStyles.paragraph.copy(fontSize = 13.sp, color = SaltTheme.colors.subText)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    minLines = 5,
                    textStyle = SaltTheme.textStyles.paragraph,
                    onValueChange = {
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
                    modifier = modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .clickable(onClick = {}),
                    keyboardOptions = keyboardOptions,
                    label = { Text(stringResource(R.string.any_thoughts)) }
                )
                // 处理选择图片显示
                if (memoInputViewModel.uploadAttachments.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .height(80.dp)
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
                                    InputImage(attachment = attachment, isEdit = true, delete = { path ->
                                        memoInputViewModel.deleteResource(path)
                                    }) {
                                        val onlyImages = memoInputViewModel.uploadAttachments.filter { it.type == Attachment.Type.IMAGE }
                                        val realImageIndex = onlyImages.indexOf(attachment)
                                        if (realImageIndex != -1) {
                                            navHostController.navigate(route = Screen.PictureDisplay(
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
                                        isEdit = true,
                                        isPlaying = isCurrentlyPlaying,
                                        progress = if (isCurrentlyPlaying) memoInputViewModel.currentAudioProgress else 0,
                                        duration = if (isCurrentlyPlaying) memoInputViewModel.currentAudioDuration else 0,
                                        delete = { path -> memoInputViewModel.deleteResource(path) },
                                        onclick = { memoInputViewModel.toggleAudio(attachment.path) },
                                        onSeek = { position -> memoInputViewModel.seekAudio(position) }
                                    )
                                }
                                else -> { }
                            }
                        }
                    }
                }
                AnimatedContent(
                    targetState = isRecording,
                    label = "recording_toolbar_animation"
                ) { targetIsRecording ->
                    if (targetIsRecording) {
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
    }
}