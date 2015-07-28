package cn.com.mobnote.golukmobile.videosuqare;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

@SuppressLint("HandlerLeak")
public class VideoSquareListView implements VideoSuqareManagerFn{
	private Context mContext=null;
	//private ListView mRTPullListView=null;
	private RTPullListView mRTPullListView=null;
	private VideoSquareListViewAdapter mVideoSquareListViewAdapter=null;
	public List<VideoSquareInfo> mDataList=null;
	private CustomLoadingDialog mCustomProgressDialog=null;
	public  static Handler mHandler=null;
	private RelativeLayout mRootLayout=null;
	private ImageView shareBg = null;
	
	SharePlatformUtil sharePlatform;
	
	private String historyDate;
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");
	
	public VideoSquareListView(Context context,SharePlatformUtil spf){
		mContext=context;
		sharePlatform = spf;
		mRTPullListView = new RTPullListView(mContext);
		mRTPullListView.setDivider(mContext.getResources().getDrawable(R.color.video_square_list_frame));
		mDataList = new ArrayList<VideoSquareInfo>();
		shareBg = (ImageView) View.inflate(context, R.layout.video_square_bj, null);
		
		historyDate = SettingUtils.getInstance().getString("hotHistoryDate", "");
		if("".equals(historyDate)){
			historyDate = sdf.format(new Date());
		}
		SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));
		shareBg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mCustomProgressDialog = null;
				httpPost(true);
			}
		});
		
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if(null != mVideoSquareManager){
			mVideoSquareManager.addVideoSquareManagerListener("hotlist", this);
		}
		
		loadHistorydata();//同步历史数据
		httpPost(true);
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					refeshData((VideoSquareInfo)msg.obj);
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
		
		
		mRootLayout = new RelativeLayout(mContext);
		RelativeLayout.LayoutParams rlp =new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
		mRootLayout.addView(shareBg,rlp);
		mRootLayout.addView(mRTPullListView);
	}
	
	/**
	 * 获取网络数据
	 * @param flag 是否显示加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void httpPost(boolean flag){
		if(flag){
			if(null == mCustomProgressDialog){
				mCustomProgressDialog = new CustomLoadingDialog(mContext,null);
				mCustomProgressDialog.show();
			}
		}
		
		if(null != GolukApplication.getInstance().getVideoSquareManager()){
			boolean result = GolukApplication.getInstance().getVideoSquareManager().getHotList("1","0");
			if(!result){
				closeProgressDialog();
			}
		}else{
			closeProgressDialog();
		}
	}
	
	private void initLayout(){
		
		if(null == mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter = new VideoSquareListViewAdapter(mContext,1,sharePlatform);
		}
		
		mVideoSquareListViewAdapter.setData(mDataList);
		mRTPullListView.setAdapter(mVideoSquareListViewAdapter,historyDate);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				historyDate = SettingUtils.getInstance().getString("hotHistoryDate", sdf.format(new Date()));
				SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));
				httpPost(true);
			}
		});
	}
	
	public void flush(){
		mVideoSquareListViewAdapter.setData(mDataList);
	}
	
	/**
	 * 关闭加载中对话框
	 * @author xuhw
	 * @date 2015年4月15日
	 */
	private void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			mCustomProgressDialog.close();
		}
	}
	
	public View getView(){
		return mRootLayout;
	}
	
	public void onBackPressed(){
		if(null != mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter.onBackPressed();
		}
	}
	
	public void onStop() {
		if(null != mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter.onStop();
		}
		if (mCustomProgressDialog != null && mCustomProgressDialog.isShowing()){
			mCustomProgressDialog.close();
		}
	}
	
	public void onResume(){
		if(null != mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter.onResume();
			mVideoSquareListViewAdapter.notifyDataSetChanged();
		}
	}
	
	public void onDestroy(){
		if(null != mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter.onDestroy();
		}
		
		for(VideoSquareInfo info : mDataList){
			String url = info.mVideoEntity.picture;
			BitmapManager.getInstance().mBitmapUtils.clearMemoryCache(url);
		}
		
		if (mCustomProgressDialog != null ){
			mCustomProgressDialog.close();
		}
	}
	

	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,Object param2) {
		if(event == SquareCmd_Req_HotList){
//			GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			closeProgressDialog();
			mRTPullListView.onRefreshComplete(historyDate);
			if(RESULE_SUCESS == msg){
				List<VideoSquareInfo> list = DataParserUtils.parserVideoSquareListData((String)param2);
				
				mDataList.clear();
				mDataList.addAll(list);
				initLayout();
			}else{
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}
			
			if(mDataList.size()>0){
				setViewListBg(false);
			}else{
				setViewListBg(true);
			}
		}
	}
	
	public void setViewListBg(boolean flog){
		if(flog){
			shareBg.setVisibility(View.VISIBLE);
		}else{
			shareBg.setVisibility(View.GONE);
		}
	}
	
	public void refeshData(VideoSquareInfo videoinfo){
		for(int i = 0;i<mDataList.size();i++){
			VideoSquareInfo vsi =  mDataList.get(i);
			if(vsi.mVideoEntity.videoid.equals(videoinfo.mVideoEntity.videoid)){
				mDataList.get(i).mVideoEntity = videoinfo.mVideoEntity;
			}
		}
		
	}
	
	public void loadHistorydata(){
		String param = GolukApplication.getInstance().getVideoSquareManager().getHotList();
		if(param != null && !"".equals(param)){
			List<VideoSquareInfo> list = DataParserUtils.parserVideoSquareListData((String)param);
			mDataList.clear();
			mDataList.addAll(list);
			initLayout();
		}
		
	}
	
}
