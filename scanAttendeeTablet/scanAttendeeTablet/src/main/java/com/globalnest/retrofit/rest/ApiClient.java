package com.globalnest.retrofit.rest;

import com.globalnest.utils.AppUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by saila_000 on 17-04-2018.
 */

public class ApiClient {
    private static Retrofit retrofit = null;
    private static  OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100,TimeUnit.SECONDS).build();

    public static Retrofit getClient(String url) {//String url
        if (retrofit==null) {
            if(AppUtils.isLogEnabled){}
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    //.baseUrl("https://na42.salesforce.com/services/apexrest/")
                    //"https://cs91.salesforce.com/services/apexrest/"
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    public static Retrofit getNetworkConnectivity(String url) {//String url
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    /*public static Client createClient() {
        if (isOkHttpEnabled()) {
            if (isOkHttpTimeoutEnabled()) {
                OkHttpClient client = new OkHttpClient();
                client.setConnectTimeout(15, TimeUnit.SECONDS);
                client.setReadTimeout(15, TimeUnit.SECONDS);
                client.setWriteTimeout(15, TimeUnit.SECONDS);
                return new OkClient(client);
            }
            return new OkClient();
        } else {
            return new UrlConnectionClient();
        }
    }*/
}
