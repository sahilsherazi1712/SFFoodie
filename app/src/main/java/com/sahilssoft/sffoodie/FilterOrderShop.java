package com.sahilssoft.sffoodie;

import android.widget.Filter;

import com.sahilssoft.sffoodie.adapters.AdapterOrderShop;
import com.sahilssoft.sffoodie.adapters.AdapterProductSeller;
import com.sahilssoft.sffoodie.models.ModelOrderShop;
import com.sahilssoft.sffoodie.models.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if (constraint != null && constraint.length()>0){
            //searching filed not empty, searching something, perform search

            //change to uppser case to make case intensive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelOrderShop> filterModels = new ArrayList<>();
            for (int i=0; i< filterList.size();i++){
                //check search by title and category
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){
                    //add filtered data to list
                    filterModels.add(filterList.get(i));
                }
            }
            results.count = filterModels.size();
            results.values = filterModels;
        }else{
            //search filed empty, not searching, return original/all/complete list
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapter.orderShopArrayList = (ArrayList<ModelOrderShop>) filterResults.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
