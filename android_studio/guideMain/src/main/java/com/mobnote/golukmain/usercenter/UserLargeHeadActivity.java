package com.mobnote.golukmain.usercenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import com.mobnote.golukmain.R;
import com.mobnote.user.UserUtils;
import com.mobnote.util.GlideUtils;

import cn.com.tiros.debug.GolukDebugUtils;

public class UserLargeHeadActivity extends Activity {

    private LargeHeadView mLargeHeadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_large_head);

        mLargeHeadView = (LargeHeadView) findViewById(R.id.lv_usercenter_large_head_view);

        Intent it = getIntent();
        String head = it.getStringExtra("headurl");
        GolukDebugUtils.e("", "UserLargeHeadActivity---------head: " + head);
        if (head.contains("http")) {
            GlideUtils.loadImage(this, mLargeHeadView, head, R.drawable.usercenter_head_default);
        } else {
            UserUtils.focusHead(this, head, mLargeHeadView);
        }
    }


    public void exit() {
        if (!isFinishing()) {
            finish();
            GlideUtils.clearMemory(this);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
        }
        return super.onKeyDown(keyCode, event);
    }
}
