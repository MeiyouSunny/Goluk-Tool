package com.mobnote.golukmain.livevideo.livecomment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.comment.CommentBean;
import com.mobnote.golukmain.comment.bean.CommentDataBean;
import com.mobnote.golukmain.comment.bean.CommentItemBean;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;
import com.mobnote.util.GolukUtils;
import com.rockerhieu.emojicon.EmojiconTextView;

import java.util.List;

/**
 * 直播页面评论adapter
 * Created by leege100 on 2016/7/26.
 */
public class LiveCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<CommentItemBean> mCommentList;
    private Context mContext;
    private OnReplySelectedListener mReplySelectedListener;
    private OnCommentItemLongClickListener mCommentItemLongClickListener;

    public LiveCommentAdapter(Context cxt,List<CommentItemBean> list,OnReplySelectedListener onReplySelectedListener,
                              OnCommentItemLongClickListener onCommentLongClickListener){
        this.mContext = cxt;
        this.mCommentList = list;
        this.mReplySelectedListener = onReplySelectedListener;
        this.mCommentItemLongClickListener = onCommentLongClickListener;
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
                liveCommentViewHolder.bindView(position);
            }
        }
    }

    public void appendData(List<CommentBean> list){

    }
    @Override
    public int getItemCount() {
        if(mCommentList == null){
            return 0;
        }else{
            return mCommentList.size();
        }
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
            mUserAvatarIv = (ImageView)itemView.findViewById(R.id.iv_live_comment_item_avatar);
            mUserAuthIv = (ImageView)itemView.findViewById(R.id.iv_live_comment_item_authentication);
            mUserNicknameTv = (TextView)itemView.findViewById(R.id.tv_live_comment_item_nickname);
            mCommentContentEmojTv = (EmojiconTextView) itemView.findViewById(R.id.emojtv_live_comment_item_content);
        }

        public void bindView(final int position) {
            if (mCommentList != null && mCommentList.size() > position ) {
                final CommentItemBean commentItemBean = mCommentList.get(position);
                if(commentItemBean == null){
                    return;
                }
                // 设置头像
                String netHeadUrl = commentItemBean.author.customavatar;
                if (null != netHeadUrl && !"".equals(netHeadUrl)) {
                    // 使用网络地址
                    GlideUtils.loadNetHead(mContext, mUserAvatarIv, netHeadUrl, R.drawable.head_unknown);
                } else {
                    // 使用本地头像
                    GlideUtils.loadLocalHead(mContext, mUserAvatarIv, UserUtils.getUserHeadImageResourceId(commentItemBean.author.avatar));
                }
                mUserAuthIv.setVisibility(View.VISIBLE);
                if ("1".equals(commentItemBean.author.label.approvelabel)) {
                    mUserAuthIv.setImageResource(R.drawable.authentication_bluev_icon);
                } else if ("1".equals(commentItemBean.author.label.headplusv)) {
                    mUserAuthIv.setImageResource(R.drawable.authentication_yellowv_icon);
                } else if ("1".equals(commentItemBean.author.label.tarento)) {
                    mUserAuthIv.setImageResource(R.drawable.authentication_star_icon);
                } else {
                    mUserAuthIv.setVisibility(View.GONE);
                }

                // 设置名称
                mUserNicknameTv.setText(commentItemBean.author.name);
                // 设置评论内容
                if (!TextUtils.isEmpty(commentItemBean.reply.name)) {
                    UserUtils.showText(mContext,mCommentContentEmojTv, commentItemBean.reply.name, commentItemBean.text);
                } else {
                    mCommentContentEmojTv.setText( commentItemBean.text);
                }
                mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(commentItemBean == null || commentItemBean.author == null){
                            return;
                        }
                        String authorName = commentItemBean.author.name;
                        String authorId = commentItemBean.author.authorid;
                        if(TextUtils.isEmpty(authorId)){
                            return;
                        }
                        if(mReplySelectedListener != null){
                            mReplySelectedListener.onReplySelected(commentItemBean.commentId,authorId,authorName);
                        }
                    }});
                mItemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(commentItemBean.author != null && GolukUtils.isLoginUser(commentItemBean.author.authorid)){
                            if(mCommentItemLongClickListener != null){
                                mCommentItemLongClickListener.onCommentLongClicked(commentItemBean.commentId);
                            }
                        }
                        return true;
                    }
                });
            }
        }
    }
    public interface OnReplySelectedListener{
        public void onReplySelected(String replyId,String replyAuthorId,String replyAuthorName);
    }
    public interface OnCommentItemLongClickListener{
        public void onCommentLongClicked(String commentId);
    }
}
