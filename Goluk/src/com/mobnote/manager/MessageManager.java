package com.mobnote.manager;

import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventMessageUpdate;

import de.greenrobot.event.EventBus;

public class MessageManager {
	private int mPraiseCount;
	private int mCommentCount;
	private int mFollowCount;

	private int mSystemMessageCount;

	/** This field remained for future */
	private int mOfficialMessageCount;

	private volatile static MessageManager mInstance;

	private MessageManager() {
	}

	public static MessageManager getMessageManager() {
		if (mInstance == null) {
			synchronized (MessageManager.class) {
				if (mInstance == null) {
					mInstance = new MessageManager();
				}
			}
		}
		return mInstance;
	}

	public int getPraiseCount() {
		return mPraiseCount;
	}

	public synchronized void setPraiseCount(int praiseCount) {
		this.mPraiseCount = praiseCount;
		EventBus.getDefault().post(new EventMessageUpdate(EventConfig.MESSAGE_UPDATE));
	}

	public int getCommentCount() {
		return mCommentCount;
	}

	public synchronized void setCommentCount(int commentCount) {
		this.mCommentCount = commentCount;
		EventBus.getDefault().post(new EventMessageUpdate(EventConfig.MESSAGE_UPDATE));
	}

	public int getSystemMessageCount() {
		return mSystemMessageCount;
	}

	public synchronized void setSystemMessageCount(int systemMessageCount) {
		this.mSystemMessageCount = systemMessageCount;
		EventBus.getDefault().post(new EventMessageUpdate(EventConfig.MESSAGE_UPDATE));
	}

	public int getMessageTotalCount() {
		return mPraiseCount + mCommentCount + mSystemMessageCount;
	}

	public synchronized void setMessageEveryCount(int praiseCount,
			int commentCount,int followCout, int systemMessageCount) {
		this.mSystemMessageCount = systemMessageCount;
		this.mCommentCount = commentCount;
		this.mPraiseCount = praiseCount;
		this.mFollowCount = followCout;
		EventBus.getDefault().post(new EventMessageUpdate(EventConfig.MESSAGE_UPDATE));
	}
	
	public int getFollowCount() {
		return mFollowCount;
	}

	public void setFollowCount(int mFollowCount) {
		this.mFollowCount = mFollowCount;
	}
}
