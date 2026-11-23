package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.TableMastery
import com.example.data.model.UnlockedAchievement
import com.example.data.model.UserProfile
import com.example.domain.AchievementDefinition
import com.example.domain.BadgeCategory
import com.example.domain.GamificationEngine
import com.example.ui.theme.EqSemanticColors
import com.example.ui.theme.EqTheme
import com.example.ui.theme.EqYellow500

private enum class BadgeFilter { ALL, UNLOCKED, LOCKED }

@Composable
fun AchievementsScreen(
    profile: UserProfile,
    tableMasteries: List<TableMastery>,
    unlockedAchievements: List<UnlockedAchievement>,
    modifier: Modifier = Modifier
) {
    val eqColors = EqTheme.colors
    var selectedFilter by remember { mutableStateOf(BadgeFilter.ALL) }
    val unlockedIds = remember(unlockedAchievements) {
        unlockedAchievements.map { it.badgeId }.toSet()
    }
    val allDefs = GamificationEngine.allAchievements
    val filtered = remember(selectedFilter, unlockedIds) {
        when (selectedFilter) {
            BadgeFilter.ALL -> allDefs
            BadgeFilter.UNLOCKED -> allDefs.filter { it.id in unlockedIds }
            BadgeFilter.LOCKED -> allDefs.filter { it.id !in unlockedIds }
        }
    }
    val totalUnlocked = unlockedIds.size
    val totalBadgeXp = unlockedAchievements.sumOf { it.xpReward }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.achievements_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
            }

            // Summary Banner (EQ-Math Deep Navy — AAA Contrast)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics(mergeDescendants = true) {
                            contentDescription = "$totalUnlocked dari ${allDefs.size} lencana terbuka. Total bonus $totalBadgeXp XP dari lencana."
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(eqColors.brandNavyStart, eqColors.brandNavyEnd)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$totalUnlocked dari ${allDefs.size} Lencana",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = eqColors.onBrandNavy
                                    )
                                    Text(
                                        text = "Total bonus +$totalBadgeXp XP dari lencana",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = eqColors.onBrandNavyMuted
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        tint = EqYellow500,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            LinearProgressIndicator(
                                progress = {
                                    (totalUnlocked.toFloat() / allDefs.size.coerceAtLeast(1).toFloat())
                                        .coerceIn(0f, 1f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .semantics {
                                        progressBarRangeInfo = ProgressBarRangeInfo(
                                            current = totalUnlocked.toFloat(),
                                            range = 0f..allDefs.size.coerceAtLeast(1).toFloat()
                                        )
                                    },
                                color = EqYellow500,
                                trackColor = Color.White.copy(alpha = 0.28f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            // Filter Chips (>= 48dp touch target)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chipColors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    FilterChip(
                        selected = selectedFilter == BadgeFilter.ALL,
                        onClick = { selectedFilter = BadgeFilter.ALL },
                        label = { Text(stringResource(R.string.achievements_filter_all)) },
                        colors = chipColors,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("filter_all_badges")
                    )
                    FilterChip(
                        selected = selectedFilter == BadgeFilter.UNLOCKED,
                        onClick = { selectedFilter = BadgeFilter.UNLOCKED },
                        label = { Text("${stringResource(R.string.achievements_filter_unlocked)} ($totalUnlocked)") },
                        colors = chipColors,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("filter_unlocked_badges")
                    )
                    FilterChip(
                        selected = selectedFilter == BadgeFilter.LOCKED,
                        onClick = { selectedFilter = BadgeFilter.LOCKED },
                        label = { Text(stringResource(R.string.achievements_filter_locked)) },
                        colors = chipColors,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("filter_locked_badges")
                    )
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, eqColors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = eqColors.starAccent,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = if (selectedFilter == BadgeFilter.UNLOCKED) {
                                    "Belum ada lencana terbuka. Selesaikan kuis pertamamu untuk membuka lencana!"
                                } else {
                                    "Keren! Semua lencana sudah berhasil kamu kumpulkan!"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { badge ->
                    val isUnlocked = badge.id in unlockedIds
                    val rawProgress = badge.progressProvider(profile, tableMasteries)
                    AchievementCard(
                        definition = badge,
                        isUnlocked = isUnlocked,
                        currentValue = rawProgress.coerceAtMost(badge.targetValue)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun AchievementCard(
    definition: AchievementDefinition,
    isUnlocked: Boolean,
    currentValue: Int
) {
    val eqColors = EqTheme.colors
    val accent = categoryAccentColor(definition.category, eqColors)
    val accentContainer = categoryContainerColor(definition.category, eqColors)
    val icon = categoryIcon(definition.category)
    val progress = if (isUnlocked) 1f else {
        (currentValue.toFloat() / definition.targetValue.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    }
    val spokenSummary = if (isUnlocked) {
        "${definition.title}. ${definition.description}. Status: sudah terbuka."
    } else {
        "${definition.title}. ${definition.description}. Status: terkunci, progres $currentValue dari ${definition.targetValue}, hadiah ${definition.xpReward} XP."
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (isUnlocked) eqColors.success.copy(alpha = 0.45f) else eqColors.cardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = spokenSummary
            }
            .testTag("badge_card_${definition.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) accentContainer else eqColors.subtleSurfaceTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) icon else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = definition.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = eqColors.successContainer,
                            border = BorderStroke(1.dp, eqColors.success.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = eqColors.success,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Terbuka",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = eqColors.onSuccessContainer
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "+${definition.xpReward} XP",
                            style = MaterialTheme.typography.labelLarge,
                            color = eqColors.starAccent
                        )
                    }
                }

                Text(
                    text = definition.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = if (isUnlocked) eqColors.success else accent,
                        trackColor = eqColors.progressTrack,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = if (isUnlocked) "${definition.targetValue}/${definition.targetValue}" else "$currentValue/${definition.targetValue}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun categoryAccentColor(category: BadgeCategory, eqColors: EqSemanticColors): Color = when (category) {
    BadgeCategory.MILESTONE -> eqColors.starAccent
    BadgeCategory.MULTIPLICATION -> eqColors.danger
    BadgeCategory.DIVISION -> eqColors.info
    BadgeCategory.STREAK -> eqColors.danger
    BadgeCategory.MASTERY -> eqColors.success
}

private fun categoryContainerColor(category: BadgeCategory, eqColors: EqSemanticColors): Color = when (category) {
    BadgeCategory.MILESTONE -> eqColors.starContainer
    BadgeCategory.MULTIPLICATION -> eqColors.dangerContainer
    BadgeCategory.DIVISION -> eqColors.infoContainer
    BadgeCategory.STREAK -> eqColors.dangerContainer
    BadgeCategory.MASTERY -> eqColors.successContainer
}

private fun categoryIcon(category: BadgeCategory): ImageVector = when (category) {
    BadgeCategory.MILESTONE -> Icons.Rounded.EmojiEvents
    BadgeCategory.MULTIPLICATION -> Icons.Rounded.Calculate
    BadgeCategory.DIVISION -> Icons.Rounded.Percent
    BadgeCategory.STREAK -> Icons.Rounded.LocalFireDepartment
    BadgeCategory.MASTERY -> Icons.Rounded.School
}
