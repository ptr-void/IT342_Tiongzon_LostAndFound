package edu.cit.tiongzon.lostandfound.feature.claims.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import edu.cit.tiongzon.lostandfound.feature.claims.data.model.ClaimDto
import edu.cit.tiongzon.lostandfound.feature.items.data.model.ItemDto
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.MobileShell
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun FileClaimScreen(
    token: String,
    itemId: Long,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var item by remember { mutableStateOf<ItemDto?>(null) }
    var claimantId by remember { mutableStateOf<Long?>(null) }
    var proofDescription by remember { mutableStateOf("") }
    var proofImageUri by remember { mutableStateOf<Uri?>(null) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var messageIsError by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        proofImageUri = uri
    }

    LaunchedEffect(token, itemId) {
        try {
            claimantId = RetrofitClient.authApi.getCurrentUser("Bearer $token").body()?.userId
            val itemResp = RetrofitClient.itemApi.getItem(itemId)
            if (itemResp.isSuccessful) item = itemResp.body()
        } catch (_: Exception) { }
    }

    MobileShell(token = token, onNavigate = onNavigate, onLogout = onLogout) {
        TextButton(onClick = onBack, colors = ButtonDefaults.textButtonColors(contentColor = Rose800)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Back")
        }

        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.linearGradient(listOf(Rose900, Rose700)), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.14f), shape = RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text("File a Claim", fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, color = Color.White)
                    Text(item?.title ?: "Provide proof of ownership.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f), maxLines = 1)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ownership Proof", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                OutlinedTextField(
                    value = proofDescription,
                    onValueChange = { proofDescription = it },
                    placeholder = { Text("Describe unique identifiers, contents, serial numbers, or other proof...", fontSize = 13.sp, color = Slate400) },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Slate200, focusedBorderColor = Rose800)
                )

                Text("Proof Image", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                val selectedImage = proofImageUri
                if (selectedImage != null) {
                    Box {
                        SubcomposeAsyncImage(
                            model = selectedImage,
                            contentDescription = "Proof image preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(14.dp))
                        )
                        TextButton(
                            onClick = { proofImageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                            colors = ButtonDefaults.textButtonColors(containerColor = Color.White.copy(alpha = 0.9f), contentColor = Rose800)
                        ) {
                            Text("Remove", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose800)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Upload receipt/photo proof", fontWeight = FontWeight.Bold)
                    }
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
                val claimant = claimantId
                if (claimant == null || proofDescription.isBlank()) {
                    message = "Proof description is required."
                    messageIsError = true
                    return@Button
                }
                saving = true
                scope.launch {
                    try {
                        val proofImagePath = proofImageUri?.let { uploadProofImage(context, it) }
                        val response = RetrofitClient.claimApi.createClaim(
                            "Bearer $token",
                            ClaimDto(
                                itemId = itemId,
                                claimantId = claimant,
                                proofDescription = proofDescription,
                                proofImagePath = proofImagePath
                            )
                        )
                        if (response.isSuccessful) {
                            message = "Claim submitted."
                            messageIsError = false
                            onNavigate("profile")
                        } else {
                            message = "Failed to submit claim."
                            messageIsError = true
                        }
                    } catch (_: Exception) {
                        message = "Backend unavailable. Please try later."
                        messageIsError = true
                    } finally {
                        saving = false
                    }
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
            Text(if (saving) "Submitting..." else "Submit Claim", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

private suspend fun uploadProofImage(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    val contentResolver = context.contentResolver
    val contentType = contentResolver.getType(uri) ?: "image/jpeg"
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: throw IllegalStateException("Unable to read selected image.")
    val fileBody = bytes.toRequestBody(contentType.toMediaType())
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", "claim-proof.jpg", fileBody)
        .addFormDataPart("upload_preset", "lost_and_found_unsigned")
        .addFormDataPart("cloud_name", "defkzzqcs")
        .build()
    val request = Request.Builder()
        .url("https://api.cloudinary.com/v1_1/defkzzqcs/image/upload")
        .post(requestBody)
        .build()

    OkHttpClient().newCall(request).execute().use { response ->
        val body = response.body?.string().orEmpty()
        val secureUrl = JSONObject(body).optString("secure_url")
        if (!response.isSuccessful || secureUrl.isBlank()) throw IllegalStateException("Failed to upload proof image.")
        secureUrl
    }
}
