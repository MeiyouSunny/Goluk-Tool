package com.mobnote.user;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.util.GlideUtils;

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
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.com.tiros.api.FileUtils;

public class UserUtils {

    public static List<Activity> mActivityList = new ArrayList<Activity>();

    /**
     * AlertDialog
     */
    public static void showDialog(Context context, String message) {
        if (!isActivityRunning(context))
            return;
        Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.setTitle(context.getResources().getString(R.string.user_dialog_hint_title))
                .setMessage(message).setPositiveButton(context.getResources().getString(R.string.user_repwd_ok), null)
                .create();
        dialog.show();
    }

    /**
     * 验证是否是数字
     */
    public static boolean isNumber(String mobiles) {
        boolean flag = false;
        try {
            Pattern p = Pattern.compile("[0-9]*"); // 验证手机号
            Matcher m = p.matcher(mobiles);
            flag = m.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
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
        ConnectivityManager conManager = (ConnectivityManager) GolukApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conManager != null) {
            NetworkInfo network = conManager.getActiveNetworkInfo();
            if (network != null) {
                bisConnFlag = network.isAvailable();
            }
        }
        return bisConnFlag;
    }

    /**
     * 个人中心模块头像的变化
     */
//	public static void userHeadChange(ImageView headImage, String headString, TextView textSex) {
//		if (headString.equals("1")) {
//			headImage.setImageResource(R.drawable.my_head_boy1);
//			textSex.setText("男");
//		} else if (headString.equals("2")) {
//			headImage.setImageResource(R.drawable.my_head_boy2);
//			textSex.setText("男");
//		} else if (headString.equals("3")) {
//			headImage.setImageResource(R.drawable.my_head_boy3);
//			textSex.setText("男");
//		} else if (headString.equals("4")) {
//			headImage.setImageResource(R.drawable.my_head_girl4);
//			textSex.setText("女");
//		} else if (headString.equals("5")) {
//			headImage.setImageResource(R.drawable.my_head_girl5);
//			textSex.setText("女");
//		} else if (headString.equals("6")) {
//			headImage.setImageResource(R.drawable.my_head_girl6);
//			textSex.setText("女");
//		} else {
//			headImage.setImageResource(R.drawable.my_head_moren7);
//			textSex.setText("未知");
//		}
//	}
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
    public static void focusHead(Context context, String headString, ImageView headImage) {
        try {
            if (null == headImage) {
                return;
            }
            if (headString.equals("1")) {
                GlideUtils.loadLocalHead(context, headImage, R.drawable.my_head_boy1);
            } else if (headString.equals("2")) {
                GlideUtils.loadLocalHead(context, headImage, R.drawable.my_head_boy2);
            } else if (headString.equals("3")) {
                GlideUtils.loadLocalHead(context, headImage, R.drawable.my_head_boy3);
            } else if (headString.equals("4")) {
                GlideUtils.loadLocalHead(context, headImage, R.drawable.my_head_girl4);
            } else if (headString.equals("5")) {
                GlideUtils.loadLocalHead(context, headImage, R.drawable.my_head_girl5);
            } else if (headString.equals("6")) {
                GlideUtils.loadLocalHead(context, headImage, R.drawable.my_head_girl6);
            } else {
                GlideUtils.loadLocalHead(context, headImage, R.drawable.my_head_moren7);
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
        }
    }

    /**
     * 升级成功
     */
    public static void showUpdateSuccess(AlertDialog showUpdateDialog, Context context, String message) {
        // Bugly #11676
        if (!isActivityRunning(context))
            return;
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


    public static boolean emailValidation(String email) {
        String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
        return email.matches(regex);
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
     * 设置普通评论显示
     *
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
     *
     * @param view
     * @param nikename
     * @param text
     */
    public static void showText(Context context, TextView view, String nikename, String text) {
        String replyLabel = context.getString(R.string.str_reply);
        String replyName = "@" + nikename + context.getString(R.string.str_colon);
        String reply_str = context.getString(R.string.str_reply) + replyName + text;
        SpannableStringBuilder style = new SpannableStringBuilder(reply_str);
        style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)), replyLabel.length(), replyName.length() + replyLabel.length() - 1,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        view.setText(style);
    }

    /**
     * 设置最新、个人主页回复评论内容显示
     *
     * @param view
     * @param nikename
     * @param replyName
     * @param text
     */
    public static void showReplyText(Context context, TextView view, String nikename, String replyName, String text) {
        String replyLabel = context.getString(R.string.str_reply);
        String replyText = "@" + replyName + context.getString(R.string.str_colon);
        String str = nikename + " " + replyLabel + replyText + text;
        SpannableStringBuilder style = new SpannableStringBuilder(str);

        style.setSpan(new ForegroundColorSpan(Color.rgb(0x11, 0x63, 0xa2)),
                nikename.length() + 1 + replyLabel.length(),
                nikename.length() + replyLabel.length() + replyText.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        view.setText(style);
    }

    /**
     * 添加Activity
     *
     * @param activity
     */
    public static void addActivity(Activity activity) {
        mActivityList.add(activity);
    }

    /**
     * 移除Activity
     */
    public static void removeActivity() {
        if (mActivityList.size() > 0) {
            mActivityList.remove(mActivityList.size() - 1);
        }
    }

    /**
     * 关闭list里面所有的Activity
     */
    public static void exit() {
        try {
            for (Activity activity : mActivityList) {
                if (activity != null) {
                    activity.finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据显示1,000
     *
     * @param fmtnumber
     * @return
     */
    public static String formatNumber(String fmtnumber) {
        String number;
        try {
            int wg = Integer.parseInt(fmtnumber);
            DecimalFormat df = new DecimalFormat("#,###");
            number = df.format(wg);
        } catch (Exception e) {
            return fmtnumber;
        }
        return number;
    }

    /**
     * Return the activity is destory
     */
    public static boolean isActivityRunning(Context context) {
        if (context == null && !(context instanceof Activity))
            return false;

        return !((Activity) context).isFinishing() && !((Activity) context).isDestroyed();
    }

}
