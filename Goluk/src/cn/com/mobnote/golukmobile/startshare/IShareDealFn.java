package cn.com.mobnote.golukmobile.startshare;

import android.content.Intent;
import cn.com.mobnote.golukmobile.thirdshare.china.ThirdShareBean;

public interface IShareDealFn {

	public void toShare(ThirdShareBean bean);

	public void onActivityResult(int requestCode, int resultCode, Intent data);

	public void setExit();

}
