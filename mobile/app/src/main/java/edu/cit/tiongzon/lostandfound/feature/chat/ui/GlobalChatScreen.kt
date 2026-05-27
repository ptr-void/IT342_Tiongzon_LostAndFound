package edu.cit.tiongzon.lostandfound.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Send
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
import edu.cit.tiongzon.lostandfound.feature.chat.data.model.MessageDto
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.api.StompClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.MobileShell
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun GlobalChatScreen(token: String, onNavigate: (String) -> Unit, onLogout: () -> Unit) {
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var currentUserId by remember { mutableStateOf<Long?>(null) }
    var currentUsername by remember { mutableStateOf("") }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val stomp = remember { StompClient(token) }

    // Load history, subscribe to the same STOMP topic as web, and poll as fallback.
    LaunchedEffect(token) {
        try {
            val me = RetrofitClient.authApi.getCurrentUser("Bearer $token").body()
            currentUserId = me?.userId
            currentUsername = me?.username ?: ""
        } catch (_: Exception) {}

        // Load history first
        try {
            val resp = RetrofitClient.messageApi.getGlobalMessages("Bearer $token")
            if (resp.isSuccessful) messages = resp.body().orEmpty()
        } catch (_: Exception) {}
        loading = false

        stomp.connect {
            stomp.subscribe("/topic/global", "sub-global") { json ->
                try {
                    val obj = org.json.JSONObject(json)
                    val msg = MessageDto(
                        id = obj.optLong("id"),
                        senderId = obj.optLong("senderId"),
                        senderName = obj.optString("senderName"),
                        content = obj.optString("content"),
                        senderAvatar = obj.optString("senderAvatar").takeIf { it.isNotBlank() },
                        sentAt = obj.optString("createdAt")
                    )
                    messages = if (messages.none { it.id == msg.id }) messages + msg else messages
                } catch (_: Exception) {}
            }
        }

        while (true) {
            delay(5000)
            try {
                val resp = RetrofitClient.messageApi.getGlobalMessages("Bearer $token")
                if (resp.isSuccessful) messages = resp.body().orEmpty()
            } catch (_: Exception) {}
        }
    }

    DisposableEffect(Unit) {
        onDispose { stomp.disconnect() }
    }

    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    MobileShell(token = token, onNavigate = onNavigate, onLogout = onLogout, currentRoute = "globalChat") {
        
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Indigo600, Rose800)), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text("Global Chat", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(Emerald400))
                        Text("Community room · ${messages.size} messages", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                }
            }
        }

        
        Card(
            modifier = Modifier.fillMaxWidth().height(480.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Rose800)
                }
            } else if (messages.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Slate300, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No messages yet", fontWeight = FontWeight.Bold, color = Slate700)
                        Text("Be the first to say something!", fontSize = 12.sp, color = Slate500)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderId == currentUserId
                        GlobalChatBubble(
                            senderName = if (isMe) "You" else (msg.senderName ?: "User"),
                            content = msg.content,
                            isMe = isMe,
                            avatarUrl = msg.senderAvatar,
                            initial = (msg.senderName?.firstOrNull() ?: 'U').uppercaseChar().toString()
                        )
                    }
                }
            }
        }

        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Message the community...", fontSize = 13.sp, color = Slate400) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Slate200,
                        focusedBorderColor = Rose800
                    )
                )
                IconButton(
                    onClick = {
                        if (input.isBlank() || currentUserId == null) return@IconButton
                        val msg = input.trim()
                        input = ""
                        sending = true
                        scope.launch {
                            try {
                                val body = JSONObject().apply {
                                    put("senderId", currentUserId!!)
                                    put("content", msg)
                                }
                                stomp.send("/app/chat.global", body)
                            } catch (_: Exception) {
                                if (input.isBlank()) input = msg
                            } finally {
                                sending = false
                            }
                        }
                    },
                    enabled = input.isNotBlank() && !sending,
                    modifier = Modifier.size(48.dp).background(
                        if (input.isNotBlank()) Rose900 else Slate300,
                        shape = RoundedCornerShape(12.dp)
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun GlobalChatBubble(senderName: String, content: String, isMe: Boolean, avatarUrl: String?, initial: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Indigo600, Rose800))),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = avatarUrl,
                        contentDescription = senderName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        error = { Text(initial, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                    )
                } else {
                    Text(initial, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(Modifier.width(8.dp))
        }
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            if (!isMe) {
                Text(senderName, fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
            }
            Box(
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .clip(
                        if (isMe) RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                        else RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .background(if (isMe) Rose900 else Slate100)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(content, fontSize = 14.sp, color = if (isMe) Color.White else Slate900, lineHeight = 20.sp)
            }
        }
    }
}
