package com.mobnote.golukmain.startshare;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobnote.eventbus.EventSharetypeSelected;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.startshare.bean.ShareTypeBean;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by leege100 on 16/5/17.
 */
public class ShareTypeAdapter extends BaseAdapter{

    private Context mContext;
    private int mCurrentSeletedType;
    private List<ShareTypeBean> mShareTypeBeanList;
    private LayoutInflater mInflater;

    public ShareTypeAdapter(Context cxt,int type){
        this.mContext = cxt;
        this.mCurrentSeletedType = type;
        mInflater = LayoutInflater.from(mContext);
        fillList();
    }

    public int getmCurrentSeletedType() {
        return mCurrentSeletedType;
    }

    public void setmCurrentSeletedType(int mCurrentSeletedType) {
        this.mCurrentSeletedType = mCurrentSeletedType;
    }

    private void fillList(){
        mShareTypeBeanList = new ArrayList<ShareTypeBean>();
        mShareTypeBeanList.add(new ShareTypeBean(ShareTypeBean.SHARE_TYPE_SSP));
        mShareTypeBeanList.add(new ShareTypeBean(ShareTypeBean.SHARE_TYPE_BGT));
        mShareTypeBeanList.add(new ShareTypeBean(ShareTypeBean.SHARE_TYPE_SGBL));
        mShareTypeBeanList.add(new ShareTypeBean(ShareTypeBean.SHARE_TYPE_MLFJ));
    }
    @Override
    public int getCount() {
        return mShareTypeBeanList == null ? 0 : mShareTypeBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        ShareTypeViewHolder shareTypeViewHolder = null;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.item_sharetype, null);
            shareTypeViewHolder = new ShareTypeViewHolder(convertView);
            convertView.setTag(shareTypeViewHolder);
        } else {
            shareTypeViewHolder = (ShareTypeViewHolder)convertView.getTag();
        }

        if (position < 0 || position >= mShareTypeBeanList.size()) {
            return convertView;
        }

        shareTypeViewHolder.render(position);
        return convertView;
    }

    private class ShareTypeViewHolder{
        private View convertView;
        private TextView tvShareTypeTitle;
        private LinearLayout llShareTypeItem;
        public ShareTypeViewHolder(View view){
            this.convertView = view;
            tvShareTypeTitle = (TextView) convertView.findViewById(R.id.tv_sharetype_item);
            llShareTypeItem = (LinearLayout) convertView.findViewById(R.id.ll_sharetype_item);
        }
        public void render(int position){
            if(mShareTypeBeanList != null && mShareTypeBeanList.size() > position){
                ShareTypeBean shareTypeBean = mShareTypeBeanList.get(position);
                if(shareTypeBean !=  null){
                    tvShareTypeTitle.setVisibility(View.VISIBLE);
                    llShareTypeItem.setVisibility(View.VISIBLE);
                    if(shareTypeBean.getShareType() == mCurrentSeletedType){

                        tvShareTypeTitle.setTextColor(Color.parseColor("#0080ff"));
                        llShareTypeItem.setBackgroundResource(R.drawable.share_promotion_frame_selected);
                    }else{
                        tvShareTypeTitle.setTextColor(Color.parseColor("#808080"));
                        llShareTypeItem.setBackgroundResource(R.drawable.share_promotion_frame);
                    }

                    String sharetypeName = null;
                    if(shareTypeBean.getShareType() == ShareTypeBean.SHARE_TYPE_SSP){
                        sharetypeName =  mContext.getResources().getString(R.string.share_str_type_ssp);
                    }else if(shareTypeBean.getShareType() == ShareTypeBean.SHARE_TYPE_BGT){
                        sharetypeName =  mContext.getResources().getString(R.string.share_str_type_bg);
                    }else if(shareTypeBean.getShareType() == ShareTypeBean.SHARE_TYPE_MLFJ){
                        sharetypeName =  mContext.getResources().getString(R.string.share_str_type_ml);
                    }else if(shareTypeBean.getShareType() == ShareTypeBean.SHARE_TYPE_SGBL) {
                        sharetypeName =  mContext.getResources().getString(R.string.share_str_type_sg);
                    }

                    tvShareTypeTitle.setText("# " + sharetypeName);
                    SharetypeItemClickListener clickListener = new SharetypeItemClickListener(shareTypeBean.getShareType(),sharetypeName,position);
                    convertView.setOnClickListener(clickListener);
                }else{
                    tvShareTypeTitle.setVisibility(View.GONE);
                    llShareTypeItem.setVisibility(View.GONE);
                }
            }
        }
    }
    private class SharetypeItemClickListener implements View.OnClickListener{

        private int mShareType;
        private String mShareName;
        int position;
        public SharetypeItemClickListener(int shareType,String shareName,int p){
            this.mShareName = shareName;
            this.mShareType = shareType;
            this.position = p;
        }

        @Override
        public void onClick(View view) {
            if(mShareTypeBeanList != null && mShareTypeBeanList.size() > position){
                ShareTypeBean tempBean = mShareTypeBeanList.get(position);
                if(tempBean != null){

                    if(tempBean.getShareType() != mCurrentSeletedType){
                        mCurrentSeletedType = tempBean.getShareType();
                    }else{
                        mCurrentSeletedType = ShareTypeBean.SHARE_TYPE_SSP;
                    }
                    ShareTypeAdapter.this.notifyDataSetChanged();
                    EventBus.getDefault().post(new EventSharetypeSelected(mShareName,mShareType));
                }
            }
        }
    }
}
