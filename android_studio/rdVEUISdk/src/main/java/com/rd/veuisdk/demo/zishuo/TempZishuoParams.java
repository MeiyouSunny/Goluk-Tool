package com.rd.veuisdk.demo.zishuo;

import com.rd.vecore.Music;

import java.util.ArrayList;

/**
 * 字说临时存储 录音、文字、题词等
 */
public class TempZishuoParams {

    private static TempZishuoParams mInstance = new TempZishuoParams();

    private TempZishuoParams() {}

    public static TempZishuoParams getInstance() {
        return mInstance;
    }

    /**
     * 录音、音乐
     */
    private ArrayList<Music> mMusic = new ArrayList<>();

    public void setMusicList(ArrayList<Music> music) {
        if (mMusic != null) {
            mMusic.clear();
            mMusic.addAll(music);
        }
    }

    public ArrayList<Music> getMusicList() {
        return mMusic;
    }

    public void removeMusic(){
        mMusic.clear();
    }

    /**
     * 文字
     */
    private ArrayList<TextNode> mTextNodes = new ArrayList<>();

    public void setTextNodes(ArrayList<TextNode> nodes) {
        mTextNodes.clear();
        mTextNodes.addAll(nodes);
    }

    public ArrayList<TextNode> getTextNodes() {
        return mTextNodes;
    }

    //总的时间 秒
    public float getDuration() {
        float time = 0;
        for (Music music : mMusic) {
            time += music.getDuration();
        }
        float time2 = 0;
        if (mTextNodes.size() > 0) {
            time2 = mTextNodes.get(mTextNodes.size() - 1).getEnd();
        }
        return time > time2? time : time2;
    }

    /**
     * 题词库 显示文字
     */
    public ArrayList<String> mStrings = new ArrayList<>();

    public ArrayList<String> getStrings() {
        return mStrings;
    }

    public void setStrings(ArrayList<String> strings) {
        mStrings.clear();
        mStrings.addAll(strings);
    }

    /**
     * 释放
     */
    public void recycle() {
        mMusic.clear();
        mStrings.clear();
        mTextNodes.clear();
        mType = 1;
        position = 0;
        mTemplate = true;
        mIsMore = false;
    }

    /**
     * 是否更多动画
     */
    private boolean mIsMore = false;

    public boolean isMore() {
        return mIsMore;
    }

    public void setMore(boolean more) {
        mIsMore = more;
    }

    //样式恢复 旋转 横 竖
    private int mType = 1;
    private int position = 0;
    private boolean mTemplate = true;//是否是新模板

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isTemplate() {
        return mTemplate;
    }

    public void setTemplate(boolean template) {
        mTemplate = template;
    }
}
