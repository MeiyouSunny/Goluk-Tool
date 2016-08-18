package com.goluk.crazy.panda.search;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.goluk.crazy.panda.R;
import com.goluk.crazy.panda.common.activity.BaseActivity;
import com.goluk.crazy.panda.common.http.SearchService;
import com.goluk.crazy.panda.common.http.bean.HttpResultBean;
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

    Subscriber<SearchUserInfoBean> mSearchUserSubscriber;
    Retrofit mRetrofit;
    @BindView(R.id.tv_username)
    TextView tvUsername;

    StringBuilder mUserNameStrBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        String baseUrl = "http://server.goluk.cn/cdcSearch/";

        mUserNameStrBuilder = new StringBuilder();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        mSearchUserSubscriber = new Subscriber<SearchUserInfoBean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.i("SearchActivity", "name: " + e.getMessage());
            }

            @Override
            public void onNext(SearchUserInfoBean searchUserInfoBean) {
                if (searchUserInfoBean == null) {
                    return;
                }
                Log.i("SearchActivity", "errorCode: " + searchUserInfoBean.getNickname());
                mUserNameStrBuilder.append("userName: " + searchUserInfoBean.getNickname() + "   ");
                tvUsername.setText(mUserNameStrBuilder.toString());
            }
        };
    }

    @OnClick(R.id.btn_search)
    public void onClick() {

        Log.i("baseUrl", "baseUrl: " + "http://server.goluk.cn/cdcSearch/");
        SearchService searchService = mRetrofit.create(SearchService.class);
        searchService.searchUser("searchUser", "200", "le", String.valueOf(1), 20, "2.0", "en")
                .map(new Func1<HttpResultBean<SearchDataBean>, SearchDataBean>() {
                    @Override
                    public SearchDataBean call(HttpResultBean<SearchDataBean> searchDataBeanHttpResultBean) {
                        if (searchDataBeanHttpResultBean == null) {
                            mSearchUserSubscriber.onError(new Throwable("net error"));
                            return null;
                        }
                        if (searchDataBeanHttpResultBean.getCode() != 0) {
                            mSearchUserSubscriber.onError(new Throwable("" + searchDataBeanHttpResultBean.getCode()));
                            return null;
                        }
                        return searchDataBeanHttpResultBean.getData();
                    }
                })
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
