package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.rd.veuisdk.model.WebFilterInfo;

import java.io.File;
import java.util.HashMap;

/**
 * 网络滤镜
 */
public class FilterData {
    private FilterData() {

    }

    private final static String TABLE_NAME = "filterInfo";
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
                + " TEXT  ," + LOCALPATH + " TEXT ," + NAME + " TEXT ,"
                + TIMEUNIX + " LONG  )";
        // 如果该表已存在则删除
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(sql);
    }

    private DatabaseRoot root;

    private static FilterData instance = null;

    public static FilterData getInstance() {

        if (null == instance) {
            instance = new FilterData();
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
    public synchronized long replace(WebFilterInfo info) {
        if (null != root) {
            SQLiteDatabase db = root.getWritableDatabase();
            try {
                delete(db, info.getUrl());
                ContentValues cv = new ContentValues();
                cv.put(URL, info.getUrl());
                cv.put(LOCALPATH, info.getLocalPath());
                cv.put(NAME, info.getName());
                cv.put(TIMEUNIX, info.getUpdatetime());
                long re = db.replace(TABLE_NAME, URL + " =  " + info.getUrl(), cv);
                db.close();
                return re;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }


    private HashMap<String, String> maps = new HashMap<String, String>(); // 防止db频繁调用

    /**
     * 查询已下载的MV
     *
     * @param url
     * @return
     */
    public synchronized WebFilterInfo quweryOne(String url) {

        if (root == null) {
            return null;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        Cursor c = db.query(TABLE_NAME, null, URL + " = ? ",
                new String[]{url}, null, null, null);
        try {
            WebFilterInfo info = null;
            if (null != c && !c.isClosed()) {
                if (c.moveToFirst()) {
                    info = new WebFilterInfo(c.getString(c.getColumnIndex(URL)),
                            "", c.getString(c.getColumnIndex(NAME)),
                            c.getString(c.getColumnIndex(LOCALPATH)),
                            c.getLong(c.getColumnIndex(TIMEUNIX)));

                    if (!TextUtils.isEmpty(info.getLocalPath())) {
                        File file = new File(info.getLocalPath());
                        if (null == file || !file.exists()) {
                            info.setLocalPath("");
                        }
                    }

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

    private synchronized int delete(SQLiteDatabase db, String url) {
        return db.delete(TABLE_NAME, URL + " = ?", new String[]{url});
    }

    /**
     * 删除已下载的记录
     *
     * @param url
     * @return
     */
    public synchronized int delete(String url) {
        SQLiteDatabase db = root.getWritableDatabase();
        int re = delete(db, url);
        db.close();
        return re;
    }


    /**
     * 关闭数据库连接
     */
    public synchronized void close() {
        if (null != root) {
            root.close();
            root = null;
        }
        instance = null;
        maps.clear();
    }
}
