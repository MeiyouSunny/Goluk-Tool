package com.rd.veuisdk.utils.apng.assist;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rd.veuisdk.utils.apng.ApngImageLoader;

public class PngImageLoader extends ImageLoader {

    private static PngImageLoader singleton;

    public static PngImageLoader getInstance() {
        if (singleton == null) {
            synchronized (ApngImageLoader.class) {
                if (singleton == null) {
                    singleton = new PngImageLoader();
                }
            }
        }
        return singleton;
    }

    protected PngImageLoader() { /*Singleton*/ }
}
