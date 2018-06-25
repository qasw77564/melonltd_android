package com.melonltd.naber.vo;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

public class CategoryFoodRelVo implements Serializable{
    private static final long serialVersionUID = 3254163581453552772L;

    public String food_uuid;
    public String category_uuid;
    public String food_name;
    public String default_price;
    public String photo;
    public String photo_type;
    public FoodItemVo food_data;

    @Override
    public String toString() {
        return MoreObjects
                .toStringHelper(this.getClass())
                .add("food_uuid",food_uuid)
                .add("category_uuid",category_uuid)
                .add("food_name",food_name)
                .add("default_price",default_price)
                .add("photo",photo)
                .add("photo_type",photo_type)
                .add("food_data",food_data)
                .toString();
    }
}