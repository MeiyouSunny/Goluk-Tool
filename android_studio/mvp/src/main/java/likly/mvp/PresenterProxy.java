//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package likly.mvp;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class PresenterProxy {
    private final MVP mMVP;
    private final Presenter mPresenter;
    private final Presenter mProxy;

    public PresenterProxy(MVP MVP, Presenter presenter) {
        this.mMVP = MVP;
        this.mPresenter = presenter;
        this.mProxy = (Presenter)Proxy.newProxyInstance(presenter.getClass().getClassLoader(), presenter.getClass().getInterfaces(), new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Log.e("METHOD:", method.getName());
                if ("getView".equals(method.getName())) {
                    return PresenterProxy.this.mMVP.getView();
                } else {
                    return "getModel".equals(method.getName()) ? PresenterProxy.this.mMVP.getModel() : method.invoke(PresenterProxy.this.mPresenter, args);
                }
            }
        });
    }

    public Presenter getProxyPresenter() {
        return this.mProxy;
    }
}
