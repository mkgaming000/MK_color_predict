package com.aicolorpredict.analytics.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.aicolorpredict.analytics.data.local.dao.ModelPerformanceDao;
import com.aicolorpredict.analytics.data.local.dao.ModelPerformanceDao_Impl;
import com.aicolorpredict.analytics.data.local.dao.PredictionDao;
import com.aicolorpredict.analytics.data.local.dao.PredictionDao_Impl;
import com.aicolorpredict.analytics.data.local.dao.RoundDao;
import com.aicolorpredict.analytics.data.local.dao.RoundDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile RoundDao _roundDao;

  private volatile PredictionDao _predictionDao;

  private volatile ModelPerformanceDao _modelPerformanceDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `rounds` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epochMs` INTEGER NOT NULL, `number` INTEGER NOT NULL, `colors` INTEGER NOT NULL, `previousNumber` INTEGER, `previous3` TEXT NOT NULL, `previous5` TEXT NOT NULL, `previous10` TEXT NOT NULL, `previous20` TEXT NOT NULL, `previous50` TEXT NOT NULL, `previous100` TEXT NOT NULL, `previous500` TEXT NOT NULL, `previous1000` TEXT NOT NULL, `streak` INTEGER NOT NULL, `isOdd` INTEGER NOT NULL, `isEven` INTEGER NOT NULL, `isSmall` INTEGER NOT NULL, `isBig` INTEGER NOT NULL, `isGreen` INTEGER NOT NULL, `isRed` INTEGER NOT NULL, `isViolet` INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_rounds_epochMs` ON `rounds` (`epochMs`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_rounds_number` ON `rounds` (`number`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_rounds_colors` ON `rounds` (`colors`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_rounds_streak` ON `rounds` (`streak`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `predictions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `roundId` INTEGER NOT NULL, `epochMs` INTEGER NOT NULL, `modelName` TEXT NOT NULL, `topPick` INTEGER NOT NULL, `topProbability` REAL NOT NULL, `confidence` REAL NOT NULL, `reason` TEXT NOT NULL, `numberProbabilities` TEXT NOT NULL, `colorProbabilities` TEXT NOT NULL, `actualOutcome` INTEGER, `correct` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_predictions_roundId` ON `predictions` (`roundId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_predictions_modelName` ON `predictions` (`modelName`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_predictions_epochMs` ON `predictions` (`epochMs`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_predictions_correct` ON `predictions` (`correct`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `model_performance` (`modelName` TEXT NOT NULL, `samplesObserved` INTEGER NOT NULL, `top1Accuracy` REAL NOT NULL, `top3Accuracy` REAL NOT NULL, `top5Accuracy` REAL NOT NULL, `logLoss` REAL NOT NULL, `brierScore` REAL NOT NULL, `precision` REAL NOT NULL, `recall` REAL NOT NULL, `f1` REAL NOT NULL, `confusionMatrixCsv` TEXT NOT NULL, `rollingAccuracy` REAL NOT NULL, `rollingWindow` INTEGER NOT NULL, `lastUpdated` INTEGER NOT NULL, PRIMARY KEY(`modelName`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '020b8069a6a430e02aa5c0a5fa5f6b74')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `rounds`");
        db.execSQL("DROP TABLE IF EXISTS `predictions`");
        db.execSQL("DROP TABLE IF EXISTS `model_performance`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsRounds = new HashMap<String, TableInfo.Column>(21);
        _columnsRounds.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("epochMs", new TableInfo.Column("epochMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("number", new TableInfo.Column("number", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("colors", new TableInfo.Column("colors", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("previousNumber", new TableInfo.Column("previousNumber", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("previous3", new TableInfo.Column("previous3", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("previous5", new TableInfo.Column("previous5", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("previous10", new TableInfo.Column("previous10", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("previous20", new TableInfo.Column("previous20", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("previous50", new TableInfo.Column("previous50", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("previous100", new TableInfo.Column("previous100", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("previous500", new TableInfo.Column("previous500", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("previous1000", new TableInfo.Column("previous1000", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("streak", new TableInfo.Column("streak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("isOdd", new TableInfo.Column("isOdd", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("isEven", new TableInfo.Column("isEven", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("isSmall", new TableInfo.Column("isSmall", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("isBig", new TableInfo.Column("isBig", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("isGreen", new TableInfo.Column("isGreen", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("isRed", new TableInfo.Column("isRed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsRounds.put("isViolet", new TableInfo.Column("isViolet", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysRounds = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesRounds = new HashSet<TableInfo.Index>(4);
        _indicesRounds.add(new TableInfo.Index("index_rounds_epochMs", false, Arrays.asList("epochMs"), Arrays.asList("ASC")));
        _indicesRounds.add(new TableInfo.Index("index_rounds_number", false, Arrays.asList("number"), Arrays.asList("ASC")));
        _indicesRounds.add(new TableInfo.Index("index_rounds_colors", false, Arrays.asList("colors"), Arrays.asList("ASC")));
        _indicesRounds.add(new TableInfo.Index("index_rounds_streak", false, Arrays.asList("streak"), Arrays.asList("ASC")));
        final TableInfo _infoRounds = new TableInfo("rounds", _columnsRounds, _foreignKeysRounds, _indicesRounds);
        final TableInfo _existingRounds = TableInfo.read(db, "rounds");
        if (!_infoRounds.equals(_existingRounds)) {
          return new RoomOpenHelper.ValidationResult(false, "rounds(com.aicolorpredict.analytics.data.local.entity.RoundEntity).\n"
                  + " Expected:\n" + _infoRounds + "\n"
                  + " Found:\n" + _existingRounds);
        }
        final HashMap<String, TableInfo.Column> _columnsPredictions = new HashMap<String, TableInfo.Column>(12);
        _columnsPredictions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("roundId", new TableInfo.Column("roundId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("epochMs", new TableInfo.Column("epochMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("modelName", new TableInfo.Column("modelName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("topPick", new TableInfo.Column("topPick", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("topProbability", new TableInfo.Column("topProbability", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("confidence", new TableInfo.Column("confidence", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("reason", new TableInfo.Column("reason", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("numberProbabilities", new TableInfo.Column("numberProbabilities", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("colorProbabilities", new TableInfo.Column("colorProbabilities", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("actualOutcome", new TableInfo.Column("actualOutcome", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPredictions.put("correct", new TableInfo.Column("correct", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPredictions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPredictions = new HashSet<TableInfo.Index>(4);
        _indicesPredictions.add(new TableInfo.Index("index_predictions_roundId", false, Arrays.asList("roundId"), Arrays.asList("ASC")));
        _indicesPredictions.add(new TableInfo.Index("index_predictions_modelName", false, Arrays.asList("modelName"), Arrays.asList("ASC")));
        _indicesPredictions.add(new TableInfo.Index("index_predictions_epochMs", false, Arrays.asList("epochMs"), Arrays.asList("ASC")));
        _indicesPredictions.add(new TableInfo.Index("index_predictions_correct", false, Arrays.asList("correct"), Arrays.asList("ASC")));
        final TableInfo _infoPredictions = new TableInfo("predictions", _columnsPredictions, _foreignKeysPredictions, _indicesPredictions);
        final TableInfo _existingPredictions = TableInfo.read(db, "predictions");
        if (!_infoPredictions.equals(_existingPredictions)) {
          return new RoomOpenHelper.ValidationResult(false, "predictions(com.aicolorpredict.analytics.data.local.entity.PredictionEntity).\n"
                  + " Expected:\n" + _infoPredictions + "\n"
                  + " Found:\n" + _existingPredictions);
        }
        final HashMap<String, TableInfo.Column> _columnsModelPerformance = new HashMap<String, TableInfo.Column>(14);
        _columnsModelPerformance.put("modelName", new TableInfo.Column("modelName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("samplesObserved", new TableInfo.Column("samplesObserved", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("top1Accuracy", new TableInfo.Column("top1Accuracy", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("top3Accuracy", new TableInfo.Column("top3Accuracy", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("top5Accuracy", new TableInfo.Column("top5Accuracy", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("logLoss", new TableInfo.Column("logLoss", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("brierScore", new TableInfo.Column("brierScore", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("precision", new TableInfo.Column("precision", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("recall", new TableInfo.Column("recall", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("f1", new TableInfo.Column("f1", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("confusionMatrixCsv", new TableInfo.Column("confusionMatrixCsv", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("rollingAccuracy", new TableInfo.Column("rollingAccuracy", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("rollingWindow", new TableInfo.Column("rollingWindow", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsModelPerformance.put("lastUpdated", new TableInfo.Column("lastUpdated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysModelPerformance = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesModelPerformance = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoModelPerformance = new TableInfo("model_performance", _columnsModelPerformance, _foreignKeysModelPerformance, _indicesModelPerformance);
        final TableInfo _existingModelPerformance = TableInfo.read(db, "model_performance");
        if (!_infoModelPerformance.equals(_existingModelPerformance)) {
          return new RoomOpenHelper.ValidationResult(false, "model_performance(com.aicolorpredict.analytics.data.local.entity.ModelPerformanceEntity).\n"
                  + " Expected:\n" + _infoModelPerformance + "\n"
                  + " Found:\n" + _existingModelPerformance);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "020b8069a6a430e02aa5c0a5fa5f6b74", "7bc8d857fe08abaa38c200a2832c3885");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "rounds","predictions","model_performance");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `rounds`");
      _db.execSQL("DELETE FROM `predictions`");
      _db.execSQL("DELETE FROM `model_performance`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(RoundDao.class, RoundDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PredictionDao.class, PredictionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ModelPerformanceDao.class, ModelPerformanceDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public RoundDao roundDao() {
    if (_roundDao != null) {
      return _roundDao;
    } else {
      synchronized(this) {
        if(_roundDao == null) {
          _roundDao = new RoundDao_Impl(this);
        }
        return _roundDao;
      }
    }
  }

  @Override
  public PredictionDao predictionDao() {
    if (_predictionDao != null) {
      return _predictionDao;
    } else {
      synchronized(this) {
        if(_predictionDao == null) {
          _predictionDao = new PredictionDao_Impl(this);
        }
        return _predictionDao;
      }
    }
  }

  @Override
  public ModelPerformanceDao modelPerformanceDao() {
    if (_modelPerformanceDao != null) {
      return _modelPerformanceDao;
    } else {
      synchronized(this) {
        if(_modelPerformanceDao == null) {
          _modelPerformanceDao = new ModelPerformanceDao_Impl(this);
        }
        return _modelPerformanceDao;
      }
    }
  }
}
