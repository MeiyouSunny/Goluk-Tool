package com.mobnote.eventbus;

import de.greenrobot.event.EventBus;

public class EventUtil {

    /* 关注状态变化(关注/取消关注) */
    private static final int EVENT_FOLLOW = 1;

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

}
