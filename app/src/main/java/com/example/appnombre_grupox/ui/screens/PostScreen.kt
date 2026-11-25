package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

/** Dashboard comercial: muestra pedidos reales, no publicaciones de JSONPlaceholder. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    pedidos: List<Pedido>,
    onCerrarSesion: () -> Unit,
    onAbrirCamara: () -> Unit,
    onAbrirPerfil: () -> Unit,
    onAbrirGps: () -> Unit,
    onAbrirCatalogo: () -> Unit,
    onAbrirPedidos: () -> Unit,
    onSeleccionarPedido: (Int) -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val formato = remember { NumberFormat.getIntegerInstance(Locale("es", "CL")) }

    Scaffold(
        containerColor = Color(0xFF080E20),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("InsertCode", color = Color.White,
                            style = MaterialTheme.typography.titleLarge)
                        Text("Ventas & Distribución Móvil · DSY1105",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuAbierto = true }) {
                            Text("☰", color = Color.White,
                                style = MaterialTheme.typography.headlineMedium)
                        }
                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false }
                        ) {
                            DropdownMenuItem(text = { Text("📦 Pedidos") }, onClick = {
                                menuAbierto = false
                                onAbrirPedidos()
                            })
                            DropdownMenuItem(text = { Text("🏪 Catálogo") }, onClick = {
                                menuAbierto = false
                                onAbrirCatalogo()
                            })
                            DropdownMenuItem(text = { Text("👤 Perfil") }, onClick = {
                                menuAbierto = false
                                onAbrirPerfil()
                            })
                            DropdownMenuItem(text = { Text("📷 Cámara") }, onClick = {
                                menuAbierto = false
                                onAbrirCamara()
                            })
                            DropdownMenuItem(text = { Text("📍 GPS") }, onClick = {
                                menuAbierto = false
                                onAbrirGps()
                            })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("Cerrar sesión") }, onClick = {
                                menuAbierto = false
                                onCerrarSesion()
                            })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAbrirPedidos) {
                Text("📦")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF080E20)),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                DashboardResumen(pedidos = pedidos, onNuevoPedido = onAbrirPedidos)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Últimos pedidos registrados", color = Color.White,
                        fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    TextButton(onClick = onAbrirPedidos) {
                        Text("Ver todos")
                    }
                }
            }
            val ultimos = pedidos.sortedByDescending { it.id }.take(5)
            if (ultimos.isEmpty()) {
                item {
                    Text("Aún no hay pedidos. Pulsa Nuevo Pedido para registrar el primero.",
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                }
            }
            items(ultimos, key = { it.id }) { pedido ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onSeleccionarPedido(pedido.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2D))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PED-${pedido.id.toString().padStart(3, '0')}",
                                color = Color(0xFF94A3B8), fontSize = 12.sp)
                            val estadoColor = when (pedido.estado) {
                                "PENDIENTE" -> Color(0xFFF59E0B)
                                "EN RUTA" -> Color(0xFF60A5FA)
                                else -> Color(0xFF10B981)
                            }
                            Text(pedido.estado, color = estadoColor, fontSize = 11.sp,
                                fontWeight = FontWeight.Bold)
                        }
                        Text(pedido.cliente, color = Color.White,
                            fontWeight = FontWeight.Bold)
                        Text(pedido.producto, color = Color(0xFFB8C6DB), fontSize = 13.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("$${formato.format(pedido.precio)}", color = Color(0xFF10B981),
                                fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("Gestionar ›", color = Color(0xFF93B8FF), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
