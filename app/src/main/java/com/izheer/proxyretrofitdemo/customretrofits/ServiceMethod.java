package com.izheer.proxyretrofitdemo.customretrofits;

import com.izheer.proxyretrofitdemo.customretrofits.annotions.Field;
import com.izheer.proxyretrofitdemo.customretrofits.annotions.GET;
import com.izheer.proxyretrofitdemo.customretrofits.annotions.POST;
import com.izheer.proxyretrofitdemo.customretrofits.annotions.Query;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * 解析保存，拼接完整地址、请求方式类型、参数（key，value）
 */
public class ServiceMethod {


    private final HttpUrl mBaseHttpUrl;
    private final Call.Factory mFactory;
    private final String mHttpMethod;
    private final String mUrlPath;
    private final ParameterHandle[] mParameterHandles;
    private HttpUrl.Builder mUrlBudiler;
    FormBody.Builder formBuild;
    private final boolean mHasBody;

    public ServiceMethod(Builder builder) {
        mBaseHttpUrl = builder.mRetrofit.mBaseHttpUrl;
        mFactory = builder.mRetrofit.mFactory;

        mHttpMethod = builder.mHttpMethod;
        mUrlPath = builder.mUrlPath;
        mParameterHandles = builder.mParameterHandles;
        mHasBody = builder.hasBody;

        if (mHasBody) {
            formBuild = new FormBody.Builder();
        }
    }

    /**
     * 执行请求方法
     * @param args 请求携带的参数值
     * @return
     */
    public Object invoke(Object[] args) {

        //处理参数，key-value
        for (int i = 0; i < mParameterHandles.length; i++) {
            ParameterHandle parameterHandle = mParameterHandles[i];
            parameterHandle.apply(this,args[i].toString());
        }

        HttpUrl httpUrl;
        if (mUrlBudiler == null) {
            mUrlBudiler = mBaseHttpUrl.newBuilder(mUrlPath);
        }
        httpUrl = mUrlBudiler.build();


        FormBody formBody = null;
        if (formBuild != null) {
            formBody = formBuild.build();
        }

        Request request = new Request.Builder().url(httpUrl).method(mHttpMethod,formBody).build();
        return mFactory.newCall(request);
    }


    /**
     * post请求，key-value放至请求体；添加Field类型注解 的参数
     * * @param key
     * @param value
     */
    public void addFieldParameter(String key, String value) {
        formBuild.add(key,value);
    }

    /**
     * get请求，拼接key-value；添加Query类型注解的参数
     * @param key
     * @param value
     */
    public void addQueryParameter(String key, String value) {
        if (mUrlBudiler == null) {
            mUrlBudiler = mBaseHttpUrl.newBuilder(mUrlPath);
        }
        mUrlBudiler.addQueryParameter(key,value);
    }

    public static class Builder {


        public static final String POST = "POST";
        public static final String GET = "GET";
        private final CustomRetrofit mRetrofit;
        private final Annotation[] methodAnnotations;
        private final Annotation[][] parameterAnnotations;
        public boolean hasBody;
        private String mUrlPath;
        private String mHttpMethod;
        private ParameterHandle[] mParameterHandles;

        public Builder(CustomRetrofit customRetrofit, Method method) {

            mRetrofit = customRetrofit;
            methodAnnotations = method.getAnnotations(); //根据反射获取method方法上的注解
            parameterAnnotations = method.getParameterAnnotations();//根据反射获取method参数上的注解

        }

        public ServiceMethod build() {

            /**
             * 解析方法上的注解
             */
            for (Annotation methodAnnotation : methodAnnotations) {
                if (methodAnnotation instanceof POST) {
                    mHttpMethod = POST;
                    mUrlPath = ((POST) methodAnnotation).value();
                    this.hasBody = true;
                }else if (methodAnnotation instanceof GET){
                    mHttpMethod = GET;
                    mUrlPath = ((GET) methodAnnotation).value();
                    this.hasBody = false;
                }
            }

            /**
             * 解析方法参数的注解
             */
            int length = parameterAnnotations.length; //有几个方法参数
            //每个参数都需要一个 ParameterHandle 处理
            mParameterHandles = new ParameterHandle[length];

            for (int i = 0; i < length; i++) {
                Annotation[] annotations = parameterAnnotations[i]; //获取此参数上的所有注解
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Field) {
                        //若请求方式类型是 Get，而参数的注解又使用Field则提示非法，需要使用Query进行注解
                        if (mHttpMethod.equals(GET)) {
                            throw new IllegalStateException("Http Method is Get，the parameter annotation need use Query，not Field");
                        }

                        //获取参数上的注解
                        String paramKey = ((Field) annotation).value();

                        ParameterHandle fieldParameter = new ParameterHandle.FieldParameterHandle(paramKey);
                        mParameterHandles[i] = fieldParameter;
                    }else if (annotation instanceof Query){
                        //获取参数上的注解
                        String paramKey = ((Query) annotation).value();
                        ParameterHandle queryParameter = new ParameterHandle.QueryParameterHandle(paramKey);
                        mParameterHandles[i] = queryParameter;
                    }
                }
            }

            return new ServiceMethod(this);
        }
    }
}
