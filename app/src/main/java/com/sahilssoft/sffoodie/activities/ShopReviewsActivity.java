package com.sahilssoft.sffoodie.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.adapters.AdapterReview;
import com.sahilssoft.sffoodie.models.ModelReview;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShopReviewsActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private CircleImageView profileIV;
    private TextView shopNameTV,ratingTV;
    private RatingBar ratingBar;
    private RecyclerView reviewsRV;

    private FirebaseAuth auth;

    private ArrayList<ModelReview> reviewArrayList;
    private AdapterReview adapterReview;

    private String shopUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reviews);

        backBtn=findViewById(R.id.backBtn);
        profileIV=findViewById(R.id.profileIV);
        shopNameTV=findViewById(R.id.shopNameTV);
        ratingBar=findViewById(R.id.ratingBar);
        ratingTV=findViewById(R.id.ratingTV);
        reviewsRV=findViewById(R.id.reviewsRV);

        shopUid = getIntent().getStringExtra("shopUid");    //get shopUid from intent

        auth = FirebaseAuth.getInstance();
        loadShopDetails(); //for shop name, image
        loadReviews(); //for reviews list, avg ratings

        backBtn.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private float ratingSum = 0;
    private void loadReviews() {
        //init list
        reviewArrayList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //clear list before adding date into it
                        reviewArrayList.clear();
                        ratingSum = 0;
                        for (DataSnapshot ds : snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue()); //e.g. 4.3
                            ratingSum = ratingSum + rating; //for avg rating, add(addition of) all ratings, later will divide it by the number of reviews

                            ModelReview modelReview = ds.getValue(ModelReview.class);
                            reviewArrayList.add(modelReview);
                        }
                        //setup adapter
                        adapterReview = new AdapterReview(ShopReviewsActivity.this,reviewArrayList);
                        //set to recycle view
                        reviewsRV.setAdapter(adapterReview);

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratingSum/numberOfReviews;

                        ratingTV.setText(String.format("%.2f", avgRating) + " [" + numberOfReviews + "]");  //e.g. 4.7 [10]
                        ratingBar.setRating(avgRating);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(shopUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        //set data
                        shopNameTV.setText(shopName);
                        try {
                            Picasso.get()
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_baseline_person_24)
                                    .into(profileIV);
                        }catch (Exception e) {
                            profileIV.setImageResource(R.drawable.ic_baseline_store_mall_directory_24);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}