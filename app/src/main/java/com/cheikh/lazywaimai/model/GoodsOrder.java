package com.cheikh.lazywaimai.model;

import com.cheikh.lazywaimai.model.bean.Business;
import com.cheikh.lazywaimai.model.bean.OrderStatus;
import com.cheikh.lazywaimai.model.bean.PayMethod;
import com.google.gson.annotations.SerializedName;

import cn.bmob.v3.BmobObject;

/**
 * Created by lenovo on 2017/4/9.
 */

public class GoodsOrder extends BmobObject {

    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("business_id")
    private String businessId;

    @SerializedName("business_info")
    private  Business businessInfo;


    @SerializedName("status")
    private OrderStatus status;

    @SerializedName("origin_price")
    private double originPrice;

    @SerializedName("discount_price")
    private double discountPrice;

    @SerializedName("total_price")
    private double totalPrice;

    @SerializedName("consignee")
    private String consignee;

    @SerializedName("phone")
    private  String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("pay_method")
    private PayMethod payMethod;

    @SerializedName("remark")
    private String remark; //备注信息

    @SerializedName("order_num")
    private  String orderNum;

    @SerializedName("booked_at")
    private  long bookedTime;

    @SerializedName("created_at")
    private  long createdAt;
}
