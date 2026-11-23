package com.example.data

import android.content.Context
import com.example.R
import com.example.data.model.MathOperation
import com.example.data.model.QuizGameMode
import com.example.data.model.QuizSessionRecord
import com.example.data.model.TableMastery
import com.example.data.model.UnlockedAchievement
import com.example.data.model.UserProfile
import com.example.domain.AchievementDefinition
import com.example.domain.GamificationEngine
import com.example.domain.RewardManager
import com.example.domain.StarMilestone
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class TableAttemptDelta(
    val operation: MathOperation,
    val tableNumber: Int,
    val attempts: Int,
    val correct: Int
)

data class QuizCompletionInput(
    val operation: MathOperation,
    val gameMode: QuizGameMode,
    val focusTable: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val multiplicationCorrect: Int,
    val divisionCorrect: Int,
    val maxCombo: Int,
    val durationSeconds: Int,
    val tableDeltas: List<TableAttemptDelta>
)

data class QuizCompletionOutcome(
    val starsEarned: Int,
    val baseXpEarned: Int,
    val bonusBadgeXp: Int,
    val totalXpAfter: Long,
    val previousLevel: Int,
    val newLevel: Int,
    val newStreakDays: Long,
    val newlyUnlockedBadges: List<AchievementDefinition>,
    val newlyReachedStarMilestones: List<StarMilestone> = emptyList()
)

interface MathRepository {
    fun observeUserProfile(userId: String): Flow<UserProfile?>
    fun observeQuizSessions(userId: String): Flow<List<QuizSessionRecord>>
    fun observeTableMastery(userId: String): Flow<List<TableMastery>>
    fun observeAchievements(userId: String): Flow<List<UnlockedAchievement>>
    suspend fun ensureUserProfile(defaultDisplayName: String): Result<UserProfile>
    suspend fun updateDisplayName(newName: String): Result<Unit>
    suspend fun recordQuizCompletion(
        input: QuizCompletionInput,
        currentProfile: UserProfile,
        existingMasteries: List<TableMastery>,
        existingUnlockedIds: Set<String>
    ): Result<QuizCompletionOutcome>
}

class MathProgressRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
    private val rewardManager: RewardManager = RewardManager()
) : MathRepository {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk dengan Google terlebih dahulu.")
    }

    override fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "UID pengguna tidak sesuai." }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    override fun observeQuizSessions(userId: String): Flow<List<QuizSessionRecord>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "UID pengguna tidak sesuai." }
        val path = "users/$uid/quizSessions"
        emitAll(
            db.collection("users").document(uid).collection("quizSessions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(25)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        QuizSessionRecord::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override fun observeTableMastery(userId: String): Flow<List<TableMastery>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "UID pengguna tidak sesuai." }
        val path = "users/$uid/tableMastery"
        emitAll(
            db.collection("users").document(uid).collection("tableMastery")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TableMastery::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override fun observeAchievements(userId: String): Flow<List<UnlockedAchievement>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "UID pengguna tidak sesuai." }
        val path = "users/$uid/achievements"
        emitAll(
            db.collection("users").document(uid).collection("achievements")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        UnlockedAchievement::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    override suspend fun ensureUserProfile(defaultDisplayName: String): Result<UserProfile> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val userRef = db.collection("users").document(uid)
            docPath = userRef.path
            val existingSnap = userRef.get().await()
            if (existingSnap.exists()) {
                val existing = existingSnap.toObject(
                    UserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                if (existing != null) return Result.success(existing)
            }

            val cleanName = sanitizeDisplayName(defaultDisplayName)
            val createPayload = mapOf(
                "userId" to uid,
                "displayName" to cleanName,
                "totalXp" to 0L,
                "totalStars" to 0L,
                "totalQuizzes" to 0L,
                "totalQuestionsAnswered" to 0L,
                "totalCorrectAnswers" to 0L,
                "multiplicationCorrect" to 0L,
                "divisionCorrect" to 0L,
                "perfectQuizzes" to 0L,
                "timedHighAccuracyQuizzes" to 0L,
                "bestCombo" to 0L,
                "currentStreakDays" to 0L,
                "bestStreakDays" to 0L,
                "lastPracticeDate" to "",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.set(createPayload).await()
            Result.success(UserProfile(id = uid, userId = uid, displayName = cleanName))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun updateDisplayName(newName: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
            val cleanName = sanitizeDisplayName(newName)
            val userRef = db.collection("users").document(uid)
            docPath = userRef.path
            userRef.update(
                mapOf(
                    "displayName" to cleanName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    override suspend fun recordQuizCompletion(
        input: QuizCompletionInput,
        currentProfile: UserProfile,
        existingMasteries: List<TableMastery>,
        existingUnlockedIds: Set<String>
    ): Result<QuizCompletionOutcome> {
        var activePath = "users/${auth.currentUser?.uid ?: "unauthenticated"}"
        return try {
            val uid = requireUserId()
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

            // 1. Write immutable QuizSession document
            val sessionRef = db.collection("users").document(uid)
                .collection("quizSessions").document()
            activePath = sessionRef.path
            val sessionPayload = mapOf(
                "userId" to uid,
                "operationType" to input.operation.name,
                "gameMode" to input.gameMode.name,
                "tableNumber" to safeTable.toLong(),
                "totalQuestions" to safeTotal.toLong(),
                "correctAnswers" to safeCorrect.toLong(),
                "starsEarned" to starsEarned.toLong(),
                "xpEarned" to baseXp.toLong(),
                "maxCombo" to safeMaxCombo.toLong(),
                "durationSeconds" to safeDuration.toLong(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            sessionRef.set(sessionPayload).await()

            // 2. Update per-table mastery records
            val masteryById = existingMasteries.associateBy { it.id }.toMutableMap()
            for (delta in input.tableDeltas) {
                if (delta.attempts <= 0 || delta.tableNumber !in 1..12) continue
                val op = if (delta.operation == MathOperation.DIVISION) {
                    MathOperation.DIVISION
                } else {
                    MathOperation.MULTIPLICATION
                }
                val docId = TableMastery.docId(op, delta.tableNumber)
                val masteryRef = db.collection("users").document(uid)
                    .collection("tableMastery").document(docId)
                activePath = masteryRef.path

                val existing = masteryById[docId]
                val nextAttempts = (existing?.attempts ?: 0L) + delta.attempts.toLong()
                val nextCorrect = ((existing?.correct ?: 0L) + delta.correct.toLong())
                    .coerceAtMost(nextAttempts)
                val nextStars = GamificationEngine.calculateMasteryStars(nextCorrect, nextAttempts).toLong()

                if (existing == null) {
                    val createMastery = mapOf(
                        "userId" to uid,
                        "operationType" to op.name,
                        "tableNumber" to delta.tableNumber.toLong(),
                        "attempts" to nextAttempts,
                        "correct" to nextCorrect,
                        "stars" to nextStars,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    masteryRef.set(createMastery).await()
                    masteryById[docId] = TableMastery(
                        id = docId,
                        userId = uid,
                        operationType = op.name,
                        tableNumber = delta.tableNumber.toLong(),
                        attempts = nextAttempts,
                        correct = nextCorrect,
                        stars = nextStars
                    )
                } else {
                    val updateMastery = mapOf(
                        "attempts" to nextAttempts,
                        "correct" to nextCorrect,
                        "stars" to nextStars,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    masteryRef.update(updateMastery).await()
                    masteryById[docId] = existing.copy(
                        attempts = nextAttempts,
                        correct = nextCorrect,
                        stars = nextStars
                    )
                }
            }

            // 3. Compute projected UserProfile & evaluate new achievements
            val today = GamificationEngine.todayDateString()
            val (nextStreak, nextBestStreak) = GamificationEngine.computeUpdatedStreak(
                lastPracticeDate = currentProfile.lastPracticeDate,
                currentStreak = currentProfile.currentStreakDays,
                bestStreak = currentProfile.bestStreakDays,
                today = today
            )
            val isPerfect = safeCorrect == safeTotal
            val isTimedHighAcc = input.gameMode == QuizGameMode.TIMED &&
                ((safeCorrect * 100) / safeTotal) >= 80

            val previousLevel = GamificationEngine.computeLevel(currentProfile.totalXp).level
            val tentativeProfile = currentProfile.copy(
                userId = uid,
                totalXp = currentProfile.totalXp + baseXp.toLong(),
                totalStars = currentProfile.totalStars + starsEarned.toLong(),
                totalQuizzes = currentProfile.totalQuizzes + 1L,
                totalQuestionsAnswered = currentProfile.totalQuestionsAnswered + safeTotal.toLong(),
                totalCorrectAnswers = currentProfile.totalCorrectAnswers + safeCorrect.toLong(),
                multiplicationCorrect = currentProfile.multiplicationCorrect + input.multiplicationCorrect.coerceAtLeast(0).toLong(),
                divisionCorrect = currentProfile.divisionCorrect + input.divisionCorrect.coerceAtLeast(0).toLong(),
                perfectQuizzes = currentProfile.perfectQuizzes + if (isPerfect) 1L else 0L,
                timedHighAccuracyQuizzes = currentProfile.timedHighAccuracyQuizzes + if (isTimedHighAcc) 1L else 0L,
                bestCombo = maxOf(currentProfile.bestCombo, safeMaxCombo.toLong()),
                currentStreakDays = nextStreak,
                bestStreakDays = nextBestStreak,
                lastPracticeDate = today
            )

            val updatedMasteryList = masteryById.values.toList()
            val rewardEval = rewardManager.evaluateSessionRewards(
                previousStars = currentProfile.totalStars,
                correctAnswers = safeCorrect,
                totalQuestions = safeTotal,
                projectedProfile = tentativeProfile,
                updatedMasteries = updatedMasteryList,
                alreadyUnlockedBadgeIds = existingUnlockedIds
            )
            val newlyUnlocked = rewardEval.newlyUnlockedBadges
            val bonusBadgeXp = rewardEval.bonusBadgeXp

            // 4. Write newly unlocked achievements
            for (badge in newlyUnlocked) {
                val badgeRef = db.collection("users").document(uid)
                    .collection("achievements").document(badge.id)
                activePath = badgeRef.path
                val badgePayload = mapOf(
                    "userId" to uid,
                    "badgeId" to badge.id,
                    "xpReward" to badge.xpReward.toLong(),
                    "unlockedAt" to FieldValue.serverTimestamp()
                )
                badgeRef.set(badgePayload).await()
            }

            // 5. Update UserProfile cumulative stats (matches Action 2 allowlisted keys!)
            val finalTotalXp = tentativeProfile.totalXp + bonusBadgeXp.toLong()
            val userRef = db.collection("users").document(uid)
            activePath = userRef.path
            val profileUpdate = mapOf(
                "totalXp" to finalTotalXp,
                "totalStars" to tentativeProfile.totalStars,
                "totalQuizzes" to tentativeProfile.totalQuizzes,
                "totalQuestionsAnswered" to tentativeProfile.totalQuestionsAnswered,
                "totalCorrectAnswers" to tentativeProfile.totalCorrectAnswers,
                "multiplicationCorrect" to tentativeProfile.multiplicationCorrect,
                "divisionCorrect" to tentativeProfile.divisionCorrect,
                "perfectQuizzes" to tentativeProfile.perfectQuizzes,
                "timedHighAccuracyQuizzes" to tentativeProfile.timedHighAccuracyQuizzes,
                "bestCombo" to tentativeProfile.bestCombo,
                "currentStreakDays" to tentativeProfile.currentStreakDays,
                "bestStreakDays" to tentativeProfile.bestStreakDays,
                "lastPracticeDate" to tentativeProfile.lastPracticeDate,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userRef.update(profileUpdate).await()

            val newLevel = GamificationEngine.computeLevel(finalTotalXp).level
            Result.success(
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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, activePath)
            Result.failure(e)
        }
    }

    private fun sanitizeDisplayName(raw: String): String {
        val trimmed = raw.trim().take(40)
        return trimmed.ifBlank { "Juara Cilik" }
    }
}
