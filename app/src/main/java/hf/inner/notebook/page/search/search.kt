package hf.inner.notebook.page.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.room.util.query
import hf.inner.notebook.R
import hf.inner.notebook.component.NoteCard
import hf.inner.notebook.component.NoteCardFrom
import hf.inner.notebook.page.router.debouncedPopBackStack
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(navController: NavHostController, searchViewModel: SearchViewModel = hiltViewModel()) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchBarExpanded by rememberSaveable { mutableStateOf(false) }
    val searchQuery by searchViewModel.query.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val filterList by searchViewModel.dataFlow.collectAsState(initial = emptyList())


    Box(Modifier.fillMaxSize()) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = { newQuery ->
                        searchViewModel.onQuery(newQuery)
                    },
                    onSearch = {
                        searchBarExpanded = true
                        searchViewModel.onSearch(it)
                        keyboardController?.hide()
                    },
                    expanded = searchBarExpanded,
                    onExpandedChange = { },
                    placeholder = {
                        Text(stringResource(id = R.string.search_hint))
                    },
                    leadingIcon = {
                        IconButton(onClick = { navController.debouncedPopBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(id = R.string.back)
                            )
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            searchViewModel.clearSearchQuery()
                        }) {
                            Icon(imageVector = Icons.Rounded.Clear, contentDescription = "")
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .focusRequester(focusRequester),
            expanded = searchBarExpanded,
            onExpandedChange = { if (!it) navController.debouncedPopBackStack() }
        ) {
            LazyColumn(
                Modifier.fillMaxSize().padding()
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(count = filterList.size, key = { it }) { index ->
                    NoteCard(noteShowBean = filterList[index], navHostController = navController, from = NoteCardFrom.SEARCH)
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
        LaunchedEffect(Unit) {
            delay(300)
            focusRequester.requestFocus()
        }
    }
}
