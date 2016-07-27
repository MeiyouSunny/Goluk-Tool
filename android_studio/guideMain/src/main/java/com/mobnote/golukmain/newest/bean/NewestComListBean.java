package com.mobnote.golukmain.newest.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by DELL-PC on 2016/7/25.
 */
public class NewestComListBean {
    /* 评论id */
    @JSONField(name="commentid")
    public String commentid;
    /* 评论作者 */
    @JSONField(name="authorid")
    public String authorid;
    /* 作者昵称 */
    @JSONField(name="name")
    public String name;
    /* 作者头像 评论作者头像 */
    @JSONField(name="avatar")
    public String avatar;
    /* 评论时间 */
    @JSONField(name="time")
    public String time;
    /* 评论内容 */
    @JSONField(name="text")
    public String text;

    /* 回复人id */
    @JSONField(name="replyid")
    public String replyid;
    /* 回复人呢称 */
    @JSONField(name="replyname")
    public String replyname;
}
