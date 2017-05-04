package io.github.maniknarang.riderr;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.ViewHolder> implements RVHAdapter
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
        TextView routeView = holder.routeView;
        TextView timeView = holder.timeView;
        TextView orderView = holder.orderView;
        nameView.setText(stop.getName());
        routeView.setText(stop.getRoute());
        timeView.setText(stop.getTime());
        orderView.setText(stop.getOrderNo());

        if(stop.getLine().equals("PURPLE"))
            holder.orderView.setBackgroundResource(R.drawable.metro_line);
        else if(stop.getLine().equals("GREEN"))
            holder.orderView.setBackgroundResource(R.drawable.metro_line_green);
    }

    @Override
    public int getItemCount()
    {
        return stopList.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition)
    {
        return false;
    }

    @Override
    public void onItemDismiss(int position, int direction) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements RVHViewHolder
    {
        public TextView nameView;
        public TextView routeView;
        public TextView timeView;
        public TextView orderView;
        private View itemView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            this.itemView = itemView;
            Typeface font1 = Typeface.createFromAsset(itemView.getContext().getAssets(),"Raleway-SemiBold.ttf");
            Typeface font2 = Typeface.createFromAsset(itemView.getContext().getAssets(),"Montserrat-Regular.otf");
            Typeface font3 = Typeface.createFromAsset(itemView.getContext().getAssets(),"Chunkfive.otf");
            Typeface font4 = Typeface.createFromAsset(itemView.getContext().getAssets(),"Exo-Bold.otf");
            nameView = (TextView)itemView.findViewById(R.id.stop_name);
            nameView.setTypeface(font1);
            routeView = (TextView) itemView.findViewById(R.id.route_no);
            routeView.setTypeface(font2);
            timeView = (TextView) itemView.findViewById(R.id.time_view);
            timeView.setTypeface(font4);
            orderView = (TextView) itemView.findViewById(R.id.order_no);
            orderView.setTypeface(font3);
        }

        @Override
        public void onItemSelected(int actionstate)
        {

        }

        @Override
        public void onItemClear()
        {

        }
    }

    public ArrayList<Stop> getStopList()
    {
        return stopList;
    }

    public void clear()
    {
        stopList.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<Stop> list)
    {
        stopList.addAll(list);
        notifyDataSetChanged();
    }
}
