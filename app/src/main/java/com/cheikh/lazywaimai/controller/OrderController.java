package com.cheikh.lazywaimai.controller;

import android.os.Handler;

import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.base.BaseController;
import com.cheikh.lazywaimai.context.AppCookie;
import com.cheikh.lazywaimai.model.ShoppingCart;
import com.cheikh.lazywaimai.model.bean.Business;
import com.cheikh.lazywaimai.model.bean.CartInfo;
import com.cheikh.lazywaimai.model.bean.Order;
import com.cheikh.lazywaimai.model.bean.OrderStatus;
import com.cheikh.lazywaimai.model.bean.PaymentPlatform;
import com.cheikh.lazywaimai.model.bean.ResponseError;
import com.cheikh.lazywaimai.model.bean.ResultsPage;
import com.cheikh.lazywaimai.model.bean.SettleResult;
import com.cheikh.lazywaimai.model.event.AccountChangedEvent;
import com.cheikh.lazywaimai.network.RequestCallback;
import com.cheikh.lazywaimai.network.RestApiClient;
import com.cheikh.lazywaimai.repository.SettingManager;
import com.cheikh.lazywaimai.ui.Display;
import com.cheikh.lazywaimai.util.EventUtil;
import com.cheikh.lazywaimai.util.StringFetcher;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Subscribe;
import com.wxhl.core.utils.L;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cheikh.lazywaimai.util.Constants.HttpCode.HTTP_UNAUTHORIZED;

/**
 * author: cheikh.wang on 17/1/5
 * email: wanghonghi@126.com
 */
public class OrderController extends BaseController<OrderController.OrderUi, OrderController.OrderUiCallbacks> {

    private static final int PAGE_SIZE = 10;

    private final RestApiClient mRestApiClient;
    private final SettingManager mSettingManager;

    private int mPageIndex;

    @Inject
    public OrderController(RestApiClient restApiClient, SettingManager settingManager) {
        super();
        mRestApiClient = Preconditions.checkNotNull(restApiClient, "restApiClient cannot be null");
        mSettingManager = Preconditions.checkNotNull(settingManager, "settingManager cannot be null");
    }

    @Subscribe
    public void onAccountChanged(AccountChangedEvent event) {
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
    protected void populateUi(OrderUi ui) {
        if (ui instanceof OrderListUi) {
            populateOrderListUi((OrderListUi) ui); //所有订单列表页面
        } else if (ui instanceof OrderDetailUi) {
            populateOrderDetailUi((OrderDetailUi) ui); //订单详情页面
        } else if (ui instanceof OrderSettleUi) {
            populateOrderSettleUi((OrderSettleUi) ui);   //订单结算页面
        } else if (ui instanceof OrderRemarkUi) {
            populateOrderRemarkUi((OrderRemarkUi) ui); //订单结算页面输入的订单备注
        } else if (ui instanceof OrderPaymentUi) {
            populateOrderPaymentUi((OrderPaymentUi) ui);  //支付界面
        }
    }

    private void populateOrderListUi(OrderListUi ui) {
        mPageIndex = 1;
        doFetchOrders(getId(ui), mPageIndex, PAGE_SIZE);
    }

    private void populateOrderDetailUi(OrderDetailUi ui) {
        doFetchOrder(getId(ui), ui.getRequestParameter());
    }

    private void populateOrderSettleUi(OrderSettleUi ui) {
        doOrderSettle(getId(ui), true);
    }

    private void populateOrderRemarkUi(OrderRemarkUi ui) {
        ui.setCommonRemarks(mSettingManager.getCommonRemarks());
    }

    private void populateOrderPaymentUi(OrderPaymentUi ui) {
        ui.setPlatforms(mSettingManager.getPaymentPlatform());
    }

    //获取所有的订单
    private void doFetchOrders(final int callingId, final int page, int size) {
        if (!AppCookie.isLoggin()) {
            OrderUi ui = findUi(callingId);
            if (ui instanceof OrderListUi) {
                ResponseError error = new ResponseError(HTTP_UNAUTHORIZED,
                        StringFetcher.getString(R.string.toast_error_not_login));
                ui.onResponseError(error);
            }
        } else {
            mRestApiClient.orderService()
                    .orders(page, size)
                    .map(new Func1<ResultsPage<Order>, List<Order>>() {
                        @Override
                        public List<Order> call(ResultsPage<Order> results) {
                            return results.results;
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            OrderUi ui = findUi(callingId);
                            if (ui instanceof OrderListUi) {
                                ((OrderListUi) ui).onStartRequest(page);
                            }
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new RequestCallback<List<Order>>() {
                        @Override
                        public void onResponse(List<Order> allOrders) {
                            OrderUi ui = findUi(callingId);
                            if (ui instanceof OrderListUi) {
                                boolean haveNextPage = allOrders != null && allOrders.size() == PAGE_SIZE;

                                L.e("--------------------------------------");
                                Order order = allOrders.get(0);
                                order.setStatus(OrderStatus.STATUS_FINISHED);
                                allOrders.add(order);

                                ((OrderListUi) ui).onFinishRequest(allOrders, page, haveNextPage);
                                Logger.e("我的所有订单为："+allOrders.toString());
                                Logger.e("我的订单数为：："+allOrders.size());
                            }
                        }

                        @Override
                        public void onFailure(ResponseError error) {
                            OrderUi ui = findUi(callingId);
                            if (ui instanceof OrderListUi) {
                                ui.onResponseError(error);
                            }
                        }
                    });
            }
    }

    /**
     * 获取订单详细信息
     * @param callingId
     */
    private void doFetchOrder(final int callingId, String id) {
        mRestApiClient.orderService()
                .detail(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<Order>() {
                    @Override
                    public void onResponse(Order order) {
                        OrderUi ui = findUi(callingId);
                        if (ui instanceof OrderDetailUi) {
                            ((OrderDetailUi) ui).showOrderInfo(order);
                            Logger.e("点击的此账单的详细信息为："+order.toString());
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        OrderUi ui = findUi(callingId);
                        if (ui instanceof OrderDetailUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }

    /**
     * 在线支付
     * @param callingId
     */
    private void doPayment(final int callingId, final Order order, String platformId) {
        mRestApiClient.orderService()
                .payment(order.getId(), platformId, order.getTotalPrice())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<Object>() {
                    @Override
                    public void onResponse(Object object) {
                        OrderUi ui = findUi(callingId);
                        if (ui instanceof OrderPaymentUi) {
                            ((OrderPaymentUi) ui).onFinishPayment(order);
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        OrderUi ui = findUi(callingId);
                        if (ui instanceof OrderPaymentUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }

    /**
     * 设置订单的具体信息
     * @param callingId
     */
    private void doOrderSettle(final int callingId, boolean isOnlinePayment) {
        ShoppingCart shoppingCart = ShoppingCart.getInstance(); //得到购物车和的信息
        mRestApiClient.orderService()
                .settle(shoppingCart.getBusinessId(), isOnlinePayment ? 1 : 0,
                        new Gson().toJson(shoppingCart.getShoppingList()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<SettleResult>() {
                    @Override
                    public void onResponse(SettleResult settleResult) {
                        OrderUi ui = findUi(callingId);
                        if (ui instanceof OrderSettleUi) {
                            ((OrderSettleUi) ui).onSettleFinish(settleResult);
                            Logger.e("设置后的订单的结果为："+settleResult.toString());
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        OrderUi ui = findUi(callingId);
                        if (ui instanceof OrderSettleUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }

    /**
     * 执行订单提交
     * @param callingId
     * @param cartId
     * @param remark
     * @param bookedAt
     */
    private void doOrderCreate(final int callingId, String cartId, long bookedAt, String remark) {
        mRestApiClient.orderService()
                .submit(cartId, bookedAt, remark)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<Order>() {
                    @Override
                    public void onResponse(Order order) {
                        OrderUi ui = findUi(callingId);
                        if (ui instanceof OrderSettleUi) {
                            ((OrderSettleUi) ui).onOrderCreateFinish(order);
                        }
                        Logger.e("提交的订单信息为：" + order.toString());
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        OrderUi ui = findUi(callingId);
                        if (ui instanceof OrderSettleUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }

    @Override
    protected OrderUiCallbacks createUiCallbacks(final OrderUi ui) {
        return new OrderUiCallbacks() {

            @Override
            public void refresh() {
                if (ui instanceof OrderListUi) {
                    mPageIndex = 1;
                    //请求第一页，一页加载十个数据
                    doFetchOrders(getId(ui), mPageIndex, PAGE_SIZE);
                } else if (ui instanceof OrderDetailUi) {
                    doFetchOrder(getId(ui), ui.getRequestParameter());
                }
            }

            @Override
            public void nextPage() {
                if (ui instanceof OrderListUi) {
                    ++mPageIndex;
                    doFetchOrders(getId(ui), mPageIndex, PAGE_SIZE);
                }
            }

            @Override
            public void showLogin() {
                Display display = getDisplay();
                if (display != null) {
                    display.showLogin();
                }
            }

            //去选择地址
            @Override
            public void chooseAddress() {
                Display display = getDisplay();
                if (display != null) {
                    display.showChooseAddress();
                }
            }

            //执行订单结算
            @Override
            public void settle() {

                doOrderSettle(getId(ui), true);
            }


            //确认下单
            @Override
            public void orderCreate(String cartId, long bookedAt, String remark) {
                doOrderCreate(getId(ui), cartId, bookedAt, remark);
            }

            @Override
            public void togglePayMethod(boolean isOnlinePayment) {
                // 切换付款方式需要显示弹出式loading对话框
                doOrderSettle(getId(ui), isOnlinePayment);
            }


            //显示备注
            @Override
            public void showRemark(String remark, int requestCode) {
                Display display = getDisplay();
                if (display != null) {
                    display.showRemark(remark, requestCode);
                }
            }

            //显示订单界面
            @Override
            public void showPayment(Order order) {
                Display display = getDisplay();
                if (display != null) {
                    display.showPayment(order);
                }
            }

            @Override
            public void showOrderDetail(Order order) {
                Display display = getDisplay();
                if (display != null) {
                    display.showOrderDetail(order);
                }
            }

            //显示某一个商家所有的商品（跳到某一个商家的详情界面）
            @Override
            public void showBusiness(Business business) {
                Preconditions.checkNotNull(business, "business cannot be null");
                Display display = getDisplay();
                if (display != null) {
                    display.showBusiness(business);
                }
            }


            @Override
            public void showEvaluate(Order order) {
                Preconditions.checkNotNull(order, "business cannot be null");
                Display display = getDisplay();
                if (display != null) {
                    display.goEvaluate(order);
                }
            }

            @Override
            public void payment(final Order order, final String platformId) {
                // 模拟耗时
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doPayment(getId(ui), order, platformId);
                    }
                }, 2000);
            }

            @Override
            public void confirmReceived() {
                // TODO 确认送达的逻辑
            }

            //再来一单
            @Override
            public void orderAgain(Order order) {
                CartInfo cartInfo = order.getCartInfo();
                if (cartInfo != null) {
                    ShoppingCart.getInstance().again(cartInfo.getShoppingProducts());
                    Display display = getDisplay();
                    if (display != null) {
                        display.showBusiness(order.getBusinessInfo());
                    }
                }
            }
        };
    }

    public interface OrderUiCallbacks {

        void refresh();

        void nextPage();

        void showLogin();

        void settle();

        void chooseAddress();


        //确认下单
        void orderCreate(String cartId, long bookedAt, String remark);

        //选择付款方式
        void togglePayMethod(boolean isOnlinePayment);

        void showRemark(String remark, int requestCode);

        void showPayment(Order order);

        void showOrderDetail(Order order);

        void showBusiness(Business business);

        void showEvaluate(Order order);

        void payment(Order order, String platformId);

        void confirmReceived();

        void orderAgain(Order order);
    }

    public interface OrderUi extends BaseController.Ui<OrderUiCallbacks> {
        String getRequestParameter();
    }

    public interface OrderListUi extends OrderUi, BaseController.ListUi<Order, OrderUiCallbacks> {}

    public interface OrderDetailUi extends OrderUi {
        void showOrderInfo(Order order);
    }

    public interface OrderSettleUi extends OrderUi {
        //结算完成一笔订单
        void onSettleFinish(SettleResult result);

        //创建一笔订单
        void onOrderCreateFinish(Order order);
    }

    public interface OrderRemarkUi extends OrderUi {
        void setCommonRemarks(List<String> remarks);
    }

    public interface OrderPaymentUi extends OrderUi {
        void setPlatforms(List<PaymentPlatform> platforms);
        void onFinishPayment(Order order);
    }
}
