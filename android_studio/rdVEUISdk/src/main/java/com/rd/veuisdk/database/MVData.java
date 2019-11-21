package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.rd.veuisdk.model.MVWebInfo;

import java.io.File;
import java.util.HashMap;

/**
 * mv
 *
 * @author JIAN
 */
public class MVData {
    private MVData() {

    }

    private final static String TABLE_NAME = "mvInfo";
    private final static String URL = "_url";
    private final static String LOCALPATH = "_LOCAL";
    private final static String TIMEUNIX = "_timeunix";
    private final static String INDEX = "_index";
    private final static String NAME = "_name";

    /**
     * 创建表
     *
     * @param db
     */
    public static void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + INDEX
                + " INTEGER PRIMARY KEY," + URL
                + " TEXT   ," + LOCALPATH + " TEXT ," + NAME + " TEXT ,"
                + TIMEUNIX + " LONG  )";
        // 如果该表已存在则删除
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(sql);
    }

    private DatabaseRoot root;

    private static MVData instance = null;

    public static MVData getInstance() {

        if (null == instance) {
            instance = new MVData();
        }
        return instance;
    }

    public void initilize(Context context) {
        root = new DatabaseRoot(context.getApplicationContext());
    }

    /**
     * @param info
     * @return
     */
    public long replace(MVWebInfo info) {
        SQLiteDatabase db = root.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(URL, info.getUrl());
        cv.put(LOCALPATH, info.getLocalPath());
        cv.put(NAME, info.getName());
        cv.put(TIMEUNIX, info.getUpdatetime());
        try {
            db.delete(TABLE_NAME, URL + " = ? ", new String[]{info.getUrl()});
            return db.replace(TABLE_NAME, URL + " =  " + info.getUrl(), cv);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    private HashMap<String, String> maps = new HashMap<String, String>(); // 防止db频繁调用

    /**
     * 查询已下载的MV
     *
     * @param url
     * @return
     */
    public synchronized MVWebInfo quweryOne(String url) {
        if (root == null) {
            return null;
        }
        MVWebInfo info = null;
        try {
            SQLiteDatabase db = root.getWritableDatabase();
            Cursor c = db.query(TABLE_NAME, null, URL + " = ? ",
                    new String[]{url}, null, null, null);
            if (null != c && !c.isClosed()) {
                if (c.moveToFirst()) {
                    info = new MVWebInfo(c.getString(c.getColumnIndex(URL)),
                            "", c.getString(c.getColumnIndex(NAME)),
                            c.getString(c.getColumnIndex(LOCALPATH)));
                    info.setUpdatetime(c.getLong(c.getColumnIndex(TIMEUNIX)));
                    if (!TextUtils.isEmpty(info.getLocalPath())) {
                        File file = new File(info.getLocalPath());
                        if (null == file || !file.exists()) {
                            info.setLocalPath("");
                        }
                    }
                }
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }


    /**
     * 关闭数据库连接
     */
    public void close() {
        if (null != root) {
            root.close();
            root = null;
        }
        instance = null;
        maps.clear();
    }
}
