package com.rd.veuisdk.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.vecore.models.MusicFilterType;
import com.rd.veuisdk.IVideoMusicEditor;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.BaseRVAdapter;
import com.rd.veuisdk.listener.OnItemClickListener;
import com.rd.veuisdk.model.MusicEffectInfo;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;
import com.rd.veuisdk.utils.IParamHandler;
import com.rd.veuisdk.utils.SysAlertDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 变声
 */
public class MusicEffectFragment extends BaseFragment {

    public static MusicEffectFragment newInstance() {
        Bundle args = new Bundle();

        MusicEffectFragment fragment = new MusicEffectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private IParamHandler mParamHandler;
    private IMusicEffectCallBack mCallBack;
    private IVideoMusicEditor mVideoEditorHander;
    private TextView tvBottomTitle;
    private RecyclerView mRecyclerView;
    private MusicEffectAdapter mAdapter;
    private int mLastMusicFilterIndex = 1;
    private View mViewPitch, mViewEffect;
    private float mPitchBackup = 0;
    private SeekBar mSbar;
    private int mInitIndex = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public interface IMusicEffectCallBack {

        /**
         * 声音特效
         */
        void changeMusicFilter();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof IParamHandler) {
            mParamHandler = (IParamHandler) context;
        }
        if (context instanceof IMusicEffectCallBack) {
            mCallBack = (IMusicEffectCallBack) context;
        }
        if (context instanceof IVideoMusicEditor) {
            mVideoEditorHander = (IVideoMusicEditor) context;
        }
        mPitchBackup = mParamHandler.getParamData().getMusicPitch();
    }


    private boolean isPitchVisible() {
        return mViewPitch.getVisibility() == View.VISIBLE;
    }

    private void resetUI() {
        mViewPitch.setVisibility(View.GONE);
        mViewEffect.setVisibility(View.VISIBLE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_music_effect, container, false);
        tvBottomTitle = $(R.id.tvBottomTitle);
        tvBottomTitle.setText(R.string.sound_effect);
        mViewPitch = $(R.id.pitchParent);
        mViewEffect = $(R.id.effectParent);
        mRecyclerView = $(R.id.recyclerViewFilter);
        mSbar = $(R.id.sbarPitch);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new MusicEffectAdapter(getContext());
        mAdapter.setEnableRepeatClick(true);
        mAdapter.setOnItemClickListener(new OnItemClickListener<MusicEffectInfo>() {
            @Override
            public void onItemClick(int position, MusicEffectInfo musicEffectInfo) {
                mParamHandler.getParamData().setSoundEffectId(musicEffectInfo.getTypeId());
                if (position == 0) {
                    //自定义
                    mViewPitch.setVisibility(View.VISIBLE);
                    mViewEffect.setVisibility(View.GONE);
                } else {
                    mCallBack.changeMusicFilter();
                }
            }
        });
        //设置适配器
        mRecyclerView.setAdapter(mAdapter);
        ArrayList<MusicEffectInfo> mList = initData();
        if (null != mParamHandler) {
            int id = mParamHandler.getParamData().getSoundEffectId();
            mLastMusicFilterIndex = getCheckIndex(mList, id);
            mInitIndex = mLastMusicFilterIndex;
        }
        mAdapter.addData(mList, mLastMusicFilterIndex);

        return mRoot;

    }

    private float tmpPitch = Float.NaN;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        $(R.id.ivSure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnSure();
            }
        });
        $(R.id.ivCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        int progress = Float.isNaN(mPitchBackup) ? 0 : (int) (mPitchBackup * 100);
        mSbar.setProgress(progress);
        mSbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tmpPitch = progress / 100.0f;
                    mHandler.removeCallbacks(mUpdatePitch);
                    mHandler.postDelayed(mUpdatePitch, 200);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tmpPitch = seekBar.getProgress() / 100.0f;
                mHandler.removeCallbacks(mUpdatePitch);
                mHandler.post(mUpdatePitch);
            }
        });

    }

    private Runnable mUpdatePitch = new Runnable() {
        @Override
        public void run() {
            mVideoEditorHander.getEditorVideo().setMusicFilter(MusicFilterType.MUSIC_FILTER_CUSTOM, tmpPitch);
        }
    };


    /**
     * 保存
     */
    private void onBtnSure() {
        if (isPitchVisible()) {
            resetUI();
            //音调参数
            mPitchBackup = tmpPitch;
            mParamHandler.getParamData().setMusicPitch(tmpPitch);
            mVideoEditorHander.getEditorVideo().setMusicFilter(MusicFilterType.MUSIC_FILTER_CUSTOM, tmpPitch);
        } else {
            mVideoEditorHander.onSure();
        }
    }

    @Override
    public int onBackPressed() {
        if (isPitchVisible()) {
            resetUI();
            if (mPitchBackup > 0) {
                mVideoEditorHander.getEditorVideo().setMusicFilter(MusicFilterType.MUSIC_FILTER_CUSTOM, mPitchBackup);
            }
            return -1;
        } else {
            onShowAlert();
        }
        return super.onBackPressed();
    }

    private void onShowAlert() {
        SysAlertDialog.createAlertDialog(getContext(),
                getString(R.string.dialog_tips),
                getString(R.string.cancel_all_changed),
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }, getString(R.string.sure),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mAdapter.onItemChecked(mInitIndex);
                        mVideoEditorHander.onBack();
                    }
                }, false, null).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mUpdatePitch);
    }

    /**
     * 获取指定的音效下标
     */
    private int getCheckIndex(ArrayList<MusicEffectInfo> list, int id) {
        int index = 0;
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (list.get(i).getTypeId() == id) {
                index = i;
                break;
            }
        }
        return index;
    }

    private ArrayList<MusicEffectInfo> initData() {
        ArrayList<MusicEffectInfo> tmp = new ArrayList<>();
        String[] soundEffectArr = getResources().getStringArray(R.array.music_filter_titles);
        tmp.add(new MusicEffectInfo(soundEffectArr[0], R.drawable.music_effect_pitch, MusicFilterType.MUSIC_FILTER_CUSTOM.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[1], R.drawable.music_effect_0, MusicFilterType.MUSIC_FILTER_NORMAL.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[2], R.drawable.music_effect_1, MusicFilterType.MUSIC_FILTER_BOY.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[3], R.drawable.music_effect_2, MusicFilterType.MUSIC_FILTER_GIRL.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[4], R.drawable.music_effect_3, MusicFilterType.MUSIC_FILTER_MONSTER.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[5], R.drawable.music_effect_4, MusicFilterType.MUSIC_FILTER_CARTOON.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[6], R.drawable.music_effect_5, MusicFilterType.MUSIC_FILTER_REVERB.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[7], R.drawable.music_effect_6, MusicFilterType.MUSIC_FILTER_ECHO.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[8], R.drawable.music_effect_7, MusicFilterType.MUSIC_FILTER_ROOM.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[9], R.drawable.music_effect_8, MusicFilterType.MUSIC_FILTER_DANCE.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[10], R.drawable.music_effect_9, MusicFilterType.MUSIC_FILTER_KTV.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[11], R.drawable.music_effect_10, MusicFilterType.MUSIC_FILTER_FACTORY.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[12], R.drawable.music_effect_11, MusicFilterType.MUSIC_FILTER_ARENA.ordinal()));
        tmp.add(new MusicEffectInfo(soundEffectArr[13], R.drawable.music_effect_12, MusicFilterType.MUSIC_FILTER_ELECTRI.ordinal()));

        return tmp;
    }


    class MusicEffectAdapter extends BaseRVAdapter<MusicEffectAdapter.ViewHolder> {
        private LayoutInflater mLayoutInflater;
        private List<MusicEffectInfo> mList = new ArrayList<>();
        private int edColor, normalColor;

        public MusicEffectInfo getItem(int position) {
            return mList.get(position);
        }


        public void addData(ArrayList<MusicEffectInfo> list, int lastIndex) {
            mList.clear();
            if (null != list) {
                mList.addAll(list);
            }
            lastCheck = lastIndex;
            notifyDataSetChanged();
        }


        public MusicEffectAdapter(Context context) {
            edColor = ContextCompat.getColor(context, R.color.main_orange);
            normalColor = ContextCompat.getColor(context, R.color.transparent);
        }


        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (null == mLayoutInflater) {
                mLayoutInflater = LayoutInflater.from(parent.getContext());
            }
            View view = mLayoutInflater.inflate(R.layout.transiton_item_layout, parent, false);
            ViewClickListener viewClickListener = new ViewClickListener();
            view.setOnClickListener(viewClickListener);
            view.setTag(viewClickListener);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ViewClickListener viewClickListener = (ViewClickListener) holder.itemView.getTag();
            viewClickListener.setPosition(position);
            MusicEffectInfo info = getItem(position);


            if (lastCheck == position) {
                holder.mText.setChecked(true);
                SimpleDraweeViewUtils.setBorderColor(holder.mImageView, edColor);
            } else {
                holder.mText.setChecked(false);
                SimpleDraweeViewUtils.setBorderColor(holder.mImageView, normalColor);
            }
            SimpleDraweeViewUtils.setCover(holder.mImageView, info.getResId());
            holder.mText.setText(info.getText());


        }


        /***
         * 设置为选中状态
         * @param nItemId
         */
        public void onItemChecked(int nItemId) {
            lastCheck = nItemId;
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(lastCheck, mList.get(lastCheck));
            }
            notifyDataSetChanged();
        }


        @Override
        public int getItemCount() {
            return mList.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            CheckedTextView mText;
            SimpleDraweeView mImageView;

            ViewHolder(View itemView) {
                super(itemView);
                mText = $(itemView, R.id.transition_item_text);
                mImageView = $(itemView, R.id.transition_item_icon);
            }
        }

        class ViewClickListener extends BaseRVAdapter.BaseItemClickListener {
            @Override
            public void onClick(View v) {
                if (lastCheck != position || enableRepeatClick) {
                    lastCheck = position;
                    notifyDataSetChanged();
                    if (null != mOnItemClickListener) {
                        mOnItemClickListener.onItemClick(position, mList.get(position));
                    }
                }
            }
        }


    }


}
