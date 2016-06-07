package com.goluk.ipcdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.goluk.ipcsdk.bean.DownloadInfo;
import com.goluk.ipcsdk.bean.FileInfo;
import com.goluk.ipcsdk.bean.RecordStorageState;
import com.goluk.ipcsdk.bean.VideoInfo;
import com.goluk.ipcsdk.command.IPCFileCommand;
import com.goluk.ipcsdk.listener.IPCFileListener;
import com.goluk.ipcsdk.main.GolukIPCSdk;

import java.util.ArrayList;

import cn.com.mobnote.module.ipcmanager.IPCManagerFn;

public class VideoListActivity extends Activity implements IPCFileListener,View.OnClickListener{
    private ListView mListView;

    private VideoListAdapter vla= null;

    private Button urgentBtn;
    private Button loopBtn;

    public IPCFileCommand mIPCFileCommand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        GolukIPCSdk.getInstance().unregisterIPC(this);
        super.onDestroy();
    }

    private void initView(){
        mListView = (ListView) findViewById(R.id.video_list);
        urgentBtn = (Button) findViewById(R.id.urgent_btn);
        loopBtn = (Button) findViewById(R.id.loop_btn);
        urgentBtn.setOnClickListener(this);
        loopBtn.setOnClickListener(this);

    }

    private void initData() {
        vla = new VideoListAdapter(this);
        mIPCFileCommand = new IPCFileCommand(this, this);
        mListView.setAdapter(vla);
        boolean flog = mIPCFileCommand.queryFileListInfo(2, 20, 0, 2147483647, "1");
    }



    @Override
    public void callback_query_files(ArrayList<VideoInfo> fileList) {
        if(fileList != null){
            vla.setData(fileList);
            vla.notifyDataSetChanged();
            Toast.makeText(this, "callback_query_files  success", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void callback_record_storage_status(RecordStorageState recordStorgeState) {

    }

    @Override
    public void callback_find_single_file(FileInfo fileInfo) {

    }

    @Override
    public void callback_download_file(DownloadInfo downloadinfo) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loop_btn:
                mIPCFileCommand.queryFileListInfo(1, 20, 0, 2147483647, "1");
                setBtnBg(IPCManagerFn.TYPE_CIRCULATE);
                break;
            case R.id.urgent_btn:
                setBtnBg(IPCManagerFn.TYPE_URGENT);
                mIPCFileCommand.queryFileListInfo(2, 20, 0, 2147483647, "1");
                break;
            default:
                break;
        }
    }

    private void setBtnBg(int type){
        urgentBtn.setBackgroundColor(getResources().getColor(R.color.not_select_color));
        loopBtn.setBackgroundColor(getResources().getColor(R.color.not_select_color));

        if(type == IPCManagerFn.TYPE_CIRCULATE){
            loopBtn.setBackgroundColor(getResources().getColor(R.color.select_color));
        }else if(type == IPCManagerFn.TYPE_URGENT){
            urgentBtn.setBackgroundColor(getResources().getColor(R.color.select_color));
        }
    }

}
