package com.rd.veuisdk.demo.zishuo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rd.veuisdk.R;
import com.rd.veuisdk.demo.zishuo.adapter.InscriptionAdapter;
import com.rd.veuisdk.fragment.BaseFragment;
import com.rd.veuisdk.hb.views.MyRefreshLayout;
import com.rd.veuisdk.listener.OnItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 题词库
 */
public class InscriptionFragment extends BaseFragment {

    private static final String TYPE_KEY = "type";
    private MyRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private String mType = null;
    private ArrayList<Inscription> mInscriptions = new ArrayList<>();
    private InscriptionAdapter mAdapter;

    public static InscriptionFragment newInstance(String type) {
        InscriptionFragment fragment = new InscriptionFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TYPE_KEY, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(TYPE_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_inscription, container, false);
        initView();
        init();
        return mRoot;
    }

    private void initView() {
        mRefreshLayout = $(R.id.swipe_inscription);
        mRecyclerView = $(R.id.rv_inscription);
    }

    private void init() {
        //recycler
        mAdapter = new InscriptionAdapter(getContext(), mInscriptions);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, Object item) {
                if (position == -1) {
                    TempZishuoParams.getInstance().setStrings(((Inscription) item).getContent());
                    if (mListener != null) {
                        mListener.sure();
                    }
                } else if (position == -2) {
                    if (mListener != null) {
                        mListener.onDetail(((Inscription) item).getTitle(), ((Inscription) item).getContent());
                    }
                }
            }
        });
        //加载
        mRefreshLayout.setOnLoadListener(new MyRefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                // 加载完后调用该方法 加载完毕
                mRefreshLayout.noLoad();
            }
        });
        //下拉
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 更新完后调用该方法结束刷新
                mRefreshLayout.setRefreshing(false);
            }
        });
        //上拉继续加载
        mRefreshLayout.onUpPromat();
        getData();
    }

    //获取数据
    private void getData() {
        if (TextUtils.isEmpty(mType)) {
            return;
        }
        mInscriptions.clear();
        //测试 添加几个
        StringBuilder newstringBuilder = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = getContext().getAssets().open("inscription.json");
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(isr);
            String jsonLine;
            while ((jsonLine = reader.readLine()) != null) {
                newstringBuilder.append(jsonLine);
            }
            reader.close();
            isr.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        onParseJson(newstringBuilder .toString(), mType);
    }

    //解析数据
    private void onParseJson(String result, String type) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jobj = jsonArray.getJSONObject(i);
                if (jobj.getString("type").equals(type)) {
                    JSONArray jarr = jobj.getJSONArray("data");
                    if (jarr != null) {
                        Inscription inscription;
                        for (int j = 0; j < jarr.length(); j++) {
                            inscription = new Inscription();
                            JSONObject object = jarr.getJSONObject(j);
                            inscription.setTitle(object.getString("title"));
                            inscription.setNum(object.getString("num"));
                            JSONArray content = object.getJSONArray("content");
                            ArrayList<String> s = new ArrayList<>(content.length());
                            for (int k = 0; k < content.length(); k++) {
                                s.add(content.getString(k));
                            }
                            inscription.setContent(s);
                            mInscriptions.add(inscription);
                        }
                    }
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class Inscription {

        private String title;
        private ArrayList<String> content;
        private String num;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public ArrayList<String> getContent() {
            return content;
        }

        public void setContent(ArrayList<String> content) {
            this.content = content;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }
    }

    private OnInscriptionClickListener mListener;

    public void setListener(OnInscriptionClickListener listener) {
        mListener = listener;
    }

    public interface OnInscriptionClickListener {

        /**
         * 使用
         */
        void sure();

        /**
         * 查看详情
         */
        void onDetail(String title, ArrayList<String> content);

    }

}
