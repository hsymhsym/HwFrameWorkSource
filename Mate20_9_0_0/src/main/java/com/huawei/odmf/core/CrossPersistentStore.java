package com.huawei.odmf.core;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import com.huawei.odmf.database.AndroidSQLiteDatabase;
import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.database.ODMFSQLiteDatabase;
import com.huawei.odmf.exception.ODMFException;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.exception.ODMFRuntimeException;
import com.huawei.odmf.exception.ODMFSQLiteDatabaseCorruptException;
import com.huawei.odmf.exception.ODMFSQLiteDiskIOException;
import com.huawei.odmf.exception.ODMFXmlParserException;
import com.huawei.odmf.model.api.ObjectModelFactory;
import com.huawei.odmf.store.AndroidDatabaseHelper;
import com.huawei.odmf.store.DatabaseHelper;
import com.huawei.odmf.store.ODMFDatabaseHelper;
import com.huawei.odmf.utils.LOG;
import java.io.File;
import java.io.IOException;
import java.util.List;

class CrossPersistentStore extends PersistentStore {
    private Context context;
    private DatabaseHelper databaseHelper;
    private List<String> databasePaths;
    private DataBase db;

    CrossPersistentStore(Context appCtx, String modelPath, String uri, Configuration configuration, List<String> databasePaths, List<byte[]> keyList) {
        ODMFException e;
        super(configuration.getPath(), configuration.getDatabaseType(), configuration.getStorageMode(), uri);
        if (modelPath == null || modelPath.equals("")) {
            this.model = null;
        } else {
            try {
                this.model = ObjectModelFactory.parse(appCtx, modelPath);
            } catch (ODMFIllegalArgumentException e2) {
                e = e2;
                LOG.logE("create mObjectModel failed!!");
                throw new ODMFRuntimeException("Xml parser failed : " + e.getMessage());
            } catch (ODMFXmlParserException e3) {
                e = e3;
                LOG.logE("create mObjectModel failed!!");
                throw new ODMFRuntimeException("Xml parser failed : " + e.getMessage());
            }
        }
        this.databasePaths = databasePaths;
        this.context = appCtx;
        String databaseName = configuration.getPath();
        if (databaseName == null || databaseName.equals("")) {
            if (this.model != null) {
                databaseName = this.model.getDatabaseName();
            } else {
                databaseName = null;
            }
        }
        this.path = databaseName;
        if (configuration.getStorageMode() == Configuration.CONFIGURATION_STORAGE_MODE_MEMORY) {
            databaseName = null;
        }
        init(databaseName, null, configuration.isThrowException(), configuration.isDetectDelete());
        if (!attachDatabases(keyList)) {
            this.db.close();
            throw new ODMFRuntimeException("error happens when attaching database");
        }
    }

    private boolean attachDatabases(List<byte[]> keyList) {
        int j;
        int size = this.databasePaths.size();
        int i = 0;
        while (i < size) {
            if (this.databasePaths.get(i) == null || ((String) this.databasePaths.get(i)).equals("")) {
                throw new ODMFIllegalArgumentException("The database which you want to attached may be a memory database.");
            }
            File file = this.context.getDatabasePath((String) this.databasePaths.get(i));
            if (file.exists()) {
                try {
                    this.db.addAttachAlias((String) this.databasePaths.get(i), file.getCanonicalPath(), (byte[]) keyList.get(i));
                    i++;
                } catch (IOException e) {
                } catch (SQLException e2) {
                }
            } else {
                for (j = i - 1; j >= 0; j++) {
                    this.db.removeAttachAlias((String) this.databasePaths.get(j));
                }
                LOG.logE("error happens when attaching database!!");
                throw new ODMFIllegalStateException("The database " + ((String) this.databasePaths.get(i)) + " you want to attached does not exist.");
            }
        }
        return true;
        LOG.logE("error happens when attaching database!!");
        for (j = 0; j < i; j++) {
            this.db.removeAttachAlias((String) this.databasePaths.get(j));
        }
        return false;
    }

    private void init(String databaseName, byte[] key, boolean throwException, boolean detectDelete) {
        try {
            if (getDatabaseType() == Configuration.CONFIGURATION_DATABASE_ANDROID) {
                this.databaseHelper = new AndroidDatabaseHelper(this.context, databaseName, getModel());
                this.db = new AndroidSQLiteDatabase(((AndroidDatabaseHelper) this.databaseHelper).getWritableDatabase());
            } else if (getDatabaseType() == Configuration.CONFIGURATION_DATABASE_ODMF) {
                this.databaseHelper = new ODMFDatabaseHelper(this.context, databaseName, getModel(), throwException, detectDelete);
                if (key != null && key.length > 0) {
                    this.databaseHelper.setDatabaseEncrypted(key);
                }
                this.db = new ODMFSQLiteDatabase(((ODMFDatabaseHelper) this.databaseHelper).getWritableDatabase());
            } else {
                throw new ODMFRuntimeException("configuration of database is wrong");
            }
            clearKey(key);
        } catch (SQLiteException e) {
            throw new ODMFRuntimeException("errors happens when initializing database");
        } catch (Throwable th) {
            clearKey(key);
        }
    }

    protected void executeRawSQL(String sql) {
        SQLException e;
        try {
            this.db.execSQL(sql);
        } catch (SQLiteException e2) {
            e = e2;
            LOG.logE("Execute SQL Failed : A SQLiteException occurred when execute SQL");
            throw new ODMFRuntimeException("Save Failed : " + e.getMessage());
        } catch (com.huawei.hwsqlite.SQLiteException e3) {
            e = e3;
            LOG.logE("Execute SQL Failed : A SQLiteException occurred when execute SQL");
            throw new ODMFRuntimeException("Save Failed : " + e.getMessage());
        } catch (IllegalStateException e4) {
            LOG.logE("Execute rawSQL failed : A IllegalStateException occurred when execute SQL.");
            throw new ODMFRuntimeException("Execute rawSQL failed : " + e4.getMessage(), e4);
        }
    }

    protected Cursor query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        SQLException e;
        try {
            return DatabaseQueryService.query(this.db, distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        } catch (SQLiteException e2) {
            e = e2;
        } catch (com.huawei.hwsqlite.SQLiteException e3) {
            e = e3;
        } catch (IllegalStateException e4) {
            LOG.logE("Execute rawSQL failed : A IllegalStateException occurred when execute SQL.");
            throw new ODMFRuntimeException("Execute rawSQL failed : " + e4.getMessage(), e4);
        }
        LOG.logE("Execute RawSQL Failed : A SQLiteException occurred when execute SQL.");
        throw new ODMFRuntimeException("Execute RawSQL Failed : " + e.getMessage());
    }

    protected Cursor executeRawQuerySQL(String sql) {
        SQLException e;
        try {
            return this.db.rawQuery(sql, null);
        } catch (SQLiteException e2) {
            e = e2;
        } catch (com.huawei.hwsqlite.SQLiteException e3) {
            e = e3;
        } catch (IllegalStateException e4) {
            LOG.logE("Execute rawQuerySQL failed : A IllegalStateException occurred when execute rawQuery.");
            throw new ODMFRuntimeException("Execute rawQuerySQL failed : " + e4.getMessage(), e4);
        }
        LOG.logE("Raw Query Failed : A SQLiteException occurred when execute rawQuery");
        throw new ODMFRuntimeException("Save Failed : " + e.getMessage());
    }

    protected void close() {
        SQLException e;
        try {
            this.databaseHelper.close();
        } catch (SQLiteDatabaseCorruptException e2) {
            e = e2;
            LOG.logE("Close database failed : A SQLiteDatabaseCorruptException occurred when close.");
            throw new ODMFSQLiteDatabaseCorruptException("Close database failed : " + e.getMessage(), e);
        } catch (com.huawei.hwsqlite.SQLiteDatabaseCorruptException e3) {
            e = e3;
            LOG.logE("Close database failed : A SQLiteDatabaseCorruptException occurred when close.");
            throw new ODMFSQLiteDatabaseCorruptException("Close database failed : " + e.getMessage(), e);
        } catch (SQLiteDiskIOException e4) {
            e = e4;
            LOG.logE("Close database failed : A SQLiteDiskIOException occurred when close.");
            throw new ODMFSQLiteDiskIOException("Close database failed : " + e.getMessage(), e);
        } catch (com.huawei.hwsqlite.SQLiteDiskIOException e5) {
            e = e5;
            LOG.logE("Close database failed : A SQLiteDiskIOException occurred when close.");
            throw new ODMFSQLiteDiskIOException("Close database failed : " + e.getMessage(), e);
        } catch (RuntimeException e6) {
            LOG.logE("Close database failed : A RuntimeException occurred when close.");
            throw new ODMFRuntimeException("Close database failed : " + e6.getMessage(), e6);
        }
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }
}
