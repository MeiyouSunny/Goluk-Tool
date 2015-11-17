/**
 * Copyright(C) 2015 LightInTheBox All rights reserved.
 *
 * Original Author: zengpeiyu@lightinthebox.com, 2015/3/25
 */
package cn.com.mobnote.golukmobile.http;

import cn.com.mobnote.application.GolukApplication;

import com.android.volley.toolbox.HurlStack;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import javax.net.ssl.SSLSocketFactory;

/**
 * LitbHurlStack base on HurlStack which support cookie.
 */
public class GolukHurlStack extends HurlStack {

    public GolukHurlStack() {
        this(null);
    }

    public GolukHurlStack(UrlRewriter urlRewriter) {
        this(urlRewriter, null);
    }

    public GolukHurlStack(UrlRewriter urlRewriter, SSLSocketFactory sslSocketFactory) {
        super(urlRewriter, sslSocketFactory);

        CookieHandler.setDefault(
                new CookieManager(new PersistentCookieStore(GolukApplication.getInstance()), CookiePolicy.ACCEPT_ALL));
    }
}
