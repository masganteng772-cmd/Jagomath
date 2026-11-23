package com.example.domain

import com.example.data.model.MathOperation
import com.example.data.model.TableMastery
import com.example.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class LevelInfo(
    val level: Int,
    val titleId: String,
    val currentLevelXp: Long,
    val nextLevelXp: Long,
    val progressFraction: Float
)

enum class BadgeCategory {
    MILESTONE,
    MULTIPLICATION,
    DIVISION,
    STREAK,
    MASTERY
}

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val category: BadgeCategory,
    val targetValue: Int,
    val progressProvider: (UserProfile, List<TableMastery>) -> Int
)

object GamificationEngine {

    private val levelThresholds = listOf(
        0L to "Pemula Ceria",
        150L to "Penjelajah Angka",
        400L to "Bintang Hitung",
        800L to "Pahlawan Perkalian",
        1350L to "Profesor Cilik",
        2100L to "Kapten Pembagian",
        3000L to "Master Matematika",
        4200L to "Legenda EQ-Math"
    )

    fun computeLevel(totalXp: Long): LevelInfo {
        val safeXp = totalXp.coerceAtLeast(0L)
        var currentIndex = 0
        for (i in levelThresholds.indices) {
            if (safeXp >= levelThresholds[i].first) {
                currentIndex = i
            }
        }
        val levelNumber = currentIndex + 1
        val title = levelThresholds[currentIndex].second
        val currentFloor = levelThresholds[currentIndex].first
        val nextCeiling = levelThresholds.getOrNull(currentIndex + 1)?.first ?: (currentFloor + 1500L)
        val xpIntoLevel = (safeXp - currentFloor).coerceAtLeast(0L)
        val span = (nextCeiling - currentFloor).coerceAtLeast(1L)
        val progress = (xpIntoLevel.toFloat() / span.toFloat()).coerceIn(0f, 1f)
        return LevelInfo(
            level = levelNumber,
            titleId = title,
            currentLevelXp = xpIntoLevel,
            nextLevelXp = span,
            progressFraction = progress
        )
    }

    fun calculateSessionStars(correct: Int, total: Int): Int {
        if (total <= 0) return 0
        val pct = (correct * 100) / total
        return when {
            pct >= 90 -> 3
            pct >= 70 -> 2
            pct >= 40 -> 1
            else -> 0
        }
    }

    fun calculateMasteryStars(correct: Long, attempts: Long): Int {
        if (attempts < 3L) return if (correct > 0L) 1 else 0
        val pct = (correct * 100L) / attempts
        return when {
            attempts >= 8L && pct >= 90L -> 3
            attempts >= 5L && pct >= 75L -> 2
            pct >= 50L -> 1
            else -> 0
        }
    }

    fun calculateSessionXp(
        correct: Int,
        total: Int,
        maxCombo: Int,
        isTimed: Boolean,
        isDaily: Boolean
    ): Int {
        val base = correct * 10
        val comboBonus = maxCombo * 3
        val perfectBonus = if (total > 0 && correct == total) 35 else 0
        val modeBonus = when {
            isDaily -> 30
            isTimed -> 20
            else -> 0
        }
        return (base + comboBonus + perfectBonus + modeBonus).coerceIn(0, 1500)
    }

    fun todayDateString(now: Date = Date()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(now)
    }

    fun computeUpdatedStreak(
        lastPracticeDate: String,
        currentStreak: Long,
        bestStreak: Long,
        today: String = todayDateString()
    ): Pair<Long, Long> {
        if (lastPracticeDate == today) {
            val safeCurrent = currentStreak.coerceAtLeast(1L)
            return safeCurrent to maxOf(bestStreak, safeCurrent)
        }
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        val nextStreak = if (lastPracticeDate == yesterday) {
            currentStreak + 1L
        } else {
            1L
        }
        return nextStreak to maxOf(bestStreak, nextStreak)
    }

    val allAchievements: List<AchievementDefinition> = listOf(
        AchievementDefinition(
            id = "langkah_pertama",
            title = "Langkah Pertama",
            description = "Selesaikan sesi kuis pertamamu",
            xpReward = 50,
            category = BadgeCategory.MILESTONE,
            targetValue = 1,
            progressProvider = { profile, _ -> profile.totalQuizzes.toInt() }
        ),
        AchievementDefinition(
            id = "jagoan_perkalian",
            title = "Jagoan Perkalian",
            description = "Jawab benar 25 soal perkalian",
            xpReward = 100,
            category = BadgeCategory.MULTIPLICATION,
            targetValue = 25,
            progressProvider = { profile, _ -> profile.multiplicationCorrect.toInt() }
        ),
        AchievementDefinition(
            id = "master_pembagian",
            title = "Master Pembagian",
            description = "Jawab benar 25 soal pembagian",
            xpReward = 100,
            category = BadgeCategory.DIVISION,
            targetValue = 25,
            progressProvider = { profile, _ -> profile.divisionCorrect.toInt() }
        ),
        AchievementDefinition(
            id = "nilai_sempurna",
            title = "Nilai Sempurna 100!",
            description = "Selesaikan 1 kuis tanpa salah satu pun",
            xpReward = 150,
            category = BadgeCategory.MILESTONE,
            targetValue = 1,
            progressProvider = { profile, _ -> profile.perfectQuizzes.toInt() }
        ),
        AchievementDefinition(
            id = "kombo_kilat",
            title = "Kombo Kilat",
            description = "Jawab benar 8 soal berturut-turut",
            xpReward = 120,
            category = BadgeCategory.STREAK,
            targetValue = 8,
            progressProvider = { profile, _ -> profile.bestCombo.toInt() }
        ),
        AchievementDefinition(
            id = "rajin_3_hari",
            title = "Semangat 3 Hari",
            description = "Capai rekor belajar 3 hari berturut-turut",
            xpReward = 150,
            category = BadgeCategory.STREAK,
            targetValue = 3,
            progressProvider = { profile, _ -> profile.bestStreakDays.toInt() }
        ),
        AchievementDefinition(
            id = "kolektor_bintang",
            title = "Kolektor Bintang",
            description = "Kumpulkan total 30 bintang dari kuis",
            xpReward = 120,
            category = BadgeCategory.MILESTONE,
            targetValue = 30,
            progressProvider = { profile, _ -> profile.totalStars.toInt() }
        ),
        AchievementDefinition(
            id = "pejuang_100_soal",
            title = "Pejuang 100 Soal",
            description = "Jawab benar total 100 soal matematika",
            xpReward = 200,
            category = BadgeCategory.MILESTONE,
            targetValue = 100,
            progressProvider = { profile, _ -> profile.totalCorrectAnswers.toInt() }
        ),
        AchievementDefinition(
            id = "penakluk_5_tabel",
            title = "Penakluk 5 Tabel",
            description = "Raih minimal 2 bintang di 5 tabel berbeda",
            xpReward = 200,
            category = BadgeCategory.MASTERY,
            targetValue = 5,
            progressProvider = { _, masteries ->
                masteries.count { it.stars >= 2L }
            }
        ),
        AchievementDefinition(
            id = "si_cepat_tangkas",
            title = "Si Cepat Tangkas",
            description = "Selesaikan kuis Kilat Waktu dengan akurasi ≥ 80%",
            xpReward = 150,
            category = BadgeCategory.MASTERY,
            targetValue = 1,
            progressProvider = { profile, _ -> profile.timedHighAccuracyQuizzes.toInt() }
        ),
        AchievementDefinition(
            id = "anak_rajin",
            title = "Anak Rajin",
            description = "Selesaikan 10 sesi kuis latihan",
            xpReward = 200,
            category = BadgeCategory.STREAK,
            targetValue = 10,
            progressProvider = { profile, _ -> profile.totalQuizzes.toInt() }
        ),
        AchievementDefinition(
            id = "profesor_cilik",
            title = "Profesor Cilik",
            description = "Capai Level 5 di EQ-Math",
            xpReward = 300,
            category = BadgeCategory.MASTERY,
            targetValue = 5,
            progressProvider = { profile, _ -> computeLevel(profile.totalXp).level }
        )
    )

    fun evaluateNewAchievements(
        updatedProfile: UserProfile,
        updatedMasteries: List<TableMastery>,
        alreadyUnlockedIds: Set<String>
    ): List<AchievementDefinition> {
        return allAchievements.filter { def ->
            def.id !in alreadyUnlockedIds &&
                def.progressProvider(updatedProfile, updatedMasteries) >= def.targetValue
        }
    }

    fun getPraiseMessage(combo: Int): String = when {
        combo >= 8 -> "Luar biasa! Kombo super hebat!"
        combo >= 5 -> "Keren sekali! Kamu sangat cepat!"
        combo >= 3 -> "Mantap! Pertahankan terus!"
        else -> listOf(
            "Tepat sekali!",
            "Hebat, kamu pintar!",
            "Bagus sekali!",
            "Benar! Lanjutkan!"
        ).random()
    }

    fun getEncouragementMessage(a: Int, b: Int, op: MathOperation, answer: Int): String {
        val sym = if (op == MathOperation.DIVISION) "÷" else "×"
        return "Belum tepat, yuk ingat: $a $sym $b = $answer"
    }
}
