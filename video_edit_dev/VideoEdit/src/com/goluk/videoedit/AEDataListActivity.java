package com.goluk.videoedit;

import java.util.ArrayList;
import java.util.List;

import com.goluk.videoedit.adapter.AEDataAdapter;
import com.goluk.videoedit.bean.AEDataBean;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class AEDataListActivity extends Activity {
	RecyclerView mRecyclerView;
	LinearLayoutManager mLayoutManager;
	List<AEDataBean> mAEdataList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ae_list);

		mLayoutManager = new LinearLayoutManager(this);
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mRecyclerView = (RecyclerView) findViewById(R.id.rv_video_edit_pic_list);
		mRecyclerView.setLayoutManager(mLayoutManager);

		mAEdataList = new ArrayList<AEDataBean>();
		mAEdataList.add(new AEDataBean(0));

		AEDataAdapter mAEAdapter = new AEDataAdapter(this,mRecyclerView, mAEdataList);
		mRecyclerView.setAdapter(mAEAdapter);

	}

}