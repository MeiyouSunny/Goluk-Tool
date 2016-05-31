package cn.com.tiros.api;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	
	public static final String TAG = DBHelper.class.getSimpleName();
	
	public static final String DB_NAME = "tirosnavidog.db";
	public static final int DB_VERSION = 1;
	
	private SQLiteDatabase mSQLite;

	public DBHelper() {
		super(Const.getAppContext(), DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	
	/**
	 * 打开数据库
	 */
	public void tr_dbopen() {
		if (mSQLite == null) {
			mSQLite = getWritableDatabase();
		}
	}
	
	/**
	 * 关闭数据库
	 */
	public void tr_dbclose() {
		if (mSQLite != null) {
			mSQLite.close();
			mSQLite = null;
		}
	}

	/**
	 * 执行sql命令
	 * 
	 * @param sql
	 */
	public boolean tr_dbexecSQL(String sql) {
		try{
			mSQLite.execSQL(sql);
		}catch(Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 执行查询
	 * @param sql
	 * @return
	 */
	public String tr_dbQuery(String sql){
		String result = "[]";
		Cursor cursor = mSQLite.rawQuery(sql, null);
		if (cursor != null && cursor.getCount() > 0) {
			result = cursorToJsonArray(cursor);
		}
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
		return result;
	}
	
	private String cursorToJsonArray(Cursor cursor) {
		try {
			StringBuffer sb = new StringBuffer("[");
			while (cursor.moveToNext()) {
				StringBuffer buf = new StringBuffer("{");
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					if (buf.length() > 1) {
						buf.append(',');
					}
					buf.append("\"" + cursor.getColumnName(i) + "\"");
					buf.append(':');
					buf.append("\"" + (cursor.getString(i) == null ? "" : cursor.getString(i)) + "\"");
				}
				buf.append('}');
				if (sb.length() > 1) {
					sb.append(',');
				}
				sb.append(buf.toString());
			}
			sb.append(']');
			return sb.toString();
		} catch (Exception e) {
			return "[]";
		}
	}
	
}
