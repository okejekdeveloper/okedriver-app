package com.application.okedriver.ui.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kotlin.random.Random

/**
 * Premium modernized Login Screen.
 * Features animated background particles, glassmorphism inputs, and refined typography.
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

    // ── Animations ────────────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "login_anims")
    
    // Floating logo animation
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_float"
    )

    // Background particle animation state
    val particleAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OkeBg)
    ) {
        // ── Animated Background Particles ─────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.4f)) {
            val seed = 42L
            val random = Random(seed)
            repeat(15) {
                val x = random.nextFloat() * size.width
                val y = (random.nextFloat() * size.height + particleAnim * 200) % size.height
                val radius = random.nextFloat() * 100f + 50f
                drawCircle(
                    color = OkePrimary.copy(alpha = 0.08f),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // ── Premium Logo Section ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .offset(y = floatAnim.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Glow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(OkePrimary.copy(alpha = 0.05f))
                )
                // Main circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(OkeLoginGradientTop, OkePrimary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛵", fontSize = 72.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Oke Driver",
                style = MaterialTheme.typography.displayMedium,
                color = OkePrimary,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Premium Driver Experience",
                style = MaterialTheme.typography.bodyLarge,
                color = OkeTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Modern Input Fields ─────────────────────────────────────────
            OkeTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                leadingIcon = Icons.Rounded.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                containerColor = OkeInputBg,
                borderColor = OkeInputBorder,
                textColor = OkeTextPrimary,
                hintColor = OkeTextHint,
                iconTint = OkePrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            OkeTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                leadingIcon = Icons.Rounded.Lock,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                containerColor = OkeInputBg,
                borderColor = OkeInputBorder,
                textColor = OkeTextPrimary,
                hintColor = OkeTextHint,
                iconTint = OkePrimary,
                trailingIcon = {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = "Toggle password",
                        tint = OkeTextHint,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 4.dp)
                            .clickable { passwordVisible = !passwordVisible }
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Login Button ──────────────────────────────────────────────
            GradientButton(
                text = "Login",
                onClick = {
                    isLoading = true
                    onLoginClick()
                },
                isLoading = isLoading,
                gradientColors = listOf(
                    OkeLoginGradientTop, OkePrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Google Sign-In ────────────────────────────────────────────
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = OkeShapeButton,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(1.dp, OkeDivider)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Language,
                        contentDescription = null,
                        tint = Color(0xFF4285F4), // Google Blue
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Continue with Google",
                        color = OkeTextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Links ─────────────────────────────────────────────────────
            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.bodyLarge,
                color = OkePrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onForgotPassword() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OkeTextSecondary
                )
                Text(
                    text = "Register Now",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OkePrimary,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.clickable { onCreateAccount() }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    OkedriverTheme { LoginScreen() }
}
