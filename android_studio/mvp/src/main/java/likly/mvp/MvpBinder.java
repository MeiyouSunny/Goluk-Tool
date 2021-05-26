//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package likly.mvp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.LayoutRes;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MvpBinder {
    @LayoutRes
    int value() default -1;

    Class<? extends Presenter> presenter() default BasePresenter.class;

    Class<? extends Model> model() default SimpleModel.class;
}
