package com.literem.matrix.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    /**
     * 获取当前时间
     * @return String类型的时间
     */
    public static String getNowDateTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        return sdf.format(date);
    }

    /**
     * 获取当前时间
     * @return String类型的时间
     */
    public static String getNowTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("_HHmmss", Locale.CHINA);
        return sdf.format(date);
    }

    public static byte[] getCurrentDatetime(){
        byte[] bytes = new byte[7];
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"),Locale.CHINA);
        bytes[0] = (byte) (calendar.get(Calendar.YEAR) % 2000);
        bytes[1] = (byte) (calendar.get(Calendar.MONTH) + 1);
        bytes[2] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
        bytes[3] = (byte) calendar.get(Calendar.DAY_OF_WEEK);
        bytes[4] = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        bytes[5] = (byte) calendar.get(Calendar.MINUTE);
        bytes[6] = (byte) calendar.get(Calendar.SECOND);
        return bytes;
    }

    public static String getDayOfWeek(int year,int month,int day) {
        TimeZone aDefault = TimeZone.getDefault();
        System.out.println(aDefault.getDisplayName());
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(),Locale.CHINA);
        calendar.set(year,month-1,day);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        String str_week = "";
        switch (week)
        {
            case 1:str_week = "周日";break;
            case 2:str_week = "周一";break;
            case 3:str_week = "周二";break;
            case 4:str_week = "周三";break;
            case 5:str_week = "周四";break;
            case 6:str_week = "周五";break;
            case 7:str_week = "周六";break;
        }
        return str_week;
    }

    /* *
     * 时间戳转换成提示性文本，例如：一小时前
     * @param timeStamp 时间戳
     * @return 提示性文本
     *
    public static String convertTimeToFormat(long timeStamp)
    {
        long currentTime = System.currentTimeMillis();
        long times = (currentTime - timeStamp) / 1000L;
        if (times >= 60 && times < 3600){
            return times / 60 + "分钟前";
        }else if(times>0 && times<60){
            return "刚刚";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);
        Date date = new Date(timeStamp);
        long day;
        try {
            Date old = sdf.parse(sdf.format(date));
            if(old == null)
                return "未知的时间";
            long oldTime = old.getTime();
            day = (currentTime - oldTime) / (86400*1000);
        } catch (ParseException e) {
            e.printStackTrace();
            return "未知的时间";
        }
        if (day < 1) {  //今天
            return format(timeStamp,"今天 HH:mm");
        } else if (day == 1) {
            return format(timeStamp,"昨天 HH:mm");
        } else if(day == 2){
            return format(timeStamp,"前天 HH:mm");
        } else {
            return format(timeStamp,"MM-dd HH:mm");
        }
    }

    /**
     * 把时间戳根据给定类型转换
     * @param timeStamp 时间戳
     * @param pattern 类型
     * @return 转换后的值
     */
    public static String format(long timeStamp,String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern,Locale.CHINA);
        return sdf.format(timeStamp);
    }

    /**
     * 时间戳转成年月日类型  yyyy-MM-dd
     * @param timeStamp   时间戳
     * @return 年，月，日
     */
    public static String[] timeStamp2YMD(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String date = sdf.format(timeStamp);
        String[] split = date.split("\\s");
        return split[0].split("-");
    }

    public static String[] dateTime2YMD(String dateTime){
        String[] split = dateTime.split("\\s");
        return split[0].split("-");
    }

    /**
     * 年月日 时分秒类型，转换成时间戳
     * @param s 年月日 时分秒类型
     * @return 时间戳
     */
    public static long date2timeStamp(String s)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date = null;
        try {
            date = simpleDateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date == null) return 0;
        return date.getTime();
    }

    /**
     * 获取当前时间戳
     * @return String类型的时间戳
     */
    public static long getTimeStamp()
    {
        return System.currentTimeMillis();
    }
}