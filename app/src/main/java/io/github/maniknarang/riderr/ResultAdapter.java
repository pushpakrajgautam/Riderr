package io.github.maniknarang.riderr;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kingpushpakraj on 01-04-2017.
 */

public class ResultAdapter extends ArrayAdapter<Result>
{
    public ResultAdapter(Context context, ArrayList<Result> resource)
    {
        super(context, 0, resource);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if(view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.result_list,parent,false);
        Result result = getItem(position);
        TextView distance = (TextView) view.findViewById(R.id.result_dist);
        distance.setText(result.distance);
        TextView duration = (TextView) view.findViewById(R.id.result_duration);
        duration.setText(result.duration);
        TextView image = (TextView) view.findViewById(R.id.result_image);
        image.setText(result.travelMode);
        //Image to be added
        return view;
    }
}
