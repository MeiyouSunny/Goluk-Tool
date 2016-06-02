package com.goluk.ipcdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.goluk.ipcsdk.bean.DownloadInfo;
import com.goluk.ipcsdk.bean.FileInfo;
import com.goluk.ipcsdk.bean.RecordStorgeState;
import com.goluk.ipcsdk.bean.VideoInfo;
import com.goluk.ipcsdk.ipcCommond.IPCConnCommand;
import com.goluk.ipcsdk.ipcCommond.IPCFileCommand;
import com.goluk.ipcsdk.listener.IPCConnListener;
import com.goluk.ipcsdk.listener.IPCFileListener;
import com.goluk.ipcsdk.utils.IPCConnectState;

import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener, IPCConnListener, IPCFileListener {

    private Button mConnWifiBt;
    private Button mConnIPCBt;
    private Button mIPCConfigBt;
    private Button mQueryFileList;
    private Button mGetSdStatus;
    private Button mFindSingleFile;
    private Button mDownloadFile;
    IPCConnCommand mIPCConnCommand;
    IPCFileCommand mIPCFileCommand;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();

    }

    private void initData() {
        mIPCConnCommand = new IPCConnCommand(this, this);
        mIPCFileCommand = new IPCFileCommand(this, this);
    }

    private void initView() {
        mConnWifiBt = (Button) findViewById(R.id.btConnWifi);
        mConnIPCBt = (Button) findViewById(R.id.btConnIPC);
        mIPCConfigBt = (Button) findViewById(R.id.btIPCConfig);
        mQueryFileList = (Button) findViewById(R.id.btQueryFileList);
        mGetSdStatus = (Button) findViewById(R.id.btGetSdStatus);
        mFindSingleFile = (Button) findViewById(R.id.btFindSingleFile);
        mDownloadFile = (Button) findViewById(R.id.btDownLoadFile);
    }

    private void initListener() {
        mConnWifiBt.setOnClickListener(this);
        mConnIPCBt.setOnClickListener(this);
        mIPCConfigBt.setOnClickListener(this);
        mQueryFileList.setOnClickListener(this);
        mGetSdStatus.setOnClickListener(this);
        mFindSingleFile.setOnClickListener(this);
        mDownloadFile.setOnClickListener(this);
    }

    private void startWifiCommond() {
        Intent intent = new Intent();
        intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
        startActivity(intent);
    }

    private void startIPCConfigActivity() {
        Intent intent = new Intent(this, IPCConfigActivity.class);
        this.startActivity(intent);
    }

    private void startConnectIPC() {
        mIPCConnCommand.connectIPC();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btConnWifi:
                startWifiCommond();
                break;
            case R.id.btConnIPC:
                startConnectIPC();
                break;
            case R.id.btIPCConfig:
                startIPCConfigActivity();
                break;
            case R.id.btQueryFileList:
                boolean flog = mIPCFileCommand.queryFileListInfo(4, 20, 0, 2147483647, "1");
                break;
            case R.id.btGetSdStatus:
                mIPCFileCommand.queryRecordStorageStatus();
                break;
            case R.id.btFindSingleFile:
                mIPCFileCommand.querySingleFile("WND_event_20160602151234_1_TX_3_0030.mp4");
                break;
            case R.id.btDownLoadFile:
                //mIPCFileCommand.downloadFile();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean wifiresult = IPCConnectState.getConnectState().getState();
        Toast.makeText(MainActivity.this, "wifi connect state :" + wifiresult, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void callback_ConnIPC(boolean isSuccess) {
        Toast.makeText(this, "IPC init success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void callback_query_files(ArrayList<VideoInfo> fileList) {
        if(fileList != null){
            Toast.makeText(this, "callback_query_files  success", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void callback_record_storage_status(RecordStorgeState recordStorgeState) {
        if(recordStorgeState != null){
            Toast.makeText(this, "callback_record_storage_status success", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void callback_find_single_file(FileInfo fileInfo) {
        if(fileInfo != null){
            Toast.makeText(this, "callback_find_single_file success", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void callback_download_file(DownloadInfo downloadinfo) {
        if(downloadinfo != null){
            Toast.makeText(this, "callback_find_single_file success", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this, "IPC Init Success", Toast.LENGTH_SHORT).show();
    }
}
