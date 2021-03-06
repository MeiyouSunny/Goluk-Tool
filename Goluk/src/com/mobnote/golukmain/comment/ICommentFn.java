package com.mobnote.golukmain.comment;

public interface ICommentFn {
	/** 视频、专题或直播的id */
	public static final String COMMENT_KEY_MID = "comment_key_mid";
	/** 评论主题类型 (1:单视频；2:专题；3:直播；4:其它) */
	public static final String COMMENT_KEY_TYPE = "comment_key_type";
	/** 是否弹出键盘, true/false 弹出/不弹出 */
	public static final String COMMENT_KEY_SHOWSOFT = "comment_key_showsoft";
	/** 是否允许评论 */
	public static final String COMMENT_KEY_ISCAN_INPUT = "comment_key_iscan_input";
	/** 视频发起者的uid */
	public static final String COMMENT_KEY_USERID = "comment_key_userid";

	/** 一页请求多少条数据 */
	public static final int PAGE_SIZE = 20;
	/** 首次进入 */
	public static final int OPERATOR_FIRST = 0;
	/** 上拉 */
	public static final int OPERATOR_UP = 1;
	/** 下拉 */
	public static final int OPERATOR_DOWN = 2;
	
	/** 空闲*/
	public static final int OPERATOR_NONE = 4;

	/** 评论超时为10 秒 */
	public static final int COMMENT_CIMMIT_TIMEOUT = 10;

	/** 单视频 */
	public static final String COMMENT_TYPE_VIDEO = "1";
	/** 精选专题 */
	public static final String COMMENT_TYPE_WONDERFUL_SPECIAL = "2";
	/** 直播 */
	public static final String COMMENT_TYPE_LIVE = "3";
	/** 活动聚合 */
	public static final String COMMENT_TYPE_CLUSTER = "4";
	/** 获奖 */
	public static final String COMMENT_TYPE_WINNING = "5";
	/** 精选单视频 */
	public static final String COMMENT_TYPE_WONDERFUL_VIDEO = "6";

}
