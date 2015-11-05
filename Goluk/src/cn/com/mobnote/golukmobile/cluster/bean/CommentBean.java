package cn.com.mobnote.golukmobile.cluster.bean;

import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class CommentBean {
	
	/**是否显示评论**/
	@JSONField(name="iscomment")
	public String iscomment;
	
	/**评论数**/
	@JSONField(name="comcount")
	public String comcount;
	
	/**评论列表**/
	@JSONField(name="comlist")
	public List<ComlistBean> comlist;

	
}
