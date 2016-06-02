package com.goluk.ipcsdk.main;

import android.content.Context;

import com.goluk.ipcsdk.ipcCommond.BaseIPCCommand;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.logic.GolukLogic;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.api.Const;

/**
 * Created by leege100 on 16/5/30.
 */
public class GolukIPCSdk implements IPCManagerFn{

    public GolukLogic mGoluk = null;
    /** IPC回调监听列表 */
    private List<BaseIPCCommand> mIpcManagerListener = null;

    private static GolukIPCSdk instance;

    private GolukIPCSdk(){
        System.loadLibrary("golukmobile");
    }

    public static GolukIPCSdk getInstance(){
        if(instance == null){
            instance = new GolukIPCSdk();
        }
        return instance;
    }

    public void addCommand(BaseIPCCommand command) {
        this.mIpcManagerListener.add(command);
    }

//    public void unregisterIPC(Context context){
//        if(mIpcManagerListener != null){
//            for(BaseIPCCommand command:mIpcManagerListener){
//                if(command == null || command.getContext() == context){
//                    mIpcManagerListener.remove(command);
//                }
//            }
//        }
//    }
    public boolean initSDK(Context cxt){
        Const.setAppContext(cxt);
        if(mIpcManagerListener == null){
            mIpcManagerListener = new ArrayList<BaseIPCCommand>();
        }
        if (null != mGoluk) {
            return true;
        }

        mGoluk = new GolukLogic();
        setIpcMode(2);

        mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_IPCManager, this);

        return true;
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

        if(mIpcManagerListener != null){
            for(BaseIPCCommand command:mIpcManagerListener){
                if(command == null || command.getContext() == null){
                    mIpcManagerListener.remove(command);
                }else{
                    command.IPCManage_CallBack(event, msg, param1, param2);
                }
//                if(command == null ){
//                    command.IPCManage_CallBack(event, msg, param1, param2);
//                }
            }
        }
    }
}
