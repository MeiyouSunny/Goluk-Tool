package cn.com.mobnote.golukmobile.comment.bean;

import cn.com.mobnote.golukmobile.cluster.bean.UserLabelBean;

import com.alibaba.fastjson.annotation.JSONField;

public class AuthorBean {
	/**评论作者**/
	@JSONField(name="id")
	public String authorid;
	
	/**作者呢称**/
	@JSONField(name="name")
	public String name;
	
	/**作者头像**/
	@JSONField(name="avatar")
	public String avatar;
	
	/**作者头像**/
	@JSONField(name="customavatar")
	public String customavatar;
	
	@JSONField(name="label")
	public UserLabelBean label;
}
