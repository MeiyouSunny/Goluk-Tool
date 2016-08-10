package com.mobnote.golukmain.live;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.livevideo.LiveActivity;
import com.mobnote.util.GolukUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

public class LiveDialogManager {
    /**
     * 单例实例
     */
    private static LiveDialogManager mManagerInstance = null;
    /**
     * 授权load对话框
     */
    private AlertDialog mLoginDialog = null;
    private AlertDialog mLiveExitDialog = null;
    /**
     * 用户主动点击退出时，提示对话框
     */
    private AlertDialog mLiveBackDialog = null;

    private AlertDialog mSingleButtonDialog = null;
    private AlertDialog mTwoButtonDialog = null;
    private CustomLoadingDialog mCustomLoadingDialog = null;
    private ProgressDialog mProgressDialog = null;
    private ProgressDialog mShareDialog = null;
    /**
     * 公共的加载对话框
     */
    private ProgressDialog mCommProgressDialog = null;

    private AlertDialog dialog = null;
    private AlertDialog ad = null;
    private AlertDialog confirmation = null;

    /**
     * 对话框回调方法
     */
    private ILiveDialogManagerFn dialogManagerFn = null;

    /**
     * 对话框的“确定”按钮
     */
    public static final int FUNCTION_DIALOG_OK = 0;
    /**
     * 对话框的“取消”按钮
     */
    public static final int FUNCTION_DIALOG_CANCEL = 1;

    /**
     * 授权对话框类型
     */
    public static final int DIALOG_TYPE_AUTHENTICATION = 0;
    /**
     * 结束直播提示框
     */
    public static final int DIALOG_TYPE_EXIT_LIVE = 1;
    /**
     * 登录对话框
     */
    public static final int DIALOG_TYPE_LOGIN = 2;
    /**
     * 直播返回
     */
    public static final int DIALOG_TYPE_LIVEBACK = 3;
    /**
     * 直播超时
     */
    public static final int DIALOG_TYPE_LIVE_TIMEOUT = 4;
    /**
     * 直播服务下线
     */
    public static final int DIALOG_TYPE_LIVE_OFFLINE = 5;

    public static final int DIALOG_TYPE_LIVE_CONTINUE = 6;
    /**
     * 进入直播
     */
    public static final int DIALOG_TYPE_LIVE_START = 7;
    /**
     * ipc未登录提示
     */
    public static final int DIALOG_TYPE_IPC_LOGINOUT = 8;
    /**
     * 程序退出提示框
     */
    public static final int DIALOG_TYPE_APP_EXIT = 9;
    /**
     * 重新上传视频
     */
    public static final int DIALOG_TYPE_LIVE_RELOAD_UPLOAD = 10;
    /**
     * 重新请服务
     */
    public static final int DIALOG_TYPE_LIVE_REQUEST_SERVER = 11;
    /**
     * 直播分享
     */
    public static final int DIALOG_TYPE_LIVE_SHARE = 12;
    /**
     * WIFI连接提示用户去系统设置连接WIFI
     */
    public static final int DIALOG_TYPE_WIFIBIND_SHOWSETTING = 13;
    /**
     * 提示用户重新启动IPC
     */
    public static final int DIALOG_TYPE_WIFIBIND_RESTART_IPC = 14;
    /**
     * 綁定失败提示框
     */
    public static final int DIALOG_TYPE_WIFIBIND_FAILED = 15;

    public static final int DIALOG_TYPE_CONFIRM = 16;
    /**
     * 提交评论
     */
    public static final int DIALOG_TYPE_COMMENT_COMMIT = 17;
    /**
     * 输入１０秒钟禁止评论限制
     */
    public static final int DIALOG_TYPE_COMMENT_TIMEOUT = 18;
    /**
     * 删除评论
     */
    public static final int DIALOG_TYPE_COMMENT_DELETE = 19;
    /**
     * 正在提交删除评论
     */
    public static final int DIALOG_TYPE_COMMENT_PROGRESS_DELETE = 20;
    /**
     * 注销
     */
    public static final int DIALOG_TYPE_LOGOUT = 21;
    /**
     * 获取推送配置
     */
    public static final int DIALOG_TYPE_GET_PUSH_CONFIGE = 22;
    /**
     * 删除我自己的视频
     */
    public static final int DIALOG_TYPE_DEL_VIDEO = 23;

    private int mCurrentDialogType = 0;

    /**
     * 获取当前类的一个实例
     *
     * @return MapDialogManager实例
     * @author jiayf
     * @date 2014-5-22
     */
    public static LiveDialogManager getManagerInstance() {
        if (null == mManagerInstance) {
            mManagerInstance = new LiveDialogManager();
        }
        return mManagerInstance;
    }

    /**
     * 设置对话框回调接口
     *
     * @param _fn 回调接口
     * @author jiayf
     * @date 2014-5-22
     */
    public void setDialogManageFn(ILiveDialogManagerFn _fn) {
        dialogManagerFn = _fn;
    }

    /**
     * 弹出举报的窗口
     *
     * @throws
     * @Title: showDialog
     * @Description: TODO void
     * @author 曾浩
     */
    public void showDialog(final Context context, final int dialogId) {
        if(null == context) {
            return;
        }
        if(context instanceof LiveActivity && !GolukUtils.isActivityAlive((LiveActivity) context)) {
            return;
        }
        dialog = new AlertDialog.Builder(context, R.style.CustomDialog).create();
        dialog.show();
        dialog.getWindow().setContentView(R.layout.video_square_dialog_main);
        dialog.getWindow().findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ad = new AlertDialog.Builder(context, R.style.CustomDialog).create();
                ad.show();
                ad.getWindow().setContentView(R.layout.video_square_dialog_selected);
                ad.getWindow().findViewById(R.id.sqds).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmation(context, dialogId, "1");
                    }
                });
                ad.getWindow().findViewById(R.id.yyhz).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmation(context, dialogId, "2");
                    }
                });
                ad.getWindow().findViewById(R.id.zzmg).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmation(context, dialogId, "3");
                    }
                });
                ad.getWindow().findViewById(R.id.qtyy).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmation(context, dialogId, "4");
                    }
                });
                ad.getWindow().findViewById(R.id.qx).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad.dismiss();
                    }
                });
            }
        });

        dialog.getWindow().findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void confirmation(final Context context, final int dialogId, final String reporttype) {
        ad.dismiss();
        confirmation = new AlertDialog.Builder(context, R.style.CustomDialog).create();
        confirmation.show();
        confirmation.getWindow().setContentView(R.layout.video_square_dialog_confirmation);
        confirmation.getWindow().findViewById(R.id.sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessageCallBack(dialogId, FUNCTION_DIALOG_CANCEL, reporttype);

                // boolean isSucess = report("1", getCurrentVideoId(),
                // reporttype);
                // if (isSucess) {
                // GolukUtils.showToast(LiveActivity.this, "举报成功,我们稍后会进行处理");
                // } else {
                // GolukUtils.showToast(LiveActivity.this, "举报失败!");
                // }

                confirmation.dismiss();
            }
        });
        confirmation.getWindow().findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmation.dismiss();
            }
        });
    }

    /**
     * 对话框回调接口
     */
    public interface ILiveDialogManagerFn {
        /**
         * 对话框管理类的回调方法
         */
        public void dialogManagerCallBack(int dialogType, int function, String data);
    }

    public void dissmissCommProgressDialog() {
        if (null != mCommProgressDialog) {
            mCommProgressDialog.dismiss();
            mCommProgressDialog = null;
        }
    }

    public void showCommProgressDialog(Context context, int type, String title, String message, boolean isCancel) {
        if(null == context) {
            return;
        }
        if(context instanceof LiveActivity && !GolukUtils.isActivityAlive((LiveActivity) context)) {
            return;
        }
        dissmissCommProgressDialog();
        mCurrentDialogType = type;
        mCommProgressDialog = ProgressDialog.show(context, title, message, true, isCancel);

        if (isCancel) {
            mCommProgressDialog.setButton(context.getString(R.string.dialog_str_cancel), new OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    dissmissCommProgressDialog();
                    sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_CANCEL, null);

                }
            });
        }

        mCommProgressDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                dissmissCommProgressDialog();
                sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_CANCEL, null);
            }
        });

    }

    public void showShareProgressDialog(Context context, int type, String title, String message) {
        if(null == context) {
            return;
        }
        if(context instanceof LiveActivity && !GolukUtils.isActivityAlive((LiveActivity) context)) {
            return;
        }
        dismissShareProgressDialog();
        mCurrentDialogType = type;
        mShareDialog = ProgressDialog.show(context, title, message, true, false);
        mShareDialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_CANCEL, null);
            }
        });
    }

    public void dismissShareProgressDialog() {
        if (null != mShareDialog) {
            mShareDialog.dismiss();
            mShareDialog = null;
        }
    }

    /**
     * 显示定制化的对话框
     *
     * @param context
     * @param msg     显示信息
     * @author jyf
     * @date 2015年6月8日
     */
    public void showCustomDialog(Context context, String msg) {
        if(null == context) {
            return;
        }
        if(context instanceof LiveActivity && !GolukUtils.isActivityAlive((LiveActivity) context)) {
            return;
        }
        dissmissCustomDialog();
        mCustomLoadingDialog = new CustomLoadingDialog(context, msg);
        mCustomLoadingDialog.show();
    }

    /**
     * 取消对话框
     *
     * @author jyf
     * @date 2015年6月8日
     */
    public void dissmissCustomDialog() {
        if (null != mCustomLoadingDialog) {
            mCustomLoadingDialog.close();
            mCustomLoadingDialog = null;
        }
    }

    public void showProgressDialog(Context context, String title, String message) {
        if(null == context) {
            return;
        }
        if(context instanceof LiveActivity && !GolukUtils.isActivityAlive((LiveActivity) context)) {
            return;
        }
        dismissProgressDialog();
        mProgressDialog = ProgressDialog.show(context, title, message, true, false);
        mProgressDialog.setCancelable(false);
    }

    public void setProgressDialogMessage(String message) {
        if (null != mProgressDialog) {
            mProgressDialog.setMessage(message);
        }
    }

    public void dismissProgressDialog() {
        if (null != mProgressDialog) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void showSingleBtnDialog(Context context, int type, String title, String message) {
        if(null == context) {
            return;
        }
        if (context instanceof Activity && !GolukUtils.isActivityAlive((Activity) context)) {
                return;
        }
        if (null != mSingleButtonDialog) {
            return;
        }
        mCurrentDialogType = type;

        mSingleButtonDialog = new AlertDialog.Builder(context).create();

        mSingleButtonDialog.setTitle(title);
        mSingleButtonDialog.setMessage(message);
        mSingleButtonDialog.setCancelable(false);

        mSingleButtonDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.user_personal_sign_title), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_OK, null);
                        dismissSingleBtnDialog();
                    }
                });
        mSingleButtonDialog.show();
    }

    public void dismissSingleBtnDialog() {
        if (null != mSingleButtonDialog && mSingleButtonDialog.isShowing()) {
            mSingleButtonDialog.dismiss();
            mSingleButtonDialog = null;
        }
    }

    public void showTwoBtnDialog(Context context, int function, String title, String message) {

        if(null == context) {
            return;
        }
        if(context instanceof LiveActivity && !GolukUtils.isActivityAlive((LiveActivity) context)) {
            return;
        }
        if (null != mLiveBackDialog) {
            return;
        }
        mCurrentDialogType = function;
        mTwoButtonDialog = new AlertDialog.Builder(context).create();

        mTwoButtonDialog.setTitle(title);
        mTwoButtonDialog.setMessage(message);
        mTwoButtonDialog.setCancelable(false);

        mTwoButtonDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.dialog_str_cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_CANCEL, null);
                        dismissTwoButtonDialog();
                    }
                });

        mTwoButtonDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.user_personal_sign_title), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_OK, null);

                        dismissTwoButtonDialog();
                    }
                });
        mTwoButtonDialog.show();

    }

    public void dismissTwoButtonDialog() {
        if (null != mTwoButtonDialog) {
            mTwoButtonDialog.dismiss();
            mTwoButtonDialog = null;
        }
    }

    private void sendMessageCallBack(int dialogType, int function, String data) {
        if (null == dialogManagerFn) {
            return;
        }
        dialogManagerFn.dialogManagerCallBack(dialogType, function, data);
    }

    // 显示登录对话框
    public void showLoginDialog(Context context, String message) {
        if (null != mLoginDialog) {
            return;
        }
        mLoginDialog = new AlertDialog.Builder(context).create();

        mLoginDialog.setTitle(context.getString(R.string.user_dialog_hint_title));
        mLoginDialog.setMessage(message);
        mLoginDialog.setCancelable(false);

        mLoginDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.user_personal_sign_title),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        sendMessageCallBack(DIALOG_TYPE_LOGIN, FUNCTION_DIALOG_OK, null);
                        dimissLoginExitDialog();
                    }
                });
        mLoginDialog.show();

    }

    // 销毁登录对话框
    public void dimissLoginExitDialog() {
        if (null != mLoginDialog) {
            mLoginDialog.dismiss();
            mLoginDialog = null;
        }
    }

    public void showLiveExitDialog(Context context, String title, String message) {
        if(null == context) {
            return;
        }
        if(context instanceof LiveActivity && !GolukUtils.isActivityAlive((LiveActivity) context)) {
            return;
        }
        if (null != mLiveExitDialog) {
            return;
        }
        mLiveExitDialog = new AlertDialog.Builder(context).create();

        mLiveExitDialog.setTitle(context.getString(R.string.user_dialog_hint_title));
        mLiveExitDialog.setMessage(message);
        mLiveExitDialog.setCancelable(false);

        mLiveExitDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.user_personal_sign_title), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        sendMessageCallBack(DIALOG_TYPE_EXIT_LIVE, FUNCTION_DIALOG_OK, null);
                        dimissLiveExitDialog();
                    }
                });
        mLiveExitDialog.show();

    }

    public void dimissLiveExitDialog() {
        if (null != mLiveExitDialog) {
            mLiveExitDialog.dismiss();
            mLiveExitDialog = null;
        }
    }

    public void showLiveBackDialog(Context context, int function, String message) {
        if(null == context) {
            return;
        }
        if(context instanceof LiveActivity && !GolukUtils.isActivityAlive((LiveActivity) context)) {
            return;
        }
        dismissLiveBackDialog();
        mCurrentDialogType = function;
        mLiveBackDialog = new AlertDialog.Builder(context).create();

        mLiveBackDialog.setTitle("");
        mLiveBackDialog.setMessage(message);
        mLiveBackDialog.setCancelable(false);

        mLiveBackDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.dialog_str_cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_CANCEL, null);
                        dismissLiveBackDialog();
                    }
                });

        mLiveBackDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.user_personal_sign_title), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        sendMessageCallBack(mCurrentDialogType, FUNCTION_DIALOG_OK, null);

                        dismissLiveBackDialog();
                    }
                });
        mLiveBackDialog.show();

    }

    public void dismissLiveBackDialog() {
        if (null != mLiveBackDialog) {
            mLiveBackDialog.dismiss();
            mLiveBackDialog = null;
        }
    }
}
