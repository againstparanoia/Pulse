package com.pulse.app.ui.chores

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pulse.app.ui.components.SwipeToDelete

@Composable
fun ChoresScreen(
    viewModel: ChoresViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.deletedChore) {
        val deleted = state.deletedChore ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "${deleted.emoji} ${deleted.name} deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short
        )
        when (result) {
            SnackbarResult.ActionPerformed -> viewModel.undoDelete()
            SnackbarResult.Dismissed -> viewModel.clearDeletedChore()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add chore")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chores",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            Spacer(Modifier.height(8.dp))

            NeglectWheel(
                chores = state.chores,
                isSpinning = state.isSpinning,
                targetChore = state.rolledChore,
                onSpinFinished = viewModel::onSpinFinished
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = viewModel::roll,
                enabled = !state.isSpinning && state.chores.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text(if (state.isSpinning) "Spinning…" else "Spin the Wheel")
            }

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = state.rolledChore != null && !state.isSpinning,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                state.rolledChore?.let { cw ->
                    RollResultCard(
                        choreWithWeight = cw,
                        onMarkDone = { viewModel.markComplete(cw.chore.id) },
                        onRollAgain = viewModel::roll,
                        onSkip = viewModel::dismissResult,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.chores.isEmpty() && !state.isLoading) {
                EmptyChoresState(modifier = Modifier.padding(32.dp))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.chores.forEach { cw ->
                        SwipeToDelete(onDelete = { viewModel.deleteChore(cw.chore) }) {
                            ChoreCard(choreWithWeight = cw)
                        }
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddSheet) {
        AddChoreSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, emoji, freq -> viewModel.addChore(name, emoji, freq) }
        )
    }
}

@Composable
private fun RollResultCard(
    choreWithWeight: ChoreWithWeight,
    onMarkDone: () -> Unit,
    onRollAgain: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = choreWithWeight.chore.emoji, fontSize = 48.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = choreWithWeight.chore.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "The wheel has spoken!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onMarkDone, modifier = Modifier.fillMaxWidth()) {
                Text("Mark Done")
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onRollAgain, modifier = Modifier.weight(1f)) {
                    Text("Roll Again")
                }
                TextButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                    Text("Skip")
                }
            }
        }
    }
}

@Composable
private fun EmptyChoresState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🧹", fontSize = 48.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "No chores yet!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Tap + to add your first chore",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
