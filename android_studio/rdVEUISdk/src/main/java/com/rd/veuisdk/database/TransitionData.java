package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rd.veuisdk.model.TransitionInfo;
import com.rd.veuisdk.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 网络转场
 */
public class TransitionData {
    private TransitionData() {

    }

    private final static String TABLE_NAME = "transitionInfo";
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

    private static TransitionData instance = null;

    public static TransitionData getInstance() {

        if (null == instance) {
            instance = new TransitionData();
        }
        return instance;
    }

    public void initilize(Context context) {
        root = new DatabaseRoot(context.getApplicationContext());
    }


    public long replace(TransitionInfo info) {
        if (root == null) {
            return -1;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        if (null == db) {
            return -1;
        }
        ContentValues cv = new ContentValues();
        cv.put(URL, info.getUrl());
        cv.put(LOCALPATH, info.getLocalPath());
        cv.put(NAME, info.getName());
        cv.put(TIMEUNIX, info.getUpdatetime());
        try {
            db.delete(TABLE_NAME, URL + " = ? ", new String[]{info.getUrl()});
            long re = db.replace(TABLE_NAME, URL + " =  " + info.getUrl(), cv);
            db.close();
            return re;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * 查询已下载
     *
     * @param url
     * @return
     */
    public synchronized TransitionInfo quweryOne(String url) {

        if (root == null) {
            return null;
        }
        SQLiteDatabase db = root.getReadableDatabase();
        if (null == db) {
            return null;
        }
        Cursor c = db.query(TABLE_NAME, null, URL + " = ? ",
                new String[]{url}, null, null, null);
        try {
            TransitionInfo info = null;
            if (null != c && !c.isClosed()) {
                if (c.moveToFirst()) {
                    info = readLine(c);
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

    private TransitionInfo readLine(Cursor c) {
        TransitionInfo info = new TransitionInfo(c.getString(c.getColumnIndex(URL)),
                "", c.getString(c.getColumnIndex(NAME)),
                c.getString(c.getColumnIndex(LOCALPATH)),
                c.getLong(c.getColumnIndex(TIMEUNIX)));
        if (!FileUtils.isExist(info.getLocalPath())) {
            //文件不存在
            info.setLocalPath("");
        }
        return info;
    }


    public synchronized List<TransitionInfo> queryAll() {
        if (root == null) {
            return null;
        }
        List<TransitionInfo> list = new ArrayList<>();
        SQLiteDatabase db = root.getReadableDatabase();
        if (null == db) {
            return null;
        }
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
        try {
            TransitionInfo info = null;
            if (null != c && !c.isClosed()) {
                while (c.moveToNext()) {
                    info = readLine(c);
                    list.add(info);
                }
                c.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;

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
    }
}
