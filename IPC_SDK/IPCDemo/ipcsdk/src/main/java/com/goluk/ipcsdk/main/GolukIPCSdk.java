package com.goluk.ipcsdk.main;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.logic.GolukLogic;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

/**
 * Created by leege100 on 16/5/30.
 */
public class GolukIPCSdk implements IPCManagerFn{

    public static final String G1_SIGN = "G1";
    public static final String G2_SIGN = "G2";
    public static final String T1_SIGN = "T1";
    public static final String T1s_SIGN = "T1S";
    public GolukLogic mGoluk = null;

    private static GolukIPCSdk instance = new GolukIPCSdk();

    private GolukIPCSdk(){
    }

    public static GolukIPCSdk getInstance(){
        return instance;
    }

    public boolean initSDK(){
        System.loadLibrary("golukmobile");

        if (null != mGoluk) {
            return true;
        }

        // 实例化JIN接口,请求网络数据
        mGoluk = new GolukLogic();
        setIpcMode(2);
        setIPCWifiState(true,"192.168.62.1");

        int result = mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_IPCManager, this);
        Log.i("registerResult",result + "");
        return true;
    }

    public boolean setIPCWifiState(boolean isConnect,String ip) {

        int state = isConnect ? 1 : 0;
        String json = "";
        JSONObject obj = new JSONObject();
        try {
            obj.put("state", state);
            obj.put("domain", ip);
            json = obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean isSucess = mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_CommCmd_WifiChanged, json);
        return isSucess;
    }

    /**
     * 设置IPC 连接模式
     *
     * @param mode
     *            0/1/2
     * @author jyf
     */
    private void setIpcMode(int mode) {
        if (mode < 0) {
            return;
        }
        String json = "";
        try {
            JSONObject obj = new JSONObject();
            obj.put("mode", mode);

            json = obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_CommCmd_SetMode, json);

    }
    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        Log.i("ipc_callback","GolukIPCSdk");
        Log.i("ipc_callback",msg + " " + param2);
    }
}
