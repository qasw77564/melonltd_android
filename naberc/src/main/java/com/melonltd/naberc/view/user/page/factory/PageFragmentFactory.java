package com.melonltd.naberc.view.user.page.factory;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.melonltd.naberc.view.user.page.abs.AbsPageFragment;
import com.melonltd.naberc.view.user.page.impl.HistoryFragment;
import com.melonltd.naberc.view.user.page.impl.HomeFragment;
import com.melonltd.naberc.view.user.page.impl.LoginFragment;
import com.melonltd.naberc.view.user.page.impl.RegisteredFragment;
import com.melonltd.naberc.view.user.page.impl.RestaurantFragment;
import com.melonltd.naberc.view.user.page.impl.SetUpFragment;
import com.melonltd.naberc.view.user.page.impl.ShoppingCartFragment;
import com.melonltd.naberc.view.user.page.impl.VerifySMSFragment;
import com.melonltd.naberc.view.user.page.type.PageType;

public class PageFragmentFactory {
    private final static String TAG = PageFragmentFactory.class.getSimpleName();

    public static AbsPageFragment of(@NonNull PageType pageType, Bundle bundle) {

        switch (pageType) {
            case HOME:
                return new HomeFragment().getInstance(bundle);
            case LOGIN:
                return new LoginFragment().getInstance(bundle);
            case SET_UP:
                return new SetUpFragment().getInstance(bundle);
            case HISTORY:
                return new HistoryFragment().getInstance(bundle);
            case REGISTERED:
                return new RegisteredFragment().getInstance(bundle);
            case RESTAURANT:
                return new RestaurantFragment().getInstance(bundle);
            case VERIFY_SMS:
                return new VerifySMSFragment().getInstance(bundle);
            case SHOPPING_CART:
                return new ShoppingCartFragment().getInstance(bundle);
            default:
                return new HomeFragment().getInstance(bundle);
        }
//        try{
//            Class<? extends AbsPageFragment> zlass = pageType.toClass();
//            AbsPageFragment fragment = (AbsPageFragment) zlass.newInstance().getInstance(bundle);
//            Log.d(TAG, zlass.getName());
//            return fragment;
//        }catch (IllegalAccessException | InstantiationException e){
//            Log.e(TAG, e.getMessage());
//            return new HomeFragment().getInstance(bundle);
//        }

    }
}