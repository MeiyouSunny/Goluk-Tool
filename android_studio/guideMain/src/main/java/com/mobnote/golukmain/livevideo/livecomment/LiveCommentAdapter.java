package com.mobnote.golukmain.livevideo.livecomment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.comment.CommentBean;
import com.mobnote.golukmain.comment.bean.CommentDataBean;
import com.mobnote.golukmain.comment.bean.CommentItemBean;
import com.rockerhieu.emojicon.EmojiconTextView;

import java.util.List;

/**
 * 直播页面评论adapter
 * Created by leege100 on 2016/7/26.
 */
public class LiveCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<CommentItemBean> mCommentList;
    private Context mContext;

    public LiveCommentAdapter(Context cxt,List<CommentItemBean> list){
        this.mContext = cxt;
        this.mCommentList = list;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.live_comment_item, parent, false);
        return new LiveCommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder != null){
            if(holder instanceof LiveCommentViewHolder){
                LiveCommentViewHolder liveCommentViewHolder = (LiveCommentViewHolder) holder;
                liveCommentViewHolder.bindView(0);
            }
        }
    }

    public void appendData(List<CommentBean> list){

    }
    @Override
    public int getItemCount() {
        return 0;
    }

    public class LiveCommentViewHolder extends RecyclerView.ViewHolder {
        private View mItemView;
        private ImageView mUserAvatarIv;
        private ImageView mUserAuthIv;
        private TextView mUserNicknameTv;
        private EmojiconTextView mCommentContentEmojTv;

        public LiveCommentViewHolder(View itemView) {
            super(itemView);

            this.mItemView = itemView;
            mUserAvatarIv = (ImageView)itemView.findViewById(R.id.iv_live_comment_item_authentication);
            mUserAuthIv = (ImageView)itemView.findViewById(R.id.iv_live_comment_item_authentication);
            mUserNicknameTv = (TextView)itemView.findViewById(R.id.tv_live_comment_item_nickname);
            mCommentContentEmojTv = (EmojiconTextView) itemView.findViewById(R.id.emojtv_live_comment_item_content);
        }

        public void bindView(final int position) {
            if (mCommentList != null && mCommentList.size() > position ) {
                mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }});
            }
        }
    }
}
