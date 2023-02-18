package com.sahilssoft.sffoodie.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.FilterOrderShop;
import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.activities.OrderDetailsSellerActivity;
import com.sahilssoft.sffoodie.activities.OrderDetailsUsersActivity;
import com.sahilssoft.sffoodie.models.ModelOrderShop;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable {

    private Context context;
    public ArrayList<ModelOrderShop> orderShopArrayList, filterList;
    private FilterOrderShop filter;

    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> orderShopArrayList) {
        this.context = context;
        this.orderShopArrayList = orderShopArrayList;
        this.filterList = orderShopArrayList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller,parent,false);
        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {
        //get data
        ModelOrderShop modelOrderShop = orderShopArrayList.get(position);
        String orderId = modelOrderShop.getOrderId();
        String orderTime = modelOrderShop.getOrderTime();
        String orderStatus = modelOrderShop.getOrderStatus();
        String orderCost = modelOrderShop.getOrderCost();
        String orderBy = modelOrderShop.getOrderBy();
        String orderTo = modelOrderShop.getOrderTo();

        //load user/buyer info
        loadUserInfo(modelOrderShop,holder);

        //set data
        holder.amountTV.setText("Amount: $" + orderCost);
        holder.statusTV.setText(orderStatus);
        holder.orderIdTV.setText("Order ID:"+orderId);
        // change order status text color
        if (orderStatus.equals("In Progress")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.teal_700));
        }else if (orderStatus.equals("Completed")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.green));
        }else if (orderStatus.equals("Cancelled")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.red));
        }

        // convert time to proper format e.g. dd/MM/yyyy
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String dateFormat = DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.orderDateTV.setText(dateFormat);

        holder.itemView.setOnClickListener(view -> {
            //open order details
            Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
            intent.putExtra("orderId",orderId); //to load order info
            intent.putExtra("orderBy",orderBy); // to load info of the user who placed order
            context.startActivity(intent);
        });
    }

    private void loadUserInfo(ModelOrderShop modelOrderShop, HolderOrderShop holder) {
        //to load email of the user/buyer: modelOrderShop.getOrderBy() contains the uid of that user/buyer
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderShop.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("email").getValue();
                        holder.emailTV.setText(email);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderShopArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterOrderShop(this, filterList);
        }
        return filter;
    }

    public class HolderOrderShop extends RecyclerView.ViewHolder {

        private TextView orderIdTV,orderDateTV,emailTV,amountTV,statusTV;

        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);

            orderIdTV = itemView.findViewById(R.id.orderIdTV);
            orderDateTV = itemView.findViewById(R.id.orderDateTV);
            emailTV = itemView.findViewById(R.id.emailTV);
            amountTV = itemView.findViewById(R.id.amountTV);
            statusTV = itemView.findViewById(R.id.statusTV);
        }
    }
}
