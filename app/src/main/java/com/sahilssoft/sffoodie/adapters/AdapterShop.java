package com.sahilssoft.sffoodie.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.activities.ShopDetailsActivity;
import com.sahilssoft.sffoodie.models.ModelShop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop> {
    private Context context;
    public ArrayList<ModelShop> shopList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopList) {
        this.context = context;
        this.shopList = shopList;
    }

    @NonNull
    @Override
    public AdapterShop.HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_shop.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_shop,parent,false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterShop.HolderShop holder, int position) {
        //get data
        ModelShop modelShop = shopList.get(position);
        String uid = modelShop.getUid();
        String email = modelShop.getEmail();
        String name = modelShop.getName();
        String shopName = modelShop.getShopName();
        String phone = modelShop.getPhone();
        String deliveryFee = modelShop.getDeliveryFee();
        String country = modelShop.getCountry();
        String state = modelShop.getState();
        String city = modelShop.getCity();
        String address = modelShop.getAddress();
        String latitude = modelShop.getLatitude();
        String longitude = modelShop.getLongitude();
        String timestamp = modelShop.getTimestamp();
        String accountType = modelShop.getAccountType();
        String online = modelShop.getOnline();
        String shopOpen = modelShop.getShopOpen();
        String profileImage = modelShop.getProfileImage();

        loadReviews(modelShop,holder);

        //set data
        holder.shopNameTV.setText(shopName);
        holder.phoneTV.setText(phone);
        holder.addressTV.setText(address);
        //check if online
        if (online.equals("true")){
            //shop owner is online
            holder.onlineIV.setVisibility(View.VISIBLE);
        }else{
            //shop owner is offline
            holder.onlineIV.setVisibility(View.GONE);
        }
        //check if shop open
        if (shopOpen.equals("true")){
            //shop is open
            holder.shopClosedTV.setVisibility(View.GONE);
        }else{
            //shop is closed
            holder.shopClosedTV.setVisibility(View.VISIBLE);
        }
        try {
            Picasso.get()
                    .load(profileImage)
                    .placeholder(R.drawable.ic_shopping_cart)
                    .into(holder.shopIV);
        }catch (Exception e){
            holder.shopIV.setImageResource(R.drawable.ic_shopping_cart);
        }

        //handle clicks listener, show shops details
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid",uid);
                context.startActivity(intent);
            }
        });

    }

    private float ratingSum = 0;
    private void loadReviews(ModelShop modelShop, HolderShop holder) {

        String shopUid = modelShop.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ratingSum = 0;
                        for (DataSnapshot ds : snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue()); //e.g. 4.3
                            ratingSum = ratingSum + rating; //for avg rating, add(addition of) all ratings, later will divide it by the number of reviews
                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        holder.ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return shopList.size(); //return number of records
    }

    public class HolderShop extends RecyclerView.ViewHolder {

        //ui view of row_shop.xml
        private CircleImageView shopIV;
        private ImageView onlineIV;
        private TextView shopClosedTV,shopNameTV,phoneTV,addressTV;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            shopIV=itemView.findViewById(R.id.shopIV);
            onlineIV=itemView.findViewById(R.id.onlineIV);
            shopClosedTV=itemView.findViewById(R.id.shopClosedTV);
            shopNameTV=itemView.findViewById(R.id.shopNameTV);
            phoneTV=itemView.findViewById(R.id.phoneTV);
            addressTV=itemView.findViewById(R.id.addressTV);
            ratingBar=itemView.findViewById(R.id.ratingBar);
        }
    }
}
