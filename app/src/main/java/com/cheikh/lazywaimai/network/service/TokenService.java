package com.cheikh.lazywaimai.network.service;

import com.cheikh.lazywaimai.model.bean.Token;

import java.util.Map;

import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import rx.Observable;

public interface TokenService {

    //获取请求的Token值
    @POST("oauth/access_token")
    Observable<Token> accessToken(@QueryMap Map<String, String> params);

    //刷新Token值
    @POST("oauth/access_token")
    Observable<Token> refreshToken(@QueryMap Map<String, String> params);
}