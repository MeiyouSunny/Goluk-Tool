package cn.com.mobnote.manager;

public class MessageManager {
	int mPraiseCount;
	int mCommentCount;
	int mSystemMessageCount;

	/** This field remained for future */
	int mOfficialMessageCount;

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

	public void setPraiseCount(int praiseCount) {
		this.mPraiseCount = praiseCount;
	}

	public int getCommentCount() {
		return mCommentCount;
	}

	public void setCommentCount(int commentCount) {
		this.mCommentCount = commentCount;
	}

	public int getSystemMessageCount() {
		return mSystemMessageCount;
	}

	public void setSystemMessageCount(int systemMessageCount) {
		this.mSystemMessageCount = systemMessageCount;
	}
}
