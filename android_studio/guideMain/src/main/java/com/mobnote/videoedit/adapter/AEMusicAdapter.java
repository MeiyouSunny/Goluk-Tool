package com.mobnote.videoedit.adapter;

import java.util.ArrayList;
import java.util.List;

import cn.npnt.ae.AfterEffect;
import cn.npnt.ae.exceptions.InvalidVideoSourceException;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.videoedit.bean.AEMusic;
import com.mobnote.videoedit.AfterEffectActivity;

public class AEMusicAdapter extends RecyclerView.Adapter<ViewHolder> {
	int mCurrSelectedIndex = -1;
	AfterEffectActivity mActivity;
	List<AEMusic> mAEMusicList;
	AfterEffect mAfterEffect;

	public AEMusicAdapter(AfterEffectActivity act, AfterEffect effect) {
		this.mActivity = act;
		mCurrSelectedIndex = 0;
		mAfterEffect = effect;
//		fillupMusicList();
	}

	public int getSelectedIndex() {
		return mCurrSelectedIndex;
	}

	public void fillupMusicList(String[] musicPaths, String[] musicNames) {
		mAEMusicList = new ArrayList<AEMusic>();
		for(int i = 0; i < musicPaths.length; i++) {
			String destPath = Environment.getExternalStorageDirectory() + "/" + musicPaths[i];
			mAEMusicList.add(new AEMusic(musicNames[i], destPath, false));
		}
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		if (mAEMusicList != null) {
			return mAEMusicList.size();
		}
		return 0;
	}

	@Override
	public void onBindViewHolder(ViewHolder vHolder, int position) {
		// TODO Auto-generated method stub

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
		TextView mAEMusicTv;

		public MusicViewHolder(View itemView) {
			super(itemView);
			// TODO Auto-generated constructor stub
			this.mItemView = itemView;
			mAEMusicIV = (ImageView)itemView.findViewById(R.id.iv_ae_music_item);
			mAEMusicTv = (TextView)itemView.findViewById(R.id.tv_ae_music_item);
		}

		public void bindView(final int position) {
			if (mAEMusicList != null && mAEMusicList.size() > position
					&& mAEMusicList.size() > mCurrSelectedIndex) {

				AEMusic aeMusic = mAEMusicList.get(position);
				if (aeMusic.isSelected()) {
					mAEMusicTv.setTextColor(Color.parseColor("#ffffff"));
				} else {
					mAEMusicTv.setTextColor(Color.parseColor("#88ffffff"));
				}
				mAEMusicTv.setText(aeMusic.getMusicName());

				if (position == 0) {
					mAEMusicIV.setImageDrawable(mActivity.getResources()
							.getDrawable(R.drawable.no_music));
				} else {
                    switch (position) {
                        case 1:
                            if (aeMusic.isSelected()) {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_selected_wave));
                            } else {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_wave));
                            }
                            break;
                        case 2:
                            if (aeMusic.isSelected()) {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_selected_discover));
                            } else {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_discover));
                            }
                            break;
                        case 3:
                            if (aeMusic.isSelected()) {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_selected_humor));
                            } else {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_humor));
                            }
                            break;
                        case 4:
                            if (aeMusic.isSelected()) {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_selected_memory));
                            } else {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_memory));
                            }
                            break;
                        case 5:
                            if (aeMusic.isSelected()) {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_selected_crowd));
                            } else {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_crowd));
                            }
                            break;
                        case 6:
                            if (aeMusic.isSelected()) {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_selected_travel));
                            } else {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_travel));
                            }
                            break;
                        case 7:
                            if (aeMusic.isSelected()) {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_selected_fresh));
                            } else {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_fresh));
                            }
                            break;
                        case 8:
                            if (aeMusic.isSelected()) {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_selected_wild));
                            } else {
                                mAEMusicIV.setImageDrawable(mActivity.getResources()
                                        .getDrawable(R.drawable.music_wild));
                            }
                            break;
                        default:
                            break;
                    }
				}

				mItemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
					// TODO Auto-generated method stub
					if (mCurrSelectedIndex != position) {
						AEMusic preSeletedMusic = mAEMusicList.get(mCurrSelectedIndex);
						preSeletedMusic.setSelected(false);
						mAEMusicList.set(mCurrSelectedIndex, preSeletedMusic);
						notifyItemChanged(mCurrSelectedIndex);

						AEMusic newSelectedMusic = mAEMusicList.get(position);
						newSelectedMusic.setSelected(true);
						mAEMusicList.set(position, newSelectedMusic);
						notifyItemChanged(position);
						mCurrSelectedIndex = position;
						String destPath = null;
						if(0 != mCurrSelectedIndex) {
							destPath = mAEMusicList.get(mCurrSelectedIndex).getMusicPath();
						}

						try {
							mAfterEffect.editBackgroundMusic(destPath);
							mActivity.seekWith(0);
                            mAfterEffect.play();
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
