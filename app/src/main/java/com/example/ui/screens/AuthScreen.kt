package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.ui.theme.EqBlue500
import com.example.ui.theme.EqGreen500
import com.example.ui.theme.EqRed500
import com.example.ui.theme.EqTheme
import com.example.ui.theme.EqWhite
import com.example.ui.theme.EqYellow500

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val eqColors = EqTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isSigningIn by remember { mutableStateOf(false) }
    var isCheckingAutoSignIn by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isCheckingAutoSignIn = false
                onAuthSuccess()
            },
            onUnauthenticated = {
                isCheckingAutoSignIn = false
            },
            scope = scope
        )
    }

    val primaryGlow = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    val infoGlow = eqColors.infoContainer.copy(alpha = 0.45f)
    val starGlow = eqColors.starContainer.copy(alpha = 0.45f)
    val bgTop = MaterialTheme.colorScheme.background
    val bgBottom = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
            .drawBehind {
                drawCircle(
                    color = primaryGlow,
                    radius = size.minDimension * 0.44f,
                    center = Offset(size.width * 0.92f, size.height * 0.08f)
                )
                drawCircle(
                    color = infoGlow,
                    radius = size.minDimension * 0.34f,
                    center = Offset(size.width * 0.08f, size.height * 0.26f)
                )
                drawCircle(
                    color = starGlow,
                    radius = size.minDimension * 0.36f,
                    center = Offset(size.width * 0.85f, size.height * 0.92f)
                )
            }
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Official EQ-Math Brand Showcase Card (Always crisp white plate so dark-navy logo stays sharp in both light & dark modes)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = eqColors.logoPlateBackground),
                border = BorderStroke(1.dp, eqColors.logoPlateBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(EqYellow500))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(EqRed500))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(EqGreen500))
                        Box(modifier = Modifier.weight(1f).fillMaxSize().background(EqBlue500))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_eq_math_logo),
                            contentDescription = stringResource(R.string.auth_mascot_desc),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth(0.86f)
                                .height(74.dp)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(eqColors.brandNavyStart, eqColors.brandNavyEnd))
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "PERKALIAN × & PEMBAGIAN ÷ SD",
                                style = MaterialTheme.typography.labelMedium,
                                color = EqWhite,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.auth_welcome_title),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.auth_welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4-Quadrant Inspired Feature Highlights (Theme-aware light/dark contrast)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeaturePill(
                    icon = Icons.Rounded.Calculate,
                    label = "Kali & Bagi",
                    containerColor = eqColors.dangerContainer,
                    iconTint = eqColors.danger,
                    contentColor = eqColors.onDangerContainer,
                    modifier = Modifier.weight(1f)
                )
                FeaturePill(
                    icon = Icons.Rounded.Star,
                    label = "Bintang",
                    containerColor = eqColors.starContainer,
                    iconTint = eqColors.starAccent,
                    contentColor = eqColors.onStarContainer,
                    modifier = Modifier.weight(1f)
                )
                FeaturePill(
                    icon = Icons.Rounded.EmojiEvents,
                    label = "12 Lencana",
                    containerColor = eqColors.infoContainer,
                    iconTint = eqColors.info,
                    contentColor = eqColors.onInfoContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .semantics { liveRegion = LiveRegionMode.Assertive }
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Button(
                onClick = {
                    errorMessage = null
                    isSigningIn = true
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isSigningIn = false
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            isSigningIn = false
                            errorMessage = msg
                        },
                        scope = scope,
                        onAuthCancelled = {
                            isSigningIn = false
                        }
                    )
                },
                enabled = !isSigningIn && !isCheckingAutoSignIn,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .testTag("google_sign_in_button")
            ) {
                if (isSigningIn || isCheckingAutoSignIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Login,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.auth_sign_in_google),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onContinueAsGuest,
                enabled = !isSigningIn,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .testTag("guest_dev_login_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.auth_guest_mode),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = eqColors.successContainer,
                border = BorderStroke(1.dp, eqColors.success.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {}
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudDone,
                        contentDescription = null,
                        tint = eqColors.success,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.auth_cloud_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = eqColors.onSuccessContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    iconTint: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.25f)),
        modifier = modifier.semantics(mergeDescendants = true) {}
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
