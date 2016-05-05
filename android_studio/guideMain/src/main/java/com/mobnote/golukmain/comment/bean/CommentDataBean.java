package com.mobnote.golukmain.comment.bean;

import java.util.ArrayList;

import com.alibaba.fastjson.annotation.JSONField;

public class CommentDataBean {
	/**评论数**/
	@JSONField(name="count")
	public String count;
	
	/**评论列表**/
	@JSONField(name="comments")
	public ArrayList<CommentItemBean> comments;
}
