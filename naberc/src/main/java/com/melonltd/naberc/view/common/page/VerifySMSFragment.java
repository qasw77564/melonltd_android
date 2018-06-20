package com.melonltd.naberc.view.common.page;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnDismissListener;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.melonltd.naberc.R;
import com.melonltd.naberc.model.api.ThreadCallback;
import com.melonltd.naberc.model.api.ApiManager;
import com.melonltd.naberc.util.Tools;
import com.melonltd.naberc.util.VerifyUtil;
import com.melonltd.naberc.view.common.BaseActivity;
import com.melonltd.naberc.view.common.BaseCore;
import com.melonltd.naberc.view.factory.PageFragmentFactory;
import com.melonltd.naberc.view.factory.PageType;
import com.melonltd.naberc.vo.AccountInfoVo;
import com.melonltd.naberc.vo.ReqData;

import java.util.Map;


public class VerifySMSFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = VerifySMSFragment.class.getSimpleName();
    public static VerifySMSFragment FRAGMENT = null;
    private EditText phoneNamberEdit, verifySMSEdit;
    private CheckBox readCheckBox;

    private Map<String, String> map = Maps.newHashMap();

    public VerifySMSFragment() {
    }

    public VerifySMSFragment getInstance(Bundle bundle) {
        if (FRAGMENT == null) {
            FRAGMENT = new VerifySMSFragment();
        }
        if (bundle != null){
            FRAGMENT.setArguments(bundle);
        }
        return FRAGMENT;
    }

    public VerifySMSFragment newInstance(Object... o) {
        return new VerifySMSFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container.getTag(R.id.user_verify_sms_page) == null) {
            View v = inflater.inflate(R.layout.fragment_verify_sms, container, false);
            getView(v);
            container.setTag(R.id.user_verify_sms_page, v);
        }
        return (View) container.getTag(R.id.user_verify_sms_page);
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                BaseCore.loadJsonData(getContext());
            }
        });
        map = Maps.newHashMap();
        phoneNamberEdit.setText("");
        verifySMSEdit.setText("");
        verifySMSEdit.setEnabled(false);
        readCheckBox.setChecked(false);
        BaseActivity.changeToolbarStatus();
        if (BaseActivity.toolbar != null) {
            BaseActivity.navigationIconDisplay(true, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    backToLoginPage();
                    BaseActivity.navigationIconDisplay(false, null);
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        BaseActivity.navigationIconDisplay(false, null);
    }

    private void getView(View v) {
        Button requestVerifyCodeBtn = v.findViewById(R.id.requestVerifyCodeBtn);
        Button submitToRegisteredBun = v.findViewById(R.id.submitToRegisteredBun);
        TextView privacyPolicyText = v.findViewById(R.id.privacyPolicyText);
        phoneNamberEdit = v.findViewById(R.id.phoneNamberEdit);
        readCheckBox = v.findViewById(R.id.readCheckBox);
        verifySMSEdit = v.findViewById(R.id.verifySMSEdit);

        // setListener
        requestVerifyCodeBtn.setOnClickListener(this);
        submitToRegisteredBun.setOnClickListener(this);
        privacyPolicyText.setOnClickListener(this);
    }


    private void backToLoginPage() {
        BaseCore.FRAGMENT_TAG = PageType.LOGIN.name();
        Fragment f = PageFragmentFactory.of(PageType.LOGIN, null);
        getFragmentManager().beginTransaction().remove(this).replace(R.id.baseContainer, f).commit();
    }

    @Override
    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(phoneNamberEdit.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(verifySMSEdit.getWindowToken(), 0);
        switch (v.getId()) {
            case R.id.requestVerifyCodeBtn:

                if (!readCheckBox.isChecked()){
                    new AlertView.Builder()
                            .setContext(getContext())
                            .setStyle(AlertView.Style.Alert)
                            .setTitle("")
                            .setMessage("請先閱讀隱私權政策條款，再進行註冊！")
                            .setCancelText("確定")
                            .build()
                            .setCancelable(true)
                            .show();
                    break;
                }
                if (!VerifyUtil.phoneNumber(phoneNamberEdit.getText().toString())){
                    new AlertView.Builder()
                            .setContext(getContext())
                            .setStyle(AlertView.Style.Alert)
                            .setTitle("")
                            .setMessage("請輸入正確手機號碼，避免無法正常接收SMS")
                            .setCancelText("關閉")
                            .build()
                            .setCancelable(true)
                            .show();
                }else {
                    map.put("phone", phoneNamberEdit.getText().toString());
                    ApiManager.getSMSCode(map, new ThreadCallback(getContext()) {
                        @Override
                        public void onSuccess(String responseBody) {
                            Map<String, String> response = Tools.JSONPARSE.fromJson(responseBody, Map.class);
                            map.put("batch_id", response.get("batch_id"));
                            verifySMSEdit.setEnabled(true);
                        }
                        @Override
                        public void onFail(Exception error, String msg) {
                            Log.d(TAG, msg);
                            new AlertView.Builder()
                                    .setContext(getContext())
                                    .setStyle(AlertView.Style.Alert)
                                    .setTitle("")
                                    .setMessage("今天求SMS密碼次數已經用盡，請明天再嘗試！！")
                                    .setCancelText("關閉")
                                    .build()
                                    .setCancelable(true)
                                    .show();
                        }
                    });
                }
                break;
            case R.id.privacyPolicyText:
                View view = getLayoutInflater().inflate(R.layout.privacy_policy_content, null);
                new AlertView.Builder()
                        .setContext(getContext())
                        .setStyle(AlertView.Style.Alert)
                        .setTitle("NABER 隱私權政策")
                        .setCancelText("確定")
                        .build()
                        .addExtView(view)
                        .setCancelable(false)
                        .setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss(Object o) {
                                readCheckBox.setChecked(true);
                            }
                        })
                        .show();
                break;
            case R.id.submitToRegisteredBun:
                if (Strings.isNullOrEmpty(map.get("batch_id")) || Strings.isNullOrEmpty(phoneNamberEdit.getText().toString()) || Strings.isNullOrEmpty(verifySMSEdit.getText().toString())){
                    new AlertView.Builder()
                            .setContext(getContext())
                            .setStyle(AlertView.Style.Alert)
                            .setTitle("")
                            .setMessage("請取得驗證碼後再送出驗證!")
                            .setCancelText("關閉")
                            .build()
                            .setCancelable(true)
                            .show();
                    break;
                }
                map.put("phone", phoneNamberEdit.getText().toString());
                map.put("code", verifySMSEdit.getText().toString());
                ApiManager.verifySMSCode(map, new ThreadCallback(getContext()) {
                    @Override
                    public void onSuccess(String responseBody) {
                        toRegisteredPage();
                    }

                    @Override
                    public void onFail(Exception error, String msg) {
                        Log.d(TAG, msg);
                        new AlertView.Builder()
                                .setContext(getContext())
                                .setStyle(AlertView.Style.Alert)
                                .setTitle("")
                                .setMessage(msg)
                                .setCancelText("關閉")
                                .build()
                                .setCancelable(true)
                                .show();
                    }
                });
                break;
        }
    }

    private void toRegisteredPage() {
        BaseCore.FRAGMENT_TAG = PageType.REGISTERED_USER.name();
        Bundle bundle = new Bundle();
        bundle.putString("phone", phoneNamberEdit.getText().toString());
        getFragmentManager().beginTransaction().remove(this).replace(R.id.baseContainer, PageFragmentFactory.of(PageType.REGISTERED_USER, bundle)).commit();
    }
}
