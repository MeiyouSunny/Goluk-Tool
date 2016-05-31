package com.goluk.ipcsdk.main;

/**
 * Created by leege100 on 16/5/30.
 */
public class GolukIPCSdk {
    public static GolukIPCSdk instance = new GolukIPCSdk();
    private GolukIPCSdk(){
        System.loadLibrary("golukmobile");
    }
    public static GolukIPCSdk getInstance(){
        return instance;
    }

}
