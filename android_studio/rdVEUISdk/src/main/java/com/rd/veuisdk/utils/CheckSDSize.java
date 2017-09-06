package com.rd.veuisdk.utils;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

/**
 * 检测SD的状态和大小的类
 * 
 * @author johnny
 * 
 */
public class CheckSDSize {

    /**
     * 判断SD是否存在
     * 
     * @return
     */
    public static boolean ExistSDCard() {
	if (Environment.getExternalStorageState().equals(
		Environment.MEDIA_MOUNTED)) {
	    return true;
	} else
	    return false;
    }

    /**
     * 查看SD卡总容量（单位MB）
     * 
     * @return
     */
    public static long getSDAllSize(String rootPath) {
	if (TextUtils.isEmpty(rootPath)) {
	    // 取得SD卡文件路径
	    File path = Environment.getExternalStorageDirectory();
	    rootPath = path.getPath();
	}
	StatFs sf = new StatFs(rootPath);
	// 获取单个数据块的大小(Byte)
	long blockSize = sf.getBlockSize();
	// 获取所有数据块数
	long allBlocks = sf.getBlockCount();
	// 返回SD卡大小
	// return allBlocks * blockSize; //单位Byte
	// return (allBlocks * blockSize)/1024; //单位KB
	return (allBlocks * blockSize) / 1024 / 1024; // 单位MB
    }

    /**
     * 查看SD卡剩余容量（单位MB）
     * 
     * @return
     */
    public static long getSDFreeSize(String rootPath) {
	if (TextUtils.isEmpty(rootPath)) {
	    // 取得SD卡文件路径
	    File path = Environment.getExternalStorageDirectory();
	    rootPath = path.getPath();
	}
	if (new File(rootPath).canWrite()) {
	    StatFs sf = new StatFs(rootPath);
	    // 获取单个数据块的大小(Byte)
	    long blockSize = sf.getBlockSize();
	    // 空闲的数据块的数量
	    long freeBlocks = sf.getAvailableBlocks();
	    // 返回SD卡空闲大小
	    // return freeBlocks * blockSize; //单位Byte
	    // return (freeBlocks * blockSize)/1024; //单位KB
	    return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
	} else {
	    return 0;
	}
    }

    /**
     * 获取SD剩余容量是否不下于当前比例
     * 
     * @param size
     *            当前小于容量值（单位MB）
     * @return
     */
    public static boolean getSDIsThanCurrentSize(String rootPath, int size) {
	if (getSDFreeSize(rootPath) > size) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * 获取SD剩余容量是否不下于200MB
     * 
     * @return
     */
    public static boolean getSDIsThanCurrentSize(String rootPath) {
	if (getSDFreeSize(rootPath) > 200) {
	    return true;
	} else {
	    return false;
	}
    }
}
