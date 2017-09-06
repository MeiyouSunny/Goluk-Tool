package com.rd.veuisdk;

import android.content.Context;

import com.rd.veuisdk.model.TransitionInfo;

import java.util.ArrayList;

/**
 * 切换转场辅助类
 * Created by JIAN on 2017/7/10.
 */

class TransitionHandler {
    private Context mContext;

    public TransitionHandler(Context context) {
        mContext = context;
        list = new ArrayList<>();
    }

    private ArrayList<TransitionInfo> list;


    public ArrayList<TransitionInfo> getList() {
        return list;
    }

    public void init() {
        int nId = 0;
        list.clear();
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_null), "asset:///transition/transition_null_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_recovery), "asset:///transition/transition_recovery_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_to_up), "asset:///transition/transition_to_up_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_to_down), "asset:///transition/transition_to_down_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_to_left), "asset:///transition/transition_to_left_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_to_right), "asset:///transition/transition_to_right_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_flash_white), "asset:///transition/transition_flash_white_normal.png"));
        list.add(new TransitionInfo(nId++, mContext.getString(R.string.show_style_item_flash_black), "asset:///transition/transition_flash_black_normal.png"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_003.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_004.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_005.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_006.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_007.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_008.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_009.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_012.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_014.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_015.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_016.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_017.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_018.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_019.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_020.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_021.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_022.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_023.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_024.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_025.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_026.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_027.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_028.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_030.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_031.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_032.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_033.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_034.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_035.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_036.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_037.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_038.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_039.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_040.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_041.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_042.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_043.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_044.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_045.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_047.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_048.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_049.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_050.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_051.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_052.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_053.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_054.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_055.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_056.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_057.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_058.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_059.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_060.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_061.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_062.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_063.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_064.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_065.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_066.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_067.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_068.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_069.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_070.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_071.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_072.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_073.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_074.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_075.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_076.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_077.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_078.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_079.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_080.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_081.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_082.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_083.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_084.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_085.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_086.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_087.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_088.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_089.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_090.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_091.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_092.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_093.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_094.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_095.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_096.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_097.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_098.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_099.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_100.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_101.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_102.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_103.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_104.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_105.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_106.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_107.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_108.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_109.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_110.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_111.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_112.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_113.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_114.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_115.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_116.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_117.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_118.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_119.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_120.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_121.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_122.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_123.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_124.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_125.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_126.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_127.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_128.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_129.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_130.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_131.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_132.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_133.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_134.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_135.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_136.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_137.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_138.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_139.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_140.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_141.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_142.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_143.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_144.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_145.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_146.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_147.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_148.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_149.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_150.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_151.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_152.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_153.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_154.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_155.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_156.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_157.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_158.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_159.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_160.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_161.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_162.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_163.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_164.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_165.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_166.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_168.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_169.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_170.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_171.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_172.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_173.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_174.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_175.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_176.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_177.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_178.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_179.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_180.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_181.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_182.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_183.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_184.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_185.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_186.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_187.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_188.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_189.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_190.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_191.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_192.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_193.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_194.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_195.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_196.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_197.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_198.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_199.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_200.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_201.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_202.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_203.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_205.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_207.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_208.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_209.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_210.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_211.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_212.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_213.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_214.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_215.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_216.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_217.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_218.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_219.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_220.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_221.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_222.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_223.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_224.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_225.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_226.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_227.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_228.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_229.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_230.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_231.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_232.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_233.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_234.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_235.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_236.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_237.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_238.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_239.JPG"));

        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_240.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_241.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_242.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_243.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_244.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_245.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_246.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_247.JPG"));
        list.add(new TransitionInfo(nId++, nId + "", "asset:///transition/transition_248.JPG"));


    }


    public void recycle() {
        if (null != list) {
            list.clear();
            list = null;
        }
    }


}
