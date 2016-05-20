package com.mobnote.golukmain.comment.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class CommentDelBean {

	@JSONField(name="result")
	public String result;
	@JSONField(name="comment_id")
	public String comment_id;
}
