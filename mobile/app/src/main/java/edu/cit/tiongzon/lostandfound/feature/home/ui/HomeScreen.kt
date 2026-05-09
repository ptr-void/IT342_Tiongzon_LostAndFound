package edu.cit.tiongzon.lostandfound.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.feature.home.data.model.UserResponse
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    token: String,
    onLogout: () -> Unit
) {
    var user by remember { mutableStateOf<UserResponse?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(token) {
        try {
            val response = RetrofitClient.authApi.getCurrentUser("Bearer $token")
            if (response.isSuccessful) {
                user = response.body()
            }
        } catch (_: Exception) { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Lost", fontWeight = FontWeight.ExtraBold, color = Rose800, fontSize = 20.sp)
                        Text("&", fontWeight = FontWeight.ExtraBold, color = Amber500, fontSize = 20.sp)
                        Text("Found", fontWeight = FontWeight.ExtraBold, color = Rose800, fontSize = 20.sp)
                    }
                },
                actions = {
                    if (user != null) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(CircleShape).background(Brush.linearGradient(colors = listOf(Rose800, Amber500))),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.size(30.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                                Text(text = (user?.username?.firstOrNull() ?: 'U').uppercase(), fontWeight = FontWeight.Bold, color = Rose800, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = user?.username ?: "", style = MaterialTheme.typography.labelLarge, color = Slate700)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Slate500)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.95f))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(colors = listOf(Rose50.copy(alpha = 0.8f), Color.White.copy(alpha = 0.8f), Amber50.copy(alpha = 0.8f))))
                    .padding(horizontal = 24.dp, vertical = 48.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(shape = RoundedCornerShape(50), colors = CardDefaults.cardColors(containerColor = Rose100)) {
                        Text("New Community Platform", modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Rose800)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Lost something?", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.ExtraBold, color = Slate900, textAlign = TextAlign.Center)
                    Text("Let's find it together.", style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp), fontWeight = FontWeight.ExtraBold, color = Rose700, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("The smartest, fastest community-driven platform to report found items, track your lost belongings, and securely claim what's yours.",
                        style = MaterialTheme.typography.bodyLarge, color = Slate600, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp), lineHeight = 24.sp)
                    if (user != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Emerald100.copy(alpha = 0.7f))) {
                            Text("✓ Welcome back, ${user?.username}!", modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Emerald600)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("How it Works", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Seamless integration for returning your valuables securely.", style = MaterialTheme.typography.bodyLarge, color = Slate500, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                FeatureCard(icon = Icons.Default.Search, iconBackground = Amber100, iconColor = Amber600,
                    title = "1. Search & Browse", description = "Search the catalog using rich filters to see if someone found your item safely.", borderColor = Amber200)
                Spacer(modifier = Modifier.height(16.dp))
                FeatureCard(icon = Icons.Default.LocationOn, iconBackground = Rose100, iconColor = Rose800,
                    title = "2. Pin Locations", description = "Reporters pin the exact location on an interactive map, helping you track the path.", borderColor = Rose200)
                Spacer(modifier = Modifier.height(16.dp))
                FeatureCard(icon = Icons.Default.Shield, iconBackground = Emerald100, iconColor = Emerald600,
                    title = "3. Secure Claims", description = "Submit private proof of ownership. Optionally pay a courier fee to get it shipped.", borderColor = Emerald100)
            }

            Box(modifier = Modifier.fillMaxWidth().background(Slate50).padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Lost & Found © 2026 — Community Platform", style = MaterialTheme.typography.bodySmall, color = Slate400)
            }
        }
    }
}

@Composable
private fun FeatureCard(icon: ImageVector, iconBackground: Color, iconColor: Color, title: String, description: String, borderColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(iconBackground), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium, color = Slate500, lineHeight = 20.sp)
            }
        }
    }
}
