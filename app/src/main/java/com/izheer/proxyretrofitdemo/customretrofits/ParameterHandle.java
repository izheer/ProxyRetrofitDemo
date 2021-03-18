package com.izheer.proxyretrofitdemo.customretrofits;

/**
 * 处理保存参数
 */
public abstract class ParameterHandle {
    abstract void apply(ServiceMethod serviceMethod,String value);


    /**
     * 处理Query类型注解的参数
     */
    static class QueryParameterHandle extends ParameterHandle{

        private final String mKey;

        public QueryParameterHandle(String key) {
            mKey = key;
        }

        @Override
        void apply(ServiceMethod serviceMethod, String value) {
            serviceMethod.addQueryParameter(mKey,value);
        }
    }

    /**
     * 处理Field类型注解的参数
     * 保存 key，value
     */
    static class FieldParameterHandle extends ParameterHandle{


        private final String mKey;

        public FieldParameterHandle(String key) {
            mKey = key;
        }

        @Override
        void apply(ServiceMethod serviceMethod, String value) {

            serviceMethod.addFieldParameter(mKey,value);
        }
    }
}
