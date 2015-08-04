package cn.com.mobnote.golukmobile.newest;

import java.util.ArrayList;
import java.util.List;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRTScrollListener;
import cn.com.mobnote.golukmobile.videosuqare.RTPullListView.OnRefreshListener;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

public class NewestListView {
	private RelativeLayout mRootLayout = null;
	private Context mContext = null;
	private RTPullListView mRTPullListView = null;
	public List<JXListItemDataInfo> mDataList = null;
	private CustomLoadingDialog mCustomProgressDialog = null;
	public  static Handler mHandler = null;
	private NewestAdapter mNewestAdapter = null;
	/** 保存列表一个显示项索引 */
	private int firstVisible;
	/** 保存列表显示item个数 */
	private int visibleCount;
	
	public NewestListView(Context context) {
		mContext = context;
		mDataList = new ArrayList<JXListItemDataInfo>();
		mRTPullListView = new RTPullListView(mContext);
		mRootLayout = new RelativeLayout(mContext);
		mRootLayout.addView(mRTPullListView);
		
		mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				switch (scrollState) {
				case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
					mNewestAdapter.lock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					mNewestAdapter.unlock();
					break;
				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					mNewestAdapter.lock();
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
		
		
//		httpPost(true);
		
		for(int i=0; i<10; i++) {
			JXListItemDataInfo info = new JXListItemDataInfo();
			info.ztitle = "辽宁卫视冲上云霄===="+i;
			if(i == 1 || i==3)
				info.ztag = "聚合";
			info.videonumber = "3,896";
			info.clicknumber = "26";
			
			if(i == 0 || i==2){
				info.jximg = "http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png";
				info.jtypeimg = "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png";
			}else{
				info.jximg = "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png";
			}
			
			mDataList.add(info);
		}
		
		initLayout();
	}
	
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
	
	private void closeProgressDialog(){
		if(null != mCustomProgressDialog){
			mCustomProgressDialog.close();
		}
	}
	
	private void initLayout(){
		if(null == mNewestAdapter){
			mNewestAdapter = new NewestAdapter(mContext);
		}
		
		mNewestAdapter.setData(mDataList);
		mRTPullListView.setAdapter(mNewestAdapter);
		mRTPullListView.setonRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				
				mRTPullListView.onRefreshComplete("2015-08-04");//historyDate);
			}
		});
	}

	public View getView(){
		return mRootLayout;
	}
	
	public void onDestroy(){
		
	}
	
}

