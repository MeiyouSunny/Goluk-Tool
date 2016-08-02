package com.mobnote.golukmain.livevideo;

import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.mobnote.golukmain.R;
import com.mobnote.golukmain.livevideo.livecomment.LiveCommentFragment;

/**
 * Created by leege100 on 2016/8/1.
 */
public class DelCommentDialog extends Dialog implements View.OnClickListener{
    private TextView mDelCommentTv, mCancleTv;
    private Context mContext;
    private String mCommentId = "";
    private Fragment mFragment;

    public DelCommentDialog(Context context, Fragment fragment ,String commentId) {
        super(context, R.style.CustomDialog);
        setContentView(R.layout.comment_delete_layout);

        setCanceledOnTouchOutside(true);
        Window window = this.getWindow();
        window.setGravity(Gravity.BOTTOM);

        this.mContext = context;
        this.mCommentId = commentId;
        this.mFragment = fragment;

        initView();
    }

    public void setmCommentId(String mCommentId) {
        this.mCommentId = mCommentId;
    }

    private void initView() {
        mDelCommentTv = (TextView) findViewById(R.id.tv_delete);
        mCancleTv = (TextView) findViewById(R.id.tv_cancle);
        mDelCommentTv.setText(mContext.getString(R.string.delete_text));

        mDelCommentTv.setOnClickListener(this);
        mCancleTv.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_delete) {
            dismiss();
            deleteComment();
        } else if (id == R.id.tv_cancle) {
            dismiss();
        } else {
        }
    }

    private void deleteComment() {
        if (mFragment instanceof LiveCommentFragment) {
            ((LiveCommentFragment) mFragment).deleteComment(mCommentId);
        }
    }
}
