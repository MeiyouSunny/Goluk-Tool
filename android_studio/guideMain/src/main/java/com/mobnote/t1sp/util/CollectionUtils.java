package com.mobnote.t1sp.util;

import java.util.List;

/**
 * Collection Util
 */
public class CollectionUtils {

    /**
     * 列表是否为空
     *
     * @param list 列表
     * @return 是否为空
     */
    public static boolean isEmpty(List list) {
        return list == null || list.isEmpty();
    }

}
