package com.goluk.videoedit.adapter;

import com.goluk.videoedit.R;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AEMusicAdapter extends RecyclerView.Adapter<ViewHolder>{

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public void onBindViewHolder(ViewHolder arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
		LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
		View view = inflater.inflate(R.layout.ae_music_item, viewGroup, false);
		return new MusicViewHolder(view);
	}

	public class MusicViewHolder extends RecyclerView.ViewHolder{

		ImageView mAEMusicIv;
		TextView mAEMusicTv;
		public MusicViewHolder(View itemView) {
			super(itemView);
			// TODO Auto-generated constructor stub
			mAEMusicIv = (ImageView) itemView.findViewById(R.id.iv_ae_music_item);
			mAEMusicTv = (TextView) itemView.findViewById(R.id.tv_ae_music_item);
		}
		
	}

}
