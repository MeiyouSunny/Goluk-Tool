package cn.com.mobnote.golukmobile.carrecorder.entity;

/**
 *
 * IPC设备存储状态
 *
 * 2015年4月3日
 *
 * @author xuhw
 */
public class DeviceState {
	/** SD卡总容量，单位为MB。仅当SDActive为YES时，此字段有意义 */
	public double totalSizeOnSD;
	/** 当前镜头状态。 1 –正常 2 –异常 */
	public int cameraStatus;
	/** SD卡剩余容量，单位为MB。仅当SDActive为YES时，此字段有意义。 */
	public double leftSizeOnSD;
	/** 当前登录用户数 */
	public int onlineUsers;
	/** SD卡在位YES(1) | SD卡不存在NO(0) */
	public int SDPresent;
	/** SD卡剩余容量太小，不能继续录制(1) | SD卡容量正常(0) */
	public int isSpaceTooSmall;
}