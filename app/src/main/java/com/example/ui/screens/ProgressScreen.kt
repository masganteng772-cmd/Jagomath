package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MathOperation
import com.example.data.model.QuizSessionRecord
import com.example.data.model.TableMastery
import com.example.data.model.UserProfile
import com.example.domain.GamificationEngine
import com.example.ui.theme.EqTheme
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProgressScreen(
    profile: UserProfile,
    recentSessions: List<QuizSessionRecord>,
    tableMasteries: List<TableMastery>,
    isGuestMode: Boolean = false,
    onPracticeTable: (MathOperation, Int) -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val eqColors = EqTheme.colors
    var selectedOp by remember { mutableStateOf(MathOperation.MULTIPLICATION) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember(profile.displayName) { mutableStateOf(profile.displayName) }

    val masteryById = remember(tableMasteries) { tableMasteries.associateBy { it.id } }
    val levelInfo = GamificationEngine.computeLevel(profile.totalXp)
    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID")) }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text(stringResource(R.string.progress_edit_name)) },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { if (it.length <= 40) editedName = it },
                    label = { Text(stringResource(R.string.progress_name_label)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditNameDialog = false
                        onUpdateDisplayName(editedName)
                    },
                    enabled = editedName.trim().isNotEmpty(),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("save_name_button")
                ) {
                    Text(stringResource(R.string.progress_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditNameDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.progress_cancel))
                }
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.progress_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
            }

            // Student Profile Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, eqColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Level ${levelInfo.level} • ${levelInfo.titleId} (${profile.totalXp} XP)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isGuestMode) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = eqColors.starContainer,
                                    border = BorderStroke(1.dp, eqColors.starAccent.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "Mode Tamu (Lokal)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = eqColors.onStarContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = {
                                editedName = profile.displayName
                                showEditNameDialog = true
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("edit_name_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.progress_edit_name),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 2x2 Summary Stats
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProgressMetricTile(
                            icon = Icons.Rounded.Quiz,
                            tint = eqColors.info,
                            container = eqColors.infoContainer,
                            value = "${profile.totalQuestionsAnswered}",
                            label = "Soal Dijawab",
                            modifier = Modifier.weight(1f)
                        )
                        ProgressMetricTile(
                            icon = Icons.Rounded.CheckCircle,
                            tint = eqColors.success,
                            container = eqColors.successContainer,
                            value = "${profile.accuracyPercent}%",
                            label = "Akurasi Total",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProgressMetricTile(
                            icon = Icons.Rounded.TrendingUp,
                            tint = eqColors.danger,
                            container = eqColors.dangerContainer,
                            value = "x${profile.bestCombo}",
                            label = "Kombo Terbaik",
                            modifier = Modifier.weight(1f)
                        )
                        ProgressMetricTile(
                            icon = Icons.Rounded.LocalFireDepartment,
                            tint = eqColors.starAccent,
                            container = eqColors.starContainer,
                            value = "${profile.bestStreakDays} Hari",
                            label = "Rekor Terpanjang",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Mastery Heatmap Grid (1..12) — Theme-aware light & dark AAA contrast!
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, eqColors.cardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.progress_mastery_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.semantics { heading() }
                        )
                        Text(
                            text = "Ketuk angka mana saja untuk langsung berlatih tabel tersebut!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val chipColors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            FilterChip(
                                selected = selectedOp == MathOperation.MULTIPLICATION,
                                onClick = { selectedOp = MathOperation.MULTIPLICATION },
                                label = { Text("Perkalian (×)") },
                                colors = chipColors,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("mastery_tab_mul")
                            )
                            FilterChip(
                                selected = selectedOp == MathOperation.DIVISION,
                                onClick = { selectedOp = MathOperation.DIVISION },
                                label = { Text("Pembagian (÷)") },
                                colors = chipColors,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("mastery_tab_div")
                            )
                        }

                        val symbol = selectedOp.symbol
                        val spokenOp = if (selectedOp == MathOperation.DIVISION) "pembagian" else "perkalian"
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..12).chunked(4).forEach { rowNums ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowNums.forEach { tableNum ->
                                        val docId = TableMastery.docId(selectedOp, tableNum)
                                        val mastery = masteryById[docId]
                                        val stars = mastery?.stars?.toInt() ?: 0
                                        val attempts = mastery?.attempts ?: 0L
                                        val acc = mastery?.accuracyPercent ?: 0

                                        val tileBg: Color
                                        val tileFg: Color
                                        val tileSubFg: Color
                                        val tileBorder: Color
                                        when {
                                            stars >= 3 -> {
                                                tileBg = eqColors.successContainer
                                                tileFg = eqColors.onSuccessContainer
                                                tileSubFg = eqColors.onSuccessContainer
                                                tileBorder = eqColors.success.copy(alpha = 0.45f)
                                            }
                                            stars == 2 -> {
                                                tileBg = eqColors.starContainer
                                                tileFg = eqColors.onStarContainer
                                                tileSubFg = eqColors.onStarContainer
                                                tileBorder = eqColors.starAccent.copy(alpha = 0.4f)
                                            }
                                            attempts > 0 -> {
                                                tileBg = MaterialTheme.colorScheme.primaryContainer
                                                tileFg = MaterialTheme.colorScheme.onPrimaryContainer
                                                tileSubFg = MaterialTheme.colorScheme.onPrimaryContainer
                                                tileBorder = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                            }
                                            else -> {
                                                tileBg = eqColors.subtleSurfaceTint
                                                tileFg = MaterialTheme.colorScheme.onSurface
                                                tileSubFg = MaterialTheme.colorScheme.onSurfaceVariant
                                                tileBorder = eqColors.cardBorder
                                            }
                                        }

                                        val spokenStatus = if (attempts > 0) {
                                            "$stars dari 3 bintang, akurasi $acc persen"
                                        } else {
                                            "belum dicoba"
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = tileBg,
                                            border = BorderStroke(1.dp, tileBorder),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 68.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .semantics(mergeDescendants = true) {
                                                    contentDescription = "Tabel $spokenOp $tableNum, $spokenStatus"
                                                }
                                                .clickable(
                                                    role = Role.Button,
                                                    onClickLabel = "Latih tabel $spokenOp $tableNum",
                                                    onClick = { onPracticeTable(selectedOp, tableNum) }
                                                )
                                                .testTag("mastery_cell_${selectedOp.name}_$tableNum")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = "$symbol$tableNum",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = tileFg
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                                    repeat(3) { sIdx ->
                                                        Icon(
                                                            imageVector = if (sIdx < stars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                                            contentDescription = null,
                                                            tint = if (sIdx < stars) eqColors.starAccent else tileSubFg.copy(alpha = 0.55f),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = if (attempts > 0) "$acc%" else "Baru",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = tileSubFg
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

            // Recent Quiz History
            item {
                Text(
                    text = stringResource(R.string.progress_history_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
            }

            if (recentSessions.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, eqColors.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = stringResource(R.string.progress_empty_history),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(recentSessions.take(15), key = { it.id.ifEmpty { "${it.createdAt?.seconds}_${it.xpEarned}" } }) { session ->
                    val dateLabel = session.createdAt?.toDate()?.let { dateFormatter.format(it) } ?: "Baru saja"
                    val tableLabel = if (session.tableNumber > 0) "Tabel ${session.operation.symbol}${session.tableNumber}" else "Acak (1–12)"
                    val (badgeBg, badgeFg) = when (session.operation) {
                        MathOperation.MULTIPLICATION -> eqColors.dangerContainer to eqColors.onDangerContainer
                        MathOperation.DIVISION -> eqColors.infoContainer to eqColors.onInfoContainer
                        MathOperation.MIXED -> eqColors.successContainer to eqColors.onSuccessContainer
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, eqColors.cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = "${session.operation.labelId}, $tableLabel, ${session.mode.labelId}, benar ${session.correctAnswers} dari ${session.totalQuestions} (${session.accuracyPercent} persen), ${session.starsEarned} bintang, tambah ${session.xpEarned} XP, $dateLabel"
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(badgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = session.operation.symbol,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = badgeFg
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${session.operation.labelId} • $tableLabel",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${session.mode.labelId} • $dateLabel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${session.correctAnswers}/${session.totalQuestions} (${session.accuracyPercent}%)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = eqColors.success
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    repeat(3) { sIdx ->
                                        Icon(
                                            imageVector = if (sIdx < session.starsEarned) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                            contentDescription = null,
                                            tint = if (sIdx < session.starsEarned) eqColors.starAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+${session.xpEarned} XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = eqColors.starAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sign Out / Exit Guest Action
            item {
                val actionColor = if (isGuestMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, actionColor),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        tint = actionColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGuestMode) {
                            stringResource(R.string.progress_exit_guest)
                        } else {
                            stringResource(R.string.progress_sign_out)
                        },
                        color = actionColor,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun ProgressMetricTile(
    icon: ImageVector,
    tint: Color,
    container: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val eqColors = EqTheme.colors
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, eqColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label: $value"
        }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(container),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
