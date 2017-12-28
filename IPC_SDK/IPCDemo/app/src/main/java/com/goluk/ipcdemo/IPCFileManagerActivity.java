package com.goluk.ipcdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.goluk.ipcsdk.bean.DownloadInfo;
import com.goluk.ipcsdk.bean.FileInfo;
import com.goluk.ipcsdk.bean.RecordStorageState;
import com.goluk.ipcsdk.bean.VideoInfo;
import com.goluk.ipcsdk.command.IPCFileCommand;
import com.goluk.ipcsdk.listener.IPCFileListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;
import com.goluk.ipcsdk.utils.GolukUtils;

import java.io.File;
import java.util.ArrayList;

import cn.com.tiros.api.FileUtils;

public class IPCFileManagerActivity extends FragmentActivity implements View.OnClickListener,IPCFileListener {

    private Button mQueryFileList;
    private Button mGetSdStatus;
    private Button mFindSingleFile;
    private Button mDownloadFile;
    private Button mStopDownLoad;
    IPCFileCommand mIPCFileCommand;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filemanager);
        initView();
        initListener();
        initData();

    }

    private void initListener() {
        mQueryFileList.setOnClickListener(this);
        mGetSdStatus.setOnClickListener(this);
        mFindSingleFile.setOnClickListener(this);
        mDownloadFile.setOnClickListener(this);
        mStopDownLoad.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        GolukIPCSdk.getInstance().unregisterIPC(this);
        super.onDestroy();
    }


    private void initData() {
        mIPCFileCommand = new IPCFileCommand(this, this);
    }


    private void initView() {
        mQueryFileList = (Button) findViewById(R.id.btQueryFileList);
        mGetSdStatus = (Button) findViewById(R.id.btGetSdStatus);
        mFindSingleFile = (Button) findViewById(R.id.btFindSingleFile);
        mDownloadFile = (Button) findViewById(R.id.btDownLoadFile);
        mStopDownLoad = (Button) findViewById(R.id.btStopDownLoad);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btQueryFileList:
                Intent intent = new Intent(this,VideoListActivity.class);
                this.startActivity(intent);
                //boolean flog = mIPCFileCommand.queryFileListInfo(4, 20, 0, 2147483647, "1");
                break;
            case R.id.btGetSdStatus:
                mIPCFileCommand.queryRecordStorageStatus();
                break;
            case R.id.btFindSingleFile:
                mIPCFileCommand.querySingleFile("WND_event_20160602151234_1_TX_3_0030.mp4");
                break;
            case R.id.btDownLoadFile:
                String savePath = GolukUtils.getSavePath(4);
                Boolean result = mIPCFileCommand.downloadFile("WND_event_20160602151234_1_TX_3_0030.mp4","videodownload",savePath,1464851530);
                downLoadVideoThumbnail("WND_event_20160602151234_1_TX_3_0030.mp4",1464851530);
                break;
            case R.id.btStopDownLoad:
                boolean data = mIPCFileCommand.stopDownloadFile();
                if(data){
                    Toast.makeText(this, "StopDownLoad SUCCESS", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "StopDownLoad FIAL", Toast.LENGTH_SHORT).show();
                }
            default:
                break;
        }
    }

    // 下载视频第一帧图片
    private void downLoadVideoThumbnail(String videoFileName, long filetime) {
        final String imgFileName = videoFileName.replace("mp4", "jpg");
        final String filePath = GolukIPCSdk.getInstance().getCarrecorderCachePath() + File.separator + "image";
        File file = new File(filePath + File.separator + imgFileName);
        if (!file.exists()) {
            mIPCFileCommand.downloadFile(imgFileName, "imgdownload", FileUtils.javaToLibPath(filePath), filetime);
        }
    }

    @Override
    public void callback_query_files(ArrayList<VideoInfo> fileList) {
        if(fileList != null){
           // Toast.makeText(this, "callback_query_files  success", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void callback_record_storage_status(RecordStorageState recordStorgeState) {
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
            //Log.e("","zh filesize: " + downloadinfo.filesize + "  filerecvsize: " + downloadinfo.filerecvsize + "  status:" + downloadinfo.status);
            if(downloadinfo.status == 0){
               // Toast.makeText(this, "callback_download_file success", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
