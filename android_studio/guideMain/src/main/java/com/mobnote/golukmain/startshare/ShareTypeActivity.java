package com.mobnote.golukmain.startshare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mobnote.eventbus.EventSharetypeSelected;
import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;
import com.mobnote.golukmain.startshare.bean.ShareDataBean;
import com.mobnote.golukmain.startshare.bean.ShareTypeBean;

import de.greenrobot.event.EventBus;

/**
 * Created by leege100 on 16/5/17.
 */
public class ShareTypeActivity extends BaseActivity implements View.OnClickListener {
    public static final String SHARE_TYPE_KEY = "sharetype";
    private TextView mSharetypeTitleTv;
    private ImageButton mSharetypeBackIb;
    private ListView mSharetypeListLv;
    private ShareTypeAdapter mSharetypeAdapter;
    private int mSelectedType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharetype);
        EventBus.getDefault().register(this);
        mSelectedType = getIntent().getIntExtra(SHARE_TYPE_KEY,ShareTypeBean.SHARE_TYPE_SSP);

        initView();
        setupView();
    }

    @Override
    protected void onDestroy() {

        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(EventSharetypeSelected event){
        if(event != null){
            this.finish();
        }
    }

    private void setupView() {
        mSharetypeBackIb.setOnClickListener(this);
        mSharetypeAdapter = new ShareTypeAdapter(this,mSelectedType);
        mSharetypeListLv.setAdapter(mSharetypeAdapter);
    }

    private void initView() {
        mSharetypeBackIb = (ImageButton) findViewById(R.id.ib_sharetype_back);
        mSharetypeTitleTv = (TextView) findViewById(R.id.tv_sharetype_title);
        mSharetypeListLv = (ListView) findViewById(R.id.lv_sharetype_list);
    }

    @Override
    public void onClick(View view) {
        int vId = view.getId();
        if(vId == R.id.ib_sharetype_back){
            this.finish();
        }
    }
}
