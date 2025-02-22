package com.picturestore.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.picturestore.data.User
import com.picturestore.pages.MainSections.FavoritesList
import com.picturestore.pages.MainSections.PicturesList
import com.picturestore.pages.MainSections.UserInfoPage
import com.picturestore.service.AuthService
import com.picturestore.service.PictureService
import com.picturestore.service.UserService
import com.picturestore.ui.theme.BackgroundScaffold
import kotlinx.coroutines.flow.first

@Composable
fun MainPage(navController: NavController) {
    val authService = remember { AuthService() }
    val pictureService = remember { PictureService() }
    val userService = remember { UserService() }
    val userId = authService.currentUser?.uid ?: ""
    var user: User = User.empty(userId)
    LaunchedEffect(userId) {
        user = userService.getProfileStream(userId).first()
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isBottomBarVisible by remember { mutableStateOf(true) }

    data class BottomNavItem(val label: String, val icon: ImageVector, val color: Color)

    val bottomNavItems = listOf(
        BottomNavItem("Pictures", Icons.Default.ColorLens, Color(56, 206, 129, 255)),
        BottomNavItem("Favorites", Icons.Default.Favorite, Color(177, 82, 236, 255)),
        BottomNavItem("Profile", Icons.Default.Person, Color(79, 34, 203, 255))
    )

    val screens = listOf<@Composable () -> Unit>(
        {
            PicturesList(
                userId = userId,
                pictureService = pictureService,
                userService = userService,
                navController
            )
        },
        {
            FavoritesList(
                userId = userId,
                pictureService = pictureService,
                userService = userService,
                navController
            )
        },
        {
            UserInfoPage(
                userId = userId,
                email = authService.currentUser?.email ?: "",
                navController = navController
            )
        }
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0 && isBottomBarVisible) {
                    isBottomBarVisible = false
                } else if (available.y > 0 && !isBottomBarVisible) {
                    isBottomBarVisible = true
                }
                return Offset.Zero;
            }
        }
    }

    Scaffold(

        containerColor = Color.Transparent,
        bottomBar = {
            AnimatedVisibility(
                visible = isBottomBarVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NavigationBar(containerColor = Color.Transparent, tonalElevation = 0.dp) {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentIndex == index,
                            onClick = { currentIndex = index },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = item.color,
                                selectedTextColor = item.color,
                                unselectedIconColor = item.color.copy(alpha = 0.4f),
                                unselectedTextColor = item.color.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(nestedScrollConnection)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (!isBottomBarVisible) {
                                isBottomBarVisible = true
                            }
                        }
                    )
                }
        ) {
            Crossfade(targetState = currentIndex) { index ->
                screens[index]()
            }
        }

    }
}