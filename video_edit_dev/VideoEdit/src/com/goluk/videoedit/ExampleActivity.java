package com.goluk.videoedit;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import com.makeramen.dragsortadapter.example.ExampleAdapter;

public class ExampleActivity extends Activity {
	RecyclerView mRecyclerView;
	LinearLayoutManager mLayoutManager;
	Handler mPlaySyncHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_example);

		int dataSize = 100;
		List<Integer> data = new ArrayList<>(dataSize);
		for (int i = 1; i < dataSize + 1; i++) {
			data.add(i);
		}

		mLayoutManager = new LinearLayoutManager(this);
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		mRecyclerView = (RecyclerView) findViewById(R.id.rv_video_edit_pic_list);
		mRecyclerView.setAdapter(new ExampleAdapter(mRecyclerView, data));
		mRecyclerView.setLayoutManager(mLayoutManager);
//		recyclerView.setItemAnimator(new DefaultItemAnimator());

		mPlaySyncHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 3003:
					mRecyclerView.post(new Runnable() {
					@Override
					public void run() {
						mRecyclerView.scrollBy(20, 0);
					}
				});
					break;
				default:
					break;
				}
				super.handleMessage(msg);
			}
		};

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				for(int i = 0; i < 1000; i++) {
					mRecyclerView.post(new Runnable() {
						@Override
						public void run() {
							mRecyclerView.scrollBy(20, 0);
						}
					});
//					mPlaySyncHandler.removeMessages(3003);
//					Message msg = mPlaySyncHandler.obtainMessage(3003);
//					mPlaySyncHandler.sendMessage(msg);
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		thread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.example, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.action_layout_grid:
//			item.setChecked(true);
//			recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
//			break;
//		case R.id.action_layout_linear:
//			item.setChecked(true);
////			LinearLayoutManager layoutManager = new LinearLayoutManager(this);
////			layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//			recyclerView.setLayoutManager(layoutManager);
//
//			recyclerView.post(new Runnable() {
//				@Override
//				public void run() {
//					// call smooth scroll
//					// recyclerView.smoothScrollToPosition(adapter.getItemCount());
//					recyclerView.scrollBy(200, 0);
//				}
//			});
//			break;
//		case R.id.action_layout_staggered:
//			item.setChecked(true);
//			recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3,
//					StaggeredGridLayoutManager.VERTICAL));
//			break;
//		}
		return super.onOptionsItemSelected(item);
	}
}
