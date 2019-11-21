package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.rd.veuisdk.model.EffectFilterInfo;
import com.rd.veuisdk.model.IApiInfo;
import com.rd.veuisdk.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 网络特效 (已下载的)
 */
public class EffectData {
    private EffectData() {

    }

    private final static String TABLE_NAME = "effectinfo";
    private final static String URL = "_url";
    private final static String LOCALPATH = "_local";
    private final static String TIMEUNIX = "_timeunix";
    private final static String INDEX = "_index";
    private final static String NAME = "_name";
    private final static String TYPE = "_type";

    /**
     * 创建表
     *
     * @param db
     */
    public static void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + INDEX
                + " INTEGER PRIMARY KEY," + URL
                + " TEXT  ," + LOCALPATH + " TEXT ," + NAME + " TEXT ," + TYPE + " TEXT ,"
                + TIMEUNIX + " LONG  )";
        // 如果该表已存在则删除
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(sql);
    }

    private DatabaseRoot root;

    private static volatile EffectData instance = null;

    public static EffectData getInstance() {

        if (null == instance) {
            synchronized (EffectData.class) {
                if (null == instance) {
                    instance = new EffectData();
                }
            }
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
    public long replace(IApiInfo info) {
        if (null != root) {
            SQLiteDatabase db = root.getWritableDatabase();
            try {
                delete(db, info.getFile());
                ContentValues cv = new ContentValues();
                cv.put(URL, info.getFile());
                cv.put(LOCALPATH, info.getLocalPath());
                cv.put(NAME, info.getName());
                cv.put(TIMEUNIX, info.getUpdatetime());
                cv.put(TYPE, ((EffectFilterInfo) info).getType());
                long re = db.replace(TABLE_NAME, URL + " =  " + info.getFile(), cv);
                db.close();
                return re;
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        return -1;
    }


    /**
     * 查询已下载的
     *
     * @param url
     * @return
     */
    public synchronized EffectFilterInfo quweryOne(String url) {

        if (root == null) {
            return null;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        Cursor c = db.query(TABLE_NAME, null, URL + " = ? ",
                new String[]{url}, null, null, null);
        try {
            EffectFilterInfo info = null;
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

    private synchronized EffectFilterInfo readLine(Cursor c) {
        EffectFilterInfo info = new EffectFilterInfo(c.getString(c.getColumnIndex(NAME)), c.getString(c.getColumnIndex(URL)), "",
                c.getLong(c.getColumnIndex(TIMEUNIX)), c.getString(c.getColumnIndex(TYPE)));
        info.setLocalPath(c.getString(c.getColumnIndex(LOCALPATH)));
        if (!FileUtils.isExist(info.getLocalPath())) {
            info.setLocalPath("");
        }
        return info;
    }


    public synchronized List<EffectFilterInfo> queryAll() {
        if (root == null) {
            return null;
        }
        List<EffectFilterInfo> list = new ArrayList<>();
        SQLiteDatabase db = root.getWritableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);
        try {
            EffectFilterInfo info = null;
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

    private int delete(SQLiteDatabase db, String url) {
        return db.delete(TABLE_NAME, URL + " = ?", new String[]{url});
    }

    /**
     * 删除已下载的记录
     *
     * @param url
     * @return
     */
    public int delete(String url) {
        SQLiteDatabase db = root.getWritableDatabase();
        int re = delete(db, url);
        db.close();
        return re;
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
