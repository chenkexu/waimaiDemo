package com.cheikh.lazywaimai.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.base.BaseController;
import com.cheikh.lazywaimai.base.BaseListFragment;
import com.cheikh.lazywaimai.context.AppContext;
import com.cheikh.lazywaimai.controller.BusinessController;
import com.cheikh.lazywaimai.model.bean.Business;
import com.cheikh.lazywaimai.ui.activity.MapActivity;
import com.cheikh.lazywaimai.ui.adapter.BusinessListAdapter;

import static com.cheikh.lazywaimai.util.Constants.ClickType.CLICK_TYPE_BUSINESS_CLICKED;

/**
 * author: cheikh.wang on 16/11/23
 * email: wanghonghi@126.com
 * 显示所有商品的fragment
 */
public class ShopFragment extends BaseListFragment<Business, BusinessController.BusinessUiCallbacks>
        implements BusinessController.BusinessListUi {

    @Override
    protected BaseController getController() {
        return AppContext.getContext().getMainController().getBusinessController();
    }

    @Override
    public Business getRequestParameter() {
        return null;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_shop);
    }

    //显示返回键
    @Override
    protected boolean isShowBack() {
        return false;
    }

    //显示图标


    @Override
    protected BusinessListAdapter getAdapter() {

        return new BusinessListAdapter();//获取Adapter
    }


    //刷新页面
    @Override
    protected void refreshPage() {
        getCallbacks().refresh();
    }


    //加载更多
    @Override
    protected void nextPage() {
        getCallbacks().nextPage();
    }

    @Override
    protected void onItemClick(int actionId, Business business) {
        switch (actionId) {
            case CLICK_TYPE_BUSINESS_CLICKED:
                getCallbacks().showBusiness(business);//显示某一个商家的具体信息
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getToolbar().inflateMenu(R.menu.menu_map);
        setHasOptionsMenu(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting:
                startActivity(new Intent(getActivity(), MapActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
