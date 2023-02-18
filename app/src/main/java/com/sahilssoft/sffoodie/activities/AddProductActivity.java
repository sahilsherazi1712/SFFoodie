package com.sahilssoft.sffoodie.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sahilssoft.sffoodie.Constants;
import com.sahilssoft.sffoodie.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddProductActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private CircleImageView productIconIV;
    private EditText titleET,descriptionET;
    private TextView categoryTV,quantityET,priceET,discountPriceET
            ,discountNoteET;
    private SwitchCompat discountSwitch;
    private Button addProductBtn;

    private Uri imageUri;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        backBtn=findViewById(R.id.backBtn);
        productIconIV=findViewById(R.id.productIconIV);
        titleET=findViewById(R.id.titleET);
        descriptionET=findViewById(R.id.descriptionET);
        categoryTV=findViewById(R.id.categoryTV);
        quantityET=findViewById(R.id.quantityET);
        priceET=findViewById(R.id.priceET);
        discountSwitch=findViewById(R.id.discountSwitch);
        discountPriceET=findViewById(R.id.discountPriceET);
        discountNoteET=findViewById(R.id.discountNoteET);
        addProductBtn=findViewById(R.id.addProductBtn);

        // on start is unchecked, so hide discountPriceET and discountNoteET
        discountPriceET.setVisibility(View.GONE);
        discountNoteET.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // if discount switch is checked: show discountPriceEt, discountNote
        // if discount switch is not checked: hide discountPriceEt, discountNote
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    // checked: show discountPriceEt, discountNote
                    discountPriceET.setVisibility(View.VISIBLE);
                    discountNoteET.setVisibility(View.VISIBLE);
                }else{
                    // not checked: hide discountPriceEt, discountNote
                    discountPriceET.setVisibility(View.GONE);
                    discountNoteET.setVisibility(View.GONE);
                }
            }
        });
        backBtn.setOnClickListener(view -> {
            onBackPressed();
        });
        productIconIV.setOnClickListener(view -> {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    .start(this);
        });
        categoryTV.setOnClickListener(view -> {
            //pick category
            //categoryDialog();
            //dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Product Category")
                    .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //get picked category
                            String category = Constants.productCategories[i];

                            //set picked category
                            categoryTV.setText(category);
                        }
                    }).show();
        });
        addProductBtn.setOnClickListener(view -> {
            //Flow;
            // 1) Input data
            // 2) Validate data
            // 3) Add data to do
            inputData();
        });
    }

    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;
    private void inputData() {

        // 1) Input data
        productTitle=titleET.getText().toString().trim();
        productDescription=descriptionET.getText().toString().trim();
        productCategory=categoryTV.getText().toString().trim();
        productQuantity=quantityET.getText().toString().trim();
        originalPrice=priceET.getText().toString().trim();
        discountAvailable=discountSwitch.isChecked(); //true/false

        // 2) Validate data
        if (TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Category is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Price is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (discountAvailable){
            // product with discount
            discountPrice = discountPriceET.getText().toString().trim();
            discountNote = discountNoteET.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)){
                Toast.makeText(this, "Discount Price is required", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            //product without discount
            discountPrice = "0";
            discountNote = "";
        }

        addProduct();
    }
    private void addProduct() {
        // 3) Add data to do
        progressDialog.setMessage("Adding Product ...");
        progressDialog.show();

        String timestamp = ""+System.currentTimeMillis();

        if (imageUri == null){
            //upload without image

            //setup data to upload
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("productId", "" + timestamp);
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + productDescription);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("productIcon", ""); //no image, set empty
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("discountAvailable", "" + discountAvailable);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("uid", "" + auth.getUid());

            //add to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(auth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //added to db
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, "Product added ...", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed adding to db
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }else{
            //upload with image

            //first upload image to store

            //image and path of image to be upload
            String filePathAndName = "product_images/"+""+timestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //images uploaded get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){
                                //url of image uploaded
                                //setup data to upload
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productId", "" + timestamp);
                                hashMap.put("productTitle", "" + productTitle);
                                hashMap.put("productDescription", "" + productDescription);
                                hashMap.put("productCategory", "" + productCategory);
                                hashMap.put("productQuantity", "" + productQuantity);
                                hashMap.put("productIcon", "" + downloadImageUri); //set image
                                hashMap.put("originalPrice", "" + originalPrice);
                                hashMap.put("discountPrice", "" + discountPrice);
                                hashMap.put("discountNote", "" + discountNote);
                                hashMap.put("discountAvailable", "" + discountAvailable);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("uid", "" + auth.getUid());

                                //add to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(auth.getUid()).child("Products").child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //added to db
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, "Product added ...", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //failed adding to db
                                                progressDialog.dismiss();
                                                Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //images not uploaded
                            progressDialog.dismiss();
                            Toast.makeText(AddProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }
    private void clearData() {
        //clear data after uploading product
        titleET.setText("");
        descriptionET.setText("");
        categoryTV.setText("");
        quantityET.setText("");
        priceET.setText("");
        discountPriceET.setText("");
        discountNoteET.setText("");
        productIconIV.setImageResource(R.drawable.ic_add_cart_red);
        imageUri=null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                imageUri = result.getUri();
                productIconIV.setImageURI(imageUri);
                Toast.makeText(this, "Cropped Successfully!", Toast.LENGTH_SHORT).show();
            }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Toast.makeText(this, "Failed to Crop"+result.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}