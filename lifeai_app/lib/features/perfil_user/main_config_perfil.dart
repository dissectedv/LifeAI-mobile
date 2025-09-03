import 'package:flutter/material.dart';

class MainPerfil extends StatelessWidget {
  static final String routeName = '/MainPerfil';

  MainPerfil({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Color(0xFF0D1C27),
      appBar: AppBar(
        title: const Text(''),
        backgroundColor: const Color(0xFF0D1C27),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [],
        ),
      ),
    );
  }
}
