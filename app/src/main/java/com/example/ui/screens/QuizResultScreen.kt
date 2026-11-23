package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.GamificationEngine
import com.example.ui.QuizResultState
import com.example.ui.theme.EqTheme

@Composable
fun QuizResultScreen(
    state: QuizResultState,
    onPlayAgain: () -> Unit,
    onBackHome: () -> Unit
) {
    val eqColors = EqTheme.colors

    BackHandler {
        onBackHome()
    }

    val total = state.totalQuestions.coerceAtLeast(1)
    val accuracy = ((state.correctAnswers * 100) / total).coerceIn(0, 100)
    val stars = state.outcome?.starsEarned
        ?: GamificationEngine.calculateSessionStars(state.correctAnswers, total)
    val headline = when {
        accuracy >= 90 -> stringResource(R.string.result_title_great)
        accuracy >= 65 -> stringResource(R.string.result_title_good)
        else -> stringResource(R.string.result_title_keep_going)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, eqColors.cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.size(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_trophy_celebration_1795454062869),
                    contentDescription = stringResource(R.string.result_trophy_desc),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Star Rating Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = "Mendapat $stars dari 3 bintang"
                }
            ) {
                repeat(3) { idx ->
                    val earned = idx < stars
                    Icon(
                        imageVector = if (earned) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = null,
                        tint = if (earned) eqColors.starFill else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(if (idx == 1) 48.dp else 38.dp)
                    )
                }
            }

            Text(
                text = headline,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )

            Text(
                text = "Kamu menjawab benar ${state.correctAnswers} dari $total soal (${accuracy}%)!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (state.saving) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        liveRegion = LiveRegionMode.Polite
                    }
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp
                    )
                    Text(
                        text = "Menyimpan progres dan memeriksa lencana...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (state.saveError != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Assertive }
                ) {
                    Text(
                        text = state.saveError,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Level-up Celebration Banner
            state.outcome?.let { out ->
                if (out.newLevel > out.previousLevel) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = eqColors.successContainer,
                        contentColor = eqColors.onSuccessContainer,
                        border = BorderStroke(1.dp, eqColors.success.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {}
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = eqColors.success,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "Naik ke Level ${out.newLevel}!",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = GamificationEngine.computeLevel(out.totalXpAfter).titleId,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Stats Grid
            val totalXpGain = (state.outcome?.baseXpEarned ?: 0) + (state.outcome?.bonusBadgeXp ?: 0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResultStatCard(
                    icon = Icons.Rounded.CheckCircle,
                    tint = eqColors.success,
                    value = "$accuracy%",
                    label = "Akurasi",
                    modifier = Modifier.weight(1f)
                )
                ResultStatCard(
                    icon = Icons.Rounded.LocalFireDepartment,
                    tint = eqColors.danger,
                    value = "x${state.maxCombo}",
                    label = "Kombo Terbaik",
                    modifier = Modifier.weight(1f)
                )
                ResultStatCard(
                    icon = Icons.Rounded.AutoAwesome,
                    tint = eqColors.info,
                    value = "+$totalXpGain",
                    label = "Total XP",
                    modifier = Modifier.weight(1f)
                )
            }

            // Newly Reached Star Milestones (Theme-safe surface cards for both light & dark mode!)
            val newStarMilestones = state.outcome?.newlyReachedStarMilestones.orEmpty()
            if (newStarMilestones.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("result_star_milestone_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = eqColors.starAccent
                            )
                            Text(
                                text = stringResource(R.string.result_new_star_milestone),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.semantics { heading() }
                            )
                        }
                        newStarMilestones.forEach { milestone ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, eqColors.cardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {}
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(eqColors.danger),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = eqColors.onDanger,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = milestone.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = milestone.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${milestone.requiredStars}★",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = eqColors.danger
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Newly Unlocked Badges Section
            val newBadges = state.outcome?.newlyUnlockedBadges.orEmpty()
            if (newBadges.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = eqColors.starContainer
                    ),
                    border = BorderStroke(1.dp, eqColors.starAccent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = eqColors.starAccent
                            )
                            Text(
                                text = stringResource(R.string.result_new_badges),
                                style = MaterialTheme.typography.titleLarge,
                                color = eqColors.onStarContainer,
                                modifier = Modifier.semantics { heading() }
                            )
                        }
                        newBadges.forEach { badge ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, eqColors.cardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {}
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(eqColors.starContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.EmojiEvents,
                                            contentDescription = null,
                                            tint = eqColors.starAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = badge.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = badge.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "+${badge.xpReward} XP",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = eqColors.starAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Button(
                onClick = onPlayAgain,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .testTag("play_again_button")
            ) {
                Icon(imageVector = Icons.Rounded.Replay, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.result_play_again),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            OutlinedButton(
                onClick = onBackHome,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("back_home_button")
            ) {
                Icon(imageVector = Icons.Rounded.Home, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.result_back_home),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun ResultStatCard(
    icon: ImageVector,
    tint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val eqColors = EqTheme.colors
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, eqColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label: $value"
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
