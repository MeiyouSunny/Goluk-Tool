package cn.com.mobnote.module.location;

public class GolukPosition {
	/** 　百度定位给的原始定位数据 (目前是一样的) */
	public double elon;
	public double elat;
	public double rawLon;
	public double rawLat;
	/** 速度 */
	public double speed;
	/** 方向 */
	public double course;
	/** 海拔 */
	public double altitude;
	/** 半径 */
	public double radius;
	public double accuracy;// 暂时没用
	/** 定位类型 LC_TYPE_BAIDU = 0 */
	public int locationType;

}
