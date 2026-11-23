package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MathOperation
import com.example.data.model.TableMastery
import com.example.data.model.UnlockedAchievement
import com.example.data.model.UserProfile
import com.example.domain.GamificationEngine
import com.example.domain.RewardDashboardState
import com.example.ui.components.RewardDashboardSection
import com.example.ui.theme.EqBlue500
import com.example.ui.theme.EqGreen500
import com.example.ui.theme.EqNavy700
import com.example.ui.theme.EqNavy900
import com.example.ui.theme.EqRed500
import com.example.ui.theme.EqTheme
import com.example.ui.theme.EqWhite
import com.example.ui.theme.EqYellow500

@Composable
fun HomeScreen(
    profile: UserProfile,
    tableMasteries: List<TableMastery>,
    unlockedAchievements: List<UnlockedAchievement>,
    rewardDashboard: RewardDashboardState,
    onStartDailyChallenge: () -> Unit,
    onStartOperation: (MathOperation) -> Unit,
    onOpenTableGuide: () -> Unit,
    onOpenAchievements: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eqColors = EqTheme.colors
    val levelInfo = GamificationEngine.computeLevel(profile.totalXp)
    val animatedXp by animateFloatAsState(
        targetValue = levelInfo.progressFraction,
        animationSpec = spring(stiffness = 220f),
        label = "xp_progress"
    )
    val unlockedIds = unlockedAchievements.map { it.badgeId }.toSet()
    val nextLockedBadge = GamificationEngine.allAchievements.firstOrNull { it.id !in unlockedIds }
    val masteredCount = tableMasteries.count { it.stars >= 2L }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. EQ-Math Brand Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = eqColors.logoPlateBackground,
                        border = BorderStroke(1.dp, eqColors.logoPlateBorder)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_eq_math_logo),
                            contentDescription = stringResource(R.string.app_name),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .height(26.dp)
                                .width(114.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = eqColors.starContainer,
                        contentColor = eqColors.onStarContainer,
                        border = BorderStroke(1.dp, eqColors.starAccent.copy(alpha = 0.3f)),
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = "Rekor belajar harian ${profile.currentStreakDays} hari berturut-turut"
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = eqColors.danger,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${profile.currentStreakDays} hr",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }

            // 1. Greeting & Gamification Header Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, eqColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Decorative 4-Quadrant EQ-Math Brand Stripe
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f).fillMaxSize().background(EqYellow500))
                            Box(modifier = Modifier.weight(1f).fillMaxSize().background(EqRed500))
                            Box(modifier = Modifier.weight(1f).fillMaxSize().background(EqGreen500))
                            Box(modifier = Modifier.weight(1f).fillMaxSize().background(EqBlue500))
                        }

                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.home_greeting, profile.displayName),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.semantics { heading() }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.home_subtitle),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Level & XP Bar
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "Lv. ${levelInfo.level}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = levelInfo.titleId,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${levelInfo.currentLevelXp}/${levelInfo.nextLevelXp} XP",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { animatedXp },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .semantics {
                                            contentDescription = "Progres level ${levelInfo.level}, ${levelInfo.currentLevelXp} dari ${levelInfo.nextLevelXp} XP"
                                            progressBarRangeInfo = ProgressBarRangeInfo(
                                                current = levelInfo.currentLevelXp.toFloat(),
                                                range = 0f..levelInfo.nextLevelXp.coerceAtLeast(1).toFloat()
                                            )
                                        },
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = eqColors.progressTrack,
                                    strokeCap = StrokeCap.Round
                                )
                            }

                            // Quick Stats Strip (WCAG AA+ contrast in both light & dark modes)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MiniStatBadge(
                                    icon = Icons.Rounded.Star,
                                    iconTint = eqColors.starAccent,
                                    value = "${profile.totalStars}",
                                    label = "Bintang",
                                    spokenDescription = "Bintang: ${profile.totalStars}",
                                    modifier = Modifier.weight(1f)
                                )
                                MiniStatBadge(
                                    icon = Icons.Rounded.EmojiEvents,
                                    iconTint = eqColors.info,
                                    value = "${unlockedAchievements.size}/${GamificationEngine.allAchievements.size}",
                                    label = "Lencana",
                                    spokenDescription = "Lencana terbuka: ${unlockedAchievements.size} dari ${GamificationEngine.allAchievements.size}",
                                    modifier = Modifier.weight(1f)
                                )
                                MiniStatBadge(
                                    icon = Icons.Rounded.AutoAwesome,
                                    iconTint = eqColors.success,
                                    value = "$masteredCount/24",
                                    label = "Tabel Mahir",
                                    spokenDescription = "Tabel mahir: $masteredCount dari 24",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Reward Dashboard (Stars, Milestones & Badges)
            item {
                RewardDashboardSection(
                    rewardState = rewardDashboard,
                    onViewAllBadges = onOpenAchievements
                )
            }

            // 3. Daily Challenge Hero Banner (EQ-Math Deep Navy Signature Gradient — AAA Contrast)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = EqYellow500,
                                contentColor = EqNavy900
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bolt,
                                        contentDescription = null,
                                        tint = EqNavy900,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "+30 BONUS XP",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EqNavy900
                                    )
                                }
                            }

                            Text(
                                text = stringResource(R.string.home_daily_title),
                                style = MaterialTheme.typography.headlineLarge,
                                color = eqColors.onBrandNavy,
                                modifier = Modifier.semantics { heading() }
                            )
                            Text(
                                text = stringResource(R.string.home_daily_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = eqColors.onBrandNavyMuted
                            )

                            Button(
                                onClick = onStartDailyChallenge,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EqWhite,
                                    contentColor = EqNavy700
                                ),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("daily_challenge_button")
                            ) {
                                Text(
                                    text = stringResource(R.string.home_daily_cta),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Adventure Mode Cards (EQ-Math Accessible 4-Quadrant Palette)
            item {
                Text(
                    text = stringResource(R.string.home_modes_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .semantics { heading() }
                )
            }

            item {
                AdventureModeCard(
                    title = stringResource(R.string.home_mul_title),
                    subtitle = stringResource(R.string.home_mul_desc),
                    symbol = "×",
                    icon = Icons.Rounded.Close,
                    badgeStart = eqColors.modeMulStart,
                    badgeEnd = eqColors.modeMulEnd,
                    arrowTint = eqColors.danger,
                    arrowContainer = eqColors.dangerContainer,
                    testTag = "quick_multiplication_card",
                    onClick = { onStartOperation(MathOperation.MULTIPLICATION) }
                )
            }

            item {
                AdventureModeCard(
                    title = stringResource(R.string.home_div_title),
                    subtitle = stringResource(R.string.home_div_desc),
                    symbol = "÷",
                    icon = Icons.Rounded.Percent,
                    badgeStart = eqColors.modeDivStart,
                    badgeEnd = eqColors.modeDivEnd,
                    arrowTint = eqColors.info,
                    arrowContainer = eqColors.infoContainer,
                    testTag = "quick_division_card",
                    onClick = { onStartOperation(MathOperation.DIVISION) }
                )
            }

            item {
                AdventureModeCard(
                    title = stringResource(R.string.home_mixed_title),
                    subtitle = stringResource(R.string.home_mixed_desc),
                    symbol = "×÷",
                    icon = Icons.Rounded.Shuffle,
                    badgeStart = eqColors.modeMixStart,
                    badgeEnd = eqColors.modeMixEnd,
                    arrowTint = eqColors.success,
                    arrowContainer = eqColors.successContainer,
                    testTag = "quick_mixed_card",
                    onClick = { onStartOperation(MathOperation.MIXED) }
                )
            }

            // 5. Smart Table Cheat Sheet Shortcut
            item {
                val guideTitle = stringResource(R.string.table_guide_title)
                OutlinedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = eqColors.infoContainer
                    ),
                    border = BorderStroke(1.dp, eqColors.info.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .semantics(mergeDescendants = true) {}
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Buka $guideTitle",
                            onClick = onOpenTableGuide
                        )
                        .testTag("open_table_guide_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(eqColors.info),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = null,
                                tint = eqColors.onInfo,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = guideTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = eqColors.onInfoContainer
                            )
                            Text(
                                text = stringResource(R.string.home_cheat_sheet_cta),
                                style = MaterialTheme.typography.bodySmall,
                                color = eqColors.onInfoContainer.copy(alpha = 0.88f)
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = eqColors.info
                        )
                    }
                }
            }

            // 6. Next Achievement Motivation Teaser
            if (nextLockedBadge != null) {
                item {
                    val current = nextLockedBadge.progressProvider(profile, tableMasteries)
                        .coerceAtMost(nextLockedBadge.targetValue)
                    val progress = (current.toFloat() / nextLockedBadge.targetValue.coerceAtLeast(1).toFloat())
                        .coerceIn(0f, 1f)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, eqColors.cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .semantics(mergeDescendants = true) {
                                contentDescription = "Target lencana berikutnya: ${nextLockedBadge.title}. ${nextLockedBadge.description}. Progres $current dari ${nextLockedBadge.targetValue}. Bonus ${nextLockedBadge.xpReward} XP."
                            }
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Lihat semua koleksi lencana",
                                onClick = onOpenAchievements
                            )
                            .testTag("next_badge_teaser_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Target Lencana Berikutnya",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "+${nextLockedBadge.xpReward} XP",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = eqColors.starAccent
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(eqColors.starContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.EmojiEvents,
                                        contentDescription = null,
                                        tint = eqColors.starAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nextLockedBadge.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = nextLockedBadge.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "$current/${nextLockedBadge.targetValue}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(999.dp)),
                                color = eqColors.starFill,
                                trackColor = eqColors.progressTrack,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun MiniStatBadge(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    spokenDescription: String,
    modifier: Modifier = Modifier
) {
    val eqColors = EqTheme.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = eqColors.subtleSurfaceTint,
        border = BorderStroke(1.dp, eqColors.cardBorder.copy(alpha = 0.75f)),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = spokenDescription
        }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdventureModeCard(
    title: String,
    subtitle: String,
    symbol: String,
    icon: ImageVector,
    badgeStart: Color,
    badgeEnd: Color,
    arrowTint: Color,
    arrowContainer: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val eqColors = EqTheme.colors
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, eqColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "$title. $subtitle"
            }
            .clickable(
                role = Role.Button,
                onClickLabel = "Buka $title",
                onClick = onClick
            )
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(badgeStart, badgeEnd))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.headlineMedium,
                    color = EqWhite
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(arrowContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = arrowTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
