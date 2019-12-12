package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;



public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;


    public Sport sport;

    public class Comfort{
        @SerializedName("txt")
        public String info;
    }
    public class Sport{
        @SerializedName("txt")
        public String info;
    }

}
