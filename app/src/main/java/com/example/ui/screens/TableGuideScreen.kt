package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.MathOperation
import com.example.ui.theme.EqTheme

@Composable
fun TableGuideScreen(
    initialOperation: MathOperation,
    onBack: () -> Unit,
    onPracticeTable: (MathOperation, Int) -> Unit
) {
    val eqColors = EqTheme.colors
    BackHandler { onBack() }

    var operation by remember {
        mutableStateOf(
            if (initialOperation == MathOperation.DIVISION) MathOperation.DIVISION else MathOperation.MULTIPLICATION
        )
    }
    var selectedTable by remember { mutableIntStateOf(2) }

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
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("table_guide_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.table_guide_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() }
                )
            }

            // Operation Chips (>= 48dp touch targets)
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipColors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                FilterChip(
                    selected = operation == MathOperation.MULTIPLICATION,
                    onClick = { operation = MathOperation.MULTIPLICATION },
                    label = { Text("Perkalian (×)") },
                    colors = chipColors,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("guide_mul_chip")
                )
                FilterChip(
                    selected = operation == MathOperation.DIVISION,
                    onClick = { operation = MathOperation.DIVISION },
                    label = { Text("Pembagian (÷)") },
                    colors = chipColors,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("guide_div_chip")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Number Selector Row (1..12)
            val spokenOp = if (operation == MathOperation.DIVISION) "pembagian" else "perkalian"
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items((1..12).toList()) { num ->
                    val selected = selectedTable == num
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        border = if (selected) null else BorderStroke(1.dp, eqColors.cardBorder),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .semantics {
                                contentDescription = "Tabel $spokenOp $num"
                            }
                            .selectable(
                                selected = selected,
                                role = Role.Tab,
                                onClick = { selectedTable = num }
                            )
                            .testTag("guide_table_$num")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$num",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Equation Table List (Natural Indonesian TalkBack announcements)
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items((1..12).toList()) { factor ->
                    val left = if (operation == MathOperation.MULTIPLICATION) selectedTable else selectedTable * factor
                    val right = if (operation == MathOperation.MULTIPLICATION) factor else selectedTable
                    val result = if (operation == MathOperation.MULTIPLICATION) selectedTable * factor else factor
                    val spokenWord = if (operation == MathOperation.DIVISION) "dibagi" else "dikali"
                    val bg = if (factor % 2 == 0) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        eqColors.subtleSurfaceTint
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = bg),
                        border = BorderStroke(1.dp, eqColors.cardBorder.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics(mergeDescendants = true) {
                                contentDescription = "$left $spokenWord $right sama dengan $result"
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$left ${operation.symbol} $right",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "= $result",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Practice This Table CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = { onPracticeTable(operation, selectedTable) },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .testTag("guide_practice_this_button")
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stringResource(R.string.table_guide_practice_this)} (${operation.symbol}$selectedTable)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
