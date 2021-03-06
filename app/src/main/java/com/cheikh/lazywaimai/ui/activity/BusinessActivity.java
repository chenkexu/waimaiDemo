package com.cheikh.lazywaimai.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.base.BaseController;
import com.cheikh.lazywaimai.base.BaseTabActivity;
import com.cheikh.lazywaimai.context.AppContext;
import com.cheikh.lazywaimai.controller.BusinessController;
import com.cheikh.lazywaimai.model.Seller;
import com.cheikh.lazywaimai.model.bean.Business;
import com.cheikh.lazywaimai.model.bean.ResponseError;
import com.cheikh.lazywaimai.ui.Display;
import com.cheikh.lazywaimai.ui.fragment.BusinessDetailFragment;
import com.cheikh.lazywaimai.ui.fragment.CommentFragment;
import com.cheikh.lazywaimai.ui.fragment.ProductFragment;
import com.cheikh.lazywaimai.util.StringFetcher;
import com.cheikh.lazywaimai.util.ToastUtil;
import com.google.common.base.Preconditions;
import com.orhanobut.logger.Logger;
import com.wxhl.core.utils.T;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * author: cheikh.wang on 17/1/5
 * email: wanghonghi@126.com
 *
 * 某一个商家所有的产品
 */
public class BusinessActivity extends BaseTabActivity<BusinessController.BusinessUiCallbacks> //实现BusinessUiCallbacks接口
        implements BusinessController.BusinessTabUi {  //实现该接口BusinessTabUi

    private BusinessController.BusinessTab[] mTabs;  //上面的Tab信息
    private MenuItem mLikeMenuItem;//收藏的item

    private Business mBusiness; //点击的某个商家
    private boolean mIsLike; //是否喜欢

    @Override
    protected BaseController getController() {
        return AppContext.getContext().getMainController().getBusinessController();
    }

    @Override
    protected void handleIntent(Intent intent, Display display) {
        mBusiness = (Business) intent.getSerializableExtra(Display.PARAM_OBJ);//穿过了的点击的某个商家
    }

    @Override
    protected void initializeViews(Bundle savedInstanceState) {
        super.initializeViews(savedInstanceState);
        setTitle(mBusiness.getName());
    }

    @Override
    public Business getRequestParameter() {
        return mBusiness;
    }

    @Override
    protected String getTabTitle(int position) {
        if (mTabs != null) {
            return StringFetcher.getString(mTabs[position]);
        }
        return null;
    }

    @Override
    public void onFavoriteFinish(boolean isLike) {
        cancelLoading();
        updateLikeMenuIcon(isLike);
        ToastUtil.showToast(isLike ? R.string.toast_success_favorite_business
                : R.string.toast_success_cancel_favorite_business);
    }

    @Override
    public void onResponseError(ResponseError error) {
        cancelLoading();
        ToastUtil.showToast(error.getMessage());
    }

    /**
     * 点击切换Tab
     * @param tabs
     */
    @Override
    public void setTabs(BusinessController.BusinessTab... tabs) {
        Preconditions.checkNotNull(tabs, "tabs cannot be null");
        mTabs = tabs;
        if (getAdapter().getCount() != tabs.length) {
            ArrayList<Fragment> fragments = new ArrayList<>();
            for (BusinessController.BusinessTab tab : tabs) {
                fragments.add(createFragmentForTab(tab));
            }
            setFragments(fragments);
        }
    }

    //加载不同的Fragment
    private Fragment createFragmentForTab(BusinessController.BusinessTab tab) {
        switch (tab) {
            case PRODUCT: //该商家所有的产品
                return ProductFragment.create(mBusiness);
            case COMMENT: //该商家的评论
                return CommentFragment.create(mBusiness);
            case DETAIL: //商家的详细信息
                return BusinessDetailFragment.create(mBusiness);
        }
        return null;
    }

    //更新是否喜欢的界面
    private void updateLikeMenuIcon(boolean isLike) {
        mIsLike = isLike;
        mLikeMenuItem.setIcon(isLike ? R.drawable.ic_liked : R.drawable.ic_like);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_business, menu);
        mLikeMenuItem = menu.findItem(R.id.menu_like);
        updateLikeMenuIcon(mBusiness.isLike());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_call: //打电话
                getCallbacks().callBusinessPhone(mBusiness);
                return true;
            case R.id.menu_like: //收藏喜欢
                showLoading(R.string.label_being_something);
                getCallbacks().favoriteBusiness(mBusiness, !mIsLike);
                String id = mBusiness.getId();

                Logger.e("id:----:"+id);

                BmobQuery<Seller> query = new BmobQuery<Seller>();
                query.addWhereEqualTo("id", id);  //条件查询

                query.findObjects(new FindListener<Seller>() {
                    @Override
                    public void done(List<Seller> list, BmobException e) {
                        if (e==null){
                            T.showShort(BusinessActivity.this,"查询成功");
                            final Seller seller = list.get(0);
                            seller.setLike(!mIsLike);
                            seller.update(seller.getObjectId(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if(e==null){
                                        T.showShort(BusinessActivity.this,"更新成功:"+seller.getUpdatedAt());
                                    }else{
                                        Logger.e("异常信息为："+e.getMessage()+e.getErrorCode());
                                        T.showShort(BusinessActivity.this,"更新失败:"+seller.getUpdatedAt());
                                    }
                                }
                            });

                        }else{
                            T.showShort(BusinessActivity.this,"查询失败");
                        }
                    }
                });



//                final Seller seller = new Seller();
//                seller.setLike(!mIsLike);
//                seller.update(id, new UpdateListener() {
//                    @Override
//                    public void done(BmobException e) {
//                        if(e==null){
//                            T.showShort(BusinessActivity.this,"更新成功:"+seller.getUpdatedAt());
//                        }else{
//                            Logger.e("异常信息为："+e.getMessage()+e.getErrorCode());
//                            T.showShort(BusinessActivity.this,"更新失败:"+seller.getUpdatedAt());
//                        }
//                    }
//                });
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
