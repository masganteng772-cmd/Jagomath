package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AnswerInputMode
import com.example.data.model.MathOperation
import com.example.data.model.QuizGameMode
import com.example.data.model.TableMastery
import com.example.domain.QuizConfig
import com.example.ui.theme.EqTheme

@Composable
fun PracticeHubScreen(
    config: QuizConfig,
    tableMasteries: List<TableMastery>,
    onUpdateConfig: ((QuizConfig) -> QuizConfig) -> Unit,
    onStartQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eqColors = EqTheme.colors
    val masteryLookup = tableMasteries.associateBy { it.id }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.practice_title),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        text = "Atur kuis sesuai tabel dan kecepatan belajarmu!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. Operation Selector
            item {
                SectionCard(title = stringResource(R.string.practice_select_op)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OperationChoiceTile(
                            label = "Perkalian",
                            symbol = "×",
                            selected = config.operation == MathOperation.MULTIPLICATION,
                            onClick = { onUpdateConfig { it.copy(operation = MathOperation.MULTIPLICATION) } },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("op_mul_chip")
                        )
                        OperationChoiceTile(
                            label = "Pembagian",
                            symbol = "÷",
                            selected = config.operation == MathOperation.DIVISION,
                            onClick = { onUpdateConfig { it.copy(operation = MathOperation.DIVISION) } },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("op_div_chip")
                        )
                        OperationChoiceTile(
                            label = "Campuran",
                            symbol = "× ÷",
                            selected = config.operation == MathOperation.MIXED,
                            onClick = { onUpdateConfig { it.copy(operation = MathOperation.MIXED) } },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("op_mixed_chip")
                        )
                    }
                }
            }

            // 2. Game Mode & Answer Input Mode
            item {
                SectionCard(title = stringResource(R.string.practice_select_mode)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModeCardOption(
                            icon = Icons.Rounded.SelfImprovement,
                            title = stringResource(R.string.practice_mode_relaxed),
                            subtitle = stringResource(R.string.practice_mode_relaxed_desc),
                            selected = config.gameMode == QuizGameMode.PRACTICE,
                            onClick = { onUpdateConfig { it.copy(gameMode = QuizGameMode.PRACTICE) } },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_relaxed_card")
                        )
                        ModeCardOption(
                            icon = Icons.Rounded.Timer,
                            title = stringResource(R.string.practice_mode_timed),
                            subtitle = stringResource(R.string.practice_mode_timed_desc),
                            selected = config.gameMode == QuizGameMode.TIMED,
                            onClick = { onUpdateConfig { it.copy(gameMode = QuizGameMode.TIMED) } },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_timed_card")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = config.inputMode == AnswerInputMode.MULTIPLE_CHOICE,
                            onClick = { onUpdateConfig { it.copy(inputMode = AnswerInputMode.MULTIPLE_CHOICE) } },
                            label = {
                                Text(
                                    text = stringResource(R.string.practice_input_choice),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.GridView,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = eqColors.infoContainer,
                                selectedLabelColor = eqColors.onInfoContainer,
                                selectedLeadingIconColor = eqColors.info
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("input_choice_chip")
                        )
                        FilterChip(
                            selected = config.inputMode == AnswerInputMode.KEYPAD,
                            onClick = { onUpdateConfig { it.copy(inputMode = AnswerInputMode.KEYPAD) } },
                            label = {
                                Text(
                                    text = stringResource(R.string.practice_input_keypad),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Dialpad,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = eqColors.infoContainer,
                                selectedLabelColor = eqColors.onInfoContainer,
                                selectedLeadingIconColor = eqColors.info
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("input_keypad_chip")
                        )
                    }
                }
            }

            // 3. Question Count
            item {
                SectionCard(title = stringResource(R.string.practice_select_count)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(5, 10, 15).forEach { count ->
                            val selected = config.questionCount == count
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else eqColors.subtleSurfaceTint,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                border = if (selected) null else BorderStroke(1.dp, eqColors.cardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .selectable(
                                        selected = selected,
                                        role = Role.RadioButton,
                                        onClick = { onUpdateConfig { it.copy(questionCount = count) } }
                                    )
                                    .testTag("count_${count}_chip")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$count Soal",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Focus Table Selector (Semua + 1..12 with mastery stars!)
            item {
                SectionCard(title = stringResource(R.string.practice_select_table)) {
                    val allSelected = config.focusTable == 0
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (allSelected) MaterialTheme.colorScheme.primaryContainer else eqColors.subtleSurfaceTint,
                        border = BorderStroke(
                            width = if (allSelected) 2.dp else 1.dp,
                            color = if (allSelected) MaterialTheme.colorScheme.primary else eqColors.cardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .selectable(
                                selected = allSelected,
                                role = Role.RadioButton,
                                onClick = { onUpdateConfig { it.copy(focusTable = 0) } }
                            )
                            .testTag("table_all_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.practice_all_tables),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (allSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val symbol = if (config.operation == MathOperation.DIVISION) "÷" else "×"
                    val spokenOp = if (config.operation == MathOperation.DIVISION) "pembagian" else "perkalian"
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..12).chunked(4).forEach { rowNumbers ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowNumbers.forEach { num ->
                                    val selected = config.focusTable == num
                                    val stars = when (config.operation) {
                                        MathOperation.MULTIPLICATION ->
                                            masteryLookup[TableMastery.docId(MathOperation.MULTIPLICATION, num)]?.stars?.toInt() ?: 0
                                        MathOperation.DIVISION ->
                                            masteryLookup[TableMastery.docId(MathOperation.DIVISION, num)]?.stars?.toInt() ?: 0
                                        MathOperation.MIXED -> {
                                            val m = masteryLookup[TableMastery.docId(MathOperation.MULTIPLICATION, num)]?.stars?.toInt() ?: 0
                                            val d = masteryLookup[TableMastery.docId(MathOperation.DIVISION, num)]?.stars?.toInt() ?: 0
                                            minOf(m, d)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (selected) MaterialTheme.colorScheme.primary else eqColors.subtleSurfaceTint,
                                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        border = if (selected) null else BorderStroke(1.dp, eqColors.cardBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 64.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .semantics(mergeDescendants = true) {
                                                contentDescription = "Tabel $spokenOp $num, $stars dari 3 bintang"
                                            }
                                            .selectable(
                                                selected = selected,
                                                role = Role.RadioButton,
                                                onClick = { onUpdateConfig { it.copy(focusTable = num) } }
                                            )
                                            .testTag("table_${num}_tile")
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 10.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$symbol$num",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                                repeat(3) { starIdx ->
                                                    Icon(
                                                        imageVector = if (starIdx < stars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                                        contentDescription = null,
                                                        tint = if (starIdx < stars) {
                                                            if (selected) MaterialTheme.colorScheme.onPrimary else eqColors.starAccent
                                                        } else {
                                                            if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.55f)
                                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                                        },
                                                        modifier = Modifier.size(14.dp)
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

            // 5. Primary Start CTA
            item {
                Button(
                    onClick = onStartQuiz,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("start_quiz_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.practice_start_cta),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    val eqColors = EqTheme.colors
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, eqColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun OperationChoiceTile(
    label: String,
    symbol: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eqColors = EqTheme.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else eqColors.subtleSurfaceTint,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else eqColors.cardBorder
        ),
        modifier = modifier
            .heightIn(min = 82.dp)
            .clip(RoundedCornerShape(14.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Operasi $label"
            }
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.headlineMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModeCardOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eqColors = EqTheme.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) eqColors.starContainer else eqColors.subtleSurfaceTint,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) eqColors.starAccent else eqColors.cardBorder
        ),
        modifier = modifier
            .heightIn(min = 88.dp)
            .clip(RoundedCornerShape(14.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Mode $title: $subtitle"
            }
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) eqColors.starAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) eqColors.onStarContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) eqColors.onStarContainer.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
