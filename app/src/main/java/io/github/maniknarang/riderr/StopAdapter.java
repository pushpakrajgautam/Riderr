package io.github.maniknarang.riderr;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.ViewHolder>
{
    private ArrayList<Stop> stopList;
    private Context context;

    public StopAdapter(Context context,ArrayList<Stop> stopList)
    {
        this.stopList=stopList;
        this.context=context;
    }
    @Override
    public StopAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View stopView = inflater.inflate(R.layout.stop_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(stopView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StopAdapter.ViewHolder holder, int position)
    {
        Stop stop = stopList.get(position);
        TextView nameView = holder.nameView;
        nameView.setText(stop.getName());

    }

    @Override
    public int getItemCount()
    {
        return stopList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView nameView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            nameView = (TextView)itemView.findViewById(R.id.stop_name);
        }
    }

    public ArrayList<Stop> getStopList()
    {
        return stopList;
    }
}
