package com.goluk.ipcsdk.main;

import android.content.Context;
import android.os.Environment;

import com.goluk.ipcsdk.ipcCommond.BaseIPCCommand;
import com.goluk.ipcsdk.utils.Utils;

import org.json.JSONObject;

import java.io.File;
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

    /** 行车记录仪缓冲路径 */
    private String carrecorderCachePath = "";

    private static GolukIPCSdk instance;
    private String mAppId;

    private GolukIPCSdk(){
        System.loadLibrary("golukmobile");
        System.loadLibrary("LiveCarRecorder");
        initCachePath();
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

    public void unregisterIPC(Context context){
        if(mIpcManagerListener != null){
            for(BaseIPCCommand command:mIpcManagerListener){
                if(command == null || command.getContext() == context){
                    mIpcManagerListener.remove(command);
                }
            }
        }
    }

    public void initSDK(Context cxt,String appId){
        if (null != mGoluk) {
            return ;
        }
        this.mAppId = appId;
        Const.setAppContext(cxt);
        if(mIpcManagerListener == null){
            mIpcManagerListener = new ArrayList<BaseIPCCommand>();
        }
        mGoluk = new GolukLogic();
        setIpcMode(2);

        mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_IPCManager, this);
    }

    /**
     * 创建行车记录仪缓冲路径
     *
     * @author xuhw
     * @date 2015年3月19日
     */
    private void initCachePath() {
        carrecorderCachePath = Environment.getExternalStorageDirectory() + File.separator + "g_video" + File.separator
                + "goluk_cache";
        Utils.makedir(carrecorderCachePath);
    }

    /**
     * 获取行车记录仪缓冲路径
     *
     * @return
     * @author xuhw
     * @date 2015年3月19日
     */
    public String getCarrecorderCachePath() {
        return this.carrecorderCachePath;
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
            }
        }
    }
}
