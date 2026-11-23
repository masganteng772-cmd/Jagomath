package com.example.data

import com.example.data.model.MathOperation
import com.example.data.model.QuizGameMode
import com.example.data.model.QuizSessionRecord
import com.example.data.model.TableMastery
import com.example.data.model.UnlockedAchievement
import com.example.data.model.UserProfile
import com.example.domain.GamificationEngine
import com.example.domain.RewardManager
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Local in-memory repository used exclusively when the user taps "Coba Tanpa Masuk (Mode Tamu)".
 * Performs zero Firestore calls so unauthenticated sessions never touch cloud endpoints.
 */
class LocalGuestMathRepository(
    private val guestUid: String = "guest_dev_session",
    private val rewardManager: RewardManager = RewardManager()
) : MathRepository {

    private val _profile = MutableStateFlow<UserProfile?>(
        UserProfile(
            id = guestUid,
            userId = guestUid,
            displayName = "Tamu Cilik",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
    )
    private val _sessions = MutableStateFlow<List<QuizSessionRecord>>(emptyList())
    private val _masteries = MutableStateFlow<List<TableMastery>>(emptyList())
    private val _achievements = MutableStateFlow<List<UnlockedAchievement>>(emptyList())

    override fun observeUserProfile(userId: String): Flow<UserProfile?> = _profile.asStateFlow()

    override fun observeQuizSessions(userId: String): Flow<List<QuizSessionRecord>> = _sessions.asStateFlow()

    override fun observeTableMastery(userId: String): Flow<List<TableMastery>> = _masteries.asStateFlow()

    override fun observeAchievements(userId: String): Flow<List<UnlockedAchievement>> = _achievements.asStateFlow()

    override suspend fun ensureUserProfile(defaultDisplayName: String): Result<UserProfile> {
        val current = _profile.value ?: UserProfile(
            id = guestUid,
            userId = guestUid,
            displayName = defaultDisplayName.trim().take(40).ifBlank { "Tamu Cilik" },
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        ).also { _profile.value = it }
        return Result.success(current)
    }

    override suspend fun updateDisplayName(newName: String): Result<Unit> {
        val cleanName = newName.trim().take(40).ifBlank { "Tamu Cilik" }
        _profile.update { current ->
            (current ?: UserProfile(id = guestUid, userId = guestUid)).copy(
                displayName = cleanName,
                updatedAt = Timestamp.now()
            )
        }
        return Result.success(Unit)
    }

    override suspend fun recordQuizCompletion(
        input: QuizCompletionInput,
        currentProfile: UserProfile,
        existingMasteries: List<TableMastery>,
        existingUnlockedIds: Set<String>
    ): Result<QuizCompletionOutcome> {
        val now = Timestamp.now()
        val safeTotal = input.totalQuestions.coerceIn(1, 30)
        val safeCorrect = input.correctAnswers.coerceIn(0, safeTotal)
        val safeMaxCombo = input.maxCombo.coerceIn(0, safeTotal)
        val safeDuration = input.durationSeconds.coerceIn(0, 3600)
        val safeTable = input.focusTable.coerceIn(0, 12)

        val starsEarned = GamificationEngine.calculateSessionStars(safeCorrect, safeTotal)
        val baseXp = GamificationEngine.calculateSessionXp(
            correct = safeCorrect,
            total = safeTotal,
            maxCombo = safeMaxCombo,
            isTimed = input.gameMode == QuizGameMode.TIMED,
            isDaily = input.gameMode == QuizGameMode.DAILY
        )

        val newSession = QuizSessionRecord(
            id = "guest_session_${UUID.randomUUID()}",
            userId = guestUid,
            operationType = input.operation.name,
            gameMode = input.gameMode.name,
            tableNumber = safeTable.toLong(),
            totalQuestions = safeTotal.toLong(),
            correctAnswers = safeCorrect.toLong(),
            starsEarned = starsEarned.toLong(),
            xpEarned = baseXp.toLong(),
            maxCombo = safeMaxCombo.toLong(),
            durationSeconds = safeDuration.toLong(),
            createdAt = now
        )
        _sessions.update { (listOf(newSession) + it).take(25) }

        val masteryById = _masteries.value.associateBy { it.id }.toMutableMap()
        for (delta in input.tableDeltas) {
            if (delta.attempts <= 0 || delta.tableNumber !in 1..12) continue
            val op = if (delta.operation == MathOperation.DIVISION) {
                MathOperation.DIVISION
            } else {
                MathOperation.MULTIPLICATION
            }
            val docId = TableMastery.docId(op, delta.tableNumber)
            val existing = masteryById[docId]
            val nextAttempts = (existing?.attempts ?: 0L) + delta.attempts.toLong()
            val nextCorrect = ((existing?.correct ?: 0L) + delta.correct.toLong())
                .coerceAtMost(nextAttempts)
            val nextStars = GamificationEngine.calculateMasteryStars(nextCorrect, nextAttempts).toLong()
            masteryById[docId] = TableMastery(
                id = docId,
                userId = guestUid,
                operationType = op.name,
                tableNumber = delta.tableNumber.toLong(),
                attempts = nextAttempts,
                correct = nextCorrect,
                stars = nextStars,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        }
        val updatedMasteries = masteryById.values.toList()
        _masteries.value = updatedMasteries

        val latestProfile = _profile.value ?: currentProfile
        val today = GamificationEngine.todayDateString()
        val (nextStreak, nextBestStreak) = GamificationEngine.computeUpdatedStreak(
            lastPracticeDate = latestProfile.lastPracticeDate,
            currentStreak = latestProfile.currentStreakDays,
            bestStreak = latestProfile.bestStreakDays,
            today = today
        )
        val isPerfect = safeCorrect == safeTotal
        val isTimedHighAcc = input.gameMode == QuizGameMode.TIMED &&
            ((safeCorrect * 100) / safeTotal) >= 80
        val previousLevel = GamificationEngine.computeLevel(latestProfile.totalXp).level

        val tentativeProfile = latestProfile.copy(
            userId = guestUid,
            totalXp = latestProfile.totalXp + baseXp.toLong(),
            totalStars = latestProfile.totalStars + starsEarned.toLong(),
            totalQuizzes = latestProfile.totalQuizzes + 1L,
            totalQuestionsAnswered = latestProfile.totalQuestionsAnswered + safeTotal.toLong(),
            totalCorrectAnswers = latestProfile.totalCorrectAnswers + safeCorrect.toLong(),
            multiplicationCorrect = latestProfile.multiplicationCorrect + input.multiplicationCorrect.coerceAtLeast(0).toLong(),
            divisionCorrect = latestProfile.divisionCorrect + input.divisionCorrect.coerceAtLeast(0).toLong(),
            perfectQuizzes = latestProfile.perfectQuizzes + if (isPerfect) 1L else 0L,
            timedHighAccuracyQuizzes = latestProfile.timedHighAccuracyQuizzes + if (isTimedHighAcc) 1L else 0L,
            bestCombo = maxOf(latestProfile.bestCombo, safeMaxCombo.toLong()),
            currentStreakDays = nextStreak,
            bestStreakDays = nextBestStreak,
            lastPracticeDate = today,
            updatedAt = now
        )

        val currentUnlockedIds = _achievements.value.map { it.badgeId }.toSet()
        val rewardEval = rewardManager.evaluateSessionRewards(
            previousStars = latestProfile.totalStars,
            correctAnswers = safeCorrect,
            totalQuestions = safeTotal,
            projectedProfile = tentativeProfile,
            updatedMasteries = updatedMasteries,
            alreadyUnlockedBadgeIds = currentUnlockedIds
        )
        val newlyUnlocked = rewardEval.newlyUnlockedBadges
        val bonusBadgeXp = rewardEval.bonusBadgeXp

        if (newlyUnlocked.isNotEmpty()) {
            val newRecords = newlyUnlocked.map { def ->
                UnlockedAchievement(
                    id = def.id,
                    userId = guestUid,
                    badgeId = def.id,
                    xpReward = def.xpReward.toLong(),
                    unlockedAt = now
                )
            }
            _achievements.update { it + newRecords }
        }

        val finalTotalXp = tentativeProfile.totalXp + bonusBadgeXp.toLong()
        _profile.value = tentativeProfile.copy(totalXp = finalTotalXp)

        val newLevel = GamificationEngine.computeLevel(finalTotalXp).level
        return Result.success(
            QuizCompletionOutcome(
                starsEarned = starsEarned,
                baseXpEarned = baseXp,
                bonusBadgeXp = bonusBadgeXp,
                totalXpAfter = finalTotalXp,
                previousLevel = previousLevel,
                newLevel = newLevel,
                newStreakDays = nextStreak,
                newlyUnlockedBadges = newlyUnlocked,
                newlyReachedStarMilestones = rewardEval.newlyReachedStarMilestones
            )
        )
    }
}
