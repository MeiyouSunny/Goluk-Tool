package cn.com.mobnote.golukmobile.newest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

@SuppressLint("InflateParams")
public class WonderfulSelectedListView implements VideoSuqareManagerFn{
	private RelativeLayout mRootLayout = null;
	private Context mContext = null;
	private RTPullListView mRTPullListView = null;
	public List<JXListItemDataInfo> mDataList = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	public  static Handler mHandler = null;
	private WonderfulSelectedAdapter mWonderfulSelectedAdapter = null;
	private String historyDate;
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");
	/** 列表添加页脚标识 */
	private boolean addFooter=false;
	/** 添加列表底部加载中布局 */
	private RelativeLayout mBottomLoadingView = null;
	private int pageCount = 4;
	private String jxid = "0";
	private boolean isGetFileListDataing = false;
	/** 保存列表一个显示项索引 */
	private int firstVisible;
	/** 保存列表显示item个数 */
	private int visibleCount;
	
	public WonderfulSelectedListView(Context context) {
		mContext = context;
		mDataList = new ArrayList<JXListItemDataInfo>();
		mRTPullListView = new RTPullListView(mContext);
		mRootLayout = new RelativeLayout(mContext);
		mRootLayout.addView(mRTPullListView);
		
		historyDate = SettingUtils.getInstance().getString("hotHistoryDate", "");
		if("".equals(historyDate)){
			historyDate = sdf.format(new Date());
		}
		SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));
		
		VideoSquareManager mVideoSquareManager = GolukApplication.getInstance().getVideoSquareManager();
		if(null != mVideoSquareManager){
			mVideoSquareManager.addVideoSquareManagerListener("wonderfulSelectedList", this);
		}
	
		initHistoryData();
		httpPost(true, "0", "");
	}
	
	private void initHistoryData() {
		String data = GolukApplication.getInstance().getVideoSquareManager().getJXList();
		if (!TextUtils.isEmpty(data)) {
			initLayout(JsonParserUtils.parserJXData(data));
		}
		
	}
	
	private void httpPost(boolean flag, String jxid, String pagesize){
		this.jxid = jxid;
		if(flag){
			if(null == mCustomProgressDialog){
				mCustomProgressDialog = new CustomLoadingDialog(mContext,null);
				mCustomProgressDialog.show();
			}
		}
		
		if(null != GolukApplication.getInstance().getVideoSquareManager()){
			if (isGetFileListDataing) {
				return;
			}
			isGetFileListDataing = true;
			GolukDebugUtils.e("", "TTTTTT=====11111=====jxid="+jxid);
			boolean result = GolukApplication.getInstance().getVideoSquareManager().getJXListData(jxid, pagesize);
			if(!result){
				closeProgressDialog();
			}
		}else{
			closeProgressDialog();
		}
	}
	
	private void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			mCustomProgressDialog.close();
		}
	}
	
	private void initLayout(List<JXListItemDataInfo> list){
		mDataList.addAll(list);
		
		if (!addFooter) {
			addFooter = true;
			mBottomLoadingView = (RelativeLayout) LayoutInflater.from(mContext)
					.inflate(R.layout.video_square_below_loading, null);
			mRTPullListView.addFooterView(mBottomLoadingView);
		}
		
		if (pageCount < 4) {
			if (addFooter) {
				addFooter = false;
				mRTPullListView.removeFooterView(mBottomLoadingView);
			}
		}
		
		if(null == mWonderfulSelectedAdapter){
			mWonderfulSelectedAdapter = new WonderfulSelectedAdapter(mContext);
		}
		
		if ("0".equals(jxid)) {
			mRTPullListView.setAdapter(mWonderfulSelectedAdapter);
		}
		mWonderfulSelectedAdapter.setData(mDataList);
		
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				historyDate = SettingUtils.getInstance().getString("hotHistoryDate", sdf.format(new Date()));
				SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));
				httpPost(true, "0", "");
			}
		});
		
		mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				switch (scrollState) {
				case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
					mWonderfulSelectedAdapter.lock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					mWonderfulSelectedAdapter.unlock();
					if (mRTPullListView.getAdapter().getCount() == (firstVisible + visibleCount)) {
						httpPost(false, mDataList.get(mDataList.size() - 1).jxid, "");
					}
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					mWonderfulSelectedAdapter.lock();
					break;
					
				default:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView arg0, int firstVisibleItem,
					int visibleItemCount, int arg3) {
				firstVisible = firstVisibleItem;
				visibleCount = visibleItemCount;
			}
			
		});
		
	}

	public View getView(){
		return mRootLayout;
	}
	
	@Override
	public void VideoSuqare_CallBack(int event, int msg, int param1,Object param2) {
		if(event == VSquare_Req_List_HandPick){
			GolukDebugUtils.e("", "TTTTTT=====2222=====param2="+param2);
			isGetFileListDataing = false;
			if ("0".equals(jxid)) {
				closeProgressDialog();
				mRTPullListView.onRefreshComplete(historyDate);
			}
			
			if(RESULE_SUCESS == msg){
				List<JXListItemDataInfo> list = JsonParserUtils.parserJXData((String)param2);
				pageCount = JsonParserUtils.parserJXCount((String)param2);
				if ("0".equals(jxid)) {
					mDataList.clear();
				}
				initLayout(list);
			}else{
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}
			
//			if(mDataList.size()>0){
//				setViewListBg(false);
//			}else{
//				setViewListBg(true);
//			}
		}
		
	}
	
	public void onDestroy(){
		
	}
	
}
