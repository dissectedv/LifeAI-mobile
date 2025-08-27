import 'package:flutter/material.dart';
import 'package:lifeai_app/features/chatIA/main_chatIa.dart';
import 'package:lifeai_app/features/home/main_home.dart';
import 'package:lifeai_app/features/perfil_user/main_config_perfil.dart';
import 'package:lifeai_app/features/saude/main_saude.dart';

class BottomNav extends StatefulWidget {
  @override
  _BottomNav createState() => _BottomNav();
}

class _BottomNav extends State<BottomNav> {
  int _indiceAtual = 0;

  final List<Widget> _pages = [
    MainHome(),
    MainSaude(),
    ChatIA(),
    MainPerfil(),
    Center(
      child: Text("Usuário", style: TextStyle(color: Colors.white)),
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _pages[_indiceAtual],

      bottomNavigationBar: NavigationBarTheme(
        data: NavigationBarThemeData(
          labelTextStyle: WidgetStateProperty.resolveWith<TextStyle>((states) {
            if (states.contains(WidgetState.selected)) {
              return const TextStyle(
                color: Colors.white, // título selecionado
                fontWeight: FontWeight.bold,
              );
            }
            return const TextStyle(
              color: Colors.white70, // título não selecionado
            );
          }),
          iconTheme: WidgetStateProperty.resolveWith<IconThemeData>((states) {
            if (states.contains(WidgetState.selected)) {
              return const IconThemeData(color: Colors.white); // ícone ativo
            }
            return const IconThemeData(color: Colors.white70); // ícone inativo
          }),
        ),
        child: NavigationBar(
          height: 70,
          backgroundColor: const Color(0xFF061721),
          indicatorColor: Colors.indigo.shade400, // bolha de seleção
          selectedIndex: _indiceAtual,
          onDestinationSelected: (index) {
            setState(() {
              _indiceAtual = index;
            });
          },
          destinations: const [
            NavigationDestination(
              icon: Icon(Icons.home),
              label: "Início"),
            NavigationDestination(
              icon: Icon(Icons.health_and_safety_sharp),
              label: "Saúde",
            ),
            NavigationDestination(
              icon: Icon(Icons.mark_unread_chat_alt_outlined),
              label: "Chat IA",
            ),
            NavigationDestination(
              icon: Icon(Icons.supervised_user_circle_outlined),
              label: "Usuário",
            ),
          ],
        ),
      ),
    );
  }
}
