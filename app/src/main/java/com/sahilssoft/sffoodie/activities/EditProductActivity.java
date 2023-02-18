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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sahilssoft.sffoodie.Constants;
import com.sahilssoft.sffoodie.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProductActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private CircleImageView productIconIV;
    private EditText titleET,descriptionET;
    private TextView categoryTV,quantityET,priceET,discountPriceET
            ,discountNoteET;
    private SwitchCompat discountSwitch;
    private Button updateProductBtn;
    
    private String productId;

    private Uri imageUri;

    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

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
        updateProductBtn=findViewById(R.id.updateProductBtn);
        
        //get product id from intent
        productId = getIntent().getStringExtra("productId");

        // on start is unchecked, so hide discountPriceET and discountNoteET
        discountPriceET.setVisibility(View.GONE);
        discountNoteET.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        
        loadProductDetails();  //to se on view

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
        updateProductBtn.setOnClickListener(view -> {
            //Flow;
            // 1) Input data
            // 2) Validate data
            // 3) Add data to do
            inputData();
        });
    }

    private void loadProductDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(auth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get data
                        String productId = "" + snapshot.child("productId").getValue();
                        String productTitle = "" + snapshot.child("productTitle").getValue();
                        String productDescription = "" + snapshot.child("productDescription").getValue();
                        String productCategory = "" + snapshot.child("productCategory").getValue();
                        String productQuantity = "" + snapshot.child("productQuantity").getValue();
                        String productIcon = "" + snapshot.child("productIcon").getValue();
                        String originalPrice = "" + snapshot.child("originalPrice").getValue();
                        String discountPrice = "" + snapshot.child("discountPrice").getValue();
                        String discountNote = "" + snapshot.child("discountNote").getValue();
                        String discountAvailable = "" + snapshot.child("discountAvailable").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();

                        //set data
                        if (discountAvailable.equals("true")){
                            discountSwitch.setChecked(true);

                            discountPriceET.setVisibility(View.VISIBLE);
                            discountNoteET.setVisibility(View.VISIBLE);
                        }else{
                            discountSwitch.setChecked(false);

                            discountPriceET.setVisibility(View.GONE);
                            discountNoteET.setVisibility(View.GONE);
                        }

                        titleET.setText(productTitle);
                        descriptionET.setText(productDescription);
                        categoryTV.setText(productCategory);
                        discountNoteET.setText(discountNote);
                        quantityET.setText(productQuantity);
                        priceET.setText(originalPrice);
                        discountPriceET.setText(discountPrice);
                        try {
                            Picasso.get()
                                    .load(productIcon)
                                    .placeholder(R.drawable.ic_add_cart_white)
                                    .into(productIconIV);
                        }catch (Exception e){
                            productIconIV.setImageResource(R.drawable.ic_add_cart_white);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
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

        updateProduct();
    }

    private void updateProduct() {
        //show dialog
        progressDialog.setMessage("Updating ...");
        progressDialog.show();

        if (imageUri == null){
            //without image
            //setup data to hashmap to update
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + productDescription);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            //here image empty icon is not needed otherwise pic of product during updating will be removed
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("discountAvailable", "" + discountAvailable);

            //update to db
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(auth.getUid()).child("Products").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Updated ...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }else{
            //update with image
            //first upload image
            //image name and path on firebase storage
            String filePathAndName = "product_images/" + "" + productId; //override previous image using same id
            //upload image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded, get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){
                                //setup data to hashmap to update
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("productTitle", "" + productTitle);
                                hashMap.put("productDescription", "" + productDescription);
                                hashMap.put("productCategory", "" + productCategory);
                                hashMap.put("productQuantity", "" + productQuantity);
                                hashMap.put("productIcon", "" + downloadImageUri);
                                hashMap.put("originalPrice", "" + originalPrice);
                                hashMap.put("discountPrice", "" + discountPrice);
                                hashMap.put("discountNote", "" + discountNote);
                                hashMap.put("discountAvailable", "" + discountAvailable);

                                //update to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(auth.getUid()).child("Products").child(productId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Updated ...", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                productIconIV.setImageURI(imageUri);
                Toast.makeText(this, "Cropped Successfully!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Failed to Crop" + result.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}