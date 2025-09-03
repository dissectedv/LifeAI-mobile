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
                color: Color(0xFF4194C8),
                fontWeight: FontWeight.bold,
              );
            }
            return const TextStyle(color: Color(0xFF4194C8));
          }),
          // removi iconTheme pra controlar via animação nos destinos
        ),
        child: NavigationBar(
          height: 80,
          backgroundColor: const Color(0xFF061721),
          indicatorColor: Colors.transparent, // sem bolha
          selectedIndex: _indiceAtual,
          onDestinationSelected: (index) {
            setState(() {
              _indiceAtual = index;
            });
          },
          destinations: [
            _buildAnimatedDestination(
              index: 0,
              icon: Icons.home,
              label: "Início",
            ),
            _buildAnimatedDestination(
              index: 1,
              icon: Icons.health_and_safety_sharp,
              label: "Saúde",
            ),
            _buildAnimatedDestination(
              index: 2,
              icon: Icons.mark_unread_chat_alt_outlined,
              label: "Chat IA",
            ),
            _buildAnimatedDestination(
              index: 3,
              icon: Icons.supervised_user_circle_outlined,
              label: "Usuário",
            ),
          ],
        ),
      ),
    );
  }

  NavigationDestination _buildAnimatedDestination({
    required int index,
    required IconData icon,
    required String label,
  }) {
    final bool selecionado = _indiceAtual == index;

    return NavigationDestination(
      label: label,
      icon: TweenAnimationBuilder<double>(
        duration: const Duration(milliseconds: 250),
        curve: Curves.easeOut,
        tween: Tween<double>(
          begin: selecionado ? 26 : 32,
          end: selecionado ? 32 : 26,
        ),
        builder: (context, size, child) {
          return Icon(
            icon,
            size: size,
            color: selecionado
                ? const Color(0xFF1AB1A9) // ativo
                : const Color(0xFF278E88), // inativo
          );
        },
      ),
    );
  }
}
