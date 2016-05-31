package cn.com.mobnote.logic;

/**
 * Created by leege100 on 16/5/30.
 */
public class GolukLogicJni {
    public static native long GolukLogicCreate();

    public static native void GolukLogicDestroy(long pLogic);

    public static native int GolukLogicRegisterNotify(long pLogic, int mID);

    public static native boolean GolukLogicCommRequest(long pLogic, int mId, int cmd, String param);

    public static native String GolukLogicCommGet(long pLogic, int mID, int cmd, String param);

    // 通用请求函数(支持并发)
    public static native long CommRequestEx(long pLogic, int mID, int cmd, String param);

}
