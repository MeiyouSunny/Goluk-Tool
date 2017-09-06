package com.mobnote.golukmain.xdpush;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventFollowPush;
import com.mobnote.golukmain.MainActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.UserOpenUrlActivity;
import com.mobnote.golukmain.carrecorder.CarRecorderActivity;
import com.mobnote.golukmain.cluster.ClusterActivity;
import com.mobnote.golukmain.http.HttpManager;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.livevideo.LiveActivity;
import com.mobnote.golukmain.msg.MsgCenterCommentActivity;
import com.mobnote.golukmain.msg.MsgCenterPraiseActivity;
import com.mobnote.golukmain.msg.SystemMsgActivity;
import com.mobnote.golukmain.profit.MyProfitActivity;
import com.mobnote.golukmain.special.SpecialListActivity;
import com.mobnote.golukmain.videodetail.VideoDetailActivity;
import com.mobnote.golukmobile.GuideActivity;
import com.mobnote.util.GolukConfig;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.JsonUtil;
import com.mobnote.util.ZhugeUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

public class GolukNotification {
    /**
     * 推送标识，主要是主界面接受，用于区分是否是推送数据
     */
    public static final String NOTIFICATION_KEY_FROM = "from";
    /**
     * 用户点击状态栏时发出的广播
     */
    public static final String NOTIFICATION_BROADCAST = "cn.com.goluk.broadcast.clicknotification";
    /**
     * 用户点击状态栏，带的数据传输给主页
     */
    public static final String NOTIFICATION_KEY_JSON = "json";

    /**
     * 这个属性决定 ，用户点击通知以后，是启动Activity 还是发广播 , true为发广播 ，false为启动Activity
     */
    private static final boolean isBroadCast = true;

    private static GolukNotification mInstance = new GolukNotification();
    /**
     * 主要封装对信鸽的操作
     */
    private XGInit xgInit = null;
    /**
     * 是否可以弹框(在一分钟只弹一次框)
     */
    private boolean isCanShowDialog = true;
    /**
     * 显示程序内推送对话框
     */
    private AlertDialog mPushShowDialog = null;
    /**
     * timer用于计时一分钟
     */
    private Timer mTimer = null;
    /**
     * Timer计时时长
     */
    private final int TIMER_OUT = 60 * 1000;
    private final static String TAG = "GolukNotification";

    private final static String UNDEFINED = "0";
    private final static String VIDEO_DETAIL = "1";
    private final static String SPECIAL_LIST = "2";
    private final static String LIVE_VIDEO = "3";
    private final static String ACTIVITY_TOGETHER = "4";
    private final static String H5_PAGE = "5";
    private final static String SPECIAL_SOLO = "6";
    private final static String HOME_PAGE = "9";
    private final static String TAG_PAGE = "10";
    private final static String WEB_DIRECT = "/navidog4MeetTrans/redirect.htm";

    public void createXG() {
        xgInit = new XGInit();
        xgInit.init();
    }

    public XGInit getXg() {
        return xgInit;
    }

    public static synchronized GolukNotification getInstance() {
        return mInstance;
    }

    /**
     * 显示程序外通知
     *
     * @param msgBean 推送解析后的数据
     * @param json    推送原始数据
     * @author jyf
     */
    public void showNotify(Context context, XingGeMsgBean msgBean, String json) {
        NotificationManager mNotiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotiManager.notify(msgBean.notifyId, createNotification(context, json, msgBean.title, msgBean.msg));
    }

    /**
     * 创建一个通知显示体
     *
     * @author jyf
     */
    @SuppressWarnings("deprecation")
    private Notification createNotification_New(Context startActivity, String json, String title, String content) {
        Bitmap bitmap = BitmapFactory.decodeResource(startActivity.getResources(), R.drawable.ic_launcher);

        Notification.Builder builder = null;
        GolukDebugUtils.e("", "init sdk  :" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 23) {
            builder = new Notification.Builder(startActivity).setContentTitle(title).setContentText(content)
                    .setContentIntent(getPendingIntent(startActivity, json));
        } else {
            builder = new Notification.Builder(startActivity).setContentTitle(title).setLargeIcon(bitmap)
                    .setContentText(content).setContentIntent(getPendingIntent(startActivity, json));
        }

        Notification notification = builder.getNotification();
        setNoticationParam(notification);
        return notification;
    }

    private PendingIntent getPendingIntent(Context startActivity, String json) {
        PendingIntent pendingIntent = null;
        if (isBroadCast) {
            Intent intent = getBroadCastIntent(json);
            // 注意最后一个参数必须 写 PendingIntent.FLAG_UPDATE_CURRENT,
            // 否则下个Activity无法接受到消息
            pendingIntent = PendingIntent.getBroadcast(startActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            // 注意最后一个参数必须 写 PendingIntent.FLAG_UPDATE_CURRENT,
            // 否则下个Activity无法接受到消息
            Intent intent = getWillStartIntent(startActivity);
            pendingIntent = PendingIntent.getActivity(startActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntent;
    }

    /**
     * 创建一个通知显示体
     *
     * @author jyf
     */
    private Notification createNotification(Context startActivity, String json, String title, String content) {
//        Notification noti = new Notification();
//        setNoticationParam(noti);
        PendingIntent contentIntent = getPendingIntent(startActivity, json);
        //noti.setLatestEventInfo(startActivity, title, content, contentIntent);

        Notification.Builder builder = new Notification.Builder(startActivity);
        builder.setContentTitle(title);//设置标题
        builder.setContentText(content);//设置内容
        builder.setContentIntent(contentIntent);//执行intent
        builder.setSmallIcon(R.drawable.ic_launcher);
        Notification notification = builder.getNotification();//将builder对象转换为普通的notification
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager manager = (NotificationManager) startActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1,notification);
        return notification;
    }

    /**
     * 设置显示状态栏通知时，配置的基本参数，比如声音等
     *
     * @author jyf
     */
    private void setNoticationParam(Notification noti) {
        if (null == noti) {
            return;
        }
        final int icon = R.drawable.ic_launcher;
        // 立即通知
        final long when = System.currentTimeMillis();
        // 通知显示图标
        noti.icon = icon;
        // 通知什么时候显示，使用当前时间 ，就是立即显示
        noti.when = when;
        if (!isNight()) {
            // 通知显示默认声音
            noti.defaults |= Notification.DEFAULT_SOUND;
        }
        // 通知可以被 状态栏的清除按钮给清除掉, 用户点击也会清除掉
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        // 让声音无限循环，直到用户响应
        // noti.flags = Notification.FLAG_INSISTENT;
    }

    /**
     * 如果是夜间，则推送来了，不响声音
     *
     * @return true/false 夜/白
     * @author jyf
     */
    private boolean isNight() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour >= 8 && hour < 22) {
            // 白天
            return false;
        }
        return true;
    }

    /**
     * 配置推送点击时发送的广播
     *
     * @return
     * @author jyf
     */
    private Intent getBroadCastIntent(String json) {
        Intent intent = new Intent(NOTIFICATION_BROADCAST);
        intent.putExtra(NOTIFICATION_KEY_FROM, "notication");
        intent.putExtra(NOTIFICATION_KEY_JSON, json);
        return intent;
    }

    /**
     * 配置，点击下拉通知时，启动的Activity
     *
     * @param startActivity
     * @return
     * @author jyf
     */
    private Intent getWillStartIntent(Context startActivity) {
        Intent intent = new Intent(startActivity, GuideActivity.class);
        // 设置启动模式,
        // 保证在程序启动的时候，把程序调入前台，并执行当前界面的onNewIntent()方法
        // 程序未启动，则把程序启动起来
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        // 添加以下action，也可以使被启动的Activity接受到消息，不知道为什么
        // intent.setAction(String.valueOf(System.currentTimeMillis()));
        intent.putExtra(NOTIFICATION_KEY_FROM, "notication");

        return intent;
    }

    /**
     * 程序退出时调用
     *
     * @author jyf
     */
    public void destroy() {
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 启动1分钟不弹框的计时
     *
     * @author jyf
     */
    private void startTimer() {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                isCanShowDialog = true;
            }
        }, TIMER_OUT);
    }

    /**
     * 销毁推送对话框
     *
     * @author jyf
     */
    public void dimissPushDialog() {
        if (null != mPushShowDialog) {
            mPushShowDialog.dismiss();
            mPushShowDialog = null;
        }
    }

    /**
     * 判断接收到的推送消息是否可以显示
     *
     * @return true/false 可以显示/不显示
     * @author jyf
     */
    private boolean isCanShowPushDialog() {
        if (!isCanShowDialog) {
            return false;
        }
        Context context = GolukApplication.getInstance().getContext();
        if (context instanceof LiveActivity || context instanceof CarRecorderActivity) {
            return false;
        }

        return true;
    }

    /**
     * 程序内推送,显示对话框
     *
     * @author jyf
     */
    public void showAppInnerPush(final Context cnt, final XingGeMsgBean msgBean) {
        if (null == msgBean || !isCanShowPushDialog()) {
            return;
        }

        isCanShowDialog = false;
        startTimer();
        dimissPushDialog();
        Context context = GolukApplication.getInstance().getContext();
        mPushShowDialog = new AlertDialog.Builder(context).create();
        mPushShowDialog.setTitle(msgBean.title);
        mPushShowDialog.setMessage(msgBean.msg);
        mPushShowDialog.setCancelable(true);
        mPushShowDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.user_cancle),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (null != mPushShowDialog) {
                            mPushShowDialog.dismiss();
                        }

                    }
                });

        mPushShowDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.user_personal_sign_title), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dealAppinnerClick(cnt, msgBean);
                        if (null != mPushShowDialog) {
                            mPushShowDialog.dismiss();
                        }
                    }
                });
        mPushShowDialog.show();
    }

    /**
     * 处理程序内推送点击，程序内Dialog弹出后，点击“确定”的处理
     *
     * @param msgBean 消息体
     * @author jyf
     */
    public void dealAppinnerClick(Context cnt, XingGeMsgBean msgBean) {
        if (null == msgBean) {
            return;
        }
        try {
            if ("0".equals(msgBean.target)) {
                // 不处理
            } else if ("1".equals(msgBean.target)) {
                // 启动程序
            } else if ("2".equals(msgBean.target)) {
                // 启动程序功能界面
                pushStartFuntion(msgBean);
            } else if ("3".equals(msgBean.target)) {
                // 打开Web页
                if (null != msgBean.weburl && !"".equals(msgBean.weburl)) {
                    Intent intent = new Intent(GolukApplication.getInstance().getContext(), UserOpenUrlActivity.class);
                    intent.putExtra("url", msgBean.weburl);
                    GolukApplication.getInstance().getContext().startActivity(intent);
                }
            }
        } catch (Exception e) {
        }
    }

    private void pushStartFuntion(XingGeMsgBean msgBean) {
        try {
            // if (ICommentFn.COMMENT_TYPE_VIDEO.equals(msgBean.tarkey)) {
            // // 启动视频详情界面
            // String[] vidArray = JsonUtil.parseVideoDetailId(msgBean.params);
            // if (null != vidArray && vidArray.length > 0) {
            // startDetail(vidArray[0]);
            // }
            // } else if
            // (ICommentFn.COMMENT_TYPE_WONDERFUL_SPECIAL.equals(msgBean.tarkey))
            // {
            // // 精选专题
            // String[] vidArray = JsonUtil.parseVideoDetailId(msgBean.params);
            // if (null != vidArray && vidArray.length > 0) {
            // startSpecial(vidArray[0], msgBean.msg);
            // }
            // } else if
            // (ICommentFn.COMMENT_TYPE_CLUSTER.equals(msgBean.tarkey)) {
            // // 活动聚合
            // String[] vidArray = JsonUtil.parseVideoDetailId(msgBean.params);
            // if (null != vidArray && vidArray.length > 0) {
            // startCluster(vidArray[0], msgBean.msg);
            // }
            //
            // } else if
            // (ICommentFn.COMMENT_TYPE_WINNING.equals(msgBean.tarkey)) {
            // // 发奖跳转页
            // String[] vidArray = JsonUtil.parseVideoDetailId(msgBean.params);
            // // if (null != vidArray && vidArray.length > 0) {
            // // startCluster(vidArray[0], msgBean.msg);
            // // }
            //
            // startWinning(vidArray[0]);
            //
            // } else if
            // (ICommentFn.COMMENT_TYPE_WONDERFUL_VIDEO.equals(msgBean.tarkey))
            // {
            // String[] vidArray = JsonUtil.parseVideoDetailId(msgBean.params);
            // if (null != vidArray && vidArray.length > 0) {
            // startDetail(vidArray[0]);
            // }
            //
            // }

            int type = 0;
            JSONArray array = new JSONArray(msgBean.params);

            int size = array.length();
            if (size > 0) {
                JSONObject obj = array.getJSONObject(0);
                type = JsonUtil.getJsonIntValue(obj, "t", 0);

                // 101 = comment
                // 102 = like/praise
                // 200~300 = system
                // 300~400 = official notification
                if (0 == type) {
                    // do nothing
                } else if (101 == type) {
                    // start comment activity
                    Context context = GolukApplication.getInstance().getContext();
                    Intent intent = new Intent(context, MsgCenterCommentActivity.class);
                    context.startActivity(intent);
                } else if (102 == type) {
                    Context context = GolukApplication.getInstance().getContext();
                    Intent intent = new Intent(context, MsgCenterPraiseActivity.class);
                    context.startActivity(intent);
                } else if (103 == type) {
                    EventBus.getDefault().post(new EventFollowPush(EventConfig.FOLLOW_PUSH));
                } else if (209 == type) {
                    Context context = GolukApplication.getInstance().getContext();
                    String vid = obj.getString("vid");
                    GolukUtils.startPublishOrWatchLiveActivity(context, false, false, vid, null, null);
                    return;
                } else if (type >= 200 && type < 300) {
                    Context context = GolukApplication.getInstance().getContext();
                    Intent intent = new Intent(context, SystemMsgActivity.class);
                    context.startActivity(intent);
                } else if (type >= 300 && type < 400) {
                    // Context context =
                    // GolukApplication.getInstance().getContext();
                    // Intent intent = new Intent(context,
                    // OfficialMessageActivity.class);
                    // context.startActivity(intent);

                    if (TextUtils.isEmpty(msgBean.tarkey)) {
                        return;
                    }

                    if (!"2".equals(msgBean.target)) {
                        return;
                    }

                    if (null == msgBean.params) {
                        return;
                    }

                    String[] vidArray = JsonUtil.parseVideoDetailId(msgBean.params);
                    if (null == vidArray || vidArray.length <= 0) {
                        return;
                    }

                    Intent intent = null;

                    if (UNDEFINED.equals(msgBean.tarkey)) {
                        // target key undefined
                        Log.d(TAG, "target key undefined");
                    } else if (VIDEO_DETAIL.equals(msgBean.tarkey)) {
                        // launch video detail
                        Context context = GolukApplication.getInstance().getContext();
                        //视频详情页访问
                        ZhugeUtils.eventVideoDetail(context, context.getString(R.string.str_zhuge_share_video_network_other));

                        intent = new Intent(context, VideoDetailActivity.class);
                        intent.putExtra(VideoDetailActivity.VIDEO_ID, vidArray[0]);
                        intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, true);
                        context.startActivity(intent);
                    } else if (SPECIAL_LIST.equals(msgBean.tarkey)) {
                        // launch special list
                        Context context = GolukApplication.getInstance().getContext();
                        intent = new Intent(context, SpecialListActivity.class);
                        intent.putExtra("ztid", vidArray[0]);
                        if (!TextUtils.isEmpty(msgBean.title)) {
                            intent.putExtra("title", msgBean.title);
                        }
                        context.startActivity(intent);
                    } else if (LIVE_VIDEO.equals(msgBean.tarkey)) {
                        // TODO: This should proceed in future
                        // intent = new Intent(mContext, LiveActivity.class);
                        // intent.putExtra(LiveActivity.KEY_IS_LIVE, false);
                        // intent.putExtra(LiveActivity.KEY_GROUPID, "");
                        // intent.putExtra(LiveActivity.KEY_PLAY_URL, "");
                        // intent.putExtra(LiveActivity.KEY_JOIN_GROUP, "");
                        // intent.putExtra(LiveActivity.KEY_USERINFO, user);
                        // mContext.startActivity(intent);
                    } else if (ACTIVITY_TOGETHER.equals(msgBean.tarkey)) {
                        // launch topic
                        Context context = GolukApplication.getInstance().getContext();
                        intent = new Intent(context, ClusterActivity.class);
                        intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID, vidArray[0]);
                        // intent.putExtra(ClusterActivity.CLUSTER_KEY_UID, "");
                        String topName = "#" + msgBean.title;
                        intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, topName);
                        context.startActivity(intent);
                    } else if (H5_PAGE.equals(msgBean.tarkey)) {
                        // launch h5 page
                        Context context = GolukApplication.getInstance().getContext();
                        intent = new Intent(context, UserOpenUrlActivity.class);
                        if (null != vidArray[0] && !TextUtils.isEmpty(vidArray[0])) {
                            String url = HttpManager.getInstance().getWebDirectHost() + WEB_DIRECT + "?type=5&access="
                                    + vidArray[0];
                            intent.putExtra(GolukConfig.H5_URL, url);
                            if (!TextUtils.isEmpty(msgBean.title)) {
                                intent.putExtra(GolukConfig.NEED_H5_TITLE, msgBean.title);
                            }
                            intent.putExtra(GolukConfig.WEB_TYPE, GolukConfig.NEED_SHARE);
                            intent.putExtra(GolukConfig.URL_OPEN_PATH, "push_start");
                            context.startActivity(intent);
                        }
                    } else if (SPECIAL_SOLO.equals(msgBean.tarkey)) {
                        Context context = GolukApplication.getInstance().getContext();
                        //视频详情页访问
                        ZhugeUtils.eventVideoDetail(context, context.getString(R.string.str_zhuge_share_video_network_other));

                        intent = new Intent(context, VideoDetailActivity.class);
                        intent.putExtra(VideoDetailActivity.VIDEO_ID, vidArray[0]);
                        intent.putExtra(VideoDetailActivity.VIDEO_ISCAN_COMMENT, true);
                        context.startActivity(intent);
                    } else if (HOME_PAGE.equals(msgBean.tarkey)) {
                        Context context = GolukApplication.getInstance().getContext();
                        GolukUtils.startUserCenterActivity(context, vidArray[0]);
                    } else if (TAG_PAGE.equals(msgBean.tarkey)) {
                        // launch topic
                        Context context = GolukApplication.getInstance().getContext();
                        intent = new Intent(context, ClusterActivity.class);
                        intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID, vidArray[0]);
                        // intent.putExtra(ClusterActivity.CLUSTER_KEY_UID, "");
                        String topName = "#" + msgBean.title;
                        intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, topName);
                        context.startActivity(intent);
                    }
                } else {
                    // do nothing
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkoutLoginState(String uid) {
        if (GolukApplication.getInstance().isUserLoginSucess) {
            UserInfo userInfo = GolukApplication.getInstance().getMyInfo();
            if (null == userInfo || null == userInfo.uid || !userInfo.uid.equals(uid)) {
                return false;
            }
            return true;
        }
        GolukUtils.showToast(GolukApplication.getInstance().getContext(), GolukApplication.getInstance().getContext()
                .getString(R.string.str_msg_push_login));
        return false;
    }

    private void startWinning(String uid) {
        if (null == uid || "".equals(uid)) {
            return;
        }

        if (!checkoutLoginState(uid)) {
            return;
        }

        Context context = GolukApplication.getInstance().getContext();
        Intent intent = new Intent(context, MyProfitActivity.class);
        // intent.putExtra("uid", uid);
        context.startActivity(intent);
    }

    private void statrtWonderfulVideo(String ztid, String title) {
        if (null == ztid || "".equals(ztid)) {
            return;
        }
        Context context = GolukApplication.getInstance().getContext();
        //视频详情页访问
        ZhugeUtils.eventVideoDetail(context, context.getString(R.string.str_zhuge_share_video_network_other));

        Intent intent = new Intent(context, VideoDetailActivity.class);
        intent.putExtra(VideoDetailActivity.TYPE, "Wonderful");
        intent.putExtra("ztid", ztid);
        intent.putExtra("title", "");
        context.startActivity(intent);
    }

    /**
     * 推送启动聚合活动
     *
     * @param cid   聚合id
     * @param title 聚合title
     * @author jyf
     */
    private void startCluster(String cid, String title) {
        if (null == cid || "".equals(cid)) {
            return;
        }
        Context context = GolukApplication.getInstance().getContext();
        Intent intent = new Intent(context, ClusterActivity.class);
        intent.putExtra(ClusterActivity.CLUSTER_KEY_ACTIVITYID, cid);
        intent.putExtra(ClusterActivity.CLUSTER_KEY_TITLE, "");
        context.startActivity(intent);
    }

    /**
     * 推送启动专题
     *
     * @param sid   专题id
     * @param title 专题title
     * @author jyf
     */
    private void startSpecial(String sid, String title) {
        if (null == sid || "".equals(sid)) {
            return;
        }
        Context context = GolukApplication.getInstance().getContext();
        Intent intent = new Intent(context, SpecialListActivity.class);
        intent.putExtra("ztid", sid);
        intent.putExtra("title", "");
        context.startActivity(intent);
    }

    /**
     * 启动视频详情界面
     *
     * @param vid 视频Id
     * @author jyf
     */
    public void startDetail(String vid) {
        Context context = GolukApplication.getInstance().getContext();
        //视频详情页访问
        ZhugeUtils.eventVideoDetail(context, context.getString(R.string.str_zhuge_share_video_network_other));

        Intent intent = new Intent(context, VideoDetailActivity.class);
        intent.putExtra(VideoDetailActivity.VIDEO_ID, vid);
        context.startActivity(intent);
    }

    /**
     * 清除某个通知
     *
     * @param activity
     * @param id
     * @author jyf
     */
    public void clearNotication(Activity activity, int id) {
        NotificationManager mNotiManager = (NotificationManager) activity
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotiManager.cancel(id);
    }

    /**
     * 用户点击状态栏上的消息处理,跳转到主界面
     *
     * @param intent 包含用户点击的数据
     * @author jyf
     */
    public void startMain(Intent intent) {
        Context currentContext = GolukApplication.getInstance().getContext();
        if (null != currentContext) {
            Intent startIn = new Intent(currentContext, MainActivity.class);
            if (null != intent) {
                String from = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_FROM);
                String json = intent.getStringExtra(GolukNotification.NOTIFICATION_KEY_JSON);
                startIn.putExtra(GolukNotification.NOTIFICATION_KEY_FROM, from);
                startIn.putExtra(GolukNotification.NOTIFICATION_KEY_JSON, json);
            }
            currentContext.startActivity(startIn);
        }
    }

    /**
     * Clear all notifications
     */
    public void clearAllNotification(Activity activity) {
        NotificationManager mNotiManager = (NotificationManager) activity
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotiManager.cancelAll();
    }

}
