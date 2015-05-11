package cn.com.mobnote.golukmobile.videosuqare;

import java.util.ArrayList;
import java.util.List;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.PauseOnScrollListener;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.BitmapManager;
import cn.com.mobnote.golukmobile.carrecorder.util.LogUtils;
import cn.com.mobnote.golukmobile.carrecorder.util.SoundUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.utils.LogUtil;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class VideoSquareListView implements VideoSuqareManagerFn{
	private Context mContext=null;
	//private ListView mRTPullListView=null;
	private RTPullListView mRTPullListView=null;
	private VideoSquareListViewAdapter mVideoSquareListViewAdapter=null;
	private List<VideoSquareInfo> mDataList=null;
	private CustomLoadingDialog mCustomProgressDialog=null;
	private Float jj= SoundUtils.getInstance().getDisplayMetrics().density;
	/** 保存列表一个显示项索引 */
	private int wonderfulFirstVisible;
	/** 保存列表显示item个数 */
	private int wonderfulVisibleCount;
	public  static Handler mHandler=null;
	private RelativeLayout mRootLayout=null;
	
	public VideoSquareListView(Context context){
		mContext=context;
		mRTPullListView = new RTPullListView(mContext);
		mRTPullListView.setDivider(mContext.getResources().getDrawable(R.color.video_square_list_frame));
		//mRTPullListView.setDividerHeight((int)(22*jj));
		mDataList = new ArrayList<VideoSquareInfo>();
		LogUtils.d("YYYYYYYY=================111111111===================");
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
//			AlertDialog dialog = new AlertDialog.Builder(mContext).create();
//			dialog.show();
//			dialog.getWindow().setContentView(R.layout.video_square_loading);
		}
		
		if(null != GolukApplication.getInstance().getVideoSquareManager()){
			boolean result = GolukApplication.getInstance().getVideoSquareManager().getHotList("1","0");
			if(!result){
				closeProgressDialog();
			}
			System.out.println("YYYY==22222==getHotList======result="+result);
		}else{
			System.out.println("YYYY==22222==getHotList======error=");
		}
	}
	
	private void initLayout(){
		
		if(null == mVideoSquareListViewAdapter){
			mVideoSquareListViewAdapter = new VideoSquareListViewAdapter(mRTPullListView, mContext,1);
		}
		
		mVideoSquareListViewAdapter.setData(mDataList);
		mRTPullListView.setAdapter(mVideoSquareListViewAdapter);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
//				mRTPullListView.onRefreshComplete();
				//Toast.makeText(mContext, "下拉刷新", Toast.LENGTH_SHORT).show();
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
			mVideoSquareListViewAdapter.notifyDataSetChanged();
		}
	}
	
	public void onDestroy(){
		GolukApplication.getInstance().getVideoSquareManager().removeVideoSquareManagerListener("hotlist");
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
		LogUtil.e("xuhw","YYYYYYY===hotlist===event="+event+"======msg="+msg+"===param2="+param2);
		if(event == SquareCmd_Req_HotList){
			closeProgressDialog();
			mRTPullListView.onRefreshComplete();
			if(RESULE_SUCESS == msg){
				List<VideoSquareInfo> list = DataParserUtils.parserVideoSquareListData((String)param2);
				mDataList.clear();
				mDataList.addAll(list);
				initLayout();
			}else{
				Toast.makeText(mContext, "网络异常，请检查网络",Toast.LENGTH_SHORT).show();
			}
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
