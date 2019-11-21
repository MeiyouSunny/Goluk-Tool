package com.rd.veuisdk.utils;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import com.rd.vecore.models.AnimationObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：JIAN on 2017/12/1 14:11
 */
public class AnimHandler {


    public AnimHandler() {
    }

    /**
     * * 1、平移（推进、推出）
     * 2、放大缩小
     * 3、平移同时放大缩小
     * 4、旋转
     * 5、旋转同时放大缩小
     * 6、淡入淡出
     * 7、按照一定角度倾斜入场（不是轨迹运动，可以理解为对角线推进推出）
     * 8、加速度/减速度入场、出场
     */

    String TAG = "AnimHanlder";

    /**
     * @return 动画效果
     */
    public List<AnimationObject> createAnimList(int type, RectF showF, float duration) {
        List<AnimationObject> list = new ArrayList<>();
        if (type == 1) { //平移（推进、推出）
            AnimationObject object = new AnimationObject(0);
            RectF src = new RectF(showF);
            src.offset(0, src.height());
//            src.offset(-src.width(), 0);
            //设置透明度 0~1.0f(1.0f代表不透明)
            object.setAlpha(1f);//第0段动画的透明度变化过程1f->0.1f
            object.setRectPosition(src);
            list.add(object);

            object = new AnimationObject(duration / 2);
            object.setRectPosition(showF);
            object.setRotate(90);
            object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
            object.setAlpha(0.5f);//第1段动画的透明度渐变过程:0.1f->0.9f
            list.add(object);

            object = new AnimationObject(duration);
            RectF rectF = new RectF(src);
            object.setRotate(0);
            object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
            rectF.offset(0.5f, 0.2f);
            object.setRectPosition(rectF);
            object.setAlpha(0.9f);
            list.add(object);

        } else if (type == 2) {//     2、放大缩小


            AnimationObject object = new AnimationObject(0);
            RectF src = createRect(showF, 0.01f);
            object.setAlpha(0.2f);//第0段动画的透明度变化过程0.2f->0.9f
            object.setRectPosition(src);
            list.add(object);


            object = new AnimationObject(duration / 2);
            object.setRectPosition(new RectF(showF));
            object.setAlpha(0.9f);//第1段动画的透明度渐变过程:0.9f->0.3f
            list.add(object);


            object = new AnimationObject(duration);
            object.setRectPosition(new RectF(src));
            object.setAlpha(0.3f);
            list.add(object);


        } else if (type == 3) { //平移同时放大缩小


            AnimationObject object = new AnimationObject(0);
            RectF src = new RectF(showF);
            object.setAlpha(0.25f);
            src.offset(-src.width(), 0);
            RectF dst = createRect(src, 0.01f);
            object.setRectPosition(dst);
            list.add(object);


            object = new AnimationObject(duration / 2);
            object.setAlpha(0.5f);
            object.setRectPosition(new RectF(showF));
            list.add(object);


            object = new AnimationObject(duration);
            RectF end = new RectF(showF);
            end.offset(end.width(), 0);
            object.setRectPosition(createRect(end, 0.1f));

            list.add(object);

        } else if (type == 4) {//旋转


            AnimationObject object = new AnimationObject(0);
            object.setRotate(0);
            object.setRectPosition(new RectF(showF));
            list.add(object);


            object = new AnimationObject(duration / 2);
            object.setRotate(180);
            list.add(object);


            object = new AnimationObject(duration);
            object.setRotate(0);

            list.add(object);

        } else if (type == 5) { //旋转同时放大缩小

            AnimationObject object = new AnimationObject(0);
            object.setRotate(0);
            RectF dst = createRect(showF, 0.1f);
            object.setRectPosition(dst);
            list.add(object);


            object = new AnimationObject(duration / 2);
            object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
            object.setRotate(3 * 360);
            list.add(object);


            object = new AnimationObject(duration);
            object.setRectPosition(new RectF(dst));
            object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
            object.setRotate(0);
            list.add(object);


        } else if (type == 6) {//淡入淡出
            //模拟淡入淡出
            AnimationObject object = new AnimationObject(0);
            object.setAlpha(0);
            list.add(object);

            object = new AnimationObject(duration / 2);
            object.setAlpha(1);
            list.add(object);


            object = new AnimationObject(duration);
            object.setAlpha(0);
            list.add(object);


        } else if (type == 7) { //按照一定角度倾斜入场（不是轨迹运动，可以理解为对角线推进推出）


            AnimationObject object = new AnimationObject();
            object.setAtTime(0);
            object.setRotate(45);
            RectF src = new RectF(showF);
            src.offset(src.width(), 0);
            object.setRectPosition(new RectF(src));
            list.add(object);


            object = new AnimationObject(duration / 2);
            object.setRotate(45);
            list.add(object);


            object = new AnimationObject(duration);
            object.setRectPosition(new RectF(src));
            object.setRotate(45);
            list.add(object);


        } else if (type == 8) {//加速度/减速度入场、出场
            AnimationObject object = new AnimationObject(0);
            RectF src = new RectF(showF);
            src.offset(src.width(), 0);
            object.setRectPosition(src);
            list.add(object);


            object = new AnimationObject(duration / 2);
            object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.ACCELERATE);
            object.setRectPosition(showF);
            list.add(object);

            object = new AnimationObject(duration);
            object.setRectPosition(new RectF(src));
            object.setAnimationInterpolation(AnimationObject.AnimationInterpolation.DECELERATE);
            list.add(object);

        } else if (type == 100) {
            //测试异形动画
            list = new testQuad().build();

        } else if (type == 101) {
            //测试异形动画
//
            float item = 1 / 15.0f;

            int len = (int) (duration / (item));

            float pX = 0.5f / len;
            float pA = 1.0f / (len / 2);
            int pRotate = 360 * 5 / len;
            for (int i = 0; i < len; i++) {


                AnimationObject object = new AnimationObject((i * item));
                //每一帧的显示位置(每一帧交由用户控制，至少需要（时间、显示位置(4个顶点坐标) 两个参数))


                PointF pl = new PointF(pX * i, 0);
                int half = len / 2;
                if (i > half) {
                    object.setAlpha(1 + 0.5f - ((i - half) * pA));
                } else {
                    object.setAlpha(pA * i);
                }
                object.setRotate(pRotate * i);
                object.setShowPointFs(pl, new PointF(1, 0), new PointF(pX * i, 0.5f), new PointF(0.8f, 1f - pX * i));
//                Log.e(TAG, "createAnimList: " + i + ">>" + object.getAtTime() + ">>" + pl.toString() + ">>>" + duration + ">>" + object.getAlpha());

                list.add(object);

            }
        } else if (type == 102) {
            //测试异形动画
//
            float item = 1 / 15.0f;

            int len = (int) (duration / (item));

            float pX = 0.5f / len;
            float pA = 1.0f / (len / 2);
            int pRotate = 360 * 5 / len;
            for (int i = 0; i < len; i++) {


                AnimationObject object = new AnimationObject((i * item));
                //每一帧的显示位置(每一帧交由用户控制，至少需要（时间、显示位置(4个顶点坐标) 两个参数))


                PointF pl = new PointF(pX * i, 0.5f);
                int half = len / 2;
                if (i > half) {
                    object.setAlpha(1 + 0.5f - ((i - half) * pA));
                } else {
                    object.setAlpha(pA * i);
                }
                object.setRotate(pRotate * i);
                object.setShowPointFs(pl, new PointF(1, 0.6f), new PointF(pX * i, 0.98f), new PointF(0.9f, 0.85f));
//                Log.e(TAG, "createAnimList: " + i + ">>" + object.getAtTime() + ">>" + pl.toString() + ">>>" + duration + ">>" + object.getAlpha());

                list.add(object);

            }
        } else {
            int len = 2;
            for (int i = 0; i < len; i++) {
                AnimationObject object = new AnimationObject((i * duration / (len + 1)));
                object.setAlpha(0.1f + 0.3f * i);
                object.setRotate(30 + (350 * i / (len + 1)));

                float left = (float) Math.random();
                float top = (float) Math.random();
                object.setRectPosition(new RectF(left, top, left + 0.2f, top + 0.2f));
                list.add(object);

            }

        }
        return list;
    }

    /**
     * 放大缩小
     *
     * @param showRectF 原始显示位置
     * @param scale     缩放比例
     * @return
     */
    private RectF createRect(RectF showRectF, float scale) {
        if (scale >= 0) {

            Matrix temp = new Matrix();

            temp.postScale(scale, scale, showRectF.centerX(), showRectF.centerY());
            RectF dst = new RectF();
            temp.mapRect(dst, showRectF);
            temp = null;
            return dst;
        } else {
            return new RectF(showRectF);
        }

    }

    /**
     * 动画的总时间单位：秒 (如果是视频，必须与视频的预览时间一致，如果是图片可以任意指定)
     *
     * @param duration
     * @return
     */
    public ArrayList<AnimationObject> createAnimList2(float duration, RectF showF) {

        ArrayList<AnimationObject> list = new ArrayList<>();
        if (null == showF) {
            showF = new RectF(0, 0, 1f, 1f);
        }


        Log.e(TAG, "createAnimList2: " + duration);
        AnimationObject object = null;
        RectF src = null;
        for (float i = 0; i < duration; i += 2f) {
            //每个动画的时间点 i
            object = new AnimationObject(i);
            src = new RectF(showF);
            if (i == 0) {
                src.offset(src.width(), 0);
            } else {
                //模拟从右往左移
                src.offset(-(0.3f + i * 0.1f), 0);
            }
//            Log.e(TAG, "createAnimList2: " + src);
//            05-14 17:53:57.039 26187-26187/com.rd.ve.demo E/AnimHanlder: createAnimList2: 7.0
//            05-14 17:53:57.040 26187-26187/com.rd.ve.demo E/AnimHanlder: createAnimList2: RectF(1.0, 0.0, 2.0, 1.0)
//            05-14 17:53:57.040 26187-26187/com.rd.ve.demo E/AnimHanlder: createAnimList2: RectF(-0.3, 0.0, 0.7, 1.0)
//            05-14 17:53:57.040 26187-26187/com.rd.ve.demo E/AnimHanlder: createAnimList2: RectF(-0.3, 0.0, 0.7, 1.0)      X
//            05-14 17:53:57.040 26187-26187/com.rd.ve.demo E/AnimHanlder: createAnimList2: RectF(-0.3, 0.0, 0.7, 1.0)      X
            object.setRectPosition(src);

            object.setAlpha(0.3f + i * 0.2f);

            list.add(object);
        }
        return list;
    }

}
