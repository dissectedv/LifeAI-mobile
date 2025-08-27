import 'package:flutter/material.dart';

class MainSaude extends StatelessWidget {
  static final String routeName = '/MainSaude';

  MainSaude({ Key? key }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text('Saúde'),),
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