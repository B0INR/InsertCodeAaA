package com.example.appnombre_grupox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import data.model.PedidosStorage
import data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import kotlinx.coroutines.launch
import ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val alcance = rememberCoroutineScope()
            var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
            var registroAbierto by remember { mutableStateOf(false) }
            var pantallaActual by remember { mutableStateOf("Dashboard") }
            var productoCotizado by remember { mutableStateOf<Producto?>(null) }
            var abrirFormularioPedido by remember { mutableStateOf(false) }
            var pedidoSeleccionadoId by remember { mutableStateOf<Int?>(null) }
            val pedidos = remember {
                mutableStateListOf<Pedido>().apply {
                    addAll(PedidosStorage.cargar(applicationContext))
                }
            }
            // Se conservan los pedidos al cerrar/reabrir la aplicación.
            val instantanea = pedidos.toList()
            LaunchedEffect(instantanea) {
                PedidosStorage.guardar(applicationContext, instantanea)
            }

            val navegarA: (String) -> Unit = { destino -> pantallaActual = destino }
            val cerrarSesion: () -> Unit = {
                abrirFormularioPedido = false
                pedidoSeleccionadoId = null
                if (FirebaseAuth.getInstance().currentUser != null) {
                    FirebaseAuth.getInstance().signOut()
                    alcance.launch {
                        try {
                            CredentialManager.create(this@MainActivity)
                                .clearCredentialState(ClearCredentialStateRequest())
                        } catch (_: Exception) { /* El cierre de Firebase ya se efectuó. */ }
                    }
                }
                usuarioActual = null
                pantallaActual = "Dashboard"
                productoCotizado = null
                registroAbierto = false
            }

            when {
                usuarioActual == null && registroAbierto -> {
                    RegistroScreen(
                        onVolverLogin = { registroAbierto = false },
                        onGoogleRegistrado = { usuario ->
                            usuarioActual = usuario
                            registroAbierto = false
                            pantallaActual = "Dashboard"
                        }
                    )
                }
                usuarioActual == null -> {
                    LoginScreen(
                        onLoginSuccess = { usuario ->
                            usuarioActual = usuario
                            pantallaActual = "Dashboard"
                        },
                        onRegistrarse = { registroAbierto = true }
                    )
                }
                else -> {
                    val usuario = requireNotNull(usuarioActual)
                    Scaffold(
                        bottomBar = {
                            BarraNavegacion(
                                pantallaActual = pantallaActual,
                                onNavegar = navegarA
                            )
                        }
                    ) { padding ->
                        Box(Modifier.fillMaxSize().padding(padding)) {
                            when (pantallaActual) {
                                "Pedidos" -> PedidosScreen(
                                    onVolver = { navegarA("Dashboard") },
                                    pedidos = pedidos,
                                    productoCotizado = productoCotizado,
                                    onCotizacionConsumida = { productoCotizado = null },
                                    abrirFormulario = abrirFormularioPedido,
                                    onFormularioAbierto = { abrirFormularioPedido = false },
                                    pedidoSeleccionadoId = pedidoSeleccionadoId,
                                    onPedidoSeleccionadoConsumido = { pedidoSeleccionadoId = null }
                                )
                                "Catalogo" -> CatalogoScreen(
                                    onVolver = { navegarA("Dashboard") },
                                    onCotizar = { producto ->
                                        productoCotizado = producto
                                        navegarA("Pedidos")
                                    }
                                )
                                "Gps" -> GpsScreen(
                                    onVolver = { navegarA("Dashboard") },
                                    pedidos = pedidos
                                )
                                "Perfil" -> ProfileScreen(
                                    onVolver = { navegarA("Dashboard") },
                                    usuario = usuario
                                )
                                "Camara" -> CameraScreen(onVolver = { navegarA("Dashboard") })
                                else -> {
                                    PostScreen(
                                        pedidos = pedidos,
                                        onCerrarSesion = cerrarSesion,
                                        onAbrirCamara = { navegarA("Camara") },
                                        onAbrirPerfil = { navegarA("Perfil") },
                                        onAbrirGps = { navegarA("Gps") },
                                        onAbrirCatalogo = { navegarA("Catalogo") },
                                        onAbrirPedidos = {
                                            pedidoSeleccionadoId = null
                                            abrirFormularioPedido = true
                                            navegarA("Pedidos")
                                        },
                                        onSeleccionarPedido = { id ->
                                            abrirFormularioPedido = false
                                            pedidoSeleccionadoId = id
                                            navegarA("Pedidos")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
