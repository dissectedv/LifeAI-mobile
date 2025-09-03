import 'package:flutter/material.dart';
import 'package:lifeai_app/features/login/main_login.dart';

void main() {
  runApp(const MyApp()); // primeira função que starta a aplicação.
  // Myapp é o primeiro widget usado no start.
}

class MyApp extends StatelessWidget {
  // TUDO é widget.
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'LifeAI',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF673AB7)),
      ),
      home: MainLogin() ,
    );
  }
}
