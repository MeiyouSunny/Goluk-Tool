package com.mobnote.golukmain.livevideo.bean;

public class StartLiveBean {
	/** 必选，rtmp 推流地址，webencode编码（先base64编码，然后url编码） */
	public String url;
	/** 可选，通道，0，高清，1，标清 */
	public String stream;
	/** 推流持续时间，单位秒，默认1800秒，30分钟。时间结束后停止推流。如果需要重新开始推流，任然需要先stop */
	public String time;
	/** 直播是否开启声音 */
	public boolean isVoice;

}
