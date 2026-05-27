package edu.cit.tiongzon.lostandfound.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import edu.cit.tiongzon.lostandfound.feature.items.data.model.ItemDto
import edu.cit.tiongzon.lostandfound.feature.chat.data.model.MessageDto
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.api.StompClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.MobileShell
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun PrivateChatScreen(
    token: String,
    otherUserId: Long,
    itemId: Long,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    var messages by remember { mutableStateOf<List<MessageDto>>(emptyList()) }
    var currentUserId by remember { mutableStateOf<Long?>(null) }
    var otherUsername by remember { mutableStateOf("User") }
    var item by remember { mutableStateOf<ItemDto?>(null) }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val stomp = remember { StompClient(token) }
    var currentUsername by remember { mutableStateOf("") }

    LaunchedEffect(token) {
        try {
            val me = RetrofitClient.authApi.getCurrentUser("Bearer $token").body()
            currentUserId = me?.userId
            currentUsername = me?.username ?: ""
        } catch (_: Exception) {}

        try {
            val itemResp = RetrofitClient.itemApi.getItem(itemId)
            item = itemResp.body()
            otherUsername = item?.reporterName ?: "User"
        } catch (_: Exception) {}

        
        try {
            val resp = RetrofitClient.messageApi.getPrivateMessages("Bearer $token", otherUserId, itemId)
            if (resp.isSuccessful) messages = resp.body().orEmpty()
        } catch (_: Exception) {}
        loading = false

        stomp.connect {
            stomp.subscribe("/user/queue/messages", "sub-private") { json ->
                try {
                    val obj = org.json.JSONObject(json)
                    val msgItemId = obj.optLong("itemId")
                    val msgSenderId = obj.optLong("senderId")
                    val msgReceiverId = obj.optLong("receiverId")
                    if (msgItemId == itemId && (msgSenderId == otherUserId || msgReceiverId == otherUserId)) {
                        val msg = MessageDto(
                            id = obj.optLong("id"),
                            senderId = msgSenderId,
                            senderName = obj.optString("senderName"),
                            receiverId = msgReceiverId,
                            itemId = msgItemId,
                            content = obj.optString("content"),
                            senderAvatar = obj.optString("senderAvatar").takeIf { it.isNotBlank() },
                            sentAt = obj.optString("createdAt")
                        )
                        messages = if (messages.none { it.id == msg.id }) messages + msg else messages
                    }
                } catch (_: Exception) {}
            }
        }

        
        while (true) {
            delay(5000)
            try {
                val resp = RetrofitClient.messageApi.getPrivateMessages("Bearer $token", otherUserId, itemId)
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

        
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.size(42.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Rose900, Amber500))),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarUrl = item?.reporterAvatar
                    val initial = otherUsername.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                    if (!avatarUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = avatarUrl,
                            contentDescription = otherUsername,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            error = { Text(initial, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                        )
                    } else {
                        Text(initial, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(otherUsername, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 15.sp)
                    item?.title?.let { title ->
                        Text("Re: $title", color = Slate500, fontSize = 12.sp, maxLines = 1)
                    }
                }
                Box(
                    Modifier.background(Emerald100, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Active", fontSize = 10.sp, color = Emerald600, fontWeight = FontWeight.Bold)
                }
            }
        }

        
        Card(
            modifier = Modifier.fillMaxWidth().height(420.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Rose800)
                }
            } else if (messages.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No messages", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate700)
                        Spacer(Modifier.height(8.dp))
                        Text("Start the conversation!", fontSize = 12.sp, color = Slate500)
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderId == currentUserId
                        PrivateMessageBubble(content = msg.content, isMe = isMe)
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
                    placeholder = { Text("Type your message...", fontSize = 13.sp, color = Slate400) },
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
                                    put("receiverId", otherUserId)
                                    put("itemId", itemId)
                                    put("content", msg)
                                }
                                stomp.send("/app/chat.private", body)
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
private fun PrivateMessageBubble(content: String, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(
                    if (isMe) RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                    else RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .background(if (isMe) Amber600 else Slate100)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(content, fontSize = 14.sp, color = if (isMe) Color.White else Slate900, lineHeight = 20.sp)
        }
    }
}
