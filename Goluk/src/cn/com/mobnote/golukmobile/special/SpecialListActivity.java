package cn.com.mobnote.golukmobile.special;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.BaseActivity;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.carrecorder.util.SettingUtils;
import cn.com.mobnote.golukmobile.carrecorder.view.CustomLoadingDialog;
import cn.com.mobnote.golukmobile.thirdshare.SharePlatformUtil;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SpecialListActivity extends BaseActivity implements OnClickListener, VideoSuqareManagerFn {
	private SpecialListViewAdapter specialListViewAdapter = null;
	private List<SpecialInfo> mDataList = null;
	public CustomLoadingDialog mCustomProgressDialog = null;
	private ImageButton mBackBtn = null;

	public String shareVideoId;

	/** 广场视频列表默认背景图片 */
	private ImageView squareTypeDefault;

	SharePlatformUtil sharePlatform;

	private TextView outurl;

	private TextView comment1;

	private TextView comment2;

	private TextView comment3;

	private TextView commentLink;

	private ListView lv;

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH时mm分ss秒");

	private SpecialDataManage sdm = new SpecialDataManage();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.special_list);

		// GolukApplication.getInstance().getVideoSquareManager().addVideoSquareManagerListener("videocategory",
		// this);

		mDataList = new ArrayList<SpecialInfo>();
		lv = (ListView) findViewById(R.id.special_list);
		squareTypeDefault = (ImageView) findViewById(R.id.square_type_default);
		squareTypeDefault.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mCustomProgressDialog = null;
				httpPost(true, "1", "0", "");
			}
		});

		/** 返回按钮 */
		mBackBtn = (ImageButton) findViewById(R.id.back_btn);
		mBackBtn.setOnClickListener(this);

		sharePlatform = new SharePlatformUtil(this);
		sharePlatform.configPlatforms();// 设置分享平台的参数
		loadHistorydata();// 显示历史请求数据
		// httpPost(true, "2", "0", "");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (null != sharePlatform) {
			sharePlatform.mSinaWBUtils.onActivityResult(requestCode, resultCode, data);
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
	private void httpPost(boolean flag, String type, String operation, String timestamp) {
		if (flag) {
			if (null == mCustomProgressDialog) {
				mCustomProgressDialog = new CustomLoadingDialog(this, null);
				mCustomProgressDialog.show();
			}
		}

		boolean result = GolukApplication.getInstance().getVideoSquareManager()
				.getSquareList("1", type, "", operation, timestamp);
		if (!result) {
			closeProgressDialog();
		}
	}

	private void init(boolean isloading) {

		if (null == specialListViewAdapter) {
			specialListViewAdapter = new SpecialListViewAdapter(this, 2);
		}

		specialListViewAdapter.setData(mDataList);
		lv.setAdapter(specialListViewAdapter);

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
			GolukUtils.showToast(SpecialListActivity.this, "第三方分享失败");
			return;
		}
		GolukApplication.getInstance().getVideoSquareManager().shareVideoUp(channel, shareVideoId);
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
	public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
		if (event == SquareCmd_Req_SquareList) {
			closeProgressDialog();
			if (RESULE_SUCESS == msg) {

				List<SpecialInfo> list;
				try {
					list = sdm.getListData(param2.toString());
					// 说明有数据
					if (list != null && list.size() > 0) {
						mDataList.clear();
						mDataList = list;
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {

				GolukUtils.showToast(SpecialListActivity.this, "网络异常，请检查网络");
			}

			if (mDataList.size() > 0) {
				squareTypeDefault.setVisibility(View.GONE);
				lv.setVisibility(View.VISIBLE);
			} else {
				squareTypeDefault.setVisibility(View.VISIBLE);
				lv.setVisibility(View.GONE);
			}
		}

	}

	/**
	 * 初始化历史请求数据
	 * 
	 * @Title: loadHistorydata
	 * @Description: TODO void
	 * @author 曾浩
	 * @throws
	 */
	public void loadHistorydata() {
		String param;
		try {
			param = this.test();// GolukApplication.getInstance().getVideoSquareManager().getSquareList("");
			if (param != null && !"".equals(param)) {
				List<SpecialInfo> list;
				try {
					list = sdm.getListData(param);
					Map<String, Object> map = sdm.getComments(param);

					if (map != null) {

						View view = LayoutInflater.from(this).inflate(R.layout.comment_below, null);

						String iscomment = map.get("iscomment").toString();
						if ("1".equals(iscomment)) {
							view.findViewById(R.id.push_comment).setVisibility(View.VISIBLE);
							view.findViewById(R.id.comments).setVisibility(View.VISIBLE);
						} else {
							view.findViewById(R.id.push_comment).setVisibility(View.GONE);
							view.findViewById(R.id.comments).setVisibility(View.GONE);
						}

						outurl = (TextView) view.findViewById(R.id.outurl);

						comment1 = (TextView) view.findViewById(R.id.comment1);

						comment2 = (TextView) view.findViewById(R.id.comment2);

						comment3 = (TextView) view.findViewById(R.id.comment3);

						commentLink = (TextView) view.findViewById(R.id.comment_link);

						commentLink.setText("查看所有  " + map.get("comcount") + " 条评论");

						lv.addFooterView(view);

						List<CommentInfo> comments = (List<CommentInfo>) map.get("comments");
						if (comments != null && comments.size() > 0) {

							String source = null;
							for (int i = 0; i < comments.size(); i++) {
								CommentInfo ci = comments.get(i);
								source = "<font color='#0B3FA2'>" + ci.name + "</font>  " + ci.text;

								if (i == 0) {
									comment1.setVisibility(View.VISIBLE);
									comment1.setText(Html.fromHtml(source));

								} else if (i == 1) {
									comment2.setVisibility(View.VISIBLE);
									comment2.setText(Html.fromHtml(source));

								} else if (i == 2) {
									comment3.setVisibility(View.VISIBLE);
									comment3.setText(Html.fromHtml(source));

								}
							}


						}
					}

					if (list != null && list.size() > 0) {
						mDataList = list;
						init(true);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public String test() throws JSONException {
		JSONObject jx = new JSONObject();
		jx.put("success", true);
		jx.put("msg", "成功");

		JSONObject data = new JSONObject();
		data.put("result", "0");
		data.put("count", "1");

		JSONObject video = new JSONObject();
		video.put("videoid", "12121");
		video.put("type", "2");
		video.put("sharingtime", "2015/08/01");
		video.put("describe", "记录卡记录卡据了解乐扣乐扣交流交流框架梁极乐空间垃圾筐拉进来");
		video.put("picture", "http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png");
		video.put("livesdkaddress", "http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png");

		JSONObject user = new JSONObject();
		user.put("uid", "32323");
		user.put("nickname", "为什么不");
		user.put("headportrait", "2");
		user.put("sex", "1");

		JSONObject videodata = new JSONObject();
		videodata.put("video", video);
		videodata.put("user", user);

		JSONArray videos = new JSONArray();
		videos.put(videodata);

		data.put("videolist", videos);

		JSONObject commentdata = new JSONObject();
		commentdata.put("commentid", "2312");
		commentdata.put("authorid", "34233");
		commentdata.put("name", "大狗");
		commentdata.put("avatar", "2");
		commentdata.put("time", "2015/02/22");
		commentdata.put("text", "来健身卡来对付框架思路东风路斯蒂芬简历上");

		JSONObject commentdata2 = new JSONObject();
		commentdata2.put("commentid", "2312");
		commentdata2.put("authorid", "34233");
		commentdata2.put("name", "二狗");
		commentdata2.put("avatar", "2");
		commentdata2.put("time", "2015/02/22");
		commentdata2.put("text", "离开家你弄死的放上来的咖啡机三闾大夫接口六角恐龙接口链接冷静冷静记录框架梁");

		JSONArray comments = new JSONArray();
		comments.put(commentdata);
		comments.put(commentdata2);

		JSONObject comment = new JSONObject();
		comment.put("iscomment", "1");
		comment.put("comcount", "2");
		comment.put("iscomment", "1");
		comment.put("comlist", comments);

		data.put("comment", comment);

		JSONObject head = new JSONObject();
		head.put("showhead", "1");
		head.put("headimg", "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
		head.put("headvideoimg", "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
		head.put("headvideo", "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
		head.put("ztIntroduction", "六角恐龙极乐空间六角恐龙极乐空间");
		head.put("outurl", "www.baidu.com");
		head.put("outurlname", "百度");
		head.put("ztitle", "测试title");

		data.put("head", head);

		jx.put("data", data);
		// {“result”:”0”,“head”:{},“videolist”:[],”commentlist”:{}}
		return jx.toString();

	}

}
