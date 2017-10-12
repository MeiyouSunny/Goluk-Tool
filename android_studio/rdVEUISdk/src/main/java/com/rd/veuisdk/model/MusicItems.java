package com.rd.veuisdk.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.database.SDMusicData;
import com.rd.veuisdk.utils.AssetUtils;
import com.rd.veuisdk.utils.HanziToPinyin;
import com.rd.veuisdk.utils.PathUtils;
import com.rd.veuisdk.utils.StorageUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

@SuppressLint({"NewApi", "DefaultLocale"})
public class MusicItems extends ArrayList<MusicItem> {
    private final long serialVersionUID = 1L;
    private final String TAG = "MusicItems";
    private Context mContext;
    /**
     * 提供检索框架和元数据从一个输入媒体文件的统一接口（版本大于或等于2.3.3）
     */
    private Object mRetriever;
    private Resources m_resSystem;
    private HashSet<String> m_hsCheckPathDuplicate = new HashSet<String>(); // 检测路径重复
    /**
     * 扫描SD目录路径 天天动听允许更改下载目录，默认都在“/ttpod”下 QQ音乐播放器也允许改变，但是必须有多张SD卡
     * 百度音乐允许更改下载目录，到处都可以，使用默认路径 多米音乐播放器允许改变下载目录，但是必须有多张SD卡
     */
    private String[] defaultDirectory = {"/ttpod/", "/qqmusic/",
            "/Baidu_music/", "/DUOMI/", "/kgmusic/"};
    /**
     * 定义扫描类型
     */
    private final int defaultScan = 1, fastScan = 2, customScan = 3;
    private boolean m_isCancel;
    /**
     * 最近一次扫描路径
     */
    private String lastScanPath;
    private final File ROOT_SD_DIR;

    public MusicItems() {
        ROOT_SD_DIR = new File(StorageUtils.getStorageDirectory());
    }

    public MusicItems(Context context) {
        this();
        this.mContext = context;
    }

    /**
     * 加载音乐项(两种方式读取)
     *
     * @param scanType
     * @param scanDirectory
     * @return
     */
    @SuppressLint("DefaultLocale")
    public HashMap<Character, Integer> loadMusicItems(int scanType,
                                                      ArrayList<String> scanDirectory) {
        // 获取SD入口根目录
        if (scanType == defaultScan
                && SDMusicData.getInstance().queryAll().size() <= 0) {
            scanContentProvider();
        } else if (scanType == fastScan) {
            scanContentProvider();
            for (int i = 0; i < defaultDirectory.length; i++) {
                scanFile(new File(ROOT_SD_DIR, defaultDirectory[i]));
            }
        } else if (scanType == customScan) {
            for (int i = 0; i < scanDirectory.size(); i++) {
                if (m_isCancel) {
                    break;
                }
                try {
                    File file = new File(scanDirectory.get(i))
                            .getCanonicalFile();
                    if (isFilterDirectoryOrFile(file.getAbsolutePath())
                            || file.getAbsolutePath().equals(lastScanPath)
                            || (ROOT_SD_DIR.getParentFile().equals(
                            file.getParentFile()) && !ROOT_SD_DIR
                            .equals(file))) {
                        // || file.equals(sdRoot.getParent())
                        continue;
                    }
                    lastScanPath = file.getAbsolutePath();
                    scanFile(file);
                } catch (IOException e) {
                    Log.w(TAG, "扫描路径异常：" + e.getMessage());
                }
            }
        }
        if (!m_isCancel) {
            ArrayList<MusicItem> allMusicItems = SDMusicData.getInstance()
                    .queryAll();
            for (MusicItem musicItem : allMusicItems) {
                musicItem.setTitleSortKey(HanziToPinyin.getPinyinName(musicItem
                        .getTitle()));
                this.add(musicItem);
            }
        }

        if (mScanFileInterface != null) {
            mScanFileInterface.scanNewFileNum(m_isCancel ? 0 : SDMusicData
                    .getInstance().getNewMusicNum());
        }
        Collections.sort(this, new MusicItemsComparator());// 排序
        // 建立字母索引
        HashMap<Character, Integer> alphaIndex = new HashMap<Character, Integer>();
        for (int i = 0; i < this.size(); i++) {
            Character letter = this.get(i).getLetter();
            Character lastLetter = (i - 1) >= 0 ? (this.get(i - 1).getLetter())
                    : ' ';
            if (!lastLetter.equals(letter)) {
                alphaIndex.put(letter, i);
            }
        }
        return alphaIndex;
    }

    /**
     * 扫描内容提供者
     */
    private void scanContentProvider() {
        // ******************系统内容提供者************************
        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,// 获取媒体仓库中音频文件路径
                new String[]{MediaStore.Audio.Media.TITLE,// 标题
                        MediaStore.Audio.Media.DURATION,// 播放时间
                        MediaStore.Audio.Media.ARTIST,// 歌手名称
                        MediaStore.Audio.Media._ID,// id编号
                        MediaStore.Audio.Media.DISPLAY_NAME,// 文件名称
                        MediaStore.Audio.Media.DATA}, null, null,// 文件路径
                MediaStore.Audio.Media.TITLE_KEY);// 排序顺序的条件
        // **********音乐播放器目录（天天动听、百度音乐、QQ播放器）**********判断版本大于或等于2.3.3
        mRetriever = new MediaMetadataRetriever();

        MusicItem musicItem;
        // 游标不断移动读取下一条数据
        while (cursor.moveToNext() && !m_isCancel) {
            musicItem = new MusicItem();
            musicItem.setId(cursor.getInt(3));
            musicItem.setTitle(cursor.getString(0));
            musicItem.setPath(cursor.getString(5));
            String art = cursor.getString(2);
            if (!TextUtils.isEmpty(art) && art.contains("<unknown>")) {
                art = "";
            }
            musicItem.setArt(art);
            musicItem.setDuration(cursor.getLong(1));
            if (musicItem.getDuration() == 0) {
                readMusicInfo(musicItem, null, false);
            }
            musicItem.setExtFile(true);
            if (isMusicAvaliable(musicItem.getPath())
                    && musicItem.getDuration() >= 1000) {
                SDMusicData.getInstance().insertData(musicItem);
            }
        }
        cursor.close();// 数据库扫描完成关闭游标
    }

    /**
     * 扫描音乐文件
     *
     * @param file
     */
    private void scanFile(File file) {
        if (!file.exists() || m_isCancel) {
            return;
        }
        // 判断是否是一个标准的文件,
        if (file.isFile() && !file.isHidden()) {
            String path = null;
            try {
                path = file.getCanonicalPath().toLowerCase(Locale.getDefault());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mScanFileInterface != null) {
                mScanFileInterface.scanFilePath(path);
            }
            if (isMusicAvaliable(path)) {
                MusicItem item = new MusicItem();
                item.setTitle(file.getName());
                item.setPath(file.getAbsolutePath());
                item.setArt("");
                readMusicInfo(item, file, false);
                if (item.getDuration() >= 1000) {
                    SDMusicData.getInstance().insertData(item);
                }
            }
        } else if (file.isDirectory()) {// 文件是否是一个目录
            // 获取路径名中的所有目录中的文件，返回一个文件数组
            File[] files = file.listFiles();
            File fCheck = new File(file, ".nomedia");
            if (files != null) {
                // 先排除当前是否存在".nomedia"的情况，在
                for (int i = 0; i < files.length; i++) {
                    if (m_isCancel) {
                        return;
                    }
                    if (isFilterDirectoryOrFile(files[i].getAbsolutePath()
                            .toLowerCase())
                            || (ROOT_SD_DIR.getParentFile().equals(
                            files[i].getParentFile()) && !ROOT_SD_DIR
                            .equals(files[i]))) {
                        continue;
                    }
                    try {
                        File file2 = new File(files[i].getCanonicalPath());
                        if (file2.getAbsolutePath().equals(lastScanPath)) {
                            continue;
                        }
                        lastScanPath = file2.getAbsolutePath();
                        scanFile(file2);
                    } catch (IOException e) {
                        Log.w(TAG, "扫描路径异常：" + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 过滤文件
     *
     * @param name
     * @return
     */
    private boolean isFilterDirectoryOrFile(String name) {
        return name.indexOf(StorageUtils.getStorageDirectory() + "data/") != -1
                || name.indexOf("/sys/") != -1;
    }

    /**
     * 读取音乐信息
     *
     * @param item
     * @param fileMusic     音乐路径
     * @param bOnlyDuration 只获取音乐持续时间
     */
    private void readMusicInfo(MusicItem item, File fileMusic,
                               boolean bOnlyDuration) {
        if (null == fileMusic) {
            fileMusic = new File(item.getPath());
        }
        if (!fileMusic.exists()) {
            return;
        }
        try {
            if (null != mRetriever && fileMusic.exists()) {
                ((MediaMetadataRetriever) mRetriever).setDataSource(fileMusic
                        .getAbsolutePath());
                if (!bOnlyDuration) {
                    item.setTitle(((MediaMetadataRetriever) mRetriever)
                            .extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                    item.setTitle(handleStringGBK(item.getTitle()));
                }
                item.setDuration(Integer.parseInt(((MediaMetadataRetriever) mRetriever)
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                return;
            }
        } catch (Exception ex) {
        }
        if (!bOnlyDuration) {
            try {
                if (fileMusic.getName().indexOf('.') >= 0) {
                    try {
                        String[] arr = fileMusic.getName().split(".");
                        if (null != arr && arr.length > 0) {
                            item.setTitle(arr[0]);
                        } else {
                            item.setTitle(fileMusic.getName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        item.setTitle(fileMusic.getName());
                    }

                } else {
                    item.setTitle(fileMusic.getName());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                item.setTitle(fileMusic.getName());
            }
        }
        item.setDuration(getMusicItemDuration(fileMusic.getAbsolutePath()));
    }

    /**
     * 修正乱码
     *
     * @param value
     * @return
     */
    public String handleStringGBK(String value) {
        try {
            if (value.equals(new String(value.getBytes("ISO-8859-1"),
                    "ISO-8859-1"))) {
                return new String(value.getBytes("ISO-8859-1"), "GBK");
            }
        } catch (Exception e) {
        }
        return value;
    }

    /**
     * 音乐文件是否可用
     *
     * @param path 音乐文件路径
     * @return
     */
    private boolean isMusicAvaliable(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            return MusicItem.checkValidExtMusicFile(path) && file.exists();
        } else {
            return false;
        }
    }

    /**
     * 使用MediaPlay获取播放时长
     *
     * @param path 音乐文件的路径
     * @return 时长
     */
    private int getMusicItemDuration(String path) {
        MediaPlayer mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepare();
            return mPlayer.getDuration();
        } catch (Exception e) {
            return 0;
        } finally {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public static int getMusicItemDurations(String path) {
        MediaPlayer mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepare();
            return mPlayer.getDuration();
        } catch (Exception e) {
            return 0;
        } finally {
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * 加载内置配乐资源
     *
     * @param context
     * @return
     */
    public MusicItems loadAssetsMusic(Context context) {
        this.mContext = context;

        String re = AssetUtils.getAssetText(mContext.getAssets(),
                "soundtrack/soundtrack.json");
        if (!TextUtils.isEmpty(re)) {
            try {
                JSONArray jarr = new JSONArray(re);
                int len = jarr.length();
                JSONObject jobj;
                for (int i = 0; i < len; i++) {
                    jobj = jarr.getJSONObject(i);
                    addAssetMusic("kxp", this,
                            jobj.getString("Caption"),
                            "soundtrack/" + jobj.getString("AssetName"), i + 1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "loadAssetsMusic->Json格式异常");
            }

        } else {
            Log.e(TAG, "解析内置音乐失败");

            addAssetMusic("kxp", this, "悦动", "soundtrack/1.mp3", 1);
            addAssetMusic("kxp", this, "励志", "soundtrack/3.mp3", 2);
            addAssetMusic("kxp", this, "浪漫", "soundtrack/5.mp3", 3);
            addAssetMusic("kxp", this, "旅行", "soundtrack/6.mp3", 4);
            addAssetMusic("kxp", this, "小清新", "soundtrack/9.mp3", 5);
            addAssetMusic("kxp", this, "小资", "soundtrack/10.mp3", 6);
            addAssetMusic("kxp", this, "时尚", "soundtrack/4.mp3", 7);
            addAssetMusic("kxp", this, "回忆", "soundtrack/7.mp3", 8);
            addAssetMusic("kxp", this, "轻松", "soundtrack/2.mp3", 9);
            addAssetMusic("kxp", this, "乡村", "soundtrack/8.mp3", 10);
            addAssetMusic("kxp", this, "悠闲", "soundtrack/11.mp3", 11);

        }
        return this;
    }

    /**
     * 添加内置配乐资源到sd卡
     *
     * @param strItemName
     * @param musicItems
     * @param strCaption
     * @param strAssetName
     * @param nItemId
     */
    private void addAssetMusic(String strItemName, MusicItems musicItems,
                               final String strCaption, String strAssetName, final int nItemId) {

        m_resSystem = mContext.getResources();
        String stAssName = strAssetName;
        if (strAssetName.contains("/")) {
            stAssName = strAssetName.substring(
                    strAssetName.lastIndexOf("/") + 1, strAssetName.length());
        }

        String path = PathUtils.getAssetFileNameForSdcard(strItemName,
                stAssName);
        try {
            File f = new File(path);
            if (f.exists()) {
                long lAssetFileLength = CoreUtils.getAssetResourceLen(
                        m_resSystem.getAssets(), strAssetName);
                if (lAssetFileLength != f.length()) {
                    CoreUtils.assetRes2File(m_resSystem.getAssets(),
                            strAssetName, f.getAbsolutePath());
                }
            } else {
                CoreUtils.assetRes2File(m_resSystem.getAssets(), strAssetName,
                        f.getAbsolutePath());
            }
            if (f.exists()) {
                MusicItem musicItem = new MusicItem();
                musicItem.setAssetsName(strAssetName);
                musicItem
                        .setDuration(getMusicItemDuration(f.getAbsolutePath()));
                if (musicItem.getDuration() < 0) {
                    musicItem.setDuration(0);
                }
                musicItem.setPath(f.getAbsolutePath());
                musicItem.setTitle(strCaption);
                musicItems.add(musicItem);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加一项音乐
     */
    @Override
    public boolean add(MusicItem item) {

        if (m_hsCheckPathDuplicate.add(item.getPath())) {
            return super.add(item);
        } else {
            return false;
        }
    }

    /**
     * 清理所有
     */
    @Override
    public void clear() {
        m_hsCheckPathDuplicate.clear();
        super.clear();
    }

    /**
     * 移除某一项
     */
    @Override
    public MusicItem remove(int index) {
        MusicItem item = super.remove(index);
        m_hsCheckPathDuplicate.remove(item.getPath());
        return item;
    }

    public static class MusicItemsComparator implements Comparator<MusicItem> {
        private final Collator mCollator = Collator.getInstance();

        @Override
        public int compare(MusicItem lhs, MusicItem rhs) {
            String strLHSTitle = lhs.getTitleSortKey();
            String strRHSTitle = rhs.getTitleSortKey();
            return mCollator.compare(strLHSTitle, strRHSTitle);
        }
    }

    /**
     * 设置是否取消加载
     *
     * @param isCancel
     */
    public void setIsCancel(boolean isCancel) {
        this.m_isCancel = isCancel;
    }

    /**
     * 显示扫描文件的路径的接口
     */
    private OnShowScanFileInterface mScanFileInterface;

    /**
     * 注册显示扫描文件发路径的接口
     *
     * @param fileInterface
     */
    public void setOnShowScanFileInterface(OnShowScanFileInterface fileInterface) {
        this.mScanFileInterface = fileInterface;
    }

}
