package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MathRepository
import com.example.data.QuizCompletionInput
import com.example.data.QuizCompletionOutcome
import com.example.data.TableAttemptDelta
import com.example.data.model.AnswerInputMode
import com.example.data.model.MathOperation
import com.example.data.model.QuizGameMode
import com.example.data.model.QuizSessionRecord
import com.example.data.model.TableMastery
import com.example.data.model.UnlockedAchievement
import com.example.data.model.UserProfile
import com.example.domain.GamificationEngine
import com.example.domain.MathQuestion
import com.example.domain.QuizConfig
import com.example.domain.QuizGenerator
import com.example.domain.RewardDashboardState
import com.example.domain.RewardManager
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val profile: UserProfile,
        val recentSessions: List<QuizSessionRecord>,
        val tableMasteries: List<TableMastery>,
        val unlockedAchievements: List<UnlockedAchievement>,
        val rewardDashboard: RewardDashboardState
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

enum class MainTab {
    HOME,
    PRACTICE,
    ACHIEVEMENTS,
    PROGRESS
}

sealed interface AppScreen {
    data class Tabs(val activeTab: MainTab = MainTab.HOME) : AppScreen
    data object Quiz : AppScreen
    data object QuizResult : AppScreen
    data class TableGuide(val initialOperation: MathOperation = MathOperation.MULTIPLICATION) : AppScreen
}

data class AnswerFeedback(
    val submittedAnswer: Int,
    val isCorrect: Boolean,
    val message: String
)

data class ActiveQuizState(
    val config: QuizConfig = QuizConfig(),
    val questions: List<MathQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val multiplicationCorrect: Int = 0,
    val divisionCorrect: Int = 0,
    val currentCombo: Int = 0,
    val maxCombo: Int = 0,
    val keypadInput: String = "",
    val feedback: AnswerFeedback? = null,
    val showVisualHint: Boolean = false,
    val elapsedSeconds: Int = 0,
    val remainingSeconds: Int? = null,
    val tableResults: Map<Pair<MathOperation, Int>, Pair<Int, Int>> = emptyMap()
) {
    val currentQuestion: MathQuestion?
        get() = questions.getOrNull(currentIndex)

    val isLastQuestion: Boolean
        get() = questions.isNotEmpty() && currentIndex >= questions.lastIndex
}

data class QuizResultState(
    val config: QuizConfig = QuizConfig(),
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val maxCombo: Int = 0,
    val durationSeconds: Int = 0,
    val saving: Boolean = false,
    val outcome: QuizCompletionOutcome? = null,
    val saveError: String? = null
)

class MathAppViewModel(
    private val repository: MathRepository,
    private val currentUserId: String,
    private val defaultUserName: String,
    private val rewardManager: RewardManager = RewardManager()
) : ViewModel() {

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Tabs(MainTab.HOME))
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _practiceConfig = MutableStateFlow(QuizConfig())
    val practiceConfig: StateFlow<QuizConfig> = _practiceConfig.asStateFlow()

    private val _quizState = MutableStateFlow(ActiveQuizState())
    val quizState: StateFlow<ActiveQuizState> = _quizState.asStateFlow()

    private val _resultState = MutableStateFlow(QuizResultState())
    val resultState: StateFlow<QuizResultState> = _resultState.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private var timerJob: Job? = null

    val dashboardState: StateFlow<DashboardUiState> = combine(
        repository.observeUserProfile(currentUserId),
        repository.observeQuizSessions(currentUserId),
        repository.observeTableMastery(currentUserId),
        repository.observeAchievements(currentUserId)
    ) { profile, sessions, masteries, achievements ->
        val safeProfile = profile ?: UserProfile(
            id = currentUserId,
            userId = currentUserId,
            displayName = defaultUserName.ifBlank { "Juara Cilik" }
        )
        DashboardUiState.Success(
            profile = safeProfile,
            recentSessions = sessions,
            tableMasteries = masteries,
            unlockedAchievements = achievements,
            rewardDashboard = rewardManager.buildDashboardState(
                profile = safeProfile,
                unlockedAchievements = achievements
            )
        ) as DashboardUiState
    }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing dashboard state", rootError)
            emit(
                DashboardUiState.Error(
                    rootError.message ?: "Gagal memuat progres belajarmu."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = DashboardUiState.Loading
        )

    init {
        ensureProfileInitialized()
    }

    fun ensureProfileInitialized() {
        viewModelScope.launch {
            val result = repository.ensureUserProfile(defaultUserName)
            result.onFailure { err ->
                Log.w(TAG, "Failed to initialize user profile", err)
                _statusBanner.value = "Gagal menyinkronkan profil awal. Periksa koneksi internetmu."
            }
        }
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun selectTab(tab: MainTab) {
        stopTimer()
        _currentScreen.value = AppScreen.Tabs(tab)
    }

    fun openTableGuide(operation: MathOperation = MathOperation.MULTIPLICATION) {
        _currentScreen.value = AppScreen.TableGuide(operation)
    }

    fun backToHome(tab: MainTab = MainTab.HOME) {
        stopTimer()
        _currentScreen.value = AppScreen.Tabs(tab)
    }

    fun updatePracticeConfig(transform: (QuizConfig) -> QuizConfig) {
        _practiceConfig.update(transform)
    }

    fun startDailyChallenge() {
        val dailyConfig = QuizConfig(
            operation = MathOperation.MIXED,
            gameMode = QuizGameMode.DAILY,
            focusTable = 0,
            questionCount = 10,
            inputMode = AnswerInputMode.MULTIPLE_CHOICE
        )
        startQuiz(dailyConfig)
    }

    fun startQuickMode(operation: MathOperation, focusTable: Int = 0) {
        val config = QuizConfig(
            operation = operation,
            gameMode = QuizGameMode.PRACTICE,
            focusTable = focusTable,
            questionCount = 10,
            inputMode = AnswerInputMode.MULTIPLE_CHOICE
        )
        _practiceConfig.value = config
        startQuiz(config)
    }

    fun startConfiguredQuiz() {
        startQuiz(_practiceConfig.value)
    }

    fun startQuiz(config: QuizConfig) {
        stopTimer()
        val questions = QuizGenerator.generateQuestions(config)
        val initialCountdown = if (config.gameMode == QuizGameMode.TIMED) {
            config.questionCount * 10
        } else {
            null
        }
        _quizState.value = ActiveQuizState(
            config = config,
            questions = questions,
            currentIndex = 0,
            remainingSeconds = initialCountdown
        )
        _currentScreen.value = AppScreen.Quiz
        startTimer(isTimed = config.gameMode == QuizGameMode.TIMED)
    }

    private fun startTimer(isTimed: Boolean) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                var timeExpired = false
                _quizState.update { state ->
                    val nextElapsed = state.elapsedSeconds + 1
                    val nextRemaining = state.remainingSeconds?.let { rem ->
                        val dec = (rem - 1).coerceAtLeast(0)
                        if (dec == 0) timeExpired = true
                        dec
                    }
                    state.copy(
                        elapsedSeconds = nextElapsed,
                        remainingSeconds = nextRemaining
                    )
                }
                if (isTimed && timeExpired) {
                    finishQuiz()
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun toggleVisualHint() {
        _quizState.update { it.copy(showVisualHint = !it.showVisualHint) }
    }

    fun toggleQuizInputMode() {
        _quizState.update { state ->
            val nextMode = if (state.config.inputMode == AnswerInputMode.MULTIPLE_CHOICE) {
                AnswerInputMode.KEYPAD
            } else {
                AnswerInputMode.MULTIPLE_CHOICE
            }
            state.copy(
                config = state.config.copy(inputMode = nextMode),
                keypadInput = ""
            )
        }
    }

    fun onKeypadDigit(digit: String) {
        _quizState.update { state ->
            if (state.feedback != null || state.keypadInput.length >= 3) return@update state
            state.copy(keypadInput = state.keypadInput + digit)
        }
    }

    fun onKeypadBackspace() {
        _quizState.update { state ->
            if (state.feedback != null) return@update state
            state.copy(keypadInput = state.keypadInput.dropLast(1))
        }
    }

    fun onKeypadClear() {
        _quizState.update { state ->
            if (state.feedback != null) return@update state
            state.copy(keypadInput = "")
        }
    }

    fun submitKeypadAnswer() {
        val value = _quizState.value.keypadInput.toIntOrNull() ?: return
        submitAnswer(value)
    }

    fun submitAnswer(answer: Int) {
        val state = _quizState.value
        val question = state.currentQuestion ?: return
        if (state.feedback != null) return

        val isCorrect = answer == question.correctAnswer
        val nextCombo = if (isCorrect) state.currentCombo + 1 else 0
        val nextMaxCombo = maxOf(state.maxCombo, nextCombo)
        val message = if (isCorrect) {
            GamificationEngine.getPraiseMessage(nextCombo)
        } else {
            GamificationEngine.getEncouragementMessage(
                question.leftOperand,
                question.rightOperand,
                question.operation,
                question.correctAnswer
            )
        }

        val key = question.operation to question.focusTableNumber
        val currentPair = state.tableResults[key] ?: (0 to 0)
        val updatedTableResults = state.tableResults.toMutableMap().apply {
            put(key, (currentPair.first + 1) to (currentPair.second + if (isCorrect) 1 else 0))
        }

        _quizState.value = state.copy(
            correctCount = state.correctCount + if (isCorrect) 1 else 0,
            multiplicationCorrect = state.multiplicationCorrect +
                if (isCorrect && question.operation == MathOperation.MULTIPLICATION) 1 else 0,
            divisionCorrect = state.divisionCorrect +
                if (isCorrect && question.operation == MathOperation.DIVISION) 1 else 0,
            currentCombo = nextCombo,
            maxCombo = nextMaxCombo,
            feedback = AnswerFeedback(
                submittedAnswer = answer,
                isCorrect = isCorrect,
                message = message
            ),
            tableResults = updatedTableResults
        )
    }

    fun nextQuestionOrFinish() {
        val state = _quizState.value
        if (state.isLastQuestion) {
            finishQuiz()
        } else {
            _quizState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                feedback = null,
                keypadInput = "",
                showVisualHint = false
            )
        }
    }

    private fun finishQuiz() {
        stopTimer()
        val state = _quizState.value
        val total = state.questions.size.coerceAtLeast(1)

        _resultState.value = QuizResultState(
            config = state.config,
            totalQuestions = total,
            correctAnswers = state.correctCount,
            maxCombo = state.maxCombo,
            durationSeconds = state.elapsedSeconds,
            saving = true,
            outcome = null,
            saveError = null
        )
        _currentScreen.value = AppScreen.QuizResult

        viewModelScope.launch {
            val currentSuccess = dashboardState.value as? DashboardUiState.Success
            val profile = currentSuccess?.profile ?: UserProfile(
                id = currentUserId,
                userId = currentUserId,
                displayName = defaultUserName.ifBlank { "Juara Cilik" }
            )
            // Ensure profile doc exists before updating
            repository.ensureUserProfile(profile.displayName)

            val masteries = currentSuccess?.tableMasteries ?: emptyList()
            val unlockedIds = currentSuccess?.unlockedAchievements?.map { it.badgeId }?.toSet()
                ?: emptySet()

            val deltas = state.tableResults.map { (key, counts) ->
                TableAttemptDelta(
                    operation = key.first,
                    tableNumber = key.second,
                    attempts = counts.first,
                    correct = counts.second
                )
            }

            val input = QuizCompletionInput(
                operation = state.config.operation,
                gameMode = state.config.gameMode,
                focusTable = state.config.focusTable,
                totalQuestions = total,
                correctAnswers = state.correctCount,
                multiplicationCorrect = state.multiplicationCorrect,
                divisionCorrect = state.divisionCorrect,
                maxCombo = state.maxCombo,
                durationSeconds = state.elapsedSeconds,
                tableDeltas = deltas
            )

            val result = repository.recordQuizCompletion(
                input = input,
                currentProfile = profile,
                existingMasteries = masteries,
                existingUnlockedIds = unlockedIds
            )

            result.onSuccess { outcome ->
                _resultState.update {
                    it.copy(saving = false, outcome = outcome, saveError = null)
                }
            }.onFailure { err ->
                Log.e(TAG, "Failed saving quiz completion", err)
                val fallbackStars = GamificationEngine.calculateSessionStars(state.correctCount, total)
                val fallbackXp = GamificationEngine.calculateSessionXp(
                    state.correctCount,
                    total,
                    state.maxCombo,
                    state.config.gameMode == QuizGameMode.TIMED,
                    state.config.gameMode == QuizGameMode.DAILY
                )
                val level = GamificationEngine.computeLevel(profile.totalXp).level
                _resultState.update {
                    it.copy(
                        saving = false,
                        outcome = QuizCompletionOutcome(
                            starsEarned = fallbackStars,
                            baseXpEarned = fallbackXp,
                            bonusBadgeXp = 0,
                            totalXpAfter = profile.totalXp,
                            previousLevel = level,
                            newLevel = level,
                            newStreakDays = profile.currentStreakDays,
                            newlyUnlockedBadges = emptyList()
                        ),
                        saveError = "Skor belum tersimpan ke awan. Periksa koneksi internetmu ya."
                    )
                }
            }
        }
    }

    fun updateStudentName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || trimmed.length > 40) {
            _statusBanner.value = "Nama harus berisi 1 sampai 40 karakter ya!"
            return
        }
        viewModelScope.launch {
            val result = repository.updateDisplayName(trimmed)
            result.onSuccess {
                _statusBanner.value = "Nama berhasil diperbarui menjadi $trimmed!"
            }.onFailure {
                _statusBanner.value = "Gagal menyimpan nama. Coba lagi ya!"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    private companion object {
        const val TAG = "MathAppVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
