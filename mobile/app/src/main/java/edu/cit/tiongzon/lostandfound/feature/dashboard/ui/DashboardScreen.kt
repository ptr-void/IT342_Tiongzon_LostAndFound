package edu.cit.tiongzon.lostandfound.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import edu.cit.tiongzon.lostandfound.feature.catalog.data.model.sampleItems
import edu.cit.tiongzon.lostandfound.feature.claims.data.model.ClaimDto
import edu.cit.tiongzon.lostandfound.feature.items.data.model.ItemDto
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.*
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(token: String, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    var items by remember { mutableStateOf<List<ItemDto>>(emptyList()) }
    var claims by remember { mutableStateOf<List<ClaimDto>>(emptyList()) }
    var username by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(token, refreshTrigger) {
        loading = true
        try {
            val me = RetrofitClient.authApi.getCurrentUser("Bearer $token").body()
            username = me?.username ?: ""
            val allItems = RetrofitClient.itemApi.getItems("Bearer $token").body().orEmpty()
            items = allItems.filter { it.reporterId == me?.userId }
            claims = RetrofitClient.claimApi.getReceivedClaims("Bearer $token").body().orEmpty()
        } catch (_: Exception) {
            items = sampleItems.take(2)
        } finally {
            loading = false
        }
    }

    MobileShell(token = token, onNavigate = onNavigate, onLogout = onLogout, currentRoute = "dashboard") {
        
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Slate800, Slate900)), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("My Dashboard", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White)
                    if (username.isNotBlank()) {
                        Text("@$username", fontSize = 13.sp, color = Color.White.copy(alpha = 0.65f))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("${items.size} item${if (items.size != 1) "s" else ""} reported", fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f))
                }
                Button(
                    onClick = { onNavigate("report") },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose900, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Report", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Total",
                value = items.size.toString(),
                color = Rose800
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Lost",
                value = items.count { it.status == "LOST" }.toString(),
                color = Rose700
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Found",
                value = items.count { it.status == "FOUND" }.toString(),
                color = Emerald600
            )
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Rose800,
                trackColor = Rose100
            )
        }

        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate700, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("My Reported Items", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
        }

        if (items.isEmpty() && !loading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate300, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No items yet", fontWeight = FontWeight.Bold, color = Slate700)
                    Text("Report a lost or found item to get started.", fontSize = 12.sp, color = Slate500)
                }
            }
        } else {
            val visibleItems = if (items.isEmpty() && !loading) sampleItems.take(2) else items
            visibleItems.forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardItemCard(
                        item = item,
                        onUpdateStatus = { newStatus ->
                            scope.launch {
                                try {
                                    val updatedItem = item.copy(status = newStatus)
                                    val res = RetrofitClient.itemApi.updateItem("Bearer $token", item.id!!, updatedItem)
                                    if (res.isSuccessful) refreshTrigger++
                                } catch (_: Exception) {}
                            }
                        },
                        onDelete = {
                            scope.launch {
                                try {
                                    val res = RetrofitClient.itemApi.deleteItem("Bearer $token", item.id!!)
                                    if (res.isSuccessful) refreshTrigger++
                                } catch (_: Exception) {}
                            }
                        }
                    )
                    val itemClaims = claims.filter { it.itemId == item.id }
                    if (itemClaims.isNotEmpty()) {
                        ItemClaimsCard(
                            claims = itemClaims,
                            onApprove = { claimId ->
                                scope.launch {
                                    try {
                                        val res = RetrofitClient.claimApi.approveClaim("Bearer $token", claimId)
                                        if (res.isSuccessful) refreshTrigger++
                                    } catch (_: Exception) { }
                                }
                            },
                            onReject = { claimId ->
                                scope.launch {
                                    try {
                                        val res = RetrofitClient.claimApi.rejectClaim("Bearer $token", claimId)
                                        if (res.isSuccessful) refreshTrigger++
                                    } catch (_: Exception) { }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemClaimsCard(claims: List<ClaimDto>, onApprove: (Long) -> Unit, onReject: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Amber100),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("File Claims Submitted for This Item", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
            claims.forEach { claim ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                Text("Claimed by ${claim.claimantName ?: "Unknown"}", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 13.sp)
                                Text(claim.proofDescription.ifBlank { "No proof description" }, color = Slate600, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            StatusPill(claim.status ?: "PENDING")
                        }
                        claim.proofImagePath?.takeIf { it.isNotBlank() }?.let {
                            SubcomposeAsyncImage(
                                model = it,
                                contentDescription = "Claim proof image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp))
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("Payment:", color = Slate500, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            StatusPill(claim.paymentStatus ?: "NOT_APPLICABLE")
                        }
                        claim.paymentIntentId?.let { Text("Ref: $it", color = Slate500, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        if (claim.status == "PENDING" && claim.id != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = { onApprove(claim.id) },
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600, contentColor = Color.White)
                                ) { Text("Approve", fontSize = 12.sp) }
                                Button(
                                    onClick = { onReject(claim.id) },
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Rose900, contentColor = Color.White)
                                ) { Text("Reject", fontSize = 12.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = color)
            Text(label, fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Medium)
        }
    }
}
