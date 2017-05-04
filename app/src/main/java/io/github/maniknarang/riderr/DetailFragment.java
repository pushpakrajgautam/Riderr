package io.github.maniknarang.riderr;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.*;

import java.util.ArrayList;

public class DetailFragment extends Fragment
{
    private MapFragment mapFragment;
    private TextView nameView;
    private TextView routeView;
    private TextView timeView;
    private TextView orderView;
    private TextView time1;
    private TextView time2;
    private TextView time3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        Bundle bundle = getArguments();
        String[] details = bundle.getStringArray("stop_detail_head");
        String[] times = bundle.getStringArray("stop_detail_time");
        View view =inflater.inflate(R.layout.detail_frag,container,false);
        mapFragment = (MapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        Typeface font1 = Typeface.createFromAsset(getContext().getAssets(),"Raleway-SemiBold.ttf");
        Typeface font2 = Typeface.createFromAsset(getContext().getAssets(),"Montserrat-Regular.otf");
        Typeface font3 = Typeface.createFromAsset(getContext().getAssets(),"Chunkfive.otf");
        Typeface font4 = Typeface.createFromAsset(getContext().getAssets(),"Exo-Bold.otf");
        nameView = (TextView) view.findViewById(R.id.stop_name_frag);
        nameView.setTypeface(font1);
        routeView = (TextView) view.findViewById(R.id.route_no_frag);
        routeView.setTypeface(font2);
        orderView = (TextView) view.findViewById(R.id.order_no_frag);
        orderView.setTypeface(font3);
        nameView.setText(details[0]);
        routeView.setText(details[1]);
        orderView.setText(details[3]);

        if(details[4].equals("PURPLE"))
            orderView.setBackgroundResource(R.drawable.metro_line);
        else if(details[4].equals("GREEN"))
            orderView.setBackgroundResource(R.drawable.metro_line_green);

        time1 = (TextView) view.findViewById(R.id.time1);
        time2 = (TextView) view.findViewById(R.id.time2);
        time3 = (TextView) view.findViewById(R.id.time3);

        time1.setTypeface(font4);
        time2.setTypeface(font4);
        time3.setTypeface(font4);
        time1.setText(times[0]);
        time2.setText(times[1]);
        time3.setText(times[2]);

        return view;
    }
}
