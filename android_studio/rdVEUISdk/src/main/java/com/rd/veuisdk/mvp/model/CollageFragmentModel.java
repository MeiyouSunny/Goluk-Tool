package com.rd.veuisdk.mvp.model;

import com.rd.veuisdk.model.CollageInfo;
import com.rd.veuisdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 画中画数据
 *
 * @author JIAN
 * @create 2019/4/9
 * @Describe
 */
public class CollageFragmentModel {


    private List<CollageInfo> mList;

    private CollageFragmentModel() {

    }

    public CollageFragmentModel(List<CollageInfo> list) {
        mList = new ArrayList<>();
        if (null != list && list.size() > 0) {
            mList.addAll(list);
        }
    }

    public List<CollageInfo> getList() {
        return mList;
    }


    /**
     * 移除画中画
     *
     * @param info
     * @return
     */
    public boolean remove(CollageInfo info) {
        return mList.remove(info);
    }

    /**
     * 新增画中画
     *
     * @param info
     */
    public void add(CollageInfo info) {
        mList.add(info);
    }

    /***
     * 当前选中的画中画
     * @param mixId  画中画Id
     * @return
     */
    public CollageInfo getMixInfo(int mixId) {
        int len = mList.size();
        CollageInfo collageInfo = null;
        CollageInfo tmp = null;
        for (int i = 0; i < len; i++) {
            tmp = mList.get(i);
            if (tmp.getId() == mixId) {
                collageInfo = tmp;
                break;
            }
        }
        return collageInfo;

    }

    /**
     * 检测该id之前是否存在于列表
     *
     * @param id
     * @return
     */
    public boolean checkExit(int id) {
        CollageInfo tmp;
        boolean hasExit = false;
        int len = mList.size();
        for (int i = 0; i < len; i++) {
            tmp = mList.get(i);
            if (tmp.getId() == id) {
                hasExit = true;
                break;
            }
        }
        return hasExit;
    }

    /**
     * 集合数据是否一致
     * @param target
     * @return
     */
    public boolean isEquals(List<CollageInfo> target) {
        return Utils.isEqualsMixList(mList, target);
    }

    public void recycle() {
        mList.clear();
        mList = null;
    }
}
