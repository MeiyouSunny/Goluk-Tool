package com.rd.veuisdk.utils;

import com.facebook.imagepipeline.nativecode.WebpTranscoderImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WebpUtils {


    public static void locWebpSaveToLocPng(String webpName, String pngName) {


        WebpTranscoderImpl imp = new WebpTranscoderImpl();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(webpName);
            fos = new FileOutputStream(pngName);
            imp.transcodeWebpToPng(fis, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (null != fos) {
                    fos.flush();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
                if (null != fis) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
