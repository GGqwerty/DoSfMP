package com.picturestore.pages.MainSections

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.picturestore.Router
import com.picturestore.data.Picture
import com.picturestore.data.User
import com.picturestore.repository.DataHolder
import com.picturestore.service.PictureService
import com.picturestore.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PictureCard(
    picture: Picture,
    user: User,
    navController: NavController,
    userService: UserService,
    scope: CoroutineScope
) {
    val context = LocalContext.current
    val isFavorite = user.favourites.contains(picture.id)
    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {
                navController.navigate(Router.PictureDetail.route) {
                    DataHolder.picture = picture;
                    DataHolder.userId = user.id;
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = picture.urlsImage.firstOrNull() ?: "https://t4.ftcdn.net/jpg/04/70/29/97/360_F_470299797_UD0eoVMMSUbHCcNJCdv2t8B2g1GVqYgs.jpg",
                contentDescription = picture.name,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = picture.authors.joinToString( ", "),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(text = "${picture.name}, ${picture.year}", style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${picture.type}, ${picture.style}, ${picture.genre}",
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = picture.size,
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                )
                IconButton(onClick = {
                    scope.launch {
                        try {
                            userService.toggleFavorite(picture.id, user.id)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Favorites update error " + e.message,
                                Toast.LENGTH_LONG
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
            }
        }
    }
}