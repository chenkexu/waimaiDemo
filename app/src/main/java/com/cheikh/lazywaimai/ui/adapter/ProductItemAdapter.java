package com.cheikh.lazywaimai.ui.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.model.ShoppingCart;
import com.cheikh.lazywaimai.model.bean.Product;
import com.cheikh.lazywaimai.model.bean.ProductCategory;
import com.cheikh.lazywaimai.util.CollectionUtil;
import com.cheikh.lazywaimai.util.StringFetcher;
import com.cheikh.lazywaimai.widget.PicassoImageView;
import com.cheikh.lazywaimai.widget.ProperRatingBar;
import com.cheikh.lazywaimai.widget.ShoppingCountView;

import java.util.List;

import za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter;

/**
 * 具体的商品的信息的Adapter
 */
public class ProductItemAdapter extends SectionedBaseAdapter {

    private LayoutInflater mInflater;
    private List<ProductCategory> mCategories;  //所有的商品分类

    private View mAnimTargetView;

    public ProductItemAdapter(Activity activity) {
        mInflater = LayoutInflater.from(activity);
    }

    public void setAnimTargetView(View animTargetView) {
        mAnimTargetView = animTargetView;
    }

    public void setItems(List<ProductCategory> categories) {
        mCategories = categories;
        notifyDataSetChanged();
    }

    //计算某一个类别下共有多少商品
    @Override
    public int getCountForSection(int section) {
        if (mCategories != null) {
            //某一分类下的所有商品
            List<Product> products = mCategories.get(section).getProducts();
            if (!CollectionUtil.isEmpty(products)) {
                return products.size();
            }
        }
        return 0;
    }

    @Override
    public int getSectionCount() {
        return mCategories != null ? mCategories.size() : 0;
    }

    @Override
    public Product getItem(int section, int position) {
        List<Product> products = mCategories.get(section).getProducts();
        return products.get(position);
    }

    @Override
    public long getItemId(int section, int position) {
        return position;
    }

    @Override
    public View getItemView(int section, int position, View convertView, ViewGroup viewGroup) {
        final ItemViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.layout_product_item, null);
            holder = new ItemViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder) convertView.getTag();
        }
        Product product = getItem(section, position);
        holder.photoImg.loadProductPhoto(product);
        holder.nameTxt.setText(product.getName());
        holder.priceTxt.setText(StringFetcher.getString(R.string.label_price, product.getPrice()));
        holder.monthSalesTxt.setText(StringFetcher.getString(R.string.label_month_sales, product.getMonthSales()));
        holder.rateRatingBar.setRating(product.getRate()); //评价几颗星


        if (!TextUtils.isEmpty(product.getDescription())) {
            holder.descriptionTxt.setVisibility(View.VISIBLE);
            holder.descriptionTxt.setText(product.getDescription());
        } else {
            holder.descriptionTxt.setVisibility(View.GONE);
        }
        if (product.getLeftNum() > 0) {
            final Product finalProduct = product;
            // 获取购物车里指定商品的数量
            int quantity = ShoppingCart.getInstance().getQuantityForProduct(finalProduct);
            holder.shoppingCountView.setShoppingCount(quantity);
            holder.shoppingCountView.setAnimTargetView(mAnimTargetView);

            //点击购买的加号控件
            holder.shoppingCountView.setOnShoppingClickListener(new ShoppingCountView.ShoppingClickListener() {
                @Override
                public void onAddClick(int num) {  //增加数量
                    if (!ShoppingCart.getInstance().push(finalProduct)) {
                        // 添加失败则恢复数量
                        int oldQuantity = ShoppingCart.getInstance().getQuantityForProduct(finalProduct);
                        holder.shoppingCountView.setShoppingCount(oldQuantity);
                        showClearDialog();
                    }
                }

                @Override
                public void onMinusClick(int num) {
                    if (!ShoppingCart.getInstance().pop(finalProduct)) {
                        // 减少失败则恢复数量
                        int oldQuantity = ShoppingCart.getInstance().getQuantityForProduct(finalProduct);
                        holder.shoppingCountView.setShoppingCount(oldQuantity);
                    }
                }
            });
            holder.shoppingCountView.setVisibility(View.VISIBLE);
            holder.leftNumTxt.setVisibility(View.GONE);
        } else {
            holder.leftNumTxt.setText(StringFetcher.getString(R.string.label_sold_out));
            holder.leftNumTxt.setVisibility(View.VISIBLE);
            holder.shoppingCountView.setVisibility(View.GONE);
        }

        return convertView;
    }


    @Override
    public View getSectionHeaderView(int position, View convertView, ViewGroup viewGroup) {
        HeaderViewHolder holder; //顶部视图
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.layout_product_header, viewGroup, false);
            holder = new HeaderViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //得到某一类商品
        ProductCategory productCategory = mCategories.get(position);
        holder.titleTxt.setText(productCategory.getName()); //显示类别名称
        if (!TextUtils.isEmpty(productCategory.getDescription())) {
            holder.descText.setText(productCategory.getDescription());
            holder.descText.setVisibility(View.VISIBLE);
        } else {
            holder.descText.setVisibility(View.GONE);
        }

        return convertView;
    }

    ////////////////////////////////////////////
    ///            view holder               ///
    ////////////////////////////////////////////

    public static class HeaderViewHolder {
        TextView titleTxt;
        TextView descText;

        HeaderViewHolder(View headerView) {
            titleTxt = (TextView) headerView.findViewById(R.id.txt_title);
            descText = (TextView) headerView.findViewById(R.id.txt_desc);
        }
    }

    //某一个具体的产品
    public static class ItemViewHolder {
        PicassoImageView photoImg;
        TextView nameTxt;
        TextView priceTxt;
        TextView descriptionTxt;
        TextView monthSalesTxt;
        ProperRatingBar rateRatingBar; //几颗星
        TextView leftNumTxt;
        ShoppingCountView shoppingCountView; //加减号控件

        ItemViewHolder(View itemView) {
            photoImg = (PicassoImageView) itemView.findViewById(R.id.img_product_photo);
            nameTxt = (TextView) itemView.findViewById(R.id.txt_product_name);
            priceTxt = (TextView) itemView.findViewById(R.id.txt_product_price);
            descriptionTxt = (TextView) itemView.findViewById(R.id.txt_product_description);
            monthSalesTxt = (TextView) itemView.findViewById(R.id.txt_product_month_sales);
            rateRatingBar = (ProperRatingBar) itemView.findViewById(R.id.rating_product_rate);
            leftNumTxt = (TextView) itemView.findViewById(R.id.txt_product_left_num);
            shoppingCountView = (ShoppingCountView) itemView.findViewById(R.id.shopping_count_view);
        }
    }

    private void showClearDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mInflater.getContext());
        builder.setTitle(R.string.dialog_shopping_cart_business_conflict_title);
        builder.setMessage(R.string.dialog_shopping_cart_business_conflict_message);
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.setPositiveButton(R.string.dialog_clear, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ShoppingCart.getInstance().clearAll();
            }
        });
        builder.create().show();
    }
}