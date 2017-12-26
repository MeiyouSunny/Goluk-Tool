
package com.mobnote.t1sp.map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.mobnote.t1sp.map.DiskLruCache.Snapshot;

/**
 * 一个管理gps轨迹的缓存类
 *
 * @author iveszhong
 */
public class TraceCacheManager {

    private static final String TAG = "CarSvc_TraceCacheManager";

    private static TraceCacheManager sIns;

    /**
     * 图片硬盘缓存核心类。
     */
    private DiskLruCache mDiskLruCache;

    public static void create(Context context, int diskCacheSize) {
        sIns = new TraceCacheManager(context, diskCacheSize);
    }

    public static void destory() {
        if (sIns != null) {
            try {
                sIns.mDiskLruCache.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    public static TraceCacheManager instance() {
        return sIns;
    }

    /**
     * @param context
     * @param diskCacheSize 本地缓存大小
     */
    private TraceCacheManager(Context context, int diskCacheSize) {
        try {
            // 获取缓存路径
            File cacheDir = getDiskCacheDir(context, "trace");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            // 创建DiskLruCache实例，初始化缓存数据
            mDiskLruCache = DiskLruCache
                    .open(cacheDir, getAppVersion(context), 1, diskCacheSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据日期获取轨迹缓存，如果无缓存，返回null,
     * 阻塞式调用，应在工作线程调用
     *
     * @param key
     * @return
     */
    public byte[] getTraceByKey(String key) {
        Log.i(TAG, "getTraceByKey:key = " + key);
        FileInputStream fileInputStream = null;
        try {
            Snapshot snapShot = mDiskLruCache.get(hashKeyForDisk(key));
            if (snapShot != null) {
                fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                int len = fileInputStream.available();
                byte data[] = new byte[len];
                fileInputStream.read(data);
                return data;
            }
        } catch (IOException e) {
            Log.i(TAG, "IOException:" + e.toString());
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 有时候会暂存一些无用的Cache
     *
     * @param key
     * @return
     */
    public void removeCache(String key) {
        try {
            mDiskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将对应的轨迹加到缓存中
     * 阻塞式调用，应在工作线程调用
     *
     * @param key
     * @param data
     * @return
     */
    public boolean putTraceToCache(String key, byte data[]) {
        Log.i(TAG, "putTraceToCache:key = " + key);
        OutputStream outputStream = null;
        DiskLruCache.Editor editor = null;
        try {
            String k = hashKeyForDisk(key);
            Snapshot snapShot = mDiskLruCache.get(k);
            if (snapShot != null) mDiskLruCache.remove(k);
            //if(snapShot == null){
            editor = mDiskLruCache.edit(k);
            outputStream = editor.newOutputStream(0);
            outputStream.write(data, 0, data.length);
            editor.commit();
            editor = null;
            return true;
            //}
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (editor != null) {
                try {
                    editor.abort();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 获取当前应用程序的版本号。
     */
    private int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 使用MD5算法对传入的key进行加密并返回。
     */
    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 根据传入的uniqueName获取硬盘缓存的路径地址。
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) && context.getExternalCacheDir() != null) {
            cachePath = context.getExternalCacheDir().getPath();
        } else if (context.getCacheDir() != null) {
            cachePath = context.getCacheDir().getPath();
        } else {
            cachePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/carcache";
        }
        return new File(cachePath + File.separator + uniqueName);
    }

}
