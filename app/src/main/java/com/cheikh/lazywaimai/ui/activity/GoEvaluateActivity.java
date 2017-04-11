package com.cheikh.lazywaimai.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.constants.IntentConstants;
import com.cheikh.lazywaimai.context.AppCookie;
import com.cheikh.lazywaimai.model.bean.Order;
import com.cheikh.lazywaimai.model.bean.User;
import com.cheikh.lazywaimai.ui.Display;
import com.cheikh.lazywaimai.util.ActivityStack;
import com.cheikh.lazywaimai.util.DateUtil;
import com.cheikh.lazywaimai.widget.ProperRatingBar;
import com.lidong.photopicker.PhotoPickerActivity;
import com.lidong.photopicker.PhotoPreviewActivity;
import com.lidong.photopicker.SelectModel;
import com.lidong.photopicker.intent.PhotoPickerIntent;
import com.lidong.photopicker.intent.PhotoPreviewIntent;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import mvp.circledemo.activity.CircleActivity;
import mvp.circledemo.bean.CircleItem;
import mvp.circledemo.bean.CommentItem;
import mvp.circledemo.bean.FavortItem;
import mvp.circledemo.bean.PhotoInfo;


public class GoEvaluateActivity extends AppCompatActivity  {
    @Nullable
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.et_evaluate_detail)
    EditText mEvaluate;

    @Bind(R.id.rating_product_rate)
    ProperRatingBar properRatingBar;

    @Bind(R.id.grv_photos)
    GridView gridView;
    private String evaluateContent;//发送的内容
    private int rating = 0;//几颗星?
    private Order order;//评价的某个订单

    private GridAdapter gridAdapter;
    private ArrayList<String> imagePaths = new ArrayList<>(); //选择的照片的地址

    private static final int REQUEST_CAMERA_CODE = 10;// 选择照片
    private static final int REQUEST_PREVIEW_CODE = 20;//预览

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_evaluate);
        ActivityStack.create().add(this);
        ButterKnife.bind(this);
        initializeToolbar();
        initializeViews(savedInstanceState);

        int cols = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi;
        cols = cols < 3 ? 3 : cols;
        gridView.setNumColumns(cols); //显示列数


        // preview点击已经选择的图片
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String imgs = (String) parent.getItemAtPosition(position);
                if ("000000".equals(imgs) ){ //如果点击的是“+”按钮
                    PhotoPickerIntent intent = new PhotoPickerIntent(GoEvaluateActivity.this);
                    intent.setSelectModel(SelectModel.MULTI);
                    intent.setShowCarema(true); // 是否显示拍照
                    intent.setMaxTotal(6); // 最多选择照片数量，默认为6
                    intent.setSelectedPaths(imagePaths); // 已选中的照片地址， 用于回显选中状态
                    startActivityForResult(intent, REQUEST_CAMERA_CODE);
                }else{ //预览
                    PhotoPreviewIntent intent = new PhotoPreviewIntent(GoEvaluateActivity.this);
                    intent.setCurrentItem(position);
                    intent.setPhotoPaths(imagePaths);
                    startActivityForResult(intent, REQUEST_PREVIEW_CODE);
                }
            }
        });
        imagePaths.add("000000");
        gridAdapter = new GridAdapter(imagePaths);
        gridView.setAdapter(gridAdapter);
    }

    /**
     * 返回到原来的界面
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                // 选择照片之后：
                case REQUEST_CAMERA_CODE:
                    ArrayList<String> list = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT);
                    Logger.e("list: " + "list = [" + list.size());
                    loadAdpater(list);//显示图片
                    break;
                // 预览照片之后：
                case REQUEST_PREVIEW_CODE:
                    ArrayList<String> ListExtra = data.getStringArrayListExtra(PhotoPreviewActivity.EXTRA_RESULT);
                    Logger.e( "ListExtra: " + "ListExtra = [" + ListExtra.size());
                    loadAdpater(ListExtra);
                    break;
            }
        }
    }


    private void initializeToolbar() {
        if (mToolbar != null) {
            mToolbar.setTitle(getTitle());
            setSupportActionBar(mToolbar);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void initializeViews(Bundle savedInstanceState) {
        mToolbar.setTitle("您的评价是我们的动力");
        properRatingBar.setListener(new ProperRatingBar.RatingListener() {
            @Override
            public void onRatePicked(ProperRatingBar ratingBar) {
                rating = ratingBar.getRating();
            }
        });
        order = (Order) getIntent().getSerializableExtra(Display.PARAM_OBJ);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_remark, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_finish) {
            submitEvaluate();
            finish();
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void loadAdpater(ArrayList<String> paths){
        if (imagePaths!=null&& imagePaths.size()>0){
            imagePaths.clear();
        }
        if (paths.contains("000000")){
            paths.remove("000000");
        }
        paths.add("000000");
        imagePaths.addAll(paths);
        Logger.e("imagePaths:"+imagePaths.toString());
        gridAdapter  = new GridAdapter(imagePaths);
        gridView.setAdapter(gridAdapter);
        try{
            JSONArray obj = new JSONArray(imagePaths);
            Log.e("--", obj.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private class GridAdapter extends BaseAdapter {
        private ArrayList<String> listUrls;
        private LayoutInflater inflater;
        public GridAdapter(ArrayList<String> listUrls) {
            this.listUrls = listUrls;
            if(listUrls.size() == 7){
                listUrls.remove(listUrls.size()-1);
            }
            inflater = LayoutInflater.from(GoEvaluateActivity.this);
        }

        public int getCount(){
            return  listUrls.size();
        }
        @Override
        public String getItem(int position) {
            return listUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_image, parent,false);
                holder.image = (ImageView) convertView.findViewById(R.id.imageView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            final String path=listUrls.get(position);

            if (path.equals("000000")){
                holder.image.setImageResource(R.drawable.icon_addpic_focused);
            }else {
                Log.e("main","path:-----------"+path);
                Glide.with(GoEvaluateActivity.this)
                        .load(path)
                        .placeholder(R.mipmap.img_header)
                        .error(R.mipmap.img_header)
                        .centerCrop()
                        .crossFade()
                        .into(holder.image);
            }
            return convertView;
        }
        class ViewHolder {
            ImageView image;
        }
    }

    //提交信息
    private void submitEvaluate() {
        evaluateContent = mEvaluate.getText().toString();
        CircleItem item = new CircleItem();
        User user = AppCookie.getUserInfo();
        item.setUser(new mvp.circledemo.bean.User(user.getId(),user.getNickname(),user.getAvatarUrl()));
        item.setContent(evaluateContent);
        item.setCreateTime(DateUtil.getDate(new Date()));
        item.setRating(rating);
        item.setOrder(order);

        item.setType(CircleItem.TYPE_IMG);
        //评论
        List<CommentItem> items = new ArrayList<>();
        item.setComments(items);
        //赞
        List<FavortItem> favorters  = new ArrayList<>();
        item.setFavorters(favorters);

        List<PhotoInfo> photos = new ArrayList<>();
        imagePaths.remove("000000");
        for (int i = 0; i <imagePaths.size() ; i++) {
            PhotoInfo photoInfo = new PhotoInfo();
            photoInfo.url = imagePaths.get(i);
            photoInfo.w = 800;
            photoInfo.h = 800;
            photos.add(photoInfo);
            Logger.e("imagePaths:"+imagePaths.get(i));
        }

        item.setPhotos(photos);
        Intent intent = new Intent(this,CircleActivity.class);
        intent.putExtra(IntentConstants.CIRCLE_ITEM,item);
        startActivity(intent);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }


}
