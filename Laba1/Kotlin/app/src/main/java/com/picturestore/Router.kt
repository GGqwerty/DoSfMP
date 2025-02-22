package com.picturestore

sealed class Router(val route: String) {
    object Home : Router("/home")
    object AuthPage : Router("/authPage")
    object AuthQualifier : Router("/authQualifier")
    object VerifyEmailPage : Router("/verifyEmailPage")
    object PictureDetail : Router("/pictureDetail")
}