package io.github.maniknarang.riderr;

public class Stop
{
    private String name;
    private String route;
    private String time;
    private String orderNo;
    private String line;

    public Stop(String name,String route,String time,String orderNo,String line)
    {
        this.name=name;
        this.route=route;
        this.time=time;
        this.orderNo=orderNo;
        this.line=line;
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
    public String getLine()
    {
        return line;
    }
}
