package cn.com.mobnote.user;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.application.SysApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.live.ILive;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.api.FileUtils;

import com.facebook.drawee.view.SimpleDraweeView;

public class UserUtils {

	/**
	 * AlertDialog
	 */
	public static void showDialog(Context context, String message) {
		Builder builder = new AlertDialog.Builder(context);
		AlertDialog dialog = builder.setTitle(context.getResources().getString(R.string.user_dialog_hint_title))
				.setMessage(message).setPositiveButton(context.getResources().getString(R.string.user_repwd_ok), null)
				.create();
		dialog.show();
	}

	/**
	 * 隐藏软件盘
	 */
	public static void hideSoftMethod(Activity activity) {
		if (null == activity) {
			return;
		}
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		View focusView = activity.getCurrentFocus();
		if (null == imm || null == focusView) {
			return;
		}
		imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
	}

	/**
	 * 常用手机号的判断
	 */
	public static boolean isMobileNO(String mobiles) {
		boolean flag = false;
		try {
			Pattern p = Pattern.compile("^[1][0-9]{10}$"); // 验证手机号
			Matcher m = p.matcher(mobiles);
			flag = m.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	public static boolean isNetDeviceAvailable(Context context) {
		boolean bisConnFlag = false;
		ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo network = conManager.getActiveNetworkInfo();
		if (network != null) {
			bisConnFlag = conManager.getActiveNetworkInfo().isAvailable();
		}
		return bisConnFlag;
	}

	/**
	 * 个人中心模块头像的变化
	 */
	public static void userHeadChange(ImageView headImage, String headString, TextView textSex) {
		if (headString.equals("1")) {
			headImage.setImageResource(R.drawable.my_head_boy1);
			textSex.setText("男");
		} else if (headString.equals("2")) {
			headImage.setImageResource(R.drawable.my_head_boy2);
			textSex.setText("男");
		} else if (headString.equals("3")) {
			headImage.setImageResource(R.drawable.my_head_boy3);
			textSex.setText("男");
		} else if (headString.equals("4")) {
			headImage.setImageResource(R.drawable.my_head_girl4);
			textSex.setText("女");
		} else if (headString.equals("5")) {
			headImage.setImageResource(R.drawable.my_head_girl5);
			textSex.setText("女");
		} else if (headString.equals("6")) {
			headImage.setImageResource(R.drawable.my_head_girl6);
			textSex.setText("女");
		} else {
			headImage.setImageResource(R.drawable.my_head_moren7);
			textSex.setText("未知");
		}
	}

	public static void userHeadChanged(ImageView headImage, String headString, String textSex) {
		if (headString.equals("1")) {
			headImage.setImageResource(R.drawable.my_head_boy1);
			textSex = "1";
		} else if (headString.equals("2")) {
			headImage.setImageResource(R.drawable.my_head_boy2);
			textSex = "1";
		} else if (headString.equals("3")) {
			headImage.setImageResource(R.drawable.my_head_boy3);
			textSex = "1";
		} else if (headString.equals("4")) {
			headImage.setImageResource(R.drawable.my_head_girl4);
			textSex = "2";
		} else if (headString.equals("5")) {
			headImage.setImageResource(R.drawable.my_head_girl5);
			textSex = "2";
		} else if (headString.equals("6")) {
			headImage.setImageResource(R.drawable.my_head_girl6);
			textSex = "2";
		} else {
			headImage.setImageResource(R.drawable.my_head_moren7);
			textSex = "0";
		}
	}

	/**
	 * UserPersonalHeadActivity默认选中的head
	 */
	public static void focusHead(String headString, SimpleDraweeView headImage) {
		try {
			if (null == headImage) {
				return;
			}
			if (headString.equals("1")) {
				headImage.setImageURI(GolukUtils.getResourceUri(R.drawable.my_head_boy1));
			} else if (headString.equals("2")) {
				headImage.setImageURI(GolukUtils.getResourceUri(R.drawable.my_head_boy2));
			} else if (headString.equals("3")) {
				headImage.setImageURI(GolukUtils.getResourceUri(R.drawable.my_head_boy3));
			} else if (headString.equals("4")) {
				headImage.setImageURI(GolukUtils.getResourceUri(R.drawable.my_head_girl4));
			} else if (headString.equals("5")) {
				headImage.setImageURI(GolukUtils.getResourceUri(R.drawable.my_head_girl5));
			} else if (headString.equals("6")) {
				headImage.setImageURI(GolukUtils.getResourceUri(R.drawable.my_head_girl6));
			} else {
				headImage.setImageURI(GolukUtils.getResourceUri(R.drawable.my_head_moren7));
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 固件升级提示框
	 */
	public static AlertDialog showDialogUpdate(Context context, String message) {
		AlertDialog showUpdateDialog = null;
		showUpdateDialog = new AlertDialog.Builder(context).setMessage(message).setCancelable(false)
				.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							return true;
						}
						return false;
					}
				}).show();
		return showUpdateDialog;
	}

	/**
	 * 固件升级取消对话框
	 */
	public static void dismissUpdateDialog(AlertDialog showUpdateDialog) {
		if (null != showUpdateDialog) {
			showUpdateDialog.dismiss();
			showUpdateDialog = null;
		}
	}

	/**
	 * 升级成功
	 */
	public static void showUpdateSuccess(AlertDialog showUpdateDialog, Context context, String message) {
		if (showUpdateDialog == null) {
			showUpdateDialog = new AlertDialog.Builder(context)
					.setTitle(context.getResources().getString(R.string.user_dialog_hint_title)).setMessage(message)
					.setPositiveButton(context.getResources().getString(R.string.user_repwd_ok), null).show();
		}
	}

	/**
	 * 判断文件是否存在
	 */
	public static boolean fileIsExists(String path) {
		try {
			String filePath = FileUtils.libToJavaPath(path);
			File f = new File(filePath);
			if (!f.exists()) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 升级提示
	 * 
	 * @param mContext
	 * @param message1
	 * @param message2
	 */
	public static void showUpgradeGoluk(final Context mContext, String message, final String url) {
		Builder mBuilder = new AlertDialog.Builder(mContext);
		AlertDialog dialog = mBuilder.setTitle("发现新版本").setMessage(message)
				.setPositiveButton("马上升级", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// 浏览器打开url
						GolukUtils.openUrl(url, mContext);

						if (GolukApplication.mMainActivity != null) {
							GolukApplication.mMainActivity.finish();
							GolukApplication.mMainActivity = null;
						}
						SysApplication.getInstance().exit();
					}
				}).setCancelable(false).setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							return true;
						}
						return false;
					}
				}).create();
		dialog.show();
	}

	/**
	 * 手机号格式化显示 3-4-4
	 * 
	 * @param s
	 * @param cursorPosition
	 * @param before
	 * @param count
	 * @param mEditPhone
	 * @param mTextWatcher
	 */
	public static void formatPhone(CharSequence s, EditText mEditPhone) {
		String contents = s.toString();
		int length = contents.length();
		if (length == 4) {
			if (contents.substring(3).equals(new String("-"))) { // -
				contents = contents.substring(0, 3);
				mEditPhone.setText(contents);
				mEditPhone.setSelection(contents.length());
			} else {
				contents = contents.substring(0, 3) + "-" + contents.substring(3);
				mEditPhone.setText(contents);
				mEditPhone.setSelection(contents.length());
			}
		} else if (length == 9) {
			if (contents.substring(8).equals(new String("-"))) { // -
				contents = contents.substring(0, 8);
				mEditPhone.setText(contents);
				mEditPhone.setSelection(contents.length());
			} else {
				contents = contents.substring(0, 8) + "-" + contents.substring(8);
				mEditPhone.setText(contents);
				mEditPhone.setSelection(contents.length());
			}
		}
	}

	/**
	 * 手机号格式化后保存
	 * 
	 * @param phone
	 */
	public static String formatSavePhone(String phone) {
		String a = phone.substring(0, 3);
		String b = phone.substring(3, 7);
		String c = phone.substring(7, phone.length());
		return a + "-" + b + "-" + c;
	}

	/**
	 * 通过头像标识，得到头像图片
	 * 
	 * @param headStr
	 *            头像标识 [1...6]
	 * @return 返回图片id (R.darwable.xxx)
	 * @author jyf
	 * @date 2015年8月7日
	 */
	public static int getUserHeadImageResourceId(String headStr) {
		try {
			if (null != headStr && !"".equals(headStr)) {
				int utype = Integer.valueOf(headStr);
				int head = ILive.mHeadImg[utype];
				return head;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 显示默认头像
		return ILive.mHeadImg[7];
	}

	/**
	 * 设置普通评论显示
	 * @param view
	 * @param nikename
	 * @param text
	 */
	public static void showCommentText(TextView view, String nikename, String text) {
		String t_str = nikename + " " + text;
		SpannableStringBuilder style = new SpannableStringBuilder(t_str);
		style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 0, nikename.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		view.setText(style);
	}
	
	/**
	 * 评论列表中回复评论颜色设置
	 * @param view
	 * @param nikename
	 * @param text
	 */
	public static void showText(TextView view, String nikename, String text) {
		String replyName = "@" + nikename + "：";
		String reply_str = "回复" + replyName + text;
		SpannableStringBuilder style = new SpannableStringBuilder(reply_str);
		style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 2,
				replyName.length() + 2, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		view.setText(style);
	}
	
	/**
	 * 设置最新、个人主页回复评论内容显示
	 * @param view
	 * @param nikename
	 * @param replyName
	 * @param text
	 */
	public static void showReplyText(TextView view, String nikename, String replyName,String text) {
		String replyText = "@"+replyName+"：";
		String str = nikename+" 回复"+replyText+text;
		SpannableStringBuilder style = new SpannableStringBuilder(str);
		style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), 0, nikename.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), nikename.length()+3, nikename.length()+3+replyText.length(),
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		view.setText(style);
	}
	
	/**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     * 
     * @param v
     * @param event
     * @return
     */
    public static boolean isShouldHideInput(View view, MotionEvent event) {
        if (view != null) {
            int[] l = { 0, 0 };
            view.getLocationInWindow(l);
            int left = l[0];
            int top = l[1];
            int bottom = top + view.getHeight();
            int right = left+ view.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }
	
}
