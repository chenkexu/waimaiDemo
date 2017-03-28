package com.cheikh.lazywaimai.controller;

import com.cheikh.lazywaimai.base.BaseController;
import com.cheikh.lazywaimai.context.AppConfig;
import com.cheikh.lazywaimai.context.AppCookie;
import com.cheikh.lazywaimai.model.bean.Business;
import com.cheikh.lazywaimai.model.bean.Favorite;
import com.cheikh.lazywaimai.model.bean.LoginInfo;
import com.cheikh.lazywaimai.model.bean.ResponseError;
import com.cheikh.lazywaimai.model.bean.ResultsPage;
import com.cheikh.lazywaimai.model.bean.Token;
import com.cheikh.lazywaimai.model.bean.User;
import com.cheikh.lazywaimai.model.event.AccountChangedEvent;
import com.cheikh.lazywaimai.network.RequestCallback;
import com.cheikh.lazywaimai.network.RestApiClient;
import com.cheikh.lazywaimai.ui.Display;
import com.cheikh.lazywaimai.util.EventUtil;
import com.google.common.base.Preconditions;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cheikh.lazywaimai.util.Constants.Key.PARAM_CLIENT_ID;
import static com.cheikh.lazywaimai.util.Constants.Key.PARAM_CLIENT_SECRET;
import static com.cheikh.lazywaimai.util.Constants.Key.PARAM_GRANT_TYPE;
import static com.cheikh.lazywaimai.util.Constants.Key.PARAM_PASSWORD;
import static com.cheikh.lazywaimai.util.Constants.Key.PARAM_USER_NAME;

@Singleton
public class UserController extends BaseController<UserController.UserUi, UserController.UserUiCallbacks> {

    private static final int PAGE_SIZE = 10;

    private final RestApiClient mRestApiClient;

    private int mPageIndex;

    @Inject
    public UserController(RestApiClient restApiClient) {
        super();
        mRestApiClient = Preconditions.checkNotNull(restApiClient, "restApiClient cannot be null");
    }


    //账户改变之后，存储用户变更的信息
    @Subscribe
    public void onAccountChanged(AccountChangedEvent event) {
        User user = event.getUser();
        if (user != null) {
            AppCookie.saveUserInfo(user);
            AppCookie.saveLastPhone(user.getMobile());
            Logger.e("当前的用户信息为："+ user.toString());
        } else { //表示的是退出登录
            AppCookie.saveUserInfo(null);
            AppCookie.saveAccessToken(null);
            AppCookie.saveRefreshToken(null);
            mRestApiClient.setToken(null);
        }
        populateUis();
    }

    @Override
    protected void onInited() {
        super.onInited();
        EventUtil.register(this);
    }

    @Override
    protected void onSuspended() {
        EventUtil.unregister(this);
        super.onSuspended();
    }

    @Override
    protected synchronized void populateUi(UserUi ui) {
        if (ui instanceof UserCenterUi) {  //我的页面
            populateUserCenterUi((UserCenterUi) ui);  //就展示个人信息
        } else if (ui instanceof UserProfileUi) {
            populateUserProfileUi((UserProfileUi) ui);  //个人中心页面（得到个人信息）

        } if (ui instanceof UserFavoriteListUi) { //我的收藏页面
            populateUserFavoriteListUi((UserFavoriteListUi) ui);
        }
    }

    private void populateUserCenterUi(UserCenterUi ui) {
        ui.showUserInfo(AppCookie.getUserInfo());
    }

    private void populateUserProfileUi(UserProfileUi ui) {
        ui.showUserInfo(AppCookie.getUserInfo());
    }

    private void populateUserFavoriteListUi(UserFavoriteListUi ui) {
        mPageIndex = 1;
        doFetchFavorites(getId(ui), mPageIndex, PAGE_SIZE);
    }

    /**
     * 注册的第1个步骤:发送短信验证码
     * @param callingId
     * @param mobile
     */
    private void doSendCode(final int callingId, String mobile) {
        mRestApiClient.accountService()
                .sendCode(mobile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof RegisterFirstStepUi) {
                            ((RegisterFirstStepUi) ui).sendCodeFinish();
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof RegisterFirstStepUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }

    /**
     * 注册的第2个步骤:检查短信验证码
     * @param callingId
     * @param mobile
     * @param code
     */
    private void doCheckCode(final int callingId, String mobile, String code) {
        mRestApiClient.accountService()
                .verifyCode(mobile, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof RegisterFirstStepUi) {
                            ((RegisterSecondStepUi) ui).verifyMobileFinish();
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof RegisterSecondStepUi) {
                            ui.onResponseError(error);
                        }
                    }
                });

    }


    /**
     * 注册的第3个步骤:创建账户
     * @param callingId
     * @param mobile
     * @param password
     */
    private void doCreateUser(final int callingId, String mobile, String password) {
        mRestApiClient.accountService()
                .register(mobile, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof RegisterThirdStepUi) {
                            ((RegisterThirdStepUi) ui).userCreateFinish();
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof RegisterThirdStepUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }

    /**
     * 用户登录,获取 Access Token 以及用户资料
     * @param callingId
     * @param username
     * @param password
     */
    //用户登录的校验
    private void doLogin(final int callingId, String username, String password) {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_CLIENT_ID, AppConfig.APP_KEY);
        params.put(PARAM_CLIENT_SECRET, AppConfig.APP_SECRET);
        params.put(PARAM_GRANT_TYPE, "password");
        params.put(PARAM_USER_NAME, username); //用户名
        params.put(PARAM_PASSWORD, password);  //密码

        mRestApiClient.tokenService()
                .accessToken(params)
                .flatMap(new Func1<Token, Observable<User>>() {
                    @Override
                    public Observable<User> call(final Token token) {

                        AppCookie.saveAccessToken(token.getAccessToken());//保存AccessToken
                        AppCookie.saveRefreshToken(token.getRefreshToken());//保存RefreshToken

                        return mRestApiClient.setToken(token.getAccessToken())
                                .accountService()
                                .profile(token.getUserId()); //根据id获取用户资料
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<User>() {
                    @Override
                    public void onResponse(User user) {
                        UserUi ui = findUi(callingId);//
                        if (ui instanceof UserLoginUi) {
                            ((UserLoginUi) ui).userLoginFinish();
                            // 发送用户账户改变的事件
                            EventUtil.sendEvent(new AccountChangedEvent(user));
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof UserLoginUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }

    /**
     * 设置用户名
     * @param callingId
     * @param username
     */
    private void doSetUsername(final int callingId, String username) {
        mRestApiClient.accountService()
                .setUsername(AppCookie.getUserInfo().getId(), username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<User>() {
                    @Override
                    public void onResponse(User user) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof UserNameUi) {
                            ((UserNameUi) ui).onSetUsernameFinish();
                            // 发送用户账户改变的事件
                            EventUtil.sendEvent(new AccountChangedEvent(user));
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof UserNameUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }

    /**
     * 设置昵称
     * @param callingId
     * @param nickname
     */
    private void doSetNickname(final int callingId, String nickname) {
        mRestApiClient.accountService()
                .setNickname(AppCookie.getUserInfo().getId(), nickname)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<User>() {
                    @Override
                    public void onResponse(User user) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof UserNicknameUi) {
                            ((UserNicknameUi) ui).onSetNicknameFinish();
                            // 发送用户账户改变的事件
                            EventUtil.sendEvent(new AccountChangedEvent(user));
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof UserNicknameUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }

    /**
     * 用户退出登录
     * @param callingId
     */
    private void doLogout(final int callingId) {
        UserUi ui = findUi(callingId);
        if (ui instanceof SettingUi) {
            // 发送用户账户改变的事件
            EventUtil.sendEvent(new AccountChangedEvent(null));
            ((SettingUi) ui).logoutFinish();
        }
    }

    /**
     * 用户上传头像
     * @param callingId
     */
    private void doUploadAvatar(final int callingId, File file) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("avatar", file.getName(), requestBody);

        mRestApiClient.commonService()
            .singleFileUpload(part)
            .flatMap(new Func1<String, Observable<User>>() {
                @Override
                public Observable<User> call(final String url) {
                    return mRestApiClient
                            .accountService()
                            .updateAvatar(AppCookie.getUserInfo().getId(), url);
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new RequestCallback<User>() {
                @Override
                public void onResponse(User user) {
                    UserUi ui = findUi(callingId);
                    if (ui instanceof UserProfileUi) {
                        ((UserProfileUi) ui).uploadAvatarFinish();
                        // 发送用户账号改变的事件
                        EventUtil.sendEvent(new AccountChangedEvent(user));
                    }
                }

                @Override
                public void onFailure(ResponseError error) {
                    UserUi ui = findUi(callingId);
                    if (ui instanceof UserProfileUi) {
                        ui.onResponseError(error);
                    }
                }
            });
    }

    /**
     * 获取当前用户收藏的所有商家
     * @param callingId
     * @param page
     * @param size
     */
    private void doFetchFavorites(final int callingId, final int page, int size) {
        mRestApiClient.accountService()
                .favorites(page, size)
                .map(new Func1<ResultsPage<Favorite>, List<Favorite>>() {
                    @Override
                    public List<Favorite> call(ResultsPage<Favorite> results) {
                        return results.results;
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof UserFavoriteListUi) {
                            ((UserFavoriteListUi) ui).onStartRequest(page);  //开始请求第一页数据
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<List<Favorite>>() {
                    @Override
                    public void onResponse(List<Favorite> favorites) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof UserFavoriteListUi) {
                            boolean haveNextPage = favorites != null && favorites.size() == PAGE_SIZE;
                            ((UserFavoriteListUi) ui).onFinishRequest(favorites, page, haveNextPage);

                            Logger.e("所有的喜欢的商家为：" +  favorites.toString());
                            Logger.e("喜欢的商家数为：" +  favorites.size());
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        UserUi ui = findUi(callingId);
                        if (ui instanceof UserFavoriteListUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }


    //关于user的一些接口
    public interface UserUiCallbacks {

        //刷新
        void refresh();

        //下一页
        void nextPage();

        //跳到登录界面
        void showLogin();

        //跳到个人信息界面
        void showUserProfile();

        //去注册界面
        void showRegister();

        //去我的地址的界面
        void showAddressList();

        //显示我的收藏界面
        void showFavoriteList();

        //跳到设置界面
        void showSetting();

        //显示我的反馈界面
        void showFeedback();

        //根据商家（参数）显示某一个商家所有的商品
        void showBusiness(Business business);

        void checkUpdate();

        //发送验证码，参数为手机号
        void sendCode(String mobile);

        //验证码校验（参数为手机号和验证码）
        void checkCode(String mobile, String code);

        //根据电话和密码创建一个账户
        void createUser(String mobile, String password);

        //上传图片
        void uploadAvatar(File file);

        boolean validateUsername(String username);

        //设置用户名
        void setUsername(String username);

        //设置昵称
        void setNickname(String nickname);

        //登录
        void login(String account, String password);

        //注销
        void logout();

        void qqlogin(LoginInfo loginInfo);
    }

    /**
     * 创建留给UI界面使用的回调
     * @param ui
     * @return
     */
    @Override
    protected UserUiCallbacks createUiCallbacks(final UserUi ui) {
        return new UserUiCallbacks() {  //实现当前用户的所有接口

            @Override
            public void refresh() {
                if (ui instanceof UserFavoriteListUi) { //如果继承的是收藏的页面，那么久执行查询收藏的操作
                    mPageIndex = 1;
                    doFetchFavorites(getId(ui), mPageIndex, PAGE_SIZE);
                }
            }

            @Override
            public void nextPage() {
                if (ui instanceof UserFavoriteListUi) {
                    ++mPageIndex;
                    doFetchFavorites(getId(ui), mPageIndex, PAGE_SIZE);
                }
            }

            @Override
            public void showRegister() {
                Display display = getDisplay();
                if (display != null) {
                    display.showRegister();
                }
            }

            @Override
            public void showLogin() {
                Display display = getDisplay();
                if (display != null) {
                    display.showLogin();
                }
            }

            @Override
            public void showUserProfile() {
                Display display = getDisplay();
                if (display != null) {
                    display.showUserProfile();
                }
            }

            @Override
            public void showAddressList() {
                Display display = getDisplay();
                if (display != null) {
                    if (AppCookie.isLoggin()) {
                        display.showAddresses();
                    } else {
                        display.showLogin();
                    }
                }
            }

            @Override
            public void showFavoriteList() {
                Display display = getDisplay();
                if (display != null) {
                    if (AppCookie.isLoggin()) {
                        display.showFavorites();
                    } else {
                        display.showLogin();
                    }
                }
            }

            @Override
            public void showSetting() {
                Display display = getDisplay();
                if (display != null) {
                    display.showSetting();
                }
            }

            @Override
            public void showFeedback() {
                Display display = getDisplay();
                if (display != null) {
                    display.showFeedback();
                }
            }

            @Override
            public void showBusiness(Business business) {
                Preconditions.checkNotNull(business, "business cannot be null");
                Display display = getDisplay();
                if (display != null) {
                    display.showBusiness(business);
                }
            }

            @Override
            public void checkUpdate() {

            }

            @Override
            public void sendCode(String mobile) {
                doSendCode(getId(ui), mobile);
            }

            @Override
            public void checkCode(String mobile, String code) {
                doCheckCode(getId(ui), mobile, code);
            }

            @Override
            public void createUser(String mobile, String password) {
                doCreateUser(getId(ui), mobile, password);
            }

            @Override
            public void uploadAvatar(File file) {

                doUploadAvatar(getId(ui), file);
            }

            @Override
            public boolean validateUsername(String username) {
                return false;
            }

            @Override
            public void setUsername(String username) {
                doSetUsername(getId(ui), username);
            }

            @Override
            public void setNickname(String nickname) {
                doSetNickname(getId(ui), nickname);
            }

            @Override
            public void login(String account, String password) {
                doLogin(getId(ui), account, password);
            }

            @Override
            public void logout() {
                doLogout(getId(ui));
            }


            @Override
            public void qqlogin(LoginInfo loginInfo) {
                mRestApiClient.setToken(loginInfo.getToken())
                        .accountService();
                AppCookie.saveAccessToken(loginInfo.getToken());//保存AccessToken
                AppCookie.saveRefreshToken(loginInfo.getToken());//保存RefreshToken
                User user = new User();
                user.setAvatarUrl(loginInfo.getUserAvaterUrl());
                user.setNickname(loginInfo.getNickName());
                user.setToken(loginInfo.getToken());
                if (ui instanceof UserLoginUi) {
                    ((UserLoginUi) ui).userLoginFinish();
                   AppCookie.saveUserInfo(user);
                }
            }
        };
    }


    public interface UserUi extends BaseController.Ui<UserUiCallbacks> {

    }

    public interface UserRegisterUi extends UserUi {}

    //注册的第一步实现的接口
    public interface RegisterFirstStepUi extends UserRegisterUi {
        void sendCodeFinish(); //发送验证码接收之后
    }

    public interface RegisterSecondStepUi extends RegisterFirstStepUi {
        void verifyMobileFinish();
    }

    public interface RegisterThirdStepUi extends UserRegisterUi {
        void userCreateFinish();
    }

    //登录界面要实现的接口
    public interface UserLoginUi extends UserUi {
        void userLoginFinish();
    }

    //"我的"界面实现的接口
    public interface UserCenterUi extends UserUi {
        void showUserInfo(User user); //显示个人信息
    }

    //个人中心需要实现的接口
    public interface UserProfileUi extends UserUi {
        void showUserInfo(User user);    //展示个人信息方法
        void uploadAvatarFinish(); //上传头像方法
    }

    public interface UserNameUi extends UserUi {  //设置昵称需要实现的接口
        void onSetUsernameFinish(); //设置用户名方法
    }

    public interface UserNicknameUi extends UserUi {
        void onSetNicknameFinish();
    }

    public interface UserFavoriteListUi extends UserUi, BaseController.ListUi<Favorite, UserUiCallbacks> {

    }

    public interface SettingUi extends UserUi {  //设置界面需要实现的接口
        void logoutFinish(); //退出登录
    }
}
