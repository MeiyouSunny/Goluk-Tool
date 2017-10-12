package com.rd.veuisdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.rd.veuisdk.model.MusicItem;
import com.rd.veuisdk.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * 检索sd磁盘的音乐 如:(QQ音乐、天天动听、酷狗、酷我等 )
 *
 * @author JIAN
 */
public class SDMusicData {
    private SDMusicData() {

    }

    /**
     * 本地音乐信息数据表名
     */
    public static final String TABLE_NAME = "sd_music_list";
    /**
     * 本地音乐信息数据表的字段
     */
    public static final String TITLE = "_title";// 标题
    public static final String ART = "_art";// 标题
    public static final String DURATION = "_duration";// 播放时间
    public static final String PLAY_PATH = "_play_path";// 播放路径
    public static final String _ID = "_id";
    private static SDMusicData instance = null;
    private int newMusicNum, lastMusicNum;

    /**
     * 获取单件实例
     *
     * @return
     */
    public static SDMusicData getInstance() {
        if (null == instance) {
            instance = new SDMusicData();
        }
        return instance;
    }

    /**
     * 新增音乐数量
     */
    public int getNewMusicNum() {
        return newMusicNum;
    }

    private DatabaseRoot root;

    public void initilize(Context context) {
        root = new DatabaseRoot(context.getApplicationContext());
    }

    /**
     * 创建表
     *
     * @param db
     */
    public static void createTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + _ID
                + " INTEGER PRIMARY KEY ," + TITLE + " TEXT NOT NULL," + ART
                + " TEXT NOT NULL," + DURATION + " TEXT," + PLAY_PATH
                + " TEXT NOT NULL)";
        // 如果该表已存在则删除
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(sql);
    }

    /**
     * 插入一列数据
     *
     * @param mItem 音乐数据对象
     * @return
     */
    public boolean insertData(MusicItem mItem) {
        if (null == root) {
            return false;
        }
        if (isExistData(mItem)) {
            return false;
        } else {
            SQLiteDatabase db = root.getWritableDatabase();
            ContentValues values = new ContentValues();
            String title = mItem.getTitle();
            if (title.lastIndexOf(".") != -1) {
                title = title.substring(0, title.lastIndexOf("."));
            }
            int id = mItem.getPath().hashCode();
            values.put(_ID, id);
            values.put(TITLE, title);
            values.put(ART, mItem.getArt());
            values.put(DURATION, mItem.getDuration() + "");
            values.put(PLAY_PATH, mItem.getPath());
            boolean result = db.replace(TABLE_NAME, _ID + " = " + id, values) > 0;
            if (result) {
                newMusicNum++;
            }
            return result;
        }
    }

    /**
     * 删除一列数据
     *
     * @param deleteId
     * @return
     */
    public boolean deleteData(int deleteId) {
        SQLiteDatabase db = root.getWritableDatabase();
        final String deleteMusic = _ID + "=?";
        // 删除数据的条件
        final String[] deleteValues = new String[]{String.valueOf(deleteId)};
        boolean result = db.delete(TABLE_NAME, deleteMusic, deleteValues) > 0;
        return result;
    }

    /**
     * 修改一列数据
     *
     * @param updateId
     * @param mItem
     * @return
     */
    public boolean updateData(int updateId, MusicItem mItem) {
        SQLiteDatabase db = root.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TITLE, mItem.getTitle());
        values.put(ART, mItem.getArt());
        values.put(DURATION, mItem.getDuration() + "");
        values.put(PLAY_PATH, mItem.getPath());
        String updateMusic = _ID + "=?";
        // 修改数据的条件
        final String[] deleteValues = new String[]{String.valueOf(updateId)};
        boolean result = db.update(TABLE_NAME, values, updateMusic,
                deleteValues) > 0;
        return result;
    }

    /**
     * 判断数据是否存在
     *
     * @param music
     * @return
     */
    public boolean isExistData(MusicItem music) {
        SQLiteDatabase db = root.getWritableDatabase();
        // 查询表中的记录是否已存在，返回Cursor游标
        Cursor cursor = db.query(TABLE_NAME, null, PLAY_PATH + " = ?",
                new String[]{String.valueOf(music.getPath())}, null, null,
                null);
        boolean isexist = cursor.getCount() > 0 ? true : false;
        cursor.close();
        return isexist;
    }

    /**
     * 查询返回所有音乐数据
     *
     * @return
     */
    public ArrayList<MusicItem> queryAll() {
        SQLiteDatabase db = root.getReadableDatabase();
        // 查询MUSICINFO表中所有记录，返回Cursor游标
        Cursor cursor = db
                .query(TABLE_NAME, null, null, null, null, null, null);
        ArrayList<MusicItem> allMusic = new ArrayList<MusicItem>();
        if (cursor != null) {
            MusicItem mItem;
            String art;
            while (cursor.moveToNext()) {
                mItem = new MusicItem();
                mItem.setId(cursor.getInt(0));
                mItem.setTitle(cursor.getString(1));
                art = cursor.getString(2);
                if (!TextUtils.isEmpty(art) && art.contains("<unknown>")) {
                    art = "";
                }
                mItem.setArt(art);
                mItem.setDuration(cursor.getLong(3));
                mItem.setPath(cursor.getString(4));
                if (checkFileIsExists(mItem)) {
                    allMusic.add(mItem);
                }
            }
            cursor.close();
            newMusicNum = allMusic.size() - lastMusicNum;
            if (newMusicNum < 0) {
                newMusicNum = 0;
            }
            lastMusicNum = allMusic.size();
        }
        return allMusic;
    }

    /**
     * 处理文件路径发生变化的情况
     *
     * @param mItem
     */
    private boolean checkFileIsExists(MusicItem mItem) {
        if (mItem == null || mItem.getPath() == null)
            return false;
        File file = new File(mItem.getPath());
        if (!file.exists() || file.length() == 0) {
            if (file.exists() && file.length() == 0) {
                file.delete();
            }
            int position = StorageUtils.getStorageDirectory().length();
            if (mItem.getPath().length() > position) {
                String videoRootPath = mItem.getPath().substring(0, position);
                String videoFile = mItem.getPath().substring(position);
                videoRootPath = videoRootPath.endsWith("/") ? videoRootPath
                        : videoRootPath + "/";
                videoFile = videoFile.startsWith("/") ? videoFile.substring(0,
                        videoFile.length() - 1) : videoFile;
                for (String mounts : StorageUtils.getmMounts()) {
                    String mount = mounts.endsWith("/") ? mounts : mounts + "/";
                    if (!videoRootPath.equals(mount)) {
                        file = new File(mount, videoFile);
                        break;
                    }
                }
            }
            if (!file.exists() || file.length() == 0) {
                deleteData(mItem.getId());
                return false;
            }
            if (updateMusicPath(mItem.getId(), file.getAbsolutePath())
                    && mItem.getDuration() >= 1000) {
                mItem.setId(file.getAbsolutePath().hashCode());
                mItem.setPath(file.getAbsolutePath());
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 更新文件路径
     *
     * @param id
     * @param path
     */
    private boolean updateMusicPath(long id, String path) {
        SQLiteDatabase db = root.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(_ID, path.hashCode());
        contentValues.put(PLAY_PATH, path);
        try {
            boolean result = db.update(TABLE_NAME, contentValues,
                    _ID + " = ? ", new String[]{String.valueOf(id)}) > 0;
            // boolean result = db.replace(MUSICINFO_TABLE_NAME,
            // _ID + " = " + String.valueOf(id), contentValues) > 0;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 添加或替换我的视频数据
     *
     * @param item
     */
    @SuppressWarnings("unused")
    private boolean replaceMusicData(SQLiteDatabase db, MusicItem item) {
        ContentValues values = new ContentValues();
        values.put(_ID, item.getId());
        values.put(TITLE, item.getTitle());
        values.put(DURATION, item.getDuration() + "");
        values.put(PLAY_PATH, item.getPath());
        values.put(ART, item.getArt());
        return db.replace(TABLE_NAME, null, values) > 0;
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
