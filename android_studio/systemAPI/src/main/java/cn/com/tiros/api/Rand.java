package cn.com.tiros.api;

import java.util.Random;

public class Rand {

	private static Random mRand = new Random();
	
	public static void sys_srand(int seed){
		mRand.setSeed(seed);
	}
	
	public static int sys_rand(){
		return mRand.nextInt();
	}
	
}
