package cn.com.mobnote.golukmobile.startshare;

public interface IUploadVideoFn {
	public static final int EVENT_EXIT = 0;
	public static final int EVENT_UPLOAD_SUCESS = 1;
	public static final int EVENT_PROCESS = 2;

	public void CallBack_UploadVideo(int event, Object obj);

}
