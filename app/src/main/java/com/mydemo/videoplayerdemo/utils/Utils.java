package com.mydemo.videoplayerdemo.utils;

/**
 * Created by chenlong on 2016/12/16.
 */
public class Utils
{
    public static String formatFileDuration(long duration)
    {
        long second = (duration / 1000);    //转换成多少秒
        long minute = second / 60;          //转换成多少分
        long hour = minute / 60;            //转换成多少时

        second = second % 60;
        minute = minute % 60;

        return (hour < 10 ? "0" + hour : hour) + ":"
                + (minute < 10 ? "0" + minute : minute) + ":"
                + (second < 10 ? "0" + second : second);
    }
}
