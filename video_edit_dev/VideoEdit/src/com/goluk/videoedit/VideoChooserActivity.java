package com.goluk.videoedit;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.goluk.videoedit.adapter.VideoChooserAdapter;
import com.goluk.videoedit.task.LoadLocalDataTask;
import com.goluk.videoedit.task.LoadLocalDataTask.VidLoadCallBack;

public class VideoChooserActivity extends Activity implements View.OnClickListener ,AdapterView.OnItemClickListener{

	ImageButton mBackIb;
	TextView mTitleView;
	GridView nVideoChooserGridview;

	VidLoadCallBack mVidLoadCallBack;
	LoadLocalDataTask mLoadLocalDataTask;

	List<String> mVideoPathList;
	VideoChooserAdapter mVideoAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_video_chooser);

		initView();
		initData();
		getLocalVideoData();
	}

	private void initView() {

		mBackIb = (ImageButton) findViewById(R.id.ib_video_chooser_back);
		mTitleView = (TextView) findViewById(R.id.tv_video_chooser_title);
		nVideoChooserGridview = (GridView) findViewById(R.id.gridview_videoChooser);

		nVideoChooserGridview.setOnItemClickListener(this);

		mBackIb.setOnClickListener(this);
	}

	private void initData(){
		mVidLoadCallBack = new VidLoadCallBack() {
			@Override
			public void OnLoadSucced(List<String> list) {
				// TODO Auto-generated method stub

				mVideoPathList = list;
				mVideoAdapter = new VideoChooserAdapter(VideoChooserActivity.this, mVideoPathList);
				nVideoChooserGridview.setAdapter(mVideoAdapter);
			}
		};
		mLoadLocalDataTask = new LoadLocalDataTask(mVidLoadCallBack);
	}

	private void getLocalVideoData(){
		mLoadLocalDataTask.execute("");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int vId = v.getId();
		if(vId == R.id.ib_video_chooser_back){
			VideoChooserActivity.this.finish();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if(mVideoPathList != null && mVideoPathList.size()>position){
			Bundle bundle = new Bundle();
			bundle.putString("vidPath", mVideoPathList.get(position));
			Intent intent = new Intent();
			intent.putExtras(bundle);
			setResult(RESULT_OK,intent);
			VideoChooserActivity.this.finish();
		}
	}

}
