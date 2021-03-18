package com.izheer.proxyretrofitdemo.click;

import android.app.Activity;
import android.view.View;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 实现OnClick点击事件的注入
 */
public class InjectOnClick {
    public static void init(Activity activity) {
        Class<? extends Activity> aClass = activity.getClass();
        Method[] methods = aClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(OnClick.class)) {

                Object proxy = Proxy.newProxyInstance(View.OnClickListener.class.getClassLoader(), new Class[]{View.OnClickListener.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                        return method.invoke(activity, args);
                    }
                });

                OnClick onClick = method.getAnnotation(OnClick.class);
                int[] ids = onClick.value();
                try {
                    for (int id : ids) {
                        View view = activity.findViewById(id);
                        Method methodOnClick = view.getClass().getMethod("setOnClickListener", View.OnClickListener.class);
                        methodOnClick.invoke(view,proxy);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
