package com.goluk.ipcsdk.ipcCommond;

import android.content.Context;

import com.goluk.ipcsdk.listener.IPCConfigListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;

import org.json.JSONObject;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

/**
 * Created by leege100 on 16/5/26.
 */
public class IPCConfigCommand extends BaseIPCCommand{

    private IPCConfigListener mIpcConfigListener;

    /**
     *
     * @param listener
     */
    public IPCConfigCommand(IPCConfigListener listener,Context cxt){
        super(cxt);
        this.mIpcConfigListener = listener;
    }

    /**
     * enable/disable audio record
     * @see com.goluk.ipcsdk.listener.IPCConfigListener
     * @param isEnable
     * @return
     */
    public boolean setAudioRecordCfg(boolean isEnable){
        String json = "";
        try {
            JSONObject obj = new JSONObject();
            obj.put("AudioEnable", isEnable?1:0);

            json = obj.toString();
        } catch (Exception e) {
            json = "";
        }
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPCManagerFn.IPC_VDCPCmd_SetRecAudioCfg, json);
    }

    /**
     *
     * @return
     */
    public boolean getAudioRecordCfg(){
        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager,
                IPC_VDCPCmd_GetRecAudioCfg, "");
    }
    /**
     * update goluk carrecorder time
     * @see com.goluk.ipcsdk.listener.IPCConfigListener
     * @param timeStamp timestamp in seconds
     * @return
     */
    public boolean setTime(long timeStamp){

        return false;
    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
        }else if (ENetTransEvent_IPC_VDCP_CommandResp == event) {
            if(msg ==  IPC_VDCP_Msg_Init){
            }else if (msg == IPC_VDCP_Msg_GetTime) {
            } else if (msg == IPC_VDCP_Msg_SetTime) {
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
