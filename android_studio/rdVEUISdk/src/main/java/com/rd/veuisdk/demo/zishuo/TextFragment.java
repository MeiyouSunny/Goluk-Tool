package com.rd.veuisdk.demo.zishuo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rd.lib.ui.ExtButton;
import com.rd.lib.ui.ExtTextView;
import com.rd.veuisdk.IVideoEditorHandler;
import com.rd.veuisdk.R;
import com.rd.veuisdk.SdkEntry;
import com.rd.veuisdk.TTFHandler;
import com.rd.veuisdk.adapter.TTFAdapter;
import com.rd.veuisdk.demo.zishuo.adapter.ColorAdapter;
import com.rd.veuisdk.demo.zishuo.adapter.TextAdapter;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.manager.UIConfiguration;
import com.rd.veuisdk.ui.RdSeekBar;
import com.rd.veuisdk.utils.DateTimeUtils;

import java.util.ArrayList;

public class TextFragment extends BaseFragment implements View.OnClickListener {

    /**
     * 顶部菜单
     *      返回、完成、取消选择、全部选择
     */
    private ExtButton mBtnRight, mBtnLeft, mBtnChooseCancel, mBtnChooseAll;
    /**
     * 中间
     *      颜色和字体、透明度和粗细、seekbar值、seekbar
     *      adapter、字体
     */
    private RecyclerView mRvColor;
    private TextView mTvAlphaThickness, mTvValue;
    private LinearLayout mLlSeekbar;
    private RdSeekBar mSeekBar;
    private ColorAdapter mColorAdapter;
    //字体
    private GridView mGvFont;
    private TTFHandler mTTFHandler;
    private TTFHandler.ITTFHandlerListener mTTFListener;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    //文字
    private RecyclerView mRvText;
    private TextAdapter mTextAdapter;
    //播放进度 进度显示、进度条、播放暂停
    private TextView mTvTime;
    private RdSeekBar mSbEditor;
    private ImageView mBtnPlay;
    private String mDuration;
    /**
     * 底部
     */
    private ExtTextView mBtnFont, mBtnStrok, mBtnShadow;
    //
    private IVideoEditorHandler mVideoEditorHandler;
    private IVideoEditorHandler.EditorPreivewPositionListener mListener;
    private ArrayList<TextNode> mTextNodes = new ArrayList<>();
    //状态 1字体状态 2 描边状态 3阴影 4颜色设置
    private int mState = 0;
    //值 阴影、粗细
    private int mAlpha = 0;
    private int mThickness = 0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mVideoEditorHandler = (IVideoEditorHandler) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_text, container, false);
        initView();
        init();
        return mRoot;
    }

    private void initView() {
        //顶部
        mBtnRight = $(R.id.btnRight);
        mBtnLeft = $(R.id.btnLeft);
        mBtnChooseCancel = $(R.id.btn_choose_cancel);
        mBtnChooseAll = $(R.id.btn_choose_all);
        //底部
        mBtnFont = $(R.id.rb_font);
        mBtnStrok = $(R.id.rb_strok);
        mBtnShadow = $(R.id.rb_shadow);
        //中间
        mRvColor = $(R.id.rv_color);
        mTvAlphaThickness = $(R.id.tv_alpha_thickness);
        mTvValue = $(R.id.tv_value);
        mLlSeekbar = $(R.id.ll_seekbar);
        mSeekBar = $(R.id.sb_alpha_thickness);
        mGvFont = $(R.id.gridview_font);
        mRvText = $(R.id.rv_text);
        mTvTime = $(R.id.tv_time);
        mSbEditor = $(R.id.sb_editor);
        mBtnPlay = $(R.id.btn_play);

        mBtnRight.setOnClickListener(this);
        mBtnLeft.setOnClickListener(this);
        mBtnChooseCancel.setOnClickListener(this);
        mBtnChooseAll.setOnClickListener(this);
        mBtnFont.setOnClickListener(this);
        mBtnStrok.setOnClickListener(this);
        mBtnShadow.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);

        //透明度、粗细
        mSeekBar.setMax(100);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mTvValue.setText(progress + "%");
                    if (mState == 2) {
                        mThickness = progress;
                        mTextAdapter.setStrokeWidth((mThickness + 0.0f) / 100);
                    } else if (mState == 3) {
                        mAlpha = progress;
                        mTextAdapter.setShadowAlpha((mAlpha + 0.0f) / 100);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //时间
        mSbEditor.setMax(mVideoEditorHandler.getDuration());
        mDuration = getFormatTime(mVideoEditorHandler.getDuration());
        mTvTime.setText(getFormatTime(0) + "/" + mDuration);
        mSbEditor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mVideoEditorHandler.seekTo(progress);
                    mTvTime.setText(getFormatTime(progress) + "/" + mDuration);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mVideoEditorHandler.isPlaying()) {
                    pause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void init() {
        //获取进度
        mListener = new IVideoEditorHandler.EditorPreivewPositionListener() {
            @Override
            public void onEditorPrepred() {

            }

            @Override
            public void onEditorGetPosition(int nPosition, int nDuration) {
                mTvTime.setText(getFormatTime(nPosition) + "/" + mDuration);
                mSbEditor.setProgress(nPosition);
            }

            @Override
            public void onEditorPreviewComplete() {
                pause();
            }
        };
        mVideoEditorHandler.registerEditorPostionListener(mListener);
        //颜色recycler
        mColorAdapter = new ColorAdapter(getContext(), 0);
        mRvColor.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRvColor.setAdapter(mColorAdapter);
        mColorAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, Object item) {
                if (position != -1) {
                    mColorAdapter.setChecked(position);
                    if (mState == 4) {
                        //字体颜色
                        mTextAdapter.setColor(String.valueOf(item));
                    } else if (mState == 2) {
                        //描边颜色
                        mTextAdapter.setStrokeColor(String.valueOf(item));
                    }
                }
            }
        });
        //字体
        mTTFListener = new TTFHandler.ITTFHandlerListener() {
            @Override
            public void onItemClick(String mGvTTF, int position) {
                if (mGvTTF.equals(getString(R.string.default_ttf))) {
                    mTextAdapter.setFont(null);
                } else {
                    mTextAdapter.setFont(mGvTTF);
                }
            }
        };
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String path = intent.getStringExtra(TTFAdapter.TTF_ITEM);
                if (!TextUtils.isEmpty(path)) {
                    mTextAdapter.setFont(path);
                }
            }

        };
        mIntentFilter = new IntentFilter(TTFAdapter.ACTION_TTF);
        getContext().registerReceiver(mReceiver, mIntentFilter);
        UIConfiguration mUIConfig = SdkEntry.getSdkService().getUIConfig();
        mTTFHandler = new TTFHandler(mGvFont, mTTFListener, false, (null != mUIConfig ? mUIConfig.fontUrl : null));
        //获取数据
        ArrayList<TextNode> textNodes = TempZishuoParams.getInstance().getTextNodes();
        mTextNodes.clear();
        for (TextNode textNode : textNodes) {
            mTextNodes.add(textNode.clone());
        }
        mTextAdapter = new TextAdapter(getContext(), mTextNodes);
        mTextAdapter.setListener(new TextAdapter.StateListener() {
            @Override
            public void onState(int state) {
                setState(state);
            }
        });
        if (mTextNodes.size() > 0) {
            mThickness = (int) (mTextNodes.get(0).getStrokeWidth() * 100);
            float alpha = mTextNodes.get(0).getShadowAlpha();
            alpha = alpha == 0? 1 : alpha;
            mAlpha = (int) (alpha * 100);

            mTextAdapter.setShadowAlpha(alpha);
            mTextAdapter.setFont(mTextNodes.get(0).getFont());
            mTextAdapter.setStrokeWidth(mTextNodes.get(0).getStrokeWidth());
            mTextAdapter.setStrokeColor(mTextNodes.get(0).getStrokeColor());
        }
        mRvText.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRvText.setAdapter(mTextAdapter);
        mTextAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, Object item) {
                if (mTextAdapter.getChoose().size() > 0) {
                    setState(4);
                } else {
                    setState(0);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnLeft) {
            //返回
            onBackPressed();
        } else if (id == R.id.btnRight) {
            //完成
            //保存修改
            mTextAdapter.save();
            mVideoEditorHandler.onSure();
        } else if (id == R.id.btn_choose_cancel) {
            //取消选择
            setState(0);
        } else if (id == R.id.btn_choose_all) {
            //选择所有
            mTextAdapter.chooseAll();
        } else if (id == R.id.rb_font) {
            //字体
            setState(1);
        } else if (id == R.id.rb_strok) {
            //描边
            setState(2);
        } else if (id == R.id.rb_shadow) {
            //阴影
            setState(3);
        } else if (id == R.id.btn_play) {
            //播放
            if (mVideoEditorHandler.isPlaying()) {
                pause();
            } else {
                play();
            }
        }
    }

    public void setState(int state) {
        mTextAdapter.setState(state);
        if (state == mState) {
            return;
        }
        mState = state;
        if (state == 1) {
            mTextAdapter.cancelChoose();
            mGvFont.setVisibility(View.VISIBLE);
            mRvColor.setVisibility(View.GONE);
            mLlSeekbar.setVisibility(View.GONE);
            //选择和取消选择
            mBtnLeft.setVisibility(View.VISIBLE);
            mBtnRight.setVisibility(View.VISIBLE);
            mBtnChooseAll.setVisibility(View.GONE);
            mBtnChooseCancel.setVisibility(View.GONE);
        } else if (state == 2) {
            mTextAdapter.cancelChoose();
            mGvFont.setVisibility(View.GONE);
            mRvColor.setVisibility(View.VISIBLE);
            mLlSeekbar.setVisibility(View.VISIBLE);
            //设置初始值
            mSeekBar.setProgress(mThickness);
            mTvValue.setText(mThickness + "%");
            mTvAlphaThickness.setText(getString(R.string.subtitle_thin));
            //选择和取消选择
            mBtnLeft.setVisibility(View.VISIBLE);
            mBtnRight.setVisibility(View.VISIBLE);
            mBtnChooseAll.setVisibility(View.GONE);
            mBtnChooseCancel.setVisibility(View.GONE);
        } else if (state == 3) {
            mTextAdapter.cancelChoose();
            //阴影
            mGvFont.setVisibility(View.GONE);
            mRvColor.setVisibility(View.GONE);
            mLlSeekbar.setVisibility(View.VISIBLE);
            //设置初始值
            mSeekBar.setProgress(mAlpha);
            mTvValue.setText(mAlpha + "%");
            mTvAlphaThickness.setText(getString(R.string.subtitle_alpha));
            //选择和取消选择
            mBtnLeft.setVisibility(View.VISIBLE);
            mBtnRight.setVisibility(View.VISIBLE);
            mBtnChooseAll.setVisibility(View.GONE);
            mBtnChooseCancel.setVisibility(View.GONE);
        } else if (state == 4) {
            //颜色设置
            mGvFont.setVisibility(View.GONE);
            mRvColor.setVisibility(View.VISIBLE);
            mLlSeekbar.setVisibility(View.GONE);
            //选择和取消选择
            mBtnLeft.setVisibility(View.GONE);
            mBtnRight.setVisibility(View.GONE);
            mBtnChooseAll.setVisibility(View.VISIBLE);
            mBtnChooseCancel.setVisibility(View.VISIBLE);
        } else {
            mTextAdapter.cancelChoose();
            mGvFont.setVisibility(View.GONE);
            mRvColor.setVisibility(View.GONE);
            mLlSeekbar.setVisibility(View.GONE);
            //选择和取消选择
            mBtnLeft.setVisibility(View.VISIBLE);
            mBtnRight.setVisibility(View.VISIBLE);
            mBtnChooseAll.setVisibility(View.GONE);
            mBtnChooseCancel.setVisibility(View.GONE);
        }
    }

    @Override
    public int onBackPressed() {
        //如果选择就取消选择 否则 返回
        if (mState != 0) {
            setState(0);
        } else {
            mVideoEditorHandler.onBack();
        }
        return super.onBackPressed();
    }

    private String getFormatTime(int msec) {
        return DateTimeUtils.stringForMillisecondTime(msec, true, true);
    }

    private void play() {
        mVideoEditorHandler.start();
        mBtnPlay.setBackgroundResource(R.drawable.btn_edit_pause);
    }

    private void pause() {
        mVideoEditorHandler.pause();
        mBtnPlay.setBackgroundResource(R.drawable.btn_edit_play);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoEditorHandler.unregisterEditorProgressListener(mListener);
        getContext().unregisterReceiver(mReceiver);
        mTTFHandler.onDestory();
        mTextNodes.clear();
        mState = 0;
    }

}
