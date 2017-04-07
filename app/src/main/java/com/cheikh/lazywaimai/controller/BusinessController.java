package com.cheikh.lazywaimai.controller;

import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.base.BaseController;
import com.cheikh.lazywaimai.context.AppCookie;
import com.cheikh.lazywaimai.model.Seller;
import com.cheikh.lazywaimai.model.ShoppingCart;
import com.cheikh.lazywaimai.model.bean.Business;
import com.cheikh.lazywaimai.model.bean.Favorite;
import com.cheikh.lazywaimai.model.bean.ProductCategory;
import com.cheikh.lazywaimai.model.bean.ResponseError;
import com.cheikh.lazywaimai.model.bean.ResultsPage;
import com.cheikh.lazywaimai.model.event.ShoppingCartChangeEvent;
import com.cheikh.lazywaimai.network.RequestCallback;
import com.cheikh.lazywaimai.network.RestApiClient;
import com.cheikh.lazywaimai.ui.Display;
import com.cheikh.lazywaimai.util.EventUtil;
import com.cheikh.lazywaimai.util.StringFetcher;
import com.google.common.base.Preconditions;
import com.orhanobut.logger.Logger;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListListener;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cheikh.lazywaimai.util.Constants.HttpCode.HTTP_UNAUTHORIZED;


//商品的Controller
public class BusinessController extends BaseController<BusinessController.BusinessUi,
        BusinessController.BusinessUiCallbacks> {

    private static final int PAGE_SIZE = 10;//一页显示10个

    private final RestApiClient mRestApiClient;

    private int mPageIndex;

    @Inject
    public BusinessController(RestApiClient restApiClient) {
        super();
        mRestApiClient = Preconditions.checkNotNull(restApiClient, "restApiClient cannot be null");
    }

    @Subscribe
    public void onShoppingCartChanged(ShoppingCartChangeEvent event) {
        for (BusinessUi ui : getUis()) {
            if (ui instanceof ProductListUi) {
                ((ProductListUi) ui).onShoppingCartChange();
                break;
            }
        }
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
    protected void populateUi(BusinessUi ui) {
        if (ui instanceof BusinessListUi) {
            populateBusinessListUi((BusinessListUi) ui);
        } else if (ui instanceof BusinessTabUi) {
            populateBusinessTabUi((BusinessTabUi) ui);
        } else if (ui instanceof ProductListUi) {
            populateProductListUi((ProductListUi) ui);
        }
    }



    //分页获取商家列表数据(参数：实现了BusinessListUi接口的Fragment)
    private void populateBusinessListUi(BusinessListUi ui) {
        mPageIndex = 1;
        doFetchBusinesses(getId(ui), mPageIndex, PAGE_SIZE);
    }

    //切换商品，评价，商家
    private void populateBusinessTabUi(BusinessTabUi ui) {
        ui.setTabs(BusinessTab.PRODUCT, BusinessTab.COMMENT, BusinessTab.DETAIL);
    }

    //获取指定商家下的所有商品数据
    private void populateProductListUi(ProductListUi ui) {
        doFetchProducts(getId(ui), ui.getRequestParameter());
    }




    /**
     * 分页获取商家列表数据
     * @param callingId 根据用户的id显示
     * @param page
     */
    private void doFetchBusinesses(final int callingId, final int page, int size) {
        mRestApiClient.businessService()
                .businesses(page, size)
                .map(new Func1<ResultsPage<Business>, List<Business>>() {
                    @Override
                    public List<Business> call(ResultsPage<Business> results) {
                        return results.results;
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        BusinessUi ui = findUi(callingId);
                        if (ui instanceof BusinessListUi) {
                            ((BusinessListUi) ui).onStartRequest(page);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<List<Business>>() {
                    @Override
                    public void onResponse(List<Business> businesses) {

                        BusinessUi ui = findUi(callingId);
                        if (ui instanceof BusinessListUi) {
                            boolean haveNextPage = businesses != null && businesses.size() == PAGE_SIZE;
                            ((BusinessListUi) ui).onFinishRequest(businesses, page, haveNextPage);
                            Logger.e("所有的商家为：" +  businesses.toString());
                            Logger.e("商家数：" +  businesses.size());
//                            addSqlite(businesses);
                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        BusinessUi ui = findUi(callingId);
                        if (ui instanceof BusinessListUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }


    /**
     * 商家信息添加到数据库
     */
    private void addSqlite(List<Business> businesses){

        List<BmobObject>  sellers = new ArrayList<>();
        for (int i = 0; i < businesses.size(); i++) {
            Business business = businesses.get(i);
            Seller seller = new Seller();
            seller.setId(business.getId());
            seller.setPhone(business.getPhone());
            seller.setAddress(business.getAddress());
            seller.setName(business.getName());
            seller.setPicUrl(business.getPicUrl());
            seller.setLike(business.isLike());
            seller.setMinPrice(business.getMinPrice());
            seller.setMonthSales(business.getMonthSales());
            seller.setProductCategories(business.getProductCategories());
            seller.setShippingFee(business.getShippingFee());
            seller.setShippingTime(business.getShippingTime());
            sellers.add(seller);
        }

        new BmobBatch().insertBatch(sellers).doBatch(new QueryListListener<BatchResult>() {

            @Override
            public void onStart() {
                super.onStart();
                Logger.e("------onStart-----------------");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Logger.e("------onFinish-----------------");
            }

            @Override
            public void done(List<BatchResult> list, BmobException e) {
                if (e==null){
                    for(int i=0;i<list.size();i++){
                        BatchResult result = list.get(i);
                        BmobException ex =result.getError();
                        if(ex==null){
                            Logger.e("第"+i+"个数据批量添加成功："+result.getCreatedAt()+","+result.getObjectId()+","+result.getUpdatedAt());
                        }else{
                            Logger.e("第"+i+"个数据批量添加失败："+ex.getMessage()+","+ex.getErrorCode());
                        }
                    }
                }else{
                    Logger.e("Bmob添加异常"+e.getMessage()+","+e.getErrorCode());
                }
            }
        });
    }



    /**
     * 获取指定商家下的所有商品分类
     * @param callingId
     * @param business
     */
    private void doFetchProducts(final int callingId, Business business) {
        mRestApiClient.businessService()
                .products(business.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestCallback<List<ProductCategory>>() {
                    @Override
                    public void onResponse(List<ProductCategory> businesses) {
                        BusinessUi ui = findUi(callingId);
                        if (ui instanceof ProductListUi) {
                            ((ProductListUi) ui).onChangeItem(businesses);

                            Logger.e("该商家所有的产品类别为：" +  businesses.toString());
                            Logger.e("该商家所有的类别数为：" +  businesses.size());

                        }
                    }

                    @Override
                    public void onFailure(ResponseError error) {
                        BusinessUi ui = findUi(callingId);
                        if (ui instanceof ProductListUi) {
                            ui.onResponseError(error);
                        }
                    }
                });
    }


    /**
     * 收藏\取消收藏店铺
     * @param callingId
     * @param business
     */
    private void doFavoriteBusiness(final int callingId, Business business, final boolean isLike) {
        if (!AppCookie.isLoggin()) {
            BusinessUi ui = findUi(callingId);
            if (ui instanceof BusinessTabUi) {
                ResponseError error = new ResponseError(HTTP_UNAUTHORIZED,
                        StringFetcher.getString(R.string.toast_error_not_login));
                ui.onResponseError(error);
            }
        } else {
            mRestApiClient.businessService()
                    .favorite(business.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new RequestCallback<Favorite>() {
                        @Override
                        public void onResponse(Favorite favorite) {
                            BusinessUi ui = findUi(callingId);
                            if (ui instanceof BusinessTabUi) {
                                ((BusinessTabUi) ui).onFavoriteFinish(isLike);
                                Logger.e("点击收藏的店铺具体信息为："+favorite.toString());
                            }
                        }
                        @Override
                        public void onFailure(ResponseError error) {
                            BusinessUi ui = findUi(callingId);
                            if (ui instanceof BusinessTabUi) {
                                ui.onResponseError(error);
                            }
                        }
                    });
          }
    }


    //关于商家的回调(具体的实现方法在BusinessController)
    public interface BusinessUiCallbacks {   //只是定义一个接口，接口的实现在BusinessController
        void refresh(); //刷新

        void nextPage();//下一页

        //添加喜欢的商店
        void favoriteBusiness(Business business, boolean isLike);

        //是否允许结账
        boolean enableSettle(Business business);

        //还差多少钱配送
        double distanceSettlePrice(Business business);

        //打电话
        void callBusinessPhone(Business business);

        //跳到账单结算的页面
        void showSettle();

        //显示商家的信息
        void showBusiness(Business business);
    }


    //实现BusinessUiCallbacks里面的所有方法。
    @Override
    protected BusinessUiCallbacks createUiCallbacks(final BusinessUi ui) {
        return new BusinessUiCallbacks() {  //实现接口里面的所有方法
            //刷新数据
            @Override
            public void refresh() {
                if (ui instanceof BusinessListUi) {
                    mPageIndex = 1;
                    doFetchBusinesses(getId(ui), mPageIndex, PAGE_SIZE);
                } else if (ui instanceof ProductListUi) {
                    doFetchProducts(getId(ui), ui.getRequestParameter());
                }
            }

            //下一页数据
            @Override
            public void nextPage() {
                if (ui instanceof BusinessListUi) {
                    ++mPageIndex;
                    doFetchBusinesses(getId(ui), mPageIndex, PAGE_SIZE);
                }
            }

            //添加喜欢的商家
            @Override
            public void favoriteBusiness(Business business, boolean isLike) {
                doFavoriteBusiness(getId(ui), business, isLike);
            }


            //是否允许配送，是否允许结账
            @Override
            public boolean enableSettle(Business business) {
                ShoppingCart shoppingCart = ShoppingCart.getInstance(); //实例化购物车
                return (shoppingCart.getTotalPrice() > business.getMinPrice())//
                        && (shoppingCart.getTotalQuantity() > 0);
            }

            //还差多少钱配送
            @Override
            public double distanceSettlePrice(Business business) {
                ShoppingCart shoppingCart = ShoppingCart.getInstance();
                double distancePrice = business.getMinPrice() - shoppingCart.getTotalPrice();
                return distancePrice > 0 ? distancePrice : 0;
            }

            //打电话
            @Override
            public void callBusinessPhone(Business business) {
                Display display = getDisplay();
                if (display != null) {
                    display.callPhone(business.getPhone());
                }
            }

            //跳到该商家的详情页面(参数为该商家)
            @Override
            public void showBusiness(Business business) {
                Preconditions.checkNotNull(business, "business cannot be null");
                Display display = getDisplay();
                if (display != null) {
                    display.showBusiness(business);
                }
            }

            //跳到账单结算的页面
            @Override
            public void showSettle() {
                Display display = getDisplay();
                if (display != null) {
                    if (AppCookie.isLoggin()) {
                        display.showSettle();
                    } else {
                        display.showLogin();
                    }
                }
            }
        };
    }

    public enum BusinessTab {
        PRODUCT, COMMENT, DETAIL
    }






    public interface BusinessUi extends BaseController.Ui<BusinessUiCallbacks> {
        Business getRequestParameter();
    }

    public interface BusinessListUi extends BusinessUi, BaseController.ListUi<Business, BusinessUiCallbacks> {

    }

    //某一个商家所有的产品，去实现这个接口
    public interface BusinessTabUi extends BusinessUi {
        void setTabs(BusinessTab... tabs);
        void onFavoriteFinish(boolean isLike);
    }

    public interface ProductListUi extends BaseController.SubUi, BusinessUi {
        void onChangeItem(List<ProductCategory> items);
        void onShoppingCartChange();
    }

    /**
     * 评论列表的ui
     */
    public interface CommentListUi extends BaseController.SubUi, BusinessUi {

    }

    public interface BusinessProfileUi extends BaseController.SubUi, BusinessUi {

    }
}
