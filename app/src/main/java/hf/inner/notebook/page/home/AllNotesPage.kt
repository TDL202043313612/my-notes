package hf.inner.notebook.page.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.moriafly.salt.ui.SaltTheme
import hf.inner.notebook.R
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.component.ModernDateRangePicker
import hf.inner.notebook.component.NoteCard
import hf.inner.notebook.component.RYScaffold
import hf.inner.notebook.page.viewmodel.LocalMemosState
import hf.inner.notebook.page.viewmodel.SortTime
import hf.inner.notebook.page.input.ChatInputDialog
import hf.inner.notebook.page.router.Screen
import hf.inner.notebook.state.NoteState
import hf.inner.notebook.utils.FirstTimeWarmDialog
import hf.inner.notebook.utils.SettingsPreferences
import hf.inner.notebook.utils.SharedPreferencesUtils
import hf.inner.notebook.utils.str
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AllNotesPage(
    navController: NavHostController,
    hideBottomNavBar: ((Boolean) -> Unit)
) {
    val noteState: NoteState = LocalMemosState.current
    val scope = rememberCoroutineScope()
    var openFilterBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showWarnDialog by rememberSaveable { mutableStateOf(false) }
    var showInputDialog by rememberSaveable { mutableStateOf(false) }
    var showDateRangePicker by rememberSaveable { mutableStateOf(false) }
    var parentNoteForComment by rememberSaveable { mutableStateOf<NoteShowBean?>(null) }
    var isGalleryMode by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        showWarnDialog = SettingsPreferences.firstLaunch.first()
    }

    RYScaffold(
        title = stringResource(R.string.all_note),
        titleContent = {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HomeTabTitle(
                    selected = !isGalleryMode,
                    text = "Notes",
                    onClick = { isGalleryMode = false }
                )
                HomeTabTitle(
                    selected = isGalleryMode,
                    text = "Gallery",
                    onClick = { isGalleryMode = true }
                )
            }
        },
        navController = null,
        actions = {
            ToolBar(
                navController,
                onSortChanged = {
                    scrollToTop(scope, listState)
                },
                dateRangeBlock = {
                    showDateRangePicker = true
                }
            )
        },
        floatingActionButton = {
            if (!showInputDialog) {
                FloatingActionButton(onClick = {
                    hideBottomNavBar.invoke(true)
                    parentNoteForComment = null
                    showInputDialog = true
                }, modifier = Modifier.padding(end = 16.dp, bottom = 32.dp)) {
                    Icon(
                        Icons.Rounded.Edit, stringResource(R.string.edit)
                    )
                }
            }
        }
    ) {
        Box {
            if (isGalleryMode) {
                val galleryNotes = remember(noteState.notes) {
                    noteState.notes.filter { it.note.attachments.isNotEmpty() }
                }
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    items(galleryNotes) { noteBean ->
                        GalleryItem(noteBean) {
                            navController.navigate(route = Screen.InputDetail(noteBean.note.noteId))
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(count = noteState.notes.size, key = { index -> noteState.notes[index].note.noteId }) { index ->
                        NoteCard(
                            noteShowBean = noteState.notes[index],
                            navHostController = navController,
                            onCommentClick = {
                                parentNoteForComment = it
                                hideBottomNavBar.invoke(true)
                                showInputDialog = true
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }

                }
            }


            BackHandler(enabled = showInputDialog) {
                hideBottomNavBar.invoke(false)
                showInputDialog = false
                parentNoteForComment = null
            }

            ChatInputDialog(
                isShow = showInputDialog,
                parentNote = parentNoteForComment,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
            ) {
                hideBottomNavBar.invoke(false)
                showInputDialog = false
                parentNoteForComment = null
                scrollToTop(scope, listState)
            }
        }
    }
    HomeFilterBottomSheet(show = openFilterBottomSheet) {
        openFilterBottomSheet = false
    }

    if (showWarnDialog) {
        FirstTimeWarmDialog{
            scope.launch {
                SettingsPreferences.changeFirstLaunch(false)
                showWarnDialog = false
            }
        }
    }

    // 时间对话框
    if (showDateRangePicker) {
        ModernDateRangePicker(
            onDismissRequest = { showDateRangePicker = false },
            onConfirm = { startTime, endTime ->
                navController.navigate(Screen.DateRangePage(startTime = startTime, endTime = endTime))
                showDateRangePicker = false
            }
        )
    }

}

private fun scrollToTop(
    coroutineScope: CoroutineScope,
    listState: LazyListState
){
    coroutineScope.launch {
        delay(200)
        listState.scrollToItem(0)
    }
}
@Composable
private fun ToolBar(navController: NavHostController, onSortChanged: () -> Unit, dateRangeBlock: () -> Unit) {
    IconButton(
        onClick = {
            dateRangeBlock()
        }
    ) {
        Icon(
            imageVector = Icons.Outlined.AvTimer,
            contentDescription = stringResource(R.string.date_range),
            tint = SaltTheme.colors.text
        )
    }
    IconButton(
        onClick = {
            navController.navigate(route = Screen.TagList) {
                // 防止重复打开当前页
                launchSingleTop = true
            }
        }
    ) {
        Icon(
            imageVector = Icons.Outlined.Tag,
            contentDescription = stringResource(R.string.tag),
            tint = SaltTheme.colors.text
        )
    }

    IconButton(
        onClick = {
            navController.navigate(route = Screen.Search) {
                launchSingleTop = true
            }
        }
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = stringResource(R.string.search_hint),
            tint = SaltTheme.colors.text
        )
    }
    SortFilterMenu {
        onSortChanged()
    }
//    IconButton(
//        onClick = {
//            filterBlock()
//        },
//    ) {
//        Icon(
//            imageVector = Icons.Outlined.FilterList,
//            contentDescription = stringResource(R.string.sort),
//            tint = SaltTheme.colors.text
//        )
//    }
}

@Composable
fun SortFilterMenu(onSortChanged: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    val sortTime by SharedPreferencesUtils.sortTime.collectAsState(SortTime.UPDATE_TIME_DESC)
    val scope = rememberCoroutineScope()
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = stringResource(R.string.sort),
                tint = SaltTheme.colors.text
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SaltTheme.colors.popup)
        ) {
            SortMenuItem(
                text = stringResource(R.string.update_time_desc),
                selected = sortTime == SortTime.UPDATE_TIME_DESC,
                onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.UPDATE_TIME_DESC)
                        expanded = false
                        onSortChanged()
                    }
                }
            )
            SortMenuItem(
                text = stringResource(R.string.update_time_asc),
                selected = sortTime == SortTime.UPDATE_TIME_ASC,
                onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.UPDATE_TIME_ASC)
                        expanded = false
                        onSortChanged()
                    }
                }
            )
            SortMenuItem(
                text = stringResource(R.string.create_time_desc),
                selected = sortTime == SortTime.CREATE_TIME_DESC,
                onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.CREATE_TIME_DESC)
                        expanded = false
                        onSortChanged()
                    }
                }
            )
            SortMenuItem(
                text = stringResource(R.string.create_time_asc),
                selected = sortTime == SortTime.CREATE_TIME_ASC,
                onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.CREATE_TIME_ASC)
                        expanded = false
                        onSortChanged()
                    }
                }
            )
        }
    }
}
@Composable
fun SortMenuItem(text: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    color = if (selected) SaltTheme.colors.highlight else SaltTheme.colors.text,
                    fontSize = 14.sp
                )
                if (selected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = SaltTheme.colors.highlight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        onClick = onClick
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFilterBottomSheet(show: Boolean, onDismissRequest: () -> Unit) {
    val sortTime = SharedPreferencesUtils.sortTime.collectAsState(SortTime.UPDATE_TIME_DESC)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    if (show) {
        ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
            Column(Modifier.fillMaxWidth()) {
                TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.UPDATE_TIME_DESC)
                        sheetState.hide()
                        onDismissRequest()
                    }
                }) {
                    Row(
                        Modifier
                        .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.update_time_desc))
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(checked = sortTime.value == SortTime.UPDATE_TIME_DESC, null)
                    }
                }

                TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.UPDATE_TIME_ASC)
                        sheetState.hide()
                        onDismissRequest()
                    }
                }) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.update_time_asc))
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(checked = sortTime.value == SortTime.UPDATE_TIME_ASC, null)
                    }
                }

                TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.CREATE_TIME_DESC)
                        sheetState.hide()
                        onDismissRequest()
                    }
                }) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.create_time_desc))
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(checked = sortTime.value == SortTime.CREATE_TIME_DESC, null)
                    }
                }

                TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.CREATE_TIME_ASC)
                        sheetState.hide()
                        onDismissRequest()
                    }
                }) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.create_time_asc))
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(checked = sortTime.value == SortTime.CREATE_TIME_ASC, null)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    var startDateText by rememberSaveable { mutableStateOf("") }
    var endDateText by rememberSaveable { mutableStateOf("") }
    var startDateError by rememberSaveable { mutableStateOf(false) }
    var endDateError by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = SaltTheme.colors.popup,
        title = { Text(stringResource(R.string.select_date)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.start_time))
                TextField(
                    value = startDateText,
                    onValueChange = {
                        startDateText = it
                        startDateError = false
                    },
                    placeholder = { Text("20260318") },
                    isError = startDateError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    )
                )
                if (startDateError) {
                    Text(
                        text = stringResource(R.string.input_correct_date),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.end_date))
                TextField(
                    value = endDateText,
                    onValueChange = {
                        endDateText = it
                        endDateError = false
                    },
                    placeholder = { Text("20250909") },
                    isError = endDateError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                )
                if (endDateError) {
                    Text(
                        text = stringResource(R.string.input_correct_date),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                try {
                    val startDate = parseCompactDate(startDateText)
                    val endDate = parseCompactDate(endDateText)
                    // 把日期（比如 2026-03-18）补全为当天的 00:00:00（凌晨）
                    val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val endMillis = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    onConfirm(startMillis, endMillis)
                } catch (e: Exception) {
                    startDateError = startDateText.isNotEmpty() && !isValidCompactDate(startDateText)
                    endDateError = endDateText.isNotEmpty() && !isValidCompactDate(endDateText)
                }
            }) {
                Text(stringResource(R.string.sure))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun isValidCompactDate(dateString: String): Boolean {
    return try {
        parseCompactDate(dateString)
        true
    }catch (e: Exception) {
        false
    }
}
@RequiresApi(Build.VERSION_CODES.O)
private fun parseCompactDate(dateString: String): LocalDate {
    if (dateString.length != 8) {
        throw IllegalArgumentException(R.string.date_not_correct.str)
    }
    val year = dateString.substring(0, 4).toInt()
    val month = dateString.substring(4, 6).toInt()
    val day = dateString.substring(6, 8).toInt()
    return LocalDate.of(year, month, day)
}

@Composable
fun HomeTabTitle(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (selected) SaltTheme.colors.text else SaltTheme.colors.subText,
        animationSpec = tween(durationMillis = 300),
        label = "tab_title_color_animation"
    )
    val fontSize by animateFloatAsState(
        targetValue = if (selected) 24f else 18f,
        animationSpec = tween(durationMillis = 300),
        label = "tab_title_size_animation"
    )
    Box(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = text,
            style = SaltTheme.textStyles.main.copy(
                fontSize = fontSize.sp,
                color = color,
                fontWeight = if(selected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
fun GalleryItem(noteBean: NoteShowBean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(6.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SaltTheme.colors.subBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column {
            val firstImage = noteBean.note.attachments.firstOrNull()
            if (firstImage != null) {
                AsyncImage(
                    model = File(firstImage.path),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            }
            Text(
                text = noteBean.note.content,
                style = SaltTheme.textStyles.main.copy(
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}