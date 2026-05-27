package edu.cit.tiongzon.lostandfound.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inbox
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
import edu.cit.tiongzon.lostandfound.feature.chat.data.model.ConversationDto
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.MobileShell
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessagesScreen(
    token: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    onConversationClick: (Long, Long) -> Unit = { _, _ -> }
) {
    var conversations by remember { mutableStateOf<List<ConversationDto>>(emptyList()) }
    var currentUserId by remember { mutableStateOf<Long?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(token) {
        try {
            val me = RetrofitClient.authApi.getCurrentUser("Bearer $token").body()
            currentUserId = me?.userId
            val resp = RetrofitClient.messageApi.getConversations("Bearer $token")
            if (resp.isSuccessful) conversations = resp.body().orEmpty()
        } catch (_: Exception) {}
        finally { loading = false }
    }

    MobileShell(token = token, onNavigate = onNavigate, onLogout = onLogout, currentRoute = "messages") {
        
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Rose900, Rose700)), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("Messages", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                    Text("Your private item conversations.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                }
            }
        }

        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Rose800, trackColor = Rose100)
        }

        if (!loading && conversations.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inbox, contentDescription = null, tint = Slate300, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No messages yet", fontWeight = FontWeight.Bold, color = Slate700)
                    Text("Start from an item's detail page.", fontSize = 12.sp, color = Slate500)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { onNavigate("catalog") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose800)
                    ) {
                        Text("Browse Items", fontSize = 13.sp)
                    }
                }
            }
        } else if (conversations.isNotEmpty()) {
            Text("Recent Conversations", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate700)

            conversations.forEach { conv ->
                ConversationCard(
                    conv = conv,
                    currentUserId = currentUserId,
                    onClick = {
                        val uid = conv.userId
                        val iid = conv.itemId
                        if (uid != null && iid != null) onConversationClick(uid, iid)
                    }
                )
            }
        }
    }
}

@Composable
private fun ConversationCard(conv: ConversationDto, currentUserId: Long?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Rose900, Amber500))),
                contentAlignment = Alignment.Center
            ) {
                val avatarUrl = conv.avatarUrl
                if (!avatarUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = avatarUrl,
                        contentDescription = conv.username,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        error = {
                            Text(
                                (conv.username?.firstOrNull() ?: 'U').uppercaseChar().toString(),
                                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    )
                } else {
                    Text(
                        (conv.username?.firstOrNull() ?: 'U').uppercaseChar().toString(),
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
            }

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(conv.username ?: "User", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 14.sp)
                    Text(formatMessageTime(conv.lastMessageAt), fontSize = 10.sp, color = Slate400)
                }
                Spacer(Modifier.height(2.dp))
                
                conv.itemTitle?.let { title ->
                    Text(
                        title,
                        fontSize = 10.sp,
                        color = Rose800,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(2.dp))
                val prefix = if (conv.lastMessageSenderId == currentUserId) "You: " else ""
                Text(
                    "$prefix${conv.lastMessage ?: ""}",
                    fontSize = 12.sp,
                    color = Slate500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate300, modifier = Modifier.size(18.dp))
        }
    }
}

private fun formatMessageTime(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(isoDate) ?: return ""
        val now = Date()
        val diff = (now.time - date.time) / 1000
        when {
            diff < 60 -> "Just now"
            diff < 3600 -> "${diff / 60}m ago"
            diff < 86400 -> "${diff / 3600}h ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
        }
    } catch (_: Exception) { "" }
}
