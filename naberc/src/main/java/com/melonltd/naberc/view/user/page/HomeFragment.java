package com.melonltd.naberc.view.user.page;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.melonltd.naberc.R;
import com.melonltd.naberc.model.api.ThreadCallback;
import com.melonltd.naberc.model.api.ApiManager;
import com.melonltd.naberc.model.bean.Model;
import com.melonltd.naberc.model.constant.NaberConstant;
import com.melonltd.naberc.model.service.SPService;
import com.melonltd.naberc.util.Tools;
import com.melonltd.naberc.view.common.BaseCore;
import com.melonltd.naberc.view.factory.PageFragmentFactory;
import com.melonltd.naberc.view.factory.PageType;
import com.melonltd.naberc.view.intro.IntroActivity;
import com.melonltd.naberc.view.user.UserMainActivity;
import com.melonltd.naberc.view.user.adapter.RestaurantAdapter;
import com.melonltd.naberc.vo.AdvertisementVo;
import com.melonltd.naberc.vo.BulletinVo;
import com.melonltd.naberc.vo.ReqData;
import com.melonltd.naberc.vo.RestaurantInfoVo;

import java.util.Iterator;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;
import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;


public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
    public static HomeFragment FRAGMENT = null;

    private RestaurantAdapter adapter;

    public HomeFragment() {
    }

    public Fragment getInstance(Bundle bundle) {
        if (FRAGMENT == null) {
            FRAGMENT = new HomeFragment();
            FRAGMENT.setArguments(bundle);
        }
        return FRAGMENT;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(getContext());
        adapter = new RestaurantAdapter(Model.RESTAURANT_INFO_LIST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container.getTag(R.id.user_home_page) == null) {
            View v = inflater.inflate(R.layout.fragment_home, container, false);
            getViews(v);
            container.setTag(R.id.user_home_page, v);
            return v;
        }
        return (View) container.getTag(R.id.user_home_page);
    }

    private void getViews(View v) {
        final BGABanner banner = v.findViewById(R.id.homeBanner);
        final TextView bulletinText = v.findViewById(R.id.bulletinText);

        final BGARefreshLayout bgaRefreshLayout = v.findViewById(R.id.top30BGARefreshLayout);
        RecyclerView recyclerView = v.findViewById(R.id.top30RecyclerView);
        BGANormalRefreshViewHolder refreshViewHolder = new BGANormalRefreshViewHolder(getContext(), true);
        refreshViewHolder.setPullDownRefreshText("Pull");
        refreshViewHolder.setRefreshingText("Pull to refresh");
        refreshViewHolder.setReleaseRefreshText("Pull to refresh");
        refreshViewHolder.setLoadingMoreText("Loading more !");

        bgaRefreshLayout.setRefreshViewHolder(refreshViewHolder);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        //setListener
        banner.setAdapter(new BGABanner.Adapter<CardView, String>() {
            @Override
            public void fillBannerItem(BGABanner banner, CardView itemView, String model, int position) {
                SimpleDraweeView simpleDraweeView = itemView.findViewById(R.id.sdv_item_fresco_content);
                simpleDraweeView.setImageURI(Uri.parse(model));
            }
        });

        // 輪播圖
        ApiManager.advertisement(new ThreadCallback(getContext()) {
            @Override
            public void onSuccess(String responseBody) {
                List<AdvertisementVo> vos = Tools.JSONPARSE.fromJsonList(responseBody, AdvertisementVo[].class);
                Model.BANNER_IMAGES = Lists.<String>newArrayList();
                for (int i = 0; i < vos.size(); i++) {
                    Model.BANNER_IMAGES.add(vos.get(i).photo);
                }
                banner.setData(R.layout.item_fresco, Model.BANNER_IMAGES, null);
            }

            @Override
            public void onFail(Exception error, String msg) {

            }
        });

        // 取得全部公告
        ApiManager.bulletin(new ThreadCallback(getContext()) {
            @Override
            public void onSuccess(String responseBody) {
                List<BulletinVo> list = Tools.JSONPARSE.fromJsonList(responseBody, BulletinVo[].class);
                for (int i = 0; i < list.size(); i++) {
                    Iterator<String> iterator =  Splitter.on("$split").split(list.get(i).content_text).iterator();
                    String content_text = "";
                    while (iterator.hasNext()){
                        content_text += iterator.next() + "\n";
                    }
                    Model.BULLETIN_VOS.put(list.get(i).bulletin_category,content_text);
                }
                bulletinText.setText(Model.BULLETIN_VOS.get("HOME"));
            }

            @Override
            public void onFail(Exception error, String msg) {
                bulletinText.setText("");
            }
        });

        recyclerView.setAdapter(adapter);
        adapter.setItemOnClickListener(new ItemOnClickListener());

        bgaRefreshLayout.setDelegate(new BGARefreshLayout.BGARefreshLayoutDelegate() {
            @Override
            public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
                bgaRefreshLayout.endRefreshing();
                doLoadData(true);
            }

            @Override
            public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
                return false;
            }
        });
    }

    private void doLoadData(boolean isRefresh) {
        if (isRefresh) {
            RestaurantFragment.TO_RESTAURANT_DETAIL_INDEX = -1;
            Model.RESTAURANT_INFO_LIST.clear();
        }
        ReqData req = new ReqData();
        req.search_type="TOP";
        ApiManager.restaurantList(req, new ThreadCallback(getContext()){
            @Override
            public void onSuccess(String responseBody) {
                List<RestaurantInfoVo> vos = Tools.JSONPARSE.fromJsonList(responseBody, RestaurantInfoVo[].class);
                Model.RESTAURANT_INFO_LIST.addAll(vos);
                adapter.setLocation(Model.LOCATION);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFail(Exception error, String msg) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        UserMainActivity.changeTabAndToolbarStatus();
        boolean isFirst = SPService.getIsFirstLogin();
        if (isFirst) {
            startActivity(new Intent(getActivity().getBaseContext(), IntroActivity.class));
        } else {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), BaseCore.LOCATION, BaseCore.LOCATION_CODE);
            } else {
                if (Model.RESTAURANT_INFO_LIST.size() == 0) {
                    doLoadData(true);
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class ItemOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int index = (int)view.getTag();
            Bundle bundle = new Bundle();
            bundle.putSerializable(NaberConstant.RESTAURANT_INFO, Model.RESTAURANT_INFO_LIST.get(index));
            RestaurantFragment.TO_RESTAURANT_DETAIL_INDEX = index;
            RestaurantDetailFragment.TO_CATEGORY_MENU_INDEX = -1;
            BaseCore.FRAGMENT_TAG = PageType.RESTAURANT_DETAIL.name();
            Fragment f = PageFragmentFactory.of(PageType.RESTAURANT_DETAIL, bundle);
            getFragmentManager().beginTransaction().replace(R.id.frameContainer, f).addToBackStack(f.toString()).commit();

        }
    }
}
