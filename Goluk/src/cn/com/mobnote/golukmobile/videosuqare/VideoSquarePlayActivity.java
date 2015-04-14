package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.golukmobile.videosuqare.VideoCategoryView.click;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class VideoSquarePlayActivity extends Activity implements OnClickListener {
	
	private RTPullListView mRTPullListView=null;
	private VideoSquareListViewAdapter mVideoSquareListViewAdapter=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square_play);
		mRTPullListView = (RTPullListView)findViewById(R.id.mRTPullListView);
		mVideoSquareListViewAdapter = new VideoSquareListViewAdapter(this);
	
		List<VideoSquareInfo> data = new ArrayList<VideoSquareInfo>();
		for(int i=0;i<100;i++){
			VideoSquareInfo info = new VideoSquareInfo();
			data.add(info);
		}
		mVideoSquareListViewAdapter.setData(data);
		mRTPullListView.setAdapter(mVideoSquareListViewAdapter);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
//				mRTPullListView.onRefreshComplete();
				Toast.makeText(VideoSquarePlayActivity.this, "下拉刷新", Toast.LENGTH_SHORT).show();
				mRTPullListView.postDelayed(new Runnable() {
					@Override
					public void run() {
						mRTPullListView.onRefreshComplete();
					}
				}, 1500);
			}
		});
		init();
	}
	
	private void init (){
		/** 返回按钮 */
		 Button mBackBtn = (Button) findViewById(R.id.back_btn);;
		 mBackBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.back_btn:
			this.finish();
			break;

		default:
			break;
		}
	}
	
	
	
}
