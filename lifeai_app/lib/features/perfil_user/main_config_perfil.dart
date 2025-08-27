import 'package:flutter/material.dart';

class MainPerfil extends StatelessWidget {
  static final String routeName = '/MainPerfil';

  MainPerfil({ Key? key }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Perfil'),),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            
          ],
        ),
      ),
    );
  }
}