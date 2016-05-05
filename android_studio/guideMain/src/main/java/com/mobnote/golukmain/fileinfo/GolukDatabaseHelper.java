package com.mobnote.golukmain.fileinfo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cn.com.tiros.debug.GolukDebugUtils;

public class GolukDatabaseHelper extends SQLiteOpenHelper {
	/** 数据库名称 */
	public static final String DB_NAME = "db_goluk";
	/** 数据库版本号 */
	public static final int DB_VERSION = 1;

	public GolukDatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		GolukDebugUtils.e("", "dbtest----GolukDatabaseHelper------111");
	}

	private void createTable(SQLiteDatabase db) {
		final String sql_CreateTable = CreateTableUtil.getCreateVideoTableSql();
		GolukDebugUtils.e("", "dbtest----GolukDatabaseHelper------createTable----1: " + sql_CreateTable);
		executeSQL(db, sql_CreateTable);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		GolukDebugUtils.e("", "dbtest----GolukDatabaseHelper------onCreate----1");
		createTable(db);
	}

	private void executeSQL(SQLiteDatabase db, String sql) {
		try {
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 当数据库的version有升级的时候，会执行这个方法
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		GolukDebugUtils.e("", "dbtest----GolukDatabaseHelper------onUpgrade----oldVersion: " + oldVersion
				+ "   newVersion: " + newVersion);
	}

	// 当数据库的version有下降的时候，会执行这个方法
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.onDowngrade(db, oldVersion, newVersion);
		GolukDebugUtils.e("", "dbtest----GolukDatabaseHelper------onDowngrade----1");
	}
}
