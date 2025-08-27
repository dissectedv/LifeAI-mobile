import 'package:flutter/material.dart';

class CriacaoPerfil extends StatelessWidget {
  static final String routeName = '/CriacaoPerfil';

  CriacaoPerfil({ Key? key }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Criação de Perfil'),),
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