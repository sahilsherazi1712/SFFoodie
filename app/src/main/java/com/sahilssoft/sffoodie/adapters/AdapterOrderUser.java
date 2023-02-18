package com.sahilssoft.sffoodie.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.activities.OrderDetailsUsersActivity;
import com.sahilssoft.sffoodie.models.ModelOrderUser;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderUser extends RecyclerView.Adapter<AdapterOrderUser.HolderOrderUser> {
    private Context context;
    private ArrayList<ModelOrderUser> orderUserList;

    public AdapterOrderUser(Context context, ArrayList<ModelOrderUser> orderUserList) {
        this.context = context;
        this.orderUserList = orderUserList;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user,parent,false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position) {
        // get data
        ModelOrderUser modelOrderUser = orderUserList.get(position);
        String orderId = modelOrderUser.getOrderId();
        String orderTime = modelOrderUser.getOrderTime();
        String orderStatus = modelOrderUser.getOrderStatus();
        String orderCost = modelOrderUser.getOrderCost();
        String orderTo = modelOrderUser.getOrderTo();
        String orderBy = modelOrderUser.getOrderBy();

        //get shop info
        loafShopInfo(modelOrderUser,holder);

        //set data
        holder.amountTV.setText("Amount: $"+orderCost);
        holder.statusTV.setText(orderStatus);
        holder.orderIdTV.setText("OrderID: "+orderId);
        //change order status text color
        if (orderStatus.equals("In Progress")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.teal_700));
        }else if (orderStatus.equals("Completed")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.green));
        }else if (orderStatus.equals("Cancelled")){
            holder.statusTV.setTextColor(context.getResources().getColor(R.color.red));
        }

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String dateFormat = DateFormat.format("dd/MM/yyyy",calendar).toString();  //e.g. 16/06/2022

        holder.dateTV.setText(dateFormat);
        //
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open order details, we need two keys there, orderId, orderTo
                Intent intent = new Intent(context, OrderDetailsUsersActivity.class);
                intent.putExtra("orderId",orderId);
                intent.putExtra("orderTo",orderTo);
                context.startActivity(intent); //now get these values through intent on OrderDetailedUsersActivity
            }
        });
    }

    private void loafShopInfo(ModelOrderUser modelOrderUser, HolderOrderUser holder) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(modelOrderUser.getOrderTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        holder.shopNameTV.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderUserList.size();
    }

    public class HolderOrderUser extends RecyclerView.ViewHolder {

        private TextView orderIdTV,dateTV,shopNameTV,amountTV,statusTV;

        public HolderOrderUser(@NonNull View itemView) {
            super(itemView);

            orderIdTV = itemView.findViewById(R.id.orderIdTV);
            dateTV = itemView.findViewById(R.id.dateTV);
            shopNameTV = itemView.findViewById(R.id.shopNameTV);
            amountTV = itemView.findViewById(R.id.amountTV);
            statusTV = itemView.findViewById(R.id.statusTV);

        }
    }
}
