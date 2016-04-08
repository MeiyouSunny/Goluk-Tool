package cn.com.mobnote.golukmobile.thirdshare.china;

import android.app.Activity;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;

public class ProxyThirdShare implements IThirdShareFn {

	public int type = 1;

	private IThirdShareFn mThirdShare = null;

	public ProxyThirdShare(Activity activity, SharePlatformUtil spf, ThirdShareBean bean) {

		if (type == 1) {
			mThirdShare = new ChinaThirdShare(activity, spf, bean.surl, bean.curl, bean.db, bean.tl, bean.bitmap,
					bean.realDesc, bean.videoId);
		} else {
			mThirdShare = new Abroad(activity, spf, bean.surl, bean.curl, bean.db, bean.tl, bean.bitmap, bean.realDesc,
					bean.videoId);
		}
	}

	@Override
	public void show() {
		if (null != mThirdShare) {
			mThirdShare.show();
		}
	}

	@Override
	public void click(String type) {
		if (null != mThirdShare) {
			mThirdShare.click(type);
		}

	}

	@Override
	public void setCurrentShareType(String type) {
		if (null != mThirdShare) {
			mThirdShare.setCurrentShareType(type);
		}

	}

	@Override
	public void CallBack_Share(int event) {
		if (null != mThirdShare) {
			mThirdShare.CallBack_Share(event);
		}

	}

}
