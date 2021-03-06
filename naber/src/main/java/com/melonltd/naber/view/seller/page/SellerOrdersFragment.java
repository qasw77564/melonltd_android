package com.melonltd.naber.view.seller.page;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bigkoo.alertview.AlertView;
import com.bigkoo.alertview.OnItemClickListener;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.melonltd.naber.R;
import com.melonltd.naber.model.api.ApiManager;
import com.melonltd.naber.model.api.ThreadCallback;
import com.melonltd.naber.model.constant.NaberConstant;
import com.melonltd.naber.model.type.OrderStatus;
import com.melonltd.naber.util.Tools;
import com.melonltd.naber.view.seller.SellerMainActivity;
import com.melonltd.naber.view.seller.adapter.SellerOrdersAdapter;
import com.melonltd.naber.vo.OrderDetail;
import com.melonltd.naber.vo.OrderVo;
import com.melonltd.naber.vo.ReqData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

import static com.melonltd.naber.model.type.OrderStatus.CAN_FETCH;
import static com.melonltd.naber.model.type.OrderStatus.PROCESSING;
import static com.melonltd.naber.model.type.OrderStatus.UNFINISH;


public class SellerOrdersFragment extends Fragment {
    private static final String TAG = SellerOrdersFragment.class.getSimpleName();
    public static SellerOrdersFragment FRAGMENT = null;
    private static TextView searchDateText;
    private TextView untreatedText, processingText, canFetchText;
    private static ReqData reqData = new ReqData();
    private static OrderStatus STATUS_TAG = OrderStatus.UNFINISH;

    private static List<OrderVo> orderList = Lists.<OrderVo>newArrayList();
    private static SellerOrdersAdapter adapter;
    private static Handler handler = new Handler();

    private static UnFinishRun unFinishRun;
    private static Context context;

    public SellerOrdersFragment() {

    }

    public Fragment getInstance(Bundle bundle) {
        if (FRAGMENT == null) {
            FRAGMENT = new SellerOrdersFragment();
        }
        if (bundle != null) {
            FRAGMENT.setArguments(bundle);
        }
        return FRAGMENT;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        reqData.search_type = OrderStatus.UNFINISH.name();
        this.context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SellerMainActivity.navigationIconDisplay(false, null);
        if (container.getTag(R.id.seller_orders_main_page) == null) {
            View v = inflater.inflate(R.layout.fragment_seller_orders, container, false);
            getViews(v);
            container.setTag(R.id.seller_orders_main_page, v);
            return v;
        }
        return (View) container.getTag(R.id.seller_orders_main_page);
    }

    @Override
    public void onResume() {
        super.onResume();
        SellerMainActivity.changeTabAndToolbarStatus();
        SellerMainActivity.lockDrawer(false);

        SellerMainActivity.notifyDateRange();

        if (OrderStatus.UNFINISH.equals(STATUS_TAG)) {
            loadUnData();
        } else {
            loadData(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeCallbacks(unFinishRun);
        }
    }

    private void getViews(View v) {
        BGARefreshLayout refreshLayout = v.findViewById(R.id.ordersBGARefreshLayout);
        BGANormalRefreshViewHolder refreshViewHolder = new BGANormalRefreshViewHolder(getContext(), true);
        refreshViewHolder.setPullDownRefreshText("Pull");
        refreshViewHolder.setRefreshingText("Pull to refresh");
        refreshViewHolder.setReleaseRefreshText("Pull to refresh");
        refreshViewHolder.setLoadingMoreText("Loading more !");

        refreshLayout.setRefreshViewHolder(refreshViewHolder);
        searchDateText = v.findViewById(R.id.searchDateText);

        untreatedText = v.findViewById(R.id.untreatedText);
        processingText = v.findViewById(R.id.processingText);
        canFetchText = v.findViewById(R.id.canFetchText);
        RecyclerView recyclerView = v.findViewById(R.id.ordersRecyclerView);

        // setListener
        searchDateText.setOnClickListener(new SelectDateListener());
        TabClickListener tabClickListener = new TabClickListener();

        untreatedText.setTag(UNFINISH);
        processingText.setTag(PROCESSING);
        canFetchText.setTag(CAN_FETCH);
        untreatedText.setOnClickListener(tabClickListener);
        processingText.setOnClickListener(tabClickListener);
        canFetchText.setOnClickListener(tabClickListener);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        this.adapter = new SellerOrdersAdapter(this.orderList, new CancelListener(), new FailureListener(), new StatusChangeClickListener());
        recyclerView.setAdapter(adapter);
        Calendar now = Calendar.getInstance();
        searchDateText.setText(new SimpleDateFormat("yyyy-MM-dd").format(now.getTime()));
        searchDateText.setTag(Tools.FORMAT.formatDate(now.getTime()));

        refreshLayout.setDelegate(new BGARefreshLayout.BGARefreshLayoutDelegate() {
            @Override
            public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
                refreshLayout.endRefreshing();

                if (OrderStatus.UNFINISH.equals(STATUS_TAG)) {
                    loadUnData();
                } else {
                    loadData(true);
                }
            }

            @Override
            public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
                refreshLayout.endLoadingMore();
                if (reqData.loadingMore) {
                    loadData(false);
                }
                return false;
            }
        });
    }

    class SelectDateListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            if (OrderStatus.UNFINISH.equals(STATUS_TAG)){
                handler.removeCallbacks(unFinishRun);
            }

            long date = 1000 * 60 * 60 * 24 * 2L;
            long dayOne = 1000 * 60 * 60 * 24 * 1L;

            Calendar now = Calendar.getInstance();
            Calendar startDate = Calendar.getInstance();
            startDate.setTimeInMillis(now.getTime().getTime() - dayOne);
            Calendar endDate = Calendar.getInstance();
            endDate.setTimeInMillis(now.getTime().getTime() + date);

            new TimePickerBuilder(getContext(),
                    new OnTimeSelectListener() {
                        @Override
                        public void onTimeSelect(Date date, View v) {
                            searchDateText.setTag(Tools.FORMAT.formatDate(date));
                            searchDateText.setText(new SimpleDateFormat("yyyy-MM-dd").format(date));

                            if (OrderStatus.UNFINISH.equals(STATUS_TAG)){
                                handler.post(unFinishRun);
                            }else {
                                loadData(true);
                            }
                        }
                    })
                    .setType(new boolean[]{true, true, true, false, false, false})
                    .setTitleSize(20)
                    .setOutSideCancelable(true)
                    .isCyclic(false)
                    .setTitleBgColor(getResources().getColor(R.color.naber_dividing_line_gray))
                    .setCancelColor(getResources().getColor(R.color.naber_dividing_gray))
                    .setSubmitColor(getResources().getColor(R.color.naber_dividing_gray))
                    .setDate(now)
                    .setRangDate(startDate, endDate)
                    .isCenterLabel(false)
                    .isDialog(false)
                    .build()
                    .show();
        }

    }

    class StatusChangeClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (handler != null) {
                handler.removeCallbacks(unFinishRun);
            }
            final  ChangeTmp tmp = (ChangeTmp) v.getTag();

            OnItemClickListener listener = new OnItemClickListener(){

                @Override
                public void onItemClick(Object o, int position) {
                    if (position == 1) {
                        new Handler().postDelayed(new StatusChangeRun(tmp.orderUUID, tmp.type, null), 300);
                    } else if (position == 0 && OrderStatus.UNFINISH.equals(STATUS_TAG)){
                        handler.postDelayed(unFinishRun, NaberConstant.SELLER_LIVE_ORDER_REFRESH_TIMER);
                    }
                }
            };

            new AlertView.Builder()
                    .setContext(getContext())
                    .setStyle(AlertView.Style.Alert)
                    .setTitle("")
                    .setMessage(tmp.alertMsg)
                    .setOthers(new String[]{"返回", "確定"})
                    .setOnItemClickListener(listener)
                    .build()
                    .setCancelable(false)
                    .show();
        }
    }


    class StatusChangeRun implements Runnable {
        private String message;
        private String orderUUID;
        private String type;

        StatusChangeRun(String orderUUID, String type, String message) {
            this.orderUUID = orderUUID;
            this.type = type;
            this.message = message;
        }

        @Override
        public void run() {
            if (handler != null) {
                handler.removeCallbacks(unFinishRun);
            }
            sellerChangeOrder(this.orderUUID, this.type, this.message);
        }
    }


    private void sellerChangeOrder(String orderUUID, String type, String message) {
        ReqData req = new ReqData();
        req.uuid = orderUUID;
        req.type = type;
        if (Strings.isNullOrEmpty(type)){
            return;
        }
        if (!Strings.isNullOrEmpty(message)){
            req.message = message;
        }

        ApiManager.sellerChangeOrder(req, new ThreadCallback(getContext()) {
            @Override
            public void onSuccess(String responseBody) {

                if (OrderStatus.UNFINISH.equals(STATUS_TAG)){
                    if (handler != null){
                        handler.removeCallbacks(unFinishRun);
                    }
                    handler.post(unFinishRun);
                }else {
                    loadData(true);
                }
            }

            @Override
            public void onFail(Exception error, String msg) {
                if (OrderStatus.UNFINISH.equals(STATUS_TAG)) {
                    if (handler != null) {
                        handler.removeCallbacks(unFinishRun);
                    }
                    handler.post(unFinishRun);
                }
            }
        });
    }

    class CancelListenerView implements View.OnClickListener {
        private View view;
        private TextView defText1;
        private TextView defText2;
        private EditText customEdit;
        private String msg = "";

        CancelListenerView() {
            this.view = getLayoutInflater().inflate(R.layout.seller_cancel_order_notifiy_to_user_message, null);
            this.defText1 = view.findViewById(R.id.nameEdit);
            this.defText2 = view.findViewById(R.id.priceEdit);
            this.customEdit = view.findViewById(R.id.customEdit);
            // 先帶入預設一
            this.msg = defText1.getText().toString();
            setListener();
        }

        private void setListener() {
            this.defText1.setOnClickListener(CancelListenerView.this);
            this.defText2.setOnClickListener(CancelListenerView.this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.nameEdit:
                    v.setBackgroundResource(R.drawable.naber_reverse_orange_button_style);
                    this.defText2.setBackgroundResource(R.drawable.naber_reverse_gary_button_style);
                    this.msg = defText1.getText().toString();
                    break;
                case R.id.priceEdit:
                    v.setBackgroundResource(R.drawable.naber_reverse_orange_button_style);
                    this.defText1.setBackgroundResource(R.drawable.naber_reverse_gary_button_style);
                    this.msg = defText2.getText().toString();
                    break;
            }
        }

        private View getView() {
            return this.view;
        }

        private String getMessage() {
            if (!Strings.isNullOrEmpty(customEdit.getText().toString()) && customEdit.getText().toString().length() > 0) {
                return customEdit.getText().toString();
            }
            return this.msg;
        }
    }

    class CancelListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            if (handler != null) {
                handler.removeCallbacks(unFinishRun);
            }

            final ChangeTmp tmp = (ChangeTmp)v.getTag();

            final CancelListenerView extView = new CancelListenerView();
            new AlertView.Builder()
                    .setContext(getContext())
                    .setStyle(AlertView.Style.Alert)
                    .setOthers(new String[]{"返回", "送出"})
                    .setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (position == 1) {
                                sellerChangeOrder(tmp.orderUUID, tmp.type, extView.getMessage());
                            } else if (position == 0 && OrderStatus.UNFINISH.equals(STATUS_TAG)) {
                                handler.postDelayed(unFinishRun, NaberConstant.SELLER_LIVE_ORDER_REFRESH_TIMER);
                            }
                        }
                    })
                    .build()
                    .addExtView(extView.getView())
                    .setCancelable(false)
                    .show();
        }
    }


    class FailureListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            if (handler != null) {
                handler.removeCallbacks(unFinishRun);
            }
            final ChangeTmp tmp = (ChangeTmp) v.getTag();
            final String msg = "確定客戶跑單嗎？\n會影響客戶點餐的權益以及紅利點數";
            new AlertView.Builder()
                    .setContext(getContext())
                    .setStyle(AlertView.Style.Alert)
                    .setTitle("")
                    .setMessage(msg)
                    .setOthers(new String[]{"返回", "送出"})
                    .setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (position == 1) {
                                sellerChangeOrder(tmp.orderUUID, tmp.type, "你的商品超過時間未領取，記點一次，請注意往後的訂餐權利！");
                            } else if (position == 0 && OrderStatus.UNFINISH.equals(STATUS_TAG)) {
                                handler.postDelayed(unFinishRun, NaberConstant.SELLER_LIVE_ORDER_REFRESH_TIMER);
                            }
                        }
                    })
                    .build()
                    .setCancelable(false)
                    .show();
        }
    }


    public static void loadUnData() {
        if (OrderStatus.UNFINISH.equals(STATUS_TAG)) {
            if (handler == null) {
                handler = new Handler();
            }

            if (unFinishRun == null) {
                unFinishRun = new UnFinishRun(context);
            }

            if (handler != null && unFinishRun != null) {
                handler.removeCallbacks(unFinishRun);
            }

            handler.post(unFinishRun);
        }
    }


    // 問處理資料 輪循打Req
    static class UnFinishRun implements Runnable {
        private Context context;

        public UnFinishRun(Context context) {
            this.context = context;
        }

        @Override
        public void run() {

            orderList.clear();
            adapter.notifyDataSetChanged();

            reqData.page = 1;
            reqData.loadingMore = true;

            ReqData req = new ReqData();
            req.page = 1;
            req.search_type = OrderStatus.UNFINISH.name();
            if (searchDateText != null){
                req.date = (String)searchDateText.getTag();
            }else {
                req.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            }


            ApiManager.sellerOrderList(req, new ThreadCallback(this.context) {
                @Override
                public void onSuccess(String responseBody) {
                    orderList.clear();
                    List<OrderVo> list = Tools.JSONPARSE.fromJsonList(responseBody, OrderVo[].class);

                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).order_detail = Tools.JSONPARSE.fromJson(list.get(i).order_data, OrderDetail.class);
                    }

                    reqData.loadingMore = list.size() % NaberConstant.PAGE == 0 && list.size() != 0;
                    orderList.addAll(list);
                    adapter.notifyDataSetChanged();

                    handler.postDelayed(unFinishRun, NaberConstant.SELLER_LIVE_ORDER_REFRESH_TIMER);
                }

                @Override
                public void onFail(Exception error, String msg) {
                    handler.postDelayed(unFinishRun, NaberConstant.SELLER_LIVE_ORDER_REFRESH_TIMER);
                }
            });
        }
    }


    private void loadData(boolean isRefresh) {
        if (isRefresh) {
            orderList.clear();
            adapter.notifyDataSetChanged();
            reqData.page = 0;
        }

        if (OrderStatus.UNFINISH.equals(STATUS_TAG)){
            handler.removeCallbacks(unFinishRun);
        }

        reqData.page++;
        reqData.date = (String) searchDateText.getTag();

        ApiManager.sellerOrderList(reqData, new ThreadCallback(getContext()) {
            @Override
            public void onSuccess(String responseBody) {

                List<OrderVo> list = Tools.JSONPARSE.fromJsonList(responseBody, OrderVo[].class);
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).order_detail = Tools.JSONPARSE.fromJson(list.get(i).order_data, OrderDetail.class);
                }

                reqData.loadingMore  = list.size() % NaberConstant.PAGE == 0 && list.size() != 0;

                orderList.addAll(list);
                adapter.notifyDataSetChanged();

                if (OrderStatus.UNFINISH.equals(STATUS_TAG)){
                    handler.postDelayed(unFinishRun, NaberConstant.SELLER_LIVE_ORDER_REFRESH_TIMER);
                }
            }

            @Override
            public void onFail(Exception error, String msg) {

            }
        });
    }

    class TabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (handler != null) {
                handler.removeCallbacks(unFinishRun);
            }

            OrderStatus status = (OrderStatus) view.getTag();
            if (STATUS_TAG.equals(status)) {
                return;
            }

            STATUS_TAG = status;
            reqData.search_type = STATUS_TAG.name();
            reqData.loadingMore = true;
            changeTab((TextView) view, status);
        }

        private void changeTab(TextView textView, OrderStatus status) {
            List<TextView> views = Lists.newArrayList(untreatedText, processingText, canFetchText);
            for (TextView tv : views) {
                tv.setTextColor(getResources().getColor(android.R.color.black));
                tv.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                if (tv.equals(textView)) {
                    tv.setBackgroundColor(getResources().getColor(R.color.naber_basis));
                    tv.setTextColor(getResources().getColor(android.R.color.white));
                }
            }
            if (OrderStatus.UNFINISH.equals(STATUS_TAG)) {
                loadUnData();
            } else {
                loadData(true);
            }
        }
    }


    public static class ChangeTmp {
        public String orderUUID;
        public String type;
        public String alertMsg;

        public ChangeTmp(String orderUUID, String type, String alertMsg){
            this.orderUUID = orderUUID;
            this.type = type;
            this.alertMsg = alertMsg;
        }
    }

}
