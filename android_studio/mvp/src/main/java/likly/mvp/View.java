//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package likly.mvp;

public interface View<P extends Presenter> {
    P getPresenter();

    int initLayoutResId();
}
