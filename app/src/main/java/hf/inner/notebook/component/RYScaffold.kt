package hf.inner.notebook.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import hf.inner.notebook.R
import hf.inner.notebook.page.router.debouncedPopBackStack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RYScaffold(
    title: String?,
    navController: NavHostController?,
    containerColor: Color = SaltTheme.colors.background,
    actions: @Composable (RowScope.() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    titleContent: @Composable (() -> Unit)? = null,
    snackBarHostState: SnackbarHostState ? = null,
    content: @Composable () -> Unit = {}
) {
    Scaffold(
        containerColor = containerColor,
        topBar = {
            if (title != null || titleContent != null) {
                TopAppBar(
                    modifier = Modifier.height(72.dp),
                    title = {
                        if (titleContent != null) {
                            titleContent()
                        } else if (title != null) {
                            Text(text = title, style = SaltTheme.textStyles.main.copy(fontSize = 24.sp))
                        }
                    },
                    navigationIcon = {
                        if (navController != null) {
                            IconButton(onClick = { navController.debouncedPopBackStack()}) {
                                Icon(
                                    imageVector = Icons.Sharp.ArrowBackIosNew,
                                    contentDescription = stringResource(R.string.back),
                                    tint = SaltTheme.colors.text,
                                )
                            }
                        }
                    },
                    actions = { actions?.invoke(this) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        content = {
            Column(modifier = Modifier.padding(it)) {
                content()
            }
        },
        bottomBar = { bottomBar?.invoke() },
        floatingActionButton = { floatingActionButton?.invoke() },
        snackbarHost = {
            snackBarHostState?.let {
                SnackbarHost(it)
            }
        }
    )
}