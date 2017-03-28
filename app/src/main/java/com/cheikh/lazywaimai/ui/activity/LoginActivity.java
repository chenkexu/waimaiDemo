package com.cheikh.lazywaimai.ui.activity;


import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.base.BaseActivity;
import com.cheikh.lazywaimai.base.BaseController;
import com.cheikh.lazywaimai.context.AppContext;
import com.cheikh.lazywaimai.controller.UserController;
import com.cheikh.lazywaimai.manager.ShareApiManager;
import com.cheikh.lazywaimai.model.bean.LoginInfo;
import com.cheikh.lazywaimai.model.bean.ResponseError;
import com.cheikh.lazywaimai.ui.Display;
import com.cheikh.lazywaimai.util.ContentView;
import com.cheikh.lazywaimai.util.L;
import com.cheikh.lazywaimai.util.LoadingDialog;
import com.cheikh.lazywaimai.util.StringUtil;
import com.cheikh.lazywaimai.util.SystemUtil;
import com.cheikh.lazywaimai.util.ToastUtil;
import com.orhanobut.logger.Logger;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;


/**
 * author: cheikh.wang on 17/1/5
 * email: wanghonghi@126.com
 * //用户登录的界面
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity<UserController.UserUiCallbacks>
        implements UserController.UserLoginUi {


    @Bind(R.id.et_user_account)
    EditText mEtAccount;

    @Bind(R.id.et_user_password)
    EditText mEtPassword;

    @Bind(R.id.iv_delete_account)
    ImageView mIvDeleteAccount;

    @Bind(R.id.iv_delete_password)
    ImageView mIvDeletePassword;

    @Bind(R.id.btn_login)
    Button mBtnLogin;

    @Bind(R.id.tv_forget_password)
    TextView mTvForgetPassword;

    @Bind(R.id.tv_go_to_register)
    TextView mTvGoToRegister;


    @Bind(R.id.iv_qq_login)
    ImageView qqLogin;

    @Bind(R.id.iv_weixin_login)
    ImageView weiChatLogin;

    private LoadingDialog loadingDialog;
    private LoginInfo loginInfo;


    @Override
    protected BaseController getController() {
        return AppContext.getContext().getMainController().getUserController();
    }

    @Override
    public void onResponseError(ResponseError error) {
        cancelLoading();
        mBtnLogin.setEnabled(true);
        ToastUtil.showToast(error.getMessage());
    }

    @Override
    public void userLoginFinish() {
        cancelLoading();
        Display display = getDisplay();
        if (display != null) {
            display.finishActivity();
        }
    }

    @OnTextChanged(R.id.et_user_account)
    public void onAccountTextChange(CharSequence s) {
        int visible = StringUtil.isEmpty(s.toString()) ? View.GONE : View.VISIBLE;
        mIvDeleteAccount.setVisibility(visible);
    }

    @OnTextChanged(R.id.et_user_password)
    public void onPasswordTextChange(CharSequence s) {
        int visible = StringUtil.isEmpty(s.toString()) ? View.GONE : View.VISIBLE;
        mIvDeletePassword.setVisibility(visible);
    }

    @OnClick({R.id.iv_delete_account, R.id.iv_weixin_login,R.id.iv_qq_login,R.id.iv_delete_password, R.id.btn_login, R.id.tv_forget_password, R.id.tv_go_to_register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_delete_account:
                mEtAccount.setText("");
                break;
            case R.id.iv_delete_password:
                mEtPassword.setText("");
                break;
            case R.id.btn_login: //登录
                login();
                break;
            case R.id.tv_forget_password:
                ToastUtil.showToast("该功能还未开发");
                break;
            case R.id.tv_go_to_register: //注册
                getCallbacks().showRegister();
                break;
            case R.id.iv_qq_login:
                qqLogin();
                break;
            case R.id.iv_weixin_login:
                weChatLogin();
                break;
        }
    }

    /**
     * 执行登录操作
     */
    private void login() {
        // 隐藏软键盘
        SystemUtil.hideKeyBoard(this);

        // 验证用户名是否为空
        final String account = mEtAccount.getText().toString().trim();
        if (TextUtils.isEmpty(account)) {
            ToastUtil.showToast(R.string.toast_error_empty_account);
            return;
        }
        // 验证密码是否为空
        final String password = mEtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            ToastUtil.showToast(R.string.toast_error_empty_password);
            return;
        }
        // 禁用登录按钮,避免重复点击
        mBtnLogin.setEnabled(false);
        // 显示提示对话框
        showLoading(R.string.label_being_something);
        // 发起登录的网络请求
        getCallbacks().login(account, password);
    }



    //微信登录
    private void weChatLogin() {
        if (ShareApiManager.isInstallWechat(this)) {
            dismissLoadingDialog();
            loadingDialog = new LoadingDialog(LoginActivity.this, R.string.waiting);
            loadingDialog.showDialog();
            ShareApiManager.wechatLogin(this, new ShareApiManager.ActionListener<LoginInfo>() {
                @Override
                public void onComplete(LoginInfo loginInfo) {
                    LoginActivity.this.loginInfo = loginInfo;//设置loginfo,logInfo不为空了就。
                    LoginActivity.this.loginInfo = loginInfo;
                    Log.e("getToken", loginInfo.getToken());
                    Log.e("getNickName", loginInfo.getNickName());
                    Log.e("getUserId", loginInfo.getUserId());
                    Log.e("getGender", loginInfo.getGender() + "");
                    Log.e("getUserType", loginInfo.getUserType() + "");
                    //调用登录的接口
                }

                @Override
                public void onError() {
                    dismissLoadingDialog();
                    ToastUtil.showToast("第三方登录失败请重试");
                }

                @Override
                public void onCancel() {
                    dismissLoadingDialog();
                    ToastUtil.showToast("登录取消");

                }
            });
        } else {
            ToastUtil.showToast(R.string.please_install_wechat);
        }
    }

    //QQ登录
    private void qqLogin() {
        dismissLoadingDialog();
        loadingDialog = new LoadingDialog(LoginActivity.this, R.string.waiting);
        loadingDialog.showDialog();
        ShareApiManager.qqLogin(this, new ShareApiManager.ActionListener<LoginInfo>() {
            @Override
            public void onComplete(LoginInfo loginInfo) {
                L.e("*****************onComplete******");
                Log.e("getToken", loginInfo.getToken());
                Log.e("getNickName", loginInfo.getNickName());
                Log.e("getUserId", loginInfo.getUserId());
                Log.e("getGender", loginInfo.getGender() + "");
                Log.e("getUserType", loginInfo.getUserType() + "");
                dismissLoadingDialog();
                String token = loginInfo.getToken();
//                AppCookie.saveAccessToken(token);//保存AccessToken
//                AppCookie.saveRefreshToken(token);//保存RefreshToken
//
//
//                User user = new User();
//                user.setAvatarUrl(loginInfo.getUserAvaterUrl());
//                user.setNickname(loginInfo.getNickName());
//                user.setToken(loginInfo.getToken());
//                AppCookie.saveUserInfo(user);
//                AppCookie.saveLastPhone(user.getMobile());
                Logger.e("当前的用户信息为："+ loginInfo.toString());
                getCallbacks().qqlogin(loginInfo);
            }

            @Override
            public void onError() {
                dismissLoadingDialog();
                ToastUtil.showToast("第三方登录失败请重试");
            }

            @Override
            public void onCancel() {
                dismissLoadingDialog();
                ToastUtil.showToast("登录取消");
            }
        });
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        loadingDialog = null;
    }

}
