package com.sahilssoft.sffoodie.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.sahilssoft.sffoodie.FilterProductUser;
import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.activities.ShopDetailsActivity;
import com.sahilssoft.sffoodie.models.ModelProduct;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterProductUser extends RecyclerView.Adapter<AdapterProductUser.HolderProductUser> implements Filterable {

    private Context context;
    public ArrayList<ModelProduct> productList,filterList;
    private FilterProductUser filter;

    public AdapterProductUser(Context context, ArrayList<ModelProduct> productList) {
        this.context = context;
        this.productList = productList;
        this.filterList = productList;
    }

    @NonNull
    @Override
    public AdapterProductUser.HolderProductUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_user,parent,false);
        return new HolderProductUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterProductUser.HolderProductUser holder, int position) {
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
        holder.titleTV.setText(title);
        holder.discountedNoteTV.setText(discountNote);
        holder.descriptionTV.setText(productDescription);
        holder.originalPriceTV.setText("$"+originalPrice);
        holder.discountedPriceTV.setText("$"+discountPrice);
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
        holder.addToCartTV.setOnClickListener(view -> {
            //add product to cart
            showQuantityDialog(modelProduct);
        });
        holder.itemView.setOnClickListener(view -> {
            //show product details

        });
    }

    private double cost = 0;
    private double finalCost = 0;
    private int quantity = 0;

    private void showQuantityDialog(ModelProduct modelProduct) {
        //inflate the layout for dialog
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity,null);
        //init layout views
        CircleImageView productIV = view.findViewById(R.id.productIV);
        TextView titleTV = view.findViewById(R.id.titleTV);
        TextView pQuantityTV = view.findViewById(R.id.pQuantityTV);
        TextView descriptionTV = view.findViewById(R.id.descriptionTV);
        TextView discountedNoteTV = view.findViewById(R.id.discountedNoteTV);
        TextView originalPriceTV = view.findViewById(R.id.originalPriceTV);
        TextView priceDiscountedTV = view.findViewById(R.id.priceDiscountedTV);
        TextView finalPriceTV = view.findViewById(R.id.finalPriceTV);
        ImageButton decrementBtn = view.findViewById(R.id.decrementBtn);
        TextView quantityTV = view.findViewById(R.id.quantityTV);
        ImageButton incrementBtn = view.findViewById(R.id.incrementBtn);
        Button continueBtn = view.findViewById(R.id.continueBtn);

        //get data from model
        String id = modelProduct.getProductId();
        String uid = modelProduct.getUid();
        String discountAvailable = modelProduct.getDiscountAvailable();
        String discountNote = modelProduct.getDiscountNote();
        String discountPrice = modelProduct.getDiscountPrice();
        String productCategory = modelProduct.getProductCategory();
        String productDescription = modelProduct.getProductDescription();
        String icon = modelProduct.getProductIcon();
        String productQuantity = modelProduct.getProductQuantity();
        String title = modelProduct.getProductTitle();
        String timestamp = modelProduct.getTimestamp();
        String originalPrice = modelProduct.getOriginalPrice();

        String price;
        if (modelProduct.getDiscountAvailable().equals("true")){
            //product have discount
            price = modelProduct.getDiscountPrice();
            discountedNoteTV.setVisibility(View.VISIBLE);
            originalPriceTV.setPaintFlags(originalPriceTV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //add strike through an original price
        }else{
            discountedNoteTV.setVisibility(View.GONE);
            priceDiscountedTV.setVisibility(View.GONE);
            price = modelProduct.getOriginalPrice();
        }

        cost = Double.parseDouble(price.replaceAll("$",""));
        finalCost = Double.parseDouble(price.replaceAll("$",""));
        quantity = 1;

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        //set data
        try{
            Picasso.get()
                    .load(icon)
                    .placeholder(R.drawable.ic_shopping_cart)
                    .into(productIV);
        }catch (Exception e){
            productIV.setImageResource(R.drawable.ic_shopping_cart);
        }
        titleTV.setText(""+title);
        pQuantityTV.setText(""+productQuantity);
        descriptionTV.setText(""+productDescription);
        discountedNoteTV.setText(""+discountNote);
        quantityTV.setText(""+quantity);
        originalPriceTV.setText("$"+modelProduct.getOriginalPrice());
        priceDiscountedTV.setText("$"+modelProduct.getDiscountPrice());
        finalPriceTV.setText("$"+finalCost);

        AlertDialog dialog = builder.create();
        dialog.show();

        //increase quantity of the product
        incrementBtn.setOnClickListener(view1 -> {
            finalCost = finalCost + cost;
            quantity++;

            finalPriceTV.setText("$"+finalCost);
            quantityTV.setText(""+quantity);
        });
        //decrease quantity of the product
        decrementBtn.setOnClickListener(view1 -> {
            if (quantity>1){
                finalCost = finalCost - cost;
                quantity--;

                finalPriceTV.setText("$"+finalCost);
                quantityTV.setText(""+quantity);
            }
        });

        continueBtn.setOnClickListener(view1 -> {
            String titles = titleTV.getText().toString().trim();
            String priceEach = price;
            String totalPrice = finalPriceTV.getText().toString().trim().replace("$","");
            String quantity = quantityTV.getText().toString().trim();


            //add to db(sqlite)
            addToCart(id,titles,priceEach,totalPrice,quantity);

            dialog.dismiss();
        });
    }

    private int itemId = 1;
    private void addToCart(String id, String titles, String priceEach, String prices, String quantity) {
        itemId++;

        EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                .setTableName("iTEMS_TABLE")
                .addColumn(new Column("Item_Id",new String[]{"text","unique"}))
                .addColumn("Item_PId",new String[]{"text","not null"})
                .addColumn("Item_Name",new String[]{"text","not null"})
                .addColumn("Item_Price_Each",new String[]{"text","not null"})
                .addColumn("Item_Price",new String[]{"text","not null"})
                .addColumn("Item_Quantity",new String[]{"text","not null"})
                .doneTableColumn();

        Boolean b = easyDB.addData("Item_Id",itemId)
                .addData("Item_PId",id)
                .addData("Item_Name",titles)
                .addData("Item_Price_Each",priceEach)
                .addData("Item_Price",prices)
                .addData("Item_Quantity",quantity)
                .doneDataAdding();

        Toast.makeText(context, "Added to Cart ...", Toast.LENGTH_SHORT).show();

        //update cart count
        ((ShopDetailsActivity)context).cartCount();

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterProductUser(this,filterList);
        }
        return filter;
    }

    public class HolderProductUser extends RecyclerView.ViewHolder {

        private ImageView productIconIV;
        private TextView discountedNoteTV,titleTV,descriptionTV,addToCartTV,discountedPriceTV,originalPriceTV;

        public HolderProductUser(@NonNull View itemView) {
            super(itemView);

            //init ui views
            productIconIV=itemView.findViewById(R.id.productIconIV);
            discountedNoteTV=itemView.findViewById(R.id.discountedNoteTV);
            titleTV=itemView.findViewById(R.id.titleTV);
            descriptionTV=itemView.findViewById(R.id.descriptionTV);
            addToCartTV=itemView.findViewById(R.id.addToCartTV);
            discountedPriceTV=itemView.findViewById(R.id.discountedPriceTV);
            originalPriceTV=itemView.findViewById(R.id.originalPriceTV);
        }
    }
}
