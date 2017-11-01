package com.mobnote.golukmain.thirdshare.bean;

import android.app.Activity;

import com.mobnote.golukmain.thirdshare.SharePlatformUtil;
import com.mobnote.golukmain.thirdshare.ThirdShareBean;
import com.mobnote.golukmain.thirdshare.ThirdShareTool;

/**
 * Created by leege100 on 2017/3/12.
 */

public class ThirdSharePlatformBean extends SharePlatform {

    private String name;
    private int largeIconRes;
    private int smallIconRes;
    private int smallIconSelectedRes;

    public ThirdSharePlatformBean(int type){
        this.platformType = type;
    }

    private int platformType;

    public int getPlatformType() {
        return platformType;
    }

    public void setPlatformType(int platformType) {
        this.platformType = platformType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLargeIconRes() {
        return largeIconRes;
    }

    public void setLargeIconRes(int largeIconRes) {
        this.largeIconRes = largeIconRes;
    }

    public int getSmallIconRes() {
        return smallIconRes;
    }

    public void setSmallIconRes(int smallIconRes) {
        this.smallIconRes = smallIconRes;
    }

    public int getSmallIconSelectedRes() {
        return smallIconSelectedRes;
    }

    public void setSmallIconSelectedRes(int smallIconSelectedRes) {
        this.smallIconSelectedRes = smallIconSelectedRes;
    }

    public void startShare(Activity activity, SharePlatformUtil spf, ThirdShareBean shareBean) {
        ThirdShareTool shareTool = new ThirdShareTool(activity, spf, shareBean);
        if(getPlatformType() == SharePlatform.SHARE_PLATFORM_WEXIN_CIRCLE){
            shareTool.click_wechat_circle();
        }else if(getPlatformType() == SharePlatform.SHARE_PLATFORM_WEXIN){
            shareTool.click_wechat();
        }else if(getPlatformType() == SharePlatform.SHARE_PLATFORM_WEIBO_SINA){
            shareTool.click_sina();
        }else if(getPlatformType() == SharePlatform.SHARE_PLATFORM_QQ_ZONE){
            shareTool.click_qqZone();
        }else if(getPlatformType() == SharePlatform.SHARE_PLATFORM_QQ){
            shareTool.click_QQ();
        }else if(getPlatformType() == SharePlatform.SHARE_PLATFORM_FACEBOOK){
            shareTool.click_facebook();
        }else if(getPlatformType() == SharePlatform.SHARE_PLATFORM_TWITTER){
            shareTool.click_twitter();
        }else if(getPlatformType() == SharePlatform.SHARE_PLATFORM_INSTAGRAM){
            shareTool.click_instagram(shareBean.filePath);
        }else if(getPlatformType() == SharePlatform.SHARE_PLATFORM_WHATSAPP){
            shareTool.click_whatsapp();
        }else if(getPlatformType() == SharePlatform.SHARE_PLATFORM_LINE){
            shareTool.click_line();
        }else if(getPlatformType() == SharePlatform.SHARE_PLATFORM_VK) {
            shareTool.click_VK();
        }
    }
}
