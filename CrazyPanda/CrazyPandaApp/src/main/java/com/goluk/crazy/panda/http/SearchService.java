package com.goluk.crazy.panda.http;

import com.goluk.crazy.panda.search.bean.SearchDataBean;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by leege100 on 2016/8/16.
 */
public interface SearchService {
    @GET("search.htm")
    Observable<HttpResultBean<SearchDataBean>> searchUser(@Query("method") String method, @Query("xieyi") String xieyi,
                                                          @Query("terms") String terms, @Query("operation") String operation,
                                                          @Query("pagesize") int pagesize, @Query("version") String version,
                                                          @Query("locale") String locale);
}
