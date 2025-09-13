import 'package:flutter/material.dart';

class Login extends StatelessWidget {
  final GlobalKey<FormState> formKey;
  final TextEditingController userController;
  final TextEditingController passController;
  final bool loading;
  final VoidCallback onLogin;
  final VoidCallback onRegister;
  final VoidCallback onGoogleLogin;

  const Login({
    super.key,
    required this.formKey,
    required this.userController,
    required this.passController,
    required this.loading,
    required this.onLogin,
    required this.onRegister,
    required this.onGoogleLogin,
  });

  @override
  Widget build(BuildContext context) {
    final alturaTela = MediaQuery.of(context).size.height;

    return Align(
      alignment: Alignment.bottomCenter,
      child: Container(
        height: alturaTela * 0.6, // aumentei um pouco para caber os novos itens
        width: double.infinity,
        padding: const EdgeInsets.all(20),
        decoration: const BoxDecoration(
          color: Color(0xFF102938),
          borderRadius: BorderRadius.only(
            topLeft: Radius.circular(25),
            topRight: Radius.circular(25),
          ),
        ),
        child: Form(
          key: formKey,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Text(
                'Login',
                style: TextStyle(
                  fontSize: 24,
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 20),
              TextFormField(
                controller: userController,
                decoration: const InputDecoration(
                  labelText: "Usuário",
                  prefixIcon: Icon(Icons.person),
                  filled: true,
                  fillColor: Colors.white,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(32)),
                  ),
                ),
              ),
              const SizedBox(height: 10),
              TextFormField(
                controller: passController,
                decoration: const InputDecoration(
                  labelText: "Senha",
                  prefixIcon: Icon(Icons.lock),
                  filled: true,
                  fillColor: Colors.white,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(32)),
                  ),
                ),
                obscureText: true,
              ),
              const SizedBox(height: 20),
              SizedBox(
                width: double.infinity,
                child: OutlinedButton(
                  onPressed: loading ? null : onLogin,
                  style: OutlinedButton.styleFrom(
                    side: const BorderSide(color: Color(0xFF14A2A7), width: 2),
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                  ),
                  child: loading
                      ? const CircularProgressIndicator(color: Colors.indigo)
                      : const Text("Entrar", style: TextStyle(fontSize: 16)),
                ),
              ),
              const SizedBox(height: 15),

              // Texto com link para registro
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text(
                    "Não tem conta? ",
                    style: TextStyle(color: Colors.white),
                  ),
                  TextButton(
                    onPressed: onRegister,
                    child: const Text(
                      "REGISTRE-SE",
                      style: TextStyle(
                        color: Color(0xFF14A2A7),
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 10),

              // Botão para login com Google
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: onGoogleLogin,
                  icon: const Icon(Icons.login, color: Colors.white),
                  label: const Text(
                    "Entrar com Google",
                    style: TextStyle(fontSize: 16),
                  ),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.red,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(32),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
