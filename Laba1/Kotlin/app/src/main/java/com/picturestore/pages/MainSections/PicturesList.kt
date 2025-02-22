package com.picturestore.pages.MainSections

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.picturestore.data.Picture
import com.picturestore.data.User
import com.picturestore.repository.DataHolder.picture
import com.picturestore.service.PictureService
import com.picturestore.service.UserService
import com.picturestore.ui.theme.BackgroundScaffold
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs
import kotlin.math.min

@Composable
fun PicturesList(
    userId: String,
    pictureService: PictureService,
    userService: UserService,
    navController: NavController
) {
    if (userId.isBlank()) {
        return
    }
    var user by remember { mutableStateOf(User.empty(userId)) }
    LaunchedEffect(userId) {
        userService.getProfileStream(userId).collectLatest { users ->
            user = users
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var allPictures by remember { mutableStateOf<List<Picture>>(emptyList()) }
    var filteredPictures by remember { mutableStateOf<List<Picture>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var types by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var styles by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedStyle by remember { mutableStateOf<String?>(null) }
    var genres by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedGenre by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pictureService) {
        pictureService.getPictures().collectLatest { pictures ->
            allPictures = pictures
            filteredPictures = allPictures.filter { picture ->
                picture.name.lowercase().contains(searchQuery.lowercase()) &&
                        (selectedGenre == null || selectedGenre.equals(picture.genre)) &&
                        (selectedStyle == null || selectedStyle.equals(picture.style))&&
                        (selectedType == null || selectedType.equals(picture.type))
            }.sortedBy { picture -> picture.name }
            types =
                pictures.flatMap { listOf(it.type) }.toSet().toList().sorted()
            styles =
                pictures.flatMap { listOf(it.style) }.toSet().toList().sorted()
            genres =
                pictures.flatMap { listOf(it.genre) }.toSet().toList().sorted()
        }
    }

    val listState = rememberLazyListState()

    var previousOffset by remember { mutableStateOf(0f) }
    var heightNow by remember { mutableStateOf(150f) }

    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        val currentOffset = (listState.firstVisibleItemScrollOffset+
                listState.firstVisibleItemIndex)/2+0f
        var scrollDelta = -currentOffset + previousOffset
        if(abs(scrollDelta)>200)
            scrollDelta=0F
        previousOffset = currentOffset
        if(heightNow+scrollDelta<0f)
        {
            heightNow=0f
        } else if(heightNow+scrollDelta>150f)
        {
            heightNow=150f
        } else{
            heightNow=heightNow+scrollDelta
        }
    }

    val alpha by animateFloatAsState(
        targetValue = heightNow/150f,
        animationSpec = tween(durationMillis = 300)
    )

    val height by animateDpAsState(
        targetValue = (heightNow).dp,
        label = "SearchBarHeight"
    )

    BackgroundScaffold {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha)
                    .height(height)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    FilterSearch(
                        modifier = Modifier
                            .weight(1f)
                            .width(105.dp),
                        genres = listOf(null) + types,
                        selectedGenre = selectedType,
                        onGenreSelected = { type ->
                            selectedType = type
                            filteredPictures = allPictures.filter { picture ->
                                picture.name.lowercase().contains(searchQuery.lowercase()) &&
                                        (selectedGenre == null || selectedGenre.equals(picture.genre)) &&
                                        (selectedStyle == null || selectedStyle.equals(picture.style))&&
                                        (selectedType == null || selectedType.equals(picture.type))
                            }.sortedBy { picture -> picture.name }
                        },
                        type = "Types"
                    )
                    Spacer(Modifier.width(16.dp))
                    FilterSearch(
                        modifier = Modifier
                            .weight(1f)
                            .width(105.dp),
                        genres = listOf(null) + styles,
                        selectedGenre = selectedStyle,
                        onGenreSelected = { style ->
                            selectedStyle = style
                            filteredPictures = allPictures.filter { picture ->
                                picture.name.lowercase().contains(searchQuery.lowercase()) &&
                                        (selectedGenre == null || selectedGenre.equals(picture.genre)) &&
                                        (selectedStyle == null || selectedStyle.equals(picture.style))&&
                                        (selectedType == null || selectedType.equals(picture.type))
                            }.sortedBy { picture -> picture.name }
                        },
                        type = "Styles"
                    )
                    Spacer(Modifier.width(16.dp))
                    FilterSearch(
                        modifier = Modifier
                            .weight(1f)
                            .width(105.dp),
                        genres = listOf(null) + genres,
                        selectedGenre = selectedGenre,
                        onGenreSelected = { genre ->
                            selectedGenre = genre
                            filteredPictures = allPictures.filter { picture ->
                                picture.name.lowercase().contains(searchQuery.lowercase()) &&
                                        (selectedGenre == null || selectedGenre.equals(picture.genre)) &&
                                        (selectedStyle == null || selectedStyle.equals(picture.style))&&
                                        (selectedType == null || selectedType.equals(picture.type))
                            }.sortedBy { picture -> picture.name }
                        },
                        type = "Genres"
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        filteredPictures = allPictures.filter { picture ->
                            picture.name.lowercase().contains(searchQuery.lowercase()) &&
                                    (selectedGenre == null || selectedGenre.equals(picture.genre)) &&
                                    (selectedStyle == null || selectedStyle.equals(picture.style))&&
                                    (selectedType == null || selectedType.equals(picture.type))
                        }.sortedBy { picture -> picture.name }
                    },
                    label = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp, 16.dp, 0.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(modifier = Modifier.fillMaxSize(),
                state = listState) {
                items(filteredPictures) { picture ->
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
}

@Composable
fun FilterSearch(
    genres: List<String?>,
    modifier: Modifier,
    selectedGenre: String?,
    onGenreSelected: (String?) -> Unit,
    type: String
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = selectedGenre ?: "All ${type}",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            maxLines = 1,
            label = { Text(type) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = modifier
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            genres.forEach { genre ->
                DropdownMenuItem(
                    text = { Text(text = genre ?: "All ${type}") },
                    onClick = {
                        onGenreSelected(genre)
                        expanded = false
                    }
                )
            }
        }
    }
}