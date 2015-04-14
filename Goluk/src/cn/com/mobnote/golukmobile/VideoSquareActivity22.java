package cn.com.mobnote.golukmobile;

import java.util.ArrayList;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.video.VideoSquareListAdapter;
import cn.com.mobnote.video.VideoSquareListManage;
import cn.com.mobnote.video.VideoSquareListManage.VideoSquareListData;
import cn.com.mobnote.view.PullToRefreshView;
import cn.com.mobnote.view.PullToRefreshView.OnFooterRefreshListener;
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
 * @ 功能描述:视频广场
 * 
 * @author 陈宣宇
 * 
 */

public class VideoSquareActivity22 extends Activity implements OnClickListener ,OnFooterRefreshListener{

	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	//private LayoutInflater mLayoutInflater = null;
	/** 返回按钮 */
	private Button mBackBtn = null;
	/** 直播列表 */
	private ListView mVideoSquareList = null;
	/** 直播列表适配器 */
	private VideoSquareListManage mVideoSquareListManage = null;
	/** 直播列表适配器 */
	private VideoSquareListAdapter mVideoSquareListAdapter = null;
	private ArrayList<VideoSquareListData> mVideoSquareListData = null;
	/** 上拉拉刷新控件 */
	private PullToRefreshView mPullToRefreshView = null;
	
	/** 视频分享页面handler用来接收消息,更新UI*/
	public static Handler mVideoShareHandler = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_square);
		mContext = this;
	
		//获得GolukApplication对象
		mApp = (GolukApplication)getApplication();
		mApp.setContext(mContext,"VideoShare");
		
		
		//视频初始化
		//videoInit();
		//页面初始化
		init();
	}
	
	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void init(){
		//获取页面元素
		mBackBtn = (Button)findViewById(R.id.back_btn);
		//注册事件
		mBackBtn.setOnClickListener(this);
		
		mVideoSquareList = (ListView)findViewById(R.id.video_square_listview);
		//上拉刷新view
//		mPullToRefreshView = (PullToRefreshView)findViewById(R.id.listview_pull_refresh_view);
		
//		mVideoSquareListManage = new VideoSquareListManage(mContext);
//		mVideoSquareListData = mVideoSquareListManage.getVideoSquareList(99);
//		mVideoSquareListAdapter = new VideoSquareListAdapter(mContext,mVideoSquareListData);
//		mVideoSquareList.setAdapter(mVideoSquareListAdapter);
		
		//注册上拉刷新事件
//		mPullToRefreshView.setOnFooterRefreshListener(this);
		
		//更新UI handler
		mVideoShareHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
			}
		};
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
		}
	}

	@Override
	public void onFooterRefresh(PullToRefreshView view) {
		// TODO Auto-generated method stub
		mPullToRefreshView.onFooterRefreshComplete();
	}
}











