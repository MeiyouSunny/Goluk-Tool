package com.goluk.ipcsdk.main;

import android.util.Log;

import com.goluk.ipcsdk.bean.BaseIPCCommand;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.logic.GolukLogic;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

/**
 * Created by leege100 on 16/5/30.
 */
public class GolukIPCSdk implements IPCManagerFn{

    public GolukLogic mGoluk = null;
    /** IPC回调监听列表 */
    private List<BaseIPCCommand> mIpcManagerListener = null;

    private static GolukIPCSdk instance;

    private GolukIPCSdk(){
        Log.i("GolukIPCSdk","loadLib");
        System.loadLibrary("golukmobile");
    }

    public static GolukIPCSdk getInstance(){
        if(instance == null){
            instance = new GolukIPCSdk();
        }
        return instance;
    }

    public void registerCommand(BaseIPCCommand command) {
        this.mIpcManagerListener.add(command);
    }

//    public void unRegisterCommand(Context from) {
//        this.mIpcManagerListener.remove(from);
//    }
    public boolean initSDK(){
        if(mIpcManagerListener == null){
            mIpcManagerListener = new ArrayList<BaseIPCCommand>();
        }
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
    // VDCP 连接状态 回调
    private void IPC_VDCP_Connect_CallBack(int msg, int param1, Object param2) {

        // 如果不是连接成功,都标识为失败
        switch (msg) {
            case ConnectionStateMsg_Idle:

                break;
            case ConnectionStateMsg_Connecting:

                break;
            case ConnectionStateMsg_Connected:
                // 只是,ipc信号连接了,初始化的东西还没完成,所以要等到ipc初始化成功,才能把isIpcLoginSuccess=true
                break;
            case ConnectionStateMsg_DisConnected:

                break;
        }
    }

    private void IPC_VDC_CommandResp_CallBack(int event, int msg, int param1, Object param2) {
        switch (msg) {
            case IPC_VDCP_Msg_Init:
//                IPC_VDCP_Command_Init_CallBack(msg, param1, param2);
                if(event == 1 && msg == 0 && param1 == 0){
                    //ipc初始化成功
                }
                break;
        }
    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
//        if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
//            Log.i("zh","zh callback : " + msg + "----param1" + param1 + "---param2 " + param2);
//            IPC_VDCP_Connect_CallBack(msg, param1, param2);
//        }else if (ENetTransEvent_IPC_VDCP_CommandResp == event) {
//            if(msg ==  IPC_VDCP_Msg_Init){
//                IPC_VDC_CommandResp_CallBack(event, msg, param1, param2);
//            }else if (msg == IPC_VDCP_Msg_GetTime) {
//                //ipcCallBack_GetTime(param1, param2);
//            } else if (msg == IPC_VDCP_Msg_SetTime) {
//                //ipcCallBack_SetTime(param1, param2);
//            }else if (IPC_VDCP_Msg_SetRecAudioCfg == msg) {
//                //callback_setVoiceRecord(event, msg, param1, param2);
//            }else if (IPC_VDCP_Msg_GetRecAudioCfg == msg) {//声音录制
//                //callback_getVoiceRecord(event, msg, param1, param2);
//            }
//        }
        if(mIpcManagerListener != null){
            for(BaseIPCCommand command:mIpcManagerListener){
                if(command == null || command.getContext() == null){
                    mIpcManagerListener.remove(command);
                }else{
                    command.IPCManage_CallBack(event, msg, param1, param2);
                }
            }
        }
    }
}
