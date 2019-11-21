package com.rd.veuisdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.rd.veuisdk.ui.SimpleDraweeViewUtils;

/**
 * 云音乐授权证书展示
 *
 * @author JIAN
 * @create 2018/11/16
 * @Describe
 */
public class MusicSignActivity extends BaseActivity {

    private static final String PARAM_URL = "url";

    static void onSign(Context context, String url) {

        Intent intent = new Intent(context, MusicSignActivity.class);
        intent.putExtra(PARAM_URL, url);
        (context).startActivity(intent);


    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_sign_layout);
        findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.yunmusic_sign);
        String url = getIntent().getStringExtra(PARAM_URL);
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.simpleDraweeView);
        //指定原图大小，高清展示
        SimpleDraweeViewUtils.setCover(draweeView, url, false, 1242, 2063);

    }
}
