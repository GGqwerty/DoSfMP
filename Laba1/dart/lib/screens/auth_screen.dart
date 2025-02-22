import 'package:flutter/material.dart';

class AuthScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Авторизация")),
      body: Center(
        child: Text("Здесь будет форма авторизации"),
      ),
    );
  }
}