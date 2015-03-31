package cn.com.mobnote.logic;

public class GolukLogicJni {

	public static native long GolukLogicCreate();

	public static native void GolukLogicDestroy(long pLogic);

	public static native int GolukLogicRegisterNotify(long pLogic, int mID);

	public static native boolean GolukLogicCommRequest(long pLogic, int mId, int cmd, String param);

	public static native String GolukLogicCommGet(long pLogic, int mID, int cmd, String param);

}
