package com.mobnote.t1sp.base.control;

public class ViewControlBinder {

    public static void bind(Object target) {
        Class clazz = target.getClass();
        BindViewControl bindViewControl = (BindViewControl) clazz.getAnnotation(BindViewControl.class);
        if (bindViewControl == null) return;

        ViewControl control = null;
        for (Class<? extends ViewControl> vc : bindViewControl.value()) {

            try {
                control = vc.newInstance();
                control.onBindView(target);
                control.onViewBind();
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("The view control can not new an instance," + vc.getName());
            }

        }


    }
}
