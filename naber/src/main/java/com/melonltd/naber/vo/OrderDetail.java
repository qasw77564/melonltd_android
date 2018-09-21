package com.melonltd.naber.vo;

import com.google.common.base.MoreObjects;
import com.melonltd.naber.model.type.BillingType;
import com.melonltd.naber.model.type.Delivery;

import java.io.Serializable;
import java.util.List;

public class OrderDetail implements Serializable {

    private static final long serialVersionUID = -5397846887657210105L;
    public String restaurant_uuid;
    public String restaurant_name = "";
    public String restaurant_address;
    public String can_discount;
    public String user_name;
    public String user_phone;
    public String fetch_date;
    public String user_message;
    public String use_bonus;
    public OrderType order_type;
    public List<OrderData> orders;

    public static OrderDetail ofOrders(List<OrderData> orders) {
        OrderDetail data = new OrderDetail();
        data.orders = orders;
        return data;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("restaurant_uuid", restaurant_uuid)
                .add("restaurant_name", restaurant_name)
                .add("restaurant_address", restaurant_address)
                .add("can_discount", can_discount)
                .add("user_name", user_name)
                .add("user_phone", user_phone)
                .add("fetch_date", fetch_date)
                .add("user_message", user_message)
                .add("order_type", order_type)
                .add("orders", orders)
                .add("use_bonus", use_bonus)
                .toString();
    }

    public static class OrderType implements  Serializable {
        private static final long serialVersionUID = 2277467248368339049L;
        public BillingType billing;
        public Delivery delivery;


        OrderType(){
        }

        public static OrderType setDefault(){
            OrderType orderType = new OrderType();
            orderType.billing = BillingType.ORIGINAL;
            orderType.delivery = Delivery.OUT;
            return  orderType;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this.getClass())
                    .add("billing", billing)
                    .add("delivery", delivery)
                    .toString();
        }
    }

    public static class OrderData implements Serializable {
        private static final long serialVersionUID = -7710327114343945469L;
        public int count;
        public String category_uuid;
        public FoodItemVo item = new FoodItemVo();

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this.getClass())
                    .add("category_uuid", category_uuid)
                    .add("count", count)
                    .add("item", item)
                    .toString();
        }

    }
}
