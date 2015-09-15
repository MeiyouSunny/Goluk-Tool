package cn.com.mobnote.golukmobile.usercenter;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.special.ClusterInfo;
import cn.com.mobnote.golukmobile.special.SpecialInfo;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

/**
 * 
 * @author 曾浩
 *
 */
public class UserCenterActivity extends BaseActivity {
	
	private RTPullListView mRTPullListView = null;
	private UserCenterAdapter uca = null;
	private SharePlatformUtil sharePlatform = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center);
		init();
	}
	
	private void init(){
		sharePlatform = new SharePlatformUtil(this);
		uca =new UserCenterAdapter(this, sharePlatform);
		mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
		//mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mRTPullListView.setAdapter(uca);
		
		SpecialInfo si = new SpecialInfo();
		si.videoid = "zh";
		
		uca.setUserData(si);
		uca.setVideoData(getData());
		uca.setPraisData(getData());
		uca.notifyDataSetChanged();
	}
	
	private List<ClusterInfo> getData(){
		List<ClusterInfo> list = new ArrayList<ClusterInfo>();
		
		ClusterInfo  ci = null;
		for (int i = 0; i < 15; i++) {
			ci = new ClusterInfo();
			ci.videoid = i + "";
			list.add(ci);
		}
		return list ;
	}

}
