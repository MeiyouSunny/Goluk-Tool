package com.mobnote.golukmain.cluster.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class RankingListUser {
	/**请求是否成功**/
	@JSONField(name="uid")
	public String uid;
	
	/**返回数据**/
	@JSONField(name="nickname")
	public String nickname;
	
	/**返回调试信息**/
	@JSONField(name="avatar")
	public String avatar;

	/**返回调试信息**/
	@JSONField(name="customavatar")
	public String customavatar;

	/**返回调试信息**/
	@JSONField(name="sex")
	public String sex;

	/**返回调试信息**/
	@JSONField(name="introduction")
	public String introduction;

	/**返回调试信息**/
	@JSONField(name="certification")
	public RankingListCertification certification;
}
