package cn.com.mobnote.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.golukmobile.R;

public class UserUtils {

	/**
	 * AlertDialog
	 */
	public static void showDialog(Context context,String message){
		Builder builder = new AlertDialog.Builder(context);
		AlertDialog dialog = builder.setMessage(message)
		.setPositiveButton("确定", null)
		.create();
		dialog.show();
	}
	/**
	 * 隐藏软件盘
	 */
	public static void hideSoftMethod(Activity activity){
		 InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		  imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
	}
	/**
	 * 常用手机号的判断
	 */
	public static boolean isMobileNO(String mobiles) {
        boolean flag = false;
        try {
            Pattern p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
            Matcher m = p.matcher(mobiles);
            flag = m.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }
	 public static boolean isNetDeviceAvailable(Context context){
	        boolean bisConnFlag=false;
	        ConnectivityManager conManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo network = conManager.getActiveNetworkInfo();
	        if(network!=null){
	            bisConnFlag=conManager.getActiveNetworkInfo().isAvailable();
	        }
	        return bisConnFlag;
	    }
	 
	 /**
	  * 个人中心模块头像的变化
	  */
	 public static void userHeadChange(ImageView headImage,String headString,TextView textSex){
		if(headString.equals("1")){
			headImage.setImageResource(R.drawable.individual_center_head_boy_one);
			textSex.setText("男");
		}else if(headString.equals("2")){
			headImage.setImageResource(R.drawable.individual_center_head_boy_two);
			textSex.setText("男");
		}else if(headString.equals("3")){
			headImage.setImageResource(R.drawable.individual_center_head_boy_three);
			textSex.setText("男");
		}else if(headString.equals("4")){
			headImage.setImageResource(R.drawable.individual_center_head_girl_one);
			textSex.setText("女");
		}else if(headString.equals("5")){
			headImage.setImageResource(R.drawable.individual_center_head_girl_two);
			textSex.setText("女");
		}else if(headString.equals("6")){
			headImage.setImageResource(R.drawable.individual_center_head_girl_three);
			textSex.setText("女");
		}else if(headString.equals("7")){
			headImage.setImageResource(R.drawable.individual_center_head_moren);
			textSex.setText("未知");
		}
	 }
	 
	 /**
	  * UserPersonalHeadActivity默认选中的head
	  */
	 public static void focusHead(String headString,ImageView headImage){
		 if(headString.equals("1")){
				headImage.setImageResource(R.drawable.individual_center_head_boy_one);
			}else if(headString.equals("2")){
				headImage.setImageResource(R.drawable.individual_center_head_boy_two);
			}else if(headString.equals("3")){
				headImage.setImageResource(R.drawable.individual_center_head_boy_three);
			}else if(headString.equals("4")){
				headImage.setImageResource(R.drawable.individual_center_head_girl_one);
			}else if(headString.equals("5")){
				headImage.setImageResource(R.drawable.individual_center_head_girl_two);
			}else if(headString.equals("6")){
				headImage.setImageResource(R.drawable.individual_center_head_girl_three);
			}else if(headString.equals("7")){
				headImage.setImageResource(R.drawable.individual_center_head_moren);
			}
	 }
	 
	 public static String getImage2Sex(int drawableImage,String image){
		 if(drawableImage == R.drawable.individual_center_head_boy_one){
			 image = "1";
		 }else if(drawableImage == R.drawable.individual_center_head_boy_two){
			 image = "2";
		 }else if(drawableImage == R.drawable.individual_center_head_boy_three){
			 image = "3";
		 }else if(drawableImage == R.drawable.individual_center_head_girl_one){
			 image = "4";
		 }else if(drawableImage == R.drawable.individual_center_head_girl_two){
			 image = "5";
		 }else if(drawableImage == R.drawable.individual_center_head_girl_three){
			 image = "6";
		 }else if(drawableImage == R.drawable.individual_center_head_moren){
			 image = "7";
		 }
		 return image;
	 }
	
}
