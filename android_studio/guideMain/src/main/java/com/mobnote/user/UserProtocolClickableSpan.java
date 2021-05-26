package com.mobnote.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.mobnote.golukmain.UserOpenUrlActivity;

/**
 * Created by lily on 16-5-30.
 */
public class UserProtocolClickableSpan extends ClickableSpan {


    private Context mContext = null;

    public UserProtocolClickableSpan(Context context) {
        this.mContext = context;
    }

    @Override
    public void onClick(View view) {
        Intent intentUrl = new Intent(mContext, UserOpenUrlActivity.class);
        intentUrl.putExtra(UserOpenUrlActivity.FROM_TAG, "protocol");
        mContext.startActivity(intentUrl);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(Color.parseColor("#0080ff"));
        ds.setUnderlineText(false);
    }
}
