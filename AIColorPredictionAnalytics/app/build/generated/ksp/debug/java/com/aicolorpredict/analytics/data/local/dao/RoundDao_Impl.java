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
import com.aicolorpredict.analytics.data.local.entity.RoundEntity;
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
public final class RoundDao_Impl implements RoundDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<RoundEntity> __insertionAdapterOfRoundEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public RoundDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfRoundEntity = new EntityInsertionAdapter<RoundEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `rounds` (`id`,`epochMs`,`number`,`colors`,`previousNumber`,`previous3`,`previous5`,`previous10`,`previous20`,`previous50`,`previous100`,`previous500`,`previous1000`,`streak`,`isOdd`,`isEven`,`isSmall`,`isBig`,`isGreen`,`isRed`,`isViolet`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final RoundEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getEpochMs());
        statement.bindLong(3, entity.getNumber());
        statement.bindLong(4, entity.getColors());
        if (entity.getPreviousNumber() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getPreviousNumber());
        }
        statement.bindString(6, entity.getPrevious3());
        statement.bindString(7, entity.getPrevious5());
        statement.bindString(8, entity.getPrevious10());
        statement.bindString(9, entity.getPrevious20());
        statement.bindString(10, entity.getPrevious50());
        statement.bindString(11, entity.getPrevious100());
        statement.bindString(12, entity.getPrevious500());
        statement.bindString(13, entity.getPrevious1000());
        statement.bindLong(14, entity.getStreak());
        final int _tmp = entity.isOdd() ? 1 : 0;
        statement.bindLong(15, _tmp);
        final int _tmp_1 = entity.isEven() ? 1 : 0;
        statement.bindLong(16, _tmp_1);
        final int _tmp_2 = entity.isSmall() ? 1 : 0;
        statement.bindLong(17, _tmp_2);
        final int _tmp_3 = entity.isBig() ? 1 : 0;
        statement.bindLong(18, _tmp_3);
        final int _tmp_4 = entity.isGreen() ? 1 : 0;
        statement.bindLong(19, _tmp_4);
        final int _tmp_5 = entity.isRed() ? 1 : 0;
        statement.bindLong(20, _tmp_5);
        final int _tmp_6 = entity.isViolet() ? 1 : 0;
        statement.bindLong(21, _tmp_6);
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM rounds";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM rounds WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final RoundEntity round, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfRoundEntity.insertAndReturnId(round);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<RoundEntity> rounds,
      final Continuation<? super List<Long>> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<List<Long>>() {
      @Override
      @NonNull
      public List<Long> call() throws Exception {
        __db.beginTransaction();
        try {
          final List<Long> _result = __insertionAdapterOfRoundEntity.insertAndReturnIdsList(rounds);
          __db.setTransactionSuccessful();
          return _result;
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
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<RoundEntity>> $completion) {
    final String _sql = "SELECT * FROM rounds ORDER BY epochMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RoundEntity>>() {
      @Override
      @NonNull
      public List<RoundEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfColors = CursorUtil.getColumnIndexOrThrow(_cursor, "colors");
          final int _cursorIndexOfPreviousNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "previousNumber");
          final int _cursorIndexOfPrevious3 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous3");
          final int _cursorIndexOfPrevious5 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous5");
          final int _cursorIndexOfPrevious10 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous10");
          final int _cursorIndexOfPrevious20 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous20");
          final int _cursorIndexOfPrevious50 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous50");
          final int _cursorIndexOfPrevious100 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous100");
          final int _cursorIndexOfPrevious500 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous500");
          final int _cursorIndexOfPrevious1000 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous1000");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfIsOdd = CursorUtil.getColumnIndexOrThrow(_cursor, "isOdd");
          final int _cursorIndexOfIsEven = CursorUtil.getColumnIndexOrThrow(_cursor, "isEven");
          final int _cursorIndexOfIsSmall = CursorUtil.getColumnIndexOrThrow(_cursor, "isSmall");
          final int _cursorIndexOfIsBig = CursorUtil.getColumnIndexOrThrow(_cursor, "isBig");
          final int _cursorIndexOfIsGreen = CursorUtil.getColumnIndexOrThrow(_cursor, "isGreen");
          final int _cursorIndexOfIsRed = CursorUtil.getColumnIndexOrThrow(_cursor, "isRed");
          final int _cursorIndexOfIsViolet = CursorUtil.getColumnIndexOrThrow(_cursor, "isViolet");
          final List<RoundEntity> _result = new ArrayList<RoundEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RoundEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final int _tmpNumber;
            _tmpNumber = _cursor.getInt(_cursorIndexOfNumber);
            final int _tmpColors;
            _tmpColors = _cursor.getInt(_cursorIndexOfColors);
            final Integer _tmpPreviousNumber;
            if (_cursor.isNull(_cursorIndexOfPreviousNumber)) {
              _tmpPreviousNumber = null;
            } else {
              _tmpPreviousNumber = _cursor.getInt(_cursorIndexOfPreviousNumber);
            }
            final String _tmpPrevious3;
            _tmpPrevious3 = _cursor.getString(_cursorIndexOfPrevious3);
            final String _tmpPrevious5;
            _tmpPrevious5 = _cursor.getString(_cursorIndexOfPrevious5);
            final String _tmpPrevious10;
            _tmpPrevious10 = _cursor.getString(_cursorIndexOfPrevious10);
            final String _tmpPrevious20;
            _tmpPrevious20 = _cursor.getString(_cursorIndexOfPrevious20);
            final String _tmpPrevious50;
            _tmpPrevious50 = _cursor.getString(_cursorIndexOfPrevious50);
            final String _tmpPrevious100;
            _tmpPrevious100 = _cursor.getString(_cursorIndexOfPrevious100);
            final String _tmpPrevious500;
            _tmpPrevious500 = _cursor.getString(_cursorIndexOfPrevious500);
            final String _tmpPrevious1000;
            _tmpPrevious1000 = _cursor.getString(_cursorIndexOfPrevious1000);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final boolean _tmpIsOdd;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOdd);
            _tmpIsOdd = _tmp != 0;
            final boolean _tmpIsEven;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEven);
            _tmpIsEven = _tmp_1 != 0;
            final boolean _tmpIsSmall;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSmall);
            _tmpIsSmall = _tmp_2 != 0;
            final boolean _tmpIsBig;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBig);
            _tmpIsBig = _tmp_3 != 0;
            final boolean _tmpIsGreen;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsGreen);
            _tmpIsGreen = _tmp_4 != 0;
            final boolean _tmpIsRed;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsRed);
            _tmpIsRed = _tmp_5 != 0;
            final boolean _tmpIsViolet;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsViolet);
            _tmpIsViolet = _tmp_6 != 0;
            _item = new RoundEntity(_tmpId,_tmpEpochMs,_tmpNumber,_tmpColors,_tmpPreviousNumber,_tmpPrevious3,_tmpPrevious5,_tmpPrevious10,_tmpPrevious20,_tmpPrevious50,_tmpPrevious100,_tmpPrevious500,_tmpPrevious1000,_tmpStreak,_tmpIsOdd,_tmpIsEven,_tmpIsSmall,_tmpIsBig,_tmpIsGreen,_tmpIsRed,_tmpIsViolet);
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
  public Object getPage(final int limit, final int offset,
      final Continuation<? super List<RoundEntity>> $completion) {
    final String _sql = "SELECT * FROM rounds ORDER BY epochMs ASC LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 2;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RoundEntity>>() {
      @Override
      @NonNull
      public List<RoundEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfColors = CursorUtil.getColumnIndexOrThrow(_cursor, "colors");
          final int _cursorIndexOfPreviousNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "previousNumber");
          final int _cursorIndexOfPrevious3 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous3");
          final int _cursorIndexOfPrevious5 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous5");
          final int _cursorIndexOfPrevious10 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous10");
          final int _cursorIndexOfPrevious20 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous20");
          final int _cursorIndexOfPrevious50 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous50");
          final int _cursorIndexOfPrevious100 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous100");
          final int _cursorIndexOfPrevious500 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous500");
          final int _cursorIndexOfPrevious1000 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous1000");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfIsOdd = CursorUtil.getColumnIndexOrThrow(_cursor, "isOdd");
          final int _cursorIndexOfIsEven = CursorUtil.getColumnIndexOrThrow(_cursor, "isEven");
          final int _cursorIndexOfIsSmall = CursorUtil.getColumnIndexOrThrow(_cursor, "isSmall");
          final int _cursorIndexOfIsBig = CursorUtil.getColumnIndexOrThrow(_cursor, "isBig");
          final int _cursorIndexOfIsGreen = CursorUtil.getColumnIndexOrThrow(_cursor, "isGreen");
          final int _cursorIndexOfIsRed = CursorUtil.getColumnIndexOrThrow(_cursor, "isRed");
          final int _cursorIndexOfIsViolet = CursorUtil.getColumnIndexOrThrow(_cursor, "isViolet");
          final List<RoundEntity> _result = new ArrayList<RoundEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RoundEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final int _tmpNumber;
            _tmpNumber = _cursor.getInt(_cursorIndexOfNumber);
            final int _tmpColors;
            _tmpColors = _cursor.getInt(_cursorIndexOfColors);
            final Integer _tmpPreviousNumber;
            if (_cursor.isNull(_cursorIndexOfPreviousNumber)) {
              _tmpPreviousNumber = null;
            } else {
              _tmpPreviousNumber = _cursor.getInt(_cursorIndexOfPreviousNumber);
            }
            final String _tmpPrevious3;
            _tmpPrevious3 = _cursor.getString(_cursorIndexOfPrevious3);
            final String _tmpPrevious5;
            _tmpPrevious5 = _cursor.getString(_cursorIndexOfPrevious5);
            final String _tmpPrevious10;
            _tmpPrevious10 = _cursor.getString(_cursorIndexOfPrevious10);
            final String _tmpPrevious20;
            _tmpPrevious20 = _cursor.getString(_cursorIndexOfPrevious20);
            final String _tmpPrevious50;
            _tmpPrevious50 = _cursor.getString(_cursorIndexOfPrevious50);
            final String _tmpPrevious100;
            _tmpPrevious100 = _cursor.getString(_cursorIndexOfPrevious100);
            final String _tmpPrevious500;
            _tmpPrevious500 = _cursor.getString(_cursorIndexOfPrevious500);
            final String _tmpPrevious1000;
            _tmpPrevious1000 = _cursor.getString(_cursorIndexOfPrevious1000);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final boolean _tmpIsOdd;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOdd);
            _tmpIsOdd = _tmp != 0;
            final boolean _tmpIsEven;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEven);
            _tmpIsEven = _tmp_1 != 0;
            final boolean _tmpIsSmall;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSmall);
            _tmpIsSmall = _tmp_2 != 0;
            final boolean _tmpIsBig;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBig);
            _tmpIsBig = _tmp_3 != 0;
            final boolean _tmpIsGreen;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsGreen);
            _tmpIsGreen = _tmp_4 != 0;
            final boolean _tmpIsRed;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsRed);
            _tmpIsRed = _tmp_5 != 0;
            final boolean _tmpIsViolet;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsViolet);
            _tmpIsViolet = _tmp_6 != 0;
            _item = new RoundEntity(_tmpId,_tmpEpochMs,_tmpNumber,_tmpColors,_tmpPreviousNumber,_tmpPrevious3,_tmpPrevious5,_tmpPrevious10,_tmpPrevious20,_tmpPrevious50,_tmpPrevious100,_tmpPrevious500,_tmpPrevious1000,_tmpStreak,_tmpIsOdd,_tmpIsEven,_tmpIsSmall,_tmpIsBig,_tmpIsGreen,_tmpIsRed,_tmpIsViolet);
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
  public Object getLastN(final int n, final Continuation<? super List<RoundEntity>> $completion) {
    final String _sql = "SELECT * FROM rounds ORDER BY epochMs DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, n);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RoundEntity>>() {
      @Override
      @NonNull
      public List<RoundEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfColors = CursorUtil.getColumnIndexOrThrow(_cursor, "colors");
          final int _cursorIndexOfPreviousNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "previousNumber");
          final int _cursorIndexOfPrevious3 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous3");
          final int _cursorIndexOfPrevious5 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous5");
          final int _cursorIndexOfPrevious10 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous10");
          final int _cursorIndexOfPrevious20 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous20");
          final int _cursorIndexOfPrevious50 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous50");
          final int _cursorIndexOfPrevious100 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous100");
          final int _cursorIndexOfPrevious500 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous500");
          final int _cursorIndexOfPrevious1000 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous1000");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfIsOdd = CursorUtil.getColumnIndexOrThrow(_cursor, "isOdd");
          final int _cursorIndexOfIsEven = CursorUtil.getColumnIndexOrThrow(_cursor, "isEven");
          final int _cursorIndexOfIsSmall = CursorUtil.getColumnIndexOrThrow(_cursor, "isSmall");
          final int _cursorIndexOfIsBig = CursorUtil.getColumnIndexOrThrow(_cursor, "isBig");
          final int _cursorIndexOfIsGreen = CursorUtil.getColumnIndexOrThrow(_cursor, "isGreen");
          final int _cursorIndexOfIsRed = CursorUtil.getColumnIndexOrThrow(_cursor, "isRed");
          final int _cursorIndexOfIsViolet = CursorUtil.getColumnIndexOrThrow(_cursor, "isViolet");
          final List<RoundEntity> _result = new ArrayList<RoundEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RoundEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final int _tmpNumber;
            _tmpNumber = _cursor.getInt(_cursorIndexOfNumber);
            final int _tmpColors;
            _tmpColors = _cursor.getInt(_cursorIndexOfColors);
            final Integer _tmpPreviousNumber;
            if (_cursor.isNull(_cursorIndexOfPreviousNumber)) {
              _tmpPreviousNumber = null;
            } else {
              _tmpPreviousNumber = _cursor.getInt(_cursorIndexOfPreviousNumber);
            }
            final String _tmpPrevious3;
            _tmpPrevious3 = _cursor.getString(_cursorIndexOfPrevious3);
            final String _tmpPrevious5;
            _tmpPrevious5 = _cursor.getString(_cursorIndexOfPrevious5);
            final String _tmpPrevious10;
            _tmpPrevious10 = _cursor.getString(_cursorIndexOfPrevious10);
            final String _tmpPrevious20;
            _tmpPrevious20 = _cursor.getString(_cursorIndexOfPrevious20);
            final String _tmpPrevious50;
            _tmpPrevious50 = _cursor.getString(_cursorIndexOfPrevious50);
            final String _tmpPrevious100;
            _tmpPrevious100 = _cursor.getString(_cursorIndexOfPrevious100);
            final String _tmpPrevious500;
            _tmpPrevious500 = _cursor.getString(_cursorIndexOfPrevious500);
            final String _tmpPrevious1000;
            _tmpPrevious1000 = _cursor.getString(_cursorIndexOfPrevious1000);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final boolean _tmpIsOdd;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOdd);
            _tmpIsOdd = _tmp != 0;
            final boolean _tmpIsEven;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEven);
            _tmpIsEven = _tmp_1 != 0;
            final boolean _tmpIsSmall;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSmall);
            _tmpIsSmall = _tmp_2 != 0;
            final boolean _tmpIsBig;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBig);
            _tmpIsBig = _tmp_3 != 0;
            final boolean _tmpIsGreen;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsGreen);
            _tmpIsGreen = _tmp_4 != 0;
            final boolean _tmpIsRed;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsRed);
            _tmpIsRed = _tmp_5 != 0;
            final boolean _tmpIsViolet;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsViolet);
            _tmpIsViolet = _tmp_6 != 0;
            _item = new RoundEntity(_tmpId,_tmpEpochMs,_tmpNumber,_tmpColors,_tmpPreviousNumber,_tmpPrevious3,_tmpPrevious5,_tmpPrevious10,_tmpPrevious20,_tmpPrevious50,_tmpPrevious100,_tmpPrevious500,_tmpPrevious1000,_tmpStreak,_tmpIsOdd,_tmpIsEven,_tmpIsSmall,_tmpIsBig,_tmpIsGreen,_tmpIsRed,_tmpIsViolet);
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
  public Flow<List<RoundEntity>> observeLastN(final int n) {
    final String _sql = "SELECT * FROM rounds ORDER BY epochMs DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, n);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"rounds"}, new Callable<List<RoundEntity>>() {
      @Override
      @NonNull
      public List<RoundEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfColors = CursorUtil.getColumnIndexOrThrow(_cursor, "colors");
          final int _cursorIndexOfPreviousNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "previousNumber");
          final int _cursorIndexOfPrevious3 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous3");
          final int _cursorIndexOfPrevious5 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous5");
          final int _cursorIndexOfPrevious10 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous10");
          final int _cursorIndexOfPrevious20 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous20");
          final int _cursorIndexOfPrevious50 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous50");
          final int _cursorIndexOfPrevious100 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous100");
          final int _cursorIndexOfPrevious500 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous500");
          final int _cursorIndexOfPrevious1000 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous1000");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfIsOdd = CursorUtil.getColumnIndexOrThrow(_cursor, "isOdd");
          final int _cursorIndexOfIsEven = CursorUtil.getColumnIndexOrThrow(_cursor, "isEven");
          final int _cursorIndexOfIsSmall = CursorUtil.getColumnIndexOrThrow(_cursor, "isSmall");
          final int _cursorIndexOfIsBig = CursorUtil.getColumnIndexOrThrow(_cursor, "isBig");
          final int _cursorIndexOfIsGreen = CursorUtil.getColumnIndexOrThrow(_cursor, "isGreen");
          final int _cursorIndexOfIsRed = CursorUtil.getColumnIndexOrThrow(_cursor, "isRed");
          final int _cursorIndexOfIsViolet = CursorUtil.getColumnIndexOrThrow(_cursor, "isViolet");
          final List<RoundEntity> _result = new ArrayList<RoundEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RoundEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final int _tmpNumber;
            _tmpNumber = _cursor.getInt(_cursorIndexOfNumber);
            final int _tmpColors;
            _tmpColors = _cursor.getInt(_cursorIndexOfColors);
            final Integer _tmpPreviousNumber;
            if (_cursor.isNull(_cursorIndexOfPreviousNumber)) {
              _tmpPreviousNumber = null;
            } else {
              _tmpPreviousNumber = _cursor.getInt(_cursorIndexOfPreviousNumber);
            }
            final String _tmpPrevious3;
            _tmpPrevious3 = _cursor.getString(_cursorIndexOfPrevious3);
            final String _tmpPrevious5;
            _tmpPrevious5 = _cursor.getString(_cursorIndexOfPrevious5);
            final String _tmpPrevious10;
            _tmpPrevious10 = _cursor.getString(_cursorIndexOfPrevious10);
            final String _tmpPrevious20;
            _tmpPrevious20 = _cursor.getString(_cursorIndexOfPrevious20);
            final String _tmpPrevious50;
            _tmpPrevious50 = _cursor.getString(_cursorIndexOfPrevious50);
            final String _tmpPrevious100;
            _tmpPrevious100 = _cursor.getString(_cursorIndexOfPrevious100);
            final String _tmpPrevious500;
            _tmpPrevious500 = _cursor.getString(_cursorIndexOfPrevious500);
            final String _tmpPrevious1000;
            _tmpPrevious1000 = _cursor.getString(_cursorIndexOfPrevious1000);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final boolean _tmpIsOdd;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOdd);
            _tmpIsOdd = _tmp != 0;
            final boolean _tmpIsEven;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEven);
            _tmpIsEven = _tmp_1 != 0;
            final boolean _tmpIsSmall;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSmall);
            _tmpIsSmall = _tmp_2 != 0;
            final boolean _tmpIsBig;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBig);
            _tmpIsBig = _tmp_3 != 0;
            final boolean _tmpIsGreen;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsGreen);
            _tmpIsGreen = _tmp_4 != 0;
            final boolean _tmpIsRed;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsRed);
            _tmpIsRed = _tmp_5 != 0;
            final boolean _tmpIsViolet;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsViolet);
            _tmpIsViolet = _tmp_6 != 0;
            _item = new RoundEntity(_tmpId,_tmpEpochMs,_tmpNumber,_tmpColors,_tmpPreviousNumber,_tmpPrevious3,_tmpPrevious5,_tmpPrevious10,_tmpPrevious20,_tmpPrevious50,_tmpPrevious100,_tmpPrevious500,_tmpPrevious1000,_tmpStreak,_tmpIsOdd,_tmpIsEven,_tmpIsSmall,_tmpIsBig,_tmpIsGreen,_tmpIsRed,_tmpIsViolet);
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
  public Object getById(final long id, final Continuation<? super RoundEntity> $completion) {
    final String _sql = "SELECT * FROM rounds WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<RoundEntity>() {
      @Override
      @Nullable
      public RoundEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfColors = CursorUtil.getColumnIndexOrThrow(_cursor, "colors");
          final int _cursorIndexOfPreviousNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "previousNumber");
          final int _cursorIndexOfPrevious3 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous3");
          final int _cursorIndexOfPrevious5 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous5");
          final int _cursorIndexOfPrevious10 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous10");
          final int _cursorIndexOfPrevious20 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous20");
          final int _cursorIndexOfPrevious50 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous50");
          final int _cursorIndexOfPrevious100 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous100");
          final int _cursorIndexOfPrevious500 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous500");
          final int _cursorIndexOfPrevious1000 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous1000");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfIsOdd = CursorUtil.getColumnIndexOrThrow(_cursor, "isOdd");
          final int _cursorIndexOfIsEven = CursorUtil.getColumnIndexOrThrow(_cursor, "isEven");
          final int _cursorIndexOfIsSmall = CursorUtil.getColumnIndexOrThrow(_cursor, "isSmall");
          final int _cursorIndexOfIsBig = CursorUtil.getColumnIndexOrThrow(_cursor, "isBig");
          final int _cursorIndexOfIsGreen = CursorUtil.getColumnIndexOrThrow(_cursor, "isGreen");
          final int _cursorIndexOfIsRed = CursorUtil.getColumnIndexOrThrow(_cursor, "isRed");
          final int _cursorIndexOfIsViolet = CursorUtil.getColumnIndexOrThrow(_cursor, "isViolet");
          final RoundEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final int _tmpNumber;
            _tmpNumber = _cursor.getInt(_cursorIndexOfNumber);
            final int _tmpColors;
            _tmpColors = _cursor.getInt(_cursorIndexOfColors);
            final Integer _tmpPreviousNumber;
            if (_cursor.isNull(_cursorIndexOfPreviousNumber)) {
              _tmpPreviousNumber = null;
            } else {
              _tmpPreviousNumber = _cursor.getInt(_cursorIndexOfPreviousNumber);
            }
            final String _tmpPrevious3;
            _tmpPrevious3 = _cursor.getString(_cursorIndexOfPrevious3);
            final String _tmpPrevious5;
            _tmpPrevious5 = _cursor.getString(_cursorIndexOfPrevious5);
            final String _tmpPrevious10;
            _tmpPrevious10 = _cursor.getString(_cursorIndexOfPrevious10);
            final String _tmpPrevious20;
            _tmpPrevious20 = _cursor.getString(_cursorIndexOfPrevious20);
            final String _tmpPrevious50;
            _tmpPrevious50 = _cursor.getString(_cursorIndexOfPrevious50);
            final String _tmpPrevious100;
            _tmpPrevious100 = _cursor.getString(_cursorIndexOfPrevious100);
            final String _tmpPrevious500;
            _tmpPrevious500 = _cursor.getString(_cursorIndexOfPrevious500);
            final String _tmpPrevious1000;
            _tmpPrevious1000 = _cursor.getString(_cursorIndexOfPrevious1000);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final boolean _tmpIsOdd;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOdd);
            _tmpIsOdd = _tmp != 0;
            final boolean _tmpIsEven;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEven);
            _tmpIsEven = _tmp_1 != 0;
            final boolean _tmpIsSmall;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSmall);
            _tmpIsSmall = _tmp_2 != 0;
            final boolean _tmpIsBig;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBig);
            _tmpIsBig = _tmp_3 != 0;
            final boolean _tmpIsGreen;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsGreen);
            _tmpIsGreen = _tmp_4 != 0;
            final boolean _tmpIsRed;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsRed);
            _tmpIsRed = _tmp_5 != 0;
            final boolean _tmpIsViolet;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsViolet);
            _tmpIsViolet = _tmp_6 != 0;
            _result = new RoundEntity(_tmpId,_tmpEpochMs,_tmpNumber,_tmpColors,_tmpPreviousNumber,_tmpPrevious3,_tmpPrevious5,_tmpPrevious10,_tmpPrevious20,_tmpPrevious50,_tmpPrevious100,_tmpPrevious500,_tmpPrevious1000,_tmpStreak,_tmpIsOdd,_tmpIsEven,_tmpIsSmall,_tmpIsBig,_tmpIsGreen,_tmpIsRed,_tmpIsViolet);
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

  @Override
  public Object getByNumber(final int n,
      final Continuation<? super List<RoundEntity>> $completion) {
    final String _sql = "SELECT * FROM rounds WHERE number = ? ORDER BY epochMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, n);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RoundEntity>>() {
      @Override
      @NonNull
      public List<RoundEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfColors = CursorUtil.getColumnIndexOrThrow(_cursor, "colors");
          final int _cursorIndexOfPreviousNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "previousNumber");
          final int _cursorIndexOfPrevious3 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous3");
          final int _cursorIndexOfPrevious5 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous5");
          final int _cursorIndexOfPrevious10 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous10");
          final int _cursorIndexOfPrevious20 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous20");
          final int _cursorIndexOfPrevious50 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous50");
          final int _cursorIndexOfPrevious100 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous100");
          final int _cursorIndexOfPrevious500 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous500");
          final int _cursorIndexOfPrevious1000 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous1000");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfIsOdd = CursorUtil.getColumnIndexOrThrow(_cursor, "isOdd");
          final int _cursorIndexOfIsEven = CursorUtil.getColumnIndexOrThrow(_cursor, "isEven");
          final int _cursorIndexOfIsSmall = CursorUtil.getColumnIndexOrThrow(_cursor, "isSmall");
          final int _cursorIndexOfIsBig = CursorUtil.getColumnIndexOrThrow(_cursor, "isBig");
          final int _cursorIndexOfIsGreen = CursorUtil.getColumnIndexOrThrow(_cursor, "isGreen");
          final int _cursorIndexOfIsRed = CursorUtil.getColumnIndexOrThrow(_cursor, "isRed");
          final int _cursorIndexOfIsViolet = CursorUtil.getColumnIndexOrThrow(_cursor, "isViolet");
          final List<RoundEntity> _result = new ArrayList<RoundEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RoundEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final int _tmpNumber;
            _tmpNumber = _cursor.getInt(_cursorIndexOfNumber);
            final int _tmpColors;
            _tmpColors = _cursor.getInt(_cursorIndexOfColors);
            final Integer _tmpPreviousNumber;
            if (_cursor.isNull(_cursorIndexOfPreviousNumber)) {
              _tmpPreviousNumber = null;
            } else {
              _tmpPreviousNumber = _cursor.getInt(_cursorIndexOfPreviousNumber);
            }
            final String _tmpPrevious3;
            _tmpPrevious3 = _cursor.getString(_cursorIndexOfPrevious3);
            final String _tmpPrevious5;
            _tmpPrevious5 = _cursor.getString(_cursorIndexOfPrevious5);
            final String _tmpPrevious10;
            _tmpPrevious10 = _cursor.getString(_cursorIndexOfPrevious10);
            final String _tmpPrevious20;
            _tmpPrevious20 = _cursor.getString(_cursorIndexOfPrevious20);
            final String _tmpPrevious50;
            _tmpPrevious50 = _cursor.getString(_cursorIndexOfPrevious50);
            final String _tmpPrevious100;
            _tmpPrevious100 = _cursor.getString(_cursorIndexOfPrevious100);
            final String _tmpPrevious500;
            _tmpPrevious500 = _cursor.getString(_cursorIndexOfPrevious500);
            final String _tmpPrevious1000;
            _tmpPrevious1000 = _cursor.getString(_cursorIndexOfPrevious1000);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final boolean _tmpIsOdd;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOdd);
            _tmpIsOdd = _tmp != 0;
            final boolean _tmpIsEven;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEven);
            _tmpIsEven = _tmp_1 != 0;
            final boolean _tmpIsSmall;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSmall);
            _tmpIsSmall = _tmp_2 != 0;
            final boolean _tmpIsBig;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBig);
            _tmpIsBig = _tmp_3 != 0;
            final boolean _tmpIsGreen;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsGreen);
            _tmpIsGreen = _tmp_4 != 0;
            final boolean _tmpIsRed;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsRed);
            _tmpIsRed = _tmp_5 != 0;
            final boolean _tmpIsViolet;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsViolet);
            _tmpIsViolet = _tmp_6 != 0;
            _item = new RoundEntity(_tmpId,_tmpEpochMs,_tmpNumber,_tmpColors,_tmpPreviousNumber,_tmpPrevious3,_tmpPrevious5,_tmpPrevious10,_tmpPrevious20,_tmpPrevious50,_tmpPrevious100,_tmpPrevious500,_tmpPrevious1000,_tmpStreak,_tmpIsOdd,_tmpIsEven,_tmpIsSmall,_tmpIsBig,_tmpIsGreen,_tmpIsRed,_tmpIsViolet);
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
  public Object getByTimeRange(final long from, final long to,
      final Continuation<? super List<RoundEntity>> $completion) {
    final String _sql = "SELECT * FROM rounds WHERE epochMs BETWEEN ? AND ? ORDER BY epochMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, from);
    _argIndex = 2;
    _statement.bindLong(_argIndex, to);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<RoundEntity>>() {
      @Override
      @NonNull
      public List<RoundEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "epochMs");
          final int _cursorIndexOfNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "number");
          final int _cursorIndexOfColors = CursorUtil.getColumnIndexOrThrow(_cursor, "colors");
          final int _cursorIndexOfPreviousNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "previousNumber");
          final int _cursorIndexOfPrevious3 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous3");
          final int _cursorIndexOfPrevious5 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous5");
          final int _cursorIndexOfPrevious10 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous10");
          final int _cursorIndexOfPrevious20 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous20");
          final int _cursorIndexOfPrevious50 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous50");
          final int _cursorIndexOfPrevious100 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous100");
          final int _cursorIndexOfPrevious500 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous500");
          final int _cursorIndexOfPrevious1000 = CursorUtil.getColumnIndexOrThrow(_cursor, "previous1000");
          final int _cursorIndexOfStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "streak");
          final int _cursorIndexOfIsOdd = CursorUtil.getColumnIndexOrThrow(_cursor, "isOdd");
          final int _cursorIndexOfIsEven = CursorUtil.getColumnIndexOrThrow(_cursor, "isEven");
          final int _cursorIndexOfIsSmall = CursorUtil.getColumnIndexOrThrow(_cursor, "isSmall");
          final int _cursorIndexOfIsBig = CursorUtil.getColumnIndexOrThrow(_cursor, "isBig");
          final int _cursorIndexOfIsGreen = CursorUtil.getColumnIndexOrThrow(_cursor, "isGreen");
          final int _cursorIndexOfIsRed = CursorUtil.getColumnIndexOrThrow(_cursor, "isRed");
          final int _cursorIndexOfIsViolet = CursorUtil.getColumnIndexOrThrow(_cursor, "isViolet");
          final List<RoundEntity> _result = new ArrayList<RoundEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final RoundEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpEpochMs;
            _tmpEpochMs = _cursor.getLong(_cursorIndexOfEpochMs);
            final int _tmpNumber;
            _tmpNumber = _cursor.getInt(_cursorIndexOfNumber);
            final int _tmpColors;
            _tmpColors = _cursor.getInt(_cursorIndexOfColors);
            final Integer _tmpPreviousNumber;
            if (_cursor.isNull(_cursorIndexOfPreviousNumber)) {
              _tmpPreviousNumber = null;
            } else {
              _tmpPreviousNumber = _cursor.getInt(_cursorIndexOfPreviousNumber);
            }
            final String _tmpPrevious3;
            _tmpPrevious3 = _cursor.getString(_cursorIndexOfPrevious3);
            final String _tmpPrevious5;
            _tmpPrevious5 = _cursor.getString(_cursorIndexOfPrevious5);
            final String _tmpPrevious10;
            _tmpPrevious10 = _cursor.getString(_cursorIndexOfPrevious10);
            final String _tmpPrevious20;
            _tmpPrevious20 = _cursor.getString(_cursorIndexOfPrevious20);
            final String _tmpPrevious50;
            _tmpPrevious50 = _cursor.getString(_cursorIndexOfPrevious50);
            final String _tmpPrevious100;
            _tmpPrevious100 = _cursor.getString(_cursorIndexOfPrevious100);
            final String _tmpPrevious500;
            _tmpPrevious500 = _cursor.getString(_cursorIndexOfPrevious500);
            final String _tmpPrevious1000;
            _tmpPrevious1000 = _cursor.getString(_cursorIndexOfPrevious1000);
            final int _tmpStreak;
            _tmpStreak = _cursor.getInt(_cursorIndexOfStreak);
            final boolean _tmpIsOdd;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsOdd);
            _tmpIsOdd = _tmp != 0;
            final boolean _tmpIsEven;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEven);
            _tmpIsEven = _tmp_1 != 0;
            final boolean _tmpIsSmall;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSmall);
            _tmpIsSmall = _tmp_2 != 0;
            final boolean _tmpIsBig;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsBig);
            _tmpIsBig = _tmp_3 != 0;
            final boolean _tmpIsGreen;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsGreen);
            _tmpIsGreen = _tmp_4 != 0;
            final boolean _tmpIsRed;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfIsRed);
            _tmpIsRed = _tmp_5 != 0;
            final boolean _tmpIsViolet;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsViolet);
            _tmpIsViolet = _tmp_6 != 0;
            _item = new RoundEntity(_tmpId,_tmpEpochMs,_tmpNumber,_tmpColors,_tmpPreviousNumber,_tmpPrevious3,_tmpPrevious5,_tmpPrevious10,_tmpPrevious20,_tmpPrevious50,_tmpPrevious100,_tmpPrevious500,_tmpPrevious1000,_tmpStreak,_tmpIsOdd,_tmpIsEven,_tmpIsSmall,_tmpIsBig,_tmpIsGreen,_tmpIsRed,_tmpIsViolet);
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
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM rounds";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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
  public Flow<Integer> observeCount() {
    final String _sql = "SELECT COUNT(*) FROM rounds";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"rounds"}, new Callable<Integer>() {
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
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object numberHistogram(final Continuation<? super List<NumberCount>> $completion) {
    final String _sql = "SELECT number, COUNT(*) AS c FROM rounds GROUP BY number ORDER BY number";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<NumberCount>>() {
      @Override
      @NonNull
      public List<NumberCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfNumber = 0;
          final int _cursorIndexOfC = 1;
          final List<NumberCount> _result = new ArrayList<NumberCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NumberCount _item;
            final int _tmpNumber;
            _tmpNumber = _cursor.getInt(_cursorIndexOfNumber);
            final int _tmpC;
            _tmpC = _cursor.getInt(_cursorIndexOfC);
            _item = new NumberCount(_tmpNumber,_tmpC);
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
  public Object latestEpoch(final Continuation<? super Long> $completion) {
    final String _sql = "SELECT MAX(epochMs) FROM rounds";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
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
