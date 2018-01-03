package com.goluk.ipcdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.goluk.ipcsdk.bean.DownloadInfo;
import com.goluk.ipcsdk.bean.FileInfo;
import com.goluk.ipcsdk.bean.RecordStorageState;
import com.goluk.ipcsdk.bean.VideoInfo;
import com.goluk.ipcsdk.command.IPCFileCommand;
import com.goluk.ipcsdk.listener.IPCFileListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;
import com.goluk.ipcsdk.utils.GolukIPCUtils;
import com.goluk.ipcsdk.utils.GolukUtils;

import java.io.File;
import java.util.ArrayList;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.api.FileUtils;

public class VideoDetailActivity extends Activity implements View.OnClickListener,IPCFileListener {
    public Button downloadimg;
    public Button downloadvideo;
    public Button stopdownload;
    private VideoView mVideoviewPlayer;
    public IPCFileCommand mIPCFileCommand;
    public EditText mSdStatus;

    private String filename;
    private long filetime;

    ProgressDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);
        Intent intent = this.getIntent();
        filename = intent.getStringExtra("filename");
        filetime = intent.getLongExtra("filetime",0);

        downloadimg = (Button) findViewById(R.id.download_img_btn);
        downloadvideo = (Button) findViewById(R.id.download_video_btn);
        mVideoviewPlayer = (VideoView) findViewById(R.id.videoviewPlayer);
        stopdownload = (Button) findViewById(R.id.stop_download_btn);
        mSdStatus = (EditText) findViewById(R.id.sd_status);
        downloadvideo.setOnClickListener(this);
        downloadimg.setOnClickListener(this);
        stopdownload.setOnClickListener(this);
        mIPCFileCommand = new IPCFileCommand(this,this);

        mIPCFileCommand.queryRecordStorageStatus();

        Uri uri = Uri.parse(GolukIPCUtils.getRemoteVideoUrl(filename));
        mVideoviewPlayer.setVideoURI(uri);
        mVideoviewPlayer.start();
    }

    @Override
    protected void onDestroy() {
        GolukIPCSdk.getInstance().unregisterIPC(this);
        super.onDestroy();
    }

    public String getSavepath(String filename){
        int type = GolukUtils.parseVideoFileType(filename);
        return GolukUtils.getSavePath(type);
    }

    public void createDialog(){
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        dialog.setMax(100);
        dialog.setButton("OK", new ProgressDialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.setCancelable(true);
        dialog.show();
        dialog.setProgress(0);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.download_video_btn){
            createDialog();
            mIPCFileCommand.downloadFile(filename,"videodownload",this.getSavepath(filename),filetime);
        }else if(v.getId() == R.id.download_img_btn){
            createDialog();
            final String imgFileName = filename.replace("mp4", "jpg");
            final String filePath = GolukIPCSdk.getInstance().getCarrecorderCachePath() + File.separator + "image";
            File file = new File(filePath + File.separator + imgFileName);
            if (!file.exists()) {
                mIPCFileCommand.downloadFile(imgFileName, "imgdownload", FileUtils.javaToLibPath(filePath), filetime);
            }
        }else if(v.getId() == R.id.stop_download_btn){
            boolean data = mIPCFileCommand.stopDownloadFile();
            if(data){
                Toast.makeText(this, "StopDownLoad SUCCESS", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "StopDownLoad FIAL", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void callback_query_files(ArrayList<VideoInfo> fileList) {

    }

    public String getStr(RecordStorageState recordStorgeState) {
        return " SDCardActive:" + recordStorgeState.SDCardActive + " isSpaceTooSmall:" + recordStorgeState.isSpaceTooSmall + " totalSdSize:" + recordStorgeState.totalSdSize + " isSpaceTooSmall:" + recordStorgeState.userFilesSize + " userFilesSize:" + recordStorgeState.leftSize + " leftSize:" + recordStorgeState.isSpaceTooSmall + " normalRecQuota:" + recordStorgeState.normalRecQuota + " normalRecSize:" + recordStorgeState.normalRecSize
                + " urgentRecQuota:" + recordStorgeState.urgentRecQuota
                + " urgentRecSize:" + recordStorgeState.urgentRecSize
                + " wonderfulRecQuota:" + recordStorgeState.wonderfulRecQuota
                + " wonderfulRecSize:" + recordStorgeState.wonderfulRecSize
                + " picQuota:" + recordStorgeState.picQuota
                + " picSize:" + recordStorgeState.picSize;
    }

    @Override
    public void callback_record_storage_status(RecordStorageState recordStorgeState) {
        if(recordStorgeState != null){

            mSdStatus.setText(getStr(recordStorgeState));
            Toast.makeText(this, "callback_record_storage_status success", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void callback_find_single_file(FileInfo fileInfo) {

    }

    @Override
    public void callback_download_file(DownloadInfo downloadinfo) {
        if(downloadinfo != null){
            if(downloadinfo.filerecvsize > 0){
                double  percent =((double) downloadinfo.filerecvsize)/ ((double) downloadinfo.filesize);
                int p = (int)(percent * 100);
                dialog.setProgress(p);
            }else{
                dialog.setProgress(100);
            }

            //Log.e("","zh filesize: " + downloadinfo.filesize + "  filerecvsize: " + downloadinfo.filerecvsize + "  status:" + downloadinfo.status);
            if(downloadinfo.status == 0){
                if (downloadinfo.filename.contains(".jpg")||downloadinfo.filename.contains(".png")){
                    Toast.makeText(this, "/sdcard/goluk/video/"+downloadinfo.filename+" download success", Toast.LENGTH_SHORT).show();
                }else{
                    if(GolukUtils.parseVideoFileType(filename) == IPCManagerFn.TYPE_SHORTCUT){
                        Toast.makeText(this, "/sdcard/goluk/video/wonderful/"+ downloadinfo.filename+ " download success", Toast.LENGTH_SHORT).show();
                    }else if(GolukUtils.parseVideoFileType(filename) == IPCManagerFn.TYPE_URGENT){
                        Toast.makeText(this, "/sdcard/goluk/video/urgent/"+ downloadinfo.filename+ " download success", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, "/sdcard/goluk/video/loop/"+ downloadinfo.filename+ " download success", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        }
    }
}
