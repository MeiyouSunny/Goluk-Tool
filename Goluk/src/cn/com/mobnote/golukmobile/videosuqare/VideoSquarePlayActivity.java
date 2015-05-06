package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.List;

import com.umeng.socialize.sso.UMSsoHandler;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.SharePlatformUtil;
import cn.com.mobnote.golukmobile.VideoShareActivity;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomProgressDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.utils.LogUtil;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

public class VideoSquarePlayActivity extends Activity implements
		OnClickListener, VideoSuqareManagerFn {
	private RTPullListView mRTPullListView = null;
	private VideoSquareListViewAdapter mVideoSquareListViewAdapter = null;
	private List<VideoSquareInfo> mDataList = null;
	public  CustomLoadingDialog mCustomProgressDialog = null;
	private VideoSquareInfo begantime = null;
	private VideoSquareInfo endtime = null;
	private ImageButton mBackBtn = null;
	private RelativeLayout loading = null;
	
	public String shareVideoId; 
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	/** 是否还有分页 */
	private boolean isHaveData = true;
	/** 视频广场类型 */
	private String type;
	/**
	 * 1：上拉  2：下拉   0:第一次
	 */
	private int uptype = 0;
	private TextView title;
	//点播分类
	private String attribute;
	
	SharePlatformUtil sharePlatform;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square_play);
		Intent intent = getIntent();
		title = (TextView) findViewById(R.id.title);
		type = intent.getStringExtra("type");// 视频广场类型
		attribute = intent.getStringExtra("attribute");//点播类型
		if("1".equals(attribute)){
			title.setText("曝光台");
		}else if("2".equals(attribute)){
			title.setText("碰瓷达人");
		}else if("3".equals(attribute)){
			title.setText("路上风景");
		}else if("4".equals(attribute)){
			title.setText("随手拍");
		}else if("5".equals(attribute)){
			title.setText("事故大爆料");
		}else if("6".equals(attribute)){
			title.setText("堵车预警");
		}else if("7".equals(attribute)){
			title.setText("惊险十分");
		}else if("8".equals(attribute)){
			title.setText("疯狂超车");
		}else if("9".equals(attribute)){
			title.setText("感人瞬间");
		}else if("10".equals(attribute)){
			title.setText("传递正能量");
		}else{
			title.setText("热门直播");
		}
		
		GolukApplication.getInstance().getVideoSquareManager()
				.addVideoSquareManagerListener("videocategory", this);
		mDataList = new ArrayList<VideoSquareInfo>();
		mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
		
		/** 返回按钮 */
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mBackBtn.setOnClickListener(this);
		
		sharePlatform = new SharePlatformUtil(this);
		sharePlatform.configPlatforms();//设置分享平台的参数
		loadHistorydata();//显示历史请求数据
		httpPost(true, type, "0", "");
	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    /**使用SSO授权必须添加如下代码 */
	    UMSsoHandler ssoHandler = sharePlatform.mController.getConfig().getSsoHandler(requestCode) ;
	    if(ssoHandler != null){
	       ssoHandler.authorizeCallBack(requestCode, resultCode, data);
	    }
	}

	/**
	 * 获取网络数据
	 * 
	 * @param flag
	 *            是否显示加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void httpPost(boolean flag,String type,String operation,String timestamp) {
		if (flag) {
			if (null == mCustomProgressDialog) {
				mCustomProgressDialog = new CustomLoadingDialog(this,null);
				mCustomProgressDialog.show();
			}
		}

		boolean result = GolukApplication.getInstance().getVideoSquareManager()
				.getSquareList("1", type, attribute, operation, timestamp);
		if (!result) {
			closeProgressDialog();
		}
		System.out.println("YYYY==22222==getSquareList======result=" + result);
	}

	private void init(boolean isloading) {

		if (null == mVideoSquareListViewAdapter) {
			mVideoSquareListViewAdapter = new VideoSquareListViewAdapter(mRTPullListView, this,2);
		}

		mVideoSquareListViewAdapter.setData(mDataList);
		mRTPullListView.setAdapter(mVideoSquareListViewAdapter);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				if(begantime !=null){
					uptype = 2;
					System.out.println("下拉刷新时间="+begantime.mVideoEntity.sharingtime);
					httpPost(true, type, "1", begantime.mVideoEntity.sharingtime);
				}else{
					mRTPullListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							mRTPullListView.onRefreshComplete();
						}
					}, 1500);
				}
				
			}
		});

		mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				LogUtil.e("", "slslslslslsls=22222222222222222222222");
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					
					if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible + wonderfulVisibleCount)) {
						if (isHaveData) {
							uptype = 1;
							System.out.println("上拉刷新时间="+endtime.mVideoEntity.sharingtime);
							httpPost(true, type, "2", endtime.mVideoEntity.sharingtime);
						}
					}
				}
			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem,
					int visibleItemCount, int arg3) {
				wonderfulFirstVisible = firstVisibleItem;
				wonderfulVisibleCount = visibleItemCount;
			}
		});
		
		if(isloading == false){
			//有下一页刷新
			if(isHaveData){
				loading = (RelativeLayout) LayoutInflater.from(this)
						.inflate(R.layout.video_square_below_loading, null);
				mRTPullListView.addFooterView(loading);
			}
		}
		
	}

	public void flush() {
		mVideoSquareListViewAdapter.setData(mDataList);
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
	
	// 分享成功后需要调用的接口
	public void shareSucessDeal(boolean isSucess, String channel) {
			if (!isSucess) {
				Toast.makeText(VideoSquarePlayActivity.this, "第三方分享失败", Toast.LENGTH_SHORT).show();
				return;
			}
			//Toast.makeText(VideoSquarePlayActivity.this, "开始第三方分享:" + channel, Toast.LENGTH_SHORT).show();
			System.out.println("shareid======"+shareVideoId + "channel======="+channel);
			boolean result = GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel,shareVideoId);
			//System.out.println("shareid"+result);
		}

	/**
	 * 关闭加载中对话框
	 * 
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void closeProgressDialog() {
		if (null != mCustomProgressDialog) {
				mCustomProgressDialog.close();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (null != mVideoSquareListViewAdapter) {
			mVideoSquareListViewAdapter.onBackPressed();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (null != mVideoSquareListViewAdapter) {
			mVideoSquareListViewAdapter.onStop();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mVideoSquareListViewAdapter) {
//			mVideoSquareListViewAdapter.onDestroy();
		}
		GolukApplication.getInstance().getVideoSquareManager()
				.removeVideoSquareManagerListener("videocategory");
	}

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,
			Object param2) {
		System.out.println("YYYY==333==getSquareList====event=" + event
				+ "===msg=" + msg + "==param2=" + param2);
		if (event == SquareCmd_Req_SquareList) {
			closeProgressDialog();
			if (RESULE_SUCESS == msg) {

				List<VideoSquareInfo> list = DataParserUtils
						.parserVideoSquareListData((String) param2);
				if (list.size() >= 30) {
					isHaveData = true;
				} else {
					isHaveData = false;
				}
				mDataList = list;
				
				
				if(list.size()>0){
					begantime = list.get(0);
					endtime = list.get(list.size()-1);
				}else{
					if(loading != null){
						if(mRTPullListView!=null){
							mRTPullListView.removeFooterView(loading);
							loading = null;
						}
					}
				}
				
				if (uptype == 0) {
					if(list.size()>0){
						init(false);
					}
				} else {
					if(2 == uptype){//如果如果是下拉,把下拉的窗口关掉
						mRTPullListView.onRefreshComplete();
					}
					if(list.size()>0){
						mDataList.addAll(list);
						flush();
					}
					
				}
			}else {
				isHaveData = false;
				
				if(0 == uptype){
					closeProgressDialog();
				} else if (1 == uptype){
					if(mRTPullListView!=null){
						mRTPullListView.removeFooterView(loading);
						loading = null;
					}
				} else if (2 == uptype){
					mRTPullListView.onRefreshComplete();
				}
				Toast.makeText(VideoSquarePlayActivity.this, "网络异常，请检查网络",Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * 初始化历史请求数据
	  * @Title: loadHistorydata 
	  * @Description: TODO void 
	  * @author 曾浩 
	  * @throws
	 */
	public void loadHistorydata(){
		String param = GolukApplication.getInstance().getVideoSquareManager().getSquareList(attribute);
		if(param != null && !"".equals(param)){
			List<VideoSquareInfo> list = DataParserUtils.parserVideoSquareListData((String)param);
			mDataList = list;
			begantime = list.get(0);
			endtime = list.get(list.size()-1);
			init(true);
		}
		
	}

}
