//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package likly.mvp;


public class BasePresenter<M extends Model, V extends View> implements Presenter<M, V> {
    private M model;
    private V view;

    public BasePresenter() {
    }

    public void setModel(M model) {
        this.model = model;
    }

    public void setView(V view) {
        this.view = view;
    }

    public M getModel() {
        return this.model;
    }

    public V getView() {
        return this.view;
    }
}
