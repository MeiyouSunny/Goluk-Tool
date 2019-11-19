package com.mobnote.t1sp.api;

import likly.reverse.Reverse;

/**
 * ApiUtil 返回对应的ApiService
 */
public class ApiUtil {

    /**
     * 小白APIService
     *
     * @return ApiServiceAit
     */
    public static ApiServiceAit apiServiceAit() {
        return Reverse.service(ApiServiceAit.class);
    }

}
