package com.sahilssoft.sffoodie.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.R;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText mailEt,passwordEt;
    private TextView forgetTv,noAccountTv;
    private Button loginBtn;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mailEt=findViewById(R.id.mailEt);
        passwordEt=findViewById(R.id.passwordEt);
        forgetTv=findViewById(R.id.forgetTv);
        loginBtn=findViewById(R.id.loginBtn);
        noAccountTv=findViewById(R.id.noAccountTv);

        auth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);


        forgetTv.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this,ForgotPasswordActivity.class));
        });
        noAccountTv.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this,RegisterUserActivity.class));
        });
        loginBtn.setOnClickListener(view -> {
            loginUser();
        });

    }

    private String email,password;
    private void loginUser() {
        email=mailEt.getText().toString().trim();
        password=passwordEt.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging in ...");
        progressDialog.show();

        auth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //logged in successfully

                        //after logging in make user online
                        progressDialog.setMessage("Checking user ...");

                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("online","true");

                        //update value to db
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference.child(auth.getUid()).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //update successfully

                                        //check user type
                                        //if user is seller, start seller main screen
                                        //if user is buyer, start user main screen
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                        ref.orderByChild("uid").equalTo(auth.getUid())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot ds : snapshot.getChildren()){
                                                            String accountType = ""+ds.child("accountType").getValue();
                                                            if (accountType.equals("Seller")){
                                                                progressDialog.dismiss();
                                                                //user is seller
                                                                startActivity(new Intent(LoginActivity.this,MainSellerActivity.class));
                                                                finish();
                                                            }else{
                                                                progressDialog.dismiss();
                                                                //user is buyer
                                                                startActivity(new Intent(LoginActivity.this,MainUserActivity.class));
                                                                finish();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //login error
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}