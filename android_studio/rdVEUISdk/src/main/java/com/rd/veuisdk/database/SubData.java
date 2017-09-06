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
 * 字幕表
 * 
 * @author JIAN
 * 
 */
public class SubData {
	private SubData() {

	}
	final static String TABLE_NAME = "sub";
	private final static String CAPTION = "_caption";
	private final static String LOCALPATH = "_LOCAL";
	private final static String CODE = "_code";
	private final static String TIMEUNIX = "_timeunix";
	private final static String INDEX = "_index";

	/**
	 * 创建表
	 * 
	 * @param db
	 */
	public static void createTable(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + TABLE_NAME + " (" + INDEX
				+ " INTEGER PRIMARY KEY," + CODE + " TEXT NOT NULL," + CAPTION
				+ " TEXT  ," + LOCALPATH + " TEXT ," + TIMEUNIX + " LONG )";
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

	/**
	 * 字幕初始化数据库
	 */
	public void initilize(Context context) {
		root = new DatabaseRoot(context.getApplicationContext());
	}

	public SQLiteDatabase getDB() {
		return root.getWritableDatabase();
	}

	public void replace(StyleInfo info) {
		SQLiteDatabase db = root.getWritableDatabase();
		replaceSingle(info, db);
	}

	private void replaceSingle(StyleInfo info, SQLiteDatabase db) {
		ContentValues cv = new ContentValues();
		cv.put(CAPTION, info.caption);
		cv.put(CODE, info.code);
		cv.put(TIMEUNIX, info.nTime);
		cv.put(INDEX, info.index);
		if (!TextUtils.isEmpty(info.mlocalpath))
			cv.put(LOCALPATH, info.mlocalpath);
		db.replace(TABLE_NAME, INDEX + " =  " + info.index, cv);
	}

	public void replaceAll(ArrayList<StyleInfo> list) {
		int len = list.size();
		SQLiteDatabase db = root.getWritableDatabase();
		db.beginTransaction();
		for (int i = 0; i < len; i++) {
			replaceSingle(list.get(i), db);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	/**
	 * 只返回存在本地sd上的文件
	 * 
	 * @return
	 */
	public ArrayList<StyleInfo> getAll() {
		ArrayList<StyleInfo> list = new ArrayList<StyleInfo>();
		SQLiteDatabase db = root.getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, INDEX
				+ " asc ");

		if (null != c) {
			StyleInfo temp = null;
			while (c.moveToNext()) {
				temp = new StyleInfo();
				temp.index = c.getInt(0);
				temp.code = c.getString(1);
				temp.caption = c.getString(2);
				temp.mlocalpath = c.getString(3);
				temp.nTime = c.getLong(4);
				if (!TextUtils.isEmpty(temp.mlocalpath)) {

					if (FileUtils.isExist(temp.mlocalpath)) {
						// temp.icon = temp.mlocalpath + "/icon.png";
						temp.isdownloaded = true;
						list.add(temp);
					} else {// 之前下载过,但sd文件被删除
						temp.isdownloaded = false;
						temp.mlocalpath = "";
						// delete(temp.caption);
						list.add(temp);
					}
				} else {
					// temp.icon = c.getString(c.getColumnIndex(ICON));
					list.add(temp);
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
	private int delete(String url) {
		SQLiteDatabase db = root.getWritableDatabase();
		return delete(db, url);
	}

	private int delete(SQLiteDatabase db, String url) {
		return db.delete(TABLE_NAME, CAPTION + " = ?", new String[] { url });
	}

	public int deleteall(SQLiteDatabase db) {
		return db.delete(TABLE_NAME, null, null);
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
				return delete(newInfo.caption) > 0;
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
