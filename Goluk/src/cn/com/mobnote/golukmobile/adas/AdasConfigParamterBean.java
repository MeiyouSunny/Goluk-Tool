package cn.com.mobnote.golukmobile.adas;

import java.io.Serializable;

public class AdasConfigParamterBean implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int enable; 	/*adas启用（0关闭，1开启）*/
    //道路的交叉点
    public int point_x;        /* cross position to the left of the image, unit pixels.  */
    public int point_y;        /* cross position to the top of the image, unit pixels.  */

    //汽车信息
    public int height_offset;  /* camera height to the ground, unit cm. */
    public int wheel_offset;   /* camera offset to the front axle, unit cm. */
    public int head_offset;    /* camera height to the head of car, unit cm. */
    public int left_offset;    /* camera height to the left wheel, unit cm. */
    public int right_offset;   /* camera height to the right wheel, unit cm. */
    //报警级别
    public int ldw_warn_level; /* LDW warn level, 0-low, 1-middle, 2-high. 3-close*/
    public int fcw_warn_level; /* FCW warn level, 0-low, 1-middle, 2-high. 3-close*/
    public int osd;           /*adas 报警叠加到视频中  （0关闭，1开启）*/
    public int fcs_enable; /*前车起步推送开关*/
    public int fcw_enable; /*前车过近预警开关*/
}
