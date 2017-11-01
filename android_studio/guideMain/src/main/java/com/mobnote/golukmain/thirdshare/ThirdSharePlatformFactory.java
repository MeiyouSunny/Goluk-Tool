package com.mobnote.golukmain.thirdshare;

import android.content.Context;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.thirdshare.bean.SharePlatform;
import com.mobnote.golukmain.thirdshare.bean.ThirdSharePlatformBean;

/**
 * Created by leege100 on 2017/3/12.
 */

public class ThirdSharePlatformFactory {
    public static ThirdSharePlatformBean createSharePlatformBean(Context cxt, int type) {
        ThirdSharePlatformBean platformBean = new ThirdSharePlatformBean(type);
        if (type == SharePlatform.SHARE_PLATFORM_QQ) {
            platformBean.setName(cxt.getString(R.string.str_qq_friends));
            platformBean.setLargeIconRes(R.drawable.share_qq_friend_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_qq);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_qq_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_QQ_ZONE) {
            platformBean.setName(cxt.getString(R.string.str_qzone));
            platformBean.setLargeIconRes(R.drawable.share_qq_zone_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_qzone);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_qzone_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_WEIBO_SINA) {
            platformBean.setName(cxt.getString(R.string.str_weibo));
            platformBean.setLargeIconRes(R.drawable.share_weibo_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_weibo);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_weibo_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_WEXIN) {
            platformBean.setName(cxt.getString(R.string.str_weixin_friends));
            platformBean.setLargeIconRes(R.drawable.share_wechat_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_wechat);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_wechat_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_WEXIN_CIRCLE) {
            platformBean.setName(cxt.getString(R.string.str_circle_of_friends));
            platformBean.setLargeIconRes(R.drawable.share_wechat_friend_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_moments);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_moments_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_FACEBOOK) {
            platformBean.setName(cxt.getString(R.string.str_facebook));
            platformBean.setLargeIconRes(R.drawable.share_facebook_friend_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_facebook);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_facebook_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_INSTAGRAM) {
            platformBean.setName(cxt.getString(R.string.str_instagram));
            platformBean.setLargeIconRes(R.drawable.share_instagram_friend_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_instagram);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_instagram_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_TWITTER) {
            platformBean.setName(cxt.getString(R.string.str_twitter));
            platformBean.setLargeIconRes(R.drawable.share_twitter_friend_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_twitter);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_twitter_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_WHATSAPP) {
            platformBean.setName(cxt.getString(R.string.str_whatsapp));
            platformBean.setLargeIconRes(R.drawable.share_whatsapp_friend_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_whatsapp);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_whatsapp_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_LINE) {
            platformBean.setName(cxt.getString(R.string.str_line));
            platformBean.setLargeIconRes(R.drawable.share_line_friend_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_line);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_line_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_VK) {
            platformBean.setName(cxt.getString(R.string.str_vk));
            platformBean.setLargeIconRes(R.drawable.share_vk_icon);
            platformBean.setSmallIconRes(R.drawable.icon_share_vk);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_vk_click);
        } else if (type == SharePlatform.SHARE_PLATFORM_COPYLINK) {
            platformBean.setName(cxt.getString(R.string.str_copy_link));
            //platformBean.setLargeIconRes(R.drawable.icon_share_copy);
            platformBean.setSmallIconRes(R.drawable.icon_share_copy);
            platformBean.setSmallIconSelectedRes(R.drawable.icon_share_copy_click);
        }
        return platformBean;
    }
}
