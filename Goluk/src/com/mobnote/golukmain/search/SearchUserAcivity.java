package com.mobnote.golukmain.search;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.mobnote.golukmain.BaseActivity;
import com.mobnote.golukmain.R;

public class SearchUserAcivity extends BaseActivity{

	private TextView mCancelTv;
	private ImageView mSearchDeleteIv;
	private EditText mSearchContentEt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_user);

		mCancelTv = (TextView) findViewById(R.id.tv_search_cancel);
		mSearchDeleteIv = (ImageView) findViewById(R.id.iv_search_delete);
		mSearchContentEt = (EditText) findViewById(R.id.et_search_content);

		mSearchDeleteIv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mSearchContentEt.setText("");;
			}
		});
		mCancelTv.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				SearchUserAcivity.this.finish();
			}});
		mSearchContentEt.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId==EditorInfo.IME_ACTION_SEARCH){
					
				}
				return true;
			}
		});
	}

}
