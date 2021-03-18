package com.izheer.proxyretrofitdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.izheer.proxyretrofitdemo.click.InjectOnClick;
import com.izheer.proxyretrofitdemo.click.OnClick;
import com.izheer.proxyretrofitdemo.customretrofits.CustomRetrofit;
import com.izheer.proxyretrofitdemo.customretrofits.CustomServiceApi;
import com.izheer.proxyretrofitdemo.databinding.ActivityMainBinding;
import com.izheer.proxyretrofitdemo.retrofits.ServiceApi;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements Handler.Callback {

    private static final String BASE_URL = "https://restapi.amap.com";
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int GET_RETROFIT = 1001;
    private static final int POST_RETROFIT = 1002;
    public static final int CUSTOM_GET = 1003;
    private static final int CUSTOM_POST = 1004;
    private ActivityMainBinding mBinding;
    private String city = "110101";
    private String key = "ae6c53e2186f33bbf240a12d80672d1b";
    private Handler mHandler;

    private ServiceApi mServiceApi;
    private CustomServiceApi mCustomServiceApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mHandler = new Handler(this::handleMessage);

        InjectOnClick.init(this);

//        setListener();

        /**
         * 使用Retrofit实现网络请求
         */
        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).build();
        mServiceApi = retrofit.create(ServiceApi.class);

        /**
         * 使用自定义的Retrofit实现网络请求
         */
        CustomRetrofit customRetrofit = new CustomRetrofit.Builder().baseUrl(BASE_URL).build();
        mCustomServiceApi = customRetrofit.create(CustomServiceApi.class);

    }

    private void setListener() {
        mBinding.btnGet1.setOnClickListener(v -> {
            //Retrofit Get 请求
            Call<ResponseBody> weatherCall = mServiceApi.getWeather(city, key);
            addCall(weatherCall, GET_RETROFIT);
        });
        mBinding.btnPost1.setOnClickListener(v -> {
            //Retrofit Post 请求
            Call<ResponseBody> weatherCall = mServiceApi.postWeather(city, key);
            addCall(weatherCall, POST_RETROFIT);
        });


        mBinding.btnGet2.setOnClickListener(v -> {
            //自定义Retrofit Get 请求
            okhttp3.Call weather = mCustomServiceApi.getWeather(city, key);
            addCallCustom(weather, CUSTOM_GET);
        });
        mBinding.btnPost2.setOnClickListener(v -> {
            //自定义Retrofit Post 请求
            okhttp3.Call weather = mCustomServiceApi.postWeather(city, key);
            addCallCustom(weather, CUSTOM_POST);
        });
    }

    @OnClick({R.id.btn_get1, R.id.btn_post1, R.id.btn_get2, R.id.btn_post2})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn_get1:
                //Retrofit Get 请求
                Call<ResponseBody> weatherCall = mServiceApi.getWeather(city, key);
                addCall(weatherCall, GET_RETROFIT);
                break;
            case R.id.btn_post1:
                //Retrofit Post 请求
                Call<ResponseBody> weatherCall2 = mServiceApi.postWeather(city, key);
                addCall(weatherCall2, POST_RETROFIT);
                break;
            case R.id.btn_get2:
                //自定义Retrofit Get 请求
                okhttp3.Call weather = mCustomServiceApi.getWeather(city, key);
                addCallCustom(weather, CUSTOM_GET);
                break;
            case R.id.btn_post2:
                //自定义Retrofit Post 请求
                okhttp3.Call weather2 = mCustomServiceApi.postWeather(city, key);
                addCallCustom(weather2, CUSTOM_POST);
                break;
            default:
                break;
        }
    }

    private void addCallCustom(okhttp3.Call call, int what) {
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String string = response.body().string();
                Log.i(TAG, "onResponse custom : " + string);
                Message message = new Message();
                message.obj = string;
                message.what = what;
                mHandler.sendMessage(message);
                response.close();
            }
        });
    }


    private void addCall(Call<ResponseBody> weatherCall, int what) {
        weatherCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    try {
                        String string = body.string();
                        Message message = new Message();
                        message.obj = string;
                        message.what = what;
                        mHandler.sendMessage(message);
                        Log.i(TAG, "onResponse get: " + string);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        body.close();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }


    @Override
    public boolean handleMessage(@NonNull Message msg) {

        switch (msg.what) {
            case GET_RETROFIT: //  Retrofit的GET 请求结果返回
                mBinding.tvContent.setText("GET_RETROFIT:" + (String) msg.obj);
                break;
            case POST_RETROFIT: //  Retrofit的Post 请求结果返回
                mBinding.tvContent.setText("POST_RETROFIT:" + (String) msg.obj);
                break;
            case CUSTOM_GET: //  自定义Retrofit的GET 请求结果返回
                mBinding.tvContent.setText("GET_CUSTOM:" + (String) msg.obj);
                break;
            case CUSTOM_POST://  自定义Retrofit的POST 请求结果返回
                mBinding.tvContent.setText("POST_CUSTOM:" + (String) msg.obj);
                break;
        }

        return false;
    }
}