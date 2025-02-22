package com.picturestore.pages.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.picturestore.Router
import com.picturestore.ui.theme.BackgroundScaffold

@Composable
fun AuthQualifier(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.popBackStack()
            navController.navigate(Router.AuthPage.route)
        } else {
            if (!currentUser.isEmailVerified) {
                navController.popBackStack()
                navController.navigate(Router.VerifyEmailPage.route)
            } else {
                navController.popBackStack()
                navController.navigate(Router.Home.route)
            }
        }
    }

    BackgroundScaffold {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
    }
}