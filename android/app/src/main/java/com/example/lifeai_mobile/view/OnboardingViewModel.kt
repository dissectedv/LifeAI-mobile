package com.example.lifeai_mobile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InputType { TEXT, GENDER, NUMBER }

data class OnboardingStep(
    val progressText: String,
    val questionText: String,
    val inputType: InputType
)

class OnboardingViewModel : ViewModel() {

    private val steps = listOf(
        OnboardingStep("1/6", "Qual seu nome?", InputType.TEXT),
        OnboardingStep("2/6", "Qual seu gênero?", InputType.GENDER),
        OnboardingStep("3/6", "Qual sua idade?", InputType.NUMBER),
        OnboardingStep("4/6", "Qual sua altura (cm)?", InputType.NUMBER),
        OnboardingStep("5/6", "Qual seu peso (kg)?", InputType.NUMBER),
        OnboardingStep("6/6", "Qual o seu principal objetivo com sua saúde hoje?", InputType.TEXT)
    )

    private val _currentStepIndex = MutableStateFlow(0)
    val currentStepIndex = _currentStepIndex.asStateFlow()

    private val _currentStep = MutableStateFlow(steps[0])
    val currentStep = _currentStep.asStateFlow()

    private val _navigateToHome = MutableSharedFlow<Unit>()
    val navigateToHome = _navigateToHome.asSharedFlow()

    var name by mutableStateOf("")
    var nameError by mutableStateOf<String?>(null)

    var gender by mutableStateOf("")

    var age by mutableStateOf("")
    var ageError by mutableStateOf<String?>(null)

    var height by mutableStateOf("")
    var heightError by mutableStateOf<String?>(null)

    var weight by mutableStateOf("")
    var weightError by mutableStateOf<String?>(null)

    var objective by mutableStateOf("")
    var objectiveError by mutableStateOf<String?>(null)


    val isNextButtonEnabled: Boolean
        get() = when (_currentStepIndex.value) {
            0 -> name.isNotBlank()
            1 -> gender.isNotBlank()
            2 -> age.isNotBlank()
            3 -> height.isNotBlank()
            4 -> weight.isNotBlank()
            5 -> objective.isNotBlank()
            else -> false
        }

    fun onNameChange(newValue: String) {
        name = newValue
        if (nameError != null) nameError = null
    }
    fun onAgeChange(newValue: String) {
        age = newValue
        if (ageError != null) ageError = null
    }
    fun onHeightChange(newValue: String) {
        height = newValue
        if (heightError != null) heightError = null
    }
    fun onWeightChange(newValue: String) {
        weight = newValue
        if (weightError != null) weightError = null
    }
    fun onObjectiveChange(newValue: String) {
        objective = newValue
        if (objectiveError != null) objectiveError = null
    }

    private fun validateCurrentStep(): Boolean {
        when (_currentStepIndex.value) {
            0 -> {
                nameError = if (name.length < 2) "Por favor, insira um nome válido." else null
                return nameError == null
            }
            2 -> {
                val ageAsInt = age.toIntOrNull()
                ageError = when {
                    ageAsInt == null -> "Por favor, insira um número."
                    ageAsInt !in 10..120 -> "Por favor, insira uma idade válida."
                    else -> null
                }
                return ageError == null
            }
            3 -> {
                val heightAsInt = height.toIntOrNull()
                heightError = when {
                    heightAsInt == null -> "Por favor, insira um número."
                    heightAsInt !in 100..250 -> "Por favor, insira uma altura válida em cm."
                    else -> null
                }
                return heightError == null
            }
            4 -> {
                val weightAsInt = weight.toIntOrNull()
                weightError = when {
                    weightAsInt == null -> "Por favor, insira um número."
                    weightAsInt !in 30..300 -> "Por favor, insira um peso válido em kg."
                    else -> null
                }
                return weightError == null
            }
            5 -> {
                objectiveError = if (objective.length < 5) "Por favor, detalhe um pouco mais seu objetivo." else null
                return objectiveError == null
            }
            else -> return true
        }
    }

    fun onNext() {
        if (!isNextButtonEnabled) return
        if (!validateCurrentStep()) return

        if (_currentStepIndex.value < steps.lastIndex) {
            _currentStepIndex.update { it + 1 }
            _currentStep.value = steps[_currentStepIndex.value]
        } else {
            viewModelScope.launch {
                _navigateToHome.emit(Unit)
            }
        }
    }

    fun onBack() {
        if (_currentStepIndex.value > 0) {
            _currentStepIndex.update { it - 1 }
            _currentStep.value = steps[_currentStepIndex.value]
        }
    }
}