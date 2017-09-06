package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rd.veuisdk.model.MVWebInfo;

import java.util.ArrayList;
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
    private final static String CODE = "_code";
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
                + " INTEGER PRIMARY KEY," + CODE + " TEXT NOT NULL," + URL
                + " TEXT  ," + LOCALPATH + " TEXT ," + NAME + " TEXT ,"
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

    public DatabaseRoot getDataBaseRoot() {
        return root;
    }

    public void replaceAll(ArrayList<MVWebInfo> list) {
        SQLiteDatabase db = root.getWritableDatabase();
        db.beginTransaction();
        int len = list.size();
        MVWebInfo info;
        // for (int i = 0; i < len; i++) {
        // info = list.get(i);
        // ContentValues cv = new ContentValues();
        // cv.put(URL, info.getUrl());
        // cv.put(CODE, info.code);
        // cv.put(INDEX, info.index);
        // cv.put(LOCALPATH, info.local_path);
        // cv.put(TIMEUNIX, info.timeunix);
        // db.replace(TABLE_NAME, URL + " =  " + info.url, cv);
        // }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public long replace(MVWebInfo info) {
        // Log.e("replace", info.getLocalPath() + "--" + info.getUrl());
        SQLiteDatabase db = root.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(URL, info.getUrl());
        cv.put(LOCALPATH, info.getLocalPath());
        cv.put(CODE, info.getUrl());
        cv.put(NAME, info.getName());
        // cv.put(INDEX, info.index);

        cv.put(TIMEUNIX, System.currentTimeMillis());
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
        SQLiteDatabase db = root.getWritableDatabase();
        Cursor c = db.query(TABLE_NAME, null, URL + " = ? ",
                new String[]{url}, null, null, null);
        try {
            MVWebInfo info = null;
            if (null != c && !c.isClosed()) {
                if (c.moveToFirst()) {
                    info = new MVWebInfo(c.getString(c.getColumnIndex(URL)),
                            "", c.getString(c.getColumnIndex(NAME)),
                            c.getString(c.getColumnIndex(LOCALPATH)));
                }
                c.close();
            }
            db.close();


            return info;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    private int delete(SQLiteDatabase db, String url) {
        return db.delete(TABLE_NAME, URL + " = ?", new String[]{url});
    }

    /**
     * delete by caption
     *
     * @param url
     * @return
     */
    private int delete(String url) {
        SQLiteDatabase db = root.getWritableDatabase();
        return delete(db, url);
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
