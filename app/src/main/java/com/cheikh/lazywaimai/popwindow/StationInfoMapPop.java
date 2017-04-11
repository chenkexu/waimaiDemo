package com.cheikh.lazywaimai.popwindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cheikh.lazywaimai.Dialog.WaterBlueDialogComfirmNav;
import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.model.Seller;
import com.cheikh.lazywaimai.model.bean.Business;
import com.cheikh.lazywaimai.ui.activity.BusinessActivity;
import com.cheikh.lazywaimai.util.CalculateUtil;
import com.cheikh.lazywaimai.widget.ProperRatingBar;
import com.easy.util.ToastUtil;

import static com.cheikh.lazywaimai.R.id.btn_navigation;
import static com.cheikh.lazywaimai.R.id.btn_sell;
import static com.cheikh.lazywaimai.R.id.tv_distance;
import static com.cheikh.lazywaimai.ui.Display.PARAM_OBJ;


/**
 * Created by Administrator on 2016/12/20.
 */

/**
 * 底部弹窗
 */
public class StationInfoMapPop extends BasePop implements View.OnClickListener{
    private RelativeLayout rlStationInfo;
    private TextView tvName;//名称
    private ImageView ivPileImage; //图片
    private TextView tvLocation;  //具体位置
    private TextView tvDistance; //距离
    private View topSpliteLine;
    private LinearLayout llNavigation;
    private Button btnSell; //立即购买
    private Button btnNavigation; //导航
    private Context context;
    private BDLocation mLocation;//当前定位的位置
    private Seller seller;//popWindow中要显示的商家
    private ProperRatingBar tvRate;

    public StationInfoMapPop(Context context, BDLocation mLocation, Seller seller) {
        super(context);// 必须调用父类的构造函数
        this.context = context;
        this.mLocation = mLocation;
        this.seller = seller;
    }


    @Override
    protected void initUI() {
        super.initUI();
        View v = LayoutInflater.from(mActivity).inflate(R.layout.layout_station_info_map_pop, null);//popWindow的布局

        // ps，关于pop，我们也可以在构造函数中传入view，而不必setContentView，因为构造函数中的view，其实最终也要setContentView
        setContentView(v);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setBackgroundDrawable(new BitmapDrawable());
        setOutsideTouchable(false);
        setFocusable(true);// 取得焦点
        //设置可以点击
        setTouchable(true);

        rlStationInfo = (RelativeLayout) v.findViewById(R.id.rl_station_info);
        tvName = (TextView) v.findViewById(R.id.tv_name);
        tvRate = (ProperRatingBar) v.findViewById(R.id.tv_seller_rate);
        ivPileImage = (ImageView) v.findViewById(R.id.iv_pile_image);
        tvLocation = (TextView) v.findViewById(R.id.tv_location);
        tvDistance = (TextView) v.findViewById(tv_distance);//距离
        topSpliteLine = (View) v.findViewById(R.id.top_splite_line);
        llNavigation = (LinearLayout) v.findViewById(R.id.ll_navigation);
        btnSell = (Button) v.findViewById(btn_sell);
        btnNavigation = (Button) v.findViewById(btn_navigation);
        tvRate.setListener(new ProperRatingBar.RatingListener() {
            @Override
            public void onRatePicked(ProperRatingBar ratingBar) {
                ratingBar.setRating(ratingBar.getRating());
            }
        });
    }

    /**
     * 初始化popWindow上面的数据
     */
    public  void init(){
        // 计算距离
        if (seller.getLatitude() != null && seller.getLongitude() != null) {
            tvDistance.setVisibility(View.VISIBLE);
            LatLng eLatLng = new LatLng(seller.getLatitude(), seller.getLongitude());
            CalculateUtil.infuseDistance(context, tvDistance, eLatLng);
            // 设定距离文本,利用html文本用橙色颜色标记距离数
//			String distanceInfo = context.getString(R.string.to_tag_distance, String.format("%.1f", distance));
//			tv_distance.setText(Html.fromHtml(String.format("%.1f", distance) + "km"));
        } else {
            tvDistance.setText("距离未知");
        }
        tvName.setText(seller.getName());
        if (seller.getAddress()!=null){
            tvLocation.setText(seller.getAddress());
        }
        Glide.with(context).load(seller.getPicUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.color.bg_no_photo)
             .into(ivPileImage);
        tvRate.setRating(2);//评价
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        btnNavigation.setOnClickListener(this);
        btnSell.setOnClickListener(this);
        rlStationInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case btn_navigation:
                callNavigation();
                break;
            case R.id.btn_sell: //去展示信息
                goProductDetail();
                break;
            case R.id.rl_station_info: //去展示信息
                goProductDetail();
                break;
        }
    }


    /**
     * 去卖家详情页
     */
    private void goProductDetail(){
        Business business = new Business();
        business.setId(seller.getId());
        Intent intent = new Intent(mActivity, BusinessActivity.class);
        intent.putExtra(PARAM_OBJ, business);
        mActivity.startActivity(intent);
    }

    //打开导航
    private void callNavigation() {
        //初始化导航弹窗
        WaterBlueDialogComfirmNav waterBlueDialogComfirmNav = new WaterBlueDialogComfirmNav(context);
        if (mLocation != null) {
            //显示弹窗
            waterBlueDialogComfirmNav.show(mLocation.getLatitude(), mLocation.getLongitude(), seller.getLatitude(),seller.getLongitude());
        } else {
            ToastUtil.show(context, "尚未定位您的位置,请稍候");
        }
        dismiss();

    }


}
