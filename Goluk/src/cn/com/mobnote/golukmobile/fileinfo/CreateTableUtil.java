package cn.com.mobnote.golukmobile.fileinfo;

import java.util.ArrayList;
import java.util.List;

public class CreateTableUtil {

	/** 文件信息表名 */
	public static final String T_VIDEOINFO = "t_videoinfo";

	public static final String KEY_VIDEOINFO_FILENAME = "filename";
	public static final String KEY_VIDEOINFO_TYPE = "type";
	public static final String KEY_VIDEOINFO_FILESIZE = "filesize";
	public static final String KEY_VIDEOINFO_RESOLUTION = "resolution";
	public static final String KEY_VIDEOINFO_PERIOD = "period";
	public static final String KEY_VIDEOINFO_TIMESTAMP = "timestamp";
	public static final String KEY_VIDEOINFO_PICNAME = "picname";
	public static final String KEY_VIDEOINFO_DEVICENAME = "devicename";
	public static final String KEY_VIDEOINFO_GPSNAME = "gpsname";
	public static final String KEY_VIDEOINFO_SAVETIME = "savetime";
	public static final String KEY_VIDEOINFO_RESERVE1 = "reserve1";
	public static final String KEY_VIDEOINFO_RESERVE2 = "reserve2";
	public static final String KEY_VIDEOINFO_RESERVE3 = "reserve3";
	public static final String KEY_VIDEOINFO_RESERVE4 = "reserve4";

	private static String getCreateTableHead(String tableName) {
		return "CREATE TABLE " + tableName + "(";
	}

	private static String getCreateTableEnd() {
		return ");";
	}

	private static String addPrimaryKey(List<String> keys) {
		if (null == keys || keys.size() <= 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("primary key(");
		final int size = keys.size();
		for (int i = 0; i < size; i++) {
			String value = keys.get(i);
			if (null != value && value.length() > 0) {
				sb.append(value);
				if (i != keys.size() - 1) {
					sb.append(",");
				}
			}
		}
		sb.append(")");
		return sb.toString();
	}

	private static String getStringKey(String key) {
		return " " + key + " varchar(50),";
	}

	private static String getIntKey(String key) {
		return " " + key + " int,";
	}

	private static String getLongKey(String key) {
		return " " + key + " long,";
	}

	private static String getDoubleKey(String key) {
		return " " + key + " double,";
	}

	public static String getCreateVideoTableSql() {

		List<String> primaryKeys = new ArrayList<String>();
		primaryKeys.add(KEY_VIDEOINFO_FILENAME);

		String createSql = getCreateTableHead(T_VIDEOINFO) + getStringKey(KEY_VIDEOINFO_FILENAME)
				+ getStringKey(KEY_VIDEOINFO_TYPE) + getStringKey(KEY_VIDEOINFO_FILESIZE)
				+ getStringKey(KEY_VIDEOINFO_RESOLUTION) + getStringKey(KEY_VIDEOINFO_PERIOD)
				+ getStringKey(KEY_VIDEOINFO_TIMESTAMP) + getStringKey(KEY_VIDEOINFO_PICNAME)
				+ getStringKey(KEY_VIDEOINFO_DEVICENAME) + getStringKey(KEY_VIDEOINFO_GPSNAME)
				+ getStringKey(KEY_VIDEOINFO_SAVETIME) + getStringKey(KEY_VIDEOINFO_RESERVE1)
				+ getStringKey(KEY_VIDEOINFO_RESERVE2) + getStringKey(KEY_VIDEOINFO_RESERVE3)
				+ getStringKey(KEY_VIDEOINFO_RESERVE4) + addPrimaryKey(primaryKeys) + getCreateTableEnd();
		return createSql;
	}

}
