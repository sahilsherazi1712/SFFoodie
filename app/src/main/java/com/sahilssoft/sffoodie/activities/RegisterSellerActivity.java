package com.sahilssoft.sffoodie.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sahilssoft.sffoodie.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterSellerActivity extends AppCompatActivity implements LocationListener {
    
    ImageButton backBtn,gpsBtn;
    CircleImageView profileIv;
    EditText nameEt,shopNameEt,phoneEt,deliveryFeeEt,countryEt,stateEt
            ,cityEt,addressEt,mailEt,passwordEt,cPasswordEt;
    Button RegisterBtn;
    TextView registerUserTv;
    
    //permission constraints
    private static final int LOCATION_REQUEST_CODE = 100;

    //permission arrays;
    private String[] locationPermissions;

    private double latitude=0.0, longitude=0.0;
    LocationManager locationManager;

    //image picked uri
    private Uri mCropImageUri;
    private Uri imageUri;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_seller);

        backBtn=findViewById(R.id.backBtn);
        gpsBtn=findViewById(R.id.gpsBtn);
        profileIv=findViewById(R.id.profileIv);
        nameEt=findViewById(R.id.nameEt);
        shopNameEt=findViewById(R.id.shopNameEt);
        phoneEt=findViewById(R.id.phoneEt);
        deliveryFeeEt=findViewById(R.id.deliveryFeeEt);
        countryEt=findViewById(R.id.countryEt);
        stateEt=findViewById(R.id.stateEt);
        cityEt=findViewById(R.id.cityEt);
        addressEt=findViewById(R.id.addressEt);
        mailEt=findViewById(R.id.mailEt);
        passwordEt=findViewById(R.id.passwordEt);
        cPasswordEt=findViewById(R.id.cPasswordEt);
        RegisterBtn=findViewById(R.id.RegisterBtn);
        registerUserTv=findViewById(R.id.registerUserTv);
        
        //init permissions array
        locationPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        auth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(view -> onBackPressed());
        gpsBtn.setOnClickListener(view -> {
            //todo: detect seller location
            if (checkLocationPermission()){
                //already allowed
                detectLocation();
            }else{
                //not allowed, request
                requestLocationPermission();
            }
        });
        profileIv.setOnClickListener(view -> {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    .start(this);
        });
        RegisterBtn.setOnClickListener(view -> {
            inputData();
        });
        registerUserTv.setOnClickListener(view ->
                startActivity(new Intent(RegisterSellerActivity.this,RegisterUserActivity.class)));
    }

    private String fullName, shopName, phoneNumber,deliveryFee, country, state, city, address, email, password, confirmPassword;
    private void inputData() {
        fullName=nameEt.getText().toString().trim();
        shopName=shopNameEt.getText().toString().trim();
        phoneNumber=phoneEt.getText().toString().trim();
        deliveryFee=deliveryFeeEt.getText().toString().trim();
        country=countryEt.getText().toString().trim();
        state=stateEt.getText().toString().trim();
        city=cityEt.getText().toString().trim();
        address=addressEt.getText().toString().trim();
        email=mailEt.getText().toString().trim();
        password=passwordEt.getText().toString().trim();
        confirmPassword=cPasswordEt.getText().toString().trim();

        //valid data
        if (TextUtils.isEmpty(fullName)){
            Toast.makeText(this, "Enter Your Name ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(shopName)) {
            Toast.makeText(this, "Enter Shop Name ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(deliveryFee)) {
            Toast.makeText(this, "Enter Delivery Fee ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter Your Phone Number ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(this, "Please click on GSP button to detect your location...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Pattern ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length()<6) {
            Toast.makeText(this, "Please Enter a Password of Length atleast 6 ...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password does not match ...", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();
    }

    private void createAccount() {
        progressDialog.setMessage("Creating Account ...");
        progressDialog.show();

        //creating account
        auth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account created
                        progressDialog.setMessage("Saving Account Info ...");

                        String timestamp = ""+System.currentTimeMillis();

                        if (imageUri == null){
                            //save data without image

                            //setup data to save
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("uid",""+auth.getUid());
                            hashMap.put("email",""+email);
                            hashMap.put("name",""+fullName);
                            hashMap.put("shopName",""+shopName);
                            hashMap.put("phone",""+phoneNumber);
                            hashMap.put("deliveryFee",""+deliveryFee);
                            hashMap.put("country",""+country);
                            hashMap.put("state",""+state);
                            hashMap.put("city",""+city);
                            hashMap.put("address",""+address);
                            hashMap.put("latitude",""+latitude);
                            hashMap.put("longitude",""+longitude);
                            hashMap.put("timestamp",""+timestamp);
                            hashMap.put("accountType","Seller");
                            hashMap.put("online","true");
                            hashMap.put("shopOpen","true");
                            hashMap.put("profileImage","");

                            //save to db
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.child(auth.getUid()).setValue(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //db updated
                                            progressDialog.dismiss();
                                            startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //failed updating db
                                            progressDialog.dismiss();
                                            startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                                            finish();
                                        }
                            });
                        }else{
                            //save info with image

                            //name and path of image
                            String filePathAndName = "profile_images/" + "" + auth.getUid();
                            //upload image
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
                            storageReference.putFile(imageUri)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            //get url of uploaded image
                                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                            while (!uriTask.isSuccessful());

                                            Uri downloadImageUri = uriTask.getResult();

                                            if (uriTask.isSuccessful()){
                                                //setup data to save
                                                HashMap<String,Object> hashMap = new HashMap<>();
                                                hashMap.put("uid",""+auth.getUid());
                                                hashMap.put("email",""+email);
                                                hashMap.put("name",""+fullName);
                                                hashMap.put("shopName",""+shopName);
                                                hashMap.put("phone",""+phoneNumber);
                                                hashMap.put("deliveryFee",""+deliveryFee);
                                                hashMap.put("country",""+country);
                                                hashMap.put("state",""+state);
                                                hashMap.put("city",""+city);
                                                hashMap.put("address",""+address);
                                                hashMap.put("latitude",""+latitude);
                                                hashMap.put("longitude",""+longitude);
                                                hashMap.put("timestamp",""+timestamp);
                                                hashMap.put("accountType","Seller");
                                                hashMap.put("online","true");
                                                hashMap.put("shopOpen","true");
                                                hashMap.put("profileImage",""+downloadImageUri); //uri of uploaded image

                                                //save to db
                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                                reference.child(auth.getUid()).setValue(hashMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                //db updated
                                                                progressDialog.dismiss();
                                                                startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                                                                finish();
                                                            }
                                                        })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        //failed updating db
                                                        progressDialog.dismiss();
                                                        startActivity(new Intent(RegisterSellerActivity.this,MainSellerActivity.class));
                                                        finish();
                                                    }
                                                });
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed creating account
                        progressDialog.dismiss();
                        Toast.makeText(RegisterSellerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void detectLocation() {
        Toast.makeText(this, "Please wait ...", Toast.LENGTH_SHORT).show();
        
        locationManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
    }

    private boolean checkLocationPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return result;
    }

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(this,locationPermissions,LOCATION_REQUEST_CODE);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //location detected
        latitude=location.getLatitude();
        longitude=location.getLongitude();
        findAddress();
    }

    private void findAddress() {
        //find address,country,state,city

        Geocoder geocoder=new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses=geocoder.getFromLocation(latitude,longitude,1);

            String address = addresses.get(0).getAddressLine(0);  //complete address
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            //set address
            countryEt.setText(country);
            cityEt.setText(city);
            stateEt.setText(state);
            addressEt.setText(address);

        }catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
//        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        //gps//location disabled
        Toast.makeText(this, "Please Enable Your Location", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (locationAccepted){
                        //permission allowed
                        detectLocation();
                    }else{
                        //permission denied
                        Toast.makeText(this, "Location permission is necessary", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                imageUri = result.getUri();
                profileIv.setImageURI(imageUri);
                Toast.makeText(this, "Cropped Successfully!", Toast.LENGTH_SHORT).show();
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(this, "Failed to Crop"+result.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}