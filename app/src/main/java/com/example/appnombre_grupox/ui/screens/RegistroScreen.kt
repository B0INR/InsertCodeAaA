package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import auth.GoogleLogin
import androidx.credentials.exceptions.GetCredentialCancellationException
import data.model.RegistroUsuarios
import data.model.Usuario
import kotlinx.coroutines.launch

@Composable
fun RegistroScreen(
    onVolverLogin: () -> Unit,
    onGoogleRegistrado: (Usuario) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var repetir by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var exito by remember { mutableStateOf(false) }
    var registrandoGoogle by remember { mutableStateOf(false) }
    val fondo = Color(0xFF080E20)
    val blanco = Color.White
    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedTextColor = blanco,
        unfocusedTextColor = blanco,
        focusedLabelColor = Color(0xFFB8C6DB),
        unfocusedLabelColor = Color(0xFFB8C6DB),
        focusedBorderColor = Color(0xFF60A5FA),
        unfocusedBorderColor = Color(0xFF56657D),
        cursorColor = Color(0xFF60A5FA)
    )

    Column(
        modifier = Modifier.fillMaxSize().background(fondo)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Crear cuenta InsertCode", color = blanco, style = MaterialTheme.typography.headlineMedium)
        Text("Crea una cuenta de prueba en este dispositivo o regístrate con Google.",
            color = Color(0xFFB8C6DB))
        OutlinedTextField(
            value = nombre, onValueChange = { nombre = it; error = "" },
            label = { Text("Nombre completo") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true, colors = coloresCampo
        )
        OutlinedTextField(
            value = correo, onValueChange = { correo = it; error = "" },
            label = { Text("Correo electrónico") }, modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = coloresCampo
        )
        OutlinedTextField(
            value = clave, onValueChange = { clave = it; error = "" },
            label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(), singleLine = true,
            colors = coloresCampo
        )
        OutlinedTextField(
            value = repetir, onValueChange = { repetir = it; error = "" },
            label = { Text("Confirmar contraseña") }, modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(), singleLine = true,
            colors = coloresCampo
        )
        if (error.isNotBlank()) Text(error, color = Color(0xFFFF8C8C))
        if (exito) Text("Cuenta local creada. Ya puedes iniciar sesión.", color = Color(0xFF10B981))

        Button(
            onClick = {
                error = if (clave != repetir) "Las contraseñas no coinciden"
                    else RegistroUsuarios.registrar(context, nombre, correo, clave) ?: ""
                exito = error.isEmpty()
            },
            enabled = !exito,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Crear cuenta con correo (local)") }

        HorizontalDivider(color = Color(0xFF263147))
        Text("O REGÍSTRATE CON", modifier = Modifier.fillMaxWidth(), color = Color(0xFFB8C6DB),
            style = MaterialTheme.typography.labelMedium)
        OutlinedButton(
            onClick = {
                if (!registrandoGoogle) {
                    registrandoGoogle = true
                    error = ""
                    scope.launch {
                        try {
                            val usuario = GoogleLogin.iniciarSesion(context)
                            onGoogleRegistrado(usuario)
                        } catch (_: GetCredentialCancellationException) {
                            // El usuario cerró el selector de Google.
                        } catch (e: Exception) {
                            error = "No se pudo registrar con Google: ${e.localizedMessage ?: "revisa tu conexión"}"
                        } finally {
                            registrandoGoogle = false
                        }
                    }
                }
            },
            enabled = !registrandoGoogle,
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) { Text(if (registrandoGoogle) "Conectando con Google..." else "🌐 Registrarse con Google") }

        OutlinedButton(
            onClick = onVolverLogin,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) { Text(if (exito) "Ir a iniciar sesión" else "Volver al inicio de sesión") }
        Text("Las cuentas con correo creadas aquí solo funcionan en este dispositivo. " +
            "Google utiliza Firebase Authentication.", color = Color(0xFFB8C6DB),
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
    }
}
