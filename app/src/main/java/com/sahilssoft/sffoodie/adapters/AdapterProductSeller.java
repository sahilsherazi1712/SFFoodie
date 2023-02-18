package com.sahilssoft.sffoodie.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sahilssoft.sffoodie.FilterProduct;
import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.activities.EditProductActivity;
import com.sahilssoft.sffoodie.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterProductSeller extends RecyclerView.Adapter<AdapterProductSeller.HolderProductSeller> implements            Filterable {

    private Context context;
    public ArrayList<ModelProduct> productList, filterList;

    private FilterProduct filter;

    public AdapterProductSeller(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public HolderProductSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_seller,parent,false);
        return new HolderProductSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProductSeller holder, int position) {
        // get data
        ModelProduct modelProduct = productList.get(position);
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        //set data
        holder.discountedNoteTV.setText(discountNote);
        holder.titleTV.setText(title);
        holder.quantityTV.setText(quantity);
        holder.discountedPriceTV.setText("$" + discountPrice);
        holder.originalPriceTV.setText("$" + originalPrice);

        if (discountAvailable.equals("true")){
            //product is on discount
            holder.discountedNoteTV.setVisibility(View.VISIBLE);
            holder.discountedPriceTV.setVisibility(View.VISIBLE);
            holder.originalPriceTV.setPaintFlags(holder.originalPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike through an original price
        }else{
            //product is not on discount
            holder.discountedNoteTV.setVisibility(View.GONE);
            holder.discountedPriceTV.setVisibility(View.GONE);
            holder.originalPriceTV.setPaintFlags(0);
        }
        try {
            Picasso.get()
                    .load(icon)
                    .placeholder(R.drawable.ic_add_cart_red)
                    .into(holder.productIconIV);
        }catch (Exception e){
            holder.productIconIV.setImageResource(R.drawable.ic_add_cart_red);
        }

        holder.itemView.setOnClickListener(view -> {
            //handle item clicks, show item details (in bottom sheet)
            detailsBottomSheet(modelProduct); // here modelProduct contains details of clicked item
        });
    }

    private void detailsBottomSheet(ModelProduct modelProduct) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view1 for bottomsheet
        View view1 = LayoutInflater.from(context).inflate(R.layout.bs_product_details_seller,null);
        //set view1 to bottom sheet
        bottomSheetDialog.setContentView(view1);

        //init view1 of bottom sheet
        ImageButton backBtn = view1.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view1.findViewById(R.id.deleteBtn);
        ImageButton editBtn = view1.findViewById(R.id.editBtn);
        ImageView productIconIV = view1.findViewById(R.id.productIconIV);
        TextView discountNoteTV = view1.findViewById(R.id.discountNoteTV);
        TextView titleTV = view1.findViewById(R.id.titleTV);
        TextView descriptionTV = view1.findViewById(R.id.descriptionTV);
        TextView categoryTV = view1.findViewById(R.id.categoryTV);
        TextView quantityTV = view1.findViewById(R.id.quantityTV);
        TextView discountedPriceTV = view1.findViewById(R.id.discountedPriceTV);
        TextView originalPriceTV = view1.findViewById(R.id.originalPriceTV);

        // get data
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String quantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        //set data
        titleTV.setText(title);
        discountNoteTV.setText(discountNote);
        descriptionTV.setText(productDescription);
        categoryTV.setText(productCategory);
        quantityTV.setText(quantity);
        discountedPriceTV.setText("$"+discountPrice);
        originalPriceTV.setText("$"+originalPrice);
        if (discountAvailable.equals("true")){
            //product is on discount
            discountedPriceTV.setVisibility(View.VISIBLE);
            discountNoteTV.setVisibility(View.VISIBLE);
            originalPriceTV.setPaintFlags(originalPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);  //add strike through on original price
        }else{
            //product is not on discount
            discountedPriceTV.setVisibility(View.GONE);
            discountNoteTV.setVisibility(View.GONE);
        }
        try {
            Picasso.get()
                    .load(icon)
                    .placeholder(R.drawable.ic_add_cart_red)
                    .into(productIconIV);
        }catch (Exception e){
            productIconIV.setImageResource(R.drawable.ic_add_cart_red);
        }

        //show dialog
        bottomSheetDialog.show();

        //edit click
        editBtn.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            //open edit product activity, pass id of product
            Intent intent = new Intent(context, EditProductActivity.class);
            intent.putExtra("productId",id);
            context.startActivity(intent);

        });
        //delete click
        deleteBtn.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            //show delete confirm dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete")
                    .setMessage("Are you sure to want to delete the product "+title+" ?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //delete
                            FirebaseAuth auth = FirebaseAuth.getInstance();
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                            reference.child(auth.getUid()).child("Products").child(id).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //product deleted
                                            Toast.makeText(context, "Product Deleted ...", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //failed deleting product
                                            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //cancel, dismiss dialog
                            dialogInterface.dismiss();
                        }
                    }).show();
        });
        //back click
        backBtn.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
         });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProduct(this,filterList);
        }
        return filter;
    }

    class HolderProductSeller extends RecyclerView.ViewHolder{

        private ImageView productIconIV;
        private TextView discountedNoteTV,titleTV,quantityTV,discountedPriceTV,originalPriceTV;
        private ImageView nextIV;

        public HolderProductSeller(@NonNull View itemView) {
            super(itemView);

            productIconIV=itemView.findViewById(R.id.productIconIV);
            discountedNoteTV=itemView.findViewById(R.id.discountedNoteTV);
            titleTV=itemView.findViewById(R.id.titleTV);
            quantityTV=itemView.findViewById(R.id.quantityTV);
            discountedPriceTV=itemView.findViewById(R.id.discountedPriceTV);
            originalPriceTV=itemView.findViewById(R.id.originalPriceTV);
            nextIV=itemView.findViewById(R.id.nextIV);

        }
    }
}
