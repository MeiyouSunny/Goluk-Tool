package cn.com.mobnote.golukmobile.newest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.VideoEntity;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareInfo;
import cn.com.mobnote.golukmobile.videosuqare.VideoSquareManager;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.tiros.debug.GolukDebugUtils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

public class WonderfulSelectedListView implements VideoSuqareManagerFn{
	private RelativeLayout mRootLayout = null;
	private Context mContext = null;
	private RTPullListView mRTPullListView = null;
	public List<JXListItemDataInfo> mDataList = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	public  static Handler mHandler = null;
	private WonderfulSelectedAdapter mWonderfulSelectedAdapter = null;
	/** 保存列表一个显示项索引 */
	private int firstVisible;
	/** 保存列表显示item个数 */
	private int visibleCount;
	private String historyDate;
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");
	
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
		
		httpPost(true, "0", "4");
		
//		for(int i=0; i<10; i++) {
//			JXListItemDataInfo info = new JXListItemDataInfo();
//			info.ztitle = "辽宁卫视冲上云霄===="+i;
//			if(i == 1 || i==3)
//				info.ztag = "聚合";
//			info.videonumber = "3,896";
//			info.clicknumber = "26";
//			
//			if(i == 0 || i==2){
//				info.jximg = "http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png";
//				info.jtypeimg = "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png";
//			}else{
//				info.jximg = "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png";
//			}
//			
//			if(i == 1) {
//				info.jxdate = "2015.08.04";
//			}
//			
//			mDataList.add(info);
//		}
//		
//		initLayout();
	}
	
	private void httpPost(boolean flag, String jxid, String pagesize){
		if(flag){
			if(null == mCustomProgressDialog){
				mCustomProgressDialog = new CustomLoadingDialog(mContext,null);
				mCustomProgressDialog.show();
			}
		}
		
		if(null != GolukApplication.getInstance().getVideoSquareManager()){
			boolean result = GolukApplication.getInstance().getVideoSquareManager().getJXListData(jxid, pagesize);
//			boolean a = GolukApplication.getInstance().getVideoSquareManager().getSPFLListData();
//			GolukApplication.getInstance().getVideoSquareManager().getZTListData("zt001");
//			GolukApplication.getInstance().getVideoSquareManager().getJHListData("zt001", "0", "", "20");
//			GolukApplication.getInstance().getVideoSquareManager().getVideoDetailData("zt001");
//			GolukApplication.getInstance().getVideoSquareManager().getSPFLListData();
			List<String> l = new ArrayList<String>();
			l.add("0");
			GolukApplication.getInstance().getVideoSquareManager().getTypeVideoList("1", "0", l, "0", "");
//			boolean a = GolukApplication.getInstance().getVideoSquareManager().getCommentListData("zt001", "2", "0", "", "");
//			boolean a = GolukApplication.getInstance().getVideoSquareManager().addComment("04DB0612A41EBB909C33DC5901307461", "1", "拼杀开始看开始", "", "");
//			boolean a = GolukApplication.getInstance().getVideoSquareManager().deleteComment("6C0DDF2E74844517925A2BE1EC9D88EB");
			
//			List<VideoSquareInfo> info = new ArrayList<VideoSquareInfo>();
//			for(int i=0;i<3;i++){
//				VideoSquareInfo v = new VideoSquareInfo();
//				VideoEntity e = new VideoEntity();
//				e.videoid = "20150617_2BBD405013F011E58050892EFD2F8892";
//				e.clicknumber="10";
//				v.mVideoEntity = e;
//				
//			}
//			boolean a = GolukApplication.getInstance().getVideoSquareManager().clickNumberUpload("1", info);
			
//			boolean a = GolukApplication.getInstance().getVideoSquareManager().clickPraise("1", "20150617_2BBD405013F011E58050892EFD2F8892", "1");
//			boolean a = GolukApplication.getInstance().getVideoSquareManager().report("1", "20150617_2BBD405013F011E58050892EFD2F8892", "1");
//			boolean a = GolukApplication.getInstance().getVideoSquareManager().recomVideo("1", "20150617_2BBD405013F011E58050892EFD2F8892", "的好感动个");
//			boolean a = GolukApplication.getInstance().getVideoSquareManager().getShareUrl("20150617_2BBD405013F011E58050892EFD2F8892", "2");
//			boolean b = GolukApplication.getInstance().getVideoSquareManager().shareVideoUp("1", "20150617_2BBD405013F011E58050892EFD2F8892");
//			boolean a = GolukApplication.getInstance().getVideoSquareManager().getTagShareUrl("1", "zt001");
			
//			GolukDebugUtils.e("", "VideoSuqare_CallBack=@@@@======a="+a);
			
			
			
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
	
	private void initLayout(){
		if(null == mWonderfulSelectedAdapter){
			mWonderfulSelectedAdapter = new WonderfulSelectedAdapter(mContext);
		}
		
		mWonderfulSelectedAdapter.setData(mDataList);
		mRTPullListView.setAdapter(mWonderfulSelectedAdapter);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				historyDate = SettingUtils.getInstance().getString("hotHistoryDate", sdf.format(new Date()));
				SettingUtils.getInstance().putString("hotHistoryDate", sdf.format(new Date()));
//				httpPost(true);
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
//			GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@@@@=event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			closeProgressDialog();
			mRTPullListView.onRefreshComplete(historyDate);
			if(RESULE_SUCESS == msg){
				List<JXListItemDataInfo> list = JsonParserUtils.parserJXData((String)param2);
				
				mDataList.addAll(list);
				initLayout();
			}else{
				GolukUtils.showToast(mContext, "网络异常，请检查网络");
			}
			
//			if(mDataList.size()>0){
//				setViewListBg(false);
//			}else{
//				setViewListBg(true);
//			}
		}else if(event == VSquare_Req_List_Topic_Content){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@@@@=event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_List_Tag_Content){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@Tag_Content==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_Get_VideoDetail){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@Get_VideoDetail==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_List_Catlog){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@List_Catlog==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_List_Video_Catlog){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@List_Video_Catlog==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
				
				String c = GolukApplication.getInstance().getVideoSquareManager().getTypeVideoList("0");
				GolukDebugUtils.e("", "VideoSuqare_C1111111=====c="+c);			
			}
		}else if(event == VSquare_Req_List_Comment){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@List_Comment==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_Add_Comment){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@Add_Comment==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_Del_Comment){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@Del_Comment==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_VOP_ClickUp){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@VOP_ClickUp==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_VOP_Praise){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@VOP_Praise==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_VOP_ReportUp){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@VOP_ReportUp==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_VOP_RecomVideo){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@VOP_RecomVideo=event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_VOP_ShareVideo){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@VOP_ShareVideo==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_VOP_GetShareURL_Video){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@GetShareURL_Video==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}else if(event == VSquare_Req_VOP_GetShareURL_Topic_Tag){
			if(RESULE_SUCESS == msg){
				GolukDebugUtils.e("xuhw", "VideoSuqare_CallBack=@@@@GetShareURL_Topic_Tag==event="+event+"=msg="+msg+"=param1="+param1+"=param2="+param2);
			}
		}
		
		
		
		
		
		
		
		
	}
	
	public void onDestroy(){
		
	}
	
}
