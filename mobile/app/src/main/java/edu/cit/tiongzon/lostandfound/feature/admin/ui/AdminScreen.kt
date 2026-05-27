package edu.cit.tiongzon.lostandfound.feature.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import edu.cit.tiongzon.lostandfound.feature.admin.data.model.AdminClaimDto
import edu.cit.tiongzon.lostandfound.feature.admin.data.model.AdminItemDto
import edu.cit.tiongzon.lostandfound.feature.admin.data.model.AdminUserDto
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.MobileShell
import edu.cit.tiongzon.lostandfound.shared.ui.components.StatusPill
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(token: String, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("users") }
    var users by remember { mutableStateOf<List<AdminUserDto>>(emptyList()) }
    var items by remember { mutableStateOf<List<AdminItemDto>>(emptyList()) }
    var claims by remember { mutableStateOf<List<AdminClaimDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }

    fun showMessage(text: String, isError: Boolean = false) {
        message = text
        messageIsError = isError
    }

    suspend fun loadAdminData() {
        loading = true
        try {
            val bearer = "Bearer $token"
            val usersResp = RetrofitClient.adminApi.getUsers(bearer)
            val itemsResp = RetrofitClient.adminApi.getItems(bearer)
            val claimsResp = RetrofitClient.adminApi.getClaims(bearer)
            if (usersResp.isSuccessful) users = usersResp.body().orEmpty()
            if (itemsResp.isSuccessful) items = itemsResp.body().orEmpty()
            if (claimsResp.isSuccessful) claims = claimsResp.body().orEmpty()
            if (!usersResp.isSuccessful || !itemsResp.isSuccessful || !claimsResp.isSuccessful) {
                showMessage("Some admin data could not be loaded.", true)
            }
        } catch (_: Exception) {
            showMessage("Backend unavailable. Admin data could not be loaded.", true)
        } finally {
            loading = false
        }
    }

    LaunchedEffect(token) { loadAdminData() }

    MobileShell(token = token, onNavigate = onNavigate, onLogout = onLogout, currentRoute = "admin") {
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Slate900, Slate800)), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(46.dp).background(Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text("Admin Panel", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                    Text("Users, warnings, bans, posts, and claims.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AdminTab("Users", Icons.Default.People, tab == "users", Modifier.weight(1f)) { tab = "users" }
            AdminTab("Posts", Icons.Default.PostAdd, tab == "posts", Modifier.weight(1f)) { tab = "posts" }
            AdminTab("Claims", Icons.Default.AdminPanelSettings, tab == "claims", Modifier.weight(1f)) { tab = "claims" }
        }

        if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Rose800, trackColor = Rose100)

        message?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (messageIsError) Rose100 else Emerald100)
            ) {
                Text(
                    it,
                    modifier = Modifier.padding(14.dp),
                    color = if (messageIsError) Rose800 else Emerald600,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        when (tab) {
            "users" -> AdminUsersSection(
                users = users,
                onWarn = { userId ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.adminApi.warnUser("Bearer $token", userId)
                            if (response.isSuccessful) {
                                val warnings = response.body()?.warningMarks ?: 0
                                users = users.map { if (it.userId == userId) it.copy(warningMarks = warnings) else it }
                                showMessage("User warned.")
                            } else showMessage("Failed to warn user.", true)
                        } catch (_: Exception) { showMessage("Network error while warning user.", true) }
                    }
                },
                onToggleBan = { userId ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.adminApi.toggleBan("Bearer $token", userId)
                            if (response.isSuccessful) {
                                val banned = response.body()?.isBanned ?: false
                                users = users.map { if (it.userId == userId) it.copy(banned = banned) else it }
                                showMessage(if (banned) "User banned." else "User unbanned.")
                            } else showMessage("Failed to update ban status.", true)
                        } catch (_: Exception) { showMessage("Network error while updating ban status.", true) }
                    }
                }
            )
            "posts" -> AdminPostsSection(
                items = items,
                onDelete = { itemId ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.adminApi.deleteItem("Bearer $token", itemId)
                            if (response.isSuccessful) {
                                items = items.filterNot { it.id == itemId }
                                showMessage("Post deleted.")
                            } else showMessage("Failed to delete post.", true)
                        } catch (_: Exception) { showMessage("Network error while deleting post.", true) }
                    }
                },
                onViewCatalog = { onNavigate("catalog") }
            )
            else -> AdminClaimsSection(
                claims = claims,
                onApprove = { claimId ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.claimApi.approveClaim("Bearer $token", claimId)
                            if (response.isSuccessful) {
                                showMessage("Claim approved.")
                                loadAdminData()
                            } else showMessage("Failed to approve claim.", true)
                        } catch (_: Exception) { showMessage("Network error while approving claim.", true) }
                    }
                },
                onReject = { claimId ->
                    scope.launch {
                        try {
                            val response = RetrofitClient.claimApi.rejectClaim("Bearer $token", claimId)
                            if (response.isSuccessful) {
                                showMessage("Claim rejected.")
                                loadAdminData()
                            } else showMessage("Failed to reject claim.", true)
                        } catch (_: Exception) { showMessage("Network error while rejecting claim.", true) }
                    }
                }
            )
        }
    }
}

@Composable
private fun AdminTab(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (active) Rose900 else Color.White, contentColor = if (active) Color.White else Slate600),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (active) 2.dp else 0.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(text, fontSize = 12.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun AdminUsersSection(users: List<AdminUserDto>, onWarn: (Long) -> Unit, onToggleBan: (Long) -> Unit) {
    AdminSectionCard(title = "User Directory", subtitle = "Warn or ban registered users. Admins are protected.") {
        if (users.isEmpty()) {
            EmptyAdminText("No users found.")
        } else {
            users.forEach { user ->
                HorizontalDivider(color = Slate100)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(if (user.role == "ADMIN") Rose900 else Slate300),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (user.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text(user.username, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 13.sp, maxLines = 1)
                        Text(user.email, color = Slate500, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            RolePill(user.role)
                            if (user.banned) StatusText("Banned", Rose100, Rose800)
                            if (user.warningMarks > 0) StatusText("${user.warningMarks} warnings", Amber100, Amber600)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.End) {
                        OutlinedButton(
                            onClick = { user.userId?.let(onWarn) },
                            enabled = user.role != "ADMIN",
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Warn", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { user.userId?.let(onToggleBan) },
                            enabled = user.role != "ADMIN",
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (user.banned) Slate200 else Rose900, contentColor = if (user.banned) Slate800 else Color.White)
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (user.banned) "Unban" else "Ban", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPostsSection(items: List<AdminItemDto>, onDelete: (Long) -> Unit, onViewCatalog: () -> Unit) {
    AdminSectionCard(title = "Content Moderation", subtitle = "View or delete inappropriate posts.") {
        if (items.isEmpty()) {
            EmptyAdminText("No posts found.")
        } else {
            items.forEach { item ->
                HorizontalDivider(color = Slate100)
                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Slate500, modifier = Modifier.size(22.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${item.category} by ${item.reporterName ?: "unknown"}", color = Slate500, fontSize = 11.sp)
                        }
                        StatusPill(item.status)
                    }
                    item.description?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = Slate600, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onViewCatalog, modifier = Modifier.weight(1f).height(36.dp), contentPadding = PaddingValues(0.dp)) {
                            Text("View Catalog", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { item.id?.let(onDelete) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Rose900, contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delete", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminClaimsSection(claims: List<AdminClaimDto>, onApprove: (Long) -> Unit, onReject: (Long) -> Unit) {
    AdminSectionCard(title = "Claims Overview", subtitle = "View claim status and proof summaries. Private messages are not shown.") {
        if (claims.isEmpty()) {
            EmptyAdminText("No claims found.")
        } else {
            claims.forEach { claim ->
                HorizontalDivider(color = Slate100)
                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(claim.itemTitle ?: "Item #${claim.itemId ?: ""}", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 14.sp, maxLines = 1)
                            Text("Claimant: ${claim.claimantName ?: "unknown"}", color = Slate500, fontSize = 11.sp)
                        }
                        StatusPill(claim.status ?: "PENDING")
                    }
                    claim.proofDescription?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = Slate600, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                    claim.proofImagePath?.takeIf { it.isNotBlank() }?.let {
                        SubcomposeAsyncImage(
                            model = it,
                            contentDescription = "Claim proof image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp))
                        )
                    }
                    if (claim.status == "PENDING" && claim.id != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { onApprove(claim.id) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald600, contentColor = Color.White)
                            ) {
                                Text("Approve", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onReject(claim.id) },
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Rose900, contentColor = Color.White)
                            ) {
                                Text("Reject", fontSize = 12.sp)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Payment:", color = Slate400, fontSize = 11.sp)
                        StatusPill(claim.paymentStatus ?: "NOT_APPLICABLE")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSectionCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 15.sp)
            Text(subtitle, color = Slate500, fontSize = 12.sp)
            content()
        }
    }
}

@Composable
private fun RolePill(role: String) {
    StatusText(role, if (role == "ADMIN") Rose100 else Slate100, if (role == "ADMIN") Rose800 else Slate600)
}

@Composable
private fun StatusText(text: String, bg: Color, fg: Color) {
    Text(
        text,
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp),
        fontSize = 10.sp,
        color = fg,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun EmptyAdminText(text: String) {
    Text(text, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), color = Slate500, fontSize = 13.sp)
}
