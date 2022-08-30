package com.example.wzweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public static class More {
        @SerializedName("txt")
        public String info;
    }
}
