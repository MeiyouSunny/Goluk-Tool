package com.goluk.ipcdemo.utils;
import android.app.Activity;
import android.content.SharedPreferences;

/**
 * 1.编辑器必须显示空白处
 *
 * 2.所有代码必须使用TAB键缩进
 *
 * 3.类首字母大写,函数、变量使用驼峰式命名,常量所有字母大写
 *
 * 4.注释必须在行首写.(枚举除外)
 *
 * 5.函数使用块注释,代码逻辑使用行注释
 *
 * 6.文件头部必须写功能说明
 *
 * 7.所有代码文件头部必须包含规则说明
 *
 * 保存配置文件信息
 *
 * 2015年3月24日
 *
 * @author xuhw
 */
public class SettingUtils {

   private SharedPreferences.Editor editor=null;
   private SharedPreferences preferences=null;
   private volatile static SettingUtils instance=null;

   public static SettingUtils getInstance() {
       if (null == instance){
           synchronized (SettingUtils.class) {
               if (null == instance){
                   instance = new SettingUtils();
               }
           }
       }
       return instance;
   }

   public SettingUtils(){
//       preferences = GolukApplication.getInstance().getSharedPreferences("settings", Activity.MODE_PRIVATE);
//       editor = preferences.edit();
   }

   public void putString(String key, String value){
       editor.putString(key, value);
       editor.commit();
   }

   public String getString(String key, String value){
       return preferences.getString(key, value);
   }

   public String getString(String key){
       return preferences.getString(key, "");
   }

   public void putBoolean(String key, boolean value){
       editor.putBoolean(key, value);
       editor.commit();
   }

   public boolean getBoolean(String key){
       return preferences.getBoolean(key, false);
   }

   public boolean getBoolean(String key, boolean value){
       return preferences.getBoolean(key, value);
   }

   public void putInt(String key, int value){
       editor.putInt(key, value);
       editor.commit();
   }

   public int getInt(String key){
       return preferences.getInt(key, -1);
   }

   public int getInt(String key, int value){
       return preferences.getInt(key, value);
   }

   public void putLong(String key, long value){
       editor.putLong(key, value);
       editor.commit();
   }

   public long getLong(String key){
       return preferences.getLong(key, -1);
   }

   public long getLong(String key, int value){
       return preferences.getLong(key, value);
   }

}
