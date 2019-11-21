package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.utils.FileUtils;

import java.util.ArrayList;

/**
 * 贴纸表
 */
public class StickerData {
    private StickerData() {

    }

    private final static String TABLE_NAME = "special";
    private final static String CAPTION = "_caption";
    private final static String INDEX = "_index";
    private final static String LOCALPATH = "_local";
    private final static String CODE = "_code";
    private final static String TIMEUNIX = "_timeunix";
    private final static String BUSECUSTOMAPI = "_customApi"; //是否使用的自定义的api ，0 未使用 ，1 使用
    private final static String ICON = "_icon";

    /**
     * 创建表
     *
     * @param db
     */
    public static void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + INDEX
                + " INTEGER PRIMARY KEY," + CODE + " TEXT NOT NULL," + CAPTION
                + " TEXT   ," + LOCALPATH + " TEXT ," + TIMEUNIX + " LONG  ," + BUSECUSTOMAPI + " INTEGER ," + ICON + " TEXT )";
        // 如果该表已存在则删除
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(sql);
    }

    private DatabaseRoot root;

    private static StickerData instance = null;

    public static StickerData getInstance() {
        if (null == instance) {
            synchronized (StickerData.class) {
                if (null == instance) {
                    instance = new StickerData();
                }
            }
        }
        return instance;
    }

    /**
     * 特效初始化数据库
     */
    public void initilize(Context context) {
        root = new DatabaseRoot(context.getApplicationContext());
    }

    public synchronized void replace(StyleInfo info) {
        SQLiteDatabase db = root.getWritableDatabase();
        if (null != db) {
            replaceSingle(info, db);
            db.close();
        }
    }

    private synchronized void replaceSingle(StyleInfo info, SQLiteDatabase db) {
        if (null == db) {
            return;
        }
        ContentValues cv = new ContentValues();
        cv.put(CAPTION, info.caption);
        cv.put(CODE, info.code);
        cv.put(INDEX, info.index);
        cv.put(BUSECUSTOMAPI, info.isbUseCustomApi() ? 1 : 0);
        cv.put(TIMEUNIX, info.nTime);
        cv.put(ICON, info.icon);
        if (!TextUtils.isEmpty(info.mlocalpath)) {
            cv.put(LOCALPATH, info.mlocalpath);
        }
        db.replace(TABLE_NAME, INDEX + " =  " + info.index, cv);
    }

    public synchronized void replaceAll(ArrayList<StyleInfo> list) {
        if (null == root || (null == list || list.size() == 0)) {
            return;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        if (null != db) {
            db.beginTransaction();
            for (int i = 0; i < list.size(); i++) {
                replaceSingle(list.get(i), db);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }

    }

    /**
     * 只返回存在本地sd上的文件
     *
     * @param bCustomApi
     * @return
     */
    public synchronized ArrayList<StyleInfo> getAll(boolean bCustomApi) {
        ArrayList<StyleInfo> list = new ArrayList<StyleInfo>();
        if (null != root) {
            SQLiteDatabase db = root.getReadableDatabase();
            if (null != db) {
                Cursor c = db.query(TABLE_NAME, null, BUSECUSTOMAPI + " = ? ", new String[]{Integer.toString(bCustomApi ? 1 : 0)}, null, null, INDEX
                        + " asc");
                if (null != c) {
                    StyleInfo temp = null;
                    while (c.moveToNext()) {
                        temp = new StyleInfo(bCustomApi, false);
                        temp.index = c.getInt(c.getColumnIndex(INDEX));
                        temp.code = c.getString(c.getColumnIndex(CODE));
                        temp.caption = c.getString(c.getColumnIndex(CAPTION));
                        temp.mlocalpath = c.getString(c.getColumnIndex(LOCALPATH));
                        temp.nTime = c.getLong(c.getColumnIndex(TIMEUNIX));
                        temp.icon = c.getString(c.getColumnIndex(ICON));
                        temp.st = com.rd.veuisdk.utils.CommonStyleUtils.STYPE.special;

                        if (!TextUtils.isEmpty(temp.mlocalpath)) {
                            if (FileUtils.isExist(temp.mlocalpath)) {
                                temp.isdownloaded = true;
                                list.add(temp);
                            } else {
                                temp.isdownloaded = false;
                                temp.mlocalpath = "";
                                list.add(temp);
                            }
                        } else {
                            list.add(temp);
                        }
                    }
                    c.close();
                }
            }
        }
        return list;

    }

    /**
     * @param url
     * @return
     */
    private synchronized int delete(String url, long upTime) {
        SQLiteDatabase db = root.getWritableDatabase();
        if (null != db) {
            return delete(db, url, upTime);
        }
        return -1;

    }


    /**
     * @param db
     * @param url
     * @param updateTime
     * @return
     */
    private synchronized int delete(SQLiteDatabase db, String url, long updateTime) {
        return db.delete(TABLE_NAME, CAPTION + " = ? and " + TIMEUNIX + " = ? ", new String[]{url, Long.toString(updateTime)});
    }

    public synchronized int deleteall(SQLiteDatabase db) {
        return db.delete(TABLE_NAME, null, null);
    }

    /**
     * 验证本地资源是否为服务器最新资源
     *
     * @param newInfo
     * @param dbInfo
     * @return //是否已删除旧的资源
     */
    public synchronized boolean checkDelete(StyleInfo newInfo, StyleInfo dbInfo) {
        if (null != newInfo && null != dbInfo
                && newInfo.caption.equals(dbInfo.caption)) {
            if (newInfo.nTime > dbInfo.nTime) {
                return delete(newInfo.caption, dbInfo.nTime) > 0;
            }
        }
        return false;
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
    }

}
