package com.rd.veuisdk.demo.zishuo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.rd.lib.utils.InputUtls;
import com.rd.veuisdk.R;
import com.rd.veuisdk.adapter.BaseRVAdapter;
import com.rd.veuisdk.demo.zishuo.StrokeEditView;
import com.rd.veuisdk.demo.zishuo.TempZishuoParams;
import com.rd.veuisdk.demo.zishuo.TextNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static android.view.View.LAYER_TYPE_SOFTWARE;

/**
 * 文字
 */
public class TextAdapter extends BaseRVAdapter<TextAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<TextNode> mTextNodes;
    private HashSet<Integer> mChoose = new HashSet<>();//选中下标
    //字体 描边宽度 描边颜色 阴影
    private String mFont;
    private float mStrokeWidth = 0;
    private String mStrokeColor = "#000000";
    private float mShadowAlpha = 0;
    //焦点
    private int etFocusPos = -1;

    public TextAdapter(Context context, ArrayList<TextNode> nodes) {
        this.mContext = context;
        this.mTextNodes = nodes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_text, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final TextNode node = mTextNodes.get(position);
        if (TextUtils.isEmpty(node.getText())) {
            holder.mCbChoose.setBackground(mContext.getResources().getDrawable(R.drawable.zishuo_plus));
            holder.mEtInput.setBackground(null);
            holder.mEtInput.setText("");
        } else {
            holder.mEtInput.setText(node.getText());
            if (mChoose.contains(position)) {
                //选中
                holder.mCbChoose.setBackground(mContext.getResources().getDrawable(R.drawable.zishuo_choose_p));
                holder.mEtInput.setBackground(mContext.getResources().getDrawable(R.drawable.item_text_bg));
            } else {
                holder.mCbChoose.setBackground(mContext.getResources().getDrawable(R.drawable.zishuo_choose_n));
                holder.mEtInput.setBackground(null);
            }
            holder.mCbChoose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mChoose.size() > 0) {
                        if (mChoose.contains(position)) {
                            mChoose.remove(position);
                        } else {
                            mChoose.add(position);
                        }
                        notifyItemChanged(position, 3);
                    } else {
                        mChoose.add(position);
                        etFocusPos = -1;
                        InputUtls.hideKeyboard(holder.mEtInput);
                        notifyDataSetChanged();
                    }
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position, 0);
                    }
                }
            });
        }
        //颜色
        holder.mEtInput.setTextColor(Color.parseColor(node.getColor()));
        //字体
        if (TextUtils.isEmpty(mFont)) {
            holder.mEtInput.setTypeface(Typeface.create(Typeface.DEFAULT,
                    Typeface.NORMAL));
        } else {
            holder.mEtInput.setTypeface(Typeface.createFromFile(mFont));
        }
        //阴影 关闭硬件加速
        holder.mEtInput.setLayerType(LAYER_TYPE_SOFTWARE, null);
        holder.mEtInput.setShadowLayer(mShadowAlpha * 5, 2, 2, Color.parseColor(mStrokeColor));
        //描边
        holder.mEtInput.setStrokeColor(mStrokeColor);
        holder.mEtInput.setStrokeWidth(mStrokeWidth);

        //当前holder是我们记录下的焦点位置时
        if (etFocusPos == position) {
            holder.mEtInput.requestFocus();
            holder.mEtInput.setSelection(holder.mEtInput.getText().length());
        }
        //我们给当前holder中的edittext添加touch事件监听，在action_up手指抬起时，记录下焦点position
        holder.mEtInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mState != 0) {
                        etFocusPos = -1;
                        if (mListener != null) {
                            mListener.onState(0);
                        }
                    } else {
                        etFocusPos = position;
                    }
                }
                return false;
            }
        });
        holder.mEtInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //判断 回车 如果光标在文字中 且 光标后面还有文字 就需要把后面的文字下一个  时间按比例计算
                //       删除 如果光标在文字最前面 且 上一个文字加上这一个少于8个 就应该合并 时间也要合并  上一句为空直接合并 这一句为空直接合并
                //       回车 如果光标在最后面 且 这一行和下一行有文字 就需要新建一个空的node 时间从上一个文字最后取得 取10%
                int start = holder.mEtInput.getSelectionStart();
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    //第一种情况 文字中
                    if (start > 0 && start < holder.mEtInput.getText().toString().length()) {
                        TextNode newNode = new TextNode();
                        TextNode oldNode = mTextNodes.get(position);
                        float center = (node.getEnd() - node.getBegin()) / node.getText().length();
                        newNode.setText(node.getText().substring(start));
                        oldNode.setText(node.getText().substring(0, start));
                        newNode.setEnd(node.getEnd());
                        oldNode.setEnd(node.getBegin() + center * start);
                        newNode.setBegin(oldNode.getEnd());

                        mTextNodes.add(position + 1, newNode);
                        notifyDataSetChanged();
                    } else if (start != 0 && start >= holder.mEtInput.getText().length()
                            && position < getItemCount() - 1
                            && !TextUtils.isEmpty(mTextNodes.get(position + 1).getText())) {
                        //第三种 文字最后面
                        TextNode newNode = new TextNode();
                        TextNode oldNode = mTextNodes.get(position);
                        newNode.setEnd(oldNode.getEnd());
                        float center = node.getEnd() - node.getBegin();
                        oldNode.setEnd(oldNode.getBegin() + center * 0.9f);
                        newNode.setBegin(oldNode.getEnd());

                        mTextNodes.add(position + 1, newNode);
                        notifyDataSetChanged();
                    }
                    etFocusPos++;
                } else if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    //删除
                    if (start == 0 && position > 0
                            && (TextUtils.isEmpty(mTextNodes.get(position).getText())//本句为空
                            || TextUtils.isEmpty(mTextNodes.get(position - 1).getText()) //上一句为空
                            || (mTextNodes.get(position - 1).getText().length() + mTextNodes.get(position).getText().length()) < 9)) { //保证文字最多8个字
                        //第二种情况
                        TextNode currentNode = mTextNodes.get(position);
                        TextNode upNode = mTextNodes.get(position - 1);
                        upNode.setEnd(currentNode.getEnd());
                        if (!TextUtils.isEmpty(currentNode.getText())) {
                            if (TextUtils.isEmpty(upNode.getText())) {
                                upNode.setText(currentNode.getText());
                            } else {
                                upNode.setText(upNode.getText() + currentNode.getText());
                            }
                        }
                        mTextNodes.remove(position);
                        etFocusPos--;
                        notifyDataSetChanged();
                    }
                }
                return false;
            }
        });
        holder.mEtInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
//                Log.d("ok", "焦点：" + etFocusPos + "==" + b);
                if (b && etFocusPos != -1){
                    InputUtls.showInput(view);
                    holder.mEtInput.setSelection(holder.mEtInput.getText().length());
                } else if (etFocusPos == -1){
                    InputUtls.hideKeyboard(view);
                }
            }
        });
        holder.mEtInput.addTextChangedListener(textWatcher);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            //刷新图片
            int type = (int) payloads.get(0);
            if (type == 1) {
                //文字由   不为空 --> 空
                holder.mCbChoose.setBackground(mContext.getResources().getDrawable(R.drawable.zishuo_plus));
            } else if (type == 2){
                //文字由  空 --> 不为空
                holder.mCbChoose.setBackground(mContext.getResources().getDrawable(R.drawable.zishuo_choose_n));
            } else if (type == 3) {
                //选择时  只需要把选择的刷新即可
                if (mChoose.contains(position)) {
                    //选中
                    holder.mCbChoose.setBackground(mContext.getResources().getDrawable(R.drawable.zishuo_choose_p));
                    holder.mEtInput.setBackground(mContext.getResources().getDrawable(R.drawable.item_text_bg));
                } else {
                    holder.mCbChoose.setBackground(mContext.getResources().getDrawable(R.drawable.zishuo_choose_n));
                    holder.mEtInput.setBackground(null);
                }
            }
        }
    }

    //监听文字变化的TextWatcher接口
    TextWatcher textWatcher = new TextWatcher() {

        boolean empty = true;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            empty = TextUtils.isEmpty(s.toString());
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            //保存输入的文字
            if (etFocusPos != -1) {
                mTextNodes.get(etFocusPos).setText(s.toString());
                if ((TextUtils.isEmpty(s) && !empty)){
                    notifyItemChanged(etFocusPos, 1);
                } else if (!TextUtils.isEmpty(s) && empty) {
                    notifyItemChanged(etFocusPos, 2);
                }
            }
        }
    };

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        //当前holder被销毁时，把holder的TextChangedListener删除
        holder.mEtInput.removeTextChangedListener(textWatcher);
    }

    @Override
    public int getItemCount() {
        return mTextNodes.size();
    }

    /**
     * 状态 1字体状态 2 描边状态 3阴影 4颜色设置
     *      文字编辑时 不为0就 先设置为0
     */
    public int mState = 0;

    public void setState(int state) {
        mState = state;
    }

    /**
     * 获取选中
     */
    public HashSet<Integer> getChoose() {
        return mChoose;
    }

    /**
     * 取消选择
     */
    public void cancelChoose(){
        mChoose.clear();
        notifyDataSetChanged();
    }

    /**
     * 全部选择
     */
    public void chooseAll(){
        mChoose.clear();
        for (int i = 0; i < mTextNodes.size(); i++) {
            mChoose.add(i);
        }
        notifyDataSetChanged();
    }

    /**
     * 设置字体颜色
     */
    public void setColor(String color) {
        Iterator<Integer> iterator = mChoose.iterator();
        while(iterator.hasNext()){
            mTextNodes.get(iterator.next()).setColor(color);
        }
        notifyDataSetChanged();
    }

    /**
     * 设置字体
     */
    public void setFont(String ttf){
       mFont = ttf;
       notifyDataSetChanged();
    }

    public void setStrokeWidth(float strokeWidth) {
        mStrokeWidth = strokeWidth;
        notifyDataSetChanged();
    }

    public void setStrokeColor(String strokeColor) {
        mStrokeColor = strokeColor;
        notifyDataSetChanged();
    }

    public void setShadowAlpha(float shadowAlpha) {
        if (shadowAlpha >= 1){
            mShadowAlpha = 0;
        } else if (shadowAlpha <= 0) {
            mShadowAlpha = 0.001f;
        } else {
            mShadowAlpha = shadowAlpha;
        }
        notifyDataSetChanged();
    }

    public void setChecked(int checkId) {
        lastCheck = checkId;
        notifyDataSetChanged();
    }

    public void save(){
        TextNode textNode;
        for (int i = 0; i < mTextNodes.size(); i++) {
            textNode = mTextNodes.get(i);
            textNode.setFont(mFont);
            textNode.setStrokeWidth(mStrokeWidth);
            textNode.setShadowAlpha(mShadowAlpha);
            textNode.setStrokeColor(mStrokeColor);
        }
        TempZishuoParams.getInstance().setTextNodes(mTextNodes);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        Button mCbChoose;
        StrokeEditView mEtInput;

        public ViewHolder(View itemView) {
            super(itemView);
            mCbChoose = itemView.findViewById(R.id.cb_choose);
            mEtInput = itemView.findViewById(R.id.et_input);
        }
    }

    public StateListener mListener;

    public void setListener(StateListener listener) {
        mListener = listener;
    }

    public interface StateListener {
        void onState(int state);
    }

}
