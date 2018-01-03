package com.mobnote.golukmain.comment.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class CommentItemBean {
	/**评论id**/
	@JSONField(name="commentId")
	public String commentId;
	
	/**评论时间**/
	@JSONField(name="time")
	public String time;

	/**标准时区时间戳**/
	@JSONField(name="ts")
	public long ts;
	
	/**评论内容**/
	@JSONField(name="text")
	public String text;

	/**楼层号**/
	@JSONField(name="seq")
	public String seq;
	
	/**楼主信息**/
	@JSONField(name="author")
	public AuthorBean author;
	
	/**回复**/
	@JSONField(name="reply")
	public ReplyBean reply;
}
