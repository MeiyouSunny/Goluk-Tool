package com.rd.veuisdk.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.rd.lib.utils.CoreUtils;
import com.rd.veuisdk.R;
import com.rd.veuisdk.manager.UIConfiguration;

/**
 * 主编辑菜单
 */
public class VideoEditFragment extends BaseFragment {
    private static final String PARAM_UI_CONFIG = "ui_config";

    public static VideoEditFragment newInstance(UIConfiguration configuration) {
        Bundle args = new Bundle();
        VideoEditFragment fragment = new VideoEditFragment();
        args.putParcelable(PARAM_UI_CONFIG, configuration);
        fragment.setArguments(args);
        return fragment;
    }

    public void setMenuListener(@NonNull IMenuListener menuListener) {
        mMenuListener = menuListener;
    }

    private IMenuListener mMenuListener;


    public static interface IMenuListener {

        void checkItemBefore();


        void checkItemEnd();

        //高级编辑

        void onCover();

        void onGraffiti();

        void onMV();

        void onCaption();

        void onSticker();

        void onFilter();

        void onEffect();

        void onCollage();

        void onOSD();


        //声音

        void onVolume();

        void onMusic();

        void onAudio();

        void onMusicEffect();

        void onSound();

        void onMusicMany();


        //片段编辑
        void onPartEdit();

        //设置
        void onProportion();

        void onAnimType();

        void onBackground();
    }

    private UIConfiguration mUIConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUIConfig = getArguments().getParcelable(PARAM_UI_CONFIG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_video_edit, container, false);
        return mRoot;
    }

    private void initView() {

        RadioGroup rgRootMenu = $(R.id.rgRootMenu);
        rgRootMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                resetMenu(checkedId);
            }
        });
        if (mUIConfig.isHidePartEdit()) {
            $(R.id.tvPartedit).setVisibility(View.GONE);
        } else {
            $(R.id.tvPartedit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mMenuListener) {
                        mMenuListener.onPartEdit();
                    }
                }
            });
        }
        rgRootMenu.check(R.id.rbAdvancedEdit);

        $(R.id.rb_theme).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_cover).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_graffiti).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_music).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_volume).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_audio).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_sound_effect).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_word).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_filter).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_sticker).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_effect).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_collage).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_mv).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_osd).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_proportion).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_animation_type).setOnClickListener(onVerVideoMenuListener);
        $(R.id.rb_background).setOnClickListener(onVerVideoMenuListener);
        //音效
        $(R.id.rb_sound).setOnClickListener(onVerVideoMenuListener);
        //多段配乐
        $(R.id.rb_music_many).setOnClickListener(onVerVideoMenuListener);


    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private int mRbWidth = 0;

    /**
     * 设置
     */
    private int onSettingUI() {
        setViewVisibility(R.id.rb_graffiti, false);
        setViewVisibility(R.id.rb_effect, false);
        setViewVisibility(R.id.rb_mv, false);
        setViewVisibility(R.id.rb_word, false);
        setViewVisibility(R.id.rb_filter, false);
        setViewVisibility(R.id.rb_sticker, false);
        setViewVisibility(R.id.rb_osd, false);
        setViewVisibility(R.id.rb_audio, false);
        setViewVisibility(R.id.rb_music, false);
        setViewVisibility(R.id.rb_collage, false);
        setViewVisibility(R.id.rb_sound_effect, false);
        //音效
        setViewVisibility(R.id.rb_sound, false);
        //多段配乐
        setViewVisibility(R.id.rb_music_many, false);
        //音量
        setViewVisibility(R.id.rb_volume, false);

        int rbCount = 0;
        if (!mUIConfig.isHideCover()) {
            rbCount++;
        }
        setViewVisibility(R.id.rb_cover, !mUIConfig.isHideCover());


        setViewVisibility(R.id.rb_proportion, true);
        rbCount++;
        setViewVisibility(R.id.rb_animation_type, true);
        rbCount++;
        setViewVisibility(R.id.rb_background, true);
        rbCount++;
        return rbCount;
    }

    /**
     * 高级编辑
     */
    private int onAdvEditUI() {
        setViewVisibility(R.id.rb_cover, false);
        setViewVisibility(R.id.rb_proportion, false);
        setViewVisibility(R.id.rb_music, false);
        setViewVisibility(R.id.rb_sound_effect, false);
        setViewVisibility(R.id.rb_animation_type, false);
        setViewVisibility(R.id.rb_background, false);
        setViewVisibility(R.id.rb_audio, false);
        //音效 多段配乐 音量
        setViewVisibility(R.id.rb_sound, false);
        setViewVisibility(R.id.rb_music_many, false);
        setViewVisibility(R.id.rb_volume, false);

        ///
        setViewVisibility(R.id.rb_word, false);
        setViewVisibility(R.id.rb_sticker, false);
        setViewVisibility(R.id.rb_collage, false);
        setViewVisibility(R.id.rb_effect, false);
        setViewVisibility(R.id.rb_osd, false);
        setViewVisibility(R.id.rb_graffiti, false);
        setViewVisibility(R.id.rb_mv, false);
        ///

        int rbCount = 0;
        //配音
        //配乐放到声音界面 直接隐藏
//        if (mUIConfig.isHideDubbing()) {
//            setViewVisibility(R.id.rb_audio, false);
//        } else {
//            if (mUIConfig.voiceLayoutTpye == UIConfiguration.VOICE_LAYOUT_2) {
//                setViewVisibility(R.id.rb_audio, false); //音效按钮放在配乐界面
//            } else {
//                rbCount++;
//                setViewVisibility(R.id.rb_audio, true);
//            }
//        }

        //mv
//        if (mUIConfig.enableMV) {
//            rbCount++;
//        }
//        setViewVisibility(R.id.rb_mv, mUIConfig.enableMV);

        //文字（字幕）
//        if (!mUIConfig.isHideTitling()) {
//            rbCount++;
//        }
//        setViewVisibility(R.id.rb_word, !mUIConfig.isHideTitling());

        //滤镜
        if (!mUIConfig.isHideFilter()) {
            rbCount++;
        }
        setViewVisibility(R.id.rb_filter, !mUIConfig.isHideFilter());


        //贴纸
//        if (!mUIConfig.isHideSpecialEffects()) {
//            rbCount++;
//        }
//        setViewVisibility(R.id.rb_sticker, !mUIConfig.isHideSpecialEffects());

        //画中画
//        if (!mUIConfig.isHideCollage()) {
//            rbCount++;
//        }
//        setViewVisibility(R.id.rb_collage, !mUIConfig.isHideCollage());

        //新特效
//        if (!mUIConfig.isHideEffects()) {
//            rbCount++;
//        }
//        setViewVisibility(R.id.rb_effect, !mUIConfig.isHideEffects());

        //去水印|马赛克
//        if (!mUIConfig.isHideDewatermark()) {
//            rbCount++;
//        }
//        setViewVisibility(R.id.rb_osd, !mUIConfig.isHideDewatermark());


        //涂鸦
//        if (!mUIConfig.isHideGraffiti()) {
//            rbCount++;
//        }
//        setViewVisibility(R.id.rb_graffiti, !mUIConfig.isHideGraffiti());

        return rbCount;
    }

    /**
     * 声音
     */
    private int onVoiceUI() {
        setViewVisibility(R.id.rb_cover, false);
        setViewVisibility(R.id.rb_graffiti, false);
        setViewVisibility(R.id.rb_effect, false);
        setViewVisibility(R.id.rb_mv, false);
        setViewVisibility(R.id.rb_word, false);
        setViewVisibility(R.id.rb_filter, false);
        setViewVisibility(R.id.rb_sticker, false);
        setViewVisibility(R.id.rb_osd, false);
        setViewVisibility(R.id.rb_proportion, false);
        setViewVisibility(R.id.rb_collage, false);
        setViewVisibility(R.id.rb_animation_type, false);
        setViewVisibility(R.id.rb_background, false);

        ///
        setViewVisibility(R.id.rb_sound_effect, false);
        setViewVisibility(R.id.rb_sound, false);
        setViewVisibility(R.id.rb_music_many, false);
        setViewVisibility(R.id.rb_audio, false);
        ///

        int rbCount = 0;
        //配音
//        if (mUIConfig.isHideDubbing()) {
//            setViewVisibility(R.id.rb_audio, false);
//        } else {
//            if (mUIConfig.voiceLayoutTpye == UIConfiguration.VOICE_LAYOUT_2) {
//                setViewVisibility(R.id.rb_audio, false);
//            } else {
//                rbCount++;
//                setViewVisibility(R.id.rb_audio, true);
//            }
//        }
        //配乐
        if (!mUIConfig.isHideSoundTrack()) {
            rbCount++;
        }
        setViewVisibility(R.id.rb_music, !mUIConfig.isHideSoundTrack());
        //变声
//        if (!mUIConfig.isHideMusicEffect()) {
//            rbCount++;
//        }
//        setViewVisibility(R.id.rb_sound_effect, !mUIConfig.isHideMusicEffect());
        //音效
//        if (!mUIConfig.isHideSound()) {
//            rbCount++;
//        }
//        setViewVisibility(R.id.rb_sound, !mUIConfig.isHideSound());
        //多段配乐
//        if (!mUIConfig.isHideMusicMany()) {
//            rbCount++;
//        }
//        setViewVisibility(R.id.rb_music_many, !mUIConfig.isHideMusicMany());
        //音量
        if (!mUIConfig.isHideVolume()) {
            rbCount++;
        }
        setViewVisibility(R.id.rb_volume, !mUIConfig.isHideVolume());

        return rbCount;
    }


    private void resetMenu(int checkedId) {

        int rbCount = 0;
        if (checkedId == R.id.rbSetting) {
            rbCount = onSettingUI();
        } else if (checkedId == R.id.rbAdvancedEdit) {
            rbCount = onAdvEditUI();
        } else if (checkedId == R.id.rbVoice) {
            rbCount = onVoiceUI();
        }

        $(R.id.rb_word).measure(0, 0);

        if (mRbWidth == 0) {
            mRbWidth = $(R.id.rb_word).getMeasuredWidth();
        }

        DisplayMetrics displayMetrics = CoreUtils.getMetrics();
        int padding = (displayMetrics.widthPixels - rbCount * mRbWidth - (rbCount + 1) * CoreUtils.dpToPixel(15)) / (rbCount + 1);
        padding = Math.max(0, padding);

        $(R.id.rb_cover).setPadding(padding, 0, 0, 0);
        $(R.id.rb_graffiti).setPadding(padding, 0, 0, 0);
        $(R.id.rb_word).setPadding(padding, 0, 0, 0);
        $(R.id.rb_filter).setPadding(padding, 0, 0, 0);
        $(R.id.rb_sticker).setPadding(padding, 0, 0, 0);
        $(R.id.rb_effect).setPadding(padding, 0, 0, 0);
        $(R.id.rb_music).setPadding(padding, 0, 0, 0);
        $(R.id.rb_osd).setPadding(padding, 0, 0, 0);
        $(R.id.rb_audio).setPadding(padding, 0, 0, 0);
        $(R.id.rb_mv).setPadding(padding, 0, 0, 0);
        $(R.id.rb_sound_effect).setPadding(padding, 0, 0, 0);
        $(R.id.rb_proportion).setPadding(padding, 0, 0, 0);
        $(R.id.rb_collage).setPadding(padding, 0, 0, 0);
        $(R.id.rb_animation_type).setPadding(padding, 0, 0, 0);
        $(R.id.rb_background).setPadding(padding, 0, 0, 0);
        $(R.id.rb_sound).setPadding(padding, 0, 0, 0);
        $(R.id.rb_music_many).setPadding(padding, 0, 0, 0);
        $(R.id.rb_volume).setPadding(padding, 0, 0, 0);
    }

    private View.OnClickListener onVerVideoMenuListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            onCheckItem(v.getId());
        }
    };

    public int getCheckedId() {
        return mCheckedId;
    }

    public static final int VIDEO_EDIT = -1; //主编辑-高级编辑
    private int mCheckedId = VIDEO_EDIT;

    /**
     * 恢复选中的按钮标记
     */
    public void resetMenu() {
        mCheckedId = VIDEO_EDIT;
    }

    /**
     * 指定要选中的模块（配乐-配音）
     */
    public void setChecked(int checked) {
        mCheckedId = checked;
    }


    private void onCheckItem(int checkedId) {
        mMenuListener.checkItemBefore();
        mCheckedId = checkedId;

        if (checkedId == R.id.rb_cover) {
            mMenuListener.onCover();
        } else if (checkedId == R.id.rb_graffiti) {
            mMenuListener.onGraffiti();
        } else if (checkedId == R.id.rb_music) {
            mMenuListener.onMusic();
        } else if (checkedId == R.id.rb_sound_effect) {
            mMenuListener.onMusicEffect();
        } else if (checkedId == R.id.rb_theme) {
        } else if (checkedId == R.id.rb_mv) {
            mMenuListener.onMV();
        } else if (checkedId == R.id.rb_proportion) {
            mMenuListener.onProportion();
        } else if (checkedId == R.id.rb_animation_type) {
            mMenuListener.onAnimType();
        } else if (checkedId == R.id.rb_background) {
            mMenuListener.onBackground();
        } else if (checkedId == R.id.rb_audio) {
            mMenuListener.onAudio();
        } else if (checkedId == R.id.rb_word) {
            mMenuListener.onCaption();
        } else if (checkedId == R.id.rb_osd) {
            mMenuListener.onOSD();
        } else if (checkedId == R.id.rb_collage) {
            mMenuListener.onCollage();
        } else if (checkedId == R.id.rb_filter) {
            mMenuListener.onFilter();
        } else if (checkedId == R.id.rb_sticker) {
            mMenuListener.onSticker();
        } else if (checkedId == R.id.rb_effect) {
            mMenuListener.onEffect();
        } else if (checkedId == R.id.rb_sound) {
            mMenuListener.onSound();
        } else if (checkedId == R.id.rb_music_many) {
            mMenuListener.onMusicMany();
        } else if (checkedId == R.id.rb_volume) {
            mMenuListener.onVolume();
        } else {
            android.util.Log.e(TAG, "onCheckItem: other ");
        }
        mMenuListener.checkItemEnd();

    }

}
