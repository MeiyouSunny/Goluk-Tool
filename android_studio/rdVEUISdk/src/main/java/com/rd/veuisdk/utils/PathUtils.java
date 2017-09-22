/**
 * PathUtils.java
 * classes：com.rd.utils.PathUtils
 *
 * @author abreal
 */
package com.rd.veuisdk.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.rd.vecore.utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


/**
 * 路径工具
 *
 * @author abreal<br/>
 *         <p>
 *         create at Jun 26, 2014
 */
public class PathUtils {
    private static String m_sRdRootPath;
    private static String m_sRdLogPath, m_sRdDataLogPath;
    private static String m_sRdTempPath;
    private static String m_sRdImagePath;
    private static String m_sRdVideoPath;
    private static String m_sRdAssetPath, m_sRdDownLoad, m_sRdTransitionPath;
    private static String m_sRdSpecailPath;
    private static String m_sRdSubPath;
    private static String m_sRdTtfPath;
    private static String m_sRdThemePath;
    private static String m_sRdFaceu;
    private static String m_sRdMV;
    private static String m_sRdMusic;
    /**
     * 原始数据库文件路径
     */
    private static String originalDBFilePath;

    public static String getOriginalDBFilePath() {
        return originalDBFilePath;
    }

    public static void setOriginalDBFilePath(String originalDBFilePath) {
        PathUtils.originalDBFilePath = originalDBFilePath;
    }

    /**
     * 获取本APP根目录
     *
     * @return
     */
    public static final String getRdRootPath() {
        return m_sRdRootPath;
    }

    /**
     * 获取系统临时目录
     *
     * @return
     */
    public static final String getRdTempPath() {
        return m_sRdTempPath;
    }

    /**
     * 获取视频录制目录
     *
     * @return
     */
    public static final String getRdVideoPath() {
        return m_sRdVideoPath;
    }

    /**
     * 获取图片录制目录
     *
     * @return
     */
    public static final String getRdImagePath() {
        return m_sRdImagePath;
    }

    /**
     * 字体icon
     *
     * @return
     */
    public static final String getRdTtfPath() {
        return m_sRdTtfPath;
    }

    /**
     * 获取日志目录
     *
     * @return
     */
    public static final String getRdLogPath() {
        String strResultPath;
        if (StorageUtils.isAvailable(false)) {
            strResultPath = m_sRdLogPath;
        } else {
            strResultPath = m_sRdDataLogPath;
        }
        File fileLog = new File(strResultPath);
        checkPath(fileLog);
        return strResultPath;
    }

    /**
     * 获取asset目录
     *
     * @return
     */
    public static String getRdAssetPath() {
        return m_sRdAssetPath;
    }

    public static String getRdTransitionPath() {
        return m_sRdTransitionPath;
    }

    public static String getRdSpecialPath() {
        return m_sRdSpecailPath;
    }

    public static String getRdSubPath() {
        return m_sRdSubPath;
    }

    public static String getRdThemePath() {
        return m_sRdThemePath;
    }

    public static String getRdFaceuPath() {
        return m_sRdFaceu;
    }

    public static String getRdMVPath() {
        return m_sRdMV;
    }

    public static String getRdMusic() {
        return m_sRdMusic;
    }


    /**
     * 解析文件存储路径
     *
     * @param context
     * @param path    根文件夹目录
     * @throws IllegalAccessException
     */
    @SuppressLint("NewApi")
    public static void initialize(Context context, File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPermission = context
                    .checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
                throw new IllegalAccessError(
                        "Can`t get READ_EXTERNAL_STORAGE permission. ");
            }
        }
        StorageUtils.getAllStorageLocations(context);
        if (null == path) {
            path = context.getExternalFilesDir("rdve");
        }

        checkPath(path);
        m_sRdRootPath = path.toString();

        path = new File(m_sRdRootPath, "log/");
        checkPath(path);
        m_sRdLogPath = path.toString();

        path = new File(m_sRdRootPath, "videos/");
        checkPath(path);
        m_sRdVideoPath = path.toString();

        path = new File(context.getExternalFilesDir("DCIM"), "shotImages/");
        checkPath(path);
        m_sRdImagePath = path.toString();

        path = new File(m_sRdRootPath, "temp/");
        checkPath(path);
        m_sRdTempPath = path.toString();

        path = new File(m_sRdRootPath, "asset/");
        checkPath(path);
        m_sRdAssetPath = path.toString();
        m_sRdTransitionPath = m_sRdAssetPath + "/transition/";

        path = new File(m_sRdRootPath, "download/"); // internet file
        checkPath(path);
        m_sRdDownLoad = path.toString();

        path = context.getDir("log", Context.MODE_PRIVATE);
        if (!path.exists()) {
            path.mkdirs();
        }
        m_sRdDataLogPath = path.toString();

        path = new File(m_sRdRootPath, "specail/");
        checkPath(path);
        m_sRdSpecailPath = path.toString();

        path = new File(m_sRdRootPath, "subs/");
        checkPath(path);
        m_sRdSubPath = path.toString();

        path = new File(m_sRdRootPath, "ttf/");
        checkPath(path);
        m_sRdTtfPath = path.toString();

        path = new File(m_sRdRootPath, "theme/");
        checkPath(path);
        m_sRdThemePath = path.toString();

        path = new File(m_sRdRootPath, "faceu/");
        checkPath(path);
        m_sRdFaceu = path.toString();
        path = new File(m_sRdRootPath, "mv/");
        checkPath(path);
        m_sRdMV = path.toString();

        path = new File(m_sRdRootPath, "music/");
        checkPath(path);
        m_sRdMusic = path.toString();
    }

    /*
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param sPath 要删除的目录或文件
     *
     * @return 删除成功返回 true，否则返回 false。
     */
    public static void DeleteFolderAll(String sDir) {
        // flag = false;
        File file = new File(sDir);
        // 判断目录或文件是否存在
        if (file.exists()) { // 不存在返回 false

            // 判断是否为文件
            if (file.isFile()) { // 为文件时调用删除文件方法
                file.delete();
            } else { // 为目录时调用删除目录方法
                deleteDirectory(sDir);
            }
        }
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static void deleteDirectory(String sPath) {
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {

        } else {

            // 删除文件夹下的所有文件(包括子目录)
            File[] files = dirFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                // 删除子文件
                if (files[i].isFile()) {
                    deleteFile(files[i].getAbsolutePath());
                } // 删除子目录
                else {
                    deleteDirectory(files[i].getAbsolutePath());

                }
            }

            // 删除当前目录
            dirFile.delete();
        }

    }

    /**
     * 删除单个文件
     *
     * @param sPath 被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static void deleteFile(String sPath) {
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

    /**
     * 移动文件
     *
     * @param srcFileName 源文件完整路径
     * @param destDirName 目的目录完整路径
     * @return 文件移动成功返回true，否则返回false
     */
    public static boolean moveFile(String srcFileName, String destDirName) {

        File srcFile = new File(srcFileName);
        if (!srcFile.exists() || !srcFile.isFile())
            return false;

        File destDir = new File(destDirName);
        if (!destDir.exists())
            destDir.mkdirs();

        return srcFile.renameTo(new File(destDirName + File.separator
                + srcFile.getName()));
    }

    /**
     * 移动整个文件夹内容
     *
     * @param srcPath  String 原文件路径
     * @param destPath String 复制后路径
     * @return boolean 是否操作成功
     */
    public static boolean fileMove(String srcPath, String destPath) {
        try {
            File srcDir = new File(srcPath);
            if (!srcDir.exists() || !srcDir.isDirectory())
                return false;
            File destDir = new File(destPath);
            if (destDir.exists()) {
                destDir.delete();
            }
            destDir.mkdirs();
            String[] file = srcDir.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                // 判断是否为斜杠结束
                if (srcPath.endsWith(File.separator)) {
                    temp = new File(srcPath + file[i]);
                } else {
                    temp = new File(srcPath + File.separator + file[i]);
                }
                // 判断是否为文件
                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(destPath
                            + "/" + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 8];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                    temp.delete();
                } else if (temp.isDirectory()) {// 如果是子文件夹，则递归调用自己
                    fileMove(srcPath + "/" + file[i], destPath + "/" + file[i]);
                    temp.delete();
                }
            }
            // 判断原文件夹是否存在，存在就删除原文件
            if (srcDir.exists()) {
                srcDir.delete();
            }
        } catch (Exception e) {
            Log.w("PathUtils", "移动整个文件夹内容操作出错");
            return false;
        }
        return true;
    }

    /**
     * 检查path，如不存在创建之<br>
     * 并检查此路径是否存在文件.nomedia,如没有创建之
     *
     * @param path
     */
    public static void checkPath(File path) {
        File fNoMedia;
        if (!path.exists())
            path.mkdirs();
        fNoMedia = new File(path, ".nomedia");
        if (!fNoMedia.exists()) {
            try {
                fNoMedia.createNewFile();
            } catch (IOException e) {
            }
        }
        fNoMedia = null;
    }

    /**
     * 获取MP4录像文件路径
     *
     * @return
     */
    public static String getMp4FileNameForSdcard() {
        return getTempFileNameForSdcard(m_sRdVideoPath, "VIDEO", "mp4");
    }

    /**
     * 获取一个指定格式的临时文件
     *
     * @param strPrefix
     * @param strExtension
     * @return
     */
    public static String getTempFileNameForSdcard(String strPrefix,
                                                  String strExtension) {
        return getTempFileNameForSdcard(m_sRdTempPath, strPrefix, strExtension);
    }

    /**
     * 获取临时文件路径(以sdcard为root目录)
     *
     * @param strPrefix
     * @param strExtension
     * @return
     */
    public static String getTempFileNameForSdcard(String strRootPath,
                                                  String strPrefix, String strExtension) {
        File rootPath = new File(strRootPath);
        checkPath(rootPath);
        File localPath = new File(rootPath, String.format("%s_%s.%s",
                strPrefix, DateFormat.format("yyyyMMdd_kkmmss", new Date()),
                strExtension));
        return localPath.toString();
    }

    /**
     * 获取临时文件路径(以sdcard为root目录)
     *
     * @param strPrefix
     * @param strExtension
     * @return
     */
    public static String getVideoShotsFileNameForSdcard(String strRootPath,
                                                        String strPrefix, String strExtension) {
        File rootPath = new File(strRootPath);
        checkPath(rootPath);
        File localPath = new File(rootPath, String.format("%s.%s", strPrefix,
                strExtension));
        return localPath.toString();
    }

    /**
     * 获取导出的资源临时文件路径
     *
     * @param strPrefix
     * @param strExtension
     * @return
     */
    public static String getAssetFileNameForSdcard(String strPrefix,
                                                   String strExtension) {
        File rootPath = new File(getRdAssetPath());
        checkPath(rootPath);
        if (TextUtils.isEmpty(strPrefix)) {
            return new File(rootPath, strExtension).toString();
        } else {
            return new File(rootPath, String.format("%s_%s", strPrefix,
                    strExtension)).toString();
        }
    }

    /**
     * 删除指定文件名前缀临时文件
     *
     * @param strPrefix 文件名前缀
     */
    public static void cleanTempFilesByPrefix(final String strPrefix) {
        File fTempPath = new File(getRdTempPath());
        File[] arrCleanTmpFiles = fTempPath.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.startsWith(strPrefix);
            }
        });
        if (arrCleanTmpFiles != null && arrCleanTmpFiles.length > 0) {
            for (File fTmp : arrCleanTmpFiles) {
                if (fTmp.exists()) {
                    fTmp.delete();
                }
            }
        }
        arrCleanTmpFiles = null;
        fTempPath = null;
    }

    /**
     * 清理数据库文件
     *
     * @param path
     */
    public static void clearUpDB(String path) {
        File[] file = new File[]{new File(path), new File(path + "-journal")};
        for (int i = 0; i < file.length; i++) {
            if (file[i].exists()) {
                file[i].delete();
            }
        }
    }

    /**
     * 判断某张表是否存在
     *
     * @param tabName 表名
     * @return
     */
    public static boolean tabIsExist(SQLiteDatabase db, String tabName) {
        boolean result = false;
        if (tabName == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"
                    + tabName.trim() + "' ";
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * 返回指定数据库中的表的个数
     *
     * @param db
     * @return
     */
    public static int allTableNums(SQLiteDatabase db) {
        if (db == null) {
            return 0;
        }
        Cursor cursor = null;
        ArrayList<String> list = new ArrayList<String>();
        try {
            String sql = "select name from sqlite_master where type='table' order by name";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                // 遍历出表名
                String name = cursor.getString(0);
                if (name.equals("android_metadata")
                        || name.equals("sqlite_sequence")
                        || name.equals("draft_share")) {
                    continue;
                }
                list.add(name);
            }
        } catch (Exception e) {
            return 0;
        }
        return list.size();
    }

    /**
     * 获取扩展名
     *
     * @param strFilePath 文件路径
     * @return
     */
    public static String getExternsionName(String strFilePath) {
        int nIndex = strFilePath.lastIndexOf(".");
        if (nIndex >= 0) {
            return strFilePath.substring(nIndex);
        }
        return "";
    }

    public static String getDownLoadDirName() {
        return m_sRdDownLoad;
    }

    /**
     * internet file on sdcard
     *
     * @param strPrefix
     * @param strExtension
     * @return
     */
    public static String getDownLoadFileNameForSdcard(String strPrefix,
                                                      String strExtension) {
        File rootPath = new File(getDownLoadDirName());
        checkPath(rootPath);
        File localPath = new File(rootPath, String.format("%s_.%s", strPrefix,
                strExtension));
        return localPath.toString();
    }

    /**
     * @param strPrefix
     * @return
     */
    public static String getTTFNameForSdcard(String strPrefix) {
        return getDownLoadFileNameForSdcard(strPrefix, FileUtils.TTF_EXTENSION);
    }

    /**
     * 清除临时文件
     */
    public static void clearTemp() {
        if (!TextUtils.isEmpty(m_sRdTempPath)) {
            File tempdir = new File(m_sRdTempPath);
            if (tempdir.exists()) {
                File[] files = tempdir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.startsWith(TEMP_THUMBNAIL)
                                || filename.startsWith(TEMP_WORD)
                                || filename.startsWith(TEMP_MIX)
                                || filename.startsWith(TEMP_MIX_EDIT)
                                || filename.startsWith(TEMP_RECORDVIDEO)
                                || filename.startsWith(TEMP_RECORDING);
                    }
                });
                if (null != files) {
                    for (int i = 0; i < files.length; i++) {
                        files[i].delete();
                    }
                }
            }
        }
    }

    public static final String TEMP_THUMBNAIL = "Temp_thumbnail_",
            TEMP_WORD = "word_", TEMP_RECORDING = "recording_", TEMP_RECORDVIDEO="record_",TEMP_MIX = "mix", TEMP_MIX_EDIT= "mix_edit",TEMP_MIX_RECORD="mix_recorder";

    /**
     * 判断指定文件路径是否有效并存在
     *
     * @param strFilePath
     * @return
     */
    public static boolean fileExists(String strFilePath) {
        if (!TextUtils.isEmpty(strFilePath)) {
            return new File(strFilePath).exists();
        } else {
            return false;
        }
    }
}
