package com.mobnote.golukmain.thirdshare;

import com.mobnote.application.GolukApplication;

import android.app.Activity;
import android.view.View;

public class ProxyThirdShare implements IThirdShareFn {

	private IThirdShareFn mThirdShare = null;

	public ProxyThirdShare(Activity activity, SharePlatformUtil spf, ThirdShareBean bean) {
		if (GolukApplication.getInstance().isMainland()) {
			mThirdShare = new ChinaThirdShare(activity, spf, bean.surl, bean.curl, bean.db, bean.tl, bean.bitmap,
					bean.realDesc, bean.videoId, bean.mShareType,bean.filePath, bean.from);
		} else {
			mThirdShare = new AbroadThirdShare(activity, spf, bean.surl, bean.curl, bean.db, bean.tl, bean.bitmap,
					bean.realDesc, bean.videoId, bean.mShareType,bean.filePath, bean.from);
		}
	}

	@Override
	public void click(String type) {
		if (null != mThirdShare) {
			mThirdShare.click(type);
		}

	}

	@Override
	public void CallBack_Share(int event) {
		if (null != mThirdShare) {
			mThirdShare.CallBack_Share(event);
		}

	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		if (null != mThirdShare) {
			mThirdShare.showAtLocation(parent, gravity, x, y);
		}
	}

	@Override
	public void close() {
		if (null != mThirdShare) {
			mThirdShare.close();
		}

	}

	@Override
	public void setShareType(String type) {
		if (null != mThirdShare) {
			mThirdShare.setShareType(type);
		}

	}

}
