package edu.cit.tiongzon.lostandfound.feature.claims.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import edu.cit.tiongzon.lostandfound.feature.claims.data.model.ClaimDto
import edu.cit.tiongzon.lostandfound.feature.home.data.model.UserResponse
import edu.cit.tiongzon.lostandfound.feature.payments.ui.StripePaymentPanel
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.components.StatusPill
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageClaimScreen(token: String, claimId: Long, onBack: () -> Unit, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var claim by remember { mutableStateOf<ClaimDto?>(null) }
    var user by remember { mutableStateOf<UserResponse?>(null) }
    var loading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            try {
                val me = RetrofitClient.authApi.getCurrentUser("Bearer $token").body()
                user = me
                val claimRes = RetrofitClient.claimApi.getClaim("Bearer $token", claimId)
                if (claimRes.isSuccessful) {
                    claim = claimRes.body()
                } else {
                    onBack()
                }
            } catch (_: Exception) {
                onBack()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(claimId) {
        loadData()
    }

    fun handleUpdateStatus(action: String) {
        if (isUpdating) return
        isUpdating = true
        scope.launch {
            try {
                val res = if (action == "approve") {
                    RetrofitClient.claimApi.approveClaim("Bearer $token", claimId)
                } else {
                    RetrofitClient.claimApi.rejectClaim("Bearer $token", claimId)
                }
                if (res.isSuccessful) {
                    loadData()
                }
            } catch (_: Exception) {
            } finally {
                isUpdating = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Claim Details", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate50
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Rose800)
            }
            return@Scaffold
        }

        val currentClaim = claim ?: return@Scaffold
        val currentUser = user ?: return@Scaffold

        val isClaimant = currentClaim.claimantId == currentUser.userId
        val isPoster = currentClaim.itemReporterId == currentUser.userId
        val isAdmin = currentUser.role == "ADMIN"

        if (!isClaimant && !isPoster && !isAdmin) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You do not have permission to view this claim.", color = Slate500)
            }
            return@Scaffold
        }

        val youNeedToPay = (isClaimant && currentClaim.itemStatus == "FOUND") || (isPoster && currentClaim.itemStatus == "LOST")
        val otherPartyNeedsToPay = (isClaimant && currentClaim.itemStatus == "LOST") || (isPoster && currentClaim.itemStatus == "FOUND")
        val isPaid = currentClaim.paymentStatus == "PAID"
        val canPay = currentClaim.status == "APPROVED" && !isPaid && youNeedToPay

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Claim #${currentClaim.id}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Slate900)
                    Text("Filed on ${currentClaim.createdAt?.take(10) ?: "N/A"}", fontSize = 12.sp, color = Slate500)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusPill(currentClaim.status ?: "PENDING")
                    if (isPaid) {
                        Text("Paid", color = Emerald700, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Emerald100, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Information", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    Divider(color = Slate100)

                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text("Item", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                            Text(currentClaim.itemTitle ?: "Item #${currentClaim.itemId}", fontSize = 14.sp, color = Slate900, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            val isLost = currentClaim.itemStatus == "LOST"
                            Text(
                                currentClaim.itemStatus ?: "UNKNOWN",
                                color = if (isLost) Rose700 else Emerald700,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(if (isLost) Rose50 else Emerald50, RoundedCornerShape(4.dp))
                                    .border(1.dp, if (isLost) Rose200 else Emerald200, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Claimant", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                            Text(currentClaim.claimantName ?: "Unknown", fontSize = 14.sp, color = Slate900, fontWeight = FontWeight.Medium)
                        }
                    }

                    Column {
                        Text("Proof Description", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                        Text(currentClaim.proofDescription.takeIf { it.isNotBlank() } ?: "No description provided.", fontSize = 13.sp, color = Slate700, modifier = Modifier.fillMaxWidth().background(Slate50, RoundedCornerShape(8.dp)).padding(12.dp))
                    }

                    currentClaim.proofImagePath?.takeIf { it.isNotBlank() }?.let { imagePath ->
                        Column {
                            Text("Proof Image", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            SubcomposeAsyncImage(
                                model = imagePath,
                                contentDescription = "Proof",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, Slate200, RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }

            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Status & Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                    Divider(color = Slate100)

                    when (currentClaim.status) {
                        "PENDING" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Amber50).border(1.dp, Amber200, RoundedCornerShape(8.dp)).padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Amber600, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Waiting for Review", fontWeight = FontWeight.Bold, color = Amber800, fontSize = 14.sp)
                                    Text("This claim is pending review by the item poster.", color = Amber700, fontSize = 12.sp)
                                }
                            }

                            if (isPoster && !isAdmin) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { handleUpdateStatus("approve") },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = !isUpdating
                                    ) {
                                        Text("Approve Claim")
                                    }
                                    Button(
                                        onClick = { handleUpdateStatus("reject") },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Rose700),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = !isUpdating
                                    ) {
                                        Text("Reject Claim")
                                    }
                                }
                            }
                        }
                        "REJECTED" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Rose50).border(1.dp, Rose200, RoundedCornerShape(8.dp)).padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Rose600, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Claim Rejected", fontWeight = FontWeight.Bold, color = Rose800, fontSize = 14.sp)
                                    Text("This claim was rejected by the item poster.", color = Rose700, fontSize = 12.sp)
                                }
                            }
                        }
                        "APPROVED" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Emerald50).border(1.dp, Emerald200, RoundedCornerShape(8.dp)).padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                                Column {
                                    Text("Claim Approved", fontWeight = FontWeight.Bold, color = Emerald800, fontSize = 14.sp)
                                    Text("The claim has been verified and approved.", color = Emerald700, fontSize = 12.sp)
                                }
                            }

                            if (!isPaid) {
                                Divider(color = Slate100, modifier = Modifier.padding(vertical = 8.dp))
                                if (canPay) {
                                    StripePaymentPanel(
                                        token = token,
                                        claimId = claimId,
                                        amount = 5000,
                                        currency = "php",
                                        description = if (currentClaim.itemStatus == "LOST") "Send reward payment to the finder (${currentClaim.claimantName})" else "Pay recovery fee to the finder",
                                        onSuccess = { loadData() }
                                    )
                                } else if (otherPartyNeedsToPay) {
                                    Text("Waiting for the other party to complete the payment via Stripe.", color = Slate500, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().background(Slate50, RoundedCornerShape(8.dp)).padding(12.dp))
                                } else if (isAdmin) {
                                    Text("Waiting for payment completion between parties.", color = Slate500, fontSize = 13.sp)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Emerald50).border(1.dp, Emerald200, RoundedCornerShape(8.dp)).padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text("Payment Completed", fontWeight = FontWeight.Bold, color = Emerald800, fontSize = 14.sp)
                                        Text("Stripe reference: ${currentClaim.paymentIntentId}", color = Emerald700, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
