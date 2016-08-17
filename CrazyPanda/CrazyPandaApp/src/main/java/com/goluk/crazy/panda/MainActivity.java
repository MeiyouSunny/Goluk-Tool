package com.goluk.crazy.panda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.goluk.crazy.panda.utils.Test;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    private ImageView mImageView;
    Test mTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTest = new Test();
        mTest.setTest("1111111111111@@@");
        Log.d("CK1", mTest.getTest());
        mImageView = (ImageView)findViewById(R.id.iv_test);
        Toast.makeText(this, mTest.getTest(), Toast.LENGTH_LONG).show();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTest.showImage(this, mImageView);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(String event) {
        if (null == event) {
            return;
        }
        Log.d("CK1", "@@@" + event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
