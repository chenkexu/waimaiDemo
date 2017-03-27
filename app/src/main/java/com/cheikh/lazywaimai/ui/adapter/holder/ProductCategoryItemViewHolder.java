package com.cheikh.lazywaimai.ui.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.base.BaseViewHolder;
import com.cheikh.lazywaimai.model.ShoppingCart;
import com.cheikh.lazywaimai.model.bean.ProductCategory;

import butterknife.Bind;
import cn.bingoogolapple.badgeview.BGABadgeFrameLayout;

/**
 * author: cheikh.wang on 16/11/23
 * email: wanghonghi@126.com
 */
public class ProductCategoryItemViewHolder extends BaseViewHolder<ProductCategory> {

    @Bind(R.id.txt_name)
    TextView mNameTxt;

    @Bind(R.id.badge_view)
    BGABadgeFrameLayout mBadgeView;

    public ProductCategoryItemViewHolder(View itemView) {
        super(itemView);
    }

    public void bind(ProductCategory category) {
        mNameTxt.setText(category.getName());  //类别的名字

        //获取购物车里指定商品分类的数量
        int count = ShoppingCart.getInstance().getQuantityForCategory(category);

        if (count > 0) {
            mBadgeView.showTextBadge(String.valueOf(count)); //设置提示小红点信息
        } else {
            mBadgeView.hiddenBadge(); //影藏小红点
        }
    }
}
