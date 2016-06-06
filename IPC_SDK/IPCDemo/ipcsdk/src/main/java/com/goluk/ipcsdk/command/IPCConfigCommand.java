package com.goluk.ipcsdk.command;

import android.content.Context;

import com.goluk.ipcsdk.listener.IPCConfigListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;
import com.goluk.ipcsdk.utils.IPCDataParser;

import org.json.JSONObject;

import cn.com.mobnote.logic.GolukModule;

/**
 * Created by leege100 on 16/5/26.
 */
public class IPCConfigCommand extends BaseIPCCommand{

    private IPCConfigListener mIpcConfigListener;

    /**
     * @see com.goluk.ipcsdk.listener.IPCConfigListener
     * @param listener
     * @param cxt
     */
    public IPCConfigCommand(IPCConfigListener listener,Context cxt){
        super(cxt);
        this.mIpcConfigListener = listener;
    }

    /**
     * enable/disable audio record
     * @param isEnable
     * @return
     */
    public boolean setAudioRecordCfg(boolean isEnable){
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return false;
        }
        String json = "";
        try {
            JSONObject obj = new JSONObject();
            obj.put("AudioEnable", isEnable?1:0);

            json = obj.toString();
        } catch (Exception e) {
            json = "";
        }
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SetRecAudioCfg, json);
    }

    /**
     *
     * @return
     */
    public boolean getAudioRecordCfg(){
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return false;
        }
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_GetRecAudioCfg, "");
    }
    /**
     * update goluk carrecorder time
     * @see com.goluk.ipcsdk.listener.IPCConfigListener
     * @param time timestamp in seconds
     * @return
     */
    public boolean setTime(long time){

        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return false;
        }
        String json = "";
        try {
            JSONObject obj = new JSONObject();
            obj.put("time", time);

            json =  obj.toString();
        } catch (Exception e) {
        }
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_SetTime, json);
    }

    public boolean getTime(){
        if(!GolukIPCSdk.getInstance().isSdkValid()){
            return false;
        }
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_GetTime, "");
    }
    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
        }else if (ENetTransEvent_IPC_VDCP_CommandResp == event) {
            if(msg ==  IPC_VDCP_Msg_Init){
            }else if (msg == IPC_VDCP_Msg_GetTime) {
                if (param1 == RESULE_SUCESS) {
                    long time = IPCDataParser.parseIPCTime((String) param2) * 1000;
                    mIpcConfigListener.callback_getTime(time);
                }
            } else if (msg == IPC_VDCP_Msg_SetTime) {
                if (param1 == RESULE_SUCESS) {
                    mIpcConfigListener.callback_setTime(true);
                }else{
                    mIpcConfigListener.callback_setTime(true);
                }
            }else if (IPC_VDCP_Msg_SetRecAudioCfg == msg) {
                mIpcConfigListener.callback_setAudeoRecord(true);
            }else if (IPC_VDCP_Msg_GetRecAudioCfg == msg) {//声音录制
                if (RESULE_SUCESS == param1) {
                    try {
                        JSONObject obj = new JSONObject((String) param2);
                        int mVoiceRecordState = Integer.parseInt(obj.optString("AudioEnable"));
                        if(mVoiceRecordState == 1){
                            mIpcConfigListener.callback_getAudeoRecord(true);
                        }else if(mVoiceRecordState == 0){
                            mIpcConfigListener.callback_getAudeoRecord(false);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}
