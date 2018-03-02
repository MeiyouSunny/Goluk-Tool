package com.mobnote.golukmain.carrecorder.view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;

import com.mobnote.golukmain.R;

import cn.com.tiros.debug.GolukDebugUtils;

public class CustomLoadingDialog {
    ForbidBack forbidInterface;
    private ProgressDialog mDialog;
    private String mMessage;
    private Context mContext;

    public CustomLoadingDialog(Context context, String txt) {
        mContext = context;
        mDialog = new ProgressDialog(context, R.style.CustomDialog);
        if (!"".equals(txt) && txt != null) {
            mMessage = txt;
        } else {
            mMessage = context.getResources().getString(R.string.str_loading_text);
        }
        mDialog.setMessage(mMessage);
    }

    public synchronized void show() {
        if (mDialog != null) {
            if (!mDialog.isShowing() && isActivityRunning()) {
                mDialog.show();
            }
        }
    }

    public synchronized boolean isShowing() {
        if (mDialog != null) {
            return mDialog.isShowing();
        }
        return false;
    }

    public synchronized void close() {
        if (mDialog != null && isActivityRunning()) {
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }
    }

    public void setListener(final ForbidBack forbidInterface) {
        if (null != forbidInterface) {
            this.forbidInterface = forbidInterface;
            mDialog.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_UP) {
                        GolukDebugUtils.e("", "------------------customDialog-------------back");
                        setData(1);
                    }
                    return true;
                }
            });
            this.mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    forbidInterface.forbidBackKey(1);
                }
            });
        }
    }

    private void setData(int key) {
        if (null != forbidInterface) {
            forbidInterface.forbidBackKey(key);
        }
    }

    public interface ForbidBack {
        public static final int BACK_OK = 1;

        void forbidBackKey(int backKey);
    }

    public void setCancel(boolean isCan) {
        if (null != mDialog) {
            mDialog.setCancelable(isCan);
        }
    }

    public void setTextTitle(String title) {
        mDialog.setMessage(title);
    }

    /**
     * Return activity is destory
     */
    private boolean isActivityRunning() {
        if (mContext == null && !(mContext instanceof Activity))
            return false;

        return !((Activity) mContext).isFinishing() && !((Activity) mContext).isDestroyed();
    }

//	AlertDialog customDialog;
//	String textTitle;
//	AnimationDrawable ad;
//	ForbidBack forbidInterface;
//    ImageView mLoadingImageIV;
//    TextView mLoadingTextTV;
//
//	public CustomLoadingDialog(Context context, String txt) {
//		customDialog = new AlertDialog.Builder(context, R.style.CustomDialog).create();
//		if (txt != null) {
//			textTitle = txt;
//		}
//
//	}
//
//	public void setCancel(boolean isCan) {
//		if (null != customDialog) {
//			customDialog.setCancelable(isCan);
//		}
//	}
//
//	public void setListener(ForbidBack forbidInterface) {
//		if (null != forbidInterface) {
//			this.forbidInterface = forbidInterface;
//		}
//	}
//
//	private void setData(int key) {
//		if (null != forbidInterface) {
//			forbidInterface.forbidBackKey(key);
//		}
//	}
//
//    public void setTextTitle(String title) {
//        mLoadingTextTV.setText(title);
//    }
//
//	public void show() {
//		customDialog.setOnShowListener(new OnShowListener() {
//			@Override
//			public void onShow(DialogInterface arg0) {
//                mLoadingImageIV = (ImageView) customDialog.getWindow().findViewById(R.id.loading_img);
//                mLoadingTextTV = (TextView) customDialog.getWindow().findViewById(R.id.loading_text);
//				if (textTitle != null && !"".equals(textTitle)) {
//                    mLoadingTextTV.setText(textTitle);
//				}
//				ad = (AnimationDrawable) mLoadingImageIV.getBackground();
//
//				if (ad != null) {
//					ad.start();
//				}
//			}
//		});
//
//		customDialog.setCanceledOnTouchOutside(false);
//		customDialog.show();
//		customDialog.getWindow().setContentView(R.layout.video_square_loading);
//
//		customDialog.setOnKeyListener(new OnKeyListener() {
//
//			@Override
//			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
//
//				if (arg2.getAction() == KeyEvent.ACTION_UP) {
//					GolukDebugUtils.e("", "------------------customDialog-------------back");
//					setData(1);
//				}
//				return false;
//			}
//		});
//
//	}
//
//	public void close() {
//		if (customDialog != null) {
//			if (customDialog.isShowing()) {
//				if (ad != null) {
//					ad.stop();
//				}
//				customDialog.dismiss();
//			}
//		}
//	}
//
//	public boolean isShowing() {
//		if (null != customDialog) {
//			return customDialog.isShowing();
//		}
//
//		return false;
//	}
//
//	public interface ForbidBack {
//		public static final int BACK_OK = 1;
//
//		void forbidBackKey(int backKey);
//	}

}
