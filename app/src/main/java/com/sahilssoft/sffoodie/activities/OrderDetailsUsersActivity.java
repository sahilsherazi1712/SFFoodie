package com.sahilssoft.sffoodie.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.adapters.AdapterOrderedItem;
import com.sahilssoft.sffoodie.models.ModelOrderedItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUsersActivity extends AppCompatActivity {

    private String orderId, orderTo;

    //ui
    private ImageButton backBtn,writeReviewBtn;
    private TextView orderIdTV,dateTV,orderStatusTV,shopNameTV,totalItemsTV,amountTV
            ,addressTV;
    private RecyclerView itemsRV;
    private FirebaseAuth auth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_users);

        //init views
        backBtn = findViewById(R.id.backBtn);
        orderIdTV = findViewById(R.id.orderIdTV);
        dateTV = findViewById(R.id.dateTV);
        orderStatusTV = findViewById(R.id.orderStatusTV);
        shopNameTV = findViewById(R.id.shopNameTV);
        totalItemsTV = findViewById(R.id.totalItemsTV);
        amountTV = findViewById(R.id.amountTV);
        addressTV = findViewById(R.id.addressTV);
        itemsRV = findViewById(R.id.itemsRV);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);

        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo"); //orderTo contains uid of the shop where we placed order
        orderId = intent.getStringExtra("orderId");

        auth = FirebaseAuth.getInstance();
        loadShopInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(view -> {
            onBackPressed();
        });
        writeReviewBtn.setOnClickListener(view -> {
            Intent intent1 = new Intent(OrderDetailsUsersActivity.this,WriteReviewActivity.class);
            intent1.putExtra("shopUid",orderTo); //to write review to a shop we must have uid of the shop
            startActivity(intent1);
        });

    }

    private void loadOrderedItems() {
        //init list
        orderedItemArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemArrayList.clear(); //before loading items clear list
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            //add to list
                            orderedItemArrayList.add(modelOrderedItem);
                        }
                        //all items added to list
                        //setup adapter
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsUsersActivity.this,orderedItemArrayList);
                        //set adapter
                        itemsRV.setAdapter(adapterOrderedItem);
                        //set items count
                        totalItemsTV.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        String latitude = ""+snapshot.child("latitude").getValue();
                        String longitude = ""+snapshot.child("longitude").getValue();

                        //convert timestamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String dateFormat = DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString(); //e.g. 20/05/2022 12:01 PM

                        if (orderStatus.equals("In Progress")){
                            orderStatusTV.setTextColor(getResources().getColor(R.color.teal_700));
                        }else if (orderStatus.equals("Completed")){
                            orderStatusTV.setTextColor(getResources().getColor(R.color.green));
                        }else if (orderStatus.equals("Cancelled")){
                            orderStatusTV.setTextColor(getResources().getColor(R.color.red));
                        }

                        //set data
                        orderIdTV.setText(orderId);
                        orderStatusTV.setText(orderStatus);
                        amountTV.setText("$" + orderCost + "[Including delivery fee $" + deliveryFee + "]");
                        dateTV.setText(dateFormat);

                        findAddress(latitude,longitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopInfo() {
        //get shop info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        shopNameTV.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        //find address, country, state, city
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat,lon,1);
            String address = addresses.get(0).getAddressLine(0); //complete address
            addressTV.setText(address);
        }catch (Exception e){

        }
    }
}