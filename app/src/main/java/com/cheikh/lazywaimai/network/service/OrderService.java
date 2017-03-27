package com.cheikh.lazywaimai.network.service;

import com.cheikh.lazywaimai.model.bean.Order;
import com.cheikh.lazywaimai.model.bean.ResultsPage;
import com.cheikh.lazywaimai.model.bean.SettleResult;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

//订单相关的
public interface OrderService {

    //设置订单的具体信息
    @FormUrlEncoded
    @POST("orders/check")
    Observable<SettleResult> settle(@Field("business_id") String businessId,
                                    @Field("pay_method") int payMethod,
                                    @Field("product_list") String productListJson);


    @FormUrlEncoded
    @POST("orders") //确认下单
    Observable<Order> submit(@Field("cart_id") String cartId,
                             @Field("booked_at") long bookedAt,
                             @Field("remark") String remark);

    @GET("orders?expand=business_info,cart_info")
    Observable<ResultsPage<Order>> orders(@Query("page") int page, @Query("size") int size);

    //订单详细信息
    @GET("orders/{id}?expand=cart_info")
    Observable<Order> detail(@Path("id") String orderId);

    @FormUrlEncoded
    @POST("payments")
    Observable<Object> payment(@Field("order_id") String orderId,
                               @Field("platform_id") String platformId,
                               @Field("amount") double amount);
}