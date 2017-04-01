package io.github.maniknarang.riderr;

/**
 * Created by kingpushpakraj on 01-04-2017.
 */

public class Result
{
    public String travelMode;
    public String duration;
    public String distance;
    public String curr;

    public Result(String travelMode, String duration, String distance,String curr)
    {
        this.distance=distance;
        this.travelMode=travelMode;
        this.duration=duration;
        this.curr=curr;
    }
}
