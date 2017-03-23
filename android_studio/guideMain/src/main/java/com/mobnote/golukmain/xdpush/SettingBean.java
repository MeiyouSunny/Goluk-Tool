package com.mobnote.golukmain.xdpush;

/**
 * 推送设置 数据 (服务端下发)
 *
 * @author jyf
 */
public class SettingBean {
    /**
     * 请求是否成功
     */
    public boolean isSucess;
    /**
     * 返回结果 0:成功，1：参数错误 ,2 :未知异常, 3:用户不存在
     */
    public String result;
    /**
     * 是否接受评论 1 / 0 接收/不接收
     */
    public String isComment;
    /**
     * 是否接受点赞 1 / 0 接收/不接收
     */
    public String isPraise;
    /***
     * 是否有人关注 1 / 0 接收/不接收
     */
    public String isFollow;
    /**
     * 用户 uid
     */
    public String uid;
    /**
     * 是否接收好友动态  0：不接收；1：接收；
     */
    public String isFriend;

}
