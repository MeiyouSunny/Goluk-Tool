package com.mobnote.golukmain.livevideo.livecomment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mobnote.application.GolukApplication;
import com.mobnote.eventbus.EventConfig;
import com.mobnote.eventbus.EventPraiseStatusChanged;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.comment.CommentAddRequest;
import com.mobnote.golukmain.comment.CommentBean;
import com.mobnote.golukmain.comment.CommentDeleteRequest;
import com.mobnote.golukmain.comment.ICommentFn;
import com.mobnote.golukmain.comment.bean.CommentAddBean;
import com.mobnote.golukmain.comment.bean.CommentAddResultBean;
import com.mobnote.golukmain.comment.bean.CommentDataBean;
import com.mobnote.golukmain.comment.bean.CommentDelResultBean;
import com.mobnote.golukmain.comment.bean.CommentItemBean;
import com.mobnote.golukmain.comment.bean.CommentResultBean;
import com.mobnote.golukmain.http.IRequestResultListener;
import com.mobnote.golukmain.live.LiveDialogManager;
import com.mobnote.golukmain.live.UserInfo;
import com.mobnote.golukmain.livevideo.ILiveUIChangeListener;
import com.mobnote.golukmain.praise.PraiseCancelRequest;
import com.mobnote.golukmain.praise.PraiseRequest;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultBean;
import com.mobnote.golukmain.praise.bean.PraiseCancelResultDataBean;
import com.mobnote.golukmain.praise.bean.PraiseResultBean;
import com.mobnote.golukmain.praise.bean.PraiseResultDataBean;
import com.mobnote.golukmain.videodetail.SoftKeyBoardListener;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GolukUtils;
import com.mobnote.util.ZhugeUtils;
import com.mobnote.videoedit.utils.DeviceUtil;
import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

import java.util.ArrayList;

import cn.com.mobnote.module.page.IPageNotifyFn;
import cn.com.tiros.debug.GolukDebugUtils;
import de.greenrobot.event.EventBus;

/**
 * 直播评论fragment
 * Created by leege100 on 2016/7/20.
 */
public class LiveCommentFragment extends Fragment implements IRequestResultListener, View.OnClickListener,EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener, View.OnLayoutChangeListener,ILiveUIChangeListener, ViewTreeObserver.OnGlobalLayoutListener {

    private String mVid;
    private View mRootView;
    private LinearLayout mCommentLikeAndEmojLayout;
    private ImageView mEmojIconIv;
    private LinearLayout mSendCommentAndLikeLayout;
    private LinearLayout mLikeLayout;
    private ImageView mLikeIv;
    private TextView mLikeCountTv;
    private TextView mSendCommentTv;
    private EmojiconEditText mEmojiconEt;
    public FrameLayout mEmojIconsLayout;
    private RecyclerView mLiveCommentRecyclerView;

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
    boolean isPollingCommentList;
    /**
     * 评论超时时间为10 秒
     */
    private final int COMMENT_CIMMIT_TIMEOUT = 10 * 1000;
    /**
     * 上传评论的时间
     */
    private long mLastCommentTime = 0;
    /**
     * 评论列表
     **/
    private ArrayList<CommentBean> commentDataList = null;
    private boolean isSwitchStateFinish;
    private boolean mInputState = true;
    private int screenHeight = 0;
    private int keyHeight = 0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_live_comment,container,false);
        initView();
        initEmojIconFragment();
        screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        keyHeight = screenHeight / 3;
        observeSoftKeyboard();
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        return mRootView;
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
        mLikeCountTv = (TextView) mRootView.findViewById(R.id.tv_like_count);
        mSendCommentTv = (TextView) mRootView.findViewById(R.id.tv_send_comment);
        mEmojiconEt = (EmojiconEditText) mRootView.findViewById(R.id.et_comment_input);
        mEmojIconsLayout = (FrameLayout) mRootView.findViewById(R.id.layout_emoj_icons);
        mLiveCommentRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recyclerview_live_comment);

        mEmojIconIv.setOnClickListener(this);
        mLikeLayout.setOnClickListener(this);
        mSendCommentTv.setOnClickListener(this);

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
    }
    // 发表评论
    private void click_send() {
        // 发评论／回复 前需要先判断用户是否登录
        if (!GolukApplication.getInstance().isUserLoginSucess) {
           GolukUtils.startUserLogin(getContext());
            return;
        }
        UserInfo loginUser = GolukApplication.getInstance().getMyInfo();
        if (mReplyToUserId != null && loginUser.uid.equals(mReplyToUserId) && mIsReply) {
            mIsReply = false;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastCommentTime < COMMENT_CIMMIT_TIMEOUT) {
            LiveDialogManager.getManagerInstance().showSingleBtnDialog(getContext(),
                    LiveDialogManager.DIALOG_TYPE_COMMENT_TIMEOUT, "",
                    this.getResources().getString(R.string.comment_sofast_text));
            return;
        }

        final String content = mEmojiconEt.getText().toString().trim();
        if (null == content || "".equals(content)) {
            GolukUtils.showToast(getContext(), this.getString(R.string.str_input_comment_content));
            return;
        }

        httpPost_requestAdd(content);
    }

    // 删除评论
    void httpPost_requestDel(String id) {
        CommentDeleteRequest request = new CommentDeleteRequest(IPageNotifyFn.PageType_DelComment, this);
        boolean isSucess = request.get(id);
        if (!isSucess) {
            // 失败
            GolukUtils.showToast(getContext(), this.getString(R.string.str_delete_fail));
            return;
        }
        LiveDialogManager.getManagerInstance().showCommProgressDialog(getContext(),
                LiveDialogManager.DIALOG_TYPE_COMMENT_PROGRESS_DELETE, "", this.getString(R.string.str_delete_ongoing),
                true);
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

        PraiseRequest request = new PraiseRequest(IPageNotifyFn.PageType_Praise, this);
        return request.get(ICommentFn.COMMENT_TYPE_LIVE, mVid, "1");
    }

    // 取消点赞请求
    public boolean sendCancelPraiseRequest() {
        PraiseCancelRequest request = new PraiseCancelRequest(IPageNotifyFn.PageType_PraiseCancel, this);
        return request.get(ICommentFn.COMMENT_TYPE_LIVE,mVid);
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
        isSwitchStateFinish = false;
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
        isSwitchStateFinish = true;
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
        isSwitchStateFinish = false;
        GolukUtils.hideSoft(getContext(), mEmojiconEt);
        showEmojocon();
        setSwitchState(false);
        isSwitchStateFinish = true;
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
        switch (requestType) {
            case IPageNotifyFn.PageType_CommentList:

                CommentResultBean resultBean = (CommentResultBean) result;
                if (resultBean != null && resultBean.success && resultBean.data != null) {
                    CommentDataBean dataBean = resultBean.data;
                    int count = 0;
                    if (!TextUtils.isEmpty(dataBean.count) && TextUtils.isDigitsOnly(dataBean.count)) {
                        count = Integer.parseInt(dataBean.count);
                    }

                    if (null == dataBean.comments || dataBean.comments.size() <= 0) {
                        return;
                    }

                    // 有数据
                    commentDataList.clear();
                    for (CommentItemBean item : dataBean.comments) {
                    }

                } else {
                }
                break;
            case IPageNotifyFn.PageType_DelComment:
                LiveDialogManager.getManagerInstance().dissmissCommProgressDialog();
                CommentDelResultBean DelResultBean = (CommentDelResultBean) result;

                if (DelResultBean != null && DelResultBean.data != null) {
                    if (!GolukUtils.isTokenValid(DelResultBean.data.result)) {
                        GolukUtils.startUserLogin(getContext());
                        return;
                    }
                }
                if (null != DelResultBean && DelResultBean.success) {
                    GolukUtils.showToast(getContext(), this.getString(R.string.str_delete_success));
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

                    if (addBean.label != null) {

                    }
                    if (!"".equals(addBean.result)) {
                        if ("0".equals(addBean.result)) {// 成功
                            //评论视频
                            mLastCommentTime = System.currentTimeMillis();
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
                    }else{}

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
                    } else if ("7".equals(ret.result)) {
                        GolukUtils.showToast(getContext(), this.getString(R.string.str_no_duplicated_praise));
                    } else {
                        GolukUtils.showToast(getContext(), this.getString(R.string.str_praise_failed));
                    }
                }
                break;
            case IPageNotifyFn.PageType_PraiseCancel:
                PraiseCancelResultBean praiseCancelResultBean = (PraiseCancelResultBean) result;
                if (praiseCancelResultBean == null || !praiseCancelResultBean.success) {
                    GolukUtils.showToast(getContext(), this.getString(R.string.user_net_unavailable));
                    return;
                }

                PraiseCancelResultDataBean cancelRet = praiseCancelResultBean.data;
                if (null != cancelRet && !TextUtils.isEmpty(cancelRet.result)) {
                    if ("0".equals(cancelRet.result)) {
                        EventBus.getDefault().post(
                                new EventPraiseStatusChanged(EventConfig.PRAISE_STATUS_CHANGE, mVid, false));
                    } else {
                        GolukUtils.showToast(getContext(), this.getString(R.string.str_cancel_praise_failed));
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if(viewId == R.id.iv_emojicon){
            click_switchInput();
        }else if(viewId == R.id.layout_like){
        }else if(viewId == R.id.tv_send_comment){
            click_send();
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
        if (oldBottom != 0 && bottom != 0 && (oldBottom - bottom > keyHeight)) {
            //软键盘弹起
            setSwitchState(true);
            showSendComment();
        } else if (oldBottom != 0 && bottom != 0 && (bottom - oldBottom > keyHeight)) {
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
        if(mLiveCommentRecyclerView != null){
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLiveCommentRecyclerView.getLayoutParams();
            layoutParams.setMargins(0,topMargin,0, DeviceUtil.dp2px(getContext(),48));
        }
    }

    @Override
    public void onGlobalLayout() {
        int heightDiff = mRootView.getRootView().getHeight() - mRootView.getHeight();
        if (heightDiff > 200) {
            mLikeLayout.setVisibility(View.GONE);
            mSendCommentTv.setVisibility(View.VISIBLE);
        }else{
            if(mEmojIconsLayout.getVisibility() == View.VISIBLE){
                mLikeLayout.setVisibility(View.GONE);
                mSendCommentTv.setVisibility(View.VISIBLE);
            }else{
                mLikeLayout.setVisibility(View.VISIBLE);
                mSendCommentTv.setVisibility(View.GONE);
            }
        }
    }
}
