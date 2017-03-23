package com.mobnote.golukmain.special;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmain.carrecorder.util.MD5Utils;
import com.mobnote.golukmain.carrecorder.util.SettingUtils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.videosuqare.RTPullListView;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRTScrollListener;
import com.mobnote.golukmain.videosuqare.RTPullListView.OnRefreshListener;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.SharedPrefUtil;

public class ClusterListActivity extends BaseActivity implements
        OnClickListener, VideoSuqareManagerFn {

    private RTPullListView mRTPullListView = null;
    private List<ClusterInfo> mDataList = null;

    public CustomLoadingDialog mCustomProgressDialog = null;

    private ClusterInfo begantime = null;
    private ClusterInfo endtime = null;

    private ImageButton mBackBtn = null;
    private RelativeLayout loading = null;

    /**
     * 保存列表一个显示项索引
     */
    private int wonderfulFirstVisible;
    /**
     * 保存列表显示item个数
     */
    private int wonderfulVisibleCount;
    /**
     * 是否还有分页
     */
    private boolean isHaveData = true;
    /**
     * 视频广场类型
     */
    private String type;
    /**
     * 1：上拉 2：下拉 0:第一次
     */
    private int uptype = 0;

    /**
     * 广场视频列表默认背景图片
     */
    private RelativeLayout squareTypeDefault;

    // 点播分类
    private String attribute;

    SharePlatformUtil sharePlatform;

    private String historyDate;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf;

    ClusterViewAdapter clusterViewAdapter = null;

    private SpecialDataManage sdm = new SpecialDataManage();

    private SpecialInfo headdata;

    private TextView textTitle;

    private String ztid;

    private String title;

    private static String pagesize = "20";

    private Button titleShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cluster_list);

        sdf = new SimpleDateFormat(this.getString(R.string.str_date_formatter));

        Intent intent = getIntent();

        ztid = intent.getStringExtra("ztid");
        title = intent.getStringExtra("title");

        textTitle = (TextView) findViewById(R.id.title);
        historyDate = SettingUtils.getInstance().getString("gcHistoryDate",
                sdf.format(new Date()));

        SettingUtils.getInstance().putString("gcHistoryDate",
                sdf.format(new Date()));

        GolukApplication.getInstance().getVideoSquareManager()
                .addVideoSquareManagerListener("ClusterListActivity", this);
        mDataList = new ArrayList<ClusterInfo>();

        mRTPullListView = (RTPullListView) findViewById(R.id.mRTPullListView);
        mRTPullListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        squareTypeDefault = (RelativeLayout) findViewById(R.id.square_type_default);
        squareTypeDefault.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mCustomProgressDialog = null;
                httpPost(true, "0", "", ztid);
            }
        });

        /** 返回按钮 */
        mBackBtn = (ImageButton) findViewById(R.id.back_btn);
        titleShare = (Button) findViewById(R.id.title_share);

        mBackBtn.setOnClickListener(this);
        titleShare.setOnClickListener(this);

        textTitle.setText(title);

        sharePlatform = new SharePlatformUtil(this);

        httpPost(true, "0", "", ztid);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != sharePlatform) {
            sharePlatform.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 获取网络数据
     *
     * @param flag 是否显示加载中对话框
     * @author xuhw
     * @date 2015年4月15日
     */
    private void httpPost(boolean flag, String operation, String timestamp,
                          String ztid) {
        if (flag) {
            if (null == mCustomProgressDialog) {
                mCustomProgressDialog = new CustomLoadingDialog(this, null);
                mCustomProgressDialog.show();
            }
        }

        boolean result = GolukApplication.getInstance().getVideoSquareManager()
                .getJHListData(ztid, operation, timestamp, pagesize);

        if (!result) {
            closeProgressDialog();
        }
    }

    private void init(boolean isloading) {

        if (null == clusterViewAdapter) {
            clusterViewAdapter = new ClusterViewAdapter(this, 2, sharePlatform);
        }

        mRTPullListView.setAdapter(clusterViewAdapter, historyDate);
        clusterViewAdapter.setData(mDataList, headdata);
        mRTPullListView.setonRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                historyDate = SettingUtils.getInstance().getString(
                        "gcHistoryDate", sdf.format(new Date()));
                SettingUtils.getInstance().putString("gcHistoryDate",
                        sdf.format(new Date()));
                if (begantime != null) {
                    uptype = 2;
                    httpPost(false, "0", "", ztid);

                } else {
                    mRTPullListView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRTPullListView.onRefreshComplete(historyDate);
                        }
                    }, 1500);
                }

            }
        });

        mRTPullListView.setOnRTScrollListener(new OnRTScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView arg0, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {

                    if (mRTPullListView.getAdapter().getCount() == (wonderfulFirstVisible + wonderfulVisibleCount)) {
                        if (isHaveData) {
                            uptype = 1;
                            httpPost(true, "2", endtime.sharingtime, ztid);
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

        if (isloading == false) {
            // 有下一页刷新
            if (isHaveData) {
                loading = (RelativeLayout) LayoutInflater.from(this).inflate(
                        R.layout.video_square_below_loading, null);
                mRTPullListView.addFooterView(loading);
            }
        }

    }

    public void flush() {
        clusterViewAdapter.setData(mDataList, headdata);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back_btn) {
            this.finish();
        } else if (id == R.id.title_share) {
            if (!SharePlatformUtil.checkShareableWhenNotHotspot(ClusterListActivity.this)) return;
            if (null == mCustomProgressDialog) {
                mCustomProgressDialog = new CustomLoadingDialog(this, null);
            }
            mCustomProgressDialog.show();
            boolean result = GolukApplication.getInstance().getVideoSquareManager().getTagShareUrl("2", ztid);
            if (result == false) {
                mCustomProgressDialog.close();
                GolukUtils.showToast(this, this.getString(R.string.network_error));
            } else {
                // 把之前保存的单个视频id清空，分享结果回来后就使用整个聚合的vid
                if (null != clusterViewAdapter) {
                    clusterViewAdapter.setWillShareVideoId(null);
                }
            }
        } else {
        }
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
    public void VideoSuqare_CallBack(int event, int msg, int param1,
                                     Object param2) {
        if (event == VSquare_Req_List_Tag_Content) {
            closeProgressDialog();
            if (RESULE_SUCESS == msg) {

                List<ClusterInfo> list = sdm.getClusterList((String) param2);
                headdata = sdm.getClusterHead((String) param2);
                // 说明有数据
                if (list.size() > 0) {
                    begantime = list.get(0);
                    endtime = list.get(list.size() - 1);

                    if (uptype == 0) {// 说明是第一次
                        if (list.size() >= 20) {
                            isHaveData = true;
                        } else {
                            isHaveData = false;
                        }
                        mDataList = list;
                    } else if (uptype == 1) {// 上拉刷新
                        if (list.size() >= 20) {// 数据超过20条
                            isHaveData = true;
                        } else {// 数据没有20条
                            isHaveData = false;
                            if (loading != null) {
                                if (mRTPullListView != null) {
                                    mRTPullListView.removeFooterView(loading);
                                    loading = null;
                                }
                            }
                        }
                        mDataList.addAll(list);
                        flush();
                    } else if (uptype == 2) {// 下拉刷新
                        mDataList.clear();

                        if (list.size() >= 20) {// 数据超过20条
                            isHaveData = true;
                        } else {// 数据没有20条
                            isHaveData = false;
                        }

                        if ("1".equals(type)) {// 直播
                            mDataList = list;
                        } else {
                            list.addAll(mDataList);
                            mDataList = list;
                        }

                        mRTPullListView.onRefreshComplete(historyDate);
                        flush();
                    }

                    init(false);

                } else {// 没有数据
                    isHaveData = false;

                    if (headdata != null) {
                        init(false);
                    } else {
                        if (uptype == 1) {// 上拉刷新
                            if (loading != null) {
                                if (mRTPullListView != null) {
                                    mRTPullListView.removeFooterView(loading);
                                    loading = null;
                                }
                            }
                        } else if (uptype == 2) {// 下拉刷新
                            if ("1".equals(type)) {// 直播
                                mDataList.clear();
                            }
                            mRTPullListView.onRefreshComplete(historyDate);
                        }
                    }
                }

            } else {
                isHaveData = false;

                if (0 == uptype) {
                    closeProgressDialog();
                } else if (1 == uptype) {
                    if (mRTPullListView != null) {
                        mRTPullListView.removeFooterView(loading);
                        loading = null;
                    }
                } else if (2 == uptype) {
                    mRTPullListView.onRefreshComplete(historyDate);
                }
                GolukUtils.showToast(ClusterListActivity.this, this.getString(R.string.network_error));
            }

            if (mDataList.size() > 0) {
                squareTypeDefault.setVisibility(View.GONE);
                mRTPullListView.setVisibility(View.VISIBLE);
            } else {
                squareTypeDefault.setVisibility(View.VISIBLE);
                mRTPullListView.setVisibility(View.GONE);
            }
        } else if (event == VSquare_Req_VOP_GetShareURL_Video
                || event == VSquare_Req_VOP_GetShareURL_Topic_Tag) {
            if (RESULE_SUCESS == msg) {
                try {
                    JSONObject result = new JSONObject((String) param2);
                    if (result.getBoolean("success")) {
                        JSONObject data = result.getJSONObject("data");
                        GolukDebugUtils.i("detail",
                                "------VideoSuqare_CallBack--------data-----"
                                        + data);
                        String shareurl = data.getString("shorturl");
                        String coverurl = data.getString("coverurl");
                        String describe = data.optString("describe");
                        String realDesc = this.getString(R.string.str_share_board_real_desc);

                        if (TextUtils.isEmpty(describe)) {
                            describe = this.getString(R.string.str_share_describe);
                        }
                        String ttl = this.getString(R.string.str_share_title);

                        // 缩略图
                        Bitmap bitmap = getThumbBitmap(headdata.imagepath);

                        if (this != null && !this.isFinishing()) {
                            mCustomProgressDialog.close();

                            ThirdShareBean shareBean = new ThirdShareBean();
                            shareBean.surl = shareurl;
                            shareBean.curl = coverurl;
                            shareBean.db = describe;
                            shareBean.tl = ttl;
                            shareBean.bitmap = bitmap;
                            shareBean.realDesc = realDesc;
                            shareBean.videoId = getShareVideoId();
                            shareBean.from = this.getString(R.string.str_zhuge_share_video_network_other);

                            ProxyThirdShare shareBoard = new ProxyThirdShare(this, sharePlatform, shareBean);
                            shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
                        }
                    } else {
                        GolukUtils.showToast(this, this.getString(R.string.network_error));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                mCustomProgressDialog.close();
                GolukUtils.showToast(this, this.getString(R.string.network_error));
            }
        }

    }


    private String getShareVideoId() {
        if (null != this.clusterViewAdapter) {
            // 单个视频的分享
            return this.clusterViewAdapter.getWillShareVideoId();
        } else {
            // 整个聚合的分享
            return ztid;
        }
    }

    public Bitmap getThumbBitmap(String netUrl) {
        String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
        String path = Environment.getExternalStorageDirectory()
                + File.separator + "goluk/image_cache";
        File file = new File(path + File.separator + name);
        Bitmap t_bitmap = null;
        if (file.exists()) {
            t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(),
                    50, 50);
        }
        return t_bitmap;
    }

    /**
     * 初始化历史请求数据
     *
     * @throws
     * @Title: loadHistorydata
     * @Description: TODO void
     * @author 曾浩
     */
    public void loadHistorydata() {

        String videos = "";//test();// GolukApplication.getInstance().getVideoSquareManager().getSquareList(attribute);
        if (videos != null && !"".equals(videos)) {
            List<ClusterInfo> list = sdm.getClusterList(videos);
            headdata = sdm.getClusterHead(videos);

            if (list != null && list.size() > 0) {
                mDataList = list;
                begantime = list.get(0);
                endtime = list.get(list.size() - 1);
                init(true);
            }
        }

    }

//	public String test() throws JSONException {
//		JSONObject jx = new JSONObject();
//		jx.put("success", true);
//		jx.put("msg", "成功");
//
//		JSONObject data = new JSONObject();
//		data.put("result", "0");
//		data.put("count", "1");
//
//		JSONObject video = new JSONObject();
//		video.put("videoid", "12121");
//		video.put("type", "2");
//		video.put("sharingtime", "2015/08/01");
//		video.put("describe", "记录卡记录卡据了解乐扣乐扣交流交流框架梁极乐空间垃圾筐拉进来");
//		video.put("clicknumber", "21");
//		video.put("praisenumber", "232323");
//		video.put("picture",
//				"http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png");
//		video.put("livesdkaddress",
//				"http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png");
//
//		JSONObject commentdata = new JSONObject();
//		commentdata.put("commentid", "2312");
//		commentdata.put("authorid", "34233");
//		commentdata.put("name", "大狗");
//		commentdata.put("avatar", "2");
//		commentdata.put("time", "2015/02/22");
//		commentdata.put("text", "来健身卡来对付框架思路东风路斯蒂芬简历上");
//
//		JSONObject commentdata2 = new JSONObject();
//		commentdata2.put("commentid", "2312");
//		commentdata2.put("authorid", "34233");
//		commentdata2.put("name", "二狗");
//		commentdata2.put("avatar", "2");
//		commentdata2.put("time", "2015/02/22");
//		commentdata2.put("text", "离开家你弄死的放上来的咖啡机三闾大夫接口六角恐龙接口链接冷静冷静记录框架梁");
//
//		JSONArray comments = new JSONArray();
//		comments.put(commentdata);
//		comments.put(commentdata2);
//
//		JSONObject comment = new JSONObject();
//		comment.put("iscomment", "1");
//		comment.put("comcount", "2");
//		comment.put("iscomment", "1");
//		comment.put("comlist", comments);
//
//		video.put("comment", comment);
//
//		JSONObject user = new JSONObject();
//		user.put("uid", "32323");
//		user.put("nickname", "为什么不");
//		user.put("headportrait", "2");
//		user.put("sex", "1");
//
//		JSONObject videodata = new JSONObject();
//		videodata.put("video", video);
//		videodata.put("user", user);
//
//		JSONArray videos = new JSONArray();
//		videos.put(videodata);
//
//		data.put("videolist", videos);
//
//		JSONObject head = new JSONObject();
//		head.put("showhead", "1");
//		head.put("headimg",
//				"http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
//		head.put("headvideoimg",
//				"http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
//		head.put("headvideo",
//				"http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
//		head.put("ztIntroduction", "六角恐龙极乐空间六角恐龙极乐空间");
//		head.put("outurl", "www.baidu.com");
//		head.put("outurlname", "百度");
//		head.put("ztitle", "测试title");
//
//		data.put("head", head);
//
//		jx.put("data", data);
//		// {“result”:”0”,“head”:{},“videolist”:[],”commentlist”:{}}
//		return jx.toString();
//
//	}

}
