package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.repository.AuthRepository
import com.example.coffeeshop.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationState())
    val uiState: StateFlow<RegistrationState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onPasswordVisibilityChange() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun onShowSuccessDialogChange(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSuccessDialog = show)
    }

    fun register() {
        val state = _uiState.value
        if (state.name.isEmpty() || state.email.isEmpty() || state.password.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Все поля должны быть заполнены")
            return
        }
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = authRepository.register(state.email, state.password, state.name, "BUYER")) {
                is AuthResult.Success -> _uiState.value = state.copy(isLoading = false, showSuccessDialog = true)
                is AuthResult.Error -> _uiState.value = state.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class RegistrationState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSuccessDialog: Boolean = false
)
