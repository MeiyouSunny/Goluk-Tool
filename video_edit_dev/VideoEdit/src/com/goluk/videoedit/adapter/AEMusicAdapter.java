package com.goluk.videoedit.adapter;

import java.util.ArrayList;
import java.util.List;

import com.goluk.videoedit.R;
import com.goluk.videoedit.bean.AEMusic;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AEMusicAdapter extends RecyclerView.Adapter<ViewHolder> {
	int mCurrSelectedIndex;
	Context mContext;
	List<AEMusic> mAEMusicList;

	public AEMusicAdapter(Context cxt) {
		this.mContext = cxt;
		mCurrSelectedIndex = 0;
		fillupMusicList();
	}

	private void fillupMusicList() {
		mAEMusicList = new ArrayList<AEMusic>();
		mAEMusicList.add(new AEMusic("无", "", true));
		mAEMusicList.add(new AEMusic("Dreamer", "", false));
		mAEMusicList.add(new AEMusic("Champions", "", false));
		mAEMusicList.add(new AEMusic("HollyWood", "", false));
		mAEMusicList.add(new AEMusic("PretyMood", "", false));
		mAEMusicList.add(new AEMusic("Yongth", "", false));
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
		ImageView mAEMusicIv;
		TextView mAEMusicTv;

		public MusicViewHolder(View itemView) {
			super(itemView);
			// TODO Auto-generated constructor stub
			this.mItemView = itemView;
			mAEMusicIv = (ImageView) itemView
					.findViewById(R.id.iv_ae_music_item);
			mAEMusicTv = (TextView) itemView
					.findViewById(R.id.tv_ae_music_item);
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
					mAEMusicIv.setImageDrawable(mContext.getResources()
							.getDrawable(R.drawable.no_music));
				} else {
					if (aeMusic.isSelected()) {
						mAEMusicIv.setImageDrawable(mContext.getResources()
								.getDrawable(R.drawable.ic_ae_cd_selected));
					} else {
						mAEMusicIv.setImageDrawable(mContext.getResources()
								.getDrawable(R.drawable.ic_ae_cd_unselected));
					}
				}
				mItemView.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (mCurrSelectedIndex != position) {
							AEMusic preSeletedMusic = mAEMusicList
									.get(mCurrSelectedIndex);
							preSeletedMusic.setSelected(false);
							mAEMusicList.set(mCurrSelectedIndex,
									preSeletedMusic);
							notifyItemChanged(mCurrSelectedIndex);

							AEMusic newSelectedMusic = mAEMusicList
									.get(position);
							newSelectedMusic.setSelected(true);
							mAEMusicList.set(position, newSelectedMusic);
							notifyItemChanged(position);
							mCurrSelectedIndex = position;
						}
					}
				});
			}
		}
	}

}
