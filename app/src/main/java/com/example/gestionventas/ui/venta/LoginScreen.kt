package com.example.gestionventas.ui.venta

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import com.example.gestionventas.model.Usuario

@Composable
fun LoginScreen(
    vm: LoginViewModel,
    onLoginOk: (Usuario) -> Unit
) {
    val estado by vm.estado.collectAsState()
    var mostrarClave by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(72.dp)
        )
        Text("Gestión de Ventas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text("Registro y validación de ventas (MVP académico)", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        CampoValidado(
            value = estado.username,
            onValueChange = vm::onUsernameChange,
            label = "Usuario",
            icono = Icons.Default.Person,
            error = estado.usernameError
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = estado.password,
            onValueChange = vm::onPasswordChange,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { mostrarClave = !mostrarClave }) {
                    Icon(
                        if (mostrarClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (mostrarClave) "Ocultar contraseña" else "Ver contraseña"
                    )
                }
            },
            isError = estado.passwordError != null,
            supportingText = {
                AnimatedVisibility(visible = estado.passwordError != null) {
                    Text(estado.passwordError ?: "", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = estado.error != null) {
            Text(
                text = estado.error ?: "",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = { vm.submit(onLoginOk) }, modifier = Modifier.fillMaxWidth()) {
            Text("Iniciar sesión")
        }

        Spacer(Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Cuentas de prueba (ficticias)", fontWeight = FontWeight.Bold)
                Text("Vendedor: vendedor1 / vend123")
                Text("Vendedor: vendedor2 / vend123")
                Text("Encargado: encargado / enc123")
            }
        }
    }
}
