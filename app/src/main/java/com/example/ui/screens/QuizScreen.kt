package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AnswerInputMode
import com.example.data.model.MathOperation
import com.example.domain.MathQuestion
import com.example.ui.ActiveQuizState
import com.example.ui.theme.EqTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizScreen(
    state: ActiveQuizState,
    onSubmitAnswer: (Int) -> Unit,
    onKeypadDigit: (String) -> Unit,
    onKeypadBackspace: () -> Unit,
    onKeypadClear: () -> Unit,
    onSubmitKeypad: () -> Unit,
    onNextQuestion: () -> Unit,
    onToggleHint: () -> Unit,
    onToggleInputMode: () -> Unit,
    onExitQuiz: () -> Unit
) {
    val eqColors = EqTheme.colors
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.quiz_exit_title)) },
            text = { Text(stringResource(R.string.quiz_exit_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onExitQuiz()
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("confirm_exit_quiz_button")
                ) {
                    Text(
                        text = stringResource(R.string.quiz_exit_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = { showExitDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.quiz_exit_cancel))
                }
            }
        )
    }

    val question = state.currentQuestion ?: return
    val total = state.questions.size.coerceAtLeast(1)
    val progress by animateFloatAsState(
        targetValue = (state.currentIndex + if (state.feedback != null) 1 else 0).toFloat() / total.toFloat(),
        animationSpec = spring(stiffness = 260f),
        label = "quiz_progress"
    )
    val spokenEquation = if (question.operation == MathOperation.DIVISION) {
        "${question.leftOperand} dibagi ${question.rightOperand} sama dengan berapa?"
    } else {
        "${question.leftOperand} dikali ${question.rightOperand} sama dengan berapa?"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top HUD Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = { showExitDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("exit_quiz_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Keluar kuis",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.quiz_question_counter, state.currentIndex + 1, total),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Benar: ${state.correctCount}",
                            style = MaterialTheme.typography.labelLarge,
                            color = eqColors.success
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .semantics {
                                contentDescription = "Progres kuis: soal ${state.currentIndex + 1} dari $total"
                                progressBarRangeInfo = ProgressBarRangeInfo(
                                    current = (state.currentIndex + 1).toFloat(),
                                    range = 0f..total.toFloat()
                                )
                            },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = eqColors.progressTrack,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            // Live Combo & Timer Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (state.currentCombo >= 2) eqColors.starContainer else eqColors.subtleSurfaceTint,
                    contentColor = if (state.currentCombo >= 2) eqColors.onStarContainer else MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(1.dp, eqColors.cardBorder),
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "Kombo saat ini ${state.currentCombo} berturut-turut"
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
                            tint = if (state.currentCombo >= 2) eqColors.danger else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Kombo x${state.currentCombo}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.remainingSeconds != null) {
                        val urgent = state.remainingSeconds <= 15
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (urgent) eqColors.dangerContainer else eqColors.infoContainer,
                            border = BorderStroke(
                                1.dp,
                                if (urgent) eqColors.danger.copy(alpha = 0.4f) else eqColors.info.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.semantics(mergeDescendants = true) {
                                contentDescription = "Sisa waktu ${state.remainingSeconds} detik"
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = if (urgent) eqColors.danger else eqColors.info,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = "${state.remainingSeconds} dtk",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (urgent) eqColors.onDangerContainer else eqColors.onInfoContainer
                                )
                            }
                        }
                    }

                    FilledTonalButton(
                        onClick = onToggleInputMode,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("toggle_input_mode_button")
                    ) {
                        Icon(
                            imageVector = if (state.config.inputMode == AnswerInputMode.MULTIPLE_CHOICE) {
                                Icons.Rounded.Dialpad
                            } else {
                                Icons.Rounded.GridView
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.config.inputMode == AnswerInputMode.MULTIPLE_CHOICE) "Ketik" else "Pilihan",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Equation Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, eqColors.cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = if (question.operation == MathOperation.DIVISION) {
                            eqColors.infoContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Text(
                            text = question.operation.labelId,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (question.operation == MathOperation.DIVISION) {
                                eqColors.onInfoContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }

                    Text(
                        text = question.expressionText,
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .semantics {
                                contentDescription = spokenEquation
                            }
                            .testTag("quiz_equation_text")
                    )

                    TextButton(
                        onClick = onToggleHint,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("quiz_hint_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = eqColors.starAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.showVisualHint) "Sembunyikan Bantuan" else stringResource(R.string.quiz_hint_button),
                            style = MaterialTheme.typography.labelLarge,
                            color = eqColors.starAccent
                        )
                    }

                    AnimatedVisibility(
                        visible = state.showVisualHint,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        VisualMathHintBox(question = question)
                    }
                }
            }

            // Answer Feedback Banner (Assertive Live Region for immediate TalkBack feedback)
            AnimatedVisibility(visible = state.feedback != null) {
                state.feedback?.let { fb ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (fb.isCorrect) eqColors.successContainer else eqColors.dangerContainer,
                        contentColor = if (fb.isCorrect) eqColors.onSuccessContainer else eqColors.onDangerContainer,
                        border = BorderStroke(
                            1.dp,
                            if (fb.isCorrect) eqColors.success.copy(alpha = 0.45f) else eqColors.danger.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                liveRegion = LiveRegionMode.Assertive
                            }
                            .testTag("answer_feedback_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (fb.isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = if (fb.isCorrect) eqColors.success else eqColors.danger,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = fb.message,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Answer Area: Multiple Choice OR Numeric Keypad
            if (state.config.inputMode == AnswerInputMode.MULTIPLE_CHOICE) {
                MultipleChoiceGrid(
                    choices = question.choices,
                    correctAnswer = question.correctAnswer,
                    feedback = state.feedback,
                    onSelect = onSubmitAnswer
                )
            } else {
                NumericKeypadPanel(
                    inputText = state.keypadInput,
                    feedback = state.feedback,
                    onDigit = onKeypadDigit,
                    onBackspace = onKeypadBackspace,
                    onClear = onKeypadClear,
                    onSubmit = onSubmitKeypad
                )
            }

            // Next / Finish CTA after answering
            AnimatedVisibility(visible = state.feedback != null) {
                Button(
                    onClick = onNextQuestion,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = eqColors.info,
                        contentColor = eqColors.onInfo
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("next_question_button")
                ) {
                    Text(
                        text = if (state.isLastQuestion) {
                            stringResource(R.string.quiz_finish_button)
                        } else {
                            stringResource(R.string.quiz_next_button)
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisualMathHintBox(question: MathQuestion) {
    val eqColors = EqTheme.colors
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = eqColors.starContainer,
        border = BorderStroke(1.dp, eqColors.starAccent.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                liveRegion = LiveRegionMode.Polite
            }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = question.hintTextId,
                style = MaterialTheme.typography.bodyMedium,
                color = eqColors.onStarContainer,
                textAlign = TextAlign.Center
            )

            if (question.operation == MathOperation.MULTIPLICATION) {
                val groups = minOf(question.leftOperand, question.rightOperand)
                val perGroup = maxOf(question.leftOperand, question.rightOperand)
                if (groups <= 5 && perGroup <= 6) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(groups) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, eqColors.cardBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(perGroup) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(eqColors.danger)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val boxes = question.rightOperand
                if (boxes <= 6 && question.correctAnswer <= 6) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(boxes) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, eqColors.info.copy(alpha = 0.45f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(question.correctAnswer) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(eqColors.info)
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
}

@Composable
private fun MultipleChoiceGrid(
    choices: List<Int>,
    correctAnswer: Int,
    feedback: com.example.ui.AnswerFeedback?,
    onSelect: (Int) -> Unit
) {
    val eqColors = EqTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        choices.chunked(2).forEachIndexed { rowIdx, rowChoices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowChoices.forEachIndexed { colIdx, value ->
                    val btnIdx = rowIdx * 2 + colIdx
                    val isCorrectOption = feedback != null && value == correctAnswer
                    val isWrongChosen = feedback != null && feedback.submittedAnswer == value && !feedback.isCorrect

                    val containerColor = when {
                        feedback == null -> MaterialTheme.colorScheme.surface
                        isCorrectOption -> eqColors.success
                        isWrongChosen -> eqColors.danger
                        else -> eqColors.subtleSurfaceTint
                    }
                    val contentColor = when {
                        feedback == null -> MaterialTheme.colorScheme.onSurface
                        isCorrectOption -> eqColors.onSuccess
                        isWrongChosen -> eqColors.onDanger
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val spokenState = when {
                        isCorrectOption -> "Pilihan $value, jawaban benar"
                        isWrongChosen -> "Pilihan $value, jawaban belum tepat"
                        else -> "Pilihan jawaban $value"
                    }

                    Button(
                        onClick = { onSelect(value) },
                        enabled = feedback == null,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = containerColor,
                            contentColor = contentColor,
                            disabledContainerColor = containerColor,
                            disabledContentColor = contentColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        border = if (feedback == null) {
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                        } else {
                            null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 72.dp)
                            .semantics { contentDescription = spokenState }
                            .testTag("choice_button_$btnIdx")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isCorrectOption) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else if (isWrongChosen) {
                                Icon(
                                    imageVector = Icons.Rounded.Cancel,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = value.toString(),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NumericKeypadPanel(
    inputText: String,
    feedback: com.example.ui.AnswerFeedback?,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 62.dp)
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = if (inputText.isEmpty()) {
                        "Kotak jawaban masih kosong, ketik jawabanmu menggunakan tombol angka"
                    } else {
                        "Jawaban yang diketik: $inputText"
                    }
                }
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = inputText.ifEmpty { "Ketik jawabanmu..." },
                    style = if (inputText.isEmpty()) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineLarge,
                    color = if (inputText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { digit ->
                    OutlinedButton(
                        onClick = { onDigit(digit) },
                        enabled = feedback == null,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 54.dp)
                    ) {
                        Text(
                            text = digit,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onClear,
                enabled = feedback == null && inputText.isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 54.dp)
            ) {
                Text(stringResource(R.string.quiz_clear), style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = { onDigit("0") },
                enabled = feedback == null,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 54.dp)
            ) {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            OutlinedButton(
                onClick = onBackspace,
                enabled = feedback == null && inputText.isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 54.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = "Hapus satu angka"
                )
            }
        }

        if (feedback == null) {
            Button(
                onClick = onSubmit,
                enabled = inputText.isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .testTag("submit_keypad_button")
            ) {
                Text(
                    text = stringResource(R.string.quiz_submit_answer),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
