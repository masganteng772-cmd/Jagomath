package com.example

import com.example.data.model.UnlockedAchievement
import com.example.data.model.UserProfile
import com.example.domain.RewardManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardManagerTest {

    private val rewardManager = RewardManager()

    @Test
    fun `evaluateSessionRewards detects crossed star milestone and first quiz badge`() {
        val projected = UserProfile(
            userId = "u1",
            totalStars = 5L,
            totalQuizzes = 1L,
            totalQuestionsAnswered = 10L,
            totalCorrectAnswers = 10L,
            perfectQuizzes = 1L
        )
        val result = rewardManager.evaluateSessionRewards(
            previousStars = 2L,
            correctAnswers = 10,
            totalQuestions = 10,
            projectedProfile = projected,
            updatedMasteries = emptyList(),
            alreadyUnlockedBadgeIds = emptySet()
        )

        assertEquals(3, result.starsEarned)
        assertEquals(listOf("star_bronze"), result.newlyReachedStarMilestones.map { it.id })
        assertTrue(result.newlyUnlockedBadges.any { it.id == "langkah_pertama" })
        assertTrue(result.newlyUnlockedBadges.any { it.id == "nilai_sempurna" })
    }

    @Test
    fun `buildDashboardState computes next star milestone and tier accurately`() {
        val profile = UserProfile(userId = "u1", totalStars = 18L)
        val unlocked = listOf(
            UnlockedAchievement(id = "langkah_pertama", userId = "u1", badgeId = "langkah_pertama", xpReward = 50L)
        )

        val dashboard = rewardManager.buildDashboardState(profile, unlocked)

        assertEquals(18, dashboard.totalStars)
        assertEquals("Bintang Perak", dashboard.currentTierTitle)
        assertEquals("star_gold", dashboard.nextStarMilestone?.id)
        assertEquals(12, dashboard.starsToNextMilestone)
        assertEquals(1, dashboard.unlockedBadges.size)
        assertTrue("star_bronze" in dashboard.reachedMilestoneIds)
        assertTrue("star_silver" in dashboard.reachedMilestoneIds)
    }

    @Test
    fun `buildDashboardState handles max star tier reached`() {
        val profile = UserProfile(userId = "u1", totalStars = 120L)
        val dashboard = rewardManager.buildDashboardState(profile, emptyList())

        assertEquals("Mahkota Galaksi", dashboard.currentTierTitle)
        assertNull(dashboard.nextStarMilestone)
        assertEquals(0, dashboard.starsToNextMilestone)
        assertEquals(1f, dashboard.nextMilestoneProgress, 0.001f)
    }
}
