//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package likly.mvp;

import android.content.Context;

public interface ViewHandler<P extends Presenter> extends View<P> {
    Context getContext();

    void onViewCreated();
}
