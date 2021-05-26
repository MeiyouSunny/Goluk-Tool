//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package likly.mvp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BaseFragment<P extends Presenter> extends Fragment implements ViewHandler<P> {
    private MVP mMVP;

    public BaseFragment() {
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return this.mMVP.bind(this, inflater, container);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mMVP.onBind(this);
        this.onViewCreated();
    }

    public P getPresenter() {
        return (P) mMVP.getPresenter();
    }

    public int initLayoutResId() {
        return -1;
    }

    public void onViewCreated() {
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMVP = new MVP();
        this.mMVP.onCreate();
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
