package com.mobnote.user;

import android.content.Context;
import android.os.Environment;

import com.mobnote.util.GolukFileUtils;

import java.io.File;
import java.math.BigDecimal;

import cn.com.tiros.api.Const;
import cn.com.tiros.debug.GolukDebugUtils;

public class DataCleanManage {

	public static String getTotalCacheSize(Context context) throws Exception {
		long cacheSize = getFolderSize(Const.getAppContext().getCacheDir());
		String logPath = Environment.getExternalStorageDirectory() + File.separator + GolukFileUtils.GOLUK_LOG_PATH;
		long logSize = getFolderSize(new File(logPath));
		GolukDebugUtils.i("lily", "===cacheDir====="+Const.getAppContext().getCacheDir()+"==cacheSize=="+cacheSize);
		return getFormatSize(cacheSize + logSize);
	}

	// 获取文件
	public static long getFolderSize(File file) throws Exception {
		if (!file.exists()) {
			return 0;
		}
		long size = 0;
		try {
			if(file.isDirectory()){
				size += file.length();
				File[] fileList = file.listFiles();
				for (int i = 0; i < fileList.length; i++) {
					// 如果下面还有文件
					if (fileList[i].isDirectory()) {
						size = size + getFolderSize(fileList[i]);
					} else {
						size = size + fileList[i].length();
					}
				}
			}else{
				return file.length();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}

	/**
	 * 格式化单位
	 * @param size
	 * @return
	 */
	public static String getFormatSize(double size) {
		double kiloByte = size / 1024;
		if (kiloByte < 1) {
			// return size + "Byte";
			return "0.00B";
		}

		double megaByte = kiloByte / 1024;
		if (megaByte < 1) {
			BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
			return result1.setScale(2, BigDecimal.ROUND_HALF_UP)
					.toPlainString() + "KB";
		}

		double gigaByte = megaByte / 1024;
		if (gigaByte < 1) {
			BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
			return result2.setScale(2, BigDecimal.ROUND_HALF_UP)
					.toPlainString() + "MB";
		}

		double teraBytes = gigaByte / 1024;
		if (teraBytes < 1) {
			BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
			return result3.setScale(2, BigDecimal.ROUND_HALF_UP)
					.toPlainString() + "GB";
		}
		BigDecimal result4 = new BigDecimal(teraBytes);
		return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
				+ "TB";
	}
	
	/**
     * 递归删除文件和文件夹
     * @param file
     * 
     */ 
    public static void deleteFile(File file) { 
        if (file.exists() == false) { 
//        	if (UserSetupActivity.mHandler != null){
//        		UserSetupActivity.mHandler.sendEmptyMessage(0);
//        	}
            return; 
        } else { 
            if (file.isFile()) { 
                file.delete(); 
                return; 
            } 
            if (file.isDirectory()) { 
                File[] childFile = file.listFiles(); 
                if (childFile == null || childFile.length == 0) { 
                    file.delete(); 
                    return; 
                } 
                for (File f : childFile) { 
                    deleteFile(f); 
                } 
                file.delete(); 
            } 
        } 
    }
}
