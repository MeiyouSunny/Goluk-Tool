package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.rd.veuisdk.model.TtfInfo;
import com.rd.veuisdk.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 字体表
 *
 * @author JIAN
 */
public class TTFData {

    private final static String TABLE_NAME = "font";
    private final static String URL = "_url";
    private final static String LOCALPATH = "_LOCAL";
    private final static String CODE = "_code";
    private final static String TIMEUNIX = "_timeunix";
    private final static String INDEX = "_index";
    private final static String BUSECUSTOMAPI = "_customApi"; //是否使用的自定义的api ，0 未使用 ，1 使用
    private final static String ICON = "_icon";

    private TTFData() {

    }

    /**
     * 创建表
     *
     * @param db
     */
    public static void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + INDEX
                + " INTEGER PRIMARY KEY," + CODE + " TEXT NOT NULL," + URL
                + " TEXT  ," + LOCALPATH + " TEXT ," + TIMEUNIX + " LONG ," + BUSECUSTOMAPI + " INTEGER ," + ICON + " TEXT )";
        // 如果该表已存在则删除
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(sql);
    }

    private DatabaseRoot root;

    private static TTFData instance = null;

    public static TTFData getInstance() {

        if (null == instance) {
            instance = new TTFData();
        }
        return instance;
    }

    /**
     * 字体初始化数据库
     */
    public void initilize(Context context) {
        if (null == root) {
            root = new DatabaseRoot(context.getApplicationContext());
        }
    }

    public DatabaseRoot getDataBaseRoot() {
        return root;
    }

    public synchronized void replaceAll(ArrayList<TtfInfo> list) {
        if (null == root) {
            return;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        if (null == db) {
            return;
        }
        db.beginTransaction();
        int len = list.size();
        TtfInfo info;
        for (int i = 0; i < len; i++) {
            info = list.get(i);
            ContentValues cv = new ContentValues();
            cv.put(URL, info.url);
            cv.put(CODE, info.code);
            cv.put(ICON, info.icon);
            cv.put(BUSECUSTOMAPI, info.bCustomApi ? 1 : 0);
            cv.put(INDEX, info.index);
            cv.put(LOCALPATH, info.local_path);
            cv.put(TIMEUNIX, info.timeunix);
            db.replace(TABLE_NAME, URL + " =  " + info.url, cv);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public synchronized long replace(TtfInfo info) {
        if (null == root) {
            return -1;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        if (null == db) {
            return -1;
        }
        ContentValues cv = new ContentValues();
        cv.put(URL, info.url);
        cv.put(CODE, info.code);
        cv.put(INDEX, info.index);
        cv.put(ICON, info.icon);
        cv.put(BUSECUSTOMAPI, info.bCustomApi ? 1 : 0);
        cv.put(TIMEUNIX, info.timeunix);
        cv.put(LOCALPATH, info.local_path);
        long re = db.replace(TABLE_NAME, URL + " =  " + info.url, cv);
        db.close();
        return re;
    }

    /**
     * 只返回存在本地sd上的文件
     *
     * @param bCustomApi
     * @return
     */
    public synchronized ArrayList<TtfInfo> getAll(boolean bCustomApi) {
        ArrayList<TtfInfo> list = new ArrayList<TtfInfo>();
        if (root == null) {
            return null;
        }
        SQLiteDatabase db = root.getReadableDatabase();
        if (db == null) {
            return null;
        }
        Cursor c = db.query(TABLE_NAME, null, BUSECUSTOMAPI + " = ? ", new String[]{Integer.toString(bCustomApi ? 1 : 0)}, null, null, INDEX
                + " asc ");

        if (null != c) {
            TtfInfo temp = null;
            while (c.moveToNext()) {
                temp = new TtfInfo();
                temp.code = c.getString(c.getColumnIndex(CODE));
                temp.url = c.getString(c.getColumnIndex(URL));
                temp.timeunix = c.getLong(c.getColumnIndex(TIMEUNIX));
                temp.index = c.getInt(c.getColumnIndex(INDEX));
                temp.local_path = c.getString(c.getColumnIndex(LOCALPATH));
                temp.icon = c.getString(c.getColumnIndex(ICON));
                temp.bCustomApi = c.getInt(c.getColumnIndex(BUSECUSTOMAPI)) == 1;
                if (FileUtils.isExist(temp.local_path)) {
                    list.add(temp);
                } else {
                    temp.local_path = "";
                    list.add(temp);
                }
            }
            c.close();
        }
        return list;
    }

    private HashMap<String, String> maps = new HashMap<String, String>(); // 防止db频繁调用

    /**
     * 特效字幕需要
     *
     * @param code
     * @return 返回ttf的本地路径
     */
    public synchronized String quweryOne(String code) {

        if (maps.containsKey(code)) {
            return maps.get(code);
        }

        String mpath = null;
        if (root == null) {
            return null;
        }
        if (TextUtils.isEmpty(code)) {
            return null;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{LOCALPATH}, CODE
                + " = ? ", new String[]{code}, null, null, null);
        try {

            if (null != c && !c.isClosed()) {
                if (c.moveToFirst()) {
                    mpath = c.getString(0);
                }
                c.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(mpath)) {
            if (!FileUtils.isExist(mpath)) {
                mpath = null;
            }
        }
        maps.put(code, mpath);
        return mpath;

    }

    private synchronized int delete(SQLiteDatabase db, String url) {
        return db.delete(TABLE_NAME, URL + " = ?", new String[]{url});
    }

    /**
     * delete by url
     */
    private synchronized int delete(String url) {
        SQLiteDatabase db = root.getWritableDatabase();
        return delete(db, url);
    }

    /**
     * 验证本地资源是否为服务器最新资源
     *
     * @param newInfo
     * @param dbInfo
     * @return
     */
    public boolean checkDelete(TtfInfo newInfo, TtfInfo dbInfo) {

        if (null != newInfo && null != dbInfo && newInfo.url.equals(dbInfo.url)) {
            if (newInfo.timeunix > dbInfo.timeunix) {
                if (!TextUtils.isEmpty(dbInfo.local_path)) {
                    FileUtils.deleteAll(new File(dbInfo.local_path));
                }
                return delete(newInfo.url) > 0;
            }

        }
        return false;

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
