package cn.com.mobnote.golukmobile.thirdshare.china;

import com.umeng.socialize.Config;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;

public abstract class AbsThirdShare implements IThirdShareFn {

	protected Activity mActivity;
	/** 保存当前的分享方式 */
	protected String mCurrentShareType = "2";
	protected SharePlatformUtil sharePlatform;
	protected String shareurl = "";
	protected String mImageUrl = "";
	protected String mDescribe = "";
	protected String mTitle = "";
	protected Bitmap mThumbBitmap = null;
	/** 新浪微博Txt */
	protected String mSinaTxt = null;
	/** 视频Id ,用户服务器上报　 */
	protected String mVideoId = null;

	public AbsThirdShare(Activity activity, SharePlatformUtil spf, String surl, String curl, String db, String tl,
			Bitmap bitmap, String realDesc, String videoId) {
		this.mActivity = activity;
		sharePlatform = spf;
		shareurl = surl;
		mImageUrl = curl;
		mDescribe = db;
		mTitle = tl;
		mThumbBitmap = bitmap;
		mSinaTxt = realDesc;
		mVideoId = videoId;
		
		
	}

	

}
