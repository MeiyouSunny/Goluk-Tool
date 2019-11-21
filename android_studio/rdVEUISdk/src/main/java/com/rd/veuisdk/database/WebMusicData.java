package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.rd.veuisdk.model.WebMusicInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * 网络配乐音乐下载
 *
 * @author JIAN
 */
public class WebMusicData {

    private static final String TABLE_NAME = "web_music_list";

    private static final String ID = "_m_id";
    private static final String LOCAL_PATH = "_localpath";
    private static final String URL_STRING = "_url";
    private static final String ART_NAME = "_art";
    private static final String MUSIC_NAME = "_musicname";
    private static final String MUSIC_INDEX = "_download_time";
    private static final String DURATION = "_duration";

    private WebMusicData() {

    }

    private static WebMusicData s_instance = null;

    /**
     * 获取单件实例
     *
     * @return
     */
    public static WebMusicData getInstance() {
        if (null == s_instance) {
            s_instance = new WebMusicData();
        }
        return s_instance;
    }

    /**
     * 创建表
     *
     * @param db
     */
    public static void createTable(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            String sqlString = "CREATE TABLE " + TABLE_NAME + " (" + ID
                    + " LONG PRIMARY KEY," + URL_STRING + " TEXT ,"
                    + LOCAL_PATH + "  TEXT ," + ART_NAME + " TEXT ,"
                    + MUSIC_NAME + " TEXT ," + MUSIC_INDEX
                    + " LONG DEFAULT  0 ," + DURATION + " LONG DEFAULT  0)";
            db.execSQL(sqlString);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private DatabaseRoot root;

    public void initilize(Context context) {
        root = new DatabaseRoot(context.getApplicationContext());
    }

    /**
     * 填入本机已赞列表
     *
     * @param vi
     * @return true填入成功false失败
     */
    public boolean replaceMusic(WebMusicInfo vi) {
        return replaceMusic(vi.getId(), vi.getMusicUrl(), vi.getLocalPath(),
                vi.getArtName(), vi.getMusicName(), vi.getDuration());
    }

    /**
     * xiazai wancheng
     *
     * @param mId
     * @param url
     * @param localPath
     * @param artName
     * @param musicName
     * @param duration
     * @return
     */
    public boolean replaceMusic(long mId, String url, String localPath,
                                String artName, String musicName, long duration) {
        if (null == root) {
            return false;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        if (null == db) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(ID, mId);
        values.put(URL_STRING, url);
        values.put(LOCAL_PATH, localPath);
        values.put(ART_NAME, artName);
        values.put(MUSIC_NAME, musicName);
        values.put(MUSIC_INDEX, System.currentTimeMillis());
        values.put(DURATION, duration);
        boolean result = db.replace(TABLE_NAME, null, values) > 0;
        return result;

    }

    /**
     * 查询单一的音乐，返回localpath
     *
     * @param music
     * @return
     */
    public String queryone(WebMusicInfo music) {
        if (null == root) {
            return null;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        if (null == db) {
            return null;
        }
        String localpath = null;
        Cursor c = db
                .query(TABLE_NAME, null, ID + " =  ? ",
                        new String[]{Long.toString(music.getId())}, null,
                        null, null);

        if (c != null) {
            if (c.moveToFirst()) {
                localpath = c.getString(c.getColumnIndex(LOCAL_PATH));
            }
            c.close();
        }
        return localpath;
    }

    /**
     * 删除指定音乐记录
     *
     * @param id
     */
    public boolean deleteItem(long id) {
        if (null == root) {
            return false;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        if (null == db) {
            return false;
        }
        boolean result = db.delete(TABLE_NAME, ID + " = ? ",
                new String[]{Long.toString(id)}) > 0;
        return result;
    }

    /**
     * 加载已下载的网络音乐
     *
     * @return
     */
    public ArrayList<WebMusicInfo> queryAll() {
        ArrayList<WebMusicInfo> all = new ArrayList<WebMusicInfo>();
        if (null == root) {
            return all;
        }
        SQLiteDatabase db = root.getWritableDatabase();
        if (null == db) {
            return all;
        }
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null,
                MUSIC_INDEX + " desc ");
        if (c != null) {
            WebMusicInfo music;
            while (c.moveToNext()) {
                music = new WebMusicInfo();
                getItem(music, c);
                if (checkMusicFileIsExists(music)) {
                    all.add(music);
                }
            }
            c.close();
        }
        return all;
    }

    /**
     * 加载已下载的网络音乐
     *
     * @return
     */
    public WebMusicInfo queryOne(String id) {
        if (null == root) {
            return null;
        }
        SQLiteDatabase db = root.getReadableDatabase();
        if (null == db) {
            return null;
        }
        Cursor c = db.query(TABLE_NAME, null, ID + " = ?", new String[]{id},
                null, null, null);
        WebMusicInfo music = null;
        if (c != null) {
            if (c.moveToFirst()) {
                music = new WebMusicInfo();
                getItem(music, c);

            }
            c.close();
        }
        return music;

    }

    public String queryAllIds() {

        if (null == root) {
            return null;
        }
        SQLiteDatabase db = root.getReadableDatabase();
        if (null == db) {
            return null;
        }
        StringBuffer sbBuffer = new StringBuffer();
        Cursor c = db.query(TABLE_NAME, new String[]{ID}, null, null, null,
                null, null);
        if (c != null) {

            while (c.moveToNext()) {
                sbBuffer.append(c.getLong(0) + ",");
            }
            c.close();
        }

        if (sbBuffer.length() > 1) {
            sbBuffer.deleteCharAt(sbBuffer.lastIndexOf(","));
            return sbBuffer.toString();
        }
        return "";

    }

    private void getItem(WebMusicInfo music, Cursor c) {
        music.setId(c.getLong(0));
        music.setMusicUrl(c.getString(1));
        music.setLocalPath(c.getString(2));
        music.setArtName(c.getString(3));
        music.setMusicName(c.getString(4));
        music.setDuration(c.getLong(6));

    }

    /**
     * 处理文件路径发生变化的情况
     *
     * @param music
     * @return
     */
    public boolean checkMusicFileIsExists(WebMusicInfo music) {
        if (music == null || TextUtils.isEmpty(music.getLocalPath()))
            return false;
        File file = new File(music.getLocalPath());
        if (!file.exists() || file.length() == 0) {
            if (file.exists() && file.length() == 0) {
                file.delete();
                deleteItem(music.getId());
                return false;
            }
        }
        return true;
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        if (null != root) {
            root.close();
        }
        s_instance = null;
    }

}
