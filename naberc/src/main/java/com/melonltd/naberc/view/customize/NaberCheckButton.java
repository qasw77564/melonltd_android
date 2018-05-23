package com.melonltd.naberc.view.customize;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.common.base.Strings;
import com.melonltd.naberc.R;
import com.melonltd.naberc.util.Tools;


/**
 * 半形空白字元（\u0020）
 */
public class NaberCheckButton {
    private static final String TAG = NaberCheckButton.class.getSimpleName();
    private CheckBox box;
    private String title = "\u0020", price = "\u0020", symbol = "\u0020";
    private String text = "";

    public NaberCheckButton() {

    }

    private NaberCheckButton setParameter(CheckBox box, String title, String price, String symbol) {
        this.box = box;
        setTitleAndPriceAndSymbol(title, price, symbol);
        return this;
    }

    public static Builder Builder(Context context) {
        return new Builder(context);
    }

    private void setTitleAndPriceAndSymbol(String title, String price, String symbol) {
        this.title = Tools.MAKEUP.makeUpCharacter(title, 24, Tools.MakeUp.Direction.RIGHT);
        this.price = Tools.MAKEUP.makeUpCharacter(price, 10, Tools.MakeUp.Direction.LEFT);
        this.symbol = Tools.MAKEUP.makeUpCharacter(symbol, 2, Tools.MakeUp.Direction.LEFT);
        this.text = this.title + Tools.MAKEUP.makeUpCharacter("", 10, Tools.MakeUp.Direction.RIGHT) + this.price + this.symbol;
        this.box.setText(this.text);
    }

    private CheckBox getView() {
        return this.box;
    }

    public static class Builder {
        private CheckBox box;
        private NaberCheckButton naberCheckBox;
        private String title = "\u0020", price = "\u0020", symbol = "\u0020";

        Builder(Context context) {
            this.naberCheckBox = new NaberCheckButton();
            this.box = new CheckBox(context, null, R.attr.radioButtonStyle);
            this.box.setGravity(Gravity.START);
            this.box.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            this.box.setText("");
            this.box.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            this.box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            this.box.setPaddingRelative(50, 12, 0, 0);
        }

        public Builder setChecked(boolean checked) {
            this.box.setChecked(checked);
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setPrice(String price) {
            this.price = price;
            return this;
        }

        public Builder setId(int id) {
            this.box.setId(id);
            return this;
        }

        public Builder setTag(int id, Object o) {
            this.box.setTag(id, o);
            return this;
        }

        public Builder setPadding(int left, int top, int right, int bottom) {
            this.box.setPaddingRelative(left, top, right, bottom);
            return this;
        }

        public Builder setPadding(int padding) {
            setPadding(padding, padding, padding, padding);
            return this;
        }

        public Builder setTag(Object o) {
            this.box.setTag(o);
            return this;
        }

        public Builder setSymbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
            this.box.setOnCheckedChangeListener(listener);
            return this;
        }

        public CheckBox build() {
            return this.naberCheckBox.setParameter(this.box, this.title, this.price, this.symbol).getView();
        }
    }

}
