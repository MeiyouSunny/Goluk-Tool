package com.mobnote.golukmain.special;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.Event;
import com.mobnote.eventbus.EventUtil;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.carrecorder.util.ImageManager;
import com.mobnote.golukmain.carrecorder.util.MD5Utils;
import com.mobnote.golukmain.carrecorder.util.SoundUtils;
import com.mobnote.golukmain.carrecorder.view.CustomLoadingDialog;
import com.mobnote.golukmain.comment.CommentActivity;
import com.mobnote.golukmain.comment.ICommentFn;
import com.mobnote.golukmain.thirdshare.ProxyThirdShare;
import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.videodetail.ZTHead;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class SpecialListActivity extends BaseActivity implements OnClickListener, VideoSuqareManagerFn {
    private SpecialListViewAdapter specialListViewAdapter = null;
    private List<SpecialInfo> mDataList = null;
    public CustomLoadingDialog mCustomProgressDialog = null;
    private ImageButton mBackBtn = null;

    public String shareVideoId;

    /**
     * 广场视频列表默认背景图片
     */
    private RelativeLayout squareTypeDefault;

    SharePlatformUtil sharePlatform;

    private TextView outurl;

    private TextView comment1;

    private TextView comment2;

    private TextView comment3;

    private TextView commentLink;

    private ListView lv;

    SpecialInfo headdata = null;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf;

    private SpecialDataManage sdm = new SpecialDataManage();
    private TextView textTitle;

    private String ztid;
    private String title;

    private Button titleShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.special_list);
        EventBus.getDefault().register(this);

        sdf = new SimpleDateFormat(this.getString(R.string.str_date_formatter));

        Intent intent = getIntent();

        ztid = intent.getStringExtra("ztid");
        title = intent.getStringExtra("title");

        if (!TextUtils.isEmpty(title)) {
            if (title.length() > 12) {
                title = title.substring(0, 12) + this.getString(R.string.str_omit);
            }
        }

        GolukApplication.getInstance().getVideoSquareManager()
                .addVideoSquareManagerListener("SpecialListActivity", this);

        mDataList = new ArrayList<SpecialInfo>();
        lv = (ListView) findViewById(R.id.special_list);
        squareTypeDefault = (RelativeLayout) findViewById(R.id.square_type_default);
        squareTypeDefault.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                closeProgressDialog();
                httpPost(true, ztid);
            }
        });

        /** 返回按钮 */
        mBackBtn = (ImageButton) findViewById(R.id.back_btn);
        textTitle = (TextView) findViewById(R.id.title);
        titleShare = (Button) findViewById(R.id.title_share);

        if (!TextUtils.isEmpty(title)) {
            textTitle.setText(title);
        } else {
            textTitle.setText(getString(R.string.str_special_default_title));
        }
        mBackBtn.setOnClickListener(this);
        titleShare.setOnClickListener(this);

        sharePlatform = new SharePlatformUtil(this);
        httpPost(true, ztid);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != sharePlatform) {
            sharePlatform.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onEventMainThread(Event event) {
        if (EventUtil.isCommentSuccessEvent(event)) {
            httpPost(false, ztid);
        }
    }

    /**
     * 获取网络数据
     *
     * @param flag 是否显示加载中对话框
     * @author xuhw
     * @date 2015年4月15日
     */
    private void httpPost(boolean flag, String ztid) {
        if (flag) {
            showProgressDialog();
        }

        boolean result = GolukApplication.getInstance().getVideoSquareManager().getZTListData(ztid);
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
    public void onResume() {
        GolukApplication.getInstance().getVideoSquareManager()
                .addVideoSquareManagerListener("SpecialListActivity", this);
        super.onResume();
    }

    @Override
    public void onPause() {
        // GolukApplication.getInstance().getVideoSquareManager().removeVideoSquareManagerListener("SpecialListActivity");
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            GolukApplication.getInstance().getVideoSquareManager()
                    .removeVideoSquareManagerListener("SpecialListActivity");
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        closeProgressDialog();
        GlideUtils.clearMemory(this);
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.back_btn) {
            GolukApplication.getInstance().getVideoSquareManager()
                    .removeVideoSquareManagerListener("SpecialListActivity");
            this.finish();
        } else if (id == R.id.message || id == R.id.send || id == R.id.push_comment) {
            // 输入框
            this.startCommentActivity(true);
        } else if (id == R.id.comment_link || id == R.id.comments) {
            // 查看所有评论
            this.startCommentActivity(false);
        } else if (id == R.id.outurl) {
            Intent mBugLayout = new Intent(this, UserOpenUrlActivity.class);
            mBugLayout.putExtra("url", headdata.outurl);
            startActivity(mBugLayout);
        } else if (id == R.id.title_share) {
            if (!SharePlatformUtil.checkShareableWhenNotHotspot(SpecialListActivity.this)) return;
            showProgressDialog();
            boolean result = GolukApplication.getInstance().getVideoSquareManager().getTagShareUrl("1", ztid);
            if (result == false) {
                closeProgressDialog();
                GolukUtils.showToast(this, this.getString(R.string.network_error));
            }
        }
    }

    /**
     * 跳转到评论页面
     *
     * @throws
     * @Title: startCommentActivity
     * @Description: TODO void
     * @author 曾浩
     */
    private void startCommentActivity(boolean isShowSoft) {
        Intent it = new Intent(this, CommentActivity.class);
        it.putExtra(CommentActivity.COMMENT_KEY_ISCAN_INPUT, true);
        it.putExtra(CommentActivity.COMMENT_KEY_MID, ztid);
        it.putExtra(CommentActivity.COMMENT_KEY_SHOWSOFT, isShowSoft);
        it.putExtra(CommentActivity.COMMENT_KEY_TYPE, ICommentFn.COMMENT_TYPE_WONDERFUL_SPECIAL);
        startActivity(it);
    }

    // 分享成功后需要调用的接口
    public void shareSucessDeal(boolean isSucess, String channel) {
        if (!isSucess) {
            GolukUtils.showToast(SpecialListActivity.this, this.getString(R.string.str_third_share_fail));
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
            mCustomProgressDialog = null;
        }
    }

    /**
     * 显示加载中对话框
     *
     * @author xuhw
     * @date 2015年4月15日
     */
    private void showProgressDialog() {
        if (null == mCustomProgressDialog) {
            mCustomProgressDialog = new CustomLoadingDialog(this, null);
            mCustomProgressDialog.show();
        }
    }

    private View mHeaderView, mFooterView;
    @Override
    public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
        if (event == VSquare_Req_List_Topic_Content) {
            closeProgressDialog();
            if (RESULE_SUCESS == msg) {

                List<SpecialInfo> list;
                try {
                    list = sdm.getListData(param2.toString());

                    ZTHead headData = sdm.getSpecialHeadData(param2.toString());
                    if (null != headData && null != headData.ztitle && !"".equals(headData.ztitle)) {
                        if (headData.ztitle.length() > 12) {
                            textTitle.setText(headData.ztitle.substring(0, 12) + this.getString(R.string.str_omit));
                        } else {
                            textTitle.setText(headData.ztitle);
                        }
                    }

                    headdata = sdm.getClusterHead(param2.toString());
                    // 装载头部
                    if (headdata != null) {
                        View view = LayoutInflater.from(this).inflate(R.layout.special_list_head, null);
                        ImageView image = (ImageView) view.findViewById(R.id.mPreLoading);
                        TextView txt = (TextView) view.findViewById(R.id.video_title);

                        int width = SoundUtils.getInstance().getDisplayMetrics().widthPixels;
                        int height = (int) ((float) width / 1.77f);

                        if ("1".equals(headdata.videotype)) {
                            view.findViewById(R.id.mPlayBigBtn).setVisibility(View.GONE);
                        }

                        txt.setText(headdata.describe);
                        RelativeLayout.LayoutParams mPreLoadingParams = new RelativeLayout.LayoutParams(width, height);
                        image.setLayoutParams(mPreLoadingParams);

                        GlideUtils.loadImage(this, image, headdata.imagepath, R.drawable.tacitly_pic);

                        if (mHeaderView != null)
                            lv.removeHeaderView(mHeaderView);
                        lv.addHeaderView(view);

                        image.setOnClickListener(new SpecialCommentListener(this, null, headdata.imagepath,
                                headdata.videopath, "suqare", headdata.videotype, headdata.videoid));

                        mHeaderView = view;
                    }

                    Map<String, Object> map = sdm.getComments(param2.toString());

                    // 装载尾部
                    if (map != null) {

                        View view = LayoutInflater.from(this).inflate(R.layout.comment_below, null);

                        String iscomment = map.get("iscomment").toString();
                        if ("1".equals(iscomment)) {
                            view.findViewById(R.id.push_comment).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.push_comment).setOnClickListener(this);
                            view.findViewById(R.id.comments).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.comments).setOnClickListener(this);
                        } else {
                            view.findViewById(R.id.push_comment).setVisibility(View.GONE);
                            view.findViewById(R.id.comments).setVisibility(View.GONE);
                        }

                        outurl = (TextView) view.findViewById(R.id.outurl);

                        comment1 = (TextView) view.findViewById(R.id.comment1);

                        comment2 = (TextView) view.findViewById(R.id.comment2);

                        comment3 = (TextView) view.findViewById(R.id.comment3);

                        commentLink = (TextView) view.findViewById(R.id.comment_link);

                        outurl.setText(map.get("outurlname").toString());

                        if (map.containsKey("comcount")) {
                            try {
                                int count = Integer.parseInt(map.get("comcount").toString());
                                if (count <= 3) {
                                    commentLink.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                commentLink.setVisibility(View.GONE);
                            }

                        }

                        commentLink.setText(this.getString(R.string.str_see_comments, map.get("comcount")));

                        commentLink.setOnClickListener(this);
                        outurl.setOnClickListener(this);

                        view.findViewById(R.id.message).setOnClickListener(this);
                        view.findViewById(R.id.send).setOnClickListener(this);

                        if (mFooterView != null)
                            lv.removeFooterView(mFooterView);
                        lv.addFooterView(view);
                        mFooterView = view;

                        List<CommentInfo> comments = (List<CommentInfo>) map.get("comments");
                        if (comments != null && comments.size() > 0) {

                            for (int i = 0; i < comments.size(); i++) {
                                CommentInfo ci = comments.get(i);

                                if (i == 0) {
                                    comment1.setVisibility(View.VISIBLE);
                                    if (null != ci.replyid && !"".equals(ci.replyid) && null != ci.replyname
                                            && !"".equals(ci.replyname)) {
                                        UserUtils.showReplyText(this, comment1, ci.name, ci.replyname, ci.text);
                                    } else {
                                        UserUtils.showCommentText(comment1, ci.name, ci.text);
                                    }

                                } else if (i == 1) {
                                    comment2.setVisibility(View.VISIBLE);
                                    if (null != ci.replyid && !"".equals(ci.replyid) && null != ci.replyname
                                            && !"".equals(ci.replyname)) {
                                        UserUtils.showReplyText(this, comment2, ci.name, ci.replyname, ci.text);
                                    } else {
                                        UserUtils.showCommentText(comment2, ci.name, ci.text);
                                    }

                                } else if (i == 2) {
                                    comment3.setVisibility(View.VISIBLE);
                                    if (null != ci.replyid && !"".equals(ci.replyid) && null != ci.replyname
                                            && !"".equals(ci.replyname)) {
                                        UserUtils.showReplyText(this, comment3, ci.name, ci.replyname, ci.text);
                                    } else {
                                        UserUtils.showCommentText(comment3, ci.name, ci.text);
                                    }

                                }
                            }

                        }
                    }

                    // 说明有数据 装载list
                    if (list != null && list.size() > 0) {
                        mDataList.clear();
                        mDataList = list;
                        init(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {

                GolukUtils.showToast(SpecialListActivity.this, this.getString(R.string.network_error));
            }

            if (mDataList.size() > 0) {
                squareTypeDefault.setVisibility(View.GONE);
                lv.setVisibility(View.VISIBLE);
            } else {
                squareTypeDefault.setVisibility(View.VISIBLE);
                lv.setVisibility(View.GONE);
            }
        } else if (event == VSquare_Req_VOP_GetShareURL_Topic_Tag) {
            if (RESULE_SUCESS == msg) {
                try {
                    JSONObject result = new JSONObject((String) param2);
                    if (result.getBoolean("success")) {
                        JSONObject data = result.getJSONObject("data");
                        GolukDebugUtils.i("detail", "------VideoSuqare_CallBack--------data-----" + data);
                        String shareurl = data.getString("shorturl");
                        String coverurl = data.getString("coverurl");
                        String describe = data.optString("describe");
                        String realDesc = this.getString(R.string.cluster_jxzt_share_txt);

                        if (TextUtils.isEmpty(describe)) {
                            describe = "";
                        }
                        String ttl = title;
                        if (TextUtils.isEmpty(title)) {
                            ttl = this.getString(R.string.cluster_jx_zt_share);
                        }
                        // 缩略图
                        Bitmap bitmap = null;
                        if (headdata != null) {
                            bitmap = getThumbBitmap(headdata.imagepath);
                        }

                        if (this != null && !this.isFinishing()) {
                            closeProgressDialog();

                            ThirdShareBean shareBean = new ThirdShareBean();
                            shareBean.surl = shareurl;
                            shareBean.curl = coverurl;
                            shareBean.db = describe;
                            shareBean.tl = ttl;
                            shareBean.bitmap = bitmap;
                            shareBean.realDesc = realDesc;
                            shareBean.videoId = ztid;
                            shareBean.from = this.getString(R.string.str_zhuge_share_video_network_other);

                            ProxyThirdShare shareBoard = new ProxyThirdShare(SpecialListActivity.this, sharePlatform,
                                    shareBean);
                            shareBoard.showAtLocation(SpecialListActivity.this.getWindow().getDecorView(),
                                    Gravity.BOTTOM, 0, 0);
                        }
                    } else {
                        GolukUtils.showToast(this, this.getString(R.string.network_error));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                closeProgressDialog();
                GolukUtils.showToast(this, this.getString(R.string.network_error));
            }
        }

    }

    public Bitmap getThumbBitmap(String netUrl) {
        String name = MD5Utils.hashKeyForDisk(netUrl) + ".0";
        String path = Environment.getExternalStorageDirectory() + File.separator + "goluk/image_cache";
        File file = new File(path + File.separator + name);
        Bitmap t_bitmap = null;
        if (file.exists()) {
            t_bitmap = ImageManager.getBitmapFromCache(file.getAbsolutePath(), 50, 50);
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
        String param;
        param = "";// this.test();//
        // GolukApplication.getInstance().getVideoSquareManager().getSquareList("");
        if (param != null && !"".equals(param)) {
            List<SpecialInfo> list;
            try {
                list = sdm.getListData(param);

                if (list != null && list.size() > 0) {
                    mDataList = list;
                    init(true);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    // public String test() throws JSONException {
    // JSONObject jx = new JSONObject();
    // jx.put("success", true);
    // jx.put("msg", "成功");
    //
    // JSONObject data = new JSONObject();
    // data.put("result", "0");
    // data.put("count", "1");
    //
    // JSONObject video = new JSONObject();
    // video.put("videoid", "12121");
    // video.put("type", "2");
    // video.put("sharingtime", "2015/08/01");
    // video.put("describe", "记录卡记录卡据了解乐扣乐扣交流交流框架梁极乐空间垃圾筐拉进来");
    // video.put("picture",
    // "http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png");
    // video.put("livesdkaddress",
    // "http://cdn.goluk.cn/files/cdccover/20150706/1436142110232.png");
    //
    // JSONObject user = new JSONObject();
    // user.put("uid", "32323");
    // user.put("nickname", "为什么不");
    // user.put("headportrait", "2");
    // user.put("sex", "1");
    //
    // JSONObject videodata = new JSONObject();
    // videodata.put("video", video);
    // videodata.put("user", user);
    //
    // JSONArray videos = new JSONArray();
    // videos.put(videodata);
    //
    // data.put("videolist", videos);
    //
    // JSONObject commentdata = new JSONObject();
    // commentdata.put("commentid", "2312");
    // commentdata.put("authorid", "34233");
    // commentdata.put("name", "大狗");
    // commentdata.put("avatar", "2");
    // commentdata.put("time", "2015/02/22");
    // commentdata.put("text", "来健身卡来对付框架思路东风路斯蒂芬简历上");
    //
    // JSONObject commentdata2 = new JSONObject();
    // commentdata2.put("commentid", "2312");
    // commentdata2.put("authorid", "34233");
    // commentdata2.put("name", "二狗");
    // commentdata2.put("avatar", "2");
    // commentdata2.put("time", "2015/02/22");
    // commentdata2.put("text", "离开家你弄死的放上来的咖啡机三闾大夫接口六角恐龙接口链接冷静冷静记录框架梁");
    //
    // JSONArray comments = new JSONArray();
    // comments.put(commentdata);
    // comments.put(commentdata2);
    //
    // JSONObject comment = new JSONObject();
    // comment.put("iscomment", "1");
    // comment.put("comcount", "2");
    // comment.put("iscomment", "1");
    // comment.put("comlist", comments);
    //
    // data.put("comment", comment);
    //
    // JSONObject head = new JSONObject();
    // head.put("showhead", "1");
    // head.put("headimg",
    // "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
    // head.put("headvideoimg",
    // "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
    // head.put("headvideo",
    // "http://cdn.goluk.cn/files/cdccover/20150706/1436143729381.png");
    // head.put("ztIntroduction", "六角恐龙极乐空间六角恐龙极乐空间");
    // head.put("outurl", "www.baidu.com");
    // head.put("outurlname", "百度");
    // head.put("ztitle", "测试title");
    //
    // data.put("head", head);
    //
    // jx.put("data", data);
    // // {“result”:”0”,“head”:{},“videolist”:[],”commentlist”:{}}
    // return jx.toString();
    //
    // }

}
