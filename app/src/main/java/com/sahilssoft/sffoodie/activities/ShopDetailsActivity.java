package com.sahilssoft.sffoodie.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.Constants;
import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.adapters.AdapterCartItem;
import com.sahilssoft.sffoodie.adapters.AdapterProductUser;
import com.sahilssoft.sffoodie.adapters.AdapterReview;
import com.sahilssoft.sffoodie.models.ModelCartItem;
import com.sahilssoft.sffoodie.models.ModelProduct;
import com.sahilssoft.sffoodie.models.ModelReview;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    private ImageView shopIV;
    private TextView shopNameTV,phoneTV,emailTV,openCloseTV,deliveryFeeTV,addressTV,filterProductsTV,cartCountTV;
    private ImageButton callBtn,mapBtn,cartBtn,backBtn,filterProductBtn,reviewsBtn;
    private EditText searchProductsET;
    private RecyclerView productsRV;
    private RatingBar ratingBar;

    private String shopUid;
    private String myLatitude,myLongitude,myPhone;
    private String shopName,shopEmail,shopPhone,shopAddress,shopLatitude,shopLongitude;
    public String deliveryFee;

    private FirebaseAuth auth;
    private ArrayList<ModelProduct> productList;
    private AdapterProductUser adapterProductUser;

    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;

    //progress dialog
    private ProgressDialog progressDialog;

    private EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        shopIV=findViewById(R.id.shopIV);
        shopNameTV=findViewById(R.id.shopNameTV);
        phoneTV=findViewById(R.id.phoneTV);
        emailTV=findViewById(R.id.emailTV);
        openCloseTV=findViewById(R.id.openCloseTV);
        deliveryFeeTV=findViewById(R.id.deliveryFeeTV);
        addressTV=findViewById(R.id.addressTV);
        callBtn=findViewById(R.id.callBtn);
        mapBtn=findViewById(R.id.mapBtn);
        cartBtn=findViewById(R.id.cartBtn);
        backBtn=findViewById(R.id.backBtn);
        searchProductsET=findViewById(R.id.searchProductsET);
        filterProductBtn=findViewById(R.id.filterProductBtn);
        filterProductsTV=findViewById(R.id.filterProductsTV);
        productsRV=findViewById(R.id.productsRV);
        cartCountTV=findViewById(R.id.cartCountTV);
        reviewsBtn=findViewById(R.id.reviewsBtn);
        ratingBar=findViewById(R.id.ratingBar);

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //get uid of the shop from intent
        shopUid=getIntent().getStringExtra("shopUid");
        auth=FirebaseAuth.getInstance();
        loadMyInfo();
        loadShopDetails();
        loadShopProducts();
        loadReviews(); // avg rating, set on ratingbar

        //declare it to class level and init in onCreate
        easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("iTEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn("Item_PId",new String[]{"text","not null"})
                .addColumn("Item_Name",new String[]{"text","not null"})
                .addColumn("Item_Price_Each",new String[]{"text","not null"})
                .addColumn("Item_Price",new String[]{"text","not null"})
                .addColumn("Item_Quantity",new String[]{"text","not null"})
                .doneTableColumn();

        //each shop has its own products and orders so if user add items to cart and go back and open cart in different shops
        // then cart should be different so delete cart data whenever user open this activity
        deleteCartData();
        cartCount();

        //search
        searchProductsET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterProductUser.getFilter().filter(charSequence);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        backBtn.setOnClickListener(view -> {
            //go to back activity
            onBackPressed();
        });
        cartBtn.setOnClickListener(view -> {
            //show cart dialog
            showCartDialog();
        });
        callBtn.setOnClickListener(view -> {
            dailPhone();
        });
        mapBtn.setOnClickListener(view -> {
            openMap();
        });
        filterProductBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
            builder.setTitle("Choose Category")
                    .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //get selected item
                            String selected = Constants.productCategories1[i];
                            filterProductsTV.setText(selected);
                            if (selected.equals("All")){
                                //load all
                                loadShopProducts();
                            }else{
                                //load filtered
                                adapterProductUser.getFilter().filter(selected);
                            }
                        }
                    }).show();
        });
        //handle review btn click, open reviews activity
        reviewsBtn.setOnClickListener(view -> {
            //pass shop uid to show its reviews
            Intent intent = new Intent(ShopDetailsActivity.this,ShopReviewsActivity.class);
            intent.putExtra("shopUid",shopUid);
            startActivity(intent);
        });
    }

    private float ratingSum = 0;
    private void loadReviews() {

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

                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteCartData() {
        easyDB.deleteAllDataFromTable(); //delete all records from cart
    }
    //items count
    public void cartCount(){
        //keep it public so we can access in adapter
        //get cart count
        int count = easyDB.getAllData().getCount();
        if (count<=0){
            //no item in the cart,, hide the count text view
            cartCountTV.setVisibility(View.GONE);
        }else{
            //have items in the cart, show cart count text view and set count
            cartCountTV.setVisibility(View.VISIBLE);
            cartCountTV.setText(""+count);  //concatenate with string, because we cann't set an integer in textView
        }
    }
    public double allTotalPrice = 0.00;
    //need to access these views int adapter so making public
    public TextView sTotalTV,dFeeTV,allTotalPriceTV;
    private void showCartDialog() {
        //init list
        cartItemList = new ArrayList<>();

        ////inflate cart layout
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart,null);
        //init views
        TextView shopNameTV = view.findViewById(R.id.shopNameTV);
        RecyclerView cartItemsRV = view.findViewById(R.id.cartItemsRV);
        sTotalTV = view.findViewById(R.id.sTotalTV);
        dFeeTV = view.findViewById(R.id.dFeeTV);
        allTotalPriceTV = view.findViewById(R.id.totalTV);
        Button checkoutBtn = view.findViewById(R.id.checkoutBtn);

        //dialog
        AlertDialog.Builder builder =new AlertDialog.Builder(this);
        //set view to dialog
        builder.setView(view);

        shopNameTV.setText(shopName);

        EasyDB easyDB = EasyDB.init(this,"ITEMS_DB")
                .setTableName("iTEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn("Item_PId",new String[]{"text","not null"})
                .addColumn("Item_Name",new String[]{"text","not null"})
                .addColumn("Item_Price_Each",new String[]{"text","not null"})
                .addColumn("Item_Price",new String[]{"text","not null"})
                .addColumn("Item_Quantity",new String[]{"text","not null"})
                .doneTableColumn();

        //get all records from db
        Cursor rec = easyDB.getAllData();
        while (rec.moveToNext()){
            String id = rec.getString(1);
            String pId = rec.getString(2);
            String name = rec.getString(3);
            String price = rec.getString(4);
            String cost = rec.getString(5);
            String quantity = rec.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem(
                    ""+id,
                    ""+pId,
                    ""+name,
                    ""+price,
                    ""+cost,
                    ""+quantity
            );
            cartItemList.add(modelCartItem);
        }
        //setup adapter
        adapterCartItem = new AdapterCartItem(this,cartItemList);
        //set to recycle view
        cartItemsRV.setAdapter(adapterCartItem);

        dFeeTV.setText("$" + deliveryFee);
        sTotalTV.setText("$" + String.format("%.2f", allTotalPrice));
        allTotalPriceTV.setText("$"+(allTotalPrice+Double.parseDouble(deliveryFee.replace("$",""))));

        //show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //reset total price non dialog dismiss
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                allTotalPrice = 0.00;
            }
        });

        //place order
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //first validate delivery address
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")){
                    //user didn't enter address in the profile
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your address in the profile before placing your order ...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if (myPhone.equals("") || myPhone.equals("null")){
                    //user didn't enter phone number in the profile
                    Toast.makeText(ShopDetailsActivity.this, "Please enter your phone in the profile before placing your order ...", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                if (cartItemList.size() == 0){
                    //cart list is empty
                    Toast.makeText(ShopDetailsActivity.this, "No item in the cart", Toast.LENGTH_SHORT).show();
                    return; //don't proceed further
                }
                submitOrder();
            }
        });
    }

    private void submitOrder() {
        //show progress dialog
        progressDialog.setMessage("Placing Order ...");
        progressDialog.show();

        //for order id and order time
        String timestamp = ""+System.currentTimeMillis();

        String cost = allTotalPriceTV.getText().toString().trim().replace("$","");  //remove $ if contains

        //add latitude, longitude of user to each order | delete previous orders from firebase or add manually to them

        //set order data
        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("orderId",""+timestamp);
        hashMap.put("orderTime",""+timestamp);
        hashMap.put("orderStatus","In Progress"); //In progress/completed/cancelled
        hashMap.put("orderCost",""+cost);
        hashMap.put("orderBy",""+auth.getUid());
        hashMap.put("orderTo",""+shopUid);
        hashMap.put("latitude",""+myLatitude);
        hashMap.put("longitude",""+myLongitude);

        //add to db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        reference.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added, now add order items
                        for (int i=0; i< cartItemList.size();i++){
                            String pId = cartItemList.get(i).getpId();
                            String id = cartItemList.get(i).getId();
                            String cost = cartItemList.get(i).getCost();
                            String name = cartItemList.get(i).getName();
                            String price = cartItemList.get(i).getPrice();
                            String quantity = cartItemList.get(i).getQuantity();

                            HashMap<String,String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId",pId);
                            hashMap1.put("name",name);
                            hashMap1.put("cost",cost);
                            hashMap1.put("price",price);
                            hashMap1.put("quantity",quantity);

                            reference.child(timestamp).child("Items").child(pId).setValue(hashMap1);
                        }
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed Successfully ...", Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // failed to place order
                        progressDialog.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMap() {
        //saddr means source address
        //daddr means destination address
        String address = "https://maps.google.com/maps?saddr=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(address));
        startActivity(intent);
    }

    private void dailPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+Uri.encode(shopPhone))));
        Toast.makeText(this, ""+shopPhone, Toast.LENGTH_SHORT).show();
    }

    private void loadMyInfo() {
        //load my info
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            //get user data
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            myPhone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();
                            myLatitude = ""+ds.child("latitude").getValue();
                            myLongitude = ""+ds.child("longitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopProducts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop data
                String name = ""+snapshot.child("name").getValue();
                shopName = ""+snapshot.child("shopName").getValue();
                shopEmail = ""+snapshot.child("email").getValue();
                shopPhone = ""+snapshot.child("phone").getValue();
                shopAddress = ""+snapshot.child("address").getValue();
                shopLatitude = ""+snapshot.child("latitude").getValue();
                shopLongitude = ""+snapshot.child("longitude").getValue();
                deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                String profileImage = ""+snapshot.child("profileImage").getValue();
                String shopOpen = ""+snapshot.child("shopOpen").getValue();

                //set data
                shopNameTV.setText(shopName);
                emailTV.setText(shopEmail);
                deliveryFeeTV.setText("Delivery Free: $"+deliveryFee);
                addressTV.setText(shopAddress);
                phoneTV.setText(shopPhone);
                if (shopOpen.equals("true")){
                    openCloseTV.setText("Open");
                }else{
                    openCloseTV.setText("Close");
                }
                try {
                    Picasso.get()
                            .load(profileImage)
                            .placeholder(R.drawable.ic_shopping_cart)
                            .into(shopIV);
                }catch (Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShopDetails() {
        //init list
        productList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding items
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        //setup adapter
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this,productList);
                        //set adapter
                        productsRV.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void prepareNotificationMessage(String orderId){
        //when user place order, send notification to seller

        //prepare data for notification
        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "New Order " + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations ...! You have new order.";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json (what to send ad where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid",auth.getUid());
            notificationBodyJo.put("sellerUid",shopUid);
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle",NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage",NOTIFICATION_MESSAGE);
            //where to send
            notificationJo.put("to",NOTIFICATION_TOPIC); //to all who subscribed to this topic
            notificationJo.put("data",notificationBodyJo);
        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapic.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending for start order details activity
                //open order details, we need two keys there, orderId, orderTo
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed sending fcm, still start order details activity
                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUsersActivity.class);
                intent.putExtra("orderTo",shopUid);
                intent.putExtra("orderId",orderId);
                startActivity(intent);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                //put required headers
                Map<String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                headers.put("Authorization","key="+Constants.FCM_KEY);

                return headers;
            }
        };

        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}