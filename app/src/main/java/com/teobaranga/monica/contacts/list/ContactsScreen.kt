package com.teobaranga.monica.contacts.list

import ContactsNavGraph
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.ContactDetailDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.teobaranga.monica.MonicaBackground
import com.teobaranga.monica.contacts.list.model.Contact
import com.teobaranga.monica.ui.MonicaSearchBar
import com.teobaranga.monica.ui.PreviewPixel4
import com.teobaranga.monica.ui.avatar.UserAvatar
import com.teobaranga.monica.ui.plus
import com.teobaranga.monica.ui.theme.MonicaTheme
import kotlinx.coroutines.flow.flowOf

@Destination<ContactsNavGraph>(start = true)
@Composable
fun Contacts(navigator: DestinationsNavigator) {
    val viewModel = hiltViewModel<ContactsViewModel>()
    val userAvatar by viewModel.userAvatar.collectAsStateWithLifecycle()
    val lazyItems = viewModel.items.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    ContactsScreen(
        userAvatar = userAvatar,
        onAvatarClick = {
            // TODO
        },
        lazyItems = lazyItems,
        isRefreshing = isRefreshing,
        onRefresh = {
            viewModel.refresh()
        },
        onContactSelected = { contactId ->
            navigator.navigate(ContactDetailDestination(contactId))
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsScreen(
    userAvatar: UserAvatar?,
    onAvatarClick: () -> Unit,
    lazyItems: LazyPagingItems<Contact>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onContactSelected: (Int) -> Unit,
) {
    val colors = arrayOf(
        0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.78f),
        0.75f to MaterialTheme.colorScheme.background.copy(alpha = 0.78f),
        1.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.0f),
    )
    MonicaSearchBar(
        modifier = Modifier
            .background(Brush.verticalGradient(colorStops = colors))
            .statusBarsPadding()
            .padding(top = 16.dp, bottom = 20.dp),
        userAvatar = {
            if (userAvatar != null) {
                UserAvatar(
                    userAvatar = userAvatar,
                    onClick = onAvatarClick,
                )
            }
        },
    )
    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            onRefresh()
        }
    }
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = WindowInsets.statusBars.asPaddingValues() + PaddingValues(
                top = SearchBarDefaults.InputFieldHeight + 30.dp,
                bottom = 20.dp,
            ),
        ) {
            when (lazyItems.loadState.refresh) {
                is LoadState.Error -> {
                    // TODO
                }

                is LoadState.Loading,
                is LoadState.NotLoading,
                -> {
                    items(
                        count = lazyItems.itemCount,
                        key = {
                            val contact = lazyItems[it]
                            contact?.id ?: Int.MIN_VALUE
                        },
                    ) {
                        val contact = lazyItems[it]
                        if (contact != null) {
                            ContactItem(
                                contact = contact,
                                onContactSelected = onContactSelected,
                            )
                        }
                    }
                }
            }
        }
        PullToRefreshContainer(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = SearchBarDefaults.InputFieldHeight)
                .align(Alignment.TopCenter),
            state = pullRefreshState,
        )
    }
}

@Composable
private fun ContactItem(contact: Contact, onContactSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onContactSelected(contact.id)
            }
            .padding(
                horizontal = 20.dp,
                vertical = 6.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            modifier = Modifier
                .size(48.dp),
            userAvatar = contact.userAvatar,
            onClick = {
                onContactSelected(contact.id)
            },
        )
        Text(
            modifier = Modifier
                .padding(start = 12.dp),
            text = contact.completeName,
        )
    }
}

@PreviewPixel4
@Composable
private fun PreviewContactsScreen() {
    MonicaTheme {
        MonicaBackground {
            val lazyItems = flowOf(
                PagingData.from(
                    listOf(
                        Contact(
                            id = 1,
                            firstName = "Alice",
                            lastName = null,
                            completeName = "Alice",
                            initials = "A",
                            avatarUrl = null,
                            avatarColor = "#FF0000",
                            updated = null,
                        ),
                        Contact(
                            id = 2,
                            firstName = "Bob",
                            lastName = null,
                            completeName = "Bob",
                            initials = "B",
                            avatarUrl = null,
                            avatarColor = "#00FF00",
                            updated = null,
                        ),
                    ),
                ),
            )
            ContactsScreen(
                userAvatar = UserAvatar(
                    contactId = 1,
                    initials = "TB",
                    color = "#FF0000",
                    avatarUrl = null,
                ),
                onAvatarClick = { },
                lazyItems = lazyItems.collectAsLazyPagingItems(),
                isRefreshing = false,
                onRefresh = { },
                onContactSelected = { },
            )
        }
    }
}
