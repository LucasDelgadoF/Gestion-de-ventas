package com.example.gestionventas.ui.venta

import androidx.lifecycle.ViewModel
import com.example.gestionventas.data.repository.AuthRepository
import com.example.gestionventas.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val error: String? = null
)

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado

    fun onUsernameChange(valor: String) {
        _estado.update { it.copy(username = valor, usernameError = null, error = null) }
    }

    fun onPasswordChange(valor: String) {
        _estado.update { it.copy(password = valor, passwordError = null, error = null) }
    }

    /** Valida campos y credenciales; si todo es correcto invoca onSuccess con el usuario. */
    fun submit(onSuccess: (Usuario) -> Unit) {
        val a = _estado.value
        val uErr = if (a.username.isBlank()) "Ingrese su usuario" else null
        val pErr = if (a.password.isBlank()) "Ingrese su contraseña" else null
        if (uErr != null || pErr != null) {
            _estado.update { it.copy(usernameError = uErr, passwordError = pErr) }
            return
        }
        val usuario = repo.login(a.username.trim(), a.password)
        if (usuario == null) {
            _estado.update { it.copy(error = "Credenciales inválidas") }
        } else {
            _estado.update { LoginUiState() }
            onSuccess(usuario)
        }
    }
}
