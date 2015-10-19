package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.comment.CommentBean;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ReplyDialog extends Dialog implements android.view.View.OnClickListener {

	private TextView mReplayOrDelete, mCancle;
	private Context mContext;
	private CommentBean mCommentBean = null;
	private EditText mEditText = null;
	private boolean mIsReplay = true;

	public ReplyDialog(Context context, CommentBean commentBean,EditText editText,boolean isReplay) {
		super(context, R.style.CustomDialog);
		setContentView(R.layout.video_reply_layout);
		this.mContext = context;
		this.mCommentBean = commentBean;
		this.mEditText = editText;
		this.mIsReplay = isReplay;
		
		initView();
	}

	private void initView() {
		mReplayOrDelete = (TextView) findViewById(R.id.reply_or_delete);
		mCancle = (TextView) findViewById(R.id.cancle);
		if(mIsReplay){
			mReplayOrDelete.setText("回复该评论");
		}else{
			mReplayOrDelete.setText("删除");
		}
		
		mReplayOrDelete.setOnClickListener(this);
		mCancle.setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.reply_or_delete:
			dismiss();
			deal(mIsReplay);
			break;
			
		case R.id.cancle:
			dismiss();
			break;
		default:
			break;
		}
	}
	
	private void deal(boolean flag) {
		if(mIsReplay){
			mEditText.requestFocus();
			GolukUtils.showSoft(mEditText);
			mEditText.setHint("回复＠"+mCommentBean.mUserName+"：");
//			GolukUtils.showToast(mContext, "回复评论");
//			final String requestStr = JsonUtil.getAddCommentJson(
//					mVideoJson.data.avideo.video.videoid, "1", mContent,
//					mCommentBean.mUserId, mCommentBean.mUserName);
//			boolean isSucess = GolukApplication.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
//					VideoSuqareManagerFn.VSquare_Req_Add_Comment, requestStr);
//			if (!isSucess) {
//				// 失败
//				GolukUtils.showToast(mContext, "评论失败!");
//				return;
//			}
//			LiveDialogManager.getManagerInstance().showCommProgressDialog(mContext,
//					LiveDialogManager.DIALOG_TYPE_COMMENT_COMMIT, "", "正在提交评论", true);
		}else{
			String requestStr = JsonUtil.getDelCommentJson(mCommentBean.mCommentId);
			boolean isSucess = GolukApplication.getInstance().mGoluk.GolukLogicCommRequest(GolukModule.Goluk_Module_Square,
					VideoSuqareManagerFn.VSquare_Req_Del_Comment, requestStr);
			if (!isSucess) {
				// 失败
				GolukUtils.showToast(mContext, "删除失败");
				return;
			}
			LiveDialogManager.getManagerInstance().showCommProgressDialog(mContext,
					LiveDialogManager.DIALOG_TYPE_COMMENT_PROGRESS_DELETE, "", "正在删除", true);
		}
		
	}

}
