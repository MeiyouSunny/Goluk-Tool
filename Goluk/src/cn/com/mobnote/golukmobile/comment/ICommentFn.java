package cn.com.mobnote.golukmobile.comment;

public interface ICommentFn {
	/** 视频、专题或直播的id */
	public static final String COMMENT_KEY_MID = "comment_key_mid";
	/** 评论主题类型 (1:单视频；2:专题；3:直播；4:其它) */
	public static final String COMMENT_KEY_TYPE = "comment_key_type";
	/** 是否弹出键盘, true/false 弹出/不弹出 */
	public static final String COMMENT_KEY_SHOWSOFT = "comment_key_showsoft";
	/** 是否允许评论 */
	public static final String COMMENT_KEY_ISCAN_INPUT = "comment_key_iscan_input";

	/** 一页请求多少条数据 */
	public static final int PAGE_SIZE = 20;
	/** 首次进入 */
	public static final int OPERATOR_FIRST = 0;
	/** 上拉 */
	public static final int OPERATOR_PUSH = 1;
	/** 下拉 */
	public static final int OPERATOR_PULL = 2;
}
