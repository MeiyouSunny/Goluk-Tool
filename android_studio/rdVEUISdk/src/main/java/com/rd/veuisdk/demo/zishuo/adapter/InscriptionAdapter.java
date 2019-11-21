package com.rd.veuisdk.demo.zishuo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.BaseRVAdapter;
import com.rd.veuisdk.demo.zishuo.InscriptionFragment;

import java.util.ArrayList;

/**
 * 题词
 */
public class InscriptionAdapter extends BaseRVAdapter<InscriptionAdapter.ViewHolder> {

    private ArrayList<InscriptionFragment.Inscription> mInscriptions;

    public InscriptionAdapter(Context context, ArrayList<InscriptionFragment.Inscription> inscriptions) {
        this.mInscriptions = inscriptions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_inscription, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final InscriptionFragment.Inscription inscription = mInscriptions.get(position);
        holder.mTvTitle.setText(inscription.getTitle());
        holder.mTvPeople.setText(inscription.getNum() + "人使用");
        holder.mTvContent.setText(inscription.getContent().get(0)+ "\n" + inscription.getContent().get(1) + "...");
        holder.mBtnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(-1, inscription);
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(-2, inscription);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mInscriptions.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTvTitle;
        TextView mTvContent;
        TextView mTvPeople;
        Button mBtnUse;//使用

        public ViewHolder(View itemView) {
            super(itemView);
            mTvTitle = itemView.findViewById(R.id.tv_title);
            mTvContent = itemView.findViewById(R.id.tv_content);
            mTvPeople = itemView.findViewById(R.id.tv_people);
            mBtnUse = itemView.findViewById(R.id.btn_use);
        }
    }

}
