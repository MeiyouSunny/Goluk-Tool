package com.rd.veuisdk.demo.zishuo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.demo.zishuo.adapter.ZishuoStyleAdapter;
import com.rd.veuisdk.demo.zishuo.drawtext.CustomDrawHorizontal;
import com.rd.veuisdk.demo.zishuo.drawtext.CustomDrawRotate;
import com.rd.veuisdk.demo.zishuo.drawtext.CustomDrawRotate1;
import com.rd.veuisdk.demo.zishuo.drawtext.CustomDrawVertical;
import com.rd.veuisdk.demo.zishuo.drawtext.CustomHandler;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.listener.OnItemClickListener;

import java.util.ArrayList;

public class ZishuoFragment extends BaseFragment implements View.OnClickListener {

    private IVideoEditorHandler mVideoEditorHandler;
    private RecyclerView mRvStyle;//风格
    private ZishuoStyleAdapter mAdapter;
    //类型
    private int mType = 1;
    private int mPosition = 0;
    private boolean mTemplate = true;
    //点击的id
    private int mCheckedId = -1;
    //样式存放
    private ArrayList<ZishuoStyle> mStyles = new ArrayList<>();

    public static ZishuoFragment newInstance() {
        ZishuoFragment fragment = new ZishuoFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoEditorHandler = (IVideoEditorHandler) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_zishuo, container, false);
        mType = TempZishuoParams.getInstance().getType();
        mPosition = TempZishuoParams.getInstance().getPosition();
        mTemplate = TempZishuoParams.getInstance().isTemplate();
        initView();
        return mRoot;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void initView() {
        mRvStyle = $(R.id.rv_style);

        $(R.id.rb_rotate_text).setOnClickListener(this);
        $(R.id.rb_horizontal_text).setOnClickListener(this);
        $(R.id.rb_vertical_text).setOnClickListener(this);
        $(R.id.rb_style).setOnClickListener(this);
        $(R.id.rb_word).setOnClickListener(this);
        $(R.id.rb_sticker).setOnClickListener(this);
        $(R.id.rb_music).setOnClickListener(this);
        $(R.id.rb_sound_effect).setOnClickListener(this);
        $(R.id.rb_background).setOnClickListener(this);

        if (mType == 2) {
            ((RadioButton) $(R.id.rb_horizontal_text)).setChecked(true);
            mCheckedId = R.id.rb_horizontal_text;
        } else if (mType == 3) {
            ((RadioButton) $(R.id.rb_vertical_text)).setChecked(true);
            mCheckedId = R.id.rb_vertical_text;
        } else {
            ((RadioButton) $(R.id.rb_rotate_text)).setChecked(true);
            mCheckedId = R.id.rb_rotate_text;
        }

        //设置padding
        int rbCount = 6;
        $(R.id.rb_word).measure(0, 0);
        int mRbWidth =$(R.id.rb_word).getMeasuredWidth();
        DisplayMetrics displayMetrics = CoreUtils.getMetrics();
        int padding = (displayMetrics.widthPixels - rbCount * mRbWidth - (rbCount + 1) * CoreUtils.dpToPixel(15)) / (rbCount + 1);
        padding = Math.max(0, padding);

        $(R.id.rb_style).setPadding(padding, 0, 0, 0);
        $(R.id.rb_word).setPadding(padding, 0, 0, 0);
        $(R.id.rb_sticker).setPadding(padding, 0, 0, 0);
        $(R.id.rb_music).setPadding(padding, 0, 0, 0);
        $(R.id.rb_sound_effect).setPadding(padding, 0, 0, 0);
        $(R.id.rb_background).setPadding(padding, 0, 0, 0);
    }

    private void init() {
        //初始化recycler
        mAdapter = new ZishuoStyleAdapter(mStyles, getContext());
        mRvStyle.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRvStyle.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, Object item) {
                if (position != -1) {
                    mAdapter.setChecked(position);
                    //设置样式
                    if (mMenuListener != null) {
                        mMenuListener.onTemplate(((ZishuoStyle) item).getHandler(), null);
                    }
                    mPosition = position;
                    TempZishuoParams.getInstance().setType(mType);
                    TempZishuoParams.getInstance().setPosition(position);
                }
            }
        });
        //getStyles(mType);
        //恢复数据
        handler0 = new CustomDrawRotate(getContext());
        handler1 = new CustomDrawHorizontal(getContext());
        handler2 = new CustomDrawVertical(getContext());
        if (mTemplate) {
            //初始化 默认第一个
            if (mMenuListener != null) {
                mMenuListener.onTemplate(handler0, null);
            }
            TempZishuoParams.getInstance().setTemplate(false);
        }
        mAdapter.setChecked(mPosition);
    }

    private CustomHandler handler0;
    private CustomHandler handler1;
    private CustomHandler handler2;

    //点击菜单
    public int getCheckedId() {
        return mCheckedId;
    }

    /**
     * 恢复选中的按钮标记
     */
    public void resetMenu() {
        mCheckedId = -1;
    }

    public int getType() {
        return mType;
    }

    public int getPosition() {
        return mPosition;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        mCheckedId = i;
        if (i == R.id.rb_rotate_text) {
            //旋转文字
            //getStyles(1);
            setTemplate(handler0, 1, null);
        } else if (i == R.id.rb_horizontal_text) {
            //横排文字
            //getStyles(2);
            setTemplate(handler1, 2, null);
        } else if (i == R.id.rb_vertical_text) {
            //竖排文字
            //getStyles(3);
            setTemplate(handler2, 3, null);
        } else  if (i == R.id.rb_style) {
            mMenuListener.onStyle();
        } else if (i == R.id.rb_word) {
            mMenuListener.onWord();
        } else if (i == R.id.rb_sticker) {
            mMenuListener.onSticker();
        } else if (i == R.id.rb_sound_effect) {
            mMenuListener.onSoundEffect();
        } else if (i == R.id.rb_music) {
            mMenuListener.onMusic();
        } else if (i == R.id.rb_background) {
            mMenuListener.onBackground();
        }
    }

    //设置模板
    private void setTemplate(CustomHandler handler, int type, String path) {
        mType = type;
        //设置样式
        if (mMenuListener != null) {
            mMenuListener.onTemplate(handler, path);
        }
        TempZishuoParams.getInstance().setType(mType);
        TempZishuoParams.getInstance().setPosition(0);
    }

    private void getStyles(int type) {
        //获取样式
        mType = type;

        //测试
        mStyles.clear();
        String cover = "https://rdfile.oss-cn-hangzhou.aliyuncs.com/filemanage/6ecb39f1c12f1a35/transition/1560848886031/cover.jpg";
        ZishuoStyle zishuoStyle;
        if (mType == 2) {
            //横排
            zishuoStyle = new ZishuoStyle();
            CustomDrawHorizontal handler1 = new CustomDrawHorizontal(getContext());
            zishuoStyle.setCover(cover);
            zishuoStyle.setHandler(handler1);
            mStyles.add(zishuoStyle);
        } else if (mType == 3) {
            //竖排
            zishuoStyle = new ZishuoStyle();
            CustomDrawVertical handler = new CustomDrawVertical(getContext());
            zishuoStyle.setCover(cover);
            zishuoStyle.setHandler(handler);
            mStyles.add(zishuoStyle);
        } else {
            //旋转1
            zishuoStyle = new ZishuoStyle();
            CustomHandler handler2 = new CustomDrawRotate(getContext());
            zishuoStyle.setCover(cover);
            zishuoStyle.setHandler(handler2);
            mStyles.add(zishuoStyle);
            //旋转2
            zishuoStyle = new ZishuoStyle();
            CustomHandler handler3 = new CustomDrawRotate1(getContext());
            zishuoStyle.setCover(cover);
            zishuoStyle.setHandler(handler3);
            mStyles.add(zishuoStyle);
        }
        mAdapter.setChecked(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private IMenuListener mMenuListener;

    public void setMenuListener(IMenuListener menuListener) {
        mMenuListener = menuListener;
    }

    public interface IMenuListener {

        /**
         * 样式
         */
        void onStyle();

        /**
         * 文字
         */
        void onWord();

        /**
         * 贴纸
         */
        void onSticker();

        /**
         * 变声
         */
        void onSoundEffect();

        /**
         * 音乐
         */
        void onMusic();

        /**
         * 背景
         */
        void onBackground();

        /**
         * 更改模板
         */
        void onTemplate(CustomHandler handler, String path);

    }

}
