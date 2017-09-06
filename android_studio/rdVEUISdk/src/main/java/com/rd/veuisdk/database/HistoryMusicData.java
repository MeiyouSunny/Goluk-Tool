package com.rd.veuisdk.database;

import java.io.File;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.rd.veuisdk.model.IMusic;
import com.rd.veuisdk.model.MusicHistory;
import com.rd.veuisdk.model.WebMusicInfo;
import com.rd.veuisdk.utils.FileLog;

/**
 * 配乐历史记录 （只保留top 10）
 * 
 * @author JIAN
 * 
 */
public class HistoryMusicData {
	public static final String TABLE_NAME = "music_history";

	private static final String ID = "_m_id";
	private static final String LOCAL_PATH = "_localpath";
	private static final String MUSIC_NAME = "_musicname";
	private static final String DURATION = "_duration";

	private HistoryMusicData() {

	}

	private static HistoryMusicData s_instance = null;

	/**
	 * 获取单件实例
	 * 
	 * @return
	 */
	public static HistoryMusicData getInstance() {
		if (null == s_instance) {
			s_instance = new HistoryMusicData();
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
					+ " LONG ," + MUSIC_NAME + " TEXT ," + DURATION
					+ " LONG DEFAULT  0, " + LOCAL_PATH + " text  PRIMARY KEY)";
			db.execSQL(sqlString);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private DatabaseRoot root;

	public void initilize(Context context) {
		if (null == root)
			root = new DatabaseRoot(context.getApplicationContext());
	}

	/**
	 * 加入历史库
	 * 
	 * @param localPath
	 * @param musicName
	 * @param duration
	 * @return
	 */
	public boolean replaceMusic(String localPath, String musicName,
			long duration) {

		SQLiteDatabase db = root.getWritableDatabase();

		String sql = "select  " + ID + " from " + TABLE_NAME + "   order by "
				+ ID + " desc limit 0,9";
		Cursor c = db.rawQuery(sql, null);

		long maxTime = 0;
		if (c != null) {
			if (c.moveToLast()) {
				maxTime = c.getLong(0);
			}

			c.close();
			if (maxTime != 0) {
				db.delete(TABLE_NAME, ID + " < " + maxTime, null);
			}
		}

		ContentValues values = new ContentValues();
		values.put(ID, System.currentTimeMillis());
		values.put(LOCAL_PATH, localPath);
		values.put(MUSIC_NAME, musicName);
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
		SQLiteDatabase db = root.getWritableDatabase();
		String localpath = null;
		Cursor c = db
				.query(TABLE_NAME, null, ID + " =  ? ",
						new String[] { Long.toString(music.getId()) }, null,
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
		SQLiteDatabase db = root.getWritableDatabase();
		return db.delete(TABLE_NAME, ID + " = ? ",
				new String[] { Long.toString(id) }) > 0;
	}

	/**
	 * 加载已下载的网络音乐
	 * 
	 * @return
	 */
	public ArrayList<IMusic> queryAll() {
		ArrayList<IMusic> all = new ArrayList<IMusic>();
		try {
			SQLiteDatabase db = root.getReadableDatabase();
			Cursor c = db.query(TABLE_NAME, null, null, null, null, null, ID
					+ " desc ");
			if (c != null) {
				MusicHistory music;
				while (c.moveToNext()) {
					music = new MusicHistory();
					getItem(music, c);
					if (checkMusicFileIsExists(music)) {
						all.add(music);
					}
				}
				c.close();
			}

		} catch (Exception e) {
			FileLog.writeLog(this.toString() + "queryAll:" + e.getMessage());
		}
		return all;
	}

	/**
	 * 加载已下载的网络音乐
	 * 
	 * @return
	 */
	public IMusic queryOne(String id) {
		MusicHistory music = null;
		try {

			SQLiteDatabase db = root.getReadableDatabase();
			Cursor c = db.query(TABLE_NAME, null, ID + " = ?",
					new String[] { id }, null, null, null);
			if (c != null) {
				if (c.moveToFirst()) {
					music = new MusicHistory();
					getItem(music, c);
				}
				c.close();
			}
		} catch (Exception e) {
			FileLog.writeLog(this.toString() + "method->queryOne:"
					+ e.getMessage());
		}
		return music;

	}

	public String queryAllIds() {
		StringBuffer sbBuffer = new StringBuffer();
		SQLiteDatabase db = root.getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, new String[] { ID }, null, null, null,
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

	private void getItem(MusicHistory music, Cursor c) {
		music.setId(c.getLong(0));
		music.setName(c.getString(1));
		music.setDuration(c.getInt(2));
		music.setPath(c.getString(3));
	}

	/**
	 * 处理文件路径发生变化的情况
	 * 
	 * @param vi
	 */
	public boolean checkMusicFileIsExists(IMusic music) {
		if (music == null || TextUtils.isEmpty(music.getPath()))
			return false;
		File file = new File(music.getPath());
		boolean exist = file.exists();
		long len = 0;
		if (!exist || (len = file.length()) == 0) {
			if (exist && len == 0) {
				file.delete();
				deleteItem(music.getId());
			}
			return false;
		}
		return true;
	}

	/**
	 * 关闭数据库连接
	 */
	public void close() {
		if (null != root) {
			root.close();
			root = null;
		}
		s_instance = null;
	}

}
