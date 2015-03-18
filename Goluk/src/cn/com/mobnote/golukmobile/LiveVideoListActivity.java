package cn.com.mobnote.golukmobile;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.util.console;
import cn.com.mobnote.video.LiveVideoListAdapter;
import cn.com.mobnote.video.LiveVideoListManage;
import cn.com.mobnote.video.LiveVideoListManage.LiveVideoListData;
import cn.com.mobnote.view.PullToRefreshView;
import cn.com.mobnote.view.PullToRefreshView.OnFooterRefreshListener;
import cn.com.mobonote.golukmobile.comm.GolukMobile;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

/**
 * <pre>
 * 1.类命名首字母大写
 * 2.公共函数驼峰式命名
 * 3.属性函数驼峰式命名
 * 4.变量/参数驼峰式命名
 * 5.操作符之间必须加空格
 * 6.注释都在行首写.(枚举除外)
 * 7.编辑器必须显示空白处
 * 8.所有代码必须使用TAB键缩进
 * 9.函数使用块注释,代码逻辑使用行注释
 * 10.文件头部必须写功能说明
 * 11.后续人员开发保证代码格式一致
 * </pre>
 * 
 * @ 功能描述:直播列表
 * 
 * @author 陈宣宇
 * 
 */

public class LiveVideoListActivity extends Activity implements OnClickListener ,OnFooterRefreshListener{

	/** application */
	public GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 返回按钮 */
	private Button mBackBtn = null;
	private Button mRefreshBtn = null;
	private RelativeLayout mLoading = null;
	/** 直播列表 */
	private ListView mVideoSquareList = null;
	/** 直播列表适配器 */
	private LiveVideoListManage mLiveVideoListManage = null;
	/** 直播列表适配器 */
	private LiveVideoListAdapter mLiveVideoListAdapter = null;
	private ArrayList<LiveVideoListData> mLiveVideoListData = null;
	/** 上拉拉刷新控件 */
	private PullToRefreshView mPullToRefreshView = null;
	
	/** 视频直播列表页面handler用来接收消息,更新UI*/
	public static Handler mVideoLiveListHandler = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_live_list);
		mContext = this;
	
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"LiveVideoList");
		
		
		//视频初始化
		//videoInit();
		//页面初始化
		init();
		
		//请求直播列表数据
		getLiveVideoList();
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mBackBtn = (Button)findViewById(R.id.back_btn);
		mRefreshBtn = (Button) findViewById(R.id.refresh_btn);
		mLoading = (RelativeLayout) findViewById(R.id.loading_layout);
		
		mVideoSquareList = (ListView)findViewById(R.id.video_square_listview);
		//上拉刷新view
		mPullToRefreshView = (PullToRefreshView)findViewById(R.id.listview_pull_refresh_view);
		
		mLiveVideoListManage = new LiveVideoListManage(mContext);
		mLiveVideoListData = mLiveVideoListManage.getVideoSquareList();
		mLiveVideoListAdapter = new LiveVideoListAdapter(mContext,mLiveVideoListData);
		mVideoSquareList.setAdapter(mLiveVideoListAdapter);
		
		//注册上拉刷新事件
		mPullToRefreshView.setOnFooterRefreshListener(this);
		//注册事件
		mBackBtn.setOnClickListener(this);
		mRefreshBtn.setOnClickListener(this);
		
		//更新UI handler
		mVideoLiveListHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch(what){
					case 1:
						//视频截图下载成功,刷新页面UI
						mLiveVideoListAdapter.notifyDataSetChanged();
					break;
				}
			}
		};
	}
	
	/**
	 * 请求直播列表数据
	 */
	private void getLiveVideoList(){
		boolean b = mApp.mGoluk.GoLuk_CommonGetPage(GolukMobile.PageType_GetPinData,"");
		if(!b){
			console.log("调用直播列表数据接口失败---b---" + b);
		}
		else{
			mLoading.setVisibility(View.VISIBLE);
		}
	}
	
	
	public void LiveListDataCallback(int success,Object obj){
		if(1 == success){
			String str = (String)obj;
			console.log("直播数据返回---liveListDataCallback---" + str);
			//String str = "{\"code\":\"200\",\"state\":\"true\",\"info\":[{\"utype\":\"1\",\"aid\":\"1\",\"nickname\":\"张三\",\"lon\":\"116.357428\",\"lat\":\"39.93923\",\"picurl\":\"http://img2.3lian.com/img2007/18/18/003.png\",\"speed\":\"34公里/小时\"},{\"aid\":\"2\",\"utype\":\"2\",\"nickname\":\"李四\",\"lon\":\"116.327428\",\"lat\":\"39.91923\",\"picurl\":\"http://img.cool80.com/i/png/217/02.png\",\"speed\":\"342公里/小时\"}]}";
			try {
				JSONObject json = new JSONObject(str);
				//请求成功
				JSONArray list = json.getJSONArray("info");
				mLiveVideoListManage.addLiveVideoItem(list);
				mLiveVideoListAdapter.notifyDataSetChanged();
				
				mLoading.setVisibility(View.GONE);
			}
			catch(Exception e){
				
			}
		}
		else{
			console.log("请求大头针数据错误");
		}
	}
	
	/**
	 * 下载直播图片完成
	 * @param obj
	 */
	public void downloadVideoImageCallBack(int success,Object obj){
		if(1 == success){
			//更新在线视频图片
			String imgJson = (String)obj;
			//String imgJson = "{\"path\":\"fs1:/Cache/test11.png\"}";
			console.log("下载直播图片完成---downloadVideoImageCallBack:" +imgJson);
			try {
				JSONObject json = new JSONObject(imgJson);
				mLiveVideoListManage.updateHeadImg(json);
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		else{
			console.toast("直播图片下载失败", mContext);
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch(id){
			case R.id.back_btn:
				//返回
				this.finish();
			break;
			case R.id.refresh_btn:
				//刷新
				getLiveVideoList();
			break;
		}
	}

	@Override
	public void onFooterRefresh(PullToRefreshView view) {
		// TODO Auto-generated method stub
		mPullToRefreshView.onFooterRefreshComplete();
	}
}











