package hf.inner.notebook.page.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.kizitonwose.calendar.core.yearMonth
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import hf.inner.notebook.R
import hf.inner.notebook.bean.NoteShowBean
import hf.inner.notebook.page.viewmodel.LocalMemosState
import hf.inner.notebook.page.home.clickable
import hf.inner.notebook.page.router.Screen
import hf.inner.notebook.page.router.debouncedPopBackStack
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


data class GalleryItem(val note: NoteShowBean, val path: String, val localDate: LocalDate)

@OptIn(UnstableSaltApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GalleryPage(
    navHostController: NavHostController
) {
    val noteState = LocalMemosState.current
    val memos = remember { mutableStateMapOf<String, List<GalleryItem>>() }

    LaunchedEffect(Unit) {
        val galleryList = mutableListOf<GalleryItem>()
        noteState.notes.forEach { noteShowBean ->
            val localDate = Instant.ofEpochMilli(noteShowBean.note.createTime).atZone(ZoneId.systemDefault()).toLocalDate()
            noteShowBean.note.attachments.forEach { attachment ->
                galleryList.add(GalleryItem(noteShowBean, attachment.path, localDate))
            }
        }
        val map: Map<String, List<GalleryItem>> = galleryList.groupBy { it.localDate.yearMonth.toString() }
        memos.clear()
        memos.putAll(map)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = SaltTheme.colors.background)
            .padding(top = 30.dp)
    ) {
        TitleBar(
            onBack = {
                navHostController.debouncedPopBackStack()
            },
            text = stringResource(R.string.gallery)
        )
        if (memos.isEmpty()) {
            // 相册为空
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🖼️",
                    style = SaltTheme.textStyles.main.copy(fontSize = 64.sp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "暂无照片",
                    style = SaltTheme.textStyles.main.copy(fontSize = 16.sp),
                    color = SaltTheme.colors.text
                )
                Text(
                    text = "添加笔记时上传图片即可在画廊中查看",
                    style = SaltTheme.textStyles.sub.copy(fontSize = 14.sp),
                    color = SaltTheme.colors.text,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
//            LazyVerticalGrid(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(start = 12.dp, end = 12.dp),
//                columns = GridCells.Fixed(3),
//                content = {
//                    memos.toSortedMap(compareByDescending { it }).forEach { (month, list) ->
//                        // 横跨所有的列
//                        item(span = { GridItemSpan(this.maxLineSpan) }, content = {
//                            Row(
//                                Modifier
//                                    .fillMaxWidth()
//                                    .padding(12.dp)
//                            ) {
//                                Spacer(Modifier.height(12.dp))
//                                Text(text = month, style = SaltTheme.textStyles.main.copy(fontSize = 18.sp).copy(fontWeight = FontWeight.Bold))
//                                Spacer(modifier = Modifier.height(12.dp))
//                            }
//                        })
//                        items(list.size) {
//                            AsyncImage(
//                                modifier = Modifier
//                                    .size(120.dp)
//                                    .padding(2.dp)
//                                    .clip(RoundedCornerShape(8.dp))
//                                    .clickable {
//                                        navHostController.navigate(route = Screen.InputDetail(list[it].note.note.noteId))
//                                    },
//                                contentScale = ContentScale.Crop,
//                                model = list[it].path,
//                                contentDescription = null
//                            )
//                        }
//                        item(span = { GridItemSpan(this.maxLineSpan) }, content = {
//                            Spacer(modifier = Modifier.height(20.dp))
//                        })
//                    }
//                }
//            )
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                columns = GridCells.Fixed(4),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                content = {
                    memos.toSortedMap(compareByDescending { it }).forEach { (month, list) ->
                        item(span = { GridItemSpan(this.maxLineSpan) }, content = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(16.dp)
                                            .background(SaltTheme.colors.text, shape = RoundedCornerShape(2.dp))
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = month,
                                        style = SaltTheme.textStyles.main.copy(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = SaltTheme.colors.text
                                    )
                                }
                            }
                        })
                        items(list.size) { index ->
                            val item = list[index]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable {
                                        val monthImageList = list.map { it.path }
                                        val timestamps = list.map { it.note.note.createTime }
                                        val currentIndex = index
                                        navHostController.navigate(Screen.PictureDisplay(monthImageList,
                                            currentIndex, timestamps))
                                    }
                            ) {
                                AsyncImage(
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    model = item.path,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            )
        }

    }

}