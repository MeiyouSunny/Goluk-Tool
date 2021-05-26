//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package likly.mvp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity<P extends Presenter> extends AppCompatActivity implements ViewHandler<P> {
    private MVP mMVP;

    public BaseActivity() {
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMVP = new MVP();
        this.mMVP.onCreate();
        this.mMVP.bind(this);
    }

    public P getPresenter() {
        return (P) mMVP.getPresenter();
    }

    public int initLayoutResId() {
        return -1;
    }

    public Context getContext() {
        return this;
    }

    public void onStart() {
        super.onStart();
        this.mMVP.onStart();
    }

    public void onResume() {
        super.onResume();
        this.mMVP.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
        this.mMVP.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mMVP.onDestroy();
    }
}
