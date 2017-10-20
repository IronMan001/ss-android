package com.vm.shadowsocks.ui;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/10/17 0017.
 */

public class NetUtils {
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    static{
        mOkHttpClient.setConnectTimeout(5, TimeUnit.SECONDS);
    }
    public static OkHttpClient getInstance(){
        return mOkHttpClient;
    }
    /**
     * 该不会开启异步线程。
     * @param request
     * @return
     * @throws IOException
     */
    public static Response execute(Request request) throws IOException{
//        OkHttpClient okHttpClient_temp=new OkHttpClient();
//        okHttpClient_temp.setConnectTimeout(5,TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(5,TimeUnit.SECONDS);
        mOkHttpClient.setWriteTimeout(5,TimeUnit.SECONDS);
        return mOkHttpClient.newCall(request).execute();
    }
    /**
     * 开启异步线程访问网络
     * @param request
     * @param responseCallback
     */
    public static Response enqueue(Request request, Callback responseCallback){
        mOkHttpClient.newCall(request).enqueue(responseCallback);
        return null;
    }
    /**
     * 开启异步线程访问网络, 且不在意返回结果（实现空callback）
     * @param request
     */
    public static void enqueue(Request request){
        mOkHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Response arg0) throws IOException {

            }

            @Override
            public void onFailure(Request arg0, IOException arg1) {

            }
        });
    }

    /**
     * 发起请求
     * @param url
     * @return
     * @throws IOException
     */
    public static String getStringFromServer(String url) throws IOException{
        Request request = new Request.Builder().url(url).build();
        Response response = execute(request);
        if (response.isSuccessful()) {
            String responseUrl = response.body().string();
            return responseUrl;
        }
        else {
            throw new IOException("Unexpected code " + response);
        }
    }/**
     * 发起异步请求
     * @param url
     * @return
     * @throws IOException

    public static String getStringFromServerAsys(String url,Callback responseCallback) throws IOException{
        Request request = new Request.Builder().url(url).build();
        Response response = enqueue(request,responseCallback);
        if (response.isSuccessful()) {
            String responseUrl = response.body().string();
            return responseUrl;
        } else if (response.){

        }
        else {
            throw new IOException("Unexpected code " + response);
        }
    }
     */


    private static final String CHARSET_NAME = "UTF-8";
    /**
     * 这里使用了HttpClinet的API。只是为了方便
     * @param params
     * @return
     */
    public static String formatParams(List<BasicNameValuePair> params){
        return URLEncodedUtils.format(params, CHARSET_NAME);
    }
    /**
     * 为HttpGet 的 url 方便的添加多个name value 参数。
     * @param url
     * @param params
     * @return
     */
    public static String attachHttpGetParams(String url, List<BasicNameValuePair> params){
        return url + "?" + formatParams(params);
    }
    /**
     * 为HttpGet 的 url 方便的添加1个name value 参数。
     * @param url
     * @param name
     * @param value
     * @return
     */
    public static String attachHttpGetParam(String url, String name, String value){
        return url + "?" + name + "=" + value;
    }
}
