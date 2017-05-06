package io.github.maniknarang.riderr;

import java.util.ArrayList;

public class Stop
{
    private String name;
    private String route;
    private String time;
    private String orderNo;
    private String line;
    private ArrayList<String> times;
    private ArrayList<String> opptimes;
    private String oppOrder;
    private String otherTime;
    private String polyline;
    private double lat;
    private double lng;

    public Stop(String name,String route,String time,String otherTime,String orderNo,String oppOrder, String line,
                ArrayList<String> times,ArrayList<String> opptimes, String polyline,double lat,double lng)
    {
        this.name=name;
        this.route=route;
        this.time=time;
        this.orderNo=orderNo;
        this.line=line;
        this.times = times;
        this.oppOrder = oppOrder;
        this.opptimes = opptimes;
        this.otherTime = otherTime;
        this.polyline = polyline;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName()
    {
        return name;
    }
    public String getRoute()
    {
        return route;
    }
    public String getTime()
    {
        return time;
    }
    public String getOrderNo()
    {
        return orderNo;
    }
    public String getLine() {return line;}
    public ArrayList<String> getTimes(){return times;}
    public ArrayList<String> getoppTimes(){return opptimes;}
    public String getOppOrder(){return oppOrder;}
    public String getOtherTime(){return otherTime;}
    public String getPolyline(){return polyline;}
    public double getLat(){return lat;}
    public double getLng(){return lng;}
}
