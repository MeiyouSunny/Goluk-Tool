package com.mobnote.golukmain.videosuqare;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.http.HttpCommHeaderBean;
import com.mobnote.util.GolukFastJsonUtil;
import com.mobnote.util.GolukUtils;

import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 视频广场接口管理类
 * <p/>
 * 2015年4月17日
 *
 * @author xuhw
 */
public class VideoSquareManager implements VideoSuqareManagerFn {
    /**
     * Application实例,用于调用JNI的对象
     */
    private GolukApplication mApplication = null;
    /**
     * IPC回调监听列表
     */
    private ConcurrentHashMap<String, VideoSuqareManagerFn> mVideoSquareManagerListener = null;

    public VideoSquareManager(GolukApplication application) {
        mApplication = application;
        mVideoSquareManagerListener = new ConcurrentHashMap<String, VideoSuqareManagerFn>();
        mApplication.mGoluk.GolukLogicRegisterNotify(GolukModule.Goluk_Module_Square, this);
    }

    /**
     * 获取广场列表数据
     *
     * @param channel   分享渠道：1.视频广场 2.微信 3.微博 4.QQ
     * @param type      视频类型：0.全部 1.直播 2.点播
     * @param attribute 属性标签：[“1”,”2”,”3”] 0.全部 1.碰瓷达人 2.奇葩演技 3.路上风景 4.随手拍 5.事故大爆料
     *                  6.堵车预警 7.惊险十分 8.疯狂超车 9.感人瞬间 10.传递正能量
     * @param operation 操作：0.首次进入30条 1.下拉30条 2.上拉30条
     * @param timestamp 时间戳：首次进入为空
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年4月17日
     */
    public boolean getSquareList(String channel, String type, String attribute, String operation, String timestamp) {
        List<String> arr = new ArrayList<String>();
        arr.add(attribute);
        String json = JsonCreateUtils.getSquareListRequestJson(channel, type, arr, operation, timestamp);

        GolukDebugUtils.e("", "jyf----CategoryListView------------------getSquareList  json: " + json);

        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
                VSquare_Req_List_Video_Catlog, json);
    }

    /**
     * 获取精选列表数据
     *
     * @param jxid     　精选id首次进入为0
     * @param pagesize 　默认四组
     * @return
     * @author xuhw
     * @date 2015年8月6日
     */
    public long getJXListData(String jxid, String pagesize) {
        String json = JsonCreateUtils.getJXListJson(jxid, pagesize);
        if (null == json) {
            return -1;
        }

        if (null != mApplication && null != mApplication.mGoluk) {
            return mApplication.mGoluk.CommRequestEx(GolukModule.Goluk_Module_Square, VSquare_Req_List_HandPick, json);
        }
        return -1;
    }

    /**
     * 获取专题列表数据
     *
     * @param ztid 　专题id
     * @return
     * @author xuhw
     * @date 2015年8月6日
     */
    public boolean getZTListData(String ztid) {
        String json = JsonCreateUtils.getZTJson(ztid);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
                VSquare_Req_List_Topic_Content, json);
    }

    /**
     * 获取聚合内容数据
     *
     * @param ztid      　专题id
     * @param operation 　0.首次进入　1.下拉　2.上拉
     * @param timestamp 　时间戳：首次进入为空
     * @param pagesize  　默认20个视频
     * @return
     * @author xuhw
     * @date 2015年8月6日
     */
    public boolean getJHListData(String ztid, String operation, String timestamp, String pagesize) {
        String json = JsonCreateUtils.getJHJson(ztid, operation, timestamp, pagesize);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_List_Tag_Content,
                json);
    }

    public boolean getUserInfo(String otheruid) {
        String json = JsonCreateUtils.getUserInfoJson(otheruid);
        GolukDebugUtils.e("", "=======getUserInfo==" + json);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
                VSquare_Req_MainPage_UserInfor, json);
    }

    /**
     * 获取视频分类
     *
     * @return
     * @author xuhw
     * @date 2015年8月6日
     */
    public long getZXListData() {
        HttpCommHeaderBean bean = new HttpCommHeaderBean();
        String str = GolukFastJsonUtil.setParseObj(bean);
        mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_HttpPage, IPageNotifyFn.PageType_AddCommHeader, str);
        return mApplication.mGoluk.CommRequestEx(GolukModule.Goluk_Module_Square, VSquare_Req_List_Catlog, "");
    }

    /**
     * 按类别获取视频列表（可用于更新）
     *
     * @param channel   分享渠道：1.视频广场 2.微信 3.微博 4.QQ
     * @param type      视频类型：0.全部 1.直播 2.点播
     * @param attribute 属性标签：[“1”,”2”,”3”] 0.全部 1.碰瓷达人 2.奇葩演技 3.路上风景 4.随手拍 5.事故大爆料
     *                  6.堵车预警 7.惊险十分 8.疯狂超车 9.感人瞬间 10.传递正能量
     * @param operation 操作：0.首次进入30条 1.下拉30条 2.上拉30条
     * @param timestamp 时间戳：首次进入为空
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年4月17日
     */
    public long getTypeVideoList(String channel, String type, List<String> attribute, String operation, String timestamp) {
        String json = JsonCreateUtils.getSquareListRequestJson(channel, type, attribute, operation, timestamp);
        return mApplication.mGoluk.CommRequestEx(GolukModule.Goluk_Module_Square, VSquare_Req_List_Video_Catlog, json);
    }

    /**
     * 点击次数上报
     *
     * @param channel   分享渠道：1.视频广场 2.微信 3.微博 4.QQ
     * @param mDataList 视频列表数据
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年4月17日
     */
    public boolean clickNumberUpload(String channel, List<VideoSquareInfo> mDataList) {
        String json = JsonCreateUtils.getClickVideoUploadRequestJson(channel, mDataList);
        GolukDebugUtils.e("", "VideoSuqare_CallBack=@@@@===json=" + json);
        if (null != mApplication && null != mApplication.mGoluk) {
            return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_VOP_ClickUp,
                    json);
        }
        return false;
    }

    /**
     * 举报
     *
     * @param channel    分享渠道：1.视频广场 2.微信 3.微博 4.QQ
     * @param videoid    视频id
     * @param reporttype 举报类型：1.色情低俗 2.谣言惑众 3.政治敏感 4.其他原因
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年4月17日
     */
    public boolean report(String channel, String videoid, String reporttype) {
        String json = JsonCreateUtils.getReportRequestJson(channel, videoid, reporttype);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_VOP_ReportUp,
                json);
    }

    /**
     * 推荐视频
     *
     * @param channel 分享渠道：1.视频广场 2.微信 3.微博 4.QQ
     * @param videoid 视频id
     * @param reason  推荐理由
     * @return
     * @author xuhw
     * @date 2015年8月6日
     */
    public boolean recomVideo(String channel, String videoid, String reason) {
        String json = JsonCreateUtils.getRecomJson(channel, videoid, reason);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_VOP_RecomVideo,
                json);
    }

    /**
     * 分享请求
     *
     * @param channel 分享渠道：1.视频广场 2.微信 3.微博 4.QQ
     * @param videoid 视频id
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年4月17日
     */
    public boolean shareVideoUp(String channel, String videoid) {
        String json = JsonCreateUtils.getShareVideoUpRequestJson(channel, videoid);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square, VSquare_Req_VOP_ShareVideo,
                json);
    }

    /**
     * 获取分享地址
     *
     * @param videoid 视频id
     * @param type    视频类型：1.直播 2.点播
     * @return true:命令发送成功 false:失败
     * @author xuhw
     * @date 2015年4月17日
     */
    public boolean getShareUrl(String videoid, String type) {
        String json = JsonCreateUtils.getShareUrlRequestJson(videoid, type);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
                VSquare_Req_VOP_GetShareURL_Video, json);
    }

    /**
     * 获取分享地址(专题和聚合)
     *
     * @param ztype 专题类型 1:专题 2：tag
     * @param ztid  专题id
     * @return
     * @author xuhw
     * @date 2015年8月6日
     */
    public boolean getTagShareUrl(String ztype, String ztid) {
        String json = JsonCreateUtils.getTagJson(ztype, ztid);
        return mApplication.mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
                VSquare_Req_VOP_GetShareURL_Topic_Tag, json);
    }

    /**
     * 获取精选本地缓存
     *
     * @return
     * @author xuhw
     * @date 2015年8月6日
     */
    public String getJXList() {
        return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square,
                VSquare_Req_List_HandPick_LocalCache, "");
    }

    /**
     * 获取最新视频分类缓存（不包含直播信息）
     *
     * @return
     * @author xuhw
     * @date 2015年8月6日
     */
    public String getZXList() {
        return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square,
                VSquare_Req_List_Catlog_LocalCache, "");
    }

    // 获取分类列表本地缓存(比如　曝光台，事故　，随手拍)
    public String getCategoryLocalCacheData(String json) {
        return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square,
                VSquare_Req_List_Video_Catlog_LocalCache, json);
    }

    /**
     * 同步获取视频列表本地缓存
     *
     * @param attribute 属性标签： 0.全部 1.碰瓷达人 2.奇葩演技 3.路上风景 4.随手拍 5.事故大爆料 6.堵车预警 7.惊险十分
     *                  8.疯狂超车 9.感人瞬间 10.传递正能量
     * @return
     * @author xuhw
     * @date 2015年4月27日
     */
    public String getTypeVideoList(String attribute) {
        JSONObject json = new JSONObject();
        try {
            json.put("attribute", attribute);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mApplication.mGoluk.GolukLogicCommGet(GolukModule.Goluk_Module_Square,
                VSquare_Req_List_Video_Catlog_LocalCache, json.toString());
    }

    /**
     * 添加视频广场监听
     *
     * @param from
     * @param fn
     * @author xuhw
     * @date 2015年4月14日
     */
    public void addVideoSquareManagerListener(String from, VideoSuqareManagerFn fn) {
        this.mVideoSquareManagerListener.put(from, fn);
    }

    public boolean checkVideoSquareManagerListener(String from) {
        return this.mVideoSquareManagerListener.containsKey(from);
    }

    /**
     * 删除视频广场监听
     *
     * @param from
     * @author xuhw
     * @date 2015年4月14日
     */
    public void removeVideoSquareManagerListener(String from) {
        this.mVideoSquareManagerListener.remove(from);
    }

    @Override
    public void VideoSuqare_CallBack(int event, int msg, int param1, Object param2) {
        GolukDebugUtils.e("", "jyf----VideoSquareManager----event:" + event + "  msg:" + msg + "  param1: " + param1
                + "  param2:" + param2);
        Iterator<String> iter = mVideoSquareManagerListener.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (null != key) {
                GolukDebugUtils.e("", "jyf----VideoSquareManager----key:" + key);
                VideoSuqareManagerFn fn = mVideoSquareManagerListener.get(key);
                if (null != fn) {
                    fn.VideoSuqare_CallBack(event, msg, param1, param2);
                }
            }
        }
    }
}
