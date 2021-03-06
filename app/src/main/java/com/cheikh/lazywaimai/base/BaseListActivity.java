package com.cheikh.lazywaimai.base;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.cheikh.lazywaimai.R;
import com.cheikh.lazywaimai.model.bean.ResponseError;
import com.cheikh.lazywaimai.ui.adapter.wrapper.LoadMoreWrapperAdapter;
import com.cheikh.lazywaimai.util.ContentView;
import com.cheikh.lazywaimai.util.ToastUtil;
import com.cheikh.lazywaimai.util.ViewEventListener;
import com.cheikh.lazywaimai.widget.MultiStateView;
import com.cheikh.lazywaimai.widget.refresh.OnRefreshListener;
import com.cheikh.lazywaimai.widget.refresh.RefreshLayout;
import java.util.List;
import butterknife.Bind;

/**
 * author: cheikh.wang on 17/1/11
 * email: wanghonghi@126.com
 *
 * //带列表的Fragment
 */
@ContentView(R.layout.fragment_recyclerview)
public abstract class BaseListActivity<T, UC> extends BaseActivity<UC>
        implements BaseController.ListUi<T, UC> {

    @Bind(R.id.multi_state_view)
    MultiStateView mMultiStateView;

    @Bind(R.id.refresh_layout)
    RefreshLayout mRefreshLayout;

    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView;

    protected LoadMoreWrapperAdapter<T> mAdapter;

    @Override
    protected void initializeViews(Bundle savedInstanceState) {
        mRefreshLayout.setEnabled(getEnableRefresh());
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshPage();
            }
        });

        mAdapter = new LoadMoreWrapperAdapter<>(getAdapter());
        mAdapter.setViewEventListener(new ViewEventListener<T>() {
            @Override
            public void onViewEvent(int actionId, T item, int position, View view) {
                onItemClick(actionId, item);
            }
        });
        mAdapter.setOnLoadMoreListener(new LoadMoreWrapperAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                nextPage();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mMultiStateView.setState(getDefaultState());
    }

    protected void refreshPage() {}

    protected void nextPage() {}

    protected void onItemClick(int actionId, T item) {}

    protected boolean getEnableRefresh() {
        return mMultiStateView.getState() == MultiStateView.STATE_CONTENT
                || mMultiStateView.getState() == MultiStateView.STATE_EMPTY;
    }

    @MultiStateView.State
    protected int getDefaultState() {
        return MultiStateView.STATE_LOADING;
    }

    protected abstract BaseAdapter<T> getAdapter();

    @DrawableRes
    protected int getEmptyIcon() {
        return R.drawable.ic_empty;
    }

    protected String getEmptyTitle() {
        return getString(R.string.label_empty_data);
    }

    protected boolean isDisplayError(ResponseError error) {
        return mMultiStateView.getState() != MultiStateView.STATE_CONTENT;
    }

    @DrawableRes
    protected int getErrorIcon(ResponseError error) {
        return R.drawable.ic_exception;
    }

    protected String getErrorTitle(ResponseError error) {
        return getString(R.string.label_error_network_is_bad);
    }

    protected String getErrorButton(ResponseError error) {
        return getString(R.string.label_click_button_to_retry);
    }

    protected void onRetryClick(ResponseError error) {
        mMultiStateView.setState(MultiStateView.STATE_LOADING);
        refreshPage();
    }


    //列表开始加载的时候
    @Override
    public void onStartRequest(int page) {
        if (page == 1) {
            if (mMultiStateView.getState() != MultiStateView.STATE_CONTENT) {
                mMultiStateView.setState(MultiStateView.STATE_LOADING);
            } else {
                mRefreshLayout.setRefreshing(true);
            }
        }
    }


    /**
     * 列表加载完成之后：
     * @param items
     * @param page
     * @param haveNextPage
     * 设置样式
     * 数据加载完成之后，列表类的数据在这里加载完成
     */
    @Override
    public void onFinishRequest(List<T> items, int page, boolean haveNextPage) {
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
        }
        if (mAdapter.isLoading()) {
            mAdapter.finishLoadMore();
        }

        if (items != null && !items.isEmpty()) {
            if (page == 1) {  //如果是第一页，直接设置上去
                mAdapter.setItems(items);  //数据设置上去
                mMultiStateView.setState(MultiStateView.STATE_CONTENT);
            } else { //如果是第二页，就增加数据
                mAdapter.addItems(items);
            }
        } else { //如果加载的数据为空
            if (page == 1) {  //如果第一页数据就为空，就显示数据为空
                mMultiStateView.setState(MultiStateView.STATE_EMPTY)
                        .setIcon(getEmptyIcon())
                        .setTitle(getEmptyTitle());
            } else {  //如果是加载更多的时候数据为空，就显示没有更多了
                ToastUtil.showToast(R.string.toast_error_not_have_more);
            }
        }

        mRefreshLayout.setEnabled(getEnableRefresh());
        mAdapter.setEnableLoadMore(haveNextPage);
    }

    @Override
    public void onResponseError(final ResponseError error) {
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
        }
        if (mAdapter.isLoading()) {
            mAdapter.finishLoadMore();
        }

        if (isDisplayError(error)) {
            mMultiStateView.setState(MultiStateView.STATE_ERROR)
                    .setIcon(getErrorIcon(error))
                    .setTitle(getErrorTitle(error))
                    .setButton(getErrorButton(error), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onRetryClick(error);
                        }
                    });
        } else {
            ToastUtil.showToast(error.getMessage());
        }

        mRefreshLayout.setEnabled(getEnableRefresh());
    }
}
