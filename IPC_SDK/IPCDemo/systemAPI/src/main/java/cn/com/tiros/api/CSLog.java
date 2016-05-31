package cn.com.tiros.api;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.text.format.Time;

public class CSLog {
	// private static Context context;
	// private static String tag = "CSLog";
	private static String path = "NaviDogLog.txt";
	private static FileOutputStream os;
	private static byte[] newlineBytes;
	private static boolean isWriteToFile = true;

	private static File mFile = null;

	public static void init(Context context) {

		newlineBytes = "\r\n".getBytes();
		try {
			mFile = new File(Environment.getExternalStorageDirectory(), path);
			os = new FileOutputStream(mFile, true);
			os.write(newlineBytes);
			byte[] buffer = getCurrentTime().getBytes();
			os.write(buffer);
			os.write(newlineBytes);
			os.flush();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		mFile = null;
		if (os != null) {
			try {
				os.close();
				os = null;
			} catch (IOException e) {
			}
		}
	}

	public static void i(String msg) {
		if (isWriteToFile && os != null) {
			try {
				os.write(msg.getBytes());
				os.write(newlineBytes);
				os.flush();
			} catch (IOException e) {
			}
		}

	}

	public static void w(String msg) {
		if (isWriteToFile && os != null) {
			try {
				os.write(msg.getBytes());
				os.write(newlineBytes);
				os.flush();
			} catch (IOException e) {
			}
		}
	}

	public static void e(String msg) {

		try {

			mFile = new File(Environment.getExternalStorageDirectory(), path);

			os = new FileOutputStream(mFile, true);
			
			
			os.write(msg.getBytes());

			os.write("\r\n".getBytes());

			os.flush();

		} catch (IOException e1) {
		}

//		if (os != null) {
//			try {
//				os.close();
//				os = null;
//			} catch (IOException e) {
//			}
//		}
		mFile = null;

	}

	public static void close() {
		if (os != null) {
			try {
				os.close();
				os = null;
			} catch (IOException e) {
			}
		}
	}

	public static String getCurrentTime() {
		StringBuffer buffer = new StringBuffer();
		Time t = new Time();
		t.setToNow();
		buffer.append(" ");

		buffer.append(t.year);
		buffer.append("-");
		buffer.append(t.month + 1);
		buffer.append("-");
		buffer.append(t.monthDay);
		buffer.append(" ");
		buffer.append(t.hour);
		buffer.append(":");
		buffer.append(t.minute);
		buffer.append(":");
		buffer.append(t.second);

		return new String(buffer);
	}

}
