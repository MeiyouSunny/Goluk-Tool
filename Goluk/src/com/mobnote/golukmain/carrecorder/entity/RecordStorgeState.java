package com.mobnote.golukmain.carrecorder.entity;

/**
 *
 * 录制存储状态
 *
 * 2015年4月7日
 *
 * @author xuhw
 */
public class RecordStorgeState {
	/** SD卡是否在位:YES(1)|NO(0)。仅当SD卡在位时，下列字段才有意义。 */
	public int SDCardActive;
	/** SD卡剩余容量太小，不能继续录制(1) | SD卡容量正常(0) */
	public int isSpaceTooSmall;
	/** SD卡总容量，单位为MB */
	public double totalSdSize;
	/** 用户自有文件大小，单位为MB */
	public double userFilesSize;
	/** SD卡剩余容量，单位为MB */
	public double leftSize;
	/** 行驶录像配额上限，单位为MB */
	public double normalRecQuota;
	/** 当前行驶录像总容量，单位为MB */
	public double normalRecSize;
	/** 紧急录像配额上限，单位为MB */
	public double urgentRecQuota;
	/** 当前紧急录像总容量，单位为MB */
	public double urgentRecSize;
	/** 精彩录像配额上限，单位为MB */
	public double wonderfulRecQuota;
	/** 当前精彩录像总容量，单位为MB */
	public double wonderfulRecSize;
	/** 抓图配额上限，单位为MB */
	public double picQuota;
	/** 当前抓图总容量，单位为MB */
	public double picSize;
}
