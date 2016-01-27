package cn.com.mobnote.golukmobile.videodetail;

import cn.com.mobnote.application.GolukApplication;
import cn.com.mobnote.golukmobile.R;
import cn.com.mobnote.golukmobile.comment.CommentActivity;
import cn.com.mobnote.golukmobile.comment.CommentBean;
import cn.com.mobnote.golukmobile.live.LiveDialogManager;
import cn.com.mobnote.logic.GolukModule;
import cn.com.mobnote.module.videosquare.VideoSuqareManagerFn;
import cn.com.mobnote.util.GolukUtils;
import cn.com.mobnote.util.JsonUtil;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class ReplyDialog extends Dialog implements android.view.View.OnClickListener {

	private TextView mReplayOrDelete, mCancle;
	private Context mContext;
	private CommentBean mCommentBean = null;
	private EditText mEditText = null;
	private boolean mFlagReply ;

	public ReplyDialog(Context context, CommentBean commentBean, EditText editText, boolean isReply) {
		super(context, R.style.CustomDialog);
		setContentView(R.layout.video_reply_layout);

		setCanceledOnTouchOutside(true);
		Window window = this.getWindow();
		window.setGravity(Gravity.BOTTOM);

		this.mContext = context;
		this.mCommentBean = commentBean;
		this.mEditText = editText;
		this.mFlagReply = isReply;

		initView();
	}

	private void initView() {
		mReplayOrDelete = (TextView) findViewById(R.id.reply_or_delete);
		mCancle = (TextView) findViewById(R.id.cancle);
		if (mFlagReply) {
			mReplayOrDelete.setText(mContext.getString(R.string.str_msgcenter_comment_replytext));
		} else {
			mReplayOrDelete.setText(mContext.getString(R.string.delete_text));
		}

		mReplayOrDelete.setOnClickListener(this);
		mCancle.setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.reply_or_delete:
			dismiss();
			deal();
			break;

		case R.id.cancle:
			dismiss();
			break;
		default:
			break;
		}
	}
	
	/**
	 * 点击回复或删除后处理操作
	 */
	private void deal() {
		if (mFlagReply) {
			mEditText.requestFocus();
			GolukUtils.showSoft(mEditText);
			mEditText.setHint(mContext.getString(R.string.str_reply_other_text) + mCommentBean.mUserName
					+ mContext.getString(R.string.str_colon));
		} else {
			if (mContext instanceof VideoDetailActivity) {
				((VideoDetailActivity) mContext).httpPost_requestDel(mCommentBean.mCommentId);
			} else if (mContext instanceof CommentActivity) {
				((CommentActivity) mContext).httpPost_requestDel(mCommentBean.mCommentId);
			}
		}

	}

}
