package com.rd.veuisdk.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;


/**
 * 对象->字符串；
 * 字符串->对象
 */
public class ParcelableUtils {

    /**
     * 对象持久化为数据库字符串
     *
     * @return true代表保存成功
     */
    public static String toParcelStr(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parcelable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        if (null != bytes) {
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        }
        return null;
    }


    /**
     * 还原持久化保存的对象
     *
     * @param str 数据库字符串
     */
    public static <E> E toParcelObj(String str, Parcelable.Creator<E> creator) {
        if (TextUtils.isEmpty(str))
            return null;
        return toParcelObj(Base64.decode(str, Base64.DEFAULT), creator);
    }


    /**
     * 还原持久化保存的对象
     *
     * @param parcelBytes 持续化后的字节流
     * @param creator
     */
    private static <E> E toParcelObj(byte[] parcelBytes, Parcelable.Creator<E> creator) {
        if (parcelBytes == null || parcelBytes.length == 0 || null == creator)
            return null;
        Parcel parcel = null;
        try {
            parcel = Parcel.obtain();
            parcel.unmarshall(parcelBytes, 0, parcelBytes.length);
            parcel.setDataPosition(0);
            return creator.createFromParcel(parcel);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (null != parcel) {
                parcel.recycle();
            }
        }
    }
}
