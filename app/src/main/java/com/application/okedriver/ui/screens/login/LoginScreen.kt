package com.application.okedriver.ui.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*
import com.application.okedriver.ui.components.GradientButton
import com.application.okedriver.ui.components.OkeTextField

/**
 * Login screen matching the "Oke Driver Login" design from Stitch.
 *
 * - Full-screen purple gradient background
 * - Scooter emoji "illustration"
 * - Email / Password inputs with glass-style containers
 * - Gradient Login CTA button
 * - Google Sign-In outline button
 * - Forgot Password & Create Account links
 */
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onCreateAccount: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Subtle floating animation for the scooter icon
    val infiniteTransition = rememberInfiniteTransition(label = "scooter_anim")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scooter_float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        OkeLoginGradientTop,
                        OkeLoginGradientMid,
                        OkeLoginGradientBottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // ── Scooter illustration ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(1f)
                    .offset(y = floatAnim.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛵", fontSize = 68.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Oke Driver",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Driver app for OkeJek platform",
                color = Color.White.copy(alpha = 0.70f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Email field ───────────────────────────────────────────────
            OkeTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                leadingIcon = Icons.Rounded.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                containerColor = Color.White.copy(alpha = 0.18f),
                borderColor = Color.White.copy(alpha = 0.30f),
                textColor = Color.White,
                hintColor = Color.White.copy(alpha = 0.55f),
                iconTint = Color.White.copy(alpha = 0.70f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Password field ────────────────────────────────────────────
            OkeTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                leadingIcon = Icons.Rounded.Lock,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                containerColor = Color.White.copy(alpha = 0.18f),
                borderColor = Color.White.copy(alpha = 0.30f),
                textColor = Color.White,
                hintColor = Color.White.copy(alpha = 0.55f),
                iconTint = Color.White.copy(alpha = 0.70f),
                trailingIcon = {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = "Toggle password",
                        tint = Color.White.copy(alpha = 0.70f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { passwordVisible = !passwordVisible }
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Login button ──────────────────────────────────────────────
            GradientButton(
                text = "Login",
                onClick = {
                    isLoading = true
                    onLoginClick()
                },
                isLoading = isLoading,
                gradientColors = listOf(
                    Color(0xFF9B6BFF), OkePrimaryVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Google Sign-In button ─────────────────────────────────────
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.40f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Sign in with Google", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Forgot password ───────────────────────────────────────────
            Text(
                text = "Forgot Password?",
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 14.sp,
                modifier = Modifier.clickable { onForgotPassword() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── Create Account ────────────────────────────────────────────
            Row {
                Text(
                    text = "Don't have an account? ",
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Create Account",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onCreateAccount() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    OkedriverTheme { LoginScreen() }
}
