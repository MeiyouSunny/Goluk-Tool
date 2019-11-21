package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.rd.veuisdk.model.StyleInfo;
import com.rd.veuisdk.utils.FileUtils;

import java.util.ArrayList;

/**
 * 字幕表
 *
 * @author JIAN
 */
public class SubData {
    private SubData() {

    }

    private final static String TABLE_NAME = "sub";
    private final static String CAPTION = "_caption";
    private final static String LOCALPATH = "_LOCAL";
    private final static String ICON = "_icon";
    private final static String CODE = "_code";
    private final static String TIMEUNIX = "_timeunix";
    private final static String BUSECUSTOMAPI = "_customApi"; //是否使用的自定义的api ，0 未使用 ，1 使用
    private final static String INDEX = "_index";

    /**
     * 创建表
     *
     * @param db
     */
    public static void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + INDEX
                + " INTEGER PRIMARY KEY," + CODE + " TEXT NOT NULL," + CAPTION
                + " TEXT  ," + LOCALPATH + " TEXT ," + TIMEUNIX + " LONG ," + BUSECUSTOMAPI + " INTEGER ," + ICON + " TEXT  )";
        // 如果该表已存在则删除
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(sql);
    }

    private DatabaseRoot root;

    private static SubData instance = null;

    public static SubData getInstance() {

        if (null == instance) {
            instance = new SubData();
        }
        return instance;
    }

    private Context mContext;

    /**
     * 字幕初始化数据库
     */
    public void initilize(Context context) {
        mContext = context.getApplicationContext();
        root = new DatabaseRoot(mContext);
    }

    private final String TAG = "SubData";

    private SQLiteDatabase getDB() {
        if (null == root) {
            root = new DatabaseRoot(mContext);
        }
        try {
            return root.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getDB: " + mContext);
        }
        return null;
    }

    public void replace(StyleInfo info) {
        replaceSingle(info, getDB());
    }

    private synchronized void replaceSingle(StyleInfo info, SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put(CAPTION, info.caption);
        cv.put(CODE, info.code);
        cv.put(TIMEUNIX, info.nTime);
        cv.put(BUSECUSTOMAPI, info.isbUseCustomApi() ? 1 : 0);
        cv.put(INDEX, info.index);
        cv.put(ICON, info.icon);
        if (!TextUtils.isEmpty(info.mlocalpath)) {
            cv.put(LOCALPATH, info.mlocalpath);
        }
        if (null != db) {
            db.replace(TABLE_NAME, INDEX + " =  " + info.index, cv);
        }
    }

    public synchronized void replaceAll(ArrayList<StyleInfo> list) {
        int len = list.size();
        SQLiteDatabase db = getDB();
        if (null != db) {
            db.beginTransaction();
            for (int i = 0; i < len; i++) {
                replaceSingle(list.get(i), db);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    /**
     * 只返回存在本地sd上的文件
     *
     * @return
     */
    public synchronized ArrayList<StyleInfo> getAll(boolean bCustomApi) {
        ArrayList<StyleInfo> list = new ArrayList<StyleInfo>();
        SQLiteDatabase db = getDB();
        if (null != db) {
            Cursor c = db.query(TABLE_NAME, null, BUSECUSTOMAPI + " = ? ", new String[]{Integer.toString(bCustomApi ? 1 : 0)}, null, null, INDEX
                    + " asc ");
            if (null != c) {
                while (c.moveToNext()) {
                    StyleInfo tmp = new StyleInfo(bCustomApi, true);
                    tmp.index = c.getInt(0);
                    tmp.code = c.getString(1);
                    tmp.caption = c.getString(2);
                    tmp.mlocalpath = c.getString(3);
                    tmp.nTime = c.getLong(4);
                    tmp.icon = c.getString(6);
                    //同一个 接口类型的
                    if (FileUtils.isExist(tmp.mlocalpath)) {
                        tmp.isdownloaded = true;
                        list.add(tmp);
                    } else {// 之前下载过,但sd文件被删除
                        tmp.isdownloaded = false;
                        tmp.mlocalpath = "";
                        list.add(tmp);
                    }
                }
            }
        }
        return list;
    }

    /**
     * delete by caption
     *
     * @param url
     * @return
     */
    private synchronized int delete(String url, long upTime) {
        SQLiteDatabase db = getDB();
        return delete(db, url, upTime);
    }

    /**
     * @param db
     * @param url
     * @param updateTime
     * @return
     */
    private synchronized int delete(SQLiteDatabase db, String url, long updateTime) {
        if (null != db) {
            return db.delete(TABLE_NAME, CAPTION + " = ? and " + TIMEUNIX + " = ? ", new String[]{url, Long.toString(updateTime)});
        }
        return -1;

    }

    /**
     * 验证本地资源是否为服务器最新资源
     *
     * @param newInfo
     * @param dbInfo
     * @return
     */
    public boolean checkDelete(StyleInfo newInfo, StyleInfo dbInfo) {
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
    public void close() {
        if (null != root) {
            root.close();
            root = null;
        }
        instance = null;
    }

}
