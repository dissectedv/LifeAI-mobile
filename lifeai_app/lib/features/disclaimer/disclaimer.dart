import 'package:flutter/material.dart';

class DisclaimerPage extends StatefulWidget {
  const DisclaimerPage({super.key});

  @override
  State<DisclaimerPage> createState() => _DisclaimerPageState();
}

class _DisclaimerPageState extends State<DisclaimerPage> {
  bool _isChecked = false;

  void _onAccept() {
    debugPrint("Termos aceitos. Navegando para a próxima tela...");
    // Adicione a lógica de navegação aqui
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0D1C27),
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.white),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 32.0),
          child: Center(
            child: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Center(
                    child: Text(
                      "Aviso Importante",
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        color: Color(0xFFFFD600),
                        fontSize: 32,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  const SizedBox(height: 32),
                  const Text(
                    "Uso exclusivamente educacional.\nNão substitui orientação médica profissional.",
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      height: 1.5,
                    ),
                  ),
                  const SizedBox(height: 24),
                  const Text(
                    "Ao continuar, você declara que:",
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      height: 1.5,
                    ),
                  ),
                  const SizedBox(height: 16),
                  _buildBulletPoint(
                    "Você é o único responsável pela sua prática.",
                  ),
                  const SizedBox(height: 12),
                  _buildBulletPoint(
                    "Os desenvolvedores não se responsabilizam por acidentes ou lesões.",
                  ),
                  const SizedBox(height: 24),
                  const Text(
                    "Para sua segurança, consulte sempre um profissional de saúde antes de iniciar qualquer atividade.",
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      height: 1.5,
                    ),
                  ),
                  const SizedBox(height: 40),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Align(
                        alignment: Alignment.centerLeft,
                        child: GestureDetector(
                          onTap: () {
                            setState(() {
                              _isChecked = !_isChecked;
                            });
                          },
                          child: Container(
                            color: Colors.transparent,
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Checkbox(
                                  value: _isChecked,
                                  onChanged: (bool? value) {
                                    setState(() {
                                      _isChecked = value ?? false;
                                    });
                                  },
                                  checkColor: const Color(0xFF0D1C27),
                                  activeColor: Colors.white,
                                  side: const BorderSide(
                                    color: Colors.white,
                                    width: 1.5,
                                  ),
                                ),
                                const SizedBox(width: 22),
                                const Flexible(
                                  child: Text(
                                    "Li e entendi as condições acima",
                                    style: TextStyle(
                                      // ÚNICA ALTERAÇÃO REALIZADA AQUI
                                      color: Color(0xFFBDBDBD),
                                      fontSize: 14,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      ),
                      const SizedBox(height: 20),
                      SizedBox(
                        width: 250,
                        height: 50,
                        child: ElevatedButton(
                          onPressed: _isChecked ? _onAccept : null,
                          style: ElevatedButton.styleFrom(
                            foregroundColor: Colors.white,
                            backgroundColor: Colors.transparent,
                            disabledForegroundColor: Colors.grey.shade700,
                            disabledBackgroundColor: Colors.transparent
                                .withOpacity(0.2),
                            shape: const StadiumBorder(),
                            side: BorderSide(
                              color: _isChecked
                                  ? Colors.white
                                  : Colors.grey.shade700,
                              width: 1.5,
                            ),
                          ),
                          child: Text(
                            "Aceitar",
                            style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                              color: _isChecked
                                  ? Colors.white
                                  : Colors.grey.shade700,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildBulletPoint(String text) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.only(top: 5.0, right: 8.0),
          child: Text("•", style: TextStyle(color: Colors.white, fontSize: 16)),
        ),
        Expanded(
          child: Text(
            text,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 16,
              height: 1.5,
            ),
          ),
        ),
      ],
    );
  }
}
