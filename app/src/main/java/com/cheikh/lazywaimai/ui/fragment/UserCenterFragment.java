package com.cheikh.lazywaimai.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.base.BaseController;
import com.cheikh.lazywaimai.base.BaseFragment;
import com.cheikh.lazywaimai.context.AppContext;
import com.cheikh.lazywaimai.controller.UserController;
import com.cheikh.lazywaimai.model.bean.ResponseError;
import com.cheikh.lazywaimai.model.bean.User;
import com.cheikh.lazywaimai.util.ContentView;
import com.cheikh.lazywaimai.util.ToastUtil;
import com.cheikh.lazywaimai.widget.PicassoImageView;
import com.cheikh.lazywaimai.widget.section.SectionTextItemView;

import butterknife.Bind;
import butterknife.OnClick;
import mvp.circledemo.activity.CircleActivity;

//  “我的” 的Fragment
@ContentView(R.layout.fragment_user_center)
public class UserCenterFragment extends BaseFragment<UserController.UserUiCallbacks> //继承，实现接口UserUiCallbacks
        implements UserController.UserCenterUi { //实现接口，显示个人信息

    @Bind(R.id.layout_login_before)
    View mLoginBeforeLayout;

    @Bind(R.id.layout_login_after)
    View mLoginAfterLayout;

    @Bind(R.id.img_avatar)
    PicassoImageView mAvatarImg;

    @Bind(R.id.txt_nickname)
    TextView mNicknameTxt;

    @Bind(R.id.txt_user_phone)
    TextView mUserPhoneTxt;

    @Bind(R.id.btn_my_address)
    SectionTextItemView mManageAddressBtn;

    @Bind(R.id.btn_my_favorites)
    SectionTextItemView mManageFavoriteBtn;

    @Bind(R.id.btn_my_evaluates)
    SectionTextItemView mManageEvaluate;

    @Override
    protected BaseController getController() {
        return AppContext.getContext().getMainController().getUserController();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.title_user_center);
    }

    @Override
    protected boolean isShowBack() {
        return false;
    }

    @Override
    protected void initializeViews(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
    }

    @Override
    public void showUserInfo(User user) {
        if (user != null) {
            mLoginBeforeLayout.setVisibility(View.GONE);
            mLoginAfterLayout.setVisibility(View.VISIBLE);
            mAvatarImg.loadProfile(user);
            mNicknameTxt.setText(user.getNickname());
            mUserPhoneTxt.setText(user.getMobile());
        } else {
            mLoginBeforeLayout.setVisibility(View.VISIBLE);
            mLoginAfterLayout.setVisibility(View.GONE);
        }
    }

    //异常信息处理
    @Override
    public void onResponseError(ResponseError error) {
        ToastUtil.showToast(error.getMessage());
    }

    @OnClick({
            R.id.layout_login_before,
            R.id.layout_login_after,
            R.id.btn_my_address,
            R.id.btn_my_favorites,
            R.id.btn_my_evaluates
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_login_before:
                getCallbacks().showLogin();
                break;
            case R.id.layout_login_after:
                getCallbacks().showUserProfile();
                break;
            case R.id.btn_my_address:
                getCallbacks().showAddressList();
                break;
            case R.id.btn_my_favorites:
                getCallbacks().showFavoriteList();
                break;
            case R.id.btn_my_evaluates:
                startActivity(new Intent(getActivity(), CircleActivity.class));
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_center, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting:
                getCallbacks().showSetting();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
