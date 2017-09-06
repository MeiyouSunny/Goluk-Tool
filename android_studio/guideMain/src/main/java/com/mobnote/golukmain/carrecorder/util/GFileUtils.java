package com.mobnote.golukmain.carrecorder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.util.EncodingUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class GFileUtils {

	/**
	 * 写文件(追加)
	 * 
	 * @param filename
	 *            文件绝对路径
	 * @param msg
	 *            写入文件的信息
	 * @author xuhw
	 * @date 2015年2月3日
	 */
	public static void writeFile(String filename, String msg) {
		try {
			FileOutputStream fos = new FileOutputStream(filename, true);
			fos.write(msg.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将图片数据保存图片
	 * 
	 * @param img
	 * @author xuhw
	 * @date 2015年2月12日
	 */
	public static void writeImageToDisk(String filename, byte[] img) {
		Bitmap mBitmap = null;
		FileOutputStream out = null;
		try {
			Bitmap b = Bytes2Bimap(img);
			if (null != b) {
				mBitmap = getZoomRotateBitmap(b, 854, 480);
				if (null != mBitmap) {
					out = new FileOutputStream(filename);
					mBitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
					out.flush();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != out) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (null != mBitmap) {
				if (!mBitmap.isRecycled()) {
					mBitmap.recycle();
					mBitmap = null;
				}
			}
		}
	}

	/**
	 * 将图片数据保存图片
	 * 
	 * @param img
	 * @author xuhw
	 * @date 2015年2月12日
	 */
	public static void compressImageToDisk(String originalFileName, String newFileName) {
		Bitmap mBitmap = null;
		FileOutputStream out = null;
		try {
			Bitmap b = BitmapFactory.decodeFile(originalFileName);
			if (null != b) {
				mBitmap = getZoomRotateBitmap(b, 854, 480);
				if (null != mBitmap) {
					out = new FileOutputStream(newFileName);
					mBitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
					out.flush();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != out) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (null != mBitmap) {
				if (!mBitmap.isRecycled()) {
					mBitmap.recycle();
					mBitmap = null;
				}
			}
		}
	}

	/**
	 * 保存原始图片
	 * 
	 * @param filename
	 * @param img
	 * @author xuhw
	 * @date 2015年3月12日
	 */
	public static void saveImageToDisk(String filename, byte[] img) {
		try {
			File file = new File(filename);
			FileOutputStream fops = new FileOutputStream(file);
			fops.write(img);
			fops.flush();
			fops.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 缩放图片
	 * 
	 * @param bmpOrg
	 *            　原始图片
	 * @param zoomWidth
	 *            缩放宽度
	 * @param zoomHeight
	 *            缩放高度
	 * @return　返回被缩放的图片
	 * @author xuhw
	 * @date 2015年3月3日
	 */
	public static Bitmap getZoomRotateBitmap(Bitmap bmpOrg, int zoomWidth, int zoomHeight) {
		int width = bmpOrg.getWidth();
		int height = bmpOrg.getHeight();

		int newWidth = zoomWidth;
		int newheight = zoomHeight;
		float sw = ((float) newWidth) / width;
		float sh = ((float) newheight) / height;
		android.graphics.Matrix matrix = new android.graphics.Matrix();
		matrix.postScale(sw, sh);
		matrix.postRotate(0);
		Bitmap resizeBitmap = Bitmap.createBitmap(bmpOrg, 0, 0, width, height, matrix, true);
		bmpOrg.recycle();
		return resizeBitmap;
	}

	/**
	 * 字节数组转Bitmap图片
	 * 
	 * @param b
	 *            　字节数组
	 * @return　图片对象
	 * @author xuhw
	 * @date 2015年3月2日
	 */
	private static Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	/**
	 * 读取文件信息
	 * 
	 * @param fileName
	 *            文件名称
	 * @return 文件信息
	 * @author xuhw
	 * @date 2015年2月12日
	 */
	public String readFile(String fileName) {
		String res = "";
		try {
			FileInputStream fin = new FileInputStream(fileName);
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			res = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 拷贝文件
	 * 
	 * @param oldFileName
	 *            源文件
	 * @param newFileName
	 *            新生成文件
	 * @author xuhw
	 * @date 2015年2月12日
	 */
	public static void copyFile(String oldFileName, String newFileName) {
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldFileName);
			if (oldfile.exists()) { // 文件存在时
				inStream = new FileInputStream(oldFileName); // 读入原文件
				fs = new FileOutputStream(newFileName);
				byte[] buffer = new byte[1024];
				int length;
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					fs.write(buffer, 0, byteread);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != inStream) {
					inStream.close();
				}
				if (null != fs) {
					fs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 创建文件夹
	 * 
	 * @param dirname
	 *            文件夹路径
	 * @author xuhw
	 * @date 2015年2月12日
	 */
	public static boolean makedir(String dirname) {
		File dir = new File(dirname);
		if (!dir.exists()) {
			return dir.mkdirs();
		}

		return false;
	}

	/**
	 * 写入截图上传日志文件
	 * 
	 * @param message
	 * @author xuhw
	 * @date 2015年2月3日
	 */
	public static void writeShootLog(String message) {
		// long time=System.currentTimeMillis();
		// SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		// String timename=format.format(new Date(time));
		//
		// String dirname = Environment.getExternalStorageDirectory() +
		// File.separator + "goluk"
		// + File.separator + "goluk_carrecorder";
		//
		// File dir = new File(dirname);
		// if(!dir.exists()){
		// dir.mkdirs();
		// }
		//
		// writeFile(dirname + File.separator
		// +"ScreenShotLog.txt",timename+"==>>>"+message+"\r\n");
	}

	/**
	 * 写入截图上传日志文件
	 * 
	 * @param message
	 * @author xuhw
	 * @date 2015年2月3日
	 */
	public static void writeCDCInterphoneCallBackLog(String message) {
		// long time=System.currentTimeMillis();
		// SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		// String timename=format.format(new Date(time));
		//
		// String dirname = Environment.getExternalStorageDirectory() +
		// File.separator + "goluk"
		// + File.separator + "goluk_carrecorder";
		//
		// File dir = new File(dirname);
		// if(!dir.exists()){
		// dir.mkdirs();
		// }
		//
		// writeFile(dirname + File.separator
		// +"CDCInterphoneCallBackLog.txt",timename+"==>>>"+message+"\r\n");
	}

	/**
	 * 写入IPC日志文件
	 * 
	 * @param message
	 * @author xuhw
	 * @date 2015年2月3日
	 */
	public static void writeIPCLog(String message) {
		// long time=System.currentTimeMillis();
		// SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		// String timename=format.format(new Date(time));
		//
		// String dirname = Environment.getExternalStorageDirectory() +
		// File.separator + "goluk"
		// + File.separator + "goluk_carrecorder";
		//
		// File dir = new File(dirname);
		// if(!dir.exists()){
		// dir.mkdirs();
		// }
		//
		// writeFile(dirname + File.separator
		// +"IPCLog.txt",timename+"==>>>"+message+"\r\n");
	}

	/**
	 * 写入IPC日志文件
	 * 
	 * @param message
	 * @author xuhw
	 * @date 2015年2月3日
	 */
	public static void writeIPCDataLog(String message) {
		// long time=System.currentTimeMillis();
		// SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		// String timename=format.format(new Date(time));
		//
		// String dirname = Environment.getExternalStorageDirectory() +
		// File.separator + "goluk"
		// + File.separator + "goluk_carrecorder";
		//
		// File dir = new File(dirname);
		// if(!dir.exists()){
		// dir.mkdirs();
		// }
		//
		// writeFile(dirname + File.separator
		// +"data_IpcLog.txt",timename+"==>>>"+message+"\r\n");
	}

}
