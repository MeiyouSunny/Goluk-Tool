package com.mobnote.golukmain.livevideo.livecomment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.rockerhieu.emojicon.EmojiconEditText;

/**
 * Created by leege100 on 2016/7/20.
 */
public class LiveCommentFragment extends Fragment{

    String mVid;
    View mRootView;
    LinearLayout mCommentLikeAndEmojLayout;
    ImageView mEmojIconIv;
    LinearLayout mSendCommentAndLikeLayout;
    LinearLayout mLikeLayout;
    ImageView mLikeIv;
    TextView mLikeCountTv;
    TextView mSendCommentTv;
    EmojiconEditText mEmojiconEt;
    FrameLayout mEmojIconsLayout;
    RecyclerView mLiveCommentRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_live_comment,container,false);
        initView();
        return mRootView;
    }

    private void initView() {
        mCommentLikeAndEmojLayout = (LinearLayout) mRootView.findViewById(R.id.layout_comment_like_and_emoj);
        mEmojIconIv = (ImageView) mRootView.findViewById(R.id.iv_emojicon);
        mSendCommentAndLikeLayout = (LinearLayout) mRootView.findViewById(R.id.layout_comment_and_like);
        mLikeLayout = (LinearLayout) mRootView.findViewById(R.id.layout_like);
        mLikeIv = (ImageView) mRootView.findViewById(R.id.iv_like);
        mLikeCountTv = (TextView) mRootView.findViewById(R.id.tv_like_count);
        mSendCommentTv = (TextView) mRootView.findViewById(R.id.tv_send_comment);
        mEmojiconEt = (EmojiconEditText) mRootView.findViewById(R.id.et_comment_input);
        mEmojIconsLayout = (FrameLayout) mRootView.findViewById(R.id.layout_emoj_icons);
        mLiveCommentRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerview_live_comment);
    }
    public void setmVid(String vid){
        this.mVid = vid;
    }
}
