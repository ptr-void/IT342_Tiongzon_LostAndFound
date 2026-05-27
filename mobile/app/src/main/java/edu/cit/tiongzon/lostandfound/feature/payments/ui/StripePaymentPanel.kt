package edu.cit.tiongzon.lostandfound.feature.payments.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.tiongzon.lostandfound.feature.claims.data.model.ClaimDto
import edu.cit.tiongzon.lostandfound.feature.payments.data.model.PaymentRequest
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun StripePaymentPanel(
    token: String,
    claimId: Long,
    amount: Long,
    currency: String,
    description: String,
    onSuccess: () -> Unit
) {
    var step by remember { mutableStateOf("idle") } 
    var sessionId by remember { mutableStateOf<String?>(null) }
    var paymentUrl by remember { mutableStateOf<String?>(null) }
    var err by remember { mutableStateOf<String?>(null) }

    val uriHandler = LocalUriHandler.current

    
    LaunchedEffect(sessionId) {
        if (sessionId == null || step == "done") return@LaunchedEffect

        while (isActive && step != "done") {
            try {
                val res = RetrofitClient.paymentApi.verifySession("Bearer $token", sessionId!!)
                if (res.isSuccessful && res.body()?.succeeded == true) {
                    
                    val markRes = RetrofitClient.claimApi.markClaimPaid(
                        "Bearer $token",
                        claimId,
                        ClaimDto(paymentIntentId = sessionId)
                    )
                    if (markRes.isSuccessful) {
                        step = "done"
                        onSuccess()
                        break
                    } else {
                        err = "Payment succeeded but failed to record."
                    }
                }
            } catch (e: Exception) {
                
            }
            delay(3000)
        }
    }

    if (step == "done") {
        return Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Emerald50)
                .border(1.dp, Emerald200, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
            Column {
                Text("Payment confirmed by Stripe!", color = Emerald700, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Ref: $sessionId", color = Emerald600, fontSize = 11.sp)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Slate50)
                .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(description, fontSize = 13.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
            Text("${currency.uppercase()} ${String.format("%.2f", amount / 100.0)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Rose800)
            Text("Powered by Stripe · Test mode", fontSize = 11.sp, color = Slate400)
        }

        if (err != null) {
            Text(err!!, color = Rose600, fontSize = 12.sp)
        }

        when (step) {
            "idle" -> {
                Button(
                    onClick = {
                        step = "loading"
                        err = null
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Rose900, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pay with Stripe", fontWeight = FontWeight.Bold)
                }
            }
            "loading" -> {
                LaunchedEffect(Unit) {
                    try {
                        val res = RetrofitClient.paymentApi.createCheckoutSession("Bearer $token", PaymentRequest(amount, currency))
                        if (res.isSuccessful && res.body() != null) {
                            sessionId = res.body()!!.sessionId
                            paymentUrl = res.body()!!.url
                            step = "ready"
                            uriHandler.openUri(paymentUrl!!)
                        } else {
                            err = "Failed to create payment session."
                            step = "idle"
                        }
                    } catch (e: Exception) {
                        err = "Network error: ${e.message}"
                        step = "idle"
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Rose800, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Connecting to Stripe...", color = Slate500, fontSize = 13.sp)
                }
            }
            "ready" -> {
                Button(
                    onClick = { paymentUrl?.let { uriHandler.openUri(it) } },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Color.White)
                ) {
                    Text("Re-open Stripe Checkout", fontWeight = FontWeight.SemiBold)
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Emerald50)
                        .border(1.dp, Emerald200, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Emerald600, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Waiting for payment confirmation...", color = Emerald700, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                TextButton(
                    onClick = { step = "idle"; sessionId = null; paymentUrl = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Slate500)
                }
            }
        }
    }
}
