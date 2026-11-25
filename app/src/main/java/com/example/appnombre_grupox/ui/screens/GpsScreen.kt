package ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

private val fondoGps = Color(0xFF080E20)
private val tarjetaGps = Color(0xFF111A2D)
private val azulGps = Color(0xFF3B82F6)
private val verdeGps = Color(0xFF10B981)
private val grisGps = Color(0xFF94A3B8)

@Composable
fun GpsScreen(
    onVolver: () -> Unit,
    pedidos: SnapshotStateList<Pedido>
) {
    val context = LocalContext.current
    var pedidoSeleccionado by remember { mutableStateOf<Int?>(null) }
    val pendientes = pedidos.count { it.estado == "PENDIENTE" }
    val enRuta = pedidos.count { it.estado == "EN RUTA" }
    val entregados = pedidos.count { it.estado == "ENTREGADO" }

    val gpsClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var latitud by remember {
        mutableStateOf<Double?>(null)
    }

    var longitud by remember {
        mutableStateOf<Double?>(null)
    }

    var mensaje by remember {
        mutableStateOf("Ubicación no consultada")
    }

    fun tienePermiso(): Boolean {
        return listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).any {
            ContextCompat.checkSelfPermission(
                context, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun obtenerUbicacion() {
        if (!tienePermiso()) {
            mensaje = "Permiso de ubicación necesario"
            return
        }

        mensaje = "Buscando ubicación..."

        gpsClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { ubicacion ->
            if (ubicacion != null) {
                latitud = ubicacion.latitude
                longitud = ubicacion.longitude
                mensaje = "GPS: ubicación obtenida"
            } else {
                mensaje = "No se pudo obtener la ubicación"
            }
        }.addOnFailureListener {
            mensaje = "Error de GPS: ${it.message}"
        }
    }

    val permisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) {
            obtenerUbicacion()
        } else {
            mensaje = "Permiso denegado"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(fondoGps),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        item {
            Text(
                text = "Ruta GPS de Despacho",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Seguimiento de entregas asignadas",
                color = grisGps,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onVolver) {
                Text("← Volver al Dashboard")
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = tarjetaGps
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "📍 Mapa de Ruta Interactiva",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Esquema demostrativo: los puntos no representan direcciones de los pedidos",
                        color = grisGps,
                        fontSize = 12.sp
                    )

                    MapaRutaDemo()

                    Text(
                        text = "🟢 Base  🟠 Pendiente  🔵 Destinos",
                        color = grisGps,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Secuencia de Entregas (${pedidos.size})",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Pendientes ($pendientes)  ·  En ruta ($enRuta)  ·  Entregados ($entregados)",
                color = grisGps,
                fontSize = 12.sp
            )
            Text(
                text = "Toca una entrega para actualizar su estado",
                color = Color(0xFF93B8FF),
                fontSize = 12.sp
            )
        }

        if (pedidos.isEmpty()) {
            item {
                Text(
                    "Todavía no hay entregas. Crea un pedido desde la sección Pedidos.",
                    color = grisGps,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        items(pedidos, key = { it.id }) { pedido ->
            EntregaRuta(
                numero = pedido.id,
                nombre = pedido.cliente,
                detalle = pedido.producto,
                estado = pedido.estado,
                colorEstado = colorEntrega(pedido.estado),
                onClick = { pedidoSeleccionado = pedido.id }
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = tarjetaGps
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "📡 Mi ubicación actual",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        mensaje,
                        color = grisGps
                    )

                    if (latitud != null && longitud != null) {
                        Text(
                            "Latitud: $latitud",
                            color = Color.White
                        )

                        Text(
                            "Longitud: $longitud",
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            if (tienePermiso()) {
                                obtenerUbicacion()
                            } else {
                                permisos.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📍 Obtener ubicación GPS")
                    }
                }
            }
        }
    }

    val pedidoActivo = pedidos.firstOrNull { it.id == pedidoSeleccionado }
    if (pedidoActivo != null) {
        AlertDialog(
            onDismissRequest = { pedidoSeleccionado = null },
            title = { Text("PED-${pedidoActivo.id.toString().padStart(3, '0')}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(pedidoActivo.cliente, style = MaterialTheme.typography.titleMedium)
                    Text(pedidoActivo.producto)
                    Text("Selecciona el nuevo estado de entrega:")
                    listOf("PENDIENTE", "EN RUTA", "ENTREGADO").forEach { estado ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                val indice = pedidos.indexOfFirst { it.id == pedidoActivo.id }
                                if (indice >= 0) {
                                    pedidos[indice] = pedidos[indice].copy(estado = estado)
                                }
                                pedidoSeleccionado = null
                            },
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = pedidoActivo.estado == estado,
                                onClick = {
                                    val indice = pedidos.indexOfFirst { it.id == pedidoActivo.id }
                                    if (indice >= 0) {
                                        pedidos[indice] = pedidos[indice].copy(estado = estado)
                                    }
                                    pedidoSeleccionado = null
                                }
                            )
                            Text(estado, color = colorEntrega(estado))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pedidoSeleccionado = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

private fun colorEntrega(estado: String): Color = when (estado) {
    "PENDIENTE" -> Color(0xFFF59E0B)
    "EN RUTA" -> azulGps
    "ENTREGADO" -> verdeGps
    else -> grisGps
}

@Composable
private fun MapaRutaDemo() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                fondoGps,
                RoundedCornerShape(12.dp)
            )
    ) {
        val w = size.width
        val h = size.height

        // Cuadrícula de fondo
        for (i in 1..7) {
            val x = w * i / 8f
            drawLine(
                Color(0xFF263147),
                Offset(x, 0f),
                Offset(x, h),
                strokeWidth = 1.5f
            )
        }

        for (i in 1..5) {
            val y = h * i / 6f
            drawLine(
                Color(0xFF263147),
                Offset(0f, y),
                Offset(w, y),
                strokeWidth = 1.5f
            )
        }

        val puntos = listOf(
            Offset(w * 0.12f, h * 0.78f),
            Offset(w * 0.38f, h * 0.51f),
            Offset(w * 0.68f, h * 0.28f),
            Offset(w * 0.88f, h * 0.81f)
        )

        // Ruta punteada de ejemplo
        for (i in 0 until puntos.lastIndex) {
            drawLine(
                color = azulGps,
                start = puntos[i],
                end = puntos[i + 1],
                strokeWidth = 5f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 8f)
                )
            )
        }

        puntos.forEachIndexed { indice, punto ->
            drawCircle(
                color = when (indice) {
                    0 -> verdeGps
                    1 -> Color(0xFFF59E0B)
                    else -> azulGps
                },
                radius = 13f,
                center = punto
            )

            drawCircle(
                color = Color.White,
                radius = 13f,
                center = punto,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3f
                )
            )
        }
    }
}

@Composable
private fun EntregaRuta(
    numero: Int,
    nombre: String,
    detalle: String,
    estado: String,
    colorEstado: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = tarjetaGps
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "$numero",
                color = azulGps,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    nombre,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    detalle,
                    color = grisGps,
                    fontSize = 11.sp
                )
            }

            Text(
                estado,
                color = colorEstado,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
