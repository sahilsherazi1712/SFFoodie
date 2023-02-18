package com.sahilssoft.sffoodie.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.adapters.AdapterOrderUser;
import com.sahilssoft.sffoodie.adapters.AdapterShop;
import com.sahilssoft.sffoodie.models.ModelOrderUser;
import com.sahilssoft.sffoodie.models.ModelShop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainUserActivity extends AppCompatActivity {

    private TextView nameTV,emailTV,phoneTV,tabShopsTV,tabOrdersTV;
    private ImageButton logoutBtn,editProfileBtn,settingsBtn;
    private CircleImageView profileIv;
    private RelativeLayout shopsRL,ordersRL;
    private RecyclerView shopsRV,orderRV;

    private FirebaseAuth auth;

    private ProgressDialog progressDialog;

    private ArrayList<ModelShop> shopsList;
    private AdapterShop adapterShop;

    private ArrayList<ModelOrderUser> orderList;
    private AdapterOrderUser adapterOrderUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        nameTV=findViewById(R.id.nameTV);
        logoutBtn=findViewById(R.id.logoutBtn);
        editProfileBtn=findViewById(R.id.editProfileBtn);
        profileIv=findViewById(R.id.profileIv);
        emailTV=findViewById(R.id.emailTV);
        phoneTV=findViewById(R.id.phoneTV);
        tabShopsTV=findViewById(R.id.tabShopsTV);
        tabOrdersTV=findViewById(R.id.tabOrdersTV);
        shopsRL=findViewById(R.id.shopsRL);
        ordersRL=findViewById(R.id.ordersRL);
        shopsRV=findViewById(R.id.shopsRV);
        orderRV=findViewById(R.id.orderRV);
        settingsBtn=findViewById(R.id.settingsBtn);

        auth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        checkUser();

        //at first show shops ui
        showShopsUI();

        logoutBtn.setOnClickListener(view -> {
            //make offline
            //sign out
            //go to login form activity
            makeMeOffline();
        });
        editProfileBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainUserActivity.this,ProfileEditUserActivity.class));
        });
        tabShopsTV.setOnClickListener(view -> {
            //show shops
            showShopsUI();
        });
        tabOrdersTV.setOnClickListener(view -> {
            //show orders
            showOrdersUI();
        });
        settingsBtn.setOnClickListener(view -> {
            startActivity(new Intent(MainUserActivity.this,SettingsActivity.class));
        });
    }

    private void showShopsUI() {
        //show shops ui, hide orders ui
        shopsRL.setVisibility(View.VISIBLE);
        ordersRL.setVisibility(View.GONE);

        tabShopsTV.setTextColor(getResources().getColor(R.color.black));
        tabShopsTV.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTV.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showOrdersUI() {
        //show orders ui, hide shopes ui
        ordersRL.setVisibility(View.VISIBLE);
        shopsRL.setVisibility(View.GONE);

        tabOrdersTV.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTV.setBackgroundResource(R.drawable.shape_rect04);

        tabShopsTV.setTextColor(getResources().getColor(R.color.white));
        tabShopsTV.setBackgroundColor(getResources().getColor(android.R.color.transparent));
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
                Toast.makeText(MainUserActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {
        FirebaseUser user=auth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainUserActivity.this,LoginActivity.class));
            finish();
        }else{
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
                                String phone = ""+ds.child("phone").getValue();
                                String profileImage = ""+ds.child("profileImage").getValue();
                                String accountType = ""+ds.child("accountType").getValue();
                                String city = ""+ds.child("city").getValue();

                                //nameTV.setText(name + "("+accountType+")");
                                //set user data
                                nameTV.setText(name);
                                emailTV.setText(email);
                                phoneTV.setText(phone);
                                try {
                                    Picasso.get()
                                            .load(profileImage)
                                            .placeholder(R.drawable.ic_baseline_person_24)
                                            .into(profileIv);
                                }catch (Exception e){
                                    profileIv.setImageResource(R.drawable.ic_baseline_person_24);
                                }

                                //load only those shops that are in the city of user
                                loadShops(city);
                                loadOrders();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    private void loadOrders() {
        //init order list
        orderList = new ArrayList<>();

        //get orders
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot ds : snapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    reference1.orderByChild("orderBy").equalTo(auth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        for (DataSnapshot ds : snapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                            //add to list
                                            orderList.add(modelOrderUser);
                                        }
                                        //setup adapter
                                        adapterOrderUser = new AdapterOrderUser(MainUserActivity.this,orderList);
                                        //set to recyclerview
                                        orderRV.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops(String myCity) {
        //init list
        shopsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding
                        shopsList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelShop modelShop = ds.getValue(ModelShop.class);

                            String shopCity = ""+ds.child("city").getValue();

                            //show only user city shops
                            if (shopCity.equals(myCity)){
                                shopsList.add(modelShop);
                            }
                            //if you want to display all shops, skip the if statement and add this
                            //shopsList.add(modelShop);
                        }
                        //setup adapter
                        adapterShop = new AdapterShop(MainUserActivity.this,shopsList);
                        //set adapter to recyclerview
                        shopsRV.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}