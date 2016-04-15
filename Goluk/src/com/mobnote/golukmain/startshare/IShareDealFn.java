package com.mobnote.golukmain.startshare;

import com.mobnote.golukmain.thirdshare.ThirdShareBean;

import android.content.Intent;

public interface IShareDealFn {

	public void toShare(ThirdShareBean bean);

	public void onActivityResult(int requestCode, int resultCode, Intent data);

	public void setExit();

}
