package com.example.fiszki;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FlashcardDao_Impl implements FlashcardDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FlashcardSetEntity> __insertionAdapterOfFlashcardSetEntity;

  private final EntityInsertionAdapter<FlashcardEntity> __insertionAdapterOfFlashcardEntity;

  private final EntityInsertionAdapter<StudySessionEntity> __insertionAdapterOfStudySessionEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCardsForSet;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSet;

  private final SharedSQLiteStatement __preparedStmtOfIncrementWrongCount;

  public FlashcardDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFlashcardSetEntity = new EntityInsertionAdapter<FlashcardSetEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `flashcard_sets` (`id`,`name`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FlashcardSetEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
      }
    };
    this.__insertionAdapterOfFlashcardEntity = new EntityInsertionAdapter<FlashcardEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `flashcards` (`id`,`setId`,`pojecie`,`definicja`,`kolejnosc`,`wrongCount`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FlashcardEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSetId());
        statement.bindString(3, entity.getPojecie());
        statement.bindString(4, entity.getDefinicja());
        statement.bindLong(5, entity.getKolejnosc());
        statement.bindLong(6, entity.getWrongCount());
      }
    };
    this.__insertionAdapterOfStudySessionEntity = new EntityInsertionAdapter<StudySessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `study_sessions` (`id`,`setId`,`correctCount`,`totalCount`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StudySessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSetId());
        statement.bindLong(3, entity.getCorrectCount());
        statement.bindLong(4, entity.getTotalCount());
        statement.bindLong(5, entity.getTimestamp());
      }
    };
    this.__preparedStmtOfDeleteCardsForSet = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM flashcards WHERE setId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteSet = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM flashcard_sets WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfIncrementWrongCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE flashcards SET wrongCount = wrongCount + 1 WHERE setId = ? AND pojecie = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSet(final FlashcardSetEntity set,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFlashcardSetEntity.insert(set);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCards(final List<FlashcardEntity> cards,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFlashcardEntity.insert(cards);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertSession(final StudySessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStudySessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object saveSetWithCards(final FlashcardSetEntity set, final List<FlashcardEntity> cards,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> FlashcardDao.DefaultImpls.saveSetWithCards(FlashcardDao_Impl.this, set, cards, __cont), $completion);
  }

  @Override
  public Object deleteSetComplete(final String setId,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> FlashcardDao.DefaultImpls.deleteSetComplete(FlashcardDao_Impl.this, setId, __cont), $completion);
  }

  @Override
  public Object deleteCardsForSet(final String setId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCardsForSet.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, setId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCardsForSet.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSet(final String setId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSet.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, setId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSet.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object incrementWrongCount(final String setId, final String pojecie,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementWrongCount.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, setId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, pojecie);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfIncrementWrongCount.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FlashcardSetWithCards>> getSetsWithCardsFlow() {
    final String _sql = "SELECT * FROM flashcard_sets";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"flashcards",
        "flashcard_sets"}, new Callable<List<FlashcardSetWithCards>>() {
      @Override
      @NonNull
      public List<FlashcardSetWithCards> call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
            final ArrayMap<String, ArrayList<FlashcardEntity>> _collectionCards = new ArrayMap<String, ArrayList<FlashcardEntity>>();
            while (_cursor.moveToNext()) {
              final String _tmpKey;
              _tmpKey = _cursor.getString(_cursorIndexOfId);
              if (!_collectionCards.containsKey(_tmpKey)) {
                _collectionCards.put(_tmpKey, new ArrayList<FlashcardEntity>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipflashcardsAscomExampleFiszkiFlashcardEntity(_collectionCards);
            final List<FlashcardSetWithCards> _result = new ArrayList<FlashcardSetWithCards>(_cursor.getCount());
            while (_cursor.moveToNext()) {
              final FlashcardSetWithCards _item;
              final FlashcardSetEntity _tmpSet;
              final String _tmpId;
              _tmpId = _cursor.getString(_cursorIndexOfId);
              final String _tmpName;
              _tmpName = _cursor.getString(_cursorIndexOfName);
              _tmpSet = new FlashcardSetEntity(_tmpId,_tmpName);
              final ArrayList<FlashcardEntity> _tmpCardsCollection;
              final String _tmpKey_1;
              _tmpKey_1 = _cursor.getString(_cursorIndexOfId);
              _tmpCardsCollection = _collectionCards.get(_tmpKey_1);
              _item = new FlashcardSetWithCards(_tmpSet,_tmpCardsCollection);
              _result.add(_item);
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<StudySessionEntity>> getSessionsForSetFlow(final String setId) {
    final String _sql = "SELECT * FROM study_sessions WHERE setId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, setId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"study_sessions"}, new Callable<List<StudySessionEntity>>() {
      @Override
      @NonNull
      public List<StudySessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSetId = CursorUtil.getColumnIndexOrThrow(_cursor, "setId");
          final int _cursorIndexOfCorrectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "correctCount");
          final int _cursorIndexOfTotalCount = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCount");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<StudySessionEntity> _result = new ArrayList<StudySessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StudySessionEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpSetId;
            _tmpSetId = _cursor.getString(_cursorIndexOfSetId);
            final int _tmpCorrectCount;
            _tmpCorrectCount = _cursor.getInt(_cursorIndexOfCorrectCount);
            final int _tmpTotalCount;
            _tmpTotalCount = _cursor.getInt(_cursorIndexOfTotalCount);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new StudySessionEntity(_tmpId,_tmpSetId,_tmpCorrectCount,_tmpTotalCount,_tmpTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<FlashcardEntity>> getHardestCardsForSetFlow(final String setId) {
    final String _sql = "SELECT * FROM flashcards WHERE setId = ? ORDER BY wrongCount DESC LIMIT 3";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, setId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"flashcards"}, new Callable<List<FlashcardEntity>>() {
      @Override
      @NonNull
      public List<FlashcardEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSetId = CursorUtil.getColumnIndexOrThrow(_cursor, "setId");
          final int _cursorIndexOfPojecie = CursorUtil.getColumnIndexOrThrow(_cursor, "pojecie");
          final int _cursorIndexOfDefinicja = CursorUtil.getColumnIndexOrThrow(_cursor, "definicja");
          final int _cursorIndexOfKolejnosc = CursorUtil.getColumnIndexOrThrow(_cursor, "kolejnosc");
          final int _cursorIndexOfWrongCount = CursorUtil.getColumnIndexOrThrow(_cursor, "wrongCount");
          final List<FlashcardEntity> _result = new ArrayList<FlashcardEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FlashcardEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpSetId;
            _tmpSetId = _cursor.getString(_cursorIndexOfSetId);
            final String _tmpPojecie;
            _tmpPojecie = _cursor.getString(_cursorIndexOfPojecie);
            final String _tmpDefinicja;
            _tmpDefinicja = _cursor.getString(_cursorIndexOfDefinicja);
            final int _tmpKolejnosc;
            _tmpKolejnosc = _cursor.getInt(_cursorIndexOfKolejnosc);
            final int _tmpWrongCount;
            _tmpWrongCount = _cursor.getInt(_cursorIndexOfWrongCount);
            _item = new FlashcardEntity(_tmpId,_tmpSetId,_tmpPojecie,_tmpDefinicja,_tmpKolejnosc,_tmpWrongCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipflashcardsAscomExampleFiszkiFlashcardEntity(
      @NonNull final ArrayMap<String, ArrayList<FlashcardEntity>> _map) {
    final Set<String> __mapKeySet = _map.keySet();
    if (__mapKeySet.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchArrayMap(_map, true, (map) -> {
        __fetchRelationshipflashcardsAscomExampleFiszkiFlashcardEntity(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `id`,`setId`,`pojecie`,`definicja`,`kolejnosc`,`wrongCount` FROM `flashcards` WHERE `setId` IN (");
    final int _inputSize = __mapKeySet.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : __mapKeySet) {
      _stmt.bindString(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      final int _itemKeyIndex = CursorUtil.getColumnIndex(_cursor, "setId");
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfSetId = 1;
      final int _cursorIndexOfPojecie = 2;
      final int _cursorIndexOfDefinicja = 3;
      final int _cursorIndexOfKolejnosc = 4;
      final int _cursorIndexOfWrongCount = 5;
      while (_cursor.moveToNext()) {
        final String _tmpKey;
        _tmpKey = _cursor.getString(_itemKeyIndex);
        final ArrayList<FlashcardEntity> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final FlashcardEntity _item_1;
          final int _tmpId;
          _tmpId = _cursor.getInt(_cursorIndexOfId);
          final String _tmpSetId;
          _tmpSetId = _cursor.getString(_cursorIndexOfSetId);
          final String _tmpPojecie;
          _tmpPojecie = _cursor.getString(_cursorIndexOfPojecie);
          final String _tmpDefinicja;
          _tmpDefinicja = _cursor.getString(_cursorIndexOfDefinicja);
          final int _tmpKolejnosc;
          _tmpKolejnosc = _cursor.getInt(_cursorIndexOfKolejnosc);
          final int _tmpWrongCount;
          _tmpWrongCount = _cursor.getInt(_cursorIndexOfWrongCount);
          _item_1 = new FlashcardEntity(_tmpId,_tmpSetId,_tmpPojecie,_tmpDefinicja,_tmpKolejnosc,_tmpWrongCount);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
