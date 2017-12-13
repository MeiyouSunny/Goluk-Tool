package com.mobnote.golukmain.thirdshare;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.startshare.VideoShareActivity;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;
import com.umeng.socialize.Config;
import com.umeng.socialize.ShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import cn.com.tiros.debug.GolukDebugUtils;

public abstract class AbsThirdShare implements IThirdShareFn {

    protected Activity mActivity;
    /**
     * 保存当前的分享方式
     */
    protected String mCurrentShareType = "2";
    protected SharePlatformUtil sharePlatform;
    protected String shareurl = "";
    protected String mImageUrl = "";
    protected String mDescribe = "";
    protected String mTitle = "";
    protected Bitmap mThumbBitmap = null;
    /**
     * 新浪微博Txt
     */
    protected String mSinaTxt = null;
    /**
     * 视频Id ,用户服务器上报
     */
    protected String mVideoId = null;
    /**
     * 0:普通列表分享 1:即刻分享
     **/
    protected String mShareType = "";
    /**
     * 视频文件的本地地址
     **/
    protected String filepath = "";
    /**
     * 分享来源
     **/
    protected String mFrom = "";

    public AbsThirdShare(Activity activity, SharePlatformUtil spf, String surl, String curl, String db, String tl,
                         Bitmap bitmap, String realDesc, String videoId, String shareType, String filePath, String from) {
        this.mActivity = activity;
        sharePlatform = spf;
        shareurl = surl;
        mImageUrl = curl;
        mDescribe = db;
        mTitle = tl;
        mThumbBitmap = bitmap;
        mSinaTxt = realDesc;
        mVideoId = videoId;
        filepath = filePath;
        this.mShareType = shareType;
        this.mFrom = from;

        if (TextUtils.isEmpty(mDescribe)) {
            mDescribe = mActivity.getResources().getString(R.string.app_name);
        }
    }

    protected Handler mHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    callBack_ShareSuccess();
                    break;
                case 101:
                    callBack_ShareFailed();
                    break;
                case 102:
                    GolukUtils.showToast(mActivity, mActivity.getString(R.string.um_share_cancel));
                    break;
                default:
                    break;
            }
        }

    };

    protected void modifyUMDialog() {
        ProgressDialog dialog = new ProgressDialog(mActivity);
        dialog.setMessage(mActivity.getString(R.string.str_um_share_dialog_txt));
        dialog.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface arg0) {
                setCanJump();
            }

        });
        //Config.dialog = dialog;
    }

    protected void printStr() {
        GolukDebugUtils.e("", "AbroadThirdShare-----shareurl: " + shareurl + "   coverurl:" + mImageUrl
                + "   describe:" + mDescribe + " ttl: " + mTitle);
    }

    protected boolean isCanClick() {
        if (null != mActivity && mActivity instanceof BaseActivity) {
            if (!((BaseActivity) mActivity).isAllowedClicked()) {
                return false;
            }
            ((BaseActivity) mActivity).setJumpToNext();
            return true;
        }
        return false;
    }

    public void setShareType(String type) {
        mCurrentShareType = type;
    }

    /**
     * 上报分享服务器
     *
     * @author jyf
     * @date 2015年8月11日
     */
    protected void shareUp() {
        GolukDebugUtils.e("", "jyf----thirdshare--------share up: " + "   mVideoId:" + mVideoId);
        if (this.mVideoId != null && !"".equals(this.mVideoId)) {
            GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(mCurrentShareType, this.mVideoId);
        }
    }

    protected void setCanJump() {
        if (null != mActivity && mActivity instanceof BaseActivity) {
            GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----callBack_ShareFailed----2");
            ((BaseActivity) mActivity).setCanJump();
        }
    }

    protected void notifyShareState(boolean isSucess) {
        if (null == mActivity) {
            return;
        }

        if (mActivity instanceof VideoShareActivity) {
            ((VideoShareActivity) mActivity).shareCallBack(isSucess);
        }
    }

    protected ShareContent getShareContent(String shareType) {
        if (null == shareType) {
            return null;
        }
        //分享视频
        if (null != mFrom && mFrom.equals(mActivity.getString(R.string.str_zhuge_live_share_event))) {
            ZhugeUtils.eventLiveShare(mActivity, shareType);
        } else {
            ZhugeUtils.eventShareVideo(mActivity, shareType, mFrom);
        }
        final String videoUrl = shareurl + "&type=" + shareType;
        ShareContent sc = new ShareContent();
        UMWeb web = new UMWeb(videoUrl);
        web.setTitle(mTitle);
        web.setDescription(mDescribe);
        if (!TextUtils.isEmpty(mImageUrl)) {
            final UMImage image = new UMImage(mActivity, mImageUrl);
            web.setThumb(image);
        }
        return sc;
    }

    // 分享成功后的回调
    protected void callBack_ShareSuccess() {
        notifyShareState(true);
        setCanJump();
        if (GolukUtils.isActivityAlive(mActivity)) {
            close();
        }
        Toast.makeText(mActivity, mActivity.getString(R.string.str_share_success), Toast.LENGTH_SHORT).show();
    }

    // 分享失败的回调
    protected void callBack_ShareFailed() {
        GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----callBack_ShareFailed----1");
        notifyShareState(false);
        GolukUtils.showToast(mActivity, mActivity.getString(R.string.str_share_fail));
        // 分享失败的时候，保证下次还可以点击
        setCanJump();
        if (GolukUtils.isActivityAlive(mActivity)) {
            GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----callBack_ShareFailed----3");
            close();
        }
        GolukDebugUtils.e("", "youmeng----goluk----customShareBoard----callBack_ShareFailed----4");
    }

}
