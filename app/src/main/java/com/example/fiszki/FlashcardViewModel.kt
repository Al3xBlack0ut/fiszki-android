package com.example.fiszki

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

class FlashcardViewModel(private val repository: FlashcardRepository) : ViewModel() {

    val flashcardSets: StateFlow<List<FlashcardSet>> = repository.allSets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun saveSet(setId: String, name: String, cards: List<Flashcard>) {
        viewModelScope.launch {
            repository.saveSet(setId, name, cards)
        }
    }

    fun deleteSet(setId: String) {
        viewModelScope.launch {
            repository.deleteSet(setId)
        }
    }

    // --- NOWE FUNKCJE OBSŁUGI STATYSTYK ---
    fun logSession(setId: String, correctCount: Int, totalCount: Int) {
        viewModelScope.launch {
            repository.logSession(setId, correctCount, totalCount)
        }
    }

    fun logMistake(setId: String, pojecie: String) {
        viewModelScope.launch {
            repository.logMistake(setId, pojecie)
        }
    }

    fun getSessionsForSet(setId: String): Flow<List<StudySessionEntity>> = repository.getSessionsForSet(setId)

    fun getHardestCardsForSet(setId: String): Flow<List<FlashcardEntity>> = repository.getHardestCardsForSet(setId)

    class Factory(private val repository: FlashcardRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FlashcardViewModel::class.java)) {
                return FlashcardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}