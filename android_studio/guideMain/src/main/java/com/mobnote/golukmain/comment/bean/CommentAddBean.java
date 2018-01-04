package com.mobnote.golukmain.comment.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.mobnote.golukmain.cluster.bean.UserLabelBean;

public class CommentAddBean {
	/**评论id**/
	@JSONField(name="commentid")
	public String commentid;
	
	/**评论作者**/
	@JSONField(name="authorid")
	public String authorid;
	
	/**作者呢称**/
	@JSONField(name="authorname")
	public String authorname;
	
	/**作者头像**/
	@JSONField(name="authoravatar")
	public String authoravatar;
	
	/**作者头像**/
	@JSONField(name="customavatar")
	public String customavatar;
	
	/**评论时间**/
	@JSONField(name="time")
	public String time;

	/** 标准时区时间戳 */
	public long ts;
	
	/**评论内容**/
	@JSONField(name="text")
	public String text;
	
	/**回复人id**/
	@JSONField(name="replyid")
	public String replyid;
	
	/**回复人呢称**/
	@JSONField(name="replyname")
	public String replyname;

	@JSONField(name="label")
	public UserLabelBean label;

	@JSONField(name="result")
	public String result;

	@JSONField(name="seq")
	public String seq;
}
