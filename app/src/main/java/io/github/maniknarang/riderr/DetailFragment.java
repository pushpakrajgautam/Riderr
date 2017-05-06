package io.github.maniknarang.riderr;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class DetailFragment extends Fragment
{
    private MapFragment mapFragment;
    private MapActivity mapActivity;
    private TextView nameView;
    private TextView routeView;
    private TextView timeView;
    private TextView orderView;
    private TextView opporderView;
    private TextView time1;
    private TextView time2;
    private TextView time3;
    private TextView otime1;
    private TextView otime2;
    private TextView otime3;
    private LinearLayout orderList;
    private TextView notificatText;
    private TextView favText;
    private LinearLayout detailFragHead;
    public int heightr;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        Bundle bundle = getArguments();
        String[] details = bundle.getStringArray("stop_detail_head");
        String[] times = bundle.getStringArray("stop_detail_time");
        String[] otimes = bundle.getStringArray("stop_detail_opp_time");
        View view =inflater.inflate(R.layout.detail_frag,container,false);
        mapFragment = (MapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        Typeface font1 = Typeface.createFromAsset(getContext().getAssets(),"Raleway-SemiBold.ttf");
        Typeface font2 = Typeface.createFromAsset(getContext().getAssets(),"Montserrat-Regular.otf");
        Typeface font3 = Typeface.createFromAsset(getContext().getAssets(),"Chunkfive.otf");
        Typeface font4 = Typeface.createFromAsset(getContext().getAssets(),"Exo-Bold.otf");
        notificatText = (TextView) view.findViewById(R.id.notificat_text);
        notificatText.setTypeface(font2);
        favText = (TextView) view.findViewById(R.id.fav_text);
        favText.setTypeface(font2);
        nameView = (TextView) view.findViewById(R.id.stop_name_frag);
        nameView.setTypeface(font1);
        routeView = (TextView) view.findViewById(R.id.route_no_frag);
        routeView.setTypeface(font2);
        orderView = (TextView) view.findViewById(R.id.order_no_frag);
        orderView.setTypeface(font3);
        opporderView = (TextView) view.findViewById(R.id.opp_order_no_frag);
        opporderView.setTypeface(font3);
        nameView.setText(details[0]);
        routeView.setText(details[1]);
        orderView.setText(details[4]);
        opporderView.setText(details[5]);

        orderList = (LinearLayout) view.findViewById(R.id.order_layout_frag);

        if(details[6].equals("PURPLE"))
            orderList.setBackgroundResource(R.drawable.metro_line);
        else if(details[6].equals("GREEN"))
            orderList.setBackgroundResource(R.drawable.metro_line_green);

        time1 = (TextView) view.findViewById(R.id.time1);
        time2 = (TextView) view.findViewById(R.id.time2);
        time3 = (TextView) view.findViewById(R.id.time3);

        time1.setTypeface(font4);
        time2.setTypeface(font4);
        time3.setTypeface(font4);
        time1.setText(times[0]);
        time2.setText(times[1]);
        time3.setText(times[2]);

        otime1 = (TextView) view.findViewById(R.id.otime1);
        otime2 = (TextView) view.findViewById(R.id.otime2);
        otime3 = (TextView) view.findViewById(R.id.otime3);

        otime1.setTypeface(font4);
        otime2.setTypeface(font4);
        otime3.setTypeface(font4);
        otime1.setText(otimes[0]);
        otime2.setText(otimes[1]);
        otime3.setText(otimes[2]);

        String polyline = bundle.getString("stop_polyline");
        DirectionParser directionParser = new DirectionParser();
        double[] coords = bundle.getDoubleArray("coords");
        ArrayList<LatLng> pointList = directionParser.decodePoly(polyline);
        mapFragment.makeRoute(pointList,coords[0],coords[1]);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        final View myView = view;
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
        {
            public void onGlobalLayout()
            {
                myView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                detailFragHead = (LinearLayout) myView.findViewById(R.id.detail_frag_head);
                heightr = detailFragHead.getHeight();
            }
        });
    }
}
