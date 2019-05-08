package com.mobnote.eventbus;

import de.greenrobot.event.EventBus;

public class EventUtil {

    /* 关注状态变化(关注/取消关注) */
    private static final int EVENT_FOLLOW = 1;
    /* 下载完成事件 */
    private static final int EVENT_DOWNLOAD_COMPLETE = 2;
    /* 评论成功事件 */
    private static final int EVENT_COMMENT_SUCCESS = 3;
    /* 定位:不在国内 */
    private static final int EVENT_NOT_IN_CHINA = 4;

    /////////////////////////////////////////////////////////////////////////

    /**
     * 关注状态变化(关注/取消关注)Event
     */
    public static void sendFollowEvent() {
        Event event = Event.create(EVENT_FOLLOW);
        EventBus.getDefault().post(event);
    }

    public static boolean isFollowEvent(Event event) {
        return event != null && event.type == EVENT_FOLLOW;
    }

    /**
     * 下载远程视频完成Event
     */
    public static void sendDownloadCompleteEvent() {
        Event event = Event.create(EVENT_DOWNLOAD_COMPLETE);
        EventBus.getDefault().post(event);
    }

    public static boolean isDownloadCompleteEvent(Event event) {
        return event != null && event.type == EVENT_DOWNLOAD_COMPLETE;
    }

    /**
     * 视频评论成功事件
     */
    public static void sendCommentSuccessEvent() {
        Event event = Event.create(EVENT_COMMENT_SUCCESS);
        EventBus.getDefault().post(event);
    }

    public static boolean isCommentSuccessEvent(Event event) {
        return event != null && event.type == EVENT_COMMENT_SUCCESS;
    }

    public static void sendNotInChinaEvent() {
        Event event = Event.create(EVENT_NOT_IN_CHINA);
        EventBus.getDefault().post(event);
    }

    public static boolean isNotInChinaEvent(Event event) {
        return event != null && event.type == EVENT_NOT_IN_CHINA;
    }

}
