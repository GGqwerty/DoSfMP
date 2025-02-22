package com.picturestore.pages.MainSections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.picturestore.data.Picture
import com.picturestore.data.User
import com.picturestore.service.PictureService
import com.picturestore.service.UserService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

@Composable
fun FavoritesList(
    userId: String,
    pictureService: PictureService,
    userService: UserService,
    navController: NavController
) {
    if (userId.isBlank()) {
        return
    }
    var user by remember { mutableStateOf(User.empty(userId)) }
    var pictures by remember { mutableStateOf<List<Picture>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(pictureService, userId) {
        pictureService.getFavoritePictures(userId).collectLatest { favPictures ->
            pictures = favPictures
        }
    }

    LaunchedEffect(userId) {
        userService.getProfileStream(userId).collectLatest { users ->
            user = users
        }
    }

    if (pictures.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No favorites pictures")
        }
    } else {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            itemsIndexed(pictures) { index, picture ->
                PictureCard(
                    picture = picture,
                    user = user,
                    navController = navController,
                    userService = userService,
                    scope = scope
                )
            }
        }
    }
}