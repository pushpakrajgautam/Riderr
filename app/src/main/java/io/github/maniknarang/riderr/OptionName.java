package io.github.maniknarang.riderr;

public class OptionName
{
    private String name;
    private String desc;
    private String symbol;
    private String placeId;

    public OptionName(String name, String desc, String symbol, String placeId)
    {
        this.name = name;
        this.desc = desc;
        this.symbol = symbol;
        this.placeId = placeId;
    }

    public String getName(){return name;}
    public String getDesc(){return desc;}
    public String getSymbol(){return symbol;}
    public String getPlaceId() {return placeId;}
}
