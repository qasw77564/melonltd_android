package com.melonltd.naber.view.seller.page;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melonltd.naber.R;
import com.melonltd.naber.view.common.BaseCore;
import com.melonltd.naber.view.factory.PageFragmentFactory;
import com.melonltd.naber.view.factory.PageType;
import com.melonltd.naber.view.seller.SellerMainActivity;


public class SellerOrderLogsDetailFragment extends Fragment {
    private static final String TAG = SellerOrderLogsDetailFragment.class.getSimpleName();
    public static SellerOrderLogsDetailFragment FRAGMENT = null;

    public SellerOrderLogsDetailFragment() {
    }

    public Fragment getInstance(Bundle bundle) {
        if (FRAGMENT == null) {
            FRAGMENT = new SellerOrderLogsDetailFragment();
        }
        FRAGMENT.setArguments(bundle);
        return FRAGMENT;
    }

    public Fragment newInstance(Object... o) {
        return new SellerOrderLogsDetailFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {// Inflate the layout for this fragment

        if (container.getTag(R.id.seller_order_logs_detail_page) == null) {
            View v = inflater.inflate(R.layout.fragment_seller_order_logs_detail, container, false);
            // getView
            container.setTag(R.id.seller_order_logs_detail_page, v);
            return v;
        }
        return (View) container.getTag(R.id.seller_order_logs_detail_page);
    }

    @Override
    public void onResume() {
        super.onResume();
        SellerMainActivity.changeTabAndToolbarStatus();
        if (SellerMainActivity.toolbar != null) {
            SellerMainActivity.navigationIconDisplay(true, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    backToSellerOrdersLogsPage();
                    SellerMainActivity.navigationIconDisplay(false, null);
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        SellerMainActivity.navigationIconDisplay(false, null);

    }

    private void backToSellerOrdersLogsPage(){
        BaseCore.FRAGMENT_TAG = PageType.SELLER_ORDERS_LOGS.name();
        SellerOrdersLogsFragment.TO_ORDERS_LOGS_DETAIL_INDEX = -1;
        Fragment f = PageFragmentFactory.of(PageType.SELLER_ORDERS_LOGS, null);
        getFragmentManager().beginTransaction().remove(this).replace(R.id.sellerFrameContainer, f).commit();
    }
}