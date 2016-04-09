package com.mobnote.util;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.volley.VolleyGlideModule;
import com.bumptech.glide.load.DecodeFormat;

import android.content.Context;

public class GlideConfiguration extends VolleyGlideModule {
	@Override
	public void applyOptions(Context context, GlideBuilder builder) {
		// Apply options to the builder here.
		builder.setDecodeFormat(DecodeFormat.PREFER_RGB_565);
	}
//
//	@Override
//	public void registerComponents(Context context, Glide glide) {
//		// register ModelLoaders here.
//	}
}
