package io.github.maniknarang.riderr;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AutoCompleteAdapter extends ArrayAdapter
{
    private ArrayList<OptionName> optionNames;
    public AutoCompleteAdapter(Context context, int resource,ArrayList<OptionName> optionNames)
    {
        super(context, resource);
        this.optionNames = optionNames;
    }

    @Override
    public int getCount() {return optionNames.size();}

    @Nullable
    @Override
    public OptionName getItem(int position) {return optionNames.get(position);}

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Typeface font1 = Typeface.createFromAsset(getContext().getAssets(),"Raleway-SemiBold.ttf");
        View listView = convertView;
        if(listView == null)
            listView = LayoutInflater.from(getContext()).inflate(R.layout.complete_list_item, parent, false);
        OptionName optionName = getItem(position);
        TextView nameView = (TextView) listView.findViewById(R.id.name_view);
        TextView detailView = (TextView) listView.findViewById(R.id.detail_view);
        ImageView imageView = (ImageView) listView.findViewById(R.id.option_image);
        if(optionName.getSymbol().equals("bus"))
            imageView.setImageResource(R.drawable.ic_directions_bus_black_24dp);
        else if(optionName.getSymbol().equals("metro"))
            imageView.setImageResource(R.drawable.ic_directions_subway_black_24dp);
        else
            imageView.setImageResource(R.drawable.ic_place_black1_24dp);
        nameView.setText(optionName.getName());
        nameView.setTypeface(font1);
        detailView.setText(optionName.getDesc());
        detailView.setTypeface(font1);
        return listView;
    }

    public void clear()
    {
        optionNames.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<OptionName> list)
    {
        optionNames.addAll(list);
        notifyDataSetChanged();
    }

}
