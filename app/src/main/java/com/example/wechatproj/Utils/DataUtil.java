package com.example.wechatproj.Utils;

public class DataUtil {
    private int hours=0,minutes=0,sec=0;
    private int years=0,months=0,days=0;
    public DataUtil(Long timeStamp){
        int seconds = (int) (System.currentTimeMillis()-timeStamp)/1000;
        if(seconds<0) return;

        this.sec= seconds;
        if(seconds<60) return;

        this.minutes = seconds/60;
        this.sec = seconds%60;
        if(seconds<3600) return;

        this.hours = seconds/3600;
        this.minutes = (seconds%3600)/60;
        if(seconds<86400) return;

        this.days = seconds/86400;
        this.hours = (seconds%86400)/3600;
        if(seconds<2592000) return;

        //默认1个月为 30 天;
        this.months = seconds/2592000;
        this.days = seconds/86400;
        if(seconds<31104000) return;

        //一年有12个月，360天
        this.years = seconds/31104000;
        this.months = seconds/2592000;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSec() {
        return sec;
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    public String getTimeStr(){
        if(years!=0)    return years+"年前";
        if(months!=0)    return months+"月前";
        if(days!=0)    return days+"天前";
        if(hours!=0)    return hours+"小时前";
        if(minutes!=0)    return minutes+"分钟前";
        return "刚刚";
    }
}