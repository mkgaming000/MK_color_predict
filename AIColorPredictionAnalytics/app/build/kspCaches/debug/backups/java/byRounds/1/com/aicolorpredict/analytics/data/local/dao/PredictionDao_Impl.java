package com.aicolorpredict.analytics.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.aicolorpredict.analytics.data.local.entity.PredictionEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PredictionDao_Impl implements PredictionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PredictionEntity> __insertionAdapterOfPredictionEntity;

  private final SharedSQLiteStatement __preparedStmtOfResolve;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public PredictionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPredictionEntity = new EntityInsertionAdapter<PredictionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `predictions` (`id`,`roundId`,`epochMs`,`modelName`,`topPick`,`topProbability`,`confidence`,`reason`,`numberProbabilities`,`colorProbabilities`,`actualOutcome`,`correct`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PredictionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getRoundId());
        statement.bindLong(3, entity.getEpochMs());
        statement.bindString(4, entity.getModelName());
        statement.bindLong(5, entity.getTopPick());
        statement.bindDouble(6, entity.getTopProbability());
        statement.bindDouble(7, entity.getConfidence());
        statement.bindString(8, entity.getReason());
        statement.bindString(9, entity.getNumberProbabilities());
        statement.bindString(10, entity.getColorProbabilities());
        if (entity.getActualOutcome() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getActualOutcome());
        }
        if (entity.getCorrect() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getCorrect());
        }
      }
    };
    this.__preparedStmtOfResolve = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE predictions SET actualOutcome = ?, correct = CASE WHEN topPick = ? THEN 1 ELSE 0 END WHERE roundId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM predictions";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final PredictionEntity prediction,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPredictionEntity.insertAndReturnId(prediction);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<PredictionEntity> predictions,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPredictionEntity.insert(predictions);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object resolve(final long roundId, final int actual,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfResolve.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, actual);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, actual);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, roundId);
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
          __preparedStmtOfResolve.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
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
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getByRound(final long roundId,
      final Continuation<? super List<PredictionEntity>> $completion) {
    final String _sql = "SELECT * FROM predictions WHERE roundId = ? ORDER BY modelName";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, roundId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PredictionEntity>>() {
      @Override
      @NonNull
      public List<PredictionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRoundId = CursorUtil.getColumnIndexOrThrow(_cursor, "roundId");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfTopPick = CursorUtil.getColumnIndexOrThrow(_cursor, "topPick");
          final int _cursorIndexOfTopProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "topProbability");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfNumberProbabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "numberProbabilities");
          final int _cursorIndexOfColorProbabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "colorProbabilities");
          final int _cursorIndexOfActualOutcome = CursorUtil.getColumnIndexOrThrow(_cursor, "actualOutcome");
          final int _cursorIndexOfCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "correct");
          final List<PredictionEntity> _result = new ArrayList<PredictionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PredictionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRoundId;
            _tmpRoundId = _cursor.getLong(_cursorIndexOfRoundId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final String _tmpModelName;
            _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            final int _tmpTopPick;
            _tmpTopPick = _cursor.getInt(_cursorIndexOfTopPick);
            final double _tmpTopProbability;
            _tmpTopProbability = _cursor.getDouble(_cursorIndexOfTopProbability);
            final double _tmpConfidence;
            _tmpConfidence = _cursor.getDouble(_cursorIndexOfConfidence);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpNumberProbabilities;
            _tmpNumberProbabilities = _cursor.getString(_cursorIndexOfNumberProbabilities);
            final String _tmpColorProbabilities;
            _tmpColorProbabilities = _cursor.getString(_cursorIndexOfColorProbabilities);
            final Integer _tmpActualOutcome;
            if (_cursor.isNull(_cursorIndexOfActualOutcome)) {
              _tmpActualOutcome = null;
            } else {
              _tmpActualOutcome = _cursor.getInt(_cursorIndexOfActualOutcome);
            }
            final Integer _tmpCorrect;
            if (_cursor.isNull(_cursorIndexOfCorrect)) {
              _tmpCorrect = null;
            } else {
              _tmpCorrect = _cursor.getInt(_cursorIndexOfCorrect);
            }
            _item = new PredictionEntity(_tmpId,_tmpRoundId,_tmpEpochMs,_tmpModelName,_tmpTopPick,_tmpTopProbability,_tmpConfidence,_tmpReason,_tmpNumberProbabilities,_tmpColorProbabilities,_tmpActualOutcome,_tmpCorrect);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecentByModel(final String model, final int limit,
      final Continuation<? super List<PredictionEntity>> $completion) {
    final String _sql = "SELECT * FROM predictions WHERE modelName = ? ORDER BY epochMs DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, model);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PredictionEntity>>() {
      @Override
      @NonNull
      public List<PredictionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRoundId = CursorUtil.getColumnIndexOrThrow(_cursor, "roundId");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfTopPick = CursorUtil.getColumnIndexOrThrow(_cursor, "topPick");
          final int _cursorIndexOfTopProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "topProbability");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfNumberProbabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "numberProbabilities");
          final int _cursorIndexOfColorProbabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "colorProbabilities");
          final int _cursorIndexOfActualOutcome = CursorUtil.getColumnIndexOrThrow(_cursor, "actualOutcome");
          final int _cursorIndexOfCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "correct");
          final List<PredictionEntity> _result = new ArrayList<PredictionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PredictionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRoundId;
            _tmpRoundId = _cursor.getLong(_cursorIndexOfRoundId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final String _tmpModelName;
            _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            final int _tmpTopPick;
            _tmpTopPick = _cursor.getInt(_cursorIndexOfTopPick);
            final double _tmpTopProbability;
            _tmpTopProbability = _cursor.getDouble(_cursorIndexOfTopProbability);
            final double _tmpConfidence;
            _tmpConfidence = _cursor.getDouble(_cursorIndexOfConfidence);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpNumberProbabilities;
            _tmpNumberProbabilities = _cursor.getString(_cursorIndexOfNumberProbabilities);
            final String _tmpColorProbabilities;
            _tmpColorProbabilities = _cursor.getString(_cursorIndexOfColorProbabilities);
            final Integer _tmpActualOutcome;
            if (_cursor.isNull(_cursorIndexOfActualOutcome)) {
              _tmpActualOutcome = null;
            } else {
              _tmpActualOutcome = _cursor.getInt(_cursorIndexOfActualOutcome);
            }
            final Integer _tmpCorrect;
            if (_cursor.isNull(_cursorIndexOfCorrect)) {
              _tmpCorrect = null;
            } else {
              _tmpCorrect = _cursor.getInt(_cursorIndexOfCorrect);
            }
            _item = new PredictionEntity(_tmpId,_tmpRoundId,_tmpEpochMs,_tmpModelName,_tmpTopPick,_tmpTopProbability,_tmpConfidence,_tmpReason,_tmpNumberProbabilities,_tmpColorProbabilities,_tmpActualOutcome,_tmpCorrect);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecentResolvedByModel(final String model, final int limit,
      final Continuation<? super List<PredictionEntity>> $completion) {
    final String _sql = "SELECT * FROM predictions WHERE modelName = ? AND correct IS NOT NULL ORDER BY epochMs DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, model);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PredictionEntity>>() {
      @Override
      @NonNull
      public List<PredictionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRoundId = CursorUtil.getColumnIndexOrThrow(_cursor, "roundId");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfTopPick = CursorUtil.getColumnIndexOrThrow(_cursor, "topPick");
          final int _cursorIndexOfTopProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "topProbability");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfNumberProbabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "numberProbabilities");
          final int _cursorIndexOfColorProbabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "colorProbabilities");
          final int _cursorIndexOfActualOutcome = CursorUtil.getColumnIndexOrThrow(_cursor, "actualOutcome");
          final int _cursorIndexOfCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "correct");
          final List<PredictionEntity> _result = new ArrayList<PredictionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PredictionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRoundId;
            _tmpRoundId = _cursor.getLong(_cursorIndexOfRoundId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final String _tmpModelName;
            _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            final int _tmpTopPick;
            _tmpTopPick = _cursor.getInt(_cursorIndexOfTopPick);
            final double _tmpTopProbability;
            _tmpTopProbability = _cursor.getDouble(_cursorIndexOfTopProbability);
            final double _tmpConfidence;
            _tmpConfidence = _cursor.getDouble(_cursorIndexOfConfidence);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpNumberProbabilities;
            _tmpNumberProbabilities = _cursor.getString(_cursorIndexOfNumberProbabilities);
            final String _tmpColorProbabilities;
            _tmpColorProbabilities = _cursor.getString(_cursorIndexOfColorProbabilities);
            final Integer _tmpActualOutcome;
            if (_cursor.isNull(_cursorIndexOfActualOutcome)) {
              _tmpActualOutcome = null;
            } else {
              _tmpActualOutcome = _cursor.getInt(_cursorIndexOfActualOutcome);
            }
            final Integer _tmpCorrect;
            if (_cursor.isNull(_cursorIndexOfCorrect)) {
              _tmpCorrect = null;
            } else {
              _tmpCorrect = _cursor.getInt(_cursorIndexOfCorrect);
            }
            _item = new PredictionEntity(_tmpId,_tmpRoundId,_tmpEpochMs,_tmpModelName,_tmpTopPick,_tmpTopProbability,_tmpConfidence,_tmpReason,_tmpNumberProbabilities,_tmpColorProbabilities,_tmpActualOutcome,_tmpCorrect);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PredictionEntity>> observeByRound(final long roundId) {
    final String _sql = "SELECT * FROM predictions WHERE roundId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, roundId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"predictions"}, new Callable<List<PredictionEntity>>() {
      @Override
      @NonNull
      public List<PredictionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRoundId = CursorUtil.getColumnIndexOrThrow(_cursor, "roundId");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfTopPick = CursorUtil.getColumnIndexOrThrow(_cursor, "topPick");
          final int _cursorIndexOfTopProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "topProbability");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfNumberProbabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "numberProbabilities");
          final int _cursorIndexOfColorProbabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "colorProbabilities");
          final int _cursorIndexOfActualOutcome = CursorUtil.getColumnIndexOrThrow(_cursor, "actualOutcome");
          final int _cursorIndexOfCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "correct");
          final List<PredictionEntity> _result = new ArrayList<PredictionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PredictionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRoundId;
            _tmpRoundId = _cursor.getLong(_cursorIndexOfRoundId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final String _tmpModelName;
            _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            final int _tmpTopPick;
            _tmpTopPick = _cursor.getInt(_cursorIndexOfTopPick);
            final double _tmpTopProbability;
            _tmpTopProbability = _cursor.getDouble(_cursorIndexOfTopProbability);
            final double _tmpConfidence;
            _tmpConfidence = _cursor.getDouble(_cursorIndexOfConfidence);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpNumberProbabilities;
            _tmpNumberProbabilities = _cursor.getString(_cursorIndexOfNumberProbabilities);
            final String _tmpColorProbabilities;
            _tmpColorProbabilities = _cursor.getString(_cursorIndexOfColorProbabilities);
            final Integer _tmpActualOutcome;
            if (_cursor.isNull(_cursorIndexOfActualOutcome)) {
              _tmpActualOutcome = null;
            } else {
              _tmpActualOutcome = _cursor.getInt(_cursorIndexOfActualOutcome);
            }
            final Integer _tmpCorrect;
            if (_cursor.isNull(_cursorIndexOfCorrect)) {
              _tmpCorrect = null;
            } else {
              _tmpCorrect = _cursor.getInt(_cursorIndexOfCorrect);
            }
            _item = new PredictionEntity(_tmpId,_tmpRoundId,_tmpEpochMs,_tmpModelName,_tmpTopPick,_tmpTopProbability,_tmpConfidence,_tmpReason,_tmpNumberProbabilities,_tmpColorProbabilities,_tmpActualOutcome,_tmpCorrect);
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
  public Object getUnresolved(final Continuation<? super List<PredictionEntity>> $completion) {
    final String _sql = "SELECT * FROM predictions WHERE correct IS NULL ORDER BY epochMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PredictionEntity>>() {
      @Override
      @NonNull
      public List<PredictionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRoundId = CursorUtil.getColumnIndexOrThrow(_cursor, "roundId");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfTopPick = CursorUtil.getColumnIndexOrThrow(_cursor, "topPick");
          final int _cursorIndexOfTopProbability = CursorUtil.getColumnIndexOrThrow(_cursor, "topProbability");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfNumberProbabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "numberProbabilities");
          final int _cursorIndexOfColorProbabilities = CursorUtil.getColumnIndexOrThrow(_cursor, "colorProbabilities");
          final int _cursorIndexOfActualOutcome = CursorUtil.getColumnIndexOrThrow(_cursor, "actualOutcome");
          final int _cursorIndexOfCorrect = CursorUtil.getColumnIndexOrThrow(_cursor, "correct");
          final List<PredictionEntity> _result = new ArrayList<PredictionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PredictionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpRoundId;
            _tmpRoundId = _cursor.getLong(_cursorIndexOfRoundId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final String _tmpModelName;
            _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            final int _tmpTopPick;
            _tmpTopPick = _cursor.getInt(_cursorIndexOfTopPick);
            final double _tmpTopProbability;
            _tmpTopProbability = _cursor.getDouble(_cursorIndexOfTopProbability);
            final double _tmpConfidence;
            _tmpConfidence = _cursor.getDouble(_cursorIndexOfConfidence);
            final String _tmpReason;
            _tmpReason = _cursor.getString(_cursorIndexOfReason);
            final String _tmpNumberProbabilities;
            _tmpNumberProbabilities = _cursor.getString(_cursorIndexOfNumberProbabilities);
            final String _tmpColorProbabilities;
            _tmpColorProbabilities = _cursor.getString(_cursorIndexOfColorProbabilities);
            final Integer _tmpActualOutcome;
            if (_cursor.isNull(_cursorIndexOfActualOutcome)) {
              _tmpActualOutcome = null;
            } else {
              _tmpActualOutcome = _cursor.getInt(_cursorIndexOfActualOutcome);
            }
            final Integer _tmpCorrect;
            if (_cursor.isNull(_cursorIndexOfCorrect)) {
              _tmpCorrect = null;
            } else {
              _tmpCorrect = _cursor.getInt(_cursorIndexOfCorrect);
            }
            _item = new PredictionEntity(_tmpId,_tmpRoundId,_tmpEpochMs,_tmpModelName,_tmpTopPick,_tmpTopProbability,_tmpConfidence,_tmpReason,_tmpNumberProbabilities,_tmpColorProbabilities,_tmpActualOutcome,_tmpCorrect);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object resolvedCount(final String model, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM predictions WHERE modelName = ? AND correct IS NOT NULL";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, model);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object correctCount(final String model, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM predictions WHERE modelName = ? AND correct = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, model);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
