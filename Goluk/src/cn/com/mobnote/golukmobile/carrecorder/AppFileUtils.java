package cn.com.mobnote.golukmobile.carrecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import android.content.Context;
import android.content.res.AssetManager.AssetInputStream;

public class AppFileUtils {

	public static byte[] readFromFile(String fileName, int offset, int len) {
		if (fileName == null) {
			return null;
		}

		File file = new File(fileName);
		if (!file.exists()) {
			return null;
		}

		if (len == -1) {
			len = (int) file.length();
		}

		if (offset < 0) {
			return null;
		}
		if (len <= 0) {
			return null;
		}
		if (offset + len > (int) file.length()) {
			return null;
		}

		byte[] b = null;
		try {
			RandomAccessFile in = new RandomAccessFile(fileName, "r");
			b = new byte[len];
			in.seek(offset);
			in.readFully(b);
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}
	
	public static byte[] readAssertsFile(Context aContext, String filename) {
		AssetInputStream in = null;
		try {
			in = (AssetInputStream) aContext.getAssets().open(filename);
		} catch (IOException e) {
		}
		int length = 0;
		try {
			length = in.available();
		} catch (IOException e) {
		}
		byte[] b = new byte[length];
		try {
			in.read(b);
		} catch (IOException e) {
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
		return b;
	}
	
	public static void copyAssetFile(Context aContext, String srcFileName, String dstFileName) {
		InputStream is = null;
		try {
			is = aContext.getResources().getAssets().open(srcFileName);
		} catch (IOException e1) {
			return;
		}
		File dstFile = new File(dstFileName);
		try {
			if (dstFile.exists()) {
				if (!dstFile.isDirectory()) {
					dstFile.delete();
				}
			}
		} catch (Exception e) {
		}

		if (dstFileName.lastIndexOf("/") > 0) {
			File dstDir = new File(dstFileName.substring(0,
					dstFileName.lastIndexOf("/")));
			if (!dstDir.exists()) {
				dstDir.mkdirs();
			}
			dstDir = null;
		}

		try {
			dstFile.createNewFile();
		} catch (IOException e) {
			return;
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dstFile);
		} catch (FileNotFoundException e) {
			return;
		}

		byte[] buff = new byte[1024];
		int len;
		try {
			while ((len = is.read(buff)) != -1) {
				fos.write(buff, 0, len);
			}
		} catch (IOException e) {
		}
		try {
			fos.flush();
		} catch (IOException e) {
		}
		try {
			is.close();
			fos.close();
		} catch (IOException e) {
		}
		is = null;
		fos = null;
		dstFile = null;
	}

}
