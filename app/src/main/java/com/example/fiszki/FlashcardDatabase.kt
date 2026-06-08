package com.example.fiszki

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "flashcard_sets")
data class FlashcardSetEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val setId: String,
    val pojecie: String,
    val definicja: String,
    val kolejnosc: Int = 0,
    val wrongCount: Int = 0 // <- Licznik błędów dla danej fiszki
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val setId: String,
    val correctCount: Int,
    val totalCount: Int,
    val timestamp: Long
)

data class FlashcardSetWithCards(
    @Embedded val set: FlashcardSetEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "setId"
    )
    val cards: List<FlashcardEntity>
)

@Dao
interface FlashcardDao {

    @Transaction
    @Query("SELECT * FROM flashcard_sets")
    fun getSetsWithCardsFlow(): Flow<List<FlashcardSetWithCards>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: FlashcardSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashcardEntity>)

    @Query("DELETE FROM flashcards WHERE setId = :setId")
    suspend fun deleteCardsForSet(setId: String)

    @Query("DELETE FROM flashcard_sets WHERE id = :setId")
    suspend fun deleteSet(setId: String)

    @Transaction
    suspend fun saveSetWithCards(set: FlashcardSetEntity, cards: List<FlashcardEntity>) {
        insertSet(set)
        deleteCardsForSet(set.id)
        insertCards(cards)
    }

    @Transaction
    suspend fun deleteSetComplete(setId: String) {
        deleteCardsForSet(setId)
        deleteSet(setId)
    }

    // --- NOWE METODY STATYSTYK ---
    @Insert
    suspend fun insertSession(session: StudySessionEntity)

    @Query("UPDATE flashcards SET wrongCount = wrongCount + 1 WHERE setId = :setId AND pojecie = :pojecie")
    suspend fun incrementWrongCount(setId: String, pojecie: String)

    @Query("SELECT * FROM study_sessions WHERE setId = :setId ORDER BY timestamp DESC")
    fun getSessionsForSetFlow(setId: String): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM flashcards WHERE setId = :setId ORDER BY wrongCount DESC LIMIT 3")
    fun getHardestCardsForSetFlow(setId: String): Flow<List<FlashcardEntity>>
}

@Database(entities = [FlashcardSetEntity::class, FlashcardEntity::class, StudySessionEntity::class], version = 3, exportSchema = false)
abstract class FlashcardDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: FlashcardDatabase? = null

        fun getInstance(context: Context): FlashcardDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FlashcardDatabase::class.java,
                    "fiszki_database"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}