package edu.cit.tiongzon.lostandfound.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import edu.cit.tiongzon.lostandfound.feature.home.data.model.UserResponse
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.launch

private data class NavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileShell(
    token: String = "",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    currentRoute: String = "",
    userRole: String = "",
    topBarActions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var shellUser by remember { mutableStateOf<UserResponse?>(null) }

    LaunchedEffect(token) {
        if (token.isBlank()) return@LaunchedEffect
        try {
            val response = RetrofitClient.authApi.getCurrentUser("Bearer $token")
            if (response.isSuccessful) shellUser = response.body()
        } catch (_: Exception) { }
    }

    val allNavItems = listOf(
        NavItem("Home",        "home",       Icons.Default.Home),
        NavItem("Catalog",     "catalog",    Icons.Default.Inventory2),
        NavItem("Report Item", "report",     Icons.Default.PostAdd),
        NavItem("My Items",    "dashboard",  Icons.Default.Dashboard),
        NavItem("Global Chat", "globalChat", Icons.AutoMirrored.Filled.Chat),
        NavItem("Messages",    "messages",   Icons.Default.Mail),
        NavItem("Admin",       "admin",      Icons.Default.AdminPanelSettings, adminOnly = true),
        NavItem("Profile",     "profile",    Icons.Default.AccountCircle)
    )

    
    val effectiveRole = userRole.ifBlank { shellUser?.role.orEmpty() }
    val navItems = allNavItems.filter { !it.adminOnly || effectiveRole == "ADMIN" }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp),
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.White
            ) {
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(colors = listOf(Rose900, Rose700)))
                        .padding(horizontal = 20.dp, vertical = 28.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Lost", fontWeight = FontWeight.Black, color = Color.White, fontSize = 22.sp)
                            Text("&", fontWeight = FontWeight.Black, color = Amber400, fontSize = 22.sp)
                            Text("Found", fontWeight = FontWeight.Black, color = Color.White, fontSize = 22.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Community Platform",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                    IconButton(
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.8f))
                    }
                }

                Spacer(Modifier.height(8.dp))

                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    navItems.forEach { item ->
                        val isActive = currentRoute == item.route
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isActive) Rose50 else Color.Transparent)
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    onNavigate(item.route)
                                }
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (isActive) Rose800 else Slate500,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                item.label,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp,
                                color = if (isActive) Rose800 else Slate700,
                                modifier = Modifier.weight(1f)
                            )
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Rose800)
                                )
                            }
                        }
                    }
                }

                
                HorizontalDivider(color = Slate100, modifier = Modifier.padding(horizontal = 10.dp))
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            scope.launch { drawerState.close() }
                            onLogout()
                        }
                        .padding(horizontal = 26.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = Rose700, modifier = Modifier.size(20.dp))
                    Text("Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Rose700)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column(Modifier.background(Color.White)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Slate700, modifier = Modifier.size(24.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Lost", fontWeight = FontWeight.Black, color = Rose800, fontSize = 20.sp)
                            Text("&", fontWeight = FontWeight.Black, color = Amber500, fontSize = 20.sp)
                            Text("Found", fontWeight = FontWeight.Black, color = Rose800, fontSize = 20.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        topBarActions()
                        shellUser?.let { user ->
                            Spacer(Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(colors = listOf(Rose800, Amber500))),
                                contentAlignment = Alignment.Center
                            ) {
                                val initial = (user.username.firstOrNull() ?: 'U').uppercaseChar().toString()
                                if (!user.avatarUrl.isNullOrBlank()) {
                                    SubcomposeAsyncImage(
                                        model = user.avatarUrl,
                                        contentDescription = "Profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(30.dp).clip(CircleShape),
                                        error = {
                                            Box(Modifier.size(30.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                                                Text(initial, fontWeight = FontWeight.Bold, color = Rose800, fontSize = 14.sp)
                                            }
                                        }
                                    )
                                } else {
                                    Box(Modifier.size(30.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                                        Text(initial, fontWeight = FontWeight.Bold, color = Rose800, fontSize = 14.sp)
                                    }
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = user.username,
                                style = MaterialTheme.typography.labelLarge,
                                color = Slate700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 90.dp)
                            )
                        }
                        IconButton(onClick = { onNavigate("catalog") }) {
                            Icon(Icons.Default.Search, contentDescription = "Catalog", tint = Slate500)
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Slate500)
                        }
                    }
                    HorizontalDivider(color = Slate200)
                }
            },
            containerColor = Slate50
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}
