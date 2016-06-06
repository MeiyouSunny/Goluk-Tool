package com.goluk.ipcdemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.goluk.ipcsdk.bean.DownloadInfo;
import com.goluk.ipcsdk.bean.FileInfo;
import com.goluk.ipcsdk.bean.RecordStorgeState;
import com.goluk.ipcsdk.bean.VideoInfo;
import com.goluk.ipcsdk.commond.IPCFileCommand;
import com.goluk.ipcsdk.listener.IPCFileListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;
import com.goluk.ipcsdk.utils.GolukUtils;

import java.io.File;
import java.util.ArrayList;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;
import cn.com.tiros.api.FileUtils;

public class VideoDetailActivity extends Activity implements View.OnClickListener,IPCFileListener {
    public Button downloadimg;
    public Button downloadvideo;
    private VideoView mVideoviewPlayer;
    public IPCFileCommand mIPCFileCommand;

    private String filename;
    private long filetime;

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
        downloadvideo.setOnClickListener(this);
        downloadimg.setOnClickListener(this);
        mIPCFileCommand = new IPCFileCommand(this,this);

        Uri uri = Uri.parse(GolukUtils.getRemoteVideoUrl(filename));
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


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.download_video_btn){
            mIPCFileCommand.downloadFile(filename,"videodownload",this.getSavepath(filename),filetime);
        }else if(v.getId() == R.id.download_img_btn){
            final String imgFileName = filename.replace("mp4", "jpg");
            final String filePath = GolukIPCSdk.getInstance().getCarrecorderCachePath() + File.separator + "image";
            File file = new File(filePath + File.separator + imgFileName);
            if (!file.exists()) {
                mIPCFileCommand.downloadFile(imgFileName, "imgdownload", FileUtils.javaToLibPath(filePath), filetime);
            }
        }
    }

    @Override
    public void callback_query_files(ArrayList<VideoInfo> fileList) {

    }

    @Override
    public void callback_record_storage_status(RecordStorgeState recordStorgeState) {

    }

    @Override
    public void callback_find_single_file(FileInfo fileInfo) {

    }

    @Override
    public void callback_download_file(DownloadInfo downloadinfo) {
        if(downloadinfo != null){
            Log.e("","zh filesize: " + downloadinfo.filesize + "  filerecvsize: " + downloadinfo.filerecvsize + "  status:" + downloadinfo.status);
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
