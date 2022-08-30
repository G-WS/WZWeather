package com.example.wzweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.wzweather.db.City;
import com.example.wzweather.db.County;
import com.example.wzweather.db.Province;
import com.example.wzweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    /**
     * 解析和处理服务器返回的 省级数据
     */
    public static boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvince = new JSONArray(response);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject provinceJSONObject = allProvince.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceJSONObject.getString("name"));
                    province.setProvinceCode(provinceJSONObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceID(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
//    public static boolean handleCountyResponse(String response, int cityId) {
//        if (!TextUtils.isEmpty(response)) {
//            try {
//                JSONArray allCounties = new JSONArray(response);
//                for (int i = 0; i < allCounties.length(); i++) {
//                    JSONObject countyJSONObject = allCounties.getJSONObject(i);
//                    County county = new County();
//                    county.setCountyName(countyJSONObject.getString("name"));
//                    county.setWeatherId(countyJSONObject.getString("weather_id"));
//                    county.setId(cityId);
//                    county.save();
//                }
//                return true;
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return false;
//    }
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
//                Log.d("here", "handleCountyResponse: "+response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
