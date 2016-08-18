package com.goluk.crazy.panda.main.activity;

import android.os.Bundle;
import com.google.widget.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabWidget;

import com.goluk.crazy.panda.R;
import com.goluk.crazy.panda.common.activity.BaseActivity;
import com.goluk.crazy.panda.main.fragment.FragmentAlbum;
import com.goluk.crazy.panda.main.fragment.FragmentDevice;
import com.goluk.crazy.panda.main.fragment.FragmentMine;
import com.goluk.crazy.panda.main.fragment.FragmentSquare;

public class MainActivity extends BaseActivity {
    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTabs();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initTabs() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.fl_main_tab_content);

        Bundle b = new Bundle();
        b.putString("key", "Device");
        RelativeLayout follow = (RelativeLayout) inflater.inflate(R.layout.main_tab_indicator_device, null);
        mTabHost.addTab(mTabHost.newTabSpec("Device").setIndicator(follow), FragmentDevice.class, b);

        b = new Bundle();
        b.putString("key", "Square");
        LinearLayout discover = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_square, null);
        mTabHost.addTab(mTabHost.newTabSpec("Square").setIndicator(discover), FragmentSquare.class, b);

        b = new Bundle();
        b.putString("key", "Album");
        LinearLayout album = (LinearLayout) inflater.inflate(R.layout.main_tab_indicator_album, null);
        mTabHost.addTab(mTabHost.newTabSpec("Album").setIndicator(album), FragmentAlbum.class, b);

        b = new Bundle();
        b.putString("key", "Mine");
        RelativeLayout mine = (RelativeLayout) inflater.inflate(R.layout.main_tab_indicator_mine, null);
        mTabHost.addTab(mTabHost.newTabSpec("Mine").setIndicator(mine), FragmentMine.class, b);

        TabWidget widget = mTabHost.getTabWidget();
        widget.setDividerDrawable(null);
        mTabHost.getTabWidget().setBackgroundResource(R.color.color_main_tab_bg);

        View lineView = new View(this);
        lineView.setBackgroundResource(R.color.color_line_divider);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3);
        lineView.setLayoutParams(lineParams);
        mTabHost.addView(lineView);

        for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
            mTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = (int) this.getResources().getDimension(
                    R.dimen.main_tab_height);
        }
    }
}
