package cn.com.mobnote.golukmobile.live;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.PreferencesReader;
import cn.com.mobnote.golukmobile.carrecorder.SoundUtils;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.map.BaiduMapManage;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.talk.ITalkFn;
import cn.com.mobnote.util.console;
import cn.com.tiros.utils.LogUtil;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.rd.car.CarRecorderManager;
import com.rd.car.RecorderStateException;
import com.rd.car.player.RtmpPlayerView;

public class LiveActivity extends Activity implements OnClickListener, RtmpPlayerView.RtmpPlayerViewLisener,
		View.OnTouchListener, ITalkFn {
	/** 是否是直播 */
	public static final String KEY_IS_LIVE = "isLive";
	/** 要加入的群组ID */
	public static final String KEY_GROUPID = "groupID";
	/** 播放与直播地址 */
	public static final String KEY_PLAY_URL = "key_play_url";
	public static final String KEY_JOIN_GROUP = "key_join_group";

	/** application */
	private GolukApplication mApp = null;
	/** 上下文 */
	private Context mContext = null;
	/** 返回按钮 */
	private Button mLiveBackBtn = null;
	/** 刷新按钮 */
	private Button mRefirshBtn = null;
	/** title */
	private TextView mTitleTv = null;
	/** 当前地址 */
	private TextView mAddressTv = null;
	/** 当前正在说话的 */
	private TextView mTalkingTv = null;
	/** 视频loading */
	private RelativeLayout mVideoLoading = null;
	/** 播放布局 */
	// private RelativeLayout mPlayLayout = null;
	/** 直播超时提示文字 */
	private TextView mTimeOutText = null;
	/** 百度地图 */
	private MapView mMapView = null;
	private BaiduMap mBaiduMap = null;
	private BaiduMapManage mBaiduMapManage = null;
	/** 自定义播放器支持特效 */
	public RtmpPlayerView mRPVPalyVideo = null;
	/** 用户aid */
	private String mAid = "";
	/** 用户uid */
	private String mUid = "";
	/** 视频地址 */
	private String mFilePath = "";
	/** 首页handler用来接收消息,更新UI */
	public static Handler mLiveVideoHandler = null;
	/** 是否直播 还是　看别人直播 true/false 直播/看别人直播 */
	private boolean isShareLive = true;
	/** 要加入的爱滔客群组 */
	private String mGroupId = null;
	private String mPlayUrl = null;
	private String mJoinGroupJson = null;

	/** 直播开启标志 */
	private boolean isStartLive = false;
	/** 直播视频id */
	private String liveVid;
	/** 直播录制定时器 */
	private Timer mRecordTimer = null;
	/** 直播录制时间 */
	private int curRecordTime = 0;
	/** 单次直播录制时间 */
	private final int LIVERECORDINGTIME = 45;

	private RelativeLayout mBottomLayout = null;
	/** 直播界面说话按钮 */
	private ImageButton mLiveTalk = null;
	/** 观看直播界面说话按钮 */
	private ImageButton mLiveLookTalk = null;
	private LinearLayout mQiangPaiLayout = null;
	private LinearLayout mExitLayout = null;
	private ImageView mQiangpaiImg = null;
	private ImageView mExitBtn = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().setContentView(R.layout.live);
		mContext = this;
		// 获得GolukApplication对象
		mApp = (GolukApplication) getApplication();
		mApp.setContext(this, "LiveVideo");

		// 获取数据
		getIntentData();
		// 界面初始化
		initView();
		//
		switchView();
		// 地图初始化
		initMap();
		// 请求视频直播数据
		// getVideoLiveData();

		videoInit();

		drawPersonsHead();
		// 加入爱滔客群组
		joinAitalkGroup();
	}

	private void getIntentData() {
		// 获取视频路径
		Intent intent = getIntent();
		mAid = intent.getStringExtra("cn.com.mobnote.map.aid");
		mUid = intent.getStringExtra("cn.com.mobnote.map.uid");
		isShareLive = intent.getBooleanExtra(KEY_IS_LIVE, true);
		mGroupId = intent.getStringExtra(KEY_GROUPID);
		mPlayUrl = intent.getStringExtra(KEY_PLAY_URL);
		mJoinGroupJson = intent.getStringExtra(KEY_JOIN_GROUP);
	}

	/**
	 * 页面初始化
	 */
	@SuppressLint("HandlerLeak")
	private void initView() {
		mLiveBackBtn = (Button) findViewById(R.id.live_back_btn);
		mTitleTv = (TextView) findViewById(R.id.live_title);
		mRefirshBtn = (Button) findViewById(R.id.live_refirsh_btn);

		mTimeOutText = (TextView) findViewById(R.id.live_time_out_text);
		mVideoLoading = (RelativeLayout) findViewById(R.id.live_video_loading);
		// mPlayLayout = (RelativeLayout)
		// findViewById(R.id.live_video_play_layout);

		mBottomLayout = (RelativeLayout) findViewById(R.id.live_bottomlayout);
		mLiveLookTalk = (ImageButton) findViewById(R.id.livelook_ppt);
		mLiveTalk = (ImageButton) findViewById(R.id.live_ppt);
		mQiangPaiLayout = (LinearLayout) findViewById(R.id.live_qiangpai);
		mExitLayout = (LinearLayout) findViewById(R.id.live_exit);
		mExitBtn = (ImageView) findViewById(R.id.live_exit_btn);
		mQiangPaiLayout.setOnTouchListener(this);
		mExitLayout.setOnTouchListener(this);
		mLiveLookTalk.setOnTouchListener(this);
		mLiveTalk.setOnTouchListener(this);

		mAddressTv = (TextView) findViewById(R.id.live_address);
		mTalkingTv = (TextView) findViewById(R.id.live_talking);

		mQiangpaiImg = (ImageView) findViewById(R.id.qiangpai_img);

		mRPVPalyVideo = (RtmpPlayerView) findViewById(R.id.live_vRtmpPlayVideo);
		// 先显示气泡上的默认图片

		// 注册事件
		mLiveBackBtn.setOnClickListener(this);
		mRefirshBtn.setOnClickListener(this);
		// mPlayLayout.setOnClickListener(this);

		// 更新UI handler
		mLiveVideoHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int what = msg.what;
				switch (what) {
				case 1:
					// 测试获取视频详情
					Object obj = new Object();
					LiveVideoDataCallBack(1, obj);
					break;
				case 2:
					// 5秒超时显示,提示文字
					mTimeOutText.setVisibility(View.VISIBLE);
					break;
				}
			}
		};
	}

	@Override
	protected void onResume() {
		super.onResume();
		mApp.setTalkListener(this);
	}

	private void switchView() {
		if (isShareLive) {
			// 直播
			mBottomLayout.setVisibility(View.VISIBLE);
			mLiveLookTalk.setVisibility(View.GONE);
			mLiveTalk.setVisibility(View.VISIBLE);
		} else {
			// 看别人直播
			mBottomLayout.setVisibility(View.GONE);
			mLiveLookTalk.setVisibility(View.VISIBLE);
			mLiveTalk.setVisibility(View.GONE);
		}
	}

	private void initMap() {
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.live_bmapView);

		mMapView.showZoomControls(false);
		mMapView.showScaleControl(false);
		mBaiduMap = mMapView.getMap();
		mBaiduMapManage = new BaiduMapManage(this, mBaiduMap, "LiveVideo");
	}

	/**
	 * 视频播放初始化
	 */
	private void videoInit() {
		// 视频事件回调注册
		mRPVPalyVideo.setPlayerListener(this);
		// 设置视频源
		mRPVPalyVideo.setConnectionTimeout(30000);
		if (isShareLive) {
			mFilePath = "rtsp://admin:123456@192.168.43.234/sub";
		} else {
			mFilePath = "其它人的地址";
		}

		mRPVPalyVideo.setDataSource(mFilePath);
		mRPVPalyVideo.start();

		if (isShareLive) {
			// 开启直播
			this.startLive("share110");
		}
	}

	/**
	 * 加入爱滔客群组
	 * 
	 * @author jiayf
	 * @date Apr 2, 2015
	 */
	private void joinAitalkGroup() {
		LogUtil.e(null, "jyf-------live------aitalk:join: " + mJoinGroupJson);
		final int cmd = isShareLive ? ITalkFn.Talk_CommCmd_JoinGroupWithInfo : ITalkFn.Talk_CommCmd_JoinGroupWithInfo;
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Talk, cmd, mJoinGroupJson);
	}

	private void cancelTimer() {
		if (null != mRecordTimer) {
			mRecordTimer.cancel();
			mRecordTimer.purge();
			mRecordTimer = null;
		}
	}

	/**
	 * 开启直播录制上传
	 * 
	 * @param aid
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void startLive(String aid) {
		liveVid = aid;
		curRecordTime = 0;
		cancelTimer();
		mRecordTimer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {

				curRecordTime++;
				if (curRecordTime >= LIVERECORDINGTIME) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {

							if (CarRecorderManager.isRTSPLiving()) {
								try {
									isStartLive = false;
									CarRecorderManager.stopRTSPLive();
								} catch (RecorderStateException e) {
									e.printStackTrace();
								}

							}

						}
					});
					cancelTimer();
				}
			}
		};

		mRecordTimer.schedule(task, 1000, 1000);

		if (isStartLive) {
			return;
		}
		if (!CarRecorderManager.isRTSPLiving()) {
			try {
				SharedPreferences sp = getSharedPreferences("CarRecorderPreferaces", Context.MODE_PRIVATE);
				sp.edit().putString("url_live", "rtmp://211.103.234.234/live/" + liveVid).apply();
				sp.edit().commit();
				CarRecorderManager.updateLiveConfiguration(new PreferencesReader(this).getConfig());
				CarRecorderManager.setLiveMute(true);
				CarRecorderManager.startRTSPLive();
				isStartLive = true;
			} catch (RecorderStateException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取视频直播数据
	 */
	@SuppressWarnings("static-access")
	private void getVideoLiveData() {
		// mPlayLayout.setVisibility(View.GONE);
		mVideoLoading.setVisibility(View.VISIBLE);
		mTimeOutText.setVisibility(View.GONE);

		String condi = "{\"uid\":\"" + mUid + "\",\"desAid\":\"" + mAid + "\"}";
		console.log("PageType_GetVideoDetail---获取直播详情---" + condi);
		boolean b = mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage,
				IPageNotifyFn.PageType_GetVideoDetail, condi);
		if (!b) {
			console.log("PageType_GetVideoDetail---获取直播详情---失败" + b);
		} else {
			// 5秒未开始直播,显示提示文字
			mLiveVideoHandler.sendEmptyMessageDelayed(2, 5000);
		}
	}

	/**
	 * 设置视频直播地址
	 * 
	 * @param data
	 */
	private void setVideoLiveUrl(JSONObject data) {
		try {
			JSONObject json = data.getJSONObject("data");
			// 请求成功
			// 视频直播流地址
			String vurl = json.getString("vurl");
			if (!"".equals(vurl) && null != vurl) {
				// 把视频直播流给到播放器
				mFilePath = vurl;
				// 视频初始化
				// videoInit();
			} else {
				// 获取图片数据
				String picUrl = json.getString("picurl");
				if (!"".equals(picUrl) && null != picUrl) {
					// 调用图片下载接口
				}
			}
			// 获取经纬度数据
			String lon = json.getString("lon");
			String lat = json.getString("lat");
			String head = json.getString("head");
			if (!"".equals(lon) && null != lon && !"".equals(lat) && null != lat) {
				// 添加地图大头针
				mBaiduMapManage.AddMapPoint(lon, lat, head);
				mBaiduMapManage.SetMapCenter(Double.parseDouble(lon), Double.parseDouble(lat));
			}
		} catch (Exception e) {

		}
	}

	private void drawPersonsHead() {
		String lonStr = "116.357428";
		String latStr = "39.93923";
		// 添加地图大头针
		mBaiduMapManage.AddMapPoint(lonStr, latStr, "");
		mBaiduMapManage.SetMapCenter(Double.parseDouble(lonStr), Double.parseDouble(latStr));
	}

	/**
	 * 视频直播数据返回
	 * 
	 * @param obj
	 */
	public void LiveVideoDataCallBack(int success, Object obj) {
		if (isShareLive) {
			return;
		}
		// String str =
		// "{\"code\":\"200\",\"state\":\"true\",\"vurl\":\"http://cdn3.lbs8.com/files/cdcvideo/test11.mp4\",\"cnt\":\"5\",\"lat\":\"39.93923\",\"lon\":\"116.357428\",\"picurl\":\"http://img.cool80.com/i/png/217/02.png\"}";
		console.log("视频直播数据返回--LiveVideoDataCallBack: success: " + success);
		if (1 == success) {
			String str = (String) obj;
			console.log("视频直播数据返回--LiveVideoDataCallBack:" + str);
			try {
				JSONObject data = new JSONObject(str);
				int code = data.getInt("code");
				switch (code) {
				case 200:
					// 获取直播数据成功
					setVideoLiveUrl(data);
					break;
				case 400:
					// 各种找不到数据
					break;
				case 405:
					// 直播方一定时间内没有上传经纬度信息
					break;
				case 420:
					// 直播方卡死,接受不到atk信息
					break;
				case 500:
					// 服务器异常
					break;
				}
			} catch (Exception e) {

			}
		} else {
			// console.log("请求直播详情服务错误");
			Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setTitle("提示");
			dialog.setMessage("请求直播服务超时，请重试.");
			try {
				int type = (Integer) obj;
				if (type == 0) {
					dialog.setMessage("直播方数据异常，请重试.");
				}
			} catch (Exception e) {
			}
			dialog.setNegativeButton("确认", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialoginterface, int i) {
					// 按钮事件,重试
					// showAutoLoginTip();
				}
			});
			dialog.show();

			mVideoLoading.setVisibility(View.GONE);
			// mPlayLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onDestroy() {
		console.log("liveplay---onDestroy");
		if (null != mRPVPalyVideo) {
			mRPVPalyVideo.stopPlayback();
			mRPVPalyVideo.cleanUp();
			mRPVPalyVideo = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		console.log("liveplay---onPause");
		// mPlayLayout.setVisibility(View.VISIBLE);
		super.onPause();
	};

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.live_back_btn:
			// 返回
			exit();
			break;
		case R.id.live_refirsh_btn:
			// 刷新,请求视频直播数据
			getVideoLiveData();
			break;
		case R.id.play_layout:
			// 请求视频直播数据
			getVideoLiveData();
			break;
		}
	}

	@Override
	public void onPlayerPrepared(RtmpPlayerView arg0) {
		console.log("live---onPlayerPrepared");
	}

	@Override
	public boolean onPlayerError(RtmpPlayerView arg0, int arg1, int arg2, String arg3) {
		// 视频播放出错
		console.log("live---onPlayerError" + arg2 + "," + arg3);
		mVideoLoading.setVisibility(View.GONE);
		// mPlayLayout.setVisibility(View.VISIBLE);
		return false;
	}

	@Override
	public void onPlayerCompletion(RtmpPlayerView arg0) {
		// 视频播放完成
		console.log("live---onPlayerCompletion");
		// mPlayLayout.setVisibility(View.VISIBLE);
	}

	@Override
	public void onPlayerBegin(RtmpPlayerView arg0) {
		console.log("live---onPlayerBegin");
		mVideoLoading.setVisibility(View.GONE);
	}

	@Override
	public void onPlayBuffering(RtmpPlayerView arg0, boolean start) {
		console.log("live---onPlayBuffering---" + "arg1---" + start);
		if (start) {
			// 缓冲开始
		} else {
			// 缓冲结束
		}
	}

	@Override
	public void onGetCurrentPosition(RtmpPlayerView arg0, int arg1) {
		// console.log("onGetCurrentPosition");
	}

	/** 8s视频定时器 */
	private Timer m8sTimer = null;
	/** 当前拍摄时间 */
	private int mShootTime = 0;

	int[] shootImg = { R.drawable.live_btn_8s_record, R.drawable.live_btn_7s_record, R.drawable.live_btn_6s_record,
			R.drawable.live_btn_5s_record, R.drawable.live_btn_4s_record, R.drawable.live_btn_3s_record,
			R.drawable.live_btn_2s_record, R.drawable.live_btn_1s_record };

	/**
	 * 8s视频一键抢拍
	 * 
	 * @author xuhw
	 * @date 2015年3月4日
	 */
	private void startTrimVideo() {
		if (null != m8sTimer) {
			// 正在录制
			return;
		}
		mShootTime = 0;
		m8sTimer = new Timer();
		TimerTask task = new TimerTask() {
			public void run() {
				mShootTime++;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						LogUtil.e("", "mShootTime-------: " + mShootTime);
						if (mShootTime > 16) {
							stopTrimVideo();
							return;
						}
						if (mShootTime % 2 != 0) {
							if (1 == mShootTime) {
								SoundUtils.getInstance().play(SoundUtils.RECORD_SEC);
							}
							mQiangpaiImg.setBackgroundResource(shootImg[mShootTime / 2]);
						} else {
							if (16 == mShootTime) {
								SoundUtils.getInstance().play(SoundUtils.RECORD_CAMERA);
							} else {
								SoundUtils.getInstance().play(SoundUtils.RECORD_SEC);
							}
						}
					}
				});
			}
		};
		m8sTimer.schedule(task, 500, 500);
	}

	/**
	 * 停止８s视频操作
	 * 
	 * @author xuhw
	 * @date 2015年3月8日
	 */
	private void stopTrimVideo() {
		mShootTime = 0;
		mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_8s);
		if (null != m8sTimer) {
			m8sTimer.cancel();
			m8sTimer.purge();
			m8sTimer = null;
		}
	}

	/**
	 * 退出直播或观看直播
	 * 
	 * @author jiayf
	 * @date Apr 2, 2015
	 */
	private void exit() {
		final int cmd = isShareLive ? IPageNotifyFn.PageType_LiveStop : IPageNotifyFn.PageType_PlayStop;
		mApp.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, cmd, "");
		// 停止计时器
		stopTrimVideo();
		finish();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int id = v.getId();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (id == R.id.live_qiangpai) {
				if (null != m8sTimer) {
					// 正在录制
					return true;
				}
				mQiangpaiImg.setBackgroundResource(R.drawable.live_btn_8s_press);
				LogUtil.e(null, "mobile-----onTouch-----:  qiangpai down");
			} else if (id == R.id.live_exit) {
				LogUtil.e(null, "mobile-----onTouch-----:  exit down");
				mExitBtn.setBackgroundResource(R.drawable.live_btn_off_press);
			} else if (id == R.id.livelook_ppt) {
				mLiveLookTalk.setBackgroundResource(R.drawable.livelook_btn_ptt_press);
			} else if (id == R.id.live_ppt) {
				mLiveTalk.setBackgroundResource(R.drawable.live_btn_ptt_press);
			}
			break;
		case MotionEvent.ACTION_UP:
			if (id == R.id.live_qiangpai) {
				startTrimVideo();
				LogUtil.e(null, "mobile-----onTouch-----:  qiangpai up");
			} else if (id == R.id.live_exit) {
				LogUtil.e(null, "mobile-----onTouch-----:  exit up");
				exit();
			} else if (id == R.id.livelook_ppt) {
				mLiveLookTalk.setBackgroundResource(R.drawable.livelook_btn_ptt_normal);
			} else if (id == R.id.live_ppt) {
				mLiveTalk.setBackgroundResource(R.drawable.live_btn_ptt_normal);
			}
			break;
		default:
			break;
		}
		return false;
	}

	/**
	 * 更新PPT的状态，
	 * 
	 * @param isEnable
	 *            　true/false 目前可用/不可用
	 * @author jiayf
	 * @date Apr 2, 2015
	 */
	private void refreshPPtState(boolean isEnable) {
		if (isShareLive) {
			mLiveTalk.setEnabled(isEnable);
			if (isEnable) {
				mLiveTalk.setBackgroundResource(R.drawable.live_btn_ptt_normal);
			} else {
				mLiveTalk.setBackgroundResource(R.drawable.live_btn_ptt_disable);
			}
		} else {
			mLiveLookTalk.setEnabled(isEnable);
			if (isEnable) {
				mLiveLookTalk.setBackgroundResource(R.drawable.livelook_btn_ptt_normal);
			} else {
				mLiveLookTalk.setBackgroundResource(R.drawable.livelook_btn_ptt_disable);
			}
		}
	}

	@Override
	public void TalkNotifyCallBack(int type, String data) {
		LogUtil.e(null, "jyf-------live------TalkNotifyCallBack type: " + type + "  data:" + data);
		switch (type) {
		case Talk_Event_ChanleIn:
			// 进入频道相关
			intoChannelEvent(type, data);
			break;
		case Talk_Event_ChanleInterAction:
			// 频道内交互事件
			channelInteractionEvent(type, data);
			break;
		default:
			break;
		}
	}

	/**
	 * 进入频道相关事件
	 * 
	 * @param event
	 * 
	 * @author qianwei
	 * @date 2014/04/08
	 */
	private void intoChannelEvent(int event, String message) {
		switch (event) {
		case 0:// 正在获取频道信息
			break;
		case 1:// 获取频道信息成功
			break;
		case 2:// 获取频道信息失败
			break;
		case 3:// 正在进入爱淘客频道
			break;
		case 4:// 进入爱淘客频道成功
			break;
		case 5:// 自动重新进入爱淘客频道成功
			break;
		case 6:// 进入爱淘客频道失败
			break;
		case 7:// 频道退出
			break;
		}
	}

	/**
	 * 频道内交互事件
	 * 
	 * @param event
	 * 
	 * @author qianwei
	 * @date 2014/04/08
	 */
	private void channelInteractionEvent(int event, String message) {
		// switch (event) {
		// case 0:// 有人开始说话
		// Log.e("", "SSSSSSSSSSSSSSS   有人开始说话!");
		// callBack_startSpeak(message);
		// break;
		// case 1:// 有人结束说话
		// // callBack_endSpeak(message);
		//
		// Utils.writeShootLog("================有人结束说话==================="+message);
		// mLeftViewListManger.setData(ViewListManager.KEY_LEFT_MAIN,
		// IViewDealFn.EVENT_WALKIE_CLOSE, message);
		// break;
		// case 2:// 本人说话请求被拒绝
		// case 3:// 本人说话请求正在排队
		// // 如果当前是手动模式，通知遥控器说话失败
		// if (RECORD_MODE_MANUAL == mCurrentRecordMode) {
		// this.sendSpeakState(STATE_FAILED);
		// isSpeaking = false;
		// setRecordMode(RECORD_MODE_AUTO);
		// }
		//
		// Log.e("", "SSSSSSSSSSSSSSS   本人说话请求正在排队！");
		// mHandler.removeMessages(MSG_SPEECH_OUT_TIME);
		// DialogManager.getInstance().hideLoadingDialog();
		// mTalkManage.refreshUImicroBusy();
		//
		// Utils.writeShootLog("================说话请求被拒绝==================="+message);
		// mLeftViewListManger.setData(ViewListManager.KEY_LEFT_MAIN,
		// IViewDealFn.EVENT_WALKIE_BUSY, null);
		// break;
		// case 4:// 本人说话请求状态错误
		// if (RECORD_MODE_MANUAL == mCurrentRecordMode) {
		// this.sendSpeakState(STATE_FAILED);
		// isSpeaking = false;
		// setRecordMode(RECORD_MODE_AUTO);
		// }
		// DialogManager.getInstance().hideLoadingDialog();
		// mHandler.removeMessages(MSG_SPEECH_OUT_TIME);
		// mTalkManage.refreshUImicroBusy();
		// Log.e("", "SSSSSSSSSSSSSSS   本人说话请求状态错误！");
		//
		// Utils.writeShootLog("================说话请求状态错误==================="+message);
		// mLeftViewListManger.setData(ViewListManager.KEY_LEFT_MAIN,
		// IViewDealFn.EVENT_WALKIE_BUSY, null);
		// break;
		// }
	}

	// 有人开始说话
	private void callBack_startSpeak(String message) {
		// TalkManage.isCanRefreshVolumeUI = true;
		// boolean isMe = false;
		// try {
		// JSONObject json = new JSONObject(message);
		// isMe = json.getBoolean("isme");
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }
		// // 他人说话时不做处理，只有当前用户说话时才进行30秒计时。
		// if (isMe) {
		// mTalkManage.hideMapSpeechLoading();
		// mTalkManage.refreshUICanSpeakDown();
		// // TODO
		// // 此时说话按钮应该是按下状态
		// mHandler.removeMessages(MSG_SPEECH_OUT_TIME);
		// mHandler.removeMessages(MSG_SPEECH_COUNT_DOWN);
		// // mHandler.sendEmptyMessageDelayed(MSG_SPEECH_OUT_TIME,
		// // SPEAKTIMEOUT);
		// }
	}

	// 有人结束说话
	private void callBack_endSpeak(String message) {
		// String aidEnd = null;
		// boolean isMeEnd = false;
		// try {
		// JSONObject json = new JSONObject(message);
		// aidEnd = json.optString("aid");
		// isMeEnd = json.optBoolean("isme");
		//
		// JSONObject json2 = new JSONObject();
		// json2.put("volume", 0);
		// Log.e("", "voice------value: end  ");
		// mTalkManage.refreshVolueUI(json2.toString());
		// TalkManage.isCanRefreshVolumeUI = false;
		// } catch (JSONException e) {
		// e.printStackTrace();
		// }
		// if (mSpeechEnable) {
		// mHandler.removeMessages(MSG_SPEECH_OUT_TIME);
		// mHandler.removeMessages(MSG_SPEECH_COUNT_DOWN);
		// }
	}

}
