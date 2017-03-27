package com.cheikh.lazywaimai.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.base.BaseController;
import com.cheikh.lazywaimai.base.BaseFragment;
import com.cheikh.lazywaimai.context.AppContext;
import com.cheikh.lazywaimai.controller.BusinessController;
import com.cheikh.lazywaimai.model.ShoppingCart;
import com.cheikh.lazywaimai.model.bean.Business;
import com.cheikh.lazywaimai.model.bean.ProductCategory;
import com.cheikh.lazywaimai.model.bean.ResponseError;
import com.cheikh.lazywaimai.ui.Display;
import com.cheikh.lazywaimai.ui.adapter.ProductCategoryListAdapter;
import com.cheikh.lazywaimai.ui.adapter.ProductItemAdapter;
import com.cheikh.lazywaimai.util.CollectionUtil;
import com.cheikh.lazywaimai.util.ContentView;
import com.cheikh.lazywaimai.util.StringFetcher;
import com.cheikh.lazywaimai.widget.MultiStateView;
import com.cheikh.lazywaimai.widget.ShoppingCartPanel;
import com.flipboard.bottomsheet.BottomSheetLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;

//该商家所有商品的Fragment
@ContentView(R.layout.fragment_product)
public class ProductFragment extends BaseFragment<BusinessController.BusinessUiCallbacks>
        implements BusinessController.ProductListUi, BaseController.SubUi {

    @Bind(R.id.multi_state_view)
    MultiStateView mMultiStateView;

    @Bind(R.id.bottom_sheet_layout)
    BottomSheetLayout mBottomSheetLayout; //底部抽屉栏

    @Bind(R.id.lv_product_category)
    ListView mCategoryListView; //商品分类列表

    @Bind(R.id.lv_product_list)
    PinnedHeaderListView mProductListView; //商品分类下所有的商品列表

    @Bind(R.id.layout_shopping_info)
    LinearLayout mShoppingInfoLayout;  //购买信息汇总

    @Bind(R.id.img_cart_logo)
    ImageView mCartLogoImg;  //购物车的图标

    @Bind(R.id.txt_selected_count)
    TextView mSelectedCountTxt; //购买的数量

    @Bind(R.id.txt_total_price)
    TextView mTotalPriceTxt;  //总价格

    @Bind(R.id.btn_settle)
    Button mSettleBtn; //去结算

    private ProductCategoryListAdapter mCategoryItemAdapter; //商品分类的列表adapter

    private ProductItemAdapter mProductItemAdapter;

    private ShoppingCartPanel mShoppingCartPanel; //购物车面板
    private boolean isClickTrigger;
    private Business mBusiness;  //得到的商家信息

    public static ProductFragment create(Business business) {
        ProductFragment fragment = new ProductFragment();
        if (business != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Display.PARAM_OBJ, business);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    protected BaseController getController() {
        return AppContext.getContext().getMainController().getBusinessController();
    }

    @Override
    protected void handleArguments(Bundle arguments) {
        if (arguments != null) {
            //获取到某个商家
            mBusiness = (Business) arguments.getSerializable(Display.PARAM_OBJ);
        }
    }

    @Override
    public Business getRequestParameter() {
        return mBusiness;
    }

    //组件初始化
    @Override
    protected void initializeViews(Bundle savedInstanceState) {
        mShoppingCartPanel = new ShoppingCartPanel(getContext());

        // 商品分类的列表adapter
        mCategoryItemAdapter = new ProductCategoryListAdapter();
        mCategoryListView.setAdapter(mCategoryItemAdapter);
        mCategoryListView.setSelection(0);//默认选择第一个分类


        //点击某一个类别
        mCategoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int productPos = 0;
                for (int index = 0; index < position; index++) {
                    // 加1是因为section也算一个位置
                    productPos += mProductItemAdapter.getCountForSection(index) + 1;
                }
                isClickTrigger = true;
                //某一个类别下共有多少商品 + 有几个类别
                mProductListView.setSelection(productPos);//商品详情列表选择到某一个位置
            }
        });


        // 商品分类的列表的adapter
        mProductItemAdapter = new ProductItemAdapter(getActivity());
        mProductItemAdapter.setAnimTargetView(mCartLogoImg);

        mProductListView.setAdapter(mProductItemAdapter);

        //商品列表滚动的过程中
        mProductListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (isClickTrigger) {
                    isClickTrigger = false;
                } else {
                    int section = mProductItemAdapter.getSectionForPosition(firstVisibleItem);
                    mCategoryListView.setItemChecked(section, true);
                }
            }
        });

        mMultiStateView.setState(MultiStateView.STATE_LOADING);
    }



    @OnClick({R.id.btn_settle, R.id.layout_shopping_info})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_settle://点击结账
                getCallbacks().showSettle();
                break;
            case R.id.layout_shopping_info: //点击购物车
                showShoppingCartPanel();
                break;
        }
    }


    //切换左边商家的商品条目
    @Override
    public void onChangeItem(List<ProductCategory> productCategories) {
        //参数得到所有的类别
        if (!CollectionUtil.isEmpty(productCategories)) { //如果商品分类不为空
            mCategoryItemAdapter.setItems(productCategories);
            mProductItemAdapter.setItems(productCategories);
            showShoppingCartPanel();//显示购物车面板
            refreshBottomUi();
            mMultiStateView.setState(MultiStateView.STATE_CONTENT);
        } else {
            mMultiStateView.setState(MultiStateView.STATE_EMPTY)
                    .setTitle(R.string.label_empty_product_list)
                    .setButton(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getCallbacks().refresh();
                        }
                    });
        }
    }

    @Override
    public void onResponseError(ResponseError error) {

        mMultiStateView.setState(MultiStateView.STATE_ERROR)
                .setTitle(error.getMessage())
                .setButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getCallbacks().refresh();//异常，点击重新加载
                    }
                });
    }


    @Override
    public void onShoppingCartChange() {
        refreshBottomUi();
        mShoppingCartPanel.refreshPanel();
        mCategoryItemAdapter.notifyDataSetChanged();
        mProductItemAdapter.notifyDataSetChanged();
    }

    /**
     * 更新底部购物车面板
     */
    private void refreshBottomUi() {
        ShoppingCart shoppingCart = ShoppingCart.getInstance();
        int totalCount = shoppingCart.getTotalQuantity();
        double totalPrice = shoppingCart.getTotalPrice();
        double distancePrice = getCallbacks().distanceSettlePrice(mBusiness);
        mCartLogoImg.setSelected(totalCount > 0);//如果购物车有商品，购物车图标被选择
        //购买的数量
        mSelectedCountTxt.setText(StringFetcher.getString(R.string.label_count, totalCount));
        //总价格
        mTotalPriceTxt.setText(StringFetcher.getString(R.string.label_price, totalPrice));

        //是否显示去结算
        mSettleBtn.setEnabled(getCallbacks().enableSettle(mBusiness));

        //显示还差多少配送
        mSettleBtn.setText(distancePrice != 0 ? getString(R.string.label_distance_price, distancePrice) : getString(R.string.btn_settle));
    }

    /**
     * 显示购物车面板
     */
    private void showShoppingCartPanel() {
        int count = ShoppingCart.getInstance().getTotalQuantity();//获取购物车里所有商品的数量
        if (count > 0 && !mBottomSheetLayout.isSheetShowing()) { //如果购物车不为空，并且抽屉是打开的
            mBottomSheetLayout.showWithSheetView(mShoppingCartPanel);//打开抽屉
        } else {
            mBottomSheetLayout.dismissSheet();
        }
    }
}