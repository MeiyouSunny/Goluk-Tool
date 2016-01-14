package cn.com.mobnote.golukmobile.msg.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class MessageCommentBean {

	/**评论id **/
	@JSONField(name="commentid")
	public String commentid;
	
	/**评论作者id **/
	@JSONField(name="authorid")
	public String authorid;
	
	/**评论作者呢称**/
	@JSONField(name="name")
	public String name;
	
	/**评论作者头像**/
	@JSONField(name="avatar")
	public String avatar;
	
	/**评论作者自定义头像**/
	@JSONField(name="customavatar")
	public String customavatar ;
	
	/**回复对象id **/
	@JSONField(name="replyid")
	public String replyid ;
	
	/**回复对象呢称 **/
	@JSONField(name="replyname")
	public String replyname ;
	
	/**评论内容 **/
	@JSONField(name="text")
	public String text ;
	
	/**评论时间**/
	@JSONField(name="time")
	public String time ;
	
}
