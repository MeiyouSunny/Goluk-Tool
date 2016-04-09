/**
 * Copyright(C) 2015 LightInTheBox All rights reserved.
 *
 * Original Author: zengpeiyu@lightinthebox.com, 2015/4/14
 */
package com.mobnote.golukmain.http;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ResponseDelivery;

import java.lang.ref.WeakReference;

/**
 * Deal with tag
 */
public class GolukRequestQueue extends RequestQueue {
    public GolukRequestQueue(Cache cache,
                    Network network,
                    int threadPoolSize,
                    ResponseDelivery delivery) {
        super(cache, network, threadPoolSize, delivery);
    }

    public GolukRequestQueue(Cache cache, Network network, int threadPoolSize) {
        super(cache, network, threadPoolSize);
    }

    public GolukRequestQueue(Cache cache, Network network) {
        super(cache, network);
    }

//    @Override
//    public void cancelAll(final Object tag) {
//        if (tag == null) {
//            throw new IllegalArgumentException("Cannot cancelAll with a null tag");
//        }
//        cancelAll(new RequestFilter() {
//            @Override
//            public boolean apply(Request<?> request) {
//                WeakReference<Object> realTag = (WeakReference<Object>)request.getTag();
//                return (realTag != null) && (realTag.get() == tag);
//            }
//        });
//    }
}
