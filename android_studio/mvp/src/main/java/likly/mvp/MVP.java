//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package likly.mvp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class MVP {
    private View mView;
    private MvpBinder binder;
    private Model mModel;
    private Presenter mPresenter;
    private static OnViewBindListener onViewBindListener;

    public MVP() {
    }

    public void bind(View view) {
        this.initView(view);
    }

    public android.view.View bind(View view, LayoutInflater inflater, ViewGroup container) {
        this.initView(view);
        android.view.View content = inflater.inflate(this.getLayoutResId(), container, false);
        return content;
    }

    void onCreate() {
        if (onViewBindListener != null) {
            onViewBindListener.onCreate();
        }

    }

    public void onBind(View view) {
        if (onViewBindListener != null) {
            onViewBindListener.onViewBind(view);
        }

    }

    void onStart() {
        if (onViewBindListener != null) {
            onViewBindListener.onStart();
        }

    }

    void onResume() {
        if (onViewBindListener != null) {
            onViewBindListener.onResume();
        }

    }

    void onPause() {
        if (onViewBindListener != null) {
            onViewBindListener.onPause();
        }

    }

    void onStop() {
        if (onViewBindListener != null) {
            onViewBindListener.onStop();
        }

    }

    void onDestroy() {
        if (onViewBindListener != null) {
            onViewBindListener.onDestroy();
        }

    }

    public View getView() {
        return this.mView;
    }

    public Model getModel() {
        return this.mModel;
    }

    Presenter getPresenter() {
        return this.mPresenter;
    }

    private void initView(View holder) {
        this.mView = holder;
        Class clazz = holder.getClass();
        this.binder = (MvpBinder)clazz.getAnnotation(MvpBinder.class);
        if (this.binder == null) {
            Log.e("MVP", "The mView " + clazz.getName() + " has no MvpBinder annotation");
        } else {
            Class<? extends Presenter> p = this.binder.presenter();
            if (p.isInterface()) {
                throw new IllegalArgumentException(String.format("The mView presenter can not be a interface,mView=%s,presenter=%s", clazz.getName(), p.getName()));
            } else {
                try {
                    this.mPresenter = (Presenter)p.newInstance();
                } catch (Exception var7) {
                    throw new IllegalArgumentException(String.format("The mView's presenter can not be new instance cause by %s,mView=%s,presenter=%s", var7.getCause(), clazz.getName(), p.getName()));
                }

                Class<? extends Model> m = this.binder.model();
                if (m.isInterface()) {
                    throw new IllegalArgumentException(String.format("The mView's model can not be a interface,mView=%s,model=%s", clazz.getName(), m.getName()));
                } else {
                    try {
                        this.mModel = (Model)m.newInstance();
                    } catch (Exception var6) {
                        throw new IllegalArgumentException(String.format("The mView's model can not be new instance cause by %s,mView=%s,model=%s", var6.getCause(), clazz.getName(), m.getName()));
                    }

                    this.mPresenter.setModel(this.mModel);
                    this.mPresenter.setView(holder);
                }
            }
        }
    }

    private int getLayoutResId() {
        int layout = -1;
        if (this.mView instanceof ViewHandler) {
            layout = this.mView.initLayoutResId();
        }

        if (layout == -1) {
            throw new IllegalArgumentException("The library module must override the initLayoutResId");
        } else {
            return layout;
        }
    }

    public static void registerOnViewBindListener(OnViewBindListener onViewBindListener) {
        MVP.onViewBindListener = onViewBindListener;
    }

    public interface OnViewBindListener {
        void onViewBind(View var1);

        void onCreate();

        void onStart();

        void onResume();

        void onPause();

        void onStop();

        void onDestroy();
    }
}
