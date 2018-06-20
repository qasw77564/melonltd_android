package com.melonltd.naberc.view.common.page;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bigkoo.pickerview.view.TimePickerView;
import com.google.common.base.Strings;
import com.melonltd.naberc.R;
import com.melonltd.naberc.model.api.ApiManager;
import com.melonltd.naberc.model.api.ThreadCallback;
import com.melonltd.naberc.model.bean.Model;
import com.melonltd.naberc.model.type.Identity;
import com.melonltd.naberc.util.VerifyUtil;
import com.melonltd.naberc.view.common.BaseActivity;
import com.melonltd.naberc.view.factory.PageFragmentFactory;
import com.melonltd.naberc.view.factory.PageType;
import com.melonltd.naberc.vo.AccountInfoVo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RegisteredFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = RegisteredFragment.class.getSimpleName();
    public static RegisteredFragment FRAGMENT = null;

    private AccountInfoVo account = new AccountInfoVo();
    private TextView identityText, birthdayText;
    private EditText nameEditText, addressEditText, emailEditText, passwordEditText, confirmPasswordEditText;

    public RegisteredFragment() {
    }

    public Fragment newInstance(Object... o) {
        return new RegisteredFragment();
    }

    public Fragment getInstance(Bundle bundle) {
        if (FRAGMENT == null) {
            FRAGMENT = new RegisteredFragment();
        }
        if (bundle != null) {
            FRAGMENT.setArguments(bundle);
        }
        return FRAGMENT;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container.getTag(R.id.user_registered_page) == null) {
            View v = inflater.inflate(R.layout.fragment_registered, container, false);
            getViews(v);
            container.setTag(R.id.user_registered_page, v);
            return v;
        }
        return (View) container.getTag(R.id.user_registered_page);
    }

    private void getViews(View v) {
        identityText = v.findViewById(R.id.identityEditText);
        birthdayText = v.findViewById(R.id.birthdayEditText);
        nameEditText = v.findViewById(R.id.nameEditText);
        addressEditText = v.findViewById(R.id.addressEditText);
        emailEditText = v.findViewById(R.id.emailEditText);
        passwordEditText = v.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = v.findViewById(R.id.confirmPasswordEditText);

        Button submitBtn = v.findViewById(R.id.submit);
        Button backToLoginBtn = v.findViewById(R.id.backToLoginBtn);

        // setListener
        HideKeyboard hideKeyboard = new HideKeyboard();
        identityText.setOnFocusChangeListener(hideKeyboard);
        birthdayText.setOnFocusChangeListener(hideKeyboard);
        nameEditText.setOnFocusChangeListener(hideKeyboard);
        addressEditText.setOnFocusChangeListener(hideKeyboard);
        emailEditText.setOnFocusChangeListener(hideKeyboard);
        passwordEditText.setOnFocusChangeListener(hideKeyboard);
        confirmPasswordEditText.setOnFocusChangeListener(hideKeyboard);

        submitBtn.setOnClickListener(this);
        backToLoginBtn.setOnClickListener(this);
        v.setOnClickListener(this);
        identityText.setOnClickListener(new IdentityClick());
        birthdayText.setOnClickListener(new BirthdayClick());

    }

    @Override
    public void onResume() {
        super.onResume();
        removeAllData();
        BaseActivity.changeToolbarStatus();
    }

    private void backToLoginPage() {
        Fragment fragment = PageFragmentFactory.of(PageType.LOGIN, null);
        getFragmentManager().beginTransaction().remove(this).replace(R.id.baseContainer, fragment).addToBackStack(fragment.toString()).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                if (verifyInput()) {
                    account.password = passwordEditText.getText().toString();
                    account.name = nameEditText.getText().toString();
                    account.email = emailEditText.getText().toString();
                    account.phone = getArguments().getString("phone");
                    account.address = addressEditText.getText().toString();
                    account.level = "USER";
                    ApiManager.userRegistered(account, new ThreadCallback(getContext()) {
                        @Override
                        public void onSuccess(String responseBody) {
                            backToLoginPage();
                        }
                        @Override
                        public void onFail(Exception error, String msg) {
                            // TODO
                        }
                    });
                }
                break;
            case R.id.backToLoginBtn:
                backToLoginPage();
                break;
        }
    }
    private void removeAllData(){
        account = new AccountInfoVo();
        identityText.setText("");
        birthdayText.setText("");
        nameEditText.setText("");
        addressEditText.setText("");
        emailEditText.setText("");
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
    }

    class IdentityClick implements View.OnClickListener {
        @Override
        public void onClick(final View view) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            OptionsPickerView pvOptions = new OptionsPickerBuilder(getContext(), new OnOptionsSelectListener() {
                @Override
                public void onOptionsSelect(int options1, int option2, int options3, View v) {
                    account.identity = Identity.of(Model.OPT_ITEM_1.get(options1)).name();
                    account.school_name = Model.OPT_ITEM_2.get(options1).get(option2);
                    identityText.setText(Model.OPT_ITEM_1.get(options1) + " " + Model.OPT_ITEM_2.get(options1).get(option2));
                }
            }).setTitleSize(20)
                    .setTitleBgColor(getResources().getColor(R.color.naber_dividing_line_gray))
                    .setCancelColor(getResources().getColor(R.color.naber_dividing_gray))
                    .setSubmitColor(getResources().getColor(R.color.naber_dividing_gray))
                    .build();
            pvOptions.setPicker(Model.OPT_ITEM_1, Model.OPT_ITEM_2);
            pvOptions.show();
        }
    }

    class BirthdayClick implements View.OnClickListener {
        @Override
        public void onClick(final View view) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            Calendar selectedDate = Calendar.getInstance();
            Calendar startDate = Calendar.getInstance();
            startDate.set(1948, 1, 1);
            Calendar endDate = Calendar.getInstance();
            TimePickerView tp = new TimePickerBuilder(getContext(), new OnTimeSelectListener() {
                @Override
                public void onTimeSelect(Date date, View v) {
                    account.birth_day = new SimpleDateFormat("yyyy-MM-dd").format(date);
                    identityText.setText(account.birth_day);
                }
            }).setType(new boolean[]{true, true, true, false, false, false})//"year","month","day","hours","minutes","seconds "
                    .setTitleSize(20)
                    .setOutSideCancelable(true)//点击屏幕，点在控件外部范围时，是否取消显示
                    .isCyclic(false)//是否循环滚动
                    .setTitleBgColor(getResources().getColor(R.color.naber_dividing_line_gray))
                    .setCancelColor(getResources().getColor(R.color.naber_dividing_gray))
                    .setSubmitColor(getResources().getColor(R.color.naber_dividing_gray))
                    .setDate(selectedDate)// 如果不设置的话，默认是系统时间*/
                    .setRangDate(startDate, endDate)//起始终止年月日设定
                    .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
                    .isDialog(false)//是否显示为对话框样式
                    .build();

            tp.show();
        }
    }

    class HideKeyboard implements View.OnFocusChangeListener{
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus){
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
    private boolean verifyInput() {
        boolean result = true;
        String message = "";
        // 驗證身份不為空
        if (Strings.isNullOrEmpty(identityText.getText().toString())){
            message = "驗證身份不為空";
            result = false;
        }
        // 驗證姓名不為空
        if (Strings.isNullOrEmpty(nameEditText.getText().toString())) {
            message = "驗證姓名不為空";
            result = false;
        }
        // 驗證姓名長度大於二
        if (nameEditText.getText().toString().length() <= 4) {
            message = "驗證姓名長度大於二";
            result = false;
        }
        // 驗證Email不為空
        if (Strings.isNullOrEmpty(emailEditText.getText().toString())) {
            message = "驗證Email不為空";
            result = false;
        }
        // 驗證Email錯誤格式
        if (!VerifyUtil.email(emailEditText.getText().toString())) {
            message = "驗證Email錯誤格式";
            result = false;
        }
        // 驗證密碼不為空 並需要英文大小寫數字 6 ~ 20
        if (!VerifyUtil.password(passwordEditText.getText().toString())) {
            message = "驗證密碼不為空 並需要英文大小寫數字 6 ~ 20";
            result = false;
        }
        // 驗證密碼與確認密碼一致
        if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
            message = "驗證密碼與確認密碼一致";
            result = false;
        }
        // 驗證生日不為空
        if (Strings.isNullOrEmpty(birthdayText.getText().toString())) {
            message = "驗證生日不為空";
            result = false;
        }
        if (!result) {
            new AlertView.Builder()
                    .setTitle("")
                    .setMessage(message)
                    .setContext(getContext())
                    .setStyle(AlertView.Style.Alert)
                    .setCancelText("取消")
                    .build()
                    .setCancelable(true)
                    .show();
        }

        return result;
    }
}

