package com.mobnote.golukmain.livevideo.livecomment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.comment.CommentAddRequest;
import com.mobnote.golukmain.comment.CommentDeleteRequest;
import com.mobnote.golukmain.comment.CommentListRequest;
import com.mobnote.golukmain.comment.ICommentFn;
import com.mobnote.golukmain.comment.bean.AuthorBean;
import com.mobnote.golukmain.comment.bean.CommentAddBean;
import com.mobnote.golukmain.comment.bean.CommentAddResultBean;
import com.mobnote.golukmain.comment.bean.CommentDelResultBean;
import com.mobnote.golukmain.comment.bean.CommentItemBean;
import com.mobnote.golukmain.comment.bean.CommentResultBean;
import com.mobnote.golukmain.comment.bean.ReplyBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.livevideo.DelCommentDialog;
import com.mobnote.golukmain.livevideo.ILiveUIChangeListener;
import com.mobnote.golukmain.praise.PraiseRequest;
import com.mobnote.golukmain.praise.bean.PraiseResultBean;
import com.mobnote.golukmain.praise.bean.PraiseResultDataBean;
import com.mobnote.golukmain.videodetail.SoftKeyBoardListener;
import com.mobnote.util.GolukUtils;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import java.util.ArrayList;
import java.util.List;

import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;

/**
 * 直播评论fragment
 * Created by leege100 on 2016/7/20.
 */
public class LiveCommentFragment extends Fragment implements IRequestResultListener, View.OnClickListener,EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener, View.OnLayoutChangeListener,ILiveUIChangeListener, ViewTreeObserver.OnGlobalLayoutListener, LiveCommentAdapter.OnReplySelectedListener, LiveCommentAdapter.OnCommentItemLongClickListener {
    public FrameLayout mEmojIconsLayout;

    private String mVid;
    private View mRootView;
    private LinearLayout mCommentLikeAndEmojLayout;
    private ImageView mEmojIconIv;
    private LinearLayout mSendCommentAndLikeLayout;
    private LinearLayout mLikeLayout;
    private ImageView mLikeIv;
    private TextSwitcher mLikeCounterTs;
    private TextView mSendCommentTv;
    private EmojiconEditText mEmojiconEt;
    private RecyclerView mLiveCommentRecyclerView;
    private TextView mNewCommentTv;
    private boolean isLiked;
    private boolean isExit = false;
    private boolean isInitedMargin;
    private DelCommentDialog mDelCommentDialog;
    /**
     * 是否处于回复状态
     */
    private boolean mIsReply;
    /**
     * 回复的userId
     */
    private String mReplyToUserId;
    /**
     * 回复的userName
     */
    private String mReplyToUserName;
    /**
     * 是否正在轮询评论列表
     */
    boolean isPollingCommentList = false;
    /**
     * 评论超时时间为10 秒
     */
    private final int COMMENT_CIMMIT_TIMEOUT = 10 * 1000;
    /**
     * 上传评论的时间
     */
    private long mLastCommentTime = 0;
    private String mLastTimeStamp = "";
    /**
     * 上次发送的评论id
     */
    private String mLastSendCommentId;
    /**
     * 评论列表
     **/
    private List<CommentItemBean> mCommentDataList = null;
    private boolean mInputState = true;
    private int mScreenHeight = 0;
    private int mKeyHeight = 0;
    private int mLikeCount = 0;
    private int mTopMargin = 0;
    private LiveCommentAdapter mLiveCommentAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_live_comment,container,false);
        initView();
        updateLikeCount(0);
        initEmojIconFragment();
        mScreenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        mKeyHeight = mScreenHeight / 3;
        observeSoftKeyboard();
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mLiveCommentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mNewCommentTv.setVisibility(View.GONE);
            }
        });
        mLastCommentTime = System.currentTimeMillis();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isInitedMargin){
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLiveCommentRecyclerView.getLayoutParams();
            layoutParams.setMargins(0,mTopMargin,0,0);
        }
    }

    @Override
    public void onDestroyView() {
        onExit();
        super.onDestroyView();
    }

    public void updateLikeCount(int count){
        mLikeCount = count;
        if(getContext() != null){
            mLikeCounterTs.setText(mLikeCount + getContext().getString(R.string.str_live_ok_praise_unit));
        }
    }
    private void initEmojIconFragment() {
        EmojiconsFragment fg = EmojiconsFragment.newInstance(false);
        getChildFragmentManager().beginTransaction().replace(R.id.layout_emoj_icons, fg).commit();
    }
    private void initView() {
        mCommentLikeAndEmojLayout = (LinearLayout) mRootView.findViewById(R.id.layout_comment_like_and_emoj);
        mEmojIconIv = (ImageView) mRootView.findViewById(R.id.iv_emojicon);
        mSendCommentAndLikeLayout = (LinearLayout) mRootView.findViewById(R.id.layout_comment_and_like);
        mLikeLayout = (LinearLayout) mRootView.findViewById(R.id.layout_like);
        mLikeIv = (ImageView) mRootView.findViewById(R.id.iv_like);
        mLikeCounterTs = (TextSwitcher) mRootView.findViewById(R.id.ts_likes_counter);
        mSendCommentTv = (TextView) mRootView.findViewById(R.id.tv_send_comment);
        mEmojiconEt = (EmojiconEditText) mRootView.findViewById(R.id.et_comment_input);
        mNewCommentTv = (TextView) mRootView.findViewById(R.id.tv_new_comment);
        mEmojIconsLayout = (FrameLayout) mRootView.findViewById(R.id.layout_emoj_icons);
        mLiveCommentRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerview_live_comment);

        mEmojIconIv.setOnClickListener(this);
        mLikeLayout.setOnClickListener(this);
        mSendCommentTv.setOnClickListener(this);
        mNewCommentTv.setOnClickListener(this);

        mEmojiconEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEmojIconsLayout.getVisibility() == View.VISIBLE) {
                    mEmojIconIv.setImageDrawable(getContext().getResources().getDrawable(R.drawable.input_state_emojo));
                    mEmojIconsLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 设置videoId
     * @param vid
     */
    public void setmVid(String vid){
        this.mVid = vid;
        startPollingCommentList();
    }
    // 发表评论
    private void click_send() {
        // 发评论／回复 前需要先判断用户是否登录
        if (!GolukApplication.getInstance().isUserLoginSucess) {
            GolukUtils.startUserLogin(getContext());
            return;
        }
        UserInfo loginUser = GolukApplication.getInstance().getMyInfo();
        if (mReplyToUserId != null && loginUser.uid.equals(mReplyToUserId)) {
            mIsReply = false;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastCommentTime < COMMENT_CIMMIT_TIMEOUT) {
            LiveDialogManager.getManagerInstance().showSingleBtnDialog(getContext(),
                    LiveDialogManager.DIALOG_TYPE_COMMENT_TIMEOUT, "", this.getResources().getString(R.string.comment_sofast_text));
            return;
        }

        final String content = mEmojiconEt.getText().toString().trim();
        if (null == content || "".equals(content)) {
            mSendCommentTv.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_error));
            GolukUtils.showToast(getContext(), this.getString(R.string.str_input_comment_content));
            return;
        }

        httpPost_requestAdd(content);
    }

    // 删除评论
    public void deleteComment(String id) {
        CommentDeleteRequest request = new CommentDeleteRequest(IPageNotifyFn.PageType_DelComment, this);
        boolean isSucess = request.get(id);
        if (!isSucess) {
            // 失败
            GolukUtils.showToast(getContext(), this.getString(R.string.str_delete_fail));
            return;
        }
        LiveDialogManager.getManagerInstance().showCommProgressDialog(getContext(),
                LiveDialogManager.DIALOG_TYPE_COMMENT_PROGRESS_DELETE, "", this.getString(R.string.str_delete_ongoing), true);
    }

    /**
     * 开始轮询评论
     */
    public void startPollingCommentList(){
        if(isExit || isPollingCommentList || TextUtils.isEmpty(mVid)){
            return;
        }
        new Thread(){
            public void run(){
                isPollingCommentList = true;
                while(!isExit){
                    getCommentList();
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    /**
     * 获取评论列表数据
     */
    private void getCommentList() {
        String type = ICommentFn.COMMENT_TYPE_LIVE;
        CommentListRequest request = new CommentListRequest(IPageNotifyFn.PageType_CommentList, this);
        if(TextUtils.isEmpty(mLastTimeStamp)){
            request.getBySort(mVid, type, 0, mLastTimeStamp,"0");
        }else{
            request.getBySort(mVid, type, 1, mLastTimeStamp,"0");
        }
    }

    // 添加评论
    private void httpPost_requestAdd(String txt) {
        if (null == mVid) {
            GolukUtils.showToast(getContext(), this.getString(R.string.str_load_data_ongoing));
            return;
        }
        String type = ICommentFn.COMMENT_TYPE_LIVE;
        CommentAddRequest request = new CommentAddRequest(IPageNotifyFn.PageType_AddComment, this);
        boolean isSucess = false;
        if (mIsReply) {
            isSucess = request.get(mVid, type, txt, mReplyToUserId, mReplyToUserName, null);
        } else {
            isSucess = request.get(mVid, type, txt, "", "", null);
        }
        if (!isSucess) {
            // 失败
            GolukUtils.showToast(getContext(), this.getString(R.string.str_comment_fail));
            return;
        }
        LiveDialogManager.getManagerInstance().showCommProgressDialog(getContext(),
                LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT, "", this.getString(R.string.str_comment_ongoing), true);
    }

    // 点赞请求
    public boolean sendPraiseRequest() {
        if (!GolukApplication.getInstance().isUserLoginSucess) {
            GolukUtils.startUserLogin(getContext());
            return false;
        }
        PraiseRequest request = new PraiseRequest(IPageNotifyFn.PageType_Praise, this);
        return request.get("1", mVid,"1");
    }

    private boolean isCanShowSoft() {
        if (null == mVid) {
            return false;
        }
        return true;
    }

    private void click_switchInput() {
        if (!this.isCanShowSoft()) {
            return;
        }
        if (mInputState) {
            click_Emojocon();
        } else {
            click_soft();
        }
    }
    private void click_soft() {
        if (!this.isCanShowSoft()) {
            return;
        }
        mEmojiconEt.setFocusable(true);
        mEmojiconEt.requestFocus();

        if (!GolukUtils.isSettingBoardHeight()) {
            this.hideEmojocon();
        }

        if (mEmojIconsLayout.getVisibility() == View.GONE) {
            this.setResize();
        } else {
            setInputAdJust();
        }
        setSwitchState(true);
        GolukUtils.showSoftNotThread(mEmojiconEt);
        hideEmojocon();
        this.setResize();
    }
    private void setSwitchState(boolean isTextInput) {
        mInputState = isTextInput;
        if (isTextInput) {
            // 显示表情
            mEmojIconIv.setImageDrawable(this.getResources().getDrawable(R.drawable.input_state_emojo));
        } else {
            // 显示键盘
            mEmojIconIv.setImageDrawable(this.getResources().getDrawable(R.drawable.input_state_txt));
        }
    }
    private void hideEmojocon() {
        mEmojIconsLayout.setVisibility(View.GONE);
    }
    private void setInputAdJust() {
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }
    private void setResize() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    public void observeSoftKeyboard() {
        SoftKeyBoardListener.setListener(getActivity(), new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                GolukUtils.setKeyBoardHeight(height);
            }

            @Override
            public void keyBoardHide(int height) {
            }
        });
    }
        // 点击“显示 表情”
    private void click_Emojocon() {
        if (!isCanShowSoft()) {
            return;
        }
        GolukUtils.hideSoft(getContext(), mEmojiconEt);
        showEmojocon();
        setSwitchState(false);
    }

    private void setLayoutHeight() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mEmojIconsLayout.getLayoutParams();
        lp.height = GolukUtils.getKeyBoardHeight();
        mEmojIconsLayout.setLayoutParams(lp);
    }
    private void showEmojocon() {
        setLayoutHeight();
        mEmojIconsLayout.setVisibility(View.VISIBLE);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    @Override
    public void onLoadComplete(int requestType, Object result) {
        if(isExit){
            return;
        }
        switch (requestType) {
            case IPageNotifyFn.PageType_CommentList:
                CommentResultBean resultBean = (CommentResultBean) result;
                if (resultBean != null && resultBean.success && resultBean.data != null) {
                    addAndRefreshComments(resultBean.data.comments);
                }
                break;
            case IPageNotifyFn.PageType_DelComment:
                LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
                CommentDelResultBean delResultBean = (CommentDelResultBean) result;

                if (delResultBean != null && delResultBean.data != null) {
                    if (!GolukUtils.isTokenValid(delResultBean.data.result)) {
                        GolukUtils.startUserLogin(getContext());
                        return;
                    }
                }
                if (null != delResultBean && delResultBean.success) {
                    GolukUtils.showToast(getContext(), this.getString(R.string.str_delete_success));
                    for(CommentItemBean commentItemBean : mCommentDataList){
                        if(TextUtils.isEmpty(commentItemBean.commentId) || delResultBean.data == null){
                            return;
                        }
                        String delCommentId = delResultBean.data.commentid;
                        if(!TextUtils.isEmpty(delCommentId) && delCommentId.equals(commentItemBean.commentId)){
                            mCommentDataList.remove(commentItemBean);
                            mLiveCommentAdapter.notifyDataSetChanged();
                            return;
                        }
                    }
                } else {
                    GolukUtils.showToast(getContext(), this.getString(R.string.str_delete_fail));
                }
                break;
            case IPageNotifyFn.PageType_AddComment:
                LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
                CommentAddResultBean addResultBean = (CommentAddResultBean) result;

                if (addResultBean != null && addResultBean.data != null) {
                    if (!GolukUtils.isTokenValid(addResultBean.data.result)) {
                        GolukUtils.startUserLogin(getContext());
                        return;
                    }
                }

                if (null != addResultBean && addResultBean.success) {
                    CommentAddBean addBean = addResultBean.data;
                    if (addBean == null) {
                        GolukUtils.showToast(getContext(), this.getString(R.string.str_comment_fail));
                        return;
                    }

                    if (!"".equals(addBean.result)) {
                        if ("0".equals(addBean.result)) {// 成功
                            //评论视频
                            mLastCommentTime = System.currentTimeMillis();
                            CommentItemBean commentItemBean = new CommentItemBean();
                            commentItemBean.author = new AuthorBean();
                            commentItemBean.reply = new ReplyBean();
                            commentItemBean.commentId = addBean.commentid;
                            commentItemBean.author.authorid = addBean.authorid;
                            commentItemBean.author.name = addBean.authorname;
                            commentItemBean.author.avatar = addBean.authoravatar;
                            commentItemBean.author.customavatar = addBean.customavatar;
                            commentItemBean.author.label = addBean.label;
                            commentItemBean.reply.id = addBean.replyid;
                            commentItemBean.reply.name = addBean.replyname;
                            commentItemBean.text = addBean.text;
                            commentItemBean.time = addBean.time;

                            List<CommentItemBean> tempList = new ArrayList<CommentItemBean>();
                            tempList.add(commentItemBean);
                            addAndRefreshComments(tempList);

                            mLastSendCommentId = addBean.commentid;
                            closeSoftKeyboard();
                            cleanReplyState();
                            getCommentList();
                        } else if ("1".equals(addBean.result)) {
                            GolukDebugUtils.e("", "参数错误");
                        } else if ("2".equals(addBean.result)) {// 重复评论
                            LiveDialogManager.getManagerInstance().showSingleBtnDialog(getContext(),
                                    LiveDialogManager.FUNCTION_DIALOG_OK, "",
                                    this.getResources().getString(R.string.comment_repeat_text));
                        } else if ("3".equals(addBean.result)) {// 频繁评论
                            LiveDialogManager.getManagerInstance().showSingleBtnDialog(getContext(),
                                    LiveDialogManager.DIALOG_TYPE_COMMENT_TIMEOUT, "",
                                    this.getResources().getString(R.string.comment_sofast_text));
                        } else {
                            LiveDialogManager.getManagerInstance().showSingleBtnDialog(getContext(),
                                    LiveDialogManager.FUNCTION_DIALOG_OK, "",
                                    this.getString(R.string.str_save_comment_fail));
                        }
                    }
                } else {
                    GolukUtils.showToast(getContext(), this.getString(R.string.str_comment_fail));
                }
                break;
            case IPageNotifyFn.PageType_Praise:
                PraiseResultBean praiseResultBean = (PraiseResultBean) result;
                if (praiseResultBean == null || !praiseResultBean.success) {
                    GolukUtils.showToast(getContext(), this.getString(R.string.user_net_unavailable));
                    return;
                }

                PraiseResultDataBean ret = praiseResultBean.data;
                if (null != ret && !TextUtils.isEmpty(ret.result)) {
                    if ("0".equals(ret.result)) {
                        updateLikeCount(mLikeCount + 1);
                        isLiked = true;

                    } else if ("7".equals(ret.result)) {
                        GolukUtils.showToast(getContext(), this.getString(R.string.str_no_duplicated_praise));
                    } else {
                        GolukUtils.showToast(getContext(), this.getString(R.string.str_praise_failed));
                    }
                }
                break;
            default:
                break;
        }
    }

    private void addAndRefreshComments(List<CommentItemBean> commentList) {
        if (null == commentList || commentList.size() <= 0) {
            return;
        }
        if(null != commentList.get(0)){
            if(!TextUtils.isEmpty(commentList.get(0).time)){
                mLastTimeStamp = commentList.get(0).time;
            }
        }
        if (mCommentDataList == null) {
            mCommentDataList = new ArrayList<CommentItemBean>();
        }
        int currCommentCount = mCommentDataList.size();
        boolean hasNewComment = false;
        for (CommentItemBean comment : commentList) {
            if (comment != null) {
                if(!TextUtils.isEmpty(mLastSendCommentId) && !TextUtils.isEmpty(comment.commentId) && mLastSendCommentId.equals(comment.commentId)){
                    continue;
                }
                mCommentDataList.add(comment);
                if(comment.author == null ||(comment.author != null && !GolukUtils.isLoginUser(comment.author.authorid))){
                    hasNewComment = true;
                }
            }
        }
        if(mLiveCommentAdapter == null){
            mLiveCommentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mLiveCommentAdapter = new LiveCommentAdapter(getContext(),mCommentDataList,this,this);
            mLiveCommentRecyclerView.setAdapter(mLiveCommentAdapter);
        }else{
            mLiveCommentAdapter.notifyItemRangeChanged(currCommentCount - 1,mCommentDataList.size() - currCommentCount - 1);
        }
        if(hasNewComment){
            mNewCommentTv.setVisibility(View.VISIBLE);
        }
    }

    private void closeSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mRootView,InputMethodManager.SHOW_FORCED);
        imm.hideSoftInputFromWindow(mRootView.getWindowToken(), 0); //强制隐藏键盘
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if(viewId == R.id.iv_emojicon){
            click_switchInput();
        }else if(viewId == R.id.layout_like){
            if(!isLiked){
                sendPraiseRequest();
            }
        }else if(viewId == R.id.tv_send_comment){
            click_send();
        }else if(viewId == R.id.tv_new_comment){
            if(mLiveCommentRecyclerView != null && mCommentDataList != null && mLiveCommentAdapter != null){
                mLiveCommentRecyclerView.smoothScrollToPosition(mCommentDataList.size()-1);
                mNewCommentTv.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(mEmojiconEt);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(mEmojiconEt, emojicon);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > mKeyHeight)) {
            //软键盘弹起
            setSwitchState(true);
            showSendComment();
        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > mKeyHeight)) {
            //软键盘关闭
            if (this.mEmojiconEt.getVisibility() == View.GONE) {
                setSwitchState(true);
                showLikeLayout();
            } else {
                setSwitchState(false);
                showSendComment();
            }
        }
    }

    /**
     * 显示发送评论按钮
     */
    private void showSendComment(){
        mLikeLayout.setVisibility(View.GONE);
        mSendCommentTv.setVisibility(View.VISIBLE);
    }

    /**
     * 显示喜欢（赞）按钮
     */
    private void showLikeLayout(){
        mLikeLayout.setVisibility(View.VISIBLE);
        mSendCommentTv.setVisibility(View.GONE);
    }
    @Override
    public void onFramgentTopMarginReceived(int topMargin) {
        if(mLiveCommentRecyclerView != null && !isInitedMargin){
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLiveCommentRecyclerView.getLayoutParams();
            layoutParams.setMargins(0,topMargin,0,0);
            this.mTopMargin = topMargin;
            isInitedMargin = true;
        }
    }

    @Override
    public void onExit() {
        isExit = true;
        if(mDelCommentDialog != null && mDelCommentDialog.isShowing()){
            mDelCommentDialog.dismiss();
        }
        mDelCommentDialog = null;
    }

    @Override
    public void onGlobalLayout() {
        int heightDiff = mRootView.getRootView().getHeight() - mRootView.getHeight();
        if (heightDiff > 200) {//软键盘弹起
            mLikeLayout.setVisibility(View.GONE);
            mSendCommentTv.setVisibility(View.VISIBLE);
        }else{//软键盘处于关闭状态
            if(mEmojIconsLayout.getVisibility() == View.VISIBLE){
                mLikeLayout.setVisibility(View.GONE);
                mSendCommentTv.setVisibility(View.VISIBLE);
            }else{
                if(TextUtils.isEmpty(mEmojiconEt.getText().toString())){
                    cleanReplyState();
                }
                mLikeLayout.setVisibility(View.VISIBLE);
                mSendCommentTv.setVisibility(View.GONE);
            }
        }
    }

    public void cleanReplyState(){
        mReplyToUserId = "";
        mReplyToUserName = "";
        mEmojiconEt.setText("");
        mEmojiconEt.setHint("");
        mIsReply = false;
    }
    @Override
    public void onReplySelected(String replyId, String replyAuthorId, String replyAuthorName) {
        if(GolukApplication.getInstance().isUserLoginToServerSuccess() && GolukApplication.getInstance().getMyInfo()!= null){
            String loginUserId = GolukApplication.getInstance().getMyInfo().uid;
            if(!TextUtils.isEmpty(loginUserId)&&replyAuthorId.equals(loginUserId)){
                //不能回复自己的评论
                cleanReplyState();
                return;
            }
        }
        mReplyToUserId = replyAuthorId;
        mReplyToUserName = replyAuthorName;
        mIsReply = true;
        if(TextUtils.isEmpty(mEmojiconEt.getText().toString())){
            mEmojiconEt.setHint(getContext().getResources().getString(R.string.str_reply) + "@" + replyAuthorName);
        }
    }

    @Override
    public void onCommentLongClicked(String commentId) {
        if(mDelCommentDialog == null){
            mDelCommentDialog = new DelCommentDialog(getContext(),this, commentId);
        }
        mDelCommentDialog.setmCommentId(commentId);
        mDelCommentDialog.show();
    }
}
