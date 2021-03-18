package com.izheer.proxyretrofitdemo.customretrofits;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * 实现类似Retrofit的基础用法
 */
public class CustomRetrofit {

    Map<Method,ServiceMethod> serviceMethodMap = new ConcurrentHashMap<>();
     Call.Factory mFactory;
     HttpUrl mBaseHttpUrl;

    public CustomRetrofit(Call.Factory factory, HttpUrl baseHttpUrl) {
        this.mFactory = factory;
        this.mBaseHttpUrl = baseHttpUrl;
    }

    public <T> T create(Class<T> t) {
        return (T) Proxy.newProxyInstance(t.getClassLoader(), new Class[]{t}, (proxy, method, args) -> {
            //解析method的注解信息；请求方式、请求参数key
            ServiceMethod serviceMethod = createServiceMethod(method);
            return serviceMethod.invoke(args); //传递参数值，执行该方法
        });
    }

    private ServiceMethod createServiceMethod(Method method) {
        //先不上锁，避免synchronized的性能损失
        ServiceMethod serviceMethod = serviceMethodMap.get(method);
        if (serviceMethod != null) {
            return serviceMethod;
        }
        //避免在多线程下重复解析
        synchronized (serviceMethodMap){
            serviceMethod = serviceMethodMap.get(method);
            if (serviceMethod == null) {
                serviceMethod = new ServiceMethod.Builder(this,method).build();
                serviceMethodMap.put(method,serviceMethod);
            }
        }
        return serviceMethod;
    }

    public static class Builder {

        private HttpUrl baseHttpUrl;
        private Call.Factory factory;

        public Builder() {

        }

        public Builder baseUrl(String baseUrl) {
            Objects.requireNonNull(baseUrl);
            baseHttpUrl = HttpUrl.get(baseUrl);
            return this;
        }

        public Builder callFactory(Call.Factory factory) {
            this.factory = factory;
            return this;
        }

        public CustomRetrofit build() {
            if (baseHttpUrl == null) {
                throw new IllegalStateException("Base url is null,this is required");
            }

            Call.Factory factory = this.factory;
            if (factory == null) {
                factory = new OkHttpClient();
            }

            return new CustomRetrofit(factory, baseHttpUrl);
        }
    }


}
