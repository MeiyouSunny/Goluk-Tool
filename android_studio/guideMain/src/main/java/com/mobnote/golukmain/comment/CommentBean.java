package com.mobnote.golukmain.comment;

public class CommentBean {
	/** 评论id */
	public String mCommentId;
	/** 评论内容 */
	public String mCommentTxt;
	/** 评论时间 */
	public String mCommentTime;
	/** 标准时区时间戳 */
	public long mCommentTs;

	/** 评论人uid */
	public String mUserId;
	/** 评论人名字 */
	public String mUserName;
	/** 评论人头像 */
	public String mUserHead;
	/** 用户头像网络地址 */
	public String customavatar;

	/** 回复人id **/
	public String mReplyId;
	/** 回复人name **/
	public String mReplyName;
	
	/**正常评论、评论超时、评论重复**/
	public String result;
	
	/**评论楼层**/
	public String mSeq;
	/**认证描述**/
	public String mApprove;
	/**认证标识**/
	public String mApprovelabel;
	/**达人标识**/
	public String mTarento;
	/**加v标识**/
	public String mHeadplusv;
	/**加v描述**/
	public String mHeadplusvdes;

}
