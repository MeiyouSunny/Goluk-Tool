package com.goluk.ipcsdk.command;

import android.content.Context;

import com.goluk.ipcsdk.listener.IPCConnListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.logic.GolukModule;

/**
 * Created by leege100 on 16/6/1.
 */
public class IPCConnCommand extends BaseIPCCommand {

    IPCConnListener mIPCConnListener;

    /**
     * @param listener
     * @param cxt
     * @see IPCConnListener
     */
    public IPCConnCommand(IPCConnListener listener, Context cxt) {
        super(cxt);
        this.mIPCConnListener = listener;
    }

    /**
     * connect the IPC
     *
     * @return
     */
    public boolean connectIPC() {

        if (!GolukIPCSdk.getInstance().isSdkValid()) {
            return false;
        }

        if (!GolukIPCSdk.getInstance().changeIpcMode())
            return false;

        String json = "";
        JSONObject obj = new JSONObject();
        try {
            obj.put("state", 1);
            obj.put("domain", "192.168.62.1");
            json = obj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return GolukIPCSdk.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_CommCmd_WifiChanged, json);
    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {
        if (ENetTransEvent_IPC_VDCP_ConnectState == event) {
            // 如果不是连接成功,都标识为失败
            if (msg == ConnectionStateMsg_Idle) {

            } else if (msg == ConnectionStateMsg_Connecting) {

            } else if (msg == ConnectionStateMsg_Connected) {

            } else if (msg == ConnectionStateMsg_DisConnected) {

            } else {
            }
        } else if (ENetTransEvent_IPC_VDCP_CommandResp == event) {
            if (msg == IPC_VDCP_Msg_Init) {
                if (param1 == 0) {
                    mIPCConnListener.callback_ConnIPC(true);
                }
            } else if (msg == IPC_VDCP_Msg_GetTime) {
                //ipcCallBack_GetTime(param1, param2);
            } else if (msg == IPC_VDCP_Msg_SetTime) {
                //ipcCallBack_SetTime(param1, param2);
            } else if (IPC_VDCP_Msg_SetRecAudioCfg == msg) {
                //callback_setVoiceRecord(event, msg, param1, param2);
            } else if (IPC_VDCP_Msg_GetRecAudioCfg == msg) {//声音录制
                //callback_getVoiceRecord(event, msg, param1, param2);
            }
        }
    }
}
