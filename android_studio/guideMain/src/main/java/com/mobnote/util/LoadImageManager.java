package com.mobnote.util;

import java.util.HashMap;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * </pre>
 * 
 * @ 功能描述:本地图片加载管理
 * 
 * @author 陈宣宇
 * 
 */

public class LoadImageManager {
	/** 图片缓存map */
	private static HashMap<String, Drawable> mImageMap = new HashMap<String,Drawable>();
	
	/**
	* 加载本地图片
	* @param url
	* @return
	*/
	public static Drawable getLoacalBitmap(String url,Context context){
		try{
			//先判断缓存中是否存在
			Drawable drawable = null;
			drawable = mImageMap.get(url);
			if(null == drawable){
				Bitmap bitmap =  BitmapFactory.decodeFile(url);
				if (bitmap == null)
				{
					return null;
				}
				drawable = new BitmapDrawable(context.getResources(),bitmap);
				
				//保存图片到缓存
				mImageMap.put(url,drawable);
			}
			return drawable;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
