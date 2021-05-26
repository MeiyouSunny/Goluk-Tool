//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package likly.mvp;

public interface Presenter<M extends Model, V extends View> {
    void setModel(M var1);

    void setView(V var1);

    M getModel();

    V getView();
}
