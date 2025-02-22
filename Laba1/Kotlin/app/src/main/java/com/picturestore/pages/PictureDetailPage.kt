package com.picturestore.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.picturestore.data.Picture
import com.picturestore.data.Review
import com.picturestore.data.User
import com.picturestore.service.PictureService
import com.picturestore.service.UserService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PictureDetailPage(
    picture: Picture,
    userId: String,
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val pagerState = com.google.accompanist.pager.rememberPagerState(initialPage = picture.urlsImage.size * 500)
    val pictureService = remember { PictureService() }
    val userService = remember { UserService() }
    var user = User.empty(userId)
    var username by remember { mutableStateOf(user.firstName) }
    var isFavorite by remember { mutableStateOf(user.favourites.contains(picture.id)) }
    var updatedPicture by remember { mutableStateOf(picture) }

    LaunchedEffect(userId) {
        userService.getProfileStream(userId).collectLatest { users ->
            user=users
            isFavorite=user.favourites.contains(picture.id)
            username=user.firstName
        }
    }

    LaunchedEffect(pictureService) {
        pictureService.getPicture(picture.id).collectLatest { pictures ->
            updatedPicture=pictures
        }
    }

    Scaffold(containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = updatedPicture.name)
                },
                actions = {
                    IconButton(onClick = {
                        isFavorite = !isFavorite
                        scope.launch {
                            try {
                                userService.toggleFavorite(updatedPicture.id, userId)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Favorites update error",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            com.google.accompanist.pager.HorizontalPager(
                count = updatedPicture.urlsImage.size * 1000,
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) { page ->
                AsyncImage(
                    model = updatedPicture.urlsImage[page % updatedPicture.urlsImage.size],
                    contentDescription = "Picture Image",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                updatedPicture.urlsImage.forEachIndexed { index, _ ->
                    val selected = (pagerState.currentPage % updatedPicture.urlsImage.size) == index
                    val size = if (selected) 12.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(size)
                            .background(
                                color = if (selected) Color(0xFFA700EE) else Color.Gray,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(listOf( updatedPicture.style, updatedPicture.genre, updatedPicture.type)) { genre ->
                    AssistChip(
                        onClick = {
                        },
                        label = { Text(text = genre, color = Color.White) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.Gray)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            DetailRow(title = "Авторы", value = updatedPicture.authors.joinToString (", "))
            DetailRow(title = "Материалы", value = updatedPicture.materials.joinToString (", "))
            DetailRow(title = "Год создания", value = updatedPicture.year.toString())
            DetailRow(title = "Размер", value = updatedPicture.size)
            Spacer(modifier = Modifier.height(20.dp))
            Text("Описание", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(updatedPicture.moreInformation, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Text("Особенности", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(updatedPicture.specialInformation, style = MaterialTheme.typography.bodyMedium)
            AddReviewPage(updatedPicture, username)
            ReviewsPage(updatedPicture.reviews)
        }
    }
}

@Composable
fun AddReviewPage(picture: Picture, userName: String) {
    var rating by remember { mutableStateOf(1) }
    var comment by remember { mutableStateOf("") }
    val context = LocalContext.current
    val pictureService = remember { PictureService() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Оценить", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Row {
            (1..5).forEach { star ->
                IconButton(onClick = { rating = star }) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (star <= rating) Color(0xFFA700EE) else Color.Gray
                    )
                }
            }
        }

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Ваш комментарий") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (comment.isNotBlank()) {
                    pictureService.addReview(picture.id, userName, rating, comment) { success ->
                        if (success) {
                            Toast.makeText(context, "Отзыв добавлен!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Отправить")
        }
    }
}

@Composable
fun ReviewsPage(reviews: List<Review>) {
    if (reviews.isNullOrEmpty()) {
        Text("Нет отзывов", modifier = Modifier.padding(16.dp))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        reviews.forEach { review ->
            ReviewItem(review)
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Оценка:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                repeat(review.rating) {
                    Icon(Icons.Default.Star, contentDescription = "Star", tint = Color.Yellow)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(review.text, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Автор: ${review.username}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun DetailRow(title: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("$title: ", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}