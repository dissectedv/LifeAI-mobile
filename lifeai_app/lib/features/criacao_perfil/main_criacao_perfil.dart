import 'package:flutter/material.dart';

class CriacaoPerfil extends StatefulWidget {
  const CriacaoPerfil({super.key});

  @override
  State<CriacaoPerfil> createState() => _CriacaoPerfilState();
}

class _CriacaoPerfilState extends State<CriacaoPerfil> {
  int currentStep = 0;
  final PageController _pageController = PageController();

  final TextEditingController nomeController = TextEditingController();
  final TextEditingController idadeController = TextEditingController();
  final TextEditingController alturaController = TextEditingController();
  final TextEditingController pesoController = TextEditingController();
  String genero = "";
  String objetivo = "";

  late final List<Map<String, dynamic>> _perguntas;

  @override
  void initState() {
    super.initState();
    _perguntas = _inicializarPerguntas();
    nomeController.addListener(_onFieldChanged);
    idadeController.addListener(_onFieldChanged);
    alturaController.addListener(_onFieldChanged);
    pesoController.addListener(_onFieldChanged);
  }

  void _onFieldChanged() {
    setState(() {});
  }

  @override
  void dispose() {
    _pageController.dispose();
    nomeController.removeListener(_onFieldChanged);
    idadeController.removeListener(_onFieldChanged);
    alturaController.removeListener(_onFieldChanged);
    pesoController.removeListener(_onFieldChanged);
    nomeController.dispose();
    idadeController.dispose();
    alturaController.dispose();
    pesoController.dispose();
    super.dispose();
  }

  List<Map<String, dynamic>> _inicializarPerguntas() {
    return [
      {
        "pergunta": "Qual seu nome?",
        "campo": _buildTextField(
          controller: nomeController,
          hint: "Digite seu nome",
        ),
      },
      {"pergunta": "Qual seu gênero?", "campo": _buildGenderChoices()},
      {
        "pergunta": "Qual sua idade?",
        "campo": _buildTextField(
          controller: idadeController,
          hint: "Ex: 25",
          keyboardType: TextInputType.number,
        ),
      },
      {
        "pergunta": "Qual sua altura (cm)?",
        "campo": _buildTextField(
          controller: alturaController,
          hint: "Ex: 175",
          keyboardType: TextInputType.number,
        ),
      },
      {
        "pergunta": "Qual seu peso (kg)?",
        "campo": _buildTextField(
          controller: pesoController,
          hint: "Ex: 70",
          keyboardType: TextInputType.number,
        ),
      },
      {
        "pergunta": "Qual seu principal objetivo?",
        "campo": _buildTextField(
          onChanged: (value) => setState(() => objetivo = value.trim()),
          hint: "Ex: Ganhar massa, emagrecer...",
        ),
      },
    ];
  }

  bool _isCurrentStepValid() {
    switch (currentStep) {
      case 0:
        return nomeController.text.trim().isNotEmpty;
      case 1:
        return genero.isNotEmpty;
      case 2:
        return idadeController.text.trim().isNotEmpty;
      case 3:
        return alturaController.text.trim().isNotEmpty;
      case 4:
        return pesoController.text.trim().isNotEmpty;
      case 5:
        return objetivo.isNotEmpty;
      default:
        return false;
    }
  }

  void _onStepChanged(int step) {
    setState(() {
      currentStep = step;
    });
  }

  void nextStep() {
    if (currentStep < _perguntas.length - 1) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeIn,
      );
    } else {
      debugPrint("--- DADOS DO PERFIL ---");
      debugPrint("Nome: ${nomeController.text}");
      debugPrint("Gênero: $genero");
      debugPrint("Idade: ${idadeController.text}");
      debugPrint("Altura: ${alturaController.text}");
      debugPrint("Peso: ${pesoController.text}");
      debugPrint("Objetivo: $objetivo");
    }
  }

  void prevStep() {
    if (currentStep > 0) {
      _pageController.previousPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeIn,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0D1C27),
      resizeToAvoidBottomInset: true,
      appBar: AppBar(
        backgroundColor: const Color(0xFF0D1C27),
        elevation: 0,
        centerTitle: true,
        leading: currentStep > 0
            ? IconButton(
                icon: const Icon(Icons.arrow_back, color: Colors.white),
                onPressed: prevStep,
              )
            : null,
        title: const Text(
          "Criação de Perfil",
          style: TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
            fontSize: 18,
          ),
        ),
        actions: [
          Center(
            child: Padding(
              padding: const EdgeInsets.only(right: 16.0),
              child: Text(
                "${currentStep + 1}/${_perguntas.length}",
                style: const TextStyle(color: Colors.white70, fontSize: 16),
              ),
            ),
          ),
        ],
      ),
      body: SafeArea(
        child: GestureDetector(
          onTap: () => FocusScope.of(context).unfocus(),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 24.0),
            child: Column(
              children: [
                Expanded(
                  child: PageView.builder(
                    physics:
                        const NeverScrollableScrollPhysics(), // Impede o gesto de arrastar
                    controller: _pageController,
                    onPageChanged: _onStepChanged,
                    itemCount: _perguntas.length,
                    itemBuilder: (context, index) {
                      return _buildPerguntaLayout(
                        _perguntas[index]["pergunta"],
                        _perguntas[index]["campo"],
                      );
                    },
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.only(bottom: 30.0, top: 10.0),
                  child: Column(
                    children: [
                      _buildNavigationButton(),
                      const SizedBox(height: 16),
                      if (currentStep >= 1)
                        Text(
                          "Usamos essa informação para oferecer recomendações de saúde mais adequadas.",
                          textAlign: TextAlign.center,
                          style: TextStyle(
                            color: Colors.white.withOpacity(0.6),
                            fontSize: 12,
                          ),
                        ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildPerguntaLayout(String pergunta, Widget campo) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          pergunta,
          textAlign: TextAlign.center,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 28,
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 40),
        campo,
      ],
    );
  }

  Widget _buildTextField({
    TextEditingController? controller,
    String hint = "",
    TextInputType keyboardType = TextInputType.text,
    ValueChanged<String>? onChanged,
  }) {
    return TextField(
      controller: controller,
      onChanged: onChanged,
      keyboardType: keyboardType,
      textAlign: TextAlign.center,
      style: const TextStyle(color: Colors.white, fontSize: 22),
      decoration: InputDecoration(
        hintText: hint,
        hintStyle: TextStyle(color: Colors.grey.shade600, fontSize: 22),
        border: const UnderlineInputBorder(
          borderSide: BorderSide(color: Colors.white54),
        ),
        enabledBorder: const UnderlineInputBorder(
          borderSide: BorderSide(color: Colors.white54),
        ),
        focusedBorder: const UnderlineInputBorder(
          borderSide: BorderSide(color: Colors.white, width: 2),
        ),
      ),
    );
  }

  Widget _buildGenderChoices() {
    return Wrap(
      spacing: 12,
      runSpacing: 12,
      alignment: WrapAlignment.center,
      children: [
        _buildChoiceChip("Masculino"),
        _buildChoiceChip("Feminino"),
        _buildChoiceChip("Outro"),
      ],
    );
  }

  Widget _buildChoiceChip(String label) {
    final bool isSelected = genero == label;
    return ChoiceChip(
      label: Text(label),
      selected: isSelected,
      onSelected: (bool selected) {
        setState(() {
          if (selected) {
            genero = label;
          }
        });
      },
      backgroundColor: const Color(0xFF1A2A3A),
      selectedColor: const Color(0xFF00E676),
      labelStyle: TextStyle(
        color: isSelected ? Colors.black : Colors.white,
        fontWeight: FontWeight.bold,
        fontSize: 16,
      ),
      avatar: isSelected
          ? const Icon(Icons.check, color: Colors.black, size: 18)
          : null,
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
      shape: const StadiumBorder(),
      side: BorderSide(
        color: isSelected ? const Color(0xFF00E676) : const Color(0xFF3A4A5A),
        width: 1,
      ),
    );
  }

  Widget _buildNavigationButton() {
    final bool isLastStep = currentStep == _perguntas.length - 1;
    final bool isValid = _isCurrentStepValid();

    return SizedBox(
      width: 200,
      height: 50,
      child: ElevatedButton(
        onPressed: isValid ? nextStep : null,
        style: ElevatedButton.styleFrom(
          foregroundColor: Colors.white,
          backgroundColor: Colors.transparent,
          disabledBackgroundColor: Colors.transparent.withOpacity(0.2),
          shape: const StadiumBorder(),
          side: BorderSide(
            color: isValid ? Colors.white : Colors.grey.shade700,
            width: 1.5,
          ),
        ),
        child: Text(
          isLastStep ? "Finalizar" : "Próximo",
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: isValid ? Colors.white : Colors.grey.shade700,
          ),
        ),
      ),
    );
  }
}
