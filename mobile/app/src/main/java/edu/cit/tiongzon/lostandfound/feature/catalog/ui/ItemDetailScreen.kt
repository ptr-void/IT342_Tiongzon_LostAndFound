package edu.cit.tiongzon.lostandfound.feature.catalog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import edu.cit.tiongzon.lostandfound.feature.items.data.model.ItemDto
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.MobileShell
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*

@Composable
fun ItemDetailScreen(
    itemId: Long,
    token: String,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onMessageReporter: (Long) -> Unit,
    onFileClaim: () -> Unit,
    onLogout: () -> Unit
) {
    var item by remember { mutableStateOf<ItemDto?>(null) }
    var currentUserId by remember { mutableStateOf<Long?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(itemId, token) {
        try {
            currentUserId = RetrofitClient.authApi.getCurrentUser("Bearer $token").body()?.userId
            val resp = RetrofitClient.itemApi.getItem(itemId)
            if (resp.isSuccessful) item = resp.body()
            else error = "Item not found."
        } catch (e: Exception) {
            error = "Could not load item."
        } finally {
            loading = false
        }
    }

    MobileShell(
        token = token,
        onNavigate = onNavigate,
        onLogout = onLogout
    ) {
        TextButton(onClick = onBack, colors = ButtonDefaults.textButtonColors(contentColor = Rose800)) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Back")
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Rose800,
                trackColor = Rose100
            )
        } else if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Rose100)
            ) {
                Text(
                    error ?: "Unknown error",
                    modifier = Modifier.padding(20.dp),
                    color = Rose800,
                    fontWeight = FontWeight.Medium
                )
            }
        } else if (item != null) {
            val it = item!!

            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                val imageUrl = it.imagePath?.let { path ->
                    if (path.startsWith("http")) path else "http://10.0.2.2:8080$path"
                }
                if (!imageUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = it.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(220.dp)
                                    .background(Brush.linearGradient(listOf(Rose100, Amber100))),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator(color = Rose800, modifier = Modifier.size(36.dp)) }
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(220.dp)
                                    .background(Brush.linearGradient(listOf(Rose50, Amber50))),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate300, modifier = Modifier.size(32.dp))
                                    Text("No Image", color = Slate400, fontSize = 12.sp)
                                }
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                            .background(Brush.linearGradient(listOf(Rose50, Amber50))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate300, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No Image Available", color = Slate500, fontSize = 13.sp)
                        }
                    }
                }
            }

            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        
                        val statusColor = if (it.status == "FOUND") Emerald600 else Rose800
                        val statusBg = if (it.status == "FOUND") Emerald100 else Rose100
                        Text(
                            it.status,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(statusBg).padding(horizontal = 12.dp, vertical = 5.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor
                        )
                        Text(
                            it.category,
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Slate100).padding(horizontal = 12.dp, vertical = 5.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate600
                        )
                    }
                    Text(it.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Slate900, lineHeight = 28.sp)
                    if (!it.description.isNullOrBlank()) {
                        Text(it.description, fontSize = 14.sp, color = Slate600, lineHeight = 22.sp)
                    }
                }
            }

            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    if (!it.locationDescription.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Rose100),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Rose800, modifier = Modifier.size(18.dp)) }
                            Column {
                                Text("Location", fontSize = 11.sp, color = Slate500)
                                Text(it.locationDescription, fontSize = 14.sp, color = Slate900, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    it.createdAt?.take(10)?.let { date ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Amber100),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Amber600, modifier = Modifier.size(18.dp)) }
                            Column {
                                Text("Date Posted", fontSize = 11.sp, color = Slate500)
                                Text(date, fontSize = 14.sp, color = Slate900, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    if (!it.reporterName.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Rose900, Amber500))),
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarUrl = it.reporterAvatar
                                val initial = it.reporterName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                                if (!avatarUrl.isNullOrBlank()) {
                                    SubcomposeAsyncImage(
                                        model = avatarUrl,
                                        contentDescription = it.reporterName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        error = { Text(initial, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                                    )
                                } else {
                                    Text(initial, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Column {
                                Text("Reported by", fontSize = 11.sp, color = Slate500)
                                Text(it.reporterName, fontSize = 14.sp, color = Slate900, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            if (item?.reporterId != null && item?.reporterId != currentUserId) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { onMessageReporter(item!!.reporterId!!) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber600)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Message", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    Button(
                        onClick = onFileClaim,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rose900)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("File Claim", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
