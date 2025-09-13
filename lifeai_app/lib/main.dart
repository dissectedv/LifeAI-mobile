import 'package:flutter/material.dart';
// import 'package:lifeai_app/features/login/main_login.dart';
import 'package:lifeai_app/features/criacao_perfil/main_criacao_perfil.dart'; // caminho certo

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'LifeAI',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF673AB7)),
        useMaterial3: true,
      ),
      debugShowCheckedModeBanner: false,
      home: const CriacaoPerfil(), // tela inicial
    );
  }
}
