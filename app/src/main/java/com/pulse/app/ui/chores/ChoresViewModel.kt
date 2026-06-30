package com.pulse.app.ui.chores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulse.app.data.model.Chore
import com.pulse.app.data.repository.ChoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class ChoreWithWeight(
    val chore: Chore,
    val normalizedWeight: Float,
    val overdueRatio: Float,
    val daysOverdue: Int
)

data class ChoresUiState(
    val chores: List<ChoreWithWeight> = emptyList(),
    val rolledChore: ChoreWithWeight? = null,
    val isSpinning: Boolean = false,
    val deletedChore: Chore? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ChoresViewModel @Inject constructor(
    private val repository: ChoreRepository
) : ViewModel() {

    private val _rolledChore = MutableStateFlow<ChoreWithWeight?>(null)
    private val _isSpinning = MutableStateFlow(false)
    private val _deletedChore = MutableStateFlow<Chore?>(null)

    val uiState: StateFlow<ChoresUiState> = combine(
        repository.getActiveChores(),
        _rolledChore,
        _isSpinning,
        _deletedChore
    ) { chores, rolled, spinning, deleted ->
        ChoresUiState(
            chores = computeWeights(chores),
            rolledChore = rolled,
            isSpinning = spinning,
            deletedChore = deleted,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChoresUiState())

    private fun computeWeights(chores: List<Chore>): List<ChoreWithWeight> {
        if (chores.isEmpty()) return emptyList()
        val nowMs = System.currentTimeMillis()
        val msPerDay = 86_400_000L

        fun daysSince(chore: Chore): Float =
            if (chore.lastCompletedAt == null)
                chore.expectedFrequencyDays * 10f
            else
                ((nowMs - chore.lastCompletedAt) / msPerDay).toFloat().coerceAtLeast(0f)

        val rawWeights = chores.map { chore ->
            (daysSince(chore) / chore.expectedFrequencyDays).coerceAtLeast(0.1f)
        }
        val totalRaw = rawWeights.sum().takeIf { it > 0f } ?: 1f

        return chores.mapIndexed { i, chore ->
            val raw = rawWeights[i]
            val daysOverdue = if (chore.lastCompletedAt == null) -1
            else ((nowMs - chore.lastCompletedAt) / msPerDay).toInt() - chore.expectedFrequencyDays
            ChoreWithWeight(
                chore = chore,
                normalizedWeight = raw / totalRaw,
                overdueRatio = raw,
                daysOverdue = daysOverdue
            )
        }
    }

    fun roll() {
        val chores = uiState.value.chores
        if (chores.isEmpty() || _isSpinning.value) return
        val totalWeight = chores.sumOf { it.normalizedWeight.toDouble() }.toFloat()
        val pick = Random.nextFloat() * totalWeight
        var cumulative = 0f
        var selected = chores.last()
        for (cw in chores) {
            cumulative += cw.normalizedWeight
            if (pick <= cumulative) {
                selected = cw
                break
            }
        }
        _isSpinning.value = true
        _rolledChore.value = selected
    }

    fun onSpinFinished() {
        _isSpinning.value = false
    }

    fun dismissResult() {
        _rolledChore.value = null
    }

    fun markComplete(id: Int) {
        viewModelScope.launch {
            repository.markCompleted(id)
            _rolledChore.value = null
        }
    }

    fun addChore(name: String, emoji: String, frequencyDays: Int) {
        viewModelScope.launch {
            repository.addChore(Chore(name = name, emoji = emoji, expectedFrequencyDays = frequencyDays))
        }
    }

    fun deleteChore(chore: Chore) {
        viewModelScope.launch {
            _deletedChore.value = chore
            repository.deleteChore(chore.id)
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            _deletedChore.value?.let { repository.addChore(it.copy(id = 0)) }
            _deletedChore.value = null
        }
    }

    fun clearDeletedChore() {
        _deletedChore.value = null
    }
}
