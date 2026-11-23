package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class MathOperation(val labelId: String, val symbol: String) {
    MULTIPLICATION("Perkalian", "×"),
    DIVISION("Pembagian", "÷"),
    MIXED("Campuran", "× ÷");

    companion object {
        fun fromRaw(raw: String): MathOperation =
            entries.firstOrNull { it.name == raw } ?: MULTIPLICATION
    }
}

enum class QuizGameMode(val labelId: String) {
    PRACTICE("Santai"),
    TIMED("Kilat Waktu"),
    DAILY("Misi Harian");

    companion object {
        fun fromRaw(raw: String): QuizGameMode =
            entries.firstOrNull { it.name == raw } ?: PRACTICE
    }
}

enum class AnswerInputMode {
    MULTIPLE_CHOICE,
    KEYPAD
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Juara Cilik",
    val totalXp: Long = 0L,
    val totalStars: Long = 0L,
    val totalQuizzes: Long = 0L,
    val totalQuestionsAnswered: Long = 0L,
    val totalCorrectAnswers: Long = 0L,
    val multiplicationCorrect: Long = 0L,
    val divisionCorrect: Long = 0L,
    val perfectQuizzes: Long = 0L,
    val timedHighAccuracyQuizzes: Long = 0L,
    val bestCombo: Long = 0L,
    val currentStreakDays: Long = 0L,
    val bestStreakDays: Long = 0L,
    val lastPracticeDate: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val accuracyPercent: Int
        get() = if (totalQuestionsAnswered <= 0L) 0
        else ((totalCorrectAnswers * 100L) / totalQuestionsAnswered).toInt().coerceIn(0, 100)
}

@Keep
@IgnoreExtraProperties
data class QuizSessionRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val operationType: String = MathOperation.MULTIPLICATION.name,
    val gameMode: String = QuizGameMode.PRACTICE.name,
    val tableNumber: Long = 0L,
    val totalQuestions: Long = 10L,
    val correctAnswers: Long = 0L,
    val starsEarned: Long = 0L,
    val xpEarned: Long = 0L,
    val maxCombo: Long = 0L,
    val durationSeconds: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val operation: MathOperation
        get() = MathOperation.fromRaw(operationType)

    val mode: QuizGameMode
        get() = QuizGameMode.fromRaw(gameMode)

    val accuracyPercent: Int
        get() = if (totalQuestions <= 0L) 0
        else ((correctAnswers * 100L) / totalQuestions).toInt().coerceIn(0, 100)
}

@Keep
@IgnoreExtraProperties
data class TableMastery(
    @DocumentId val id: String = "",
    val userId: String = "",
    val operationType: String = MathOperation.MULTIPLICATION.name,
    val tableNumber: Long = 1L,
    val attempts: Long = 0L,
    val correct: Long = 0L,
    val stars: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val operation: MathOperation
        get() = MathOperation.fromRaw(operationType)

    val accuracyPercent: Int
        get() = if (attempts <= 0L) 0
        else ((correct * 100L) / attempts).toInt().coerceIn(0, 100)

    companion object {
        fun docId(operation: MathOperation, tableNumber: Int): String {
            val prefix = if (operation == MathOperation.DIVISION) "div" else "mul"
            return "${prefix}_${tableNumber.coerceIn(1, 12)}"
        }
    }
}

@Keep
@IgnoreExtraProperties
data class UnlockedAchievement(
    @DocumentId val id: String = "",
    val userId: String = "",
    val badgeId: String = "",
    val xpReward: Long = 0L,
    @ServerTimestamp val unlockedAt: Timestamp? = null
)
