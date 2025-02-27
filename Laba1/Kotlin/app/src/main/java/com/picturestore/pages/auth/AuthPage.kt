package com.picturestore.pages.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.sharp.Password
import androidx.compose.material.icons.twotone.AlternateEmail
import androidx.compose.material.icons.twotone.Password
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.picturestore.Router
import com.picturestore.service.AuthService
import com.picturestore.ui.theme.BackgroundScaffold
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthPage(navController: NavController) {

    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(true) }
    var showPassword by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val authService = remember { AuthService() }
    val snackbarHostState = remember { SnackbarHostState() }

    fun validate(): Boolean {
        var valid = true
        if (!email.contains("@")) {
            emailError = "Enter correct email"
            valid = false
        } else {
            emailError = null
        }
        if (password.length < 6) {
            passwordError = "Password should be at least 6 symbols"
            valid = false
        } else {
            passwordError = null
        }
        return valid
    }
    BackgroundScaffold {
        Scaffold(
            topBar = { TopAppBar( title = { Text(if (isLogin) "Login" else "Registration") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent))
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.TwoTone.AlternateEmail, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    isError = emailError != null
                )
                if (emailError != null) {
                    Text(
                        text = emailError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) passwordError = null
                    },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Sharp.Password, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordError != null
                )
                if (passwordError != null) {
                    Text(
                        text = passwordError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                if (!validate()) return@launch
                                isLoading = true
                                try {
                                    if (isLogin) {
                                        val user = authService.signIn(email.trim(), password.trim())
                                        if (user != null) {
                                            if (!user.isEmailVerified) {
                                                navController.popBackStack()
                                                navController.navigate(Router.VerifyEmailPage.route)
                                            } else {
                                                FirebaseFirestore.getInstance().collection("users")
                                                    .document(user.uid)
                                                    .update("emailVerified", true)
                                                    .await()
                                                navController.popBackStack()
                                                navController.navigate(Router.Home.route)
                                            }
                                        }
                                    } else {
                                        authService.signUp(email.trim(), password.trim())
                                        navController.popBackStack()
                                        navController.navigate(Router.VerifyEmailPage.route)
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(e.toString())
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isLogin) "Login" else "Register")
                    }
                    TextButton(
                        onClick = { isLogin = !isLogin },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isLogin) "Create an account" else "Already have? Login",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}