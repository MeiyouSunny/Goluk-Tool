package com.goluk.videoedit.bean;

public class AEDataBean {

//	/** 段落 */
//	private final int VIEW_TYPE_SECTION = 0;
//	/** 转场 */
//	private final int VIEW_TYPE_TRANSFER = 1;

	private int mType;

	private String mSectionPath;

	private String mTransferName;

	public AEDataBean(int type){

		this.mType = type;
	}

	public String getmSectionPath() {
		return mSectionPath;
	}

	public void setmSectionPath(String mSectionPath) {
		this.mSectionPath = mSectionPath;
	}

	public String getmTransferName() {
		return mTransferName;
	}

	public void setmTransferName(String mTransferName) {
		this.mTransferName = mTransferName;
	}

//	public boolean isSection(){
//
//		if(VIEW_TYPE_SECTION == mType)return true;
//		return false;
//	}
//
//	public boolean isTransfer(){
//
//		if(VIEW_TYPE_TRANSFER == mType)return true;
//		return false;
//	}

}
