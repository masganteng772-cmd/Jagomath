package com.example.domain

import com.example.data.model.TableMastery
import com.example.data.model.UnlockedAchievement
import com.example.data.model.UserProfile

data class StarMilestone(
    val id: String,
    val title: String,
    val requiredStars: Int,
    val description: String
)

data class UnlockedBadgeItem(
    val definition: AchievementDefinition,
    val unlockedAtEpochSeconds: Long
)

data class RewardDashboardState(
    val totalStars: Int,
    val currentTierTitle: String,
    val nextStarMilestone: StarMilestone?,
    val starsToNextMilestone: Int,
    val nextMilestoneProgress: Float,
    val milestones: List<StarMilestone>,
    val reachedMilestoneIds: Set<String>,
    val unlockedBadges: List<UnlockedBadgeItem>,
    val totalBadgesCount: Int
)

data class RewardEvaluation(
    val starsEarned: Int,
    val newlyReachedStarMilestones: List<StarMilestone>,
    val newlyUnlockedBadges: List<AchievementDefinition>,
    val bonusBadgeXp: Int
)

/**
 * Central manager responsible for tracking user stars, evaluating star & achievement milestones,
 * and projecting a concise dashboard summary for the UI.
 */
class RewardManager {

    val starMilestones: List<StarMilestone> = listOf(
        StarMilestone(
            id = "star_bronze",
            title = "Bintang Perunggu",
            requiredStars = 5,
            description = "Kumpulkan 5 bintang pertama"
        ),
        StarMilestone(
            id = "star_silver",
            title = "Bintang Perak",
            requiredStars = 15,
            description = "Kumpulkan 15 bintang dari kuis"
        ),
        StarMilestone(
            id = "star_gold",
            title = "Bintang Emas",
            requiredStars = 30,
            description = "Kumpulkan 30 bintang dengan tekun"
        ),
        StarMilestone(
            id = "star_diamond",
            title = "Bintang Berlian",
            requiredStars = 60,
            description = "Kumpulkan 60 bintang prestasi"
        ),
        StarMilestone(
            id = "star_galaxy",
            title = "Mahkota Galaksi",
            requiredStars = 100,
            description = "Kumpulkan 100 bintang legenda"
        )
    )

    fun evaluateSessionRewards(
        previousStars: Long,
        correctAnswers: Int,
        totalQuestions: Int,
        projectedProfile: UserProfile,
        updatedMasteries: List<TableMastery>,
        alreadyUnlockedBadgeIds: Set<String>
    ): RewardEvaluation {
        val starsEarned = GamificationEngine.calculateSessionStars(correctAnswers, totalQuestions)
        val totalStarsAfter = (previousStars + starsEarned.toLong()).toInt()
        val prevStarsInt = previousStars.toInt()

        val newlyReachedMilestones = starMilestones.filter { milestone ->
            prevStarsInt < milestone.requiredStars && totalStarsAfter >= milestone.requiredStars
        }

        val newlyUnlockedBadges = GamificationEngine.evaluateNewAchievements(
            updatedProfile = projectedProfile,
            updatedMasteries = updatedMasteries,
            alreadyUnlockedIds = alreadyUnlockedBadgeIds
        )

        return RewardEvaluation(
            starsEarned = starsEarned,
            newlyReachedStarMilestones = newlyReachedMilestones,
            newlyUnlockedBadges = newlyUnlockedBadges,
            bonusBadgeXp = newlyUnlockedBadges.sumOf { it.xpReward }
        )
    }

    fun buildDashboardState(
        profile: UserProfile,
        unlockedAchievements: List<UnlockedAchievement>
    ): RewardDashboardState {
        val totalStars = profile.totalStars.toInt().coerceAtLeast(0)
        val reached = starMilestones.filter { totalStars >= it.requiredStars }
        val reachedIds = reached.map { it.id }.toSet()
        val currentTierTitle = reached.lastOrNull()?.title ?: "Pemburu Bintang"
        val nextMilestone = starMilestones.firstOrNull { totalStars < it.requiredStars }

        val prevThreshold = reached.lastOrNull()?.requiredStars ?: 0
        val (starsRemaining, progressFraction) = if (nextMilestone != null) {
            val remaining = (nextMilestone.requiredStars - totalStars).coerceAtLeast(0)
            val span = (nextMilestone.requiredStars - prevThreshold).coerceAtLeast(1)
            val progress = ((totalStars - prevThreshold).toFloat() / span.toFloat()).coerceIn(0f, 1f)
            remaining to progress
        } else {
            0 to 1f
        }

        val definitionById = GamificationEngine.allAchievements.associateBy { it.id }
        val unlockedBadgeItems = unlockedAchievements
            .mapNotNull { record ->
                definitionById[record.badgeId]?.let { def ->
                    UnlockedBadgeItem(
                        definition = def,
                        unlockedAtEpochSeconds = record.unlockedAt?.seconds ?: 0L
                    )
                }
            }
            .sortedByDescending { it.unlockedAtEpochSeconds }

        return RewardDashboardState(
            totalStars = totalStars,
            currentTierTitle = currentTierTitle,
            nextStarMilestone = nextMilestone,
            starsToNextMilestone = starsRemaining,
            nextMilestoneProgress = progressFraction,
            milestones = starMilestones,
            reachedMilestoneIds = reachedIds,
            unlockedBadges = unlockedBadgeItems,
            totalBadgesCount = GamificationEngine.allAchievements.size
        )
    }
}
