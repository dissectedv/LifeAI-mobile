import 'package:flutter/material.dart';
import 'package:lifeai_app/features/login/pages/login.dart';
import 'package:lifeai_app/features/login/pages/register.dart';
// import 'package:app_flutter/data/services/api_login.dart';
// import 'package:app_flutter/widgets/bottom_nav.dart';
// import 'package:lifeai_app/widgets/bottom_nav.dart';

class MainLogin extends StatefulWidget {
  static final String routeName = '/page1';

  const MainLogin({Key? key}) : super(key: key);

  @override
  State<MainLogin> createState() => _MainLogin();
}

class _MainLogin extends State<MainLogin> {
  final _formKey = GlobalKey<FormState>();
  final _userController = TextEditingController();
  final _passController = TextEditingController();
  bool _loading = false;

  void _login() async {
    if (_formKey.currentState!.validate()) {
      setState(() => _loading = true);

      // chamada API (exemplo)
      // final token = await ApiLogin.login(
      //   _userController.text.trim(),
      //   _passController.text.trim(),
      // );

      setState(() => _loading = false);
    }
  }

  // 👇 Função chamada ao clicar em "REGISTRE-SE"
  void _goToRegister() {
    setState(() {
      showLogin = false; // troca o container pelo RegisterAccount
    });
  }

  // 👇 Função chamada ao clicar em "Entrar com Google"
  void _handleGoogleLogin() {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text("Login com Google ainda não implementado."),
        backgroundColor: Colors.blue,
      ),
    );
  }

  bool showLogin = true; // alterna entre login e registro

  @override
  Widget build(BuildContext context) {
    final alturaTela = MediaQuery.of(context).size.height;

    return Scaffold(
      backgroundColor: const Color(0xFF061721),
      body: Stack(
        children: [
          // Logo e título no topo
          Align(
            alignment: Alignment.topCenter,
            child: Padding(
              padding: const EdgeInsets.only(top: 60),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: const [
                  // Image.asset(
                  //   'assets/images/3.0x/lifeAI_logo.png',
                  //   height: 150,
                  // ),
                  SizedBox(height: 10),
                  Text(
                    'Bem-vindo de volta!',
                    style: TextStyle(
                      fontSize: 20,
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Container de login ou registro na parte inferior
          Align(
            alignment: Alignment.bottomCenter,
            child: showLogin
                ? Login(
                    formKey: _formKey,
                    userController: _userController,
                    passController: _passController,
                    loading: _loading,
                    onLogin: _login,
                    onRegister: _goToRegister, // <-- novo
                    onGoogleLogin: _handleGoogleLogin, // <-- novo
                  )
                : const RegisterAccount(),
          ),
        ],
      ),
    );
  }
}
