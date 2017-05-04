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

    public Stop(String name,String route,String time,String orderNo,String line,ArrayList<String> times)
    {
        this.name=name;
        this.route=route;
        this.time=time;
        this.orderNo=orderNo;
        this.line=line;
        this.times = times;
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
}
