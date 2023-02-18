package com.sahilssoft.sffoodie.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.adapters.AdapterOrderShop;
import com.sahilssoft.sffoodie.adapters.AdapterProductSeller;
import com.sahilssoft.sffoodie.Constants;
import com.sahilssoft.sffoodie.models.ModelOrderShop;
import com.sahilssoft.sffoodie.models.ModelProduct;
import com.sahilssoft.sffoodie.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainSellerActivity extends AppCompatActivity {

    private TextView nameTV,shopNameTV,emailTV,tabProductsTV,tabOrdersTV,filterProductTV,filteredOrdersTV;
    EditText searchProductET;
    ImageButton editProfileBtn,addProductBtn,filterProductBtn,filterOrderBtn,moreBtn;
    private CircleImageView profileIv;
    private RelativeLayout productsRL,ordersRL;
    private RecyclerView productsRv,orderRV;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_seller);

        nameTV=findViewById(R.id.nameTV);
        shopNameTV=findViewById(R.id.shopNameTV);
        emailTV=findViewById(R.id.emailTV);
        searchProductET=findViewById(R.id.searchProductET);
        editProfileBtn=findViewById(R.id.editProfileBtn);
        addProductBtn=findViewById(R.id.addProductBtn);
        filterProductBtn=findViewById(R.id.filterProductBtn);
        profileIv=findViewById(R.id.profileIv);
        tabProductsTV=findViewById(R.id.tabProductsTV);
        tabOrdersTV=findViewById(R.id.tabOrdersTV);
        filterProductTV=findViewById(R.id.filterProductTV);
        productsRL=findViewById(R.id.productsRL);
        ordersRL=findViewById(R.id.ordersRL);
        productsRv=findViewById(R.id.productsRv);
        filteredOrdersTV=findViewById(R.id.filteredOrdersTV);
        filterOrderBtn=findViewById(R.id.filterOrderBtn);
        orderRV=findViewById(R.id.orderRV);
        moreBtn=findViewById(R.id.moreBtn);

        auth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();
        loadAllProducts();
        //default is product ui, at sellers main screen products are visible
        showProductsUI();
        loadAllOrders();


        //search
        searchProductET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterProductSeller.getFilter().filter(charSequence);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        editProfileBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainSellerActivity.this,ProfileEditSellerActivity.class));
        });
        addProductBtn.setOnClickListener(view -> {
            //open edit add product activity
            startActivity(new Intent(MainSellerActivity.this,AddProductActivity.class));
        });
        tabProductsTV.setOnClickListener(view -> {
            //tab to load products
            showProductsUI();
        });
        tabOrdersTV.setOnClickListener(view -> {
            //tab to load orders
            showOrdersUI();
        });
        filterProductBtn.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
            builder.setTitle("Choose Category")
                    .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //get selected item
                            String selected = Constants.productCategories1[i];
                            filterProductTV.setText(selected);
                            if (selected.equals("All")){
                                //load all
                                loadAllProducts();
                            }else{
                                //load filtered
                                loadFilteredProducts(selected);
                            }
                        }
                    }).show();
        });
        filterOrderBtn.setOnClickListener(view -> {
            //options to display in the dialog
            final String[] options = {"All", "In Progress", "Completed", "Cancelled"};
            //dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(MainSellerActivity.this);
            builder.setTitle("Filter Orders")
                    .setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            //handle item clicks
                            if (which == 0){
                                //All clicked
                                filteredOrdersTV.setText("Showing All Orders");
                                adapterOrderShop.getFilter().filter(""); //show all orders
                            }else{
                                String optionClicked = options[which];
                                filteredOrdersTV.setText("Showing " + optionClicked + " Orders"); //e.g. Showing Completed Orders
                                adapterOrderShop.getFilter().filter(optionClicked);
                            }
                        }
                    })
                    .show();
        });

        //popup menu
        PopupMenu popupMenu = new PopupMenu(MainSellerActivity.this,moreBtn);
        //add menu items to menu
        popupMenu.getMenu().add("Settings");
        popupMenu.getMenu().add("Reviews");
        popupMenu.getMenu().add("Logout");
        //handle menu items click
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle() == "Settings"){
                    //open setting screen
                    startActivity(new Intent(MainSellerActivity.this,SettingsActivity.class));
                }else if (menuItem.getTitle() == "Reviews"){
                    //open same reviews activity as used in the user main page
                    Intent intent = new Intent(MainSellerActivity.this,ShopReviewsActivity.class);
                    intent.putExtra("shopUid",""+auth.getUid());
                    startActivity(intent);
                }else if (menuItem.getTitle() == "Logout"){
                    //make offline
                    //sign out
                    //go to login form activity
                    makeMeOffline();
                }
                return true;
            }
        });
        moreBtn.setOnClickListener(view -> {
           popupMenu.show();
        });
    }

    private void loadAllOrders() {
        //init array list
        orderShopArrayList = new ArrayList<>();

        //load orders of the shop
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding new data in it
                        orderShopArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            //add to list
                            orderShopArrayList.add(modelOrderShop);
                        }
                        //setup adapter
                        adapterOrderShop = new AdapterOrderShop(MainSellerActivity.this,orderShopArrayList);
                        //set adapter to recycle view
                        orderRV.setAdapter(adapterOrderShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){

                            String productCategory = ""+ds.child("productCategory").getValue();
                            //if selected category matches the product category then add in the list
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }
                        }
                        //set adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //before getting reset list
                        productList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                        }
                        //set adapter
                        adapterProductSeller = new AdapterProductSeller(MainSellerActivity.this,productList);
                        //set adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void showProductsUI() {
        //show products ui and hide orders ui
        productsRL.setVisibility(View.VISIBLE);
        ordersRL.setVisibility(View.GONE);

        tabProductsTV.setTextColor(getResources().getColor(R.color.black));
        tabProductsTV.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTV.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showOrdersUI() {
        //show orders ui and hide products ui
        ordersRL.setVisibility(View.VISIBLE);
        productsRL.setVisibility(View.GONE);

        tabOrdersTV.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTV.setBackgroundResource(R.drawable.shape_rect04);

        tabProductsTV.setTextColor(getResources().getColor(R.color.white));
        tabProductsTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void makeMeOffline() {
        progressDialog.setMessage("Logging out ...");

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        //update value to db
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully
                        auth.signOut();
                        checkUser();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MainSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {
        FirebaseUser user=auth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainSellerActivity.this,LoginActivity.class));
            finish();
        }else{
            //load my info
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.orderByChild("uid").equalTo(auth.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()){
                                String name = ""+ds.child("name").getValue();
                                String accountType = ""+ds.child("accountType").getValue();
                                String email = ""+ds.child("email").getValue();
                                String shopName = ""+ds.child("shopName").getValue();
                                String profileImage = ""+ds.child("profileImage").getValue();

                                //nameTV.setText(name + "("+accountType+")");
                                nameTV.setText(name);
                                emailTV.setText(email);
                                shopNameTV.setText(shopName);
                                try {
                                    Picasso.get()
                                            .load(profileImage)
                                            .placeholder(R.drawable.ic_baseline_store_mall_directory_24)
                                            .into(profileIv);
                                }catch (Exception e){
                                    profileIv.setImageResource(R.drawable.ic_baseline_store_mall_directory_24);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}