package com.goluk.crazy.panda.search;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.goluk.crazy.panda.R;
import com.goluk.crazy.panda.common.activity.BaseActivity;
import com.goluk.crazy.panda.common.http.HttpMethods;
import com.goluk.crazy.panda.common.http.SearchService;
import com.goluk.crazy.panda.common.http.bean.HttpResultBean;
import com.goluk.crazy.panda.common.widget.HeaderBar;
import com.goluk.crazy.panda.search.bean.SearchDataBean;
import com.goluk.crazy.panda.search.bean.SearchUserInfoBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SearchActivity extends BaseActivity {

    @BindView(R.id.btn_search)
    Button mBtnSearch;

    @BindView(R.id.tv_username)
    TextView tvUsername;

    @BindView(R.id.headerbar_search)
    HeaderBar mSearchHeaderbar;

    Subscriber<SearchUserInfoBean> mSearchUserSubscriber;
    StringBuilder mUserNameStrBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        mSearchHeaderbar.setOnLeftClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(SearchActivity.this,"左边被点击",Toast.LENGTH_SHORT).show();
            }
        });
        mSearchHeaderbar.setOnRightClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(SearchActivity.this,"右边被点击",Toast.LENGTH_SHORT).show();
            }
        });
        mSearchHeaderbar.setOnCenterClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(SearchActivity.this,"中间被点击",Toast.LENGTH_SHORT).show();
            }
        });

        mUserNameStrBuilder = new StringBuilder();

        mSearchUserSubscriber = new Subscriber<SearchUserInfoBean>() {
            @Override
            public void onCompleted() {
                Log.i("SearchActivity", "onCompleted()");
            }

            @Override
            public void onError(Throwable e) {
                Log.i("SearchActivity", "onError()  msg: " + e.getMessage());
            }

            @Override
            public void onNext(SearchUserInfoBean searchUserInfoBean) {
                Log.i("SearchActivity", "onNext()");
                if (searchUserInfoBean == null) {
                    return;
                }
                mUserNameStrBuilder.append("userName: " + searchUserInfoBean.getNickname() + "   ");
                tvUsername.setText(mUserNameStrBuilder.toString());
            }
        };
    }

    @OnClick(R.id.btn_search)
    public void onClick() {

        Log.i("SearchActivity", "onClick()");
        HttpMethods.getInstance().searchUser("le", String.valueOf(1), 20, "2.0", "en")
                .flatMap(new Func1<SearchDataBean, Observable<SearchUserInfoBean>>() {
                    @Override
                    public Observable<SearchUserInfoBean> call(SearchDataBean searchDataBean) {
                        if (searchDataBean != null) {
                            return Observable.from(searchDataBean.getUserlist());
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSearchUserSubscriber);

    }
}
