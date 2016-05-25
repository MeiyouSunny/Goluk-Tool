package com.mobnote.eventbus;

/**
 * Created by leege100 on 16/5/22.
 */
public class EventShareCompleted {
    private boolean isSuccess;
    public EventShareCompleted(boolean isSuccess){
        this.isSuccess = isSuccess;
    }
}
