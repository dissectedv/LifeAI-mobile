import 'package:flutter/material.dart';

class MainSaude extends StatelessWidget {
  static final String routeName = '/MainSaude';

  const MainSaude({super.key});

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
