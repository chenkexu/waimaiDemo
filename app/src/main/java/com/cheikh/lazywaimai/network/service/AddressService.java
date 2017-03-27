package com.cheikh.lazywaimai.network.service;

import com.cheikh.lazywaimai.model.bean.Address;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;


//地址相关的
public interface AddressService {

    //新建一个地址
    @POST("addresses")
    Observable<Address> create(@Body Address address);

    //删除地址
    @DELETE("addresses/{id}")
    Observable<Object> delete(@Path("id") String id);

    //根据id更换地址
    @PATCH("addresses/{id}")
    Observable<Address> change(@Path("id") String id, @Body Address address);

    //获取所有的地址
    @GET("addresses")
    Observable<List<Address>> addresses();
}
