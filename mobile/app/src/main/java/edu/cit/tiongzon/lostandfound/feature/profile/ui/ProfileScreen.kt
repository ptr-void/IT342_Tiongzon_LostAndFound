package edu.cit.tiongzon.lostandfound.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
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
import edu.cit.tiongzon.lostandfound.feature.catalog.data.model.sampleItems
import edu.cit.tiongzon.lostandfound.feature.claims.data.model.ClaimDto
import edu.cit.tiongzon.lostandfound.feature.home.data.model.UserResponse
import edu.cit.tiongzon.lostandfound.feature.items.data.model.ItemDto
import edu.cit.tiongzon.lostandfound.feature.payments.data.model.PaymentQrDto
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.CompactItemCard
import edu.cit.tiongzon.lostandfound.shared.ui.components.MobileShell
import edu.cit.tiongzon.lostandfound.shared.ui.components.StatusPill
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(token: String, onNavigate: (String) -> Unit, onLogout: () -> Unit, onManageClaim: (Long) -> Unit) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserResponse?>(null) }
    var myReports by remember { mutableStateOf<List<ItemDto>>(emptyList()) }
    var myClaims by remember { mutableStateOf<List<ClaimDto>>(emptyList()) }
    var claimsOnMyItems by remember { mutableStateOf<List<ClaimDto>>(emptyList()) }
    var paymentQr by remember { mutableStateOf<PaymentQrDto?>(null) }
    val paymentRefs = remember { mutableStateMapOf<Long, String>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(token) {
        loading = true
        try {
            val me = RetrofitClient.authApi.getCurrentUser("Bearer $token").body()
            user = me
            val allItems = RetrofitClient.itemApi.getItems("Bearer $token").body().orEmpty()
            myReports = allItems.filter { it.reporterId == me?.userId }
            myClaims = RetrofitClient.claimApi.getMyClaims("Bearer $token").body().orEmpty()
            claimsOnMyItems = RetrofitClient.claimApi.getReceivedClaims("Bearer $token").body().orEmpty()
        } catch (_: Exception) {
            myReports = sampleItems.take(1)
        } finally {
            loading = false
        }
    }

    MobileShell(token = token, onNavigate = onNavigate, onLogout = onLogout, currentRoute = "profile") {

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Rose800, trackColor = Rose100)
        }

        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                
                Box(
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Rose800, Amber500))),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(88.dp).clip(CircleShape).background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarUrl = user?.avatarUrl
                        if (!avatarUrl.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = avatarUrl,
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                error = { InitialAvatar(user?.username) }
                            )
                        } else {
                            InitialAvatar(user?.username)
                        }
                    }
                }

                Text(
                    user?.username ?: "User",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Slate900
                )
                Text(
                    user?.email ?: "",
                    fontSize = 13.sp,
                    color = Slate500
                )

                // Role + warning badges row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user?.role ?: "USER",
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(Amber100)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Amber600
                    )
                    val warnings = user?.warningMarks ?: 0
                    if (warnings > 0) {
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(Rose100)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Rose800, modifier = Modifier.size(12.dp))
                            Text("$warnings Warning${if (warnings > 1) "s" else ""}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Rose800)
                        }
                    }
                }

                HorizontalDivider(color = Slate100, modifier = Modifier.padding(top = 4.dp))

                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Reports", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                    Text("${myReports.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
                HorizontalDivider(color = Slate100)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Claims", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                    Text("${myClaims.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
                HorizontalDivider(color = Slate100)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("File Claims on Items", fontSize = 13.sp, color = Slate500, fontWeight = FontWeight.Medium)
                    Text("${claimsOnMyItems.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
                HorizontalDivider(color = Slate100)

                
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose800)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatBox(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Default.Inventory2, contentDescription = null, tint = Rose700, modifier = Modifier.size(28.dp)) },
                iconBg = Rose50,
                label = "Items Reported",
                value = myReports.size.toString()
            )
            StatBox(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Amber600, modifier = Modifier.size(28.dp)) },
                iconBg = Amber100,
                label = "My Filed Claims",
                value = myClaims.size.toString()
            )
        }

        StatBox(
            modifier = Modifier.fillMaxWidth(),
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Emerald600, modifier = Modifier.size(28.dp)) },
            iconBg = Emerald100,
            label = "File Claims on My Items",
            value = claimsOnMyItems.size.toString()
        )

        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Rose800, modifier = Modifier.size(20.dp))
            Text("My Reported Items", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate900)
            Spacer(Modifier.weight(1f))
            if (myReports.isNotEmpty()) {
                Text("${myReports.size} total", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.Medium)
            }
        }

        if (myReports.isEmpty() && !loading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "You haven't reported any lost or found items yet.",
                        color = Slate500,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            myReports.forEach { item ->
                ReportItemRow(item = item)
            }
        }

        ClaimSection(
            title = "My Filed Claims",
            emptyText = "You have no active claims.",
            claims = myClaims,
            received = false,
            onManageClaim = onManageClaim
        )

        ClaimSection(
            title = "File Claims Submitted on My Items",
            emptyText = "No one has filed a claim on your posted items yet.",
            claims = claimsOnMyItems,
            received = true,
            onManageClaim = onManageClaim
        )
    }
}

@Composable
private fun ClaimSection(
    title: String,
    emptyText: String,
    claims: List<ClaimDto>,
    received: Boolean,
    onManageClaim: (Long) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Amber600, modifier = Modifier.size(20.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate900)
    }

    if (claims.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Text(emptyText, modifier = Modifier.fillMaxWidth().padding(24.dp), color = Slate500, fontSize = 13.sp)
        }
    } else {
        claims.forEach { claim ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(claim.itemTitle ?: "Item #${claim.itemId ?: ""}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Slate900, maxLines = 1)
                            Text(
                                if (received) "Claimed by ${claim.claimantName ?: "Unknown"}" else "Submitted on ${claim.createdAt?.take(10) ?: "N/A"}",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                        StatusPill(claim.status ?: "PENDING")
                    }
                    
                    OutlinedButton(
                        onClick = { claim.id?.let { onManageClaim(it) } },
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Manage Claim →", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    iconBg: androidx.compose.ui.graphics.Color,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) { icon() }
            Column {
                Text(label, fontSize = 11.sp, color = Slate500, fontWeight = FontWeight.Medium)
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Slate900)
            }
        }
    }
}

@Composable
private fun ReportItemRow(item: ItemDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Slate900)
                Text(
                    "Reported on ${item.createdAt?.take(10) ?: "N/A"}",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }
            Spacer(Modifier.width(8.dp))
            val statusBg = if (item.status == "FOUND") Emerald100 else Rose100
            val statusFg = if (item.status == "FOUND") Emerald600 else Rose800
            Text(
                item.status,
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(statusBg).padding(horizontal = 10.dp, vertical = 4.dp),
                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusFg
            )
        }
    }
}

@Composable
private fun InitialAvatar(username: String?) {
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Rose800, Amber500))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            (username?.firstOrNull() ?: 'U').uppercaseChar().toString(),
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}
