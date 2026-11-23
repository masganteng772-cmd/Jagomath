package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.RewardDashboardState
import com.example.ui.theme.EqTheme

@Composable
fun RewardDashboardSection(
    rewardState: RewardDashboardState,
    onViewAllBadges: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eqColors = EqTheme.colors
    val animatedProgress by animateFloatAsState(
        targetValue = rewardState.nextMilestoneProgress,
        animationSpec = spring(stiffness = 220f),
        label = "star_milestone_progress"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, eqColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("reward_dashboard_section")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title + Star Tier + Total Stars Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reward_dashboard_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = rewardState.currentTierTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = eqColors.starAccent
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = eqColors.starContainer,
                    contentColor = eqColors.onStarContainer,
                    border = BorderStroke(1.dp, eqColors.starAccent.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .semantics(mergeDescendants = true) {
                            contentDescription = "Total bintang terkumpul: ${rewardState.totalStars} bintang"
                        }
                        .testTag("reward_total_stars_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = eqColors.starAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${rewardState.totalStars} Bintang",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            // Star Milestone Progress Track
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val next = rewardState.nextStarMilestone
                val milestoneStatusText = if (next != null) {
                    "${rewardState.starsToNextMilestone} bintang lagi ke ${next.title}"
                } else {
                    "Semua target bintang tercapai!"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = milestoneStatusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (next != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${rewardState.totalStars}/${next.requiredStars} ★",
                            style = MaterialTheme.typography.labelLarge,
                            color = eqColors.danger
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .semantics {
                            contentDescription = if (next != null) {
                                "Progres target bintang: ${rewardState.totalStars} dari ${next.requiredStars} bintang menuju ${next.title}"
                            } else {
                                "Semua target bintang telah tercapai"
                            }
                            progressBarRangeInfo = ProgressBarRangeInfo(
                                current = animatedProgress,
                                range = 0f..1f
                            )
                        },
                    color = eqColors.starFill,
                    trackColor = eqColors.progressTrack,
                    strokeCap = StrokeCap.Round
                )

                // Milestone Nodes Strip (5, 15, 30, 60, 100 stars)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rewardState.milestones.forEach { milestone ->
                        val reached = milestone.id in rewardState.reachedMilestoneIds
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "${milestone.title}, ${milestone.requiredStars} bintang, ${if (reached) "sudah tercapai" else "belum tercapai"}"
                                }
                                .testTag("star_milestone_${milestone.requiredStars}")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (reached) eqColors.success else eqColors.subtleSurfaceTint,
                                border = if (reached) null else BorderStroke(1.dp, eqColors.cardBorder),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (reached) Icons.Rounded.Check else Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = if (reached) eqColors.onSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${milestone.requiredStars}★",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (reached) eqColors.success else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Unlocked Badges Shelf
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lencana Terbuka (${rewardState.unlockedBadges.size}/${rewardState.totalBadgesCount})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onViewAllBadges,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("reward_view_all_badges_button")
                    ) {
                        Text(
                            text = stringResource(R.string.reward_view_all_badges),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (rewardState.unlockedBadges.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = eqColors.subtleSurfaceTint,
                        border = BorderStroke(1.dp, eqColors.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .semantics(mergeDescendants = true) {}
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Buka daftar semua lencana",
                                onClick = onViewAllBadges
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = eqColors.info,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = stringResource(R.string.reward_empty_badges_tip),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(end = 4.dp)
                    ) {
                        items(rewardState.unlockedBadges, key = { it.definition.id }) { badgeItem ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = eqColors.successContainer,
                                border = BorderStroke(1.dp, eqColors.success.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "Lencana terbuka: ${badgeItem.definition.title}"
                                    }
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "Lihat detail lencana ${badgeItem.definition.title}",
                                        onClick = onViewAllBadges
                                    )
                                    .testTag("dashboard_badge_${badgeItem.definition.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(eqColors.success),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.EmojiEvents,
                                            contentDescription = null,
                                            tint = eqColors.onSuccess,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Text(
                                        text = badgeItem.definition.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = eqColors.onSuccessContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
