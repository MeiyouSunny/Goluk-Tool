package com.goluk.crazy.panda.common.http;

import com.goluk.crazy.panda.common.http.bean.HttpResultBean;
import com.goluk.crazy.panda.search.bean.SearchDataBean;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by leege100 on 2016/8/22.
 */
public class HttpMethods {

    final String baseUrl = "http://server.goluk.cn/cdcSearch/";
    private static final int DEFAULT_TIMEOUT = 5;

    private Retrofit retrofit;
    private SearchService searchService;

    //构造方法私有
    private HttpMethods() {
        //手动创建一个OkHttpClient并设置超时时间
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();

        searchService = retrofit.create(SearchService.class);
    }

    //在访问HttpMethods时创建单例
    private static class SingletonHolder {
        private static final HttpMethods INSTANCE = new HttpMethods();
    }

    //获取单例
    public static HttpMethods getInstance(){
        return SingletonHolder.INSTANCE;
    }

    public Observable<SearchDataBean> searchUser(String terms, String operation, int pagesize, String version, String locale) {

        return searchService.searchUser("searchUser", "200", terms, operation, pagesize, version, locale)
                .map(new HttpResultFunc<SearchDataBean>());
    }

    /**
     * 用来统一处理Http的resultCode,并将HttpResult的Data部分剥离出来返回给subscriber
     *
     * @param <T>   Subscriber真正需要的数据类型，也就是Data部分的数据类型
     */
    private class HttpResultFunc<T> implements Func1<HttpResultBean<T>, T> {

        @Override
        public T call(HttpResultBean<T> httpResult) {
            if (httpResult == null) {
                throw new ApiException(-1);
            }
            if (httpResult.getCode() != 0) {
                throw new ApiException(httpResult.getCode());
            }
            return httpResult.getData();
        }
    }
}
