package com.melonltd.naberc.view.user.page.impl;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.melonltd.naberc.R;
import com.melonltd.naberc.model.helper.okhttp.ApiCallback;
import com.melonltd.naberc.model.helper.okhttp.ApiManager;
import com.melonltd.naberc.view.common.BaseCore;
import com.melonltd.naberc.view.common.abs.AbsPageFragment;
import com.melonltd.naberc.view.common.factory.PageFragmentFactory;
import com.melonltd.naberc.view.common.type.PageType;
import com.melonltd.naberc.view.customize.OnLoadLayout;
import com.melonltd.naberc.view.user.UserMainActivity;

import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class RestaurantDetailFragment extends AbsPageFragment {
    private static final String TAG = RestaurantDetailFragment.class.getSimpleName();
    private static RestaurantDetailFragment FRAGMENT = null;
    private ImageView restaurantBackgroundImage, restaurantIcon;
    private TextView restaurantBulletinText, restaurantNameText, businessTimeText, addressText, distanceText;
    private OnLoadLayout categoryOnLoadLayout;
    private CategoryAdapter adapter;
    private ListView categoryListView;

    private List<String> categoryList = Lists.newArrayList();

    public RestaurantDetailFragment() {
    }

    @Override
    public AbsPageFragment newInstance(Object... o) {
        return new RestaurantDetailFragment();
    }

    @Override
    public AbsPageFragment getInstance(Bundle bundle) {
        if (FRAGMENT == null) {
            FRAGMENT = new RestaurantDetailFragment();
        }
        FRAGMENT.setArguments(null);
        FRAGMENT.setArguments(bundle);
        return FRAGMENT;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new CategoryAdapter(getContext(), categoryList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container.getTag(R.id.user_restaurant_detail_page) == null) {
            View v = inflater.inflate(R.layout.fragment_restaurant_detail, container, false);
            getViews(v);
            setListener();
            container.setTag(R.id.user_restaurant_detail_page, v);
            return v;
        }
        return (View) container.getTag(R.id.user_restaurant_detail_page);
    }

    private void getViews(View v) {
        restaurantBackgroundImage = v.findViewById(R.id.restaurantDetailImage);
        restaurantBulletinText = v.findViewById(R.id.restaurantDetailBulletinText);
        categoryOnLoadLayout = v.findViewById(R.id.restaurantDetailOnLoadLayout);
        categoryListView = v.findViewById(R.id.restaurantDetailCategoryListView);
        categoryListView.setAdapter(adapter);
        // find include views
        restaurantIcon = v.findViewById(R.id.restaurantImageView);
        restaurantIcon.setBackground(null);
        restaurantNameText = v.findViewById(R.id.restaurantNameText);
        businessTimeText = v.findViewById(R.id.businessTimeText);
        addressText = v.findViewById(R.id.addressText);
        distanceText = v.findViewById(R.id.distanceText);
    }

    private void setListener() {
        categoryOnLoadLayout.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                doLoadData(true);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return ((OnLoadLayout) frame).isTop();
            }
        });

        categoryOnLoadLayout.setOnLoadListener(new OnLoadLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                doLoadData(false);
            }
        });

        categoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (categoryList.size() == 0){
            doLoadData(true);
        }

        if (UserMainActivity.toolbar != null) {
            UserMainActivity.navigationIconDisplay(true, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    backToRegisteredPage();
                    UserMainActivity.navigationIconDisplay(false, null);
                }
            });
        }
    }

    private void doLoadData(boolean isRefresh) {
        if (isRefresh) {
            categoryList.clear();
        }
        ApiManager.test(new ApiCallback(getActivity()) {
            @Override
            public void onSuccess(String responseBody) {
                for (int i = 0; i < 5; i++) {
                    categoryList.add("" + i);
                }
                adapter.notifyDataSetChanged();
                categoryOnLoadLayout.refreshComplete();
            }

            @Override
            public void onFail(Exception error) {

            }
        });
    }

    private void backToRegisteredPage() {
        BaseCore.FRAGMENT_TAG = PageType.RESTAURANT.name();
        RestaurantFragment.TO_RESTAURANT_DETAIL_INDEX = -1;
        AbsPageFragment f = PageFragmentFactory.of(PageType.RESTAURANT, null);
        getFragmentManager().beginTransaction().remove(this).replace(R.id.frameContainer, f).commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        UserMainActivity.navigationIconDisplay(false, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    class CategoryAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<String> list;

        public CategoryAdapter(Context context, List list) {
            this.inflater = LayoutInflater.from(context);
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView tv = (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, null);
            tv.setText(list.get(i));
            return tv;
        }
    }
}
