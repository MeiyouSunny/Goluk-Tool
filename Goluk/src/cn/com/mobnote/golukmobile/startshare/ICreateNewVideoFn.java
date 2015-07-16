package cn.com.mobnote.golukmobile.startshare;

public interface ICreateNewVideoFn {

	public static final int EVENT_START = 0;
	public static final int EVENT_SAVING = 1;
	public static final int EVENT_END = 2;
	public static final int EVENT_ERROR = 3;

	public void CallBack_CreateNewVideoFn(int event, Object obj1, Object obj2, Object obj3);

}
