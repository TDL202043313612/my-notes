package hf.inner.notebook.page.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import hf.inner.notebook.R
import hf.inner.notebook.bean.Attachment
import hf.inner.notebook.page.router.LocalRootNavController
import kotlinx.coroutines.launch

//@Composable
//fun InputImage(
//    attachment: Attachment,
//    isEdit: Boolean,
//    delete: (path: String) -> Unit,
//    onclick: () -> Unit = {}
//) {
//    var menuExpanded by remember { mutableStateOf(false) }
//    val scope = rememberCoroutineScope()
//
//    Box {
//        AsyncImage(
//            model = attachment.path,
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxHeight()
//                .aspectRatio(1f)
//                .zIndex(1f)
//                .clip(RoundedCornerShape(2.dp))
//                .clickable {
//                    if (isEdit) {
//                        menuExpanded = true
//                    } else {
//                        onclick()
//                    }
//                },
//            contentScale = ContentScale.Crop
//        )
//        if (isEdit) {
//            DropdownMenu(
//                expanded = menuExpanded,
//                onDismissRequest = { menuExpanded = false},
//                properties = PopupProperties(focusable = false)
//            ) {
//                DropdownMenuItem(
//                    text = { Text(stringResource(R.string.delete)) },
//                    onClick = {
//                        scope.launch {
//                            delete(attachment.path)
//                            menuExpanded = false
//                        }
//                    },
//                    leadingIcon = {
//                        Icon(
//                            Icons.Outlined.Delete,
//                            contentDescription = null
//                        )
//                    }
//                )
//            }
//        }
//    }
//}


//@Composable
//fun InputImage(
//    attachment: Attachment,
//    isEdit: Boolean,
//    delete: (path: String) -> Unit,
//    onclick: () -> Unit = {}
//) {
////    var menuExpanded by remember { mutableStateOf(false) }
//    val scope = rememberCoroutineScope()
//    BadgedBox(
//        modifier = Modifier
//            // 🌟 给 Box 加一点顶部和右边的外边距，给“悬在外面”的角标留出空间，防止被切
//            .padding(top = 8.dp, end = 8.dp)
//            .fillMaxHeight()
//            .aspectRatio(1f),
//        badge = {
//            if (isEdit) {
//                Badge(
//                    modifier = Modifier.clickable {
//                        scope.launch { delete(attachment.path) }
//                    },
//                    containerColor = Color.Black.copy(alpha = 0.5f)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = stringResource(R.string.delete),
//                        modifier = Modifier.size(12.dp),
//                        tint = Color.White
//                    )
//                }
//            }
//        }
//    ) {
//        AsyncImage(
//            model = attachment.path,
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxSize()
////                .fillMaxHeight()
////                .aspectRatio(1f)
//                .zIndex(1f)
//                .clip(RoundedCornerShape(2.dp))
//                .clickable {
//                    onclick()
//                },
//            contentScale = ContentScale.Crop
//        )
//    }
//}

@Composable
fun InputImage(
    attachment: Attachment,
    isEdit: Boolean,
    modifier: Modifier = Modifier.fillMaxHeight().aspectRatio(1f),
    delete: (path: String) -> Unit = {},
    onclick: () -> Unit = {}
) {
//    var menuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box(
        modifier = modifier
    ) {
        AsyncImage(
            model = attachment.path,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isEdit) 6.dp else 0.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onclick() },
            contentScale = ContentScale.Crop
        )
        if (isEdit) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(24.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
                    .clickable {
                        scope.launch { delete(attachment.path) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
    }
}