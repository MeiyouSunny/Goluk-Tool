package com.goluk.crazy.panda.ipc.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.goluk.crazy.panda.common.application.CPApplication;
import com.goluk.crazy.panda.ipc.database.table.TableAlbum;
import com.j256.ormlite.android.AndroidDatabaseConnection;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TABLE_NAME = "crazy-panda.db";
    private static final int TABLE_VERSION = 1;

    private Map<String, Dao> daos = new HashMap<String, Dao>();

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, TABLE_VERSION);
    }

    /**
     * 单例获取该Helper
     *
     * @return
     */
    public static synchronized DatabaseHelper getHelper() {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null)
                    instance = new DatabaseHelper(CPApplication.getApp());
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, TableAlbum.class);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, TableAlbum.class, true);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized Dao getDao(Class clazz) throws SQLException {
        Dao dao = null;
        String className = clazz.getSimpleName();

        if (daos.containsKey(className)) {
            dao = daos.get(className);
        }
        if (dao == null) {
            try {
                dao = super.getDao(clazz);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
            daos.put(className, dao);
        }
        return dao;
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        super.close();
        for (String key : daos.keySet()) {
            Dao dao = daos.get(key);
            dao = null;
        }
    }

}