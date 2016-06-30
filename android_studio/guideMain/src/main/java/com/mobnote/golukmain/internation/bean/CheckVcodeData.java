package com.mobnote.golukmain.internation.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by lily on 16-6-24.
 */
public class CheckVcodeData {

    /** 手机号 **/
    @JSONField(name = "phone")
    public String phone;

    /** 验证码 **/
    @JSONField(name = "vcode")
    public String vcode;

    /** 区号 **/
    @JSONField(name = "dialingcode")
    public String dialingcode;

    /** 2次验证码 **/
    @JSONField(name = "step2code")
    public String step2code;


}
