package com.sahilssoft.sffoodie.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sahilssoft.sffoodie.R;
import com.sahilssoft.sffoodie.models.ModelReview;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterReview extends RecyclerView.Adapter<AdapterReview.HolderReview> {

    private Context context;
    private ArrayList<ModelReview> reviewArrayList;

    public AdapterReview(Context context, ArrayList<ModelReview> reviewArrayList) {
        this.context = context;
        this.reviewArrayList = reviewArrayList;
    }

    @NonNull
    @Override
    public HolderReview onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_reviews,parent,false);
        return new HolderReview(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReview holder, int position) {
        //get data at position
        ModelReview modelReview = reviewArrayList.get(position);
        String uid = modelReview.getUid();
        String ratings = modelReview.getRatings();
        String review = modelReview.getReview();
        String timestamp = modelReview.getTimestamp();

        //we also need info (profile image, name) of user who wrote the review, we can do it using uid of the user
        loadUserDetail(modelReview,holder);

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String dateFormat = DateFormat.format("dd/MM/yyyy",calendar).toString();

        //set data
        holder.dateTV.setText(dateFormat);
        holder.reviewTV.setText(review);
        holder.ratingBar.setRating(Float.parseFloat(ratings));
    }

    private void loadUserDetail(ModelReview modelReview, HolderReview holder) {
        // uid of the user who wrote review
        String uid = modelReview.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get user info, use same key names as in firebase
                        String name = ""+snapshot.child("name").getValue();
                        String profileImage = ""+snapshot.child("profileImage").getValue();

                        //set data
                        holder.nameTV.setText(name);
                        try {
                            Picasso.get()
                                    .load(profileImage)
                                    .placeholder(R.drawable.ic_baseline_store_mall_directory_24)
                                    .into(holder.profileIV);
                        }catch (Exception e){
                            holder.profileIV.setImageResource(R.drawable.ic_baseline_store_mall_directory_24);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return reviewArrayList.size();
    }

    public class HolderReview extends RecyclerView.ViewHolder {

        private CircleImageView profileIV;
        private TextView nameTV,dateTV,reviewTV;
        private RatingBar ratingBar;

        public HolderReview(@NonNull View itemView) {
            super(itemView);

            profileIV = itemView.findViewById(R.id.profileIV);
            nameTV = itemView.findViewById(R.id.nameTV);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            dateTV = itemView.findViewById(R.id.dateTV);
            reviewTV = itemView.findViewById(R.id.reviewTV);

        }
    }
}
