package com.sahilssoft.sffoodie.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class WriteReviewActivity extends AppCompatActivity {

    private String shopUid;

    private ImageButton backBtn;
    private CircleImageView profileIV;
    private TextView shopNameTV;
    private RatingBar ratingBar;
    private EditText reviewET;
    private FloatingActionButton submitBtn;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        backBtn = findViewById(R.id.backBtn);
        profileIV = findViewById(R.id.profileIV);
        shopNameTV = findViewById(R.id.shopNameTV);
        ratingBar = findViewById(R.id.ratingBar);
        reviewET = findViewById(R.id.reviewET);
        submitBtn = findViewById(R.id.submitBtn);

        shopUid = getIntent().getStringExtra("shopUid"); //to write review we need the id of the shop in which user ordered

        auth = FirebaseAuth.getInstance();
        //if user has written review to this shop, load it
        loadMyReview();
        //load shop info: shop name, shop image
        loadShopInfo();

        backBtn.setOnClickListener(view -> {
            onBackPressed();
        });
        //input data
        submitBtn.setOnClickListener(view -> {
            inputData();
        });
    }

    private void loadShopInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //get shop info
                String shopName = ""+snapshot.child("shopName").getValue();
                String shopImage = ""+snapshot.child("profileImage").getValue();

                // set shop info to ui
                shopNameTV.setText(shopName);
                try {
                    Picasso.get()
                            .load(shopImage)
                            .placeholder(R.drawable.ic_baseline_store_mall_directory_24)
                            .into(profileIV);
                }catch (Exception e){
                    profileIV.setImageResource(R.drawable.ic_baseline_store_mall_directory_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMyReview() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(auth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            //my review is available to this shop

                            //get review details
                            String uid = ""+snapshot.child("uid").getValue();
                            String ratings = ""+snapshot.child("ratings").getValue();
                            String review = ""+snapshot.child("review").getValue();
                            String timestamp = ""+snapshot.child("timestamp").getValue();

                            //set review details to our ui
                            float myRating = Float.parseFloat(ratings);
                            ratingBar.setRating(myRating);
                            reviewET.setText(review);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void inputData() {
        String ratings = "" + ratingBar.getRating();
        String review = reviewET.getText().toString().trim();

        //for time of review
        String timestamp = ""+System.currentTimeMillis();

        //setup data in hashmap
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + auth.getUid());
        hashMap.put("ratings", "" + ratings);  //e.g. 4.6
        hashMap.put("review", "" + review);  //e.g. Good News
        hashMap.put("timestamp", "" + timestamp);

        //put to db: DB > Users > ShopUid > Ratings
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings").child(auth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //review added to db
                        Toast.makeText(WriteReviewActivity.this, "Review Published Successfully ...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed adding review to db
                        Toast.makeText(WriteReviewActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}