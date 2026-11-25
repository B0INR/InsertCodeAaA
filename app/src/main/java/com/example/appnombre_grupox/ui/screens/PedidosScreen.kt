package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

private val fondoPedidos = Color(0xFF080E20)
private val tarjetaPedidos = Color(0xFF111A2D)
private val azulPedidos = Color(0xFF2563EB)
private val verdePedidos = Color(0xFF10B981)

/** Estados válidos: PENDIENTE, EN RUTA, ENTREGADO. */
data class Pedido(
    val id: Int,
    val cliente: String,
    val producto: String,
    val precio: Int,
    val estado: String
)

@Composable
fun PedidosScreen(
    onVolver: () -> Unit,
    pedidos: SnapshotStateList<Pedido>,
    productoCotizado: Producto? = null,
    onCotizacionConsumida: () -> Unit = {},
    abrirFormulario: Boolean = false,
    onFormularioAbierto: () -> Unit = {},
    pedidoSeleccionadoId: Int? = null,
    onPedidoSeleccionadoConsumido: () -> Unit = {}
) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<Pedido?>(null) }
    var cliente by remember { mutableStateOf("") }
    var producto by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("PENDIENTE") }
    val formato = remember { NumberFormat.getIntegerInstance(Locale("es", "CL")) }

    fun nuevoPedido() {
        editando = null
        cliente = ""
        producto = ""
        precio = ""
        estado = "PENDIENTE"
        mostrarDialogo = true
    }

    fun editarPedido(pedido: Pedido) {
        editando = pedido
        cliente = pedido.cliente
        producto = pedido.producto
        precio = pedido.precio.toString()
        estado = pedido.estado
        mostrarDialogo = true
    }

    LaunchedEffect(productoCotizado) {
        productoCotizado?.let {
            nuevoPedido()
            producto = it.nombre
            precio = it.precio.toString()
            onCotizacionConsumida()
        }
    }
    LaunchedEffect(pedidoSeleccionadoId) {
        pedidoSeleccionadoId?.let { id ->
            pedidos.find { it.id == id }?.let { editarPedido(it) }
            onPedidoSeleccionadoConsumido()
        }
    }
    LaunchedEffect(abrirFormulario) {
        if (abrirFormulario) {
            nuevoPedido()
            onFormularioAbierto()
        }
    }

    Column(Modifier.fillMaxSize().background(fondoPedidos)) {
        Text("Gestión de Pedidos", color = Color.White, fontSize = 24.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 18.dp, end = 16.dp))
        Text("Toca un pedido para editarlo o cambiar su estado.", color = Color(0xFFB8C6DB),
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onVolver) { Text("← Inicio") }
            Button(onClick = { nuevoPedido() }, colors = ButtonDefaults.buttonColors(containerColor = azulPedidos)) {
                Text("+ Nuevo pedido")
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pedidos, key = { it.id }) { pedido ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { editarPedido(pedido) },
                    colors = CardDefaults.cardColors(containerColor = tarjetaPedidos),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PED-${pedido.id.toString().padStart(3, '0')}", color = Color(0xFFB8C6DB))
                            Text(pedido.estado, color = colorEstado(pedido.estado),
                                style = MaterialTheme.typography.labelMedium)
                        }
                        Text(pedido.cliente, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(pedido.producto, color = Color(0xFFB8C6DB))
                        Text("$${formato.format(pedido.precio)}", color = verdePedidos,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        HorizontalDivider(color = Color(0xFF263147))
                        Text("Tocar para editar pedido y estado  ›", color = Color(0xFF93B8FF),
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            if (pedidos.isEmpty()) item {
                Text("Aún no hay pedidos. Pulsa Nuevo pedido para comenzar.",
                    color = Color.White, modifier = Modifier.padding(16.dp))
            }
        }
    }

    if (mostrarDialogo) {
        val pedidoOriginal = editando
        val precioValido = precio.toIntOrNull()
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(if (pedidoOriginal == null) "Nuevo pedido" else "Editar PED-${pedidoOriginal.id.toString().padStart(3,'0')}") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState()).imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(cliente, { cliente = it }, label = { Text("Cliente") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(producto, { producto = it }, label = { Text("Producto") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(precio, { precio = it.filter(Char::isDigit) },
                        label = { Text("Precio (CLP)") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Text("Estado del pedido", style = MaterialTheme.typography.titleSmall)
                    listOf("PENDIENTE", "EN RUTA", "ENTREGADO").forEach { opcion ->
                        Row(modifier = Modifier.fillMaxWidth()
                            .clickable { estado = opcion },
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(selected = estado == opcion, onClick = { estado = opcion })
                            Text(opcion, modifier = Modifier.padding(start = 4.dp),
                                color = colorEstado(opcion))
                        }
                    }
                    if (pedidoOriginal != null) {
                        TextButton(
                            onClick = {
                                pedidos.removeAll { it.id == pedidoOriginal.id }
                                mostrarDialogo = false
                            }
                        ) { Text("Eliminar pedido", color = Color(0xFFE57373)) }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = cliente.isNotBlank() && producto.isNotBlank() &&
                        precioValido != null && precioValido > 0,
                    onClick = {
                        val nuevo = Pedido(
                            id = pedidoOriginal?.id ?: ((pedidos.maxOfOrNull { it.id } ?: 0) + 1),
                            cliente = cliente.trim(), producto = producto.trim(),
                            precio = requireNotNull(precioValido), estado = estado
                        )
                        val indice = pedidos.indexOfFirst { it.id == nuevo.id }
                        if (indice >= 0) pedidos[indice] = nuevo else pedidos.add(nuevo)
                        mostrarDialogo = false
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}

private fun colorEstado(estado: String): Color = when (estado) {
    "PENDIENTE" -> Color(0xFFF59E0B)
    "EN RUTA" -> Color(0xFF60A5FA)
    else -> Color(0xFF10B981)
}
