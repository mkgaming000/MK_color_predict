package com.aicolorpredict.analytics.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.aicolorpredict.analytics.data.local.entity.ModelPerformanceEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class ModelPerformanceDao_Impl implements ModelPerformanceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ModelPerformanceEntity> __insertionAdapterOfModelPerformanceEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public ModelPerformanceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfModelPerformanceEntity = new EntityInsertionAdapter<ModelPerformanceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `model_performance` (`modelName`,`samplesObserved`,`top1Accuracy`,`top3Accuracy`,`top5Accuracy`,`logLoss`,`brierScore`,`precision`,`recall`,`f1`,`confusionMatrixCsv`,`rollingAccuracy`,`rollingWindow`,`lastUpdated`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ModelPerformanceEntity entity) {
        statement.bindString(1, entity.getModelName());
        statement.bindLong(2, entity.getSamplesObserved());
        statement.bindDouble(3, entity.getTop1Accuracy());
        statement.bindDouble(4, entity.getTop3Accuracy());
        statement.bindDouble(5, entity.getTop5Accuracy());
        statement.bindDouble(6, entity.getLogLoss());
        statement.bindDouble(7, entity.getBrierScore());
        statement.bindDouble(8, entity.getPrecision());
        statement.bindDouble(9, entity.getRecall());
        statement.bindDouble(10, entity.getF1());
        statement.bindString(11, entity.getConfusionMatrixCsv());
        statement.bindDouble(12, entity.getRollingAccuracy());
        statement.bindLong(13, entity.getRollingWindow());
        statement.bindLong(14, entity.getLastUpdated());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM model_performance";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final ModelPerformanceEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfModelPerformanceEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertAll(final List<ModelPerformanceEntity> entities,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfModelPerformanceEntity.insert(entities);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
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
  public Object getAll(final Continuation<? super List<ModelPerformanceEntity>> $completion) {
    final String _sql = "SELECT * FROM model_performance ORDER BY top1Accuracy DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ModelPerformanceEntity>>() {
      @Override
      @NonNull
      public List<ModelPerformanceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfSamplesObserved = CursorUtil.getColumnIndexOrThrow(_cursor, "samplesObserved");
          final int _cursorIndexOfTop1Accuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "top1Accuracy");
          final int _cursorIndexOfTop3Accuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "top3Accuracy");
          final int _cursorIndexOfTop5Accuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "top5Accuracy");
          final int _cursorIndexOfLogLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "logLoss");
          final int _cursorIndexOfBrierScore = CursorUtil.getColumnIndexOrThrow(_cursor, "brierScore");
          final int _cursorIndexOfPrecision = CursorUtil.getColumnIndexOrThrow(_cursor, "precision");
          final int _cursorIndexOfRecall = CursorUtil.getColumnIndexOrThrow(_cursor, "recall");
          final int _cursorIndexOfF1 = CursorUtil.getColumnIndexOrThrow(_cursor, "f1");
          final int _cursorIndexOfConfusionMatrixCsv = CursorUtil.getColumnIndexOrThrow(_cursor, "confusionMatrixCsv");
          final int _cursorIndexOfRollingAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "rollingAccuracy");
          final int _cursorIndexOfRollingWindow = CursorUtil.getColumnIndexOrThrow(_cursor, "rollingWindow");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final List<ModelPerformanceEntity> _result = new ArrayList<ModelPerformanceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ModelPerformanceEntity _item;
            final String _tmpModelName;
            _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            final int _tmpSamplesObserved;
            _tmpSamplesObserved = _cursor.getInt(_cursorIndexOfSamplesObserved);
            final double _tmpTop1Accuracy;
            _tmpTop1Accuracy = _cursor.getDouble(_cursorIndexOfTop1Accuracy);
            final double _tmpTop3Accuracy;
            _tmpTop3Accuracy = _cursor.getDouble(_cursorIndexOfTop3Accuracy);
            final double _tmpTop5Accuracy;
            _tmpTop5Accuracy = _cursor.getDouble(_cursorIndexOfTop5Accuracy);
            final double _tmpLogLoss;
            _tmpLogLoss = _cursor.getDouble(_cursorIndexOfLogLoss);
            final double _tmpBrierScore;
            _tmpBrierScore = _cursor.getDouble(_cursorIndexOfBrierScore);
            final double _tmpPrecision;
            _tmpPrecision = _cursor.getDouble(_cursorIndexOfPrecision);
            final double _tmpRecall;
            _tmpRecall = _cursor.getDouble(_cursorIndexOfRecall);
            final double _tmpF1;
            _tmpF1 = _cursor.getDouble(_cursorIndexOfF1);
            final String _tmpConfusionMatrixCsv;
            _tmpConfusionMatrixCsv = _cursor.getString(_cursorIndexOfConfusionMatrixCsv);
            final double _tmpRollingAccuracy;
            _tmpRollingAccuracy = _cursor.getDouble(_cursorIndexOfRollingAccuracy);
            final int _tmpRollingWindow;
            _tmpRollingWindow = _cursor.getInt(_cursorIndexOfRollingWindow);
            final long _tmpLastUpdated;
            _tmpLastUpdated = _cursor.getLong(_cursorIndexOfLastUpdated);
            _item = new ModelPerformanceEntity(_tmpModelName,_tmpSamplesObserved,_tmpTop1Accuracy,_tmpTop3Accuracy,_tmpTop5Accuracy,_tmpLogLoss,_tmpBrierScore,_tmpPrecision,_tmpRecall,_tmpF1,_tmpConfusionMatrixCsv,_tmpRollingAccuracy,_tmpRollingWindow,_tmpLastUpdated);
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
  public Flow<List<ModelPerformanceEntity>> observeAll() {
    final String _sql = "SELECT * FROM model_performance ORDER BY top1Accuracy DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"model_performance"}, new Callable<List<ModelPerformanceEntity>>() {
      @Override
      @NonNull
      public List<ModelPerformanceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfSamplesObserved = CursorUtil.getColumnIndexOrThrow(_cursor, "samplesObserved");
          final int _cursorIndexOfTop1Accuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "top1Accuracy");
          final int _cursorIndexOfTop3Accuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "top3Accuracy");
          final int _cursorIndexOfTop5Accuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "top5Accuracy");
          final int _cursorIndexOfLogLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "logLoss");
          final int _cursorIndexOfBrierScore = CursorUtil.getColumnIndexOrThrow(_cursor, "brierScore");
          final int _cursorIndexOfPrecision = CursorUtil.getColumnIndexOrThrow(_cursor, "precision");
          final int _cursorIndexOfRecall = CursorUtil.getColumnIndexOrThrow(_cursor, "recall");
          final int _cursorIndexOfF1 = CursorUtil.getColumnIndexOrThrow(_cursor, "f1");
          final int _cursorIndexOfConfusionMatrixCsv = CursorUtil.getColumnIndexOrThrow(_cursor, "confusionMatrixCsv");
          final int _cursorIndexOfRollingAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "rollingAccuracy");
          final int _cursorIndexOfRollingWindow = CursorUtil.getColumnIndexOrThrow(_cursor, "rollingWindow");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final List<ModelPerformanceEntity> _result = new ArrayList<ModelPerformanceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ModelPerformanceEntity _item;
            final String _tmpModelName;
            _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            final int _tmpSamplesObserved;
            _tmpSamplesObserved = _cursor.getInt(_cursorIndexOfSamplesObserved);
            final double _tmpTop1Accuracy;
            _tmpTop1Accuracy = _cursor.getDouble(_cursorIndexOfTop1Accuracy);
            final double _tmpTop3Accuracy;
            _tmpTop3Accuracy = _cursor.getDouble(_cursorIndexOfTop3Accuracy);
            final double _tmpTop5Accuracy;
            _tmpTop5Accuracy = _cursor.getDouble(_cursorIndexOfTop5Accuracy);
            final double _tmpLogLoss;
            _tmpLogLoss = _cursor.getDouble(_cursorIndexOfLogLoss);
            final double _tmpBrierScore;
            _tmpBrierScore = _cursor.getDouble(_cursorIndexOfBrierScore);
            final double _tmpPrecision;
            _tmpPrecision = _cursor.getDouble(_cursorIndexOfPrecision);
            final double _tmpRecall;
            _tmpRecall = _cursor.getDouble(_cursorIndexOfRecall);
            final double _tmpF1;
            _tmpF1 = _cursor.getDouble(_cursorIndexOfF1);
            final String _tmpConfusionMatrixCsv;
            _tmpConfusionMatrixCsv = _cursor.getString(_cursorIndexOfConfusionMatrixCsv);
            final double _tmpRollingAccuracy;
            _tmpRollingAccuracy = _cursor.getDouble(_cursorIndexOfRollingAccuracy);
            final int _tmpRollingWindow;
            _tmpRollingWindow = _cursor.getInt(_cursorIndexOfRollingWindow);
            final long _tmpLastUpdated;
            _tmpLastUpdated = _cursor.getLong(_cursorIndexOfLastUpdated);
            _item = new ModelPerformanceEntity(_tmpModelName,_tmpSamplesObserved,_tmpTop1Accuracy,_tmpTop3Accuracy,_tmpTop5Accuracy,_tmpLogLoss,_tmpBrierScore,_tmpPrecision,_tmpRecall,_tmpF1,_tmpConfusionMatrixCsv,_tmpRollingAccuracy,_tmpRollingWindow,_tmpLastUpdated);
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
  public Object getByName(final String name,
      final Continuation<? super ModelPerformanceEntity> $completion) {
    final String _sql = "SELECT * FROM model_performance WHERE modelName = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, name);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ModelPerformanceEntity>() {
      @Override
      @Nullable
      public ModelPerformanceEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfModelName = CursorUtil.getColumnIndexOrThrow(_cursor, "modelName");
          final int _cursorIndexOfSamplesObserved = CursorUtil.getColumnIndexOrThrow(_cursor, "samplesObserved");
          final int _cursorIndexOfTop1Accuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "top1Accuracy");
          final int _cursorIndexOfTop3Accuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "top3Accuracy");
          final int _cursorIndexOfTop5Accuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "top5Accuracy");
          final int _cursorIndexOfLogLoss = CursorUtil.getColumnIndexOrThrow(_cursor, "logLoss");
          final int _cursorIndexOfBrierScore = CursorUtil.getColumnIndexOrThrow(_cursor, "brierScore");
          final int _cursorIndexOfPrecision = CursorUtil.getColumnIndexOrThrow(_cursor, "precision");
          final int _cursorIndexOfRecall = CursorUtil.getColumnIndexOrThrow(_cursor, "recall");
          final int _cursorIndexOfF1 = CursorUtil.getColumnIndexOrThrow(_cursor, "f1");
          final int _cursorIndexOfConfusionMatrixCsv = CursorUtil.getColumnIndexOrThrow(_cursor, "confusionMatrixCsv");
          final int _cursorIndexOfRollingAccuracy = CursorUtil.getColumnIndexOrThrow(_cursor, "rollingAccuracy");
          final int _cursorIndexOfRollingWindow = CursorUtil.getColumnIndexOrThrow(_cursor, "rollingWindow");
          final int _cursorIndexOfLastUpdated = CursorUtil.getColumnIndexOrThrow(_cursor, "lastUpdated");
          final ModelPerformanceEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpModelName;
            _tmpModelName = _cursor.getString(_cursorIndexOfModelName);
            final int _tmpSamplesObserved;
            _tmpSamplesObserved = _cursor.getInt(_cursorIndexOfSamplesObserved);
            final double _tmpTop1Accuracy;
            _tmpTop1Accuracy = _cursor.getDouble(_cursorIndexOfTop1Accuracy);
            final double _tmpTop3Accuracy;
            _tmpTop3Accuracy = _cursor.getDouble(_cursorIndexOfTop3Accuracy);
            final double _tmpTop5Accuracy;
            _tmpTop5Accuracy = _cursor.getDouble(_cursorIndexOfTop5Accuracy);
            final double _tmpLogLoss;
            _tmpLogLoss = _cursor.getDouble(_cursorIndexOfLogLoss);
            final double _tmpBrierScore;
            _tmpBrierScore = _cursor.getDouble(_cursorIndexOfBrierScore);
            final double _tmpPrecision;
            _tmpPrecision = _cursor.getDouble(_cursorIndexOfPrecision);
            final double _tmpRecall;
            _tmpRecall = _cursor.getDouble(_cursorIndexOfRecall);
            final double _tmpF1;
            _tmpF1 = _cursor.getDouble(_cursorIndexOfF1);
            final String _tmpConfusionMatrixCsv;
            _tmpConfusionMatrixCsv = _cursor.getString(_cursorIndexOfConfusionMatrixCsv);
            final double _tmpRollingAccuracy;
            _tmpRollingAccuracy = _cursor.getDouble(_cursorIndexOfRollingAccuracy);
            final int _tmpRollingWindow;
            _tmpRollingWindow = _cursor.getInt(_cursorIndexOfRollingWindow);
            final long _tmpLastUpdated;
            _tmpLastUpdated = _cursor.getLong(_cursorIndexOfLastUpdated);
            _result = new ModelPerformanceEntity(_tmpModelName,_tmpSamplesObserved,_tmpTop1Accuracy,_tmpTop3Accuracy,_tmpTop5Accuracy,_tmpLogLoss,_tmpBrierScore,_tmpPrecision,_tmpRecall,_tmpF1,_tmpConfusionMatrixCsv,_tmpRollingAccuracy,_tmpRollingWindow,_tmpLastUpdated);
          } else {
            _result = null;
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
