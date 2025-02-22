package com.picturestore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.picturestore.pages.MainPage
import com.picturestore.pages.PictureDetailPage
import com.picturestore.pages.auth.AuthPage
import com.picturestore.pages.auth.AuthQualifier
import com.picturestore.pages.auth.VerifyEmailPage
import com.picturestore.repository.DataHolder
import com.picturestore.ui.theme.BackgroundScaffold

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            BackgroundScaffold {
                AppNavigator()
            }
        }
    }

    @Composable
    fun AppNavigator() {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = Router.AuthQualifier.route) {
            composable(Router.AuthQualifier.route) {AuthQualifier(navController) }
            composable(Router.AuthPage.route) { AuthPage(navController) }
            composable(Router.VerifyEmailPage.route) { VerifyEmailPage(navController) }
            composable(Router.Home.route) { MainPage(navController) }
            composable(Router.PictureDetail.route) { PictureDetailPage(DataHolder.picture, DataHolder.userId) }
        }
    }
}

