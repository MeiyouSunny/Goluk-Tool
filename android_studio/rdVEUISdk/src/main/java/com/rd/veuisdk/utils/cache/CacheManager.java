package com.rd.veuisdk.utils.cache;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.rd.http.MD5;
import com.rd.vecore.RdVECore;
import com.rd.veuisdk.utils.ParcelableUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 微型缓存策略 （防止 部分fragment 频繁切换 频繁请求网络）
 */
public class CacheManager {
    private static class CacheInfo implements Parcelable {
        public CacheInfo(String data) {
            this.data = data;
            expiredDate = System.currentTimeMillis() + MAX_CACHE_TIME;
        }

        private long expiredDate; //过期时间
        private String data; //数据


        protected CacheInfo(Parcel in) {
            expiredDate = in.readLong();
            data = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(expiredDate);
            dest.writeString(data);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<CacheInfo> CREATOR = new Creator<CacheInfo>() {
            @Override
            public CacheInfo createFromParcel(Parcel in) {
                return new CacheInfo(in);
            }

            @Override
            public CacheInfo[] newArray(int size) {
                return new CacheInfo[size];
            }
        };
    }


    private static final int MAX_CACHE_TIME = 60 * 1000; //默认最大缓存60秒
    //max cache size 10mb
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 10;

    private volatile static CacheManager mCacheManager;
    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }


    public static CacheManager getCacheManager() {
        if (null == mCacheManager) {
            mCacheManager = new CacheManager(mContext);
        }
        return mCacheManager;
    }

    public String getKey(String text) {
        return MD5.getMD5(text);
    }


    public void close() {
        try {
            mDiskLruCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCacheManager = null;
    }


    public static final String TAG = "CacheManager";


    private static final int DISK_CACHE_INDEX = 0;

    private static final String CACHE_DIR = "okhttp3";

    private DiskLruCache mDiskLruCache;


    public static CacheManager getInstance() {
        if (mCacheManager == null) {
            synchronized (CacheManager.class) {
                if (mCacheManager == null) {
                    mCacheManager = new CacheManager(mContext.getApplicationContext());
                }
            }
        }
        return mCacheManager;
    }


    public void delete(Context context) throws Exception {
        File diskCacheDir = getDiskCacheDir(context, CACHE_DIR);
        if (mDiskLruCache != null) {
            DiskLruCache.deleteContents(diskCacheDir);
        }
    }

    private CacheManager(Context context) {
        File diskCacheDir = getDiskCacheDir(context, CACHE_DIR);
        if (!diskCacheDir.exists()) {
            boolean b = diskCacheDir.mkdirs();
            Log.d(TAG, "!diskCacheDir.exists() --- diskCacheDir.mkdirs()=" + b);
        }
        if (diskCacheDir.getUsableSpace() > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, RdVECore.getVersionCode(), 1/*一个key对应多少个文件*/, DISK_CACHE_SIZE);
                Log.d(TAG, "mDiskLruCache created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 同步设置缓存
     */
    public void putCache(String key, String value) {
        if (mDiskLruCache == null)
            return;
        OutputStream os = null;
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            os = editor.newOutputStream(DISK_CACHE_INDEX);
            os.write(ParcelableUtils.toParcelStr(new CacheInfo(value)).getBytes());
            os.flush();
            editor.commit();
            mDiskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 字符串转对象再比较有效期
     *
     * @param key
     * @param cache
     * @return
     */
    private String getCacheInfo(String key, String cache) {
        CacheInfo cacheInfo = ParcelableUtils.toParcelObj(cache, CacheInfo.CREATOR);
        if (null != cacheInfo) {
            if (cacheInfo.expiredDate > System.currentTimeMillis()) { //有效期内
                return cacheInfo.data;
            } else {
                removeCache(key);
            }
        }
        return null;
    }

    /**
     * 同步获取缓存
     */
    private String getLruCache(String key) {
        if (mDiskLruCache == null) {
            return null;
        }
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                fis = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                bos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = fis.read(buf)) != -1) {
                    bos.write(buf, 0, len);
                }
                byte[] data = bos.toByteArray();
                return new String(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 同步获取缓存
     */
    public String getCache(final String key) {
        String cache = getLruCache(key);
        return getCacheInfo(key, cache);
    }

    /**
     * 移除缓存
     */
    private boolean removeCache(String key) {
        if (mDiskLruCache != null) {
            try {
                return mDiskLruCache.remove(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取缓存目录
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath = context.getExternalCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }


}
