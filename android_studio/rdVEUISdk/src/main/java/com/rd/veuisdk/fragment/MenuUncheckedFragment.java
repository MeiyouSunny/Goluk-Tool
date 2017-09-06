package com.rd.veuisdk.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rd.veuisdk.R;

/**
 * 未选中任何功能时的界面
 * 
 * @author scott
 * 
 */
public class MenuUncheckedFragment extends BaseFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View fragmentLayout = inflater.inflate(
				R.layout.fragment_unchecked_layout, null);
		return fragmentLayout;
	}

}
