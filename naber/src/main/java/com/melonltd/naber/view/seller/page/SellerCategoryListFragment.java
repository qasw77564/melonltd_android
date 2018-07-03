package com.melonltd.naber.view.seller.page;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.google.common.base.Strings;
import com.melonltd.naber.R;
import com.melonltd.naber.model.api.ApiManager;
import com.melonltd.naber.model.api.ThreadCallback;
import com.melonltd.naber.model.bean.Model;
import com.melonltd.naber.model.constant.NaberConstant;
import com.melonltd.naber.model.type.SwitchStatus;
import com.melonltd.naber.util.Tools;
import com.melonltd.naber.view.customize.SwitchButton;
import com.melonltd.naber.view.factory.PageType;
import com.melonltd.naber.view.seller.SellerMainActivity;
import com.melonltd.naber.view.seller.adapter.CategoryAdapter;
import com.melonltd.naber.vo.ReqData;
import com.melonltd.naber.vo.RestaurantCategoryRelVo;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

public class SellerCategoryListFragment extends Fragment {
    private static final String TAG = SellerCategoryListFragment.class.getSimpleName();
    public static SellerCategoryListFragment FRAGMENT = null;

    private EditText categoryEdit;
    private Button newCategoryBtn;
    private CategoryAdapter adapter;
    private Bundle bundle;

    public static int TO_CATEGORY_LIST_PAGE_INDEX = -1;

    public SellerCategoryListFragment() {
    }

    public Fragment getInstance(Bundle bundle) {
        if (FRAGMENT == null) {
            FRAGMENT = new SellerCategoryListFragment();
            TO_CATEGORY_LIST_PAGE_INDEX = -1;
        }
        if (bundle != null) {
            FRAGMENT.setArguments(bundle);
        }
        return FRAGMENT;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new CategoryAdapter(new SwitchListener(), new EditListener(), new DeleteListener());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (container.getTag(R.id.seller_category_list_main_page) == null) {
        View v = inflater.inflate(R.layout.fragment_seller_category_list, container, false);
        getViews(v);
//            container.setTag(R.id.seller_category_list_main_page, v);
        return v;
//        }
//        return (View) container.getTag(R.id.seller_category_list_main_page);
    }


    private void getViews(View v) {
        categoryEdit = v.findViewById(R.id.categoryEdit);
        newCategoryBtn = v.findViewById(R.id.newCategoryBtn);
        BGARefreshLayout refreshLayout = v.findViewById(R.id.categoryBGARefreshLayout);
        RecyclerView recyclerView = v.findViewById(R.id.categoryRecyclerView);

        BGANormalRefreshViewHolder refreshViewHolder = new BGANormalRefreshViewHolder(getContext(), true);
        refreshViewHolder.setPullDownRefreshText("Pull");
        refreshViewHolder.setRefreshingText("Pull to refresh");
        refreshViewHolder.setReleaseRefreshText("Pull to refresh");
        refreshViewHolder.setLoadingMoreText("Loading more !");
        refreshLayout.setRefreshViewHolder(refreshViewHolder);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        //setListener
        recyclerView.setAdapter(adapter);
        newCategoryBtn.setOnClickListener(new AddCategoryListener());

        refreshLayout.setDelegate(new BGARefreshLayout.BGARefreshLayoutDelegate() {
            @Override
            public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
                refreshLayout.endRefreshing();
                loadData();
            }

            @Override
            public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
                refreshLayout.endLoadingMore();
                return false;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        categoryEdit.setText("");
        SellerMainActivity.changeTabAndToolbarStatus();
        SellerMainActivity.lockDrawer(true);
        if (TO_CATEGORY_LIST_PAGE_INDEX >= 0) {
            SellerMainActivity.removeAndReplaceWhere(FRAGMENT, PageType.SELLER_FOOD_LIST, bundle);
        } else {
            loadData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void loadData() {
        Model.SELLER_CATEGORY_LIST.clear();
        adapter.notifyDataSetChanged();
        ApiManager.sellerCategoryList(new ThreadCallback(getContext()) {
            @Override
            public void onSuccess(String responseBody) {
                Log.i(TAG, responseBody);
                Model.SELLER_CATEGORY_LIST = Tools.JSONPARSE.fromJsonList(responseBody, RestaurantCategoryRelVo[].class);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(Exception error, String msg) {

            }
        });
    }

    class SwitchListener implements SwitchButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(final SwitchButton view, final boolean isChecked) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            final int index = (int) view.getTag();
            if (Model.SELLER_CATEGORY_LIST.get(index).status.getStatus() != isChecked) {
                ReqData req = new ReqData();
                req.uuid = Model.SELLER_CATEGORY_LIST.get(index).category_uuid;
                final SwitchStatus status = SwitchStatus.of(isChecked);
                req.status = status.getName();
                ApiManager.sellerChangeCategoryStatus(req, new ThreadCallback(getContext()) {
                    @Override
                    public void onSuccess(String responseBody) {
                        Model.SELLER_CATEGORY_LIST.get(index).status = status;
                    }

                    @Override
                    public void onFail(Exception error, String msg) {
                        view.setChecked(Model.SELLER_CATEGORY_LIST.get(index).status.getStatus());
                    }
                });
            }
        }
    }

    class EditListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int index = (int) view.getTag();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            TO_CATEGORY_LIST_PAGE_INDEX = index;
            bundle = new Bundle();
            bundle.putString(NaberConstant.SELLER_CATEGORY_NAME, Model.SELLER_CATEGORY_LIST.get(index).category_name);
            bundle.putString(NaberConstant.SELLER_CATEGORY_UUID, Model.SELLER_CATEGORY_LIST.get(index).category_uuid);
            SellerMainActivity.removeAndReplaceWhere(FRAGMENT, PageType.SELLER_FOOD_LIST, bundle);
        }
    }

    class DeleteListener implements View.OnClickListener {
        @Override
        public void onClick(final View view) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            new AlertView.Builder()
                    .setTitle("")
                    .setMessage("請注意刪除種類\n，將會影響種類下的產品!")
                    .setContext(getContext())
                    .setStyle(AlertView.Style.Alert)
                    .setOthers(new String[]{"確定刪除", "取消"})
                    .setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (position == 0) {
                                int index = (int) view.getTag();
                                new Handler().postDelayed(new DeleteRun(index), 300);
                            }
                        }
                    })
                    .build()
                    .setCancelable(true)
                    .show();
        }
    }


    class DeleteRun implements Runnable {
        private int index;

        DeleteRun(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            ReqData req = new ReqData();
            req.uuid = Model.SELLER_CATEGORY_LIST.get(index).category_uuid;
            ApiManager.sellerDeleteCategory(req, new ThreadCallback(getContext()) {
                @Override
                public void onSuccess(String responseBody) {
                    Model.SELLER_CATEGORY_LIST.remove(index);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFail(Exception error, String msg) {
                }
            });
        }
    }

    class AddCategoryListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (Strings.isNullOrEmpty(categoryEdit.getText().toString())) {
                new AlertView.Builder()
                        .setTitle("")
                        .setMessage("請輸入種類名稱")
                        .setContext(getContext())
                        .setStyle(AlertView.Style.Alert)
                        .setOthers(new String[]{"取消"})
                        .build()
                        .setCancelable(true)
                        .show();
            } else {
                ReqData req = new ReqData();
                req.name = categoryEdit.getText().toString();
                ApiManager.sellerAddCategory(req, new ThreadCallback(getContext()) {
                    @Override
                    public void onSuccess(String responseBody) {
                        RestaurantCategoryRelVo categoryRel = Tools.JSONPARSE.fromJson(responseBody, RestaurantCategoryRelVo.class);
                        Model.SELLER_CATEGORY_LIST.add(0, categoryRel);
                        categoryEdit.setText("");

                        TO_CATEGORY_LIST_PAGE_INDEX = 0;
                        bundle = new Bundle();
                        bundle.putString(NaberConstant.SELLER_CATEGORY_NAME, categoryRel.category_name);
                        bundle.putString(NaberConstant.SELLER_CATEGORY_UUID, categoryRel.category_uuid);
                        SellerMainActivity.removeAndReplaceWhere(FRAGMENT, PageType.SELLER_FOOD_LIST, bundle);
                    }

                    @Override
                    public void onFail(Exception error, String msg) {
                        Log.i(TAG, msg);
                    }
                });
            }
        }
    }

}
