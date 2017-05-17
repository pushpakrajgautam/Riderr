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

public class AutoCompleteAdapter extends ArrayAdapter implements Filterable
{
    private ArrayList<OptionName> optionNames;
    public AutoCompleteAdapter(Context context, int resource)
    {
        super(context, resource);
        optionNames = new ArrayList<>();
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
        nameView.setText(optionName.getName());
        nameView.setTypeface(font1);
        detailView.setText(optionName.getDesc());
        detailView.setTypeface(font1);
        imageView.setImageBitmap(null);
        return listView;
    }

    @NonNull
    @Override
    public Filter getFilter()
    {
        Filter filter = new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                FilterResults filterResults = new FilterResults();
                if (constraint != null)
                {
                    try
                    {
                        ArrayList<OptionName> nameArrayList = new AutoCompleteTask().execute(constraint.toString()).get();
                        filterResults.count = nameArrayList.size();
                        filterResults.values = nameArrayList;
                    }
                    catch (InterruptedException e) {}
                    catch (ExecutionException e) {}
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults results)
            {
                if (results != null && results.count > 0)
                {
                    optionNames = (ArrayList<OptionName>) results.values;
                    notifyDataSetChanged();
                }
                else
                    notifyDataSetInvalidated();
            }
        };
        return filter;
    }

    private class AutoCompleteTask extends AsyncTask<String,Void,ArrayList<OptionName>>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<OptionName> doInBackground(String... urls)
        {
            ArrayList<OptionName> names = new ArrayList<>();
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" +
                          urls[0] + "&key=AIzaSyDAc8Rzeb8RitUsXEUr7CTU-hc5EdAo4Xg";
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10,TimeUnit.SECONDS).writeTimeout(10,TimeUnit.SECONDS).build();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = null;
            try
            {
                response = client.newCall(request).execute();
                String responseBody = response.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                JSONArray jsonArray = jsonObject.getJSONArray("predictions");
                for(int i=0; i<jsonArray.length(); i++)
                {
                    String fullName = jsonArray.getJSONObject(i).getString("description");
                    String placeId = jsonArray.getJSONObject(i).getString("id");
                    String[] named = fullName.split(",");
                    String detaild = "";
                    for(int j=1; j<named.length; j++)
                    {
                        if(j!=named.length-1)
                            detaild += named[j] + ",";
                        else
                            detaild += named[j];
                    }
                    if(detaild.length()>1)
                        detaild = detaild.substring(1);
                    names.add(new OptionName(named[0], detaild, fullName, placeId));
                }

                return names;
            }
            catch (IOException e) {}
            catch (JSONException e) {}
            return names;
        }
    }
}
