package com.cheikh.lazywaimai.model.bean;

import com.google.gson.annotations.SerializedName;

/**
 * author：cheikh on 16/5/12 00:11
 * email：wanghonghi@126.com
 */
public class Token {

    @SerializedName("access_token")
    String accessToken; //访问的令牌

    @SerializedName("user_id")
    String userId; //令牌类型

    @SerializedName("expires_in")
    int expiresin; //过期时间

    @SerializedName("token_type")
    String tokenType; //表示更新令牌，用来获取下一次的访问令牌，可选项。

    @SerializedName("scope")
    String scope; //权限范围

    @SerializedName("refresh_token")
    String refreshToken; //；access token过期后刷新access token的一个标记.





    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserId() {   //根据返回的Token值得到userId
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getExpiresin() {
        return expiresin;
    }

    public void setExpiresin(int expiresin) {
        this.expiresin = expiresin;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
