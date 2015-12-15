package cn.com.mobnote.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rd.car.utils.CheckSDSize;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

public class AssetsFileUtils {
    private static String m_record;
    private static String m_assets;
    private static String m_temp;

    /**
     * 获取在安装应用发配置路径下，创建assets目录
     * 
     * @param context
     * @return
     */
    public static String getCreateAssetDir(Context context) {
	if (TextUtils.isEmpty(m_assets)) {
	    File fileAsset = new File(CheckSDSize.ExistSDCard() ? context
		    .getExternalFilesDir("").getPath() : context.getFilesDir()
		    .getPath(), "assets");
	    checkPath(fileAsset);
	    m_assets = fileAsset.getAbsolutePath();
	}
	return m_assets;
    }

    /**
     * 获取在安装应用配置路径下，创建temp目录
     * 
     * @param context
     * @return
     */
    public static String getCreateTempFileDir(Context context) {
	if (TextUtils.isEmpty(m_temp)) {
	    File fileTemp = new File(CheckSDSize.ExistSDCard() ? context
		    .getExternalFilesDir("").getPath() : context.getFilesDir()
		    .getPath(), "temp");
	    checkPath(fileTemp);
	    m_temp = fileTemp.getAbsolutePath();
	}

	return m_temp;
    }

    /**
     * 获取在安装应用发配置路径下，创建record目录的一个临时文件
     * 
     * @param context
     * @return
     */
    public static File getCreateRecordingTempFile(Context context,
	    String fileName, String strExtension) {
	File localPath = new File(getTempFileName(getRecordingDir(context),
		fileName, strExtension));
	return localPath;
    }

    /**
     * 获取录音文件目录
     * 
     * @param context
     * @return
     */
    public static String getRecordingDir(Context context) {
	if (TextUtils.isEmpty(m_record)) {
	    File fileRecord = new File(CheckSDSize.ExistSDCard() ? context
		    .getExternalFilesDir("").getPath() : context.getFilesDir()
		    .getPath(), "record");
	    checkPath(fileRecord);
	    m_record = fileRecord.getAbsolutePath();
	}
	return m_record;
    }

    /**
     * 获取导出的资源临时文件路径
     * 
     * @param strPrefix
     * @param strExtension
     * @return
     */
    public static String getAssetFileNameForSdcard(Context context,
	    String strFile) {
	File localPath = new File(getCreateAssetDir(context), strFile);
	return localPath.toString();
    }

    /**
     * 检查path，如不存在创建之<br>
     * 并检查此路径是否存在文件.nomedia,如没有创建之
     * 
     * @param path
     */
    private static void checkPath(File path) {
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

    private static SimpleDateFormat dateFormat;

    /**
     * 获取临时文件路径(以当前应用配置的临时目录为入口)
     * 
     * @param strRootPath
     * @param strFileName
     * @param strExtension
     * @return
     */
    public static String getTempFileName(String strRootPath,
	    String strFileName, String strExtension) {
	Date date = new Date();
	if (null == dateFormat) {
	    dateFormat = new SimpleDateFormat("yyyyMMdd_kkmmssSSS");
	}
	File localPath = new File(strRootPath, String.format("%s_%s.%s",
		strFileName, dateFormat.format(date), strExtension));
	return localPath.toString();
    }

    /**
     * 将asset文件保存为指定文件
     * 
     * @param am
     * @param strAssetName
     * @param sdFileName
     * @throws IOException
     */
    public static boolean CopyAssets(AssetManager am, String strAssetFileName,
	    String strDstFile) {
	OutputStream os = null;
	try {
	    File checkDstDir = new File(strDstFile).getParentFile();
	    if (checkDstDir != null && !checkDstDir.exists()) {
		checkDstDir.mkdirs();
	    }
	    os = new FileOutputStream(strDstFile);
	    byte[] pBuffer = new byte[1024];
	    int nReadLen;
	    if (null == am) {
		return false;
	    }
	    InputStream is = am.open(strAssetFileName);
	    while ((nReadLen = is.read(pBuffer)) != -1) {
		os.write(pBuffer, 0, nReadLen);
	    }
	    os.flush();
	    os.close();
	    is.close();
	    os = null;
	    return true;
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		if (os != null) {
		    os.close();
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	return false;
    }

    /**
     * 复制文件
     * 
     * @param sourceFile
     * @param targetFile
     */
    public static void copyFile(File sourceFile, File targetFile) {
	try {
	    // 新建文件输入流并对它进行缓冲
	    FileInputStream input = new FileInputStream(sourceFile);
	    BufferedInputStream inBuff = new BufferedInputStream(input);

	    // 新建文件输出流并对它进行缓冲
	    FileOutputStream output = new FileOutputStream(targetFile);
	    BufferedOutputStream outBuff = new BufferedOutputStream(output);

	    // 缓冲数组
	    byte[] b = new byte[1024 * 5];
	    int len;
	    while ((len = inBuff.read(b)) != -1) {
		outBuff.write(b, 0, len);
	    }
	    // 刷新此缓冲的输出流
	    outBuff.flush();
	    // 关闭流
	    inBuff.close();
	    outBuff.close();
	    output.close();
	    input.close();
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
	/**
	 * 方法追加文件：使用FileWriter
	 */
	public static void appendFileData(String path, String content) {
		FileWriter writer = null;
		try {
			File file = new File(path);
			if(!file.exists()){
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			//打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			writer = new FileWriter(path, true);
			writer.write(content);
		}
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 方法追加文件：使用FileWriter
	 */
	public static void wirterFileData(String path, String content) {
		try {
			File file = new File(path);
			if(!file.exists()){
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			//打开一个写文件器，构造函数中的第二个参数false表示以覆盖形式写文件
			FileWriter writer = new FileWriter(path, false);
			writer.write(content);
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}