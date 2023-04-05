package com.literem.matrix.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigUtils {

    private Context context;
    private final String name = "config_data";

    public enum ConfigType {
        display_time,
        is_run,
        device_name,
        device_address
    }


    public ConfigUtils(Context context)
    {
        this.context = context;
    }

    /**
     * 加载String类型的数据
     * @return string
     */
    public String loadStringData(ConfigType type) {
        SharedPreferences shp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return shp.getString(getKey(type), "");
    }

    public boolean loadBooleanData(ConfigType type) {
        SharedPreferences shp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return shp.getBoolean(getKey(type),false);
    }

    /**
     * 保存String类型的数据
     * @param value point
     */
    public void saveStringData(ConfigType type, String value) {
        SharedPreferences shp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putString(getKey(type), value);
        editor.apply();
    }

    public void saveBooleanData(ConfigType type,boolean value) {
        SharedPreferences shp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putBoolean(getKey(type), value);
        editor.apply();
    }

    public void saveIntData(ConfigType type,int value) {
        SharedPreferences shp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putInt(getKey(type), value);
        editor.apply();
    }

    public int loadIntData(ConfigType type) {
        SharedPreferences shp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return shp.getInt(getKey(type), 0);
    }

    private String getKey(ConfigType type) {
        String key = null;
        switch (type)
        {
            case device_name:
                key = "device_name";
                break;
            case device_address:
                key = "device_address";
                break;
            case display_time:
                key = "display_time";
                break;
            case is_run:
                key = "is_run";
                break;
        }
        return key;
    }

}
