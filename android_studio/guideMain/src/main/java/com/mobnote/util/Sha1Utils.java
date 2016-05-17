package com.mobnote.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import cn.com.tiros.debug.GolukDebugUtils;

/**
 * Created by wangli on 2016/5/12.
 */
public class Sha1Utils {
    public static String getFileSha1(String path) throws OutOfMemoryError,IOException {
        File file=new File(path);
        FileInputStream in = new FileInputStream(file);
        MessageDigest messagedigest;
        try {
            messagedigest = MessageDigest.getInstance("SHA-1");

            byte[] buffer = new byte[1024 * 1024 * 10];
            int len = 0;

            while ((len = in.read(buffer)) >0) {
                //该对象通过使用 update（）方法处理数据
                messagedigest.update(buffer, 0, len);
            }

            //对于给定数量的更新数据，digest 方法只能被调用一次。在调用 digest 之后，MessageDigest 对象被重新设置成其初始状态。
            return bytesToHexString(messagedigest.digest());
        }   catch (NoSuchAlgorithmException e) {
            GolukDebugUtils.e("NoSuchAlgorithmExcp", e.toString());
            e.printStackTrace();
        }
        catch (OutOfMemoryError e) {

            GolukDebugUtils.e("OutOfMemoryError###", e.toString());
            e.printStackTrace();
            throw e;
        }
        finally{
            in.close();
        }
        return null;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int value = b & 0xFF;
            if (value < 16) {
                // if value less than 16, then it's hex String will be only
                // one character, so we need to append a character of '0'
                sb.append("0");
            }
            sb.append(Integer.toHexString(value).toUpperCase());
        }
        return sb.toString();
    }
}
