import 'package:flutter/material.dart';
import 'package:lifeai_app/widgets/bottom_nav.dart';

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
      home: BottomNav(),
    );
  }
}
