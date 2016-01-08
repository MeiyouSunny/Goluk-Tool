package cn.com.mobnote.golukmobile.adas;

import java.io.Serializable;

public class VehicleParamterBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String name;
	public int height_offset;  /* camera height to the ground, unit cm. */
	public int wheel_offset;   /* camera offset to the front axle, unit cm. */
	public int head_offset;    /* camera height to the head of car, unit cm. */
	public int left_offset;    /* camera height to the left wheel, unit cm. */
	public int right_offset;   /* camera height to the right wheel, unit cm. */
}
