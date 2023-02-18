package com.sahilssoft.sffoodie.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.R;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();

        //start login activity after 2sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = auth.getCurrentUser();
                if (user == null){
                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    finish();
                }else{
                    //user is already logged in, check user type
                    //if user is seller, start seller main screen
                    //if user is buyer, start user main screen
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.child(auth.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String accountType = ""+snapshot.child("accountType").getValue();
                                    if (accountType.equals("Seller")){
                                        //user is seller
                                        startActivity(new Intent(SplashActivity.this,MainSellerActivity.class));
                                        finish();
                                    }else{
                                        //user is buyer
                                        startActivity(new Intent(SplashActivity.this,MainUserActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }
        },2000);
    }
}