package com.goluk.ipcsdk.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.goluk.ipcsdk.command.BaseIPCCommand;
import com.goluk.ipcsdk.listener.IPCInitListener;
import com.goluk.ipcsdk.utils.GolukUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.mobnote.logic.GolukLogic;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.api.Const;
import cn.com.tiros.api.IpcWifiManager;

/**
 * Created by leege100 on 16/5/30.
 */
public class GolukIPCSdk implements IPCManagerFn {

    private IPCInitListener mIPCInitListener;
    public GolukLogic mGoluk = null;
    private List<BaseIPCCommand> mCommandList = null;
    private String carrecorderCachePath = "";

    private static GolukIPCSdk instance;
    private String mAppId;
    private int mIpcMode;

    private boolean isIPCLogicInited;
    public static Context mAPPContext;

    private GolukIPCSdk() {
        System.loadLibrary("golukmobile");
        System.loadLibrary("LiveCarRecorder");
        initCachePath();
    }

    public static GolukIPCSdk getInstance() {
        if (instance == null) {
            instance = new GolukIPCSdk();
        }
        return instance;
    }

    public boolean isSdkValid() {
        if (isIPCLogicInited && isAppAuthValid()) {
            return true;
        } else {
            return false;
        }
    }

    public void addCommand(BaseIPCCommand command) {
        if (isSdkValid()) {
            this.mCommandList.add(command);
        }
    }

    synchronized public void unregisterIPC(Context context) {
        if (mCommandList != null) {
            for (BaseIPCCommand command : mCommandList) {
                if (command == null || command.getContext() == context) {
                    mCommandList.remove(command);
                }
            }
        }
    }

    public void initSDK(Context cxt, String appId, IPCInitListener listener) {

        this.mAPPContext = cxt;
        this.mAppId = appId;
        this.mIPCInitListener = listener;

        if (mCommandList == null) {
            mCommandList = new CopyOnWriteArrayList<BaseIPCCommand>();
        }

        // Init WIfiManager
        IpcWifiManager.init(cxt.getApplicationContext());

        initIPCLogic();
        new GetAuthAsyncTask(mAppId).execute();

    }

    private boolean isAppAuthValid() {

        SharedPreferences sharedPreferences = mAPPContext.getSharedPreferences("golukIPCSdk", Context.MODE_PRIVATE);

        String startTime = sharedPreferences.getString("startTime", "");
        String endTime = sharedPreferences.getString("endTime", "");
        if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
            Date startDate = getDate(startTime);
            Date endDate = getDate(endTime);
            Date currDate = new Date();
            if (startDate.before(currDate) && endDate.after(currDate)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param dateStr
     * @return
     */
    private Date getDate(String dateStr) {
        Date date = new Date();
        DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        try {
            date = sdf.parse(dateStr);
            System.out.println(date.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    private void initIPCLogic() {
        if (!isAppAuthValid()) {
            return;
        }

        if (null != mGoluk) {
            return;
        }
        Const.setAppContext(mAPPContext);

        mGoluk = new GolukLogic();
        setIpcMode(0);

        mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_IPCManager, this);

        if (mIPCInitListener != null) {
            mIPCInitListener.initCallback(true, "success");
            mIPCInitListener = null;
        }
        isIPCLogicInited = true;
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
        GolukUtils.makedir(carrecorderCachePath);
    }

    /**
     * get carrecorder cache path
     *
     * @return
     * @author xuhw
     * @date 2015年3月19日
     */
    public String getCarrecorderCachePath() {
        return this.carrecorderCachePath;
    }

    private class GetAuthAsyncTask extends AsyncTask<Integer, Integer, String> {

        private String appId;

        /**
         * @param id
         */
        public GetAuthAsyncTask(String id) {
            this.appId = id;
        }

        @Override
        protected String doInBackground(Integer... params) {
            String postUrl = "https://s.goluk.cn/cdcGraph/open.htm";
            Map<String, String> map = new HashMap<>();
            map.put("method", "appConfig");
            map.put("xieyi", "200");
            map.put("appid", appId);

            HttpURLConnection connection = null;
            URL url;

            StringBuffer sb = new StringBuffer();
            if (map != null) {
                for (Map.Entry<String, String> e : map.entrySet()) {
                    sb.append(e.getKey());
                    sb.append("=");
                    sb.append(e.getValue());
                    sb.append("&");
                }
                sb.substring(0, sb.length() - 1);
            }
            try {
                url = new URL(postUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setConnectTimeout(5000);

                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
                osw.write(sb.toString());
                osw.flush();
                osw.close();

                int responseCode = connection.getResponseCode();

                if (responseCode == 200) {
                    StringBuffer buffer = new StringBuffer();
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String temp;
                    while ((temp = br.readLine()) != null) {
                        buffer.append(temp);
                        buffer.append("\n");
                        if (buffer != null) {
                            return buffer.toString();
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            if (TextUtils.isEmpty(result)) {
                if (mIPCInitListener != null) {
                    mIPCInitListener.initCallback(isAppAuthValid(), result);
                    mIPCInitListener = null;
                }
                return;
            }

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
                //Log.i("IPCSdk","result:" + result);
                JSONObject dataObject = jsonObject.getJSONObject("data");
                String startTime = dataObject.getString("starttime");
                String endTime = dataObject.getString("endtime");
                if (!TextUtils.isEmpty(startTime) && !TextUtils.isEmpty(endTime)) {
                    SharedPreferences sharedPreferences = mAPPContext.getSharedPreferences("golukIPCSdk", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
                    editor.putString("startTime", startTime);
                    editor.putString("endTime", endTime);
                    editor.commit();//提交修改
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            initIPCLogic();
        }
    }

    /**
     * set conn mode
     *
     * @param mode 0/1/2
     * @author jyf
     */
    public boolean setIpcMode(int mode) {
        if (mode < 0) {
            return false;
        }

        if (mIpcMode == mode)
            return true;

        String json = "";
        try {
            JSONObject obj = new JSONObject();
            obj.put("mode", mode);
            json = obj.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_IPCManager, IPC_CommCmd_SetMode, json);
    }

    /**
     * IPC changed
     */
    public boolean changeIpcMode() {
        int ipcModeValue = IpcWifiManager.getIpcModeValue();
        if (ipcModeValue != -1)
            return setIpcMode(ipcModeValue);

        return false;
    }

    @Override
    public void IPCManage_CallBack(int event, int msg, int param1, Object param2) {

        if (mCommandList != null) {
            for (BaseIPCCommand command : mCommandList) {
                if (command == null || command.getContext() == null) {
                    mCommandList.remove(command);
                } else {
                    command.IPCManage_CallBack(event, msg, param1, param2);
                }
            }
        }
    }
}
