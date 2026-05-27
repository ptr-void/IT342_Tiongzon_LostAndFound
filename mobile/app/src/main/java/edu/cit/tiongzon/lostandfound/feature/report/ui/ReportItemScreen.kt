package edu.cit.tiongzon.lostandfound.feature.report.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import coil.compose.AsyncImage
import edu.cit.tiongzon.lostandfound.feature.items.data.model.ItemDto
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.*
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sinh
import kotlin.math.tan

@Composable
fun ReportItemScreen(token: String, onBack: () -> Unit, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("LOST") }
    var category by remember { mutableStateOf("ELECTRONICS") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var locationLat by remember { mutableStateOf("10.2948") }
    var locationLng by remember { mutableStateOf("123.8816") }
    var reporterId by remember { mutableStateOf<Long?>(null) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }

    LaunchedEffect(token) {
        try {
            val response = RetrofitClient.authApi.getCurrentUser("Bearer $token")
            reporterId = response.body()?.userId?.toLong()
        } catch (_: Exception) { }
    }

    MobileShell(
        token = token,
        onNavigate = onNavigate,
        onLogout = onLogout,
        currentRoute = "report"
    ) {
        TextButton(onClick = onBack, colors = ButtonDefaults.textButtonColors(contentColor = Rose800)) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Back")
        }

        
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Rose900, Rose700)), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Text("Report an Item", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White)
                Text("Fill in details to report a lost or found item.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
            }
        }

        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                FormSection("Item Title") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("E.g. Blue Hydro Flask or Found iPhone 13", fontSize = 13.sp, color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Slate200,
                            focusedBorderColor = Rose800
                        )
                    )
                }

                
                FormSection("Status") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("LOST" to "I Lost This", "FOUND" to "I Found This").forEach { (value, label) ->
                            val active = status == value
                            val bg = if (active) (if (value == "LOST") Rose900 else Emerald600) else Color.White
                            val fg = if (active) Color.White else Slate600
                            FilterChip(
                                selected = active,
                                onClick = { status = value },
                                label = { Text(label, fontSize = 13.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = bg,
                                    selectedLabelColor = fg,
                                    containerColor = Color.White,
                                    labelColor = Slate600
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = active,
                                    selectedBorderColor = bg,
                                    borderColor = Slate200
                                )
                            )
                        }
                    }
                }

                
                FormSection("Category") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ELECTRONICS", "DOCUMENTS", "VALUABLES").forEach { value ->
                            val active = category == value
                            FilterChip(
                                selected = active,
                                onClick = { category = value },
                                label = { Text(value.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Slate900,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Slate600
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = active,
                                    selectedBorderColor = Slate900,
                                    borderColor = Slate200
                                )
                            )
                        }
                    }
                }

                
                FormSection("Description") {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Provide a detailed description of the item...", fontSize = 13.sp, color = Slate400) },
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Slate200,
                            focusedBorderColor = Rose800
                        )
                    )
                }

                
                FormSection("Photo (Optional)") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Rose50),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Rose300, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("Tap to upload a photo", fontSize = 13.sp, color = Rose300)
                            Text("PNG, JPG up to 5MB", fontSize = 11.sp, color = Slate400)
                        }
                    }
                }

                
                FormSection("Location") {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = { Text("Where was this lost/found?", fontSize = 13.sp, color = Slate400) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Rose800, modifier = Modifier.size(18.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Slate200,
                            focusedBorderColor = Rose800
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = locationLat,
                            onValueChange = { locationLat = it },
                            label = { Text("Latitude", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Slate200, focusedBorderColor = Rose800)
                        )
                        OutlinedTextField(
                            value = locationLng,
                            onValueChange = { locationLng = it },
                            label = { Text("Longitude", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Slate200, focusedBorderColor = Rose800)
                        )
                    }
                    MobileMapPreview(
                        lat = locationLat.toDoubleOrNull() ?: 10.2948,
                        lng = locationLng.toDoubleOrNull() ?: 123.8816,
                        onLocationSelected = { lat, lng ->
                            locationLat = lat
                            locationLng = lng
                        }
                    )
                }
            }
        }

        
        message?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (messageIsError) Rose100 else Emerald100)
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!messageIsError) Icon(Icons.Default.Check, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                    Text(it, color = if (messageIsError) Rose800 else Emerald600, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        
        Button(
            onClick = {
                if (title.isBlank() || reporterId == null) {
                    message = "Please enter a title and wait for user info to load."
                    messageIsError = true
                    return@Button
                }
                saving = true
                scope.launch {
                    try {
                        val payload = ItemDto(
                            title = title,
                            description = description,
                            status = status,
                            category = category,
                            locationDescription = location,
                            locationLat = locationLat.toDoubleOrNull(),
                            locationLng = locationLng.toDoubleOrNull(),
                            reporterId = reporterId
                        )
                        val response = RetrofitClient.itemApi.createItem("Bearer $token", payload)
                        if (response.isSuccessful) {
                            message = "Item reported successfully!"
                            messageIsError = false
                            onNavigate("catalog")
                        } else {
                            message = "Failed to save. Please try again."
                            messageIsError = true
                        }
                    } catch (e: Exception) {
                        message = "Backend unavailable. Please try later."
                        messageIsError = true
                    } finally { saving = false }
                }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Rose900, contentColor = Color.White)
        ) {
            if (saving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (saving) "Submitting..." else "Submit Report", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun MobileMapPreview(lat: Double, lng: Double, onLocationSelected: (String, String) -> Unit) {
    val zoom = 16
    val density = LocalDensity.current
    val centerWorld = latLngToWorldPixels(lat, lng, zoom)
    val centerTileX = floor(centerWorld.x / 256.0).toInt()
    val centerTileY = floor(centerWorld.y / 256.0).toInt()

    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(lat, lng) {
                    detectTapGestures { offset ->
                        val selected = offsetToLatLng(offset, centerWorld, size.width.toDouble(), size.height.toDouble(), zoom)
                        onLocationSelected("%.6f".format(selected.first), "%.6f".format(selected.second))
                    }
                }
        ) {
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            for (dx in -1..1) {
                for (dy in -1..1) {
                    val tileX = centerTileX + dx
                    val tileY = centerTileY + dy
                    val leftPx = tileX * 256.0 - centerWorld.x + widthPx / 2.0
                    val topPx = tileY * 256.0 - centerWorld.y + heightPx / 2.0
                    AsyncImage(
                        model = "https://tile.openstreetmap.org/$zoom/$tileX/$tileY.png",
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .size(with(density) { 256.toDp() })
                            .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(18.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Rose900),
                contentAlignment = Alignment.Center
            ) {
                Box(Modifier.size(7.dp).clip(RoundedCornerShape(50)).background(Color.White))
            }
        }
    }
}

private data class WorldPoint(val x: Double, val y: Double)

private fun latLngToWorldPixels(lat: Double, lng: Double, zoom: Int): WorldPoint {
    val siny = kotlin.math.sin(lat * PI / 180.0).coerceIn(-0.9999, 0.9999)
    val scale = 256.0 * 2.0.pow(zoom)
    val x = (lng + 180.0) / 360.0 * scale
    val y = (0.5 - ln((1 + siny) / (1 - siny)) / (4 * PI)) * scale
    return WorldPoint(x, y)
}

private fun offsetToLatLng(offset: Offset, center: WorldPoint, width: Double, height: Double, zoom: Int): Pair<Double, Double> {
    val scale = 256.0 * 2.0.pow(zoom)
    val worldX = center.x + offset.x - width / 2.0
    val worldY = center.y + offset.y - height / 2.0
    val lng = worldX / scale * 360.0 - 180.0
    val lat = atan(sinh(PI * (1.0 - 2.0 * worldY / scale))) * 180.0 / PI
    return lat to lng
}

@Composable
private fun FormSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
        content()
    }
}
