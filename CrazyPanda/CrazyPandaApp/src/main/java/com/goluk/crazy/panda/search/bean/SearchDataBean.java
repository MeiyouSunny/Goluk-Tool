package com.goluk.crazy.panda.search.bean;

import java.util.List;

/**
 * Created by leege100 on 2016/8/16.
 */
public class SearchDataBean {

    private int usercount;
    private List<SearchUserInfoBean> userlist;
    private List<SearchUserInfoBean> recomlist;

    public int getUsercount() {
        return usercount;
    }

    public void setUsercount(int usercount) {
        this.usercount = usercount;
    }

    public List<SearchUserInfoBean> getUserlist() {
        return userlist;
    }

    public void setUserlist(List<SearchUserInfoBean> userlist) {
        this.userlist = userlist;
    }

    public List<?> getRecomlist() {
        return recomlist;
    }

    public void setRecomlist(List<SearchUserInfoBean> recomlist) {
        this.recomlist = recomlist;
    }
}
