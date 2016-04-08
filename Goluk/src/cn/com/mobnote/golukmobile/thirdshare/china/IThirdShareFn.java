package cn.com.mobnote.golukmobile.thirdshare.china;

public interface IThirdShareFn {

	// 显示窗口
	public void show();

	// 单击 "微信"， “微博 ”, "facebook"
	public void click(String type);

	public void setCurrentShareType(String type);

	// 分享回调
	public void CallBack_Share(int event);

}
