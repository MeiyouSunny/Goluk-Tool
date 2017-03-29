package com.mobnote.golukmain.fileinfo;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.com.tiros.debug.GolukDebugUtils;

public class GolukVideoInfoDbManager implements IVideoInfoDataFn {
	private GolukDatabaseHelper mDbHelper = null;
	private SQLiteDatabase db = null;
	private boolean isOpen = false;
	private Context mContext = null;

	private static GolukVideoInfoDbManager mInstance = new GolukVideoInfoDbManager();

	private GolukVideoInfoDbManager() {
	}

	public static synchronized GolukVideoInfoDbManager getInstance() {
		if (null != mInstance) {
			return mInstance;
		}
		mInstance = new GolukVideoInfoDbManager();
		return mInstance;
	}

	public void initDb(Context context) {
		mContext = context;
		if (isOpen) {
			return;
		}
		openDBPre();
	}

	private void openDBPre() {
		if (null == mContext) {
			return;
		}
		if (null == mDbHelper) {
			mDbHelper = new GolukDatabaseHelper(mContext);
		}
		openDb();
	}

	private void openDb() {
		new Thread() {
			public void run() {
				GolukDebugUtils.e("", "dbtest----GolukDbManager------openDb----1");
				db = mDbHelper.getWritableDatabase();
				if (null != db) {
					isOpen = true;
				}
				GolukDebugUtils.e("", "dbtest----GolukDbManager------openDb----isOpen:  " + isOpen);
			}

		}.start();
	}

	@Override
	public long addVideoInfoData(VideoFileInfoBean bean) {
		if (!isOpen) {
			openDBPre();
			return -1;
		}
		if (null == bean) {
			return -1;
		}
		ContentValues cv = beanToContentValues(bean);
		if (null == cv) {
			return -1;
		}
		long insertid = db.insert(CreateTableUtil.T_VIDEOINFO, null, cv);
		return insertid;
	}

	@Override
	public void editVideoInfoData(VideoFileInfoBean bean) {
		String sql_allFalse = "UPDATE  " + CreateTableUtil.T_VIDEOINFO + " SET "
				+ CreateTableUtil.KEY_VIDEOINFO_PICNAME + " = " + bean.picname + " WHERE "
				+ CreateTableUtil.KEY_VIDEOINFO_FILENAME + " = " + bean.filename;
		executeSQL(sql_allFalse);
	}

	@Override
	public void delVideoInfo(String fileName) {
		if (!isOpen) {
			openDBPre();
			return;
		}
		String sql_delete = " DELETE FROM " + CreateTableUtil.T_VIDEOINFO + " WHERE "
				+ CreateTableUtil.KEY_VIDEOINFO_FILENAME + " = " + fileName;

		GolukDebugUtils.e("", "dbtest----GolukDbManager------delVideoInfo----:  " + sql_delete);
		executeSQL(sql_delete);
	}

	// 4 表示精彩, 1表示循环, 2是紧急
	@Override
	public List<VideoFileInfoBean> selectAllData(String type) {
		if (!isOpen) {
			openDBPre();
			return null;
		}
		final String selectSql = "select * from " + CreateTableUtil.T_VIDEOINFO + " where "
				+ CreateTableUtil.KEY_VIDEOINFO_TYPE + "= ?";
		final String[] selectArg = { type };
		Cursor cursor = db.rawQuery(selectSql, selectArg);
		if (null == cursor) {
			return null;
		}
		List<VideoFileInfoBean> list = new ArrayList<VideoFileInfoBean>();
		while (cursor.moveToNext()) {
			try {
				// 根据列名获取列索引
				VideoFileInfoBean bean = cursorToBean(cursor);
				if (null != bean) {
					list.add(bean);
				}
			} catch (Exception e) {
			}
		}
		cursor.close();
		return list;
	}

	@Override
	public VideoFileInfoBean selectSingleData(String fileName) {
		Cursor cursor = null;
		VideoFileInfoBean bean = null;
		if (!isOpen) {
			openDBPre();
			return null;
		}
		try {
			final String selectSql = "select * from " + CreateTableUtil.T_VIDEOINFO + " where "
					+ CreateTableUtil.KEY_VIDEOINFO_FILENAME + "= ?";
			final String[] selectArg = {fileName};
			cursor = db.rawQuery(selectSql, selectArg);
			if (null == cursor || cursor.getCount() == 0) {
				if (null != cursor) {
					cursor.close();
				}
				return null;
			}
			bean = new VideoFileInfoBean();
			while (cursor.moveToNext()) {
				bean = cursorToBean(cursor);
				if (null != bean) {
					break;
				}
			}
		}finally {
			if(cursor!=null){
				cursor.close();
			}
		}
		return bean;
	}

	@Override
	public void destroy() {
		if (null != db) {
			isOpen = false;
			db.close();
		}
	}

	private void executeSQL(String sql) {
		try {
			db.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ContentValues beanToContentValues(VideoFileInfoBean bean) {
		if (null == bean) {
			return null;
		}
		ContentValues cv = new ContentValues();
		cv.put(CreateTableUtil.KEY_VIDEOINFO_FILENAME, bean.filename);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_TYPE, bean.type);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_FILESIZE, bean.filesize);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_RESOLUTION, bean.resolution);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_PERIOD, bean.period);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_TIMESTAMP, bean.timestamp);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_PICNAME, bean.picname);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_DEVICENAME, bean.devicename);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_GPSNAME, bean.gpsname);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_SAVETIME, bean.savetime);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_RESERVE1, bean.reserve1);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_RESERVE2, bean.reserve2);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_RESERVE3, bean.reserve3);
		cv.put(CreateTableUtil.KEY_VIDEOINFO_RESERVE4, bean.reserve4);

		return cv;
	}

	private VideoFileInfoBean cursorToBean(Cursor cursor) {
		if (null == cursor) {
			return null;
		}
		try {
			VideoFileInfoBean bean = new VideoFileInfoBean();
			String fileName1 = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_FILENAME));
			String fileType = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_TYPE));
			String fileSize = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_FILESIZE));
			String resolution = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_RESOLUTION));
			String period = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_PERIOD));
			String timestamp = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_TIMESTAMP));
			String picname = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_PICNAME));
			String devicename = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_DEVICENAME));
			String gpsname = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_GPSNAME));
			String savetime = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_SAVETIME));
			String reserve1 = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_RESERVE1));
			String reserve2 = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_RESERVE2));
			String reserve3 = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_RESERVE3));
			String reserve4 = cursor.getString(cursor.getColumnIndex(CreateTableUtil.KEY_VIDEOINFO_RESERVE4));

			bean.filename = fileName1;
			bean.type = fileType;
			bean.filesize = fileSize;
			bean.resolution = resolution;
			bean.period = period;
			bean.timestamp = timestamp;
			bean.picname = picname;
			bean.devicename = devicename;
			bean.gpsname = gpsname;
			bean.savetime = savetime;
			bean.reserve1 = reserve1;
			bean.reserve2 = reserve2;
			bean.reserve3 = reserve3;
			bean.reserve4 = reserve4;

			return bean;
		} catch (Exception e) {

		}

		return null;
	}

}
