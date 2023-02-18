package com.sahilssoft.sffoodie.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.activities.ShopDetailsActivity;
import com.sahilssoft.sffoodie.models.ModelCartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {
    private Context context;
    private ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public AdapterCartItem.HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate row_cartItem.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem,parent,false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCartItem.HolderCartItem holder, int position) {
        //get data
        ModelCartItem modelCartItem = cartItems.get(position);
        String id = modelCartItem.getId();
        String pId = modelCartItem.getpId();
        String title = modelCartItem.getName();
        String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        //set data
        holder.itemTitleTv.setText(""+title);
        holder.itemPriceTv.setText(""+cost);
        holder.itemQuantityTv.setText("["+quantity+"]"); //e.g. [3]
        holder.itemPriceEachTv.setText(""+price);

        //handle remove click listener, delete item from cart
        holder.itemRemoveTv.setOnClickListener(view -> {
            //will create table if not exists, but in that case will must exist
            EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                    .setTableName("iTEMS_TABLE")
                    .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                    .addColumn("Item_PId",new String[]{"text","not null"})
                    .addColumn("Item_Name",new String[]{"text","not null"})
                    .addColumn("Item_Price_Each",new String[]{"text","not null"})
                    .addColumn("Item_Price",new String[]{"text","not null"})
                    .addColumn("Item_Quantity",new String[]{"text","not null"})
                    .doneTableColumn();

            easyDB.deleteRow(1,id);
            Toast.makeText(context, "Removed from cart ...", Toast.LENGTH_SHORT).show();

            //refresh list
            cartItems.remove(position);
            notifyItemChanged(position);
            notifyDataSetChanged();

            double tx = Double.parseDouble((((ShopDetailsActivity)context).allTotalPriceTV.getText().toString().trim().replace("$","")));
            double totalPrice = tx - Double.parseDouble(cost.replace("$",""));
            double deliveryFee = Double.parseDouble((((ShopDetailsActivity)context).deliveryFee.replace("$","")));
            double sTotalPrice = Double.parseDouble(String.format("%.2f",totalPrice)) - Double.parseDouble(String.format("%.2f",deliveryFee));
            ((ShopDetailsActivity)context).allTotalPrice=0.00;
            ((ShopDetailsActivity)context).sTotalTV.setText("$"+String.format("%.2f",sTotalPrice));
            ((ShopDetailsActivity)context).allTotalPriceTV.setText("$"+String.format("%.2f",Double.parseDouble(String.format("%.2f",totalPrice))));

            //after removing item from cart, update cart count
            ((ShopDetailsActivity)context).cartCount();
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class HolderCartItem extends RecyclerView.ViewHolder {

        private TextView itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantityTv,itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);
        }
    }
}
