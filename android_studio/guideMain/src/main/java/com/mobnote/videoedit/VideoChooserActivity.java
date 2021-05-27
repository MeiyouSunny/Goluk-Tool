package com.mobnote.videoedit;

import android.os.Bundle;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.videoedit.fragment.VideoChooseFragment;

public class VideoChooserActivity extends BaseActivity implements View.OnClickListener/*, AdapterView.OnItemClickListener*/ {

    ImageView mBackIV;
    TextView mTitleView;
//    GridView nVideoChooserGridview;

//    LoadLocalDataTask.VidLoadCallBack mVidLoadCallBack;
//    LoadLocalDataTask mLoadLocalDataTask;

//    List<String> mVideoPathList;
//    VideoChooserAdapter mVideoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_chooser);

        initView();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_ae_add_video, new VideoChooseFragment());
        ft.commit();
//        initData();
//        getLocalVideoData();
    }

    private void initView() {
        mBackIV = (ImageView) findViewById(R.id.iv_video_chooser_back);
        mTitleView = (TextView) findViewById(R.id.tv_video_chooser_title);
//        nVideoChooserGridview = (GridView) findViewById(R.id.gridview_videoChooser);

//        nVideoChooserGridview.setOnItemClickListener(this);

        mBackIV.setOnClickListener(this);
    }

//    private void initData() {
//        mVidLoadCallBack = new LoadLocalDataTask.VidLoadCallBack() {
//            @Override
//            public void OnLoadSucced(List<String> list) {
//
//                mVideoPathList = list;
//                mVideoAdapter = new VideoChooserAdapter(VideoChooserActivity.this, mVideoPathList);
//                nVideoChooserGridview.setAdapter(mVideoAdapter);
//            }
//        };
//        mLoadLocalDataTask = new LoadLocalDataTask(mVidLoadCallBack);
//    }
//
//    private void getLocalVideoData() {
//        mLoadLocalDataTask.execute("");
//    }

    @Override
    public void onClick(View v) {
        int vId = v.getId();
        if (vId == R.id.iv_video_chooser_back) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        if (mVideoPathList != null && mVideoPathList.size() > position) {
//            String selectedVidPath = getAbsoluteVidPath(mVideoPathList.get(position));
//            Bundle bundle = new Bundle();
//            bundle.putString("vidPath", selectedVidPath);
//            Intent intent = new Intent();
//            intent.putExtras(bundle);
//            setResult(RESULT_OK, intent);
//            VideoChooserActivity.this.finish();
//        }
//    }
//
//    private String getAbsoluteVidPath(String path) {
//        if (TextUtils.isEmpty(path)) {
//            return null;
//        }
//        if (path.startsWith("WND")) {
//            return android.os.Environment.getExternalStorageDirectory().getPath() + "/goluk/video/wonderful/" + path;
//        } else if (path.startsWith("URG")) {
//            return android.os.Environment.getExternalStorageDirectory().getPath() + "/goluk/video/urgent/" + path;
//        } else if (path.startsWith("NRM")) {
//            return android.os.Environment.getExternalStorageDirectory().getPath() + "/goluk/video/loop/" + path;
//        }
//        return path;
//    }
}
