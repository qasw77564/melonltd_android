package com.melonltd.naberc.view.seller.page.impl;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melonltd.naberc.R;
import com.melonltd.naberc.view.common.BaseCore;
import com.melonltd.naberc.view.common.abs.AbsPageFragment;
import com.melonltd.naberc.view.common.factory.PageFragmentFactory;
import com.melonltd.naberc.view.common.type.PageType;

public class SellerSetUpFragment extends AbsPageFragment implements View.OnClickListener {
    private static final String TAG = SellerSetUpFragment.class.getSimpleName();
    private static SellerSetUpFragment FRAGMENT = null;

    private TextView toSellerEdit;
    public static int TO_SELLER_DETAIL_INDEX = -1;

    public SellerSetUpFragment() {
        // Required empty public constructor
    }

    @Override
    public AbsPageFragment getInstance(Bundle bundle) {
        if (FRAGMENT == null) {
            FRAGMENT = new SellerSetUpFragment();
        }
        FRAGMENT.setArguments(bundle);
        return FRAGMENT;
    }

    @Override
    public AbsPageFragment newInstance(Object... o) {
        return new SellerSetUpFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container.getTag(R.id.seller_set_up_page) == null) {
            View v = inflater.inflate(R.layout.fragment_seller_set_up, container, false);
            getViews(v);
            setListener();
            container.setTag(R.id.seller_set_up_page, v);
            return v;
        }
        return (View) container.getTag(R.id.seller_set_up_page);
    }

    private void getViews(View v) {
        toSellerEdit = v.findViewById(R.id.toSellerEdit);
    }

    private void setListener() {
        toSellerEdit.setOnClickListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (TO_SELLER_DETAIL_INDEX > 0){
            toSellerDetail(TO_SELLER_DETAIL_INDEX);
        }
    }

    private void toSellerDetail(int i) {
        TO_SELLER_DETAIL_INDEX = i;
        BaseCore.FRAGMENT_TAG = PageType.SELLER_DETAIL.name();
        AbsPageFragment f = PageFragmentFactory.of(PageType.SELLER_DETAIL, null);
        getFragmentManager().beginTransaction().replace(R.id.sellerFrameContainer, f).commit();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.toSellerEdit:
                toSellerDetail(1);
                break;
        }

    }
}
