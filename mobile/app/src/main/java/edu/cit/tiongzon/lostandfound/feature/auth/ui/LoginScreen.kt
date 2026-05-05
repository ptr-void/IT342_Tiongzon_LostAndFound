package edu.cit.tiongzon.lostandfound.feature.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cit.tiongzon.lostandfound.shared.api.RetrofitClient
import edu.cit.tiongzon.lostandfound.feature.auth.data.model.LoginRequest
import edu.cit.tiongzon.lostandfound.shared.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Slate50, Amber50, Slate50)
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Amber100.copy(alpha = 0.4f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 250.dp, y = 100.dp)
                .clip(CircleShape)
                .background(Rose100.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Amber100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Login,
                    contentDescription = "Login",
                    tint = Amber600,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your credentials to access your account.",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate500,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Username", style = MaterialTheme.typography.labelLarge, color = Slate700)
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type your username", color = Slate400) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Rose800, unfocusedBorderColor = Slate200,
                            focusedContainerColor = Color.White, unfocusedContainerColor = Slate50
                        )
                    )

                    Text("Password", style = MaterialTheme.typography.labelLarge, color = Slate700)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type your password", color = Slate400) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password", tint = Slate400
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Rose800, unfocusedBorderColor = Slate200,
                            focusedContainerColor = Color.White, unfocusedContainerColor = Slate50
                        )
                    )

                    AnimatedVisibility(visible = errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Rose50),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = errorMessage ?: "", color = Rose700,
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                        }
                    }

                    AnimatedVisibility(visible = successMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Emerald100),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = successMessage ?: "", color = Emerald600,
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all fields."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val response = RetrofitClient.authApi.login(LoginRequest(username, password))
                                    if (response.isSuccessful) {
                                        val token = response.body()?.token
                                        if (token != null) {
                                            successMessage = "Login successful! Redirecting..."
                                            delay(1500)
                                            onLoginSuccess(token)
                                        } else {
                                            errorMessage = "Unexpected response from server."
                                        }
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        errorMessage = try {
                                            org.json.JSONObject(errorBody ?: "").optString("message", "Invalid credentials!")
                                        } catch (e: Exception) { "Invalid credentials!" }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Network error. Is the server running?"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rose900),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = if (isLoading) "Logging in..." else "Sign in", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Text("Don't have an account? ", color = Slate500, style = MaterialTheme.typography.bodyMedium)
                Text("Sign up", color = Amber600, fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable { onNavigateToRegister() })
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
