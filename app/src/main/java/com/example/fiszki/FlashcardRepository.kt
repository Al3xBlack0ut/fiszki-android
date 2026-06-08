package com.example.fiszki

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FlashcardRepository(private val dao: FlashcardDao) {

    val allSets: Flow<List<FlashcardSet>> = dao.getSetsWithCardsFlow().map { entityList ->
        entityList.map { setWithCards ->
            FlashcardSet(
                id = setWithCards.set.id,
                name = setWithCards.set.name,
                cards = setWithCards.cards.sortedBy { it.kolejnosc }.map { cardEntity ->
                    Flashcard(cardEntity.pojecie, cardEntity.definicja, cardEntity.wrongCount)
                }
            )
        }
    }

    suspend fun saveSet(setId: String, name: String, cards: List<Flashcard>) {
        val setEntity = FlashcardSetEntity(id = setId, name = name)
        val cardEntities = cards.mapIndexed { index, card ->
            FlashcardEntity(
                setId = setId,
                pojecie = card.pojecie,
                definicja = card.definicja,
                kolejnosc = index,
                wrongCount = card.wrongCount
            )
        }
        dao.saveSetWithCards(setEntity, cardEntities)
    }

    suspend fun deleteSet(setId: String) {
        dao.deleteSetComplete(setId)
    }

    // --- NOWE FUNKCJE STATYSTYK ---
    suspend fun logSession(setId: String, correctCount: Int, totalCount: Int) {
        dao.insertSession(StudySessionEntity(setId = setId, correctCount = correctCount, totalCount = totalCount, timestamp = System.currentTimeMillis()))
    }

    suspend fun logMistake(setId: String, pojecie: String) {
        dao.incrementWrongCount(setId, pojecie)
    }

    fun getSessionsForSet(setId: String): Flow<List<StudySessionEntity>> = dao.getSessionsForSetFlow(setId)

    fun getHardestCardsForSet(setId: String): Flow<List<FlashcardEntity>> = dao.getHardestCardsForSetFlow(setId)
}