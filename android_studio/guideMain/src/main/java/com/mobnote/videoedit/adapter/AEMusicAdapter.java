package com.mobnote.videoedit.adapter;

import java.util.ArrayList;
import java.util.List;

import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.exceptions.InvalidVideoSourceException;

import android.graphics.Color;
import android.os.Environment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.videoedit.bean.AEMusic;
import com.mobnote.videoedit.AfterEffectActivity;

public class AEMusicAdapter extends RecyclerView.Adapter<ViewHolder> {
    int mCurrSelectedIndex = 0;
    AfterEffectActivity mActivity;
    List<AEMusic> mAEMusicList;
    AfterEffect mAfterEffect;


    public AEMusicAdapter(AfterEffectActivity act, AfterEffect effect) {
        this.mActivity = act;
        mCurrSelectedIndex = 0;
        mAfterEffect = effect;
    }

    public int getSelectedIndex() {
        return mCurrSelectedIndex;
    }

    public void fillupMusicList(String[] musicPaths, String[] musicNames, int[] coverNormal, int[] coverSelected) {
        mAEMusicList = new ArrayList<AEMusic>();
        for (int i = 0; i < musicPaths.length; i++) {
            String destPath = Environment.getExternalStorageDirectory() + "/" + musicPaths[i];
            mAEMusicList.add(new AEMusic(musicNames[i], destPath, false, coverNormal[i], coverSelected[i]));
        }
    }

    @Override
    public int getItemCount() {
        if (mAEMusicList != null) {
            return mAEMusicList.size();
        }
        return 0;
    }

    @Override
    public void onBindViewHolder(ViewHolder vHolder, int position) {
        if (vHolder instanceof MusicViewHolder) {
            MusicViewHolder mViewHolder = (MusicViewHolder) vHolder;
            mViewHolder.bindView(position);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.ae_music_item, viewGroup, false);
        return new MusicViewHolder(view);
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        View mItemView;
        ImageView mAEMusicIV;
        TextView mAEMusicTV;

        public MusicViewHolder(View itemView) {
            super(itemView);

            this.mItemView = itemView;
            mAEMusicIV = (ImageView)itemView.findViewById(R.id.iv_ae_music_item);
            mAEMusicTV = (TextView)itemView.findViewById(R.id.tv_ae_music_item);
        }

        public void bindView(final int position) {
            if (mAEMusicList != null && mAEMusicList.size() > position
                    && mAEMusicList.size() > mCurrSelectedIndex) {

                AEMusic aeMusic = mAEMusicList.get(position);
                if (aeMusic.isSelected()) {
                    mAEMusicTV.setTextColor(Color.parseColor("#ffffff"));
                } else {
                    mAEMusicTV.setTextColor(Color.parseColor("#88ffffff"));
                }
                mAEMusicTV.setText(aeMusic.getMusicName());
                if(aeMusic.isSelected()) {
                    mAEMusicIV.setImageResource(aeMusic.getMusicCoverSelected());
                } else {
                    mAEMusicIV.setImageResource(aeMusic.getMusicCoverNormal());
                }

                mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(1 == mActivity.needMusicMoreScroll(position)) {
                            mActivity.moreMusicScrollRight();
                        } else if(-1 == mActivity.needMusicMoreScroll(position)) {
                            mActivity.moreMusicScrollLeft();
                        } else {
                            // 0, do nothing
                        }

                        if (mCurrSelectedIndex != position) {
                            if(mCurrSelectedIndex != -1) {
                                AEMusic preSeletedMusic = mAEMusicList.get(mCurrSelectedIndex);
                                preSeletedMusic.setSelected(false);
                                notifyItemChanged(mCurrSelectedIndex);
                            }

                            AEMusic newSelectedMusic = mAEMusicList.get(position);
                            newSelectedMusic.setSelected(true);
                            notifyItemChanged(position);
                            mCurrSelectedIndex = position;
                            String destPath = null;
                            if (0 != mCurrSelectedIndex) {
                                destPath = mAEMusicList.get(mCurrSelectedIndex).getMusicPath();
                            }

                            try {
                                mAfterEffect.replayWithMusic(destPath);
                            } catch (InvalidVideoSourceException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }
}
