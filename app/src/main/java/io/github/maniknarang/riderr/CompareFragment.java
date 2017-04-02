package io.github.maniknarang.riderr;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class CompareFragment extends android.support.v4.app.Fragment
{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.graph,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView graph = (ImageView) view.findViewById(R.id.graph);
        double origin1 = ((ResultActivity) getActivity()).origin1;
        double origin2 = ((ResultActivity) getActivity()).origin2;
        double dest1 = ((ResultActivity) getActivity()).dest1;
        double dest2 = ((ResultActivity) getActivity()).dest2;
        Glide.with(this).load("http://serv1.anmolahuja.com/api/get_graph?start_longitude="+
                origin1+"&start_latitude="+origin2+"&end_longitude="+dest1+"&end_latitude="+dest2).into(graph);
    }
}
