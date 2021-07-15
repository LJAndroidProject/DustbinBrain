package com.ffst.dustbinbrain.kotlin_mvp.utils;

import com.ffst.dustbinbrain.kotlin_mvp.bean.BinsWorkTimeBean;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BinsWorkTimeUntil {
    public static boolean getBinsWorkTime(BinsWorkTimeBean binsWorkTimeBean){

        //  如果是为 null 则直接为可以投放
        if(binsWorkTimeBean == null){
            return true;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());

        //  获取时间戳
        long am_start_time = convertTimeToLong(formatter.format(date) + " " + binsWorkTimeBean.getData().getAm_start_time());

        long am_end_time = convertTimeToLong(formatter.format(date) + " " + binsWorkTimeBean.getData().getAm_end_time());

        long pm_start_time = convertTimeToLong(formatter.format(date) + " " + binsWorkTimeBean.getData().getPm_start_time());

        long pm_end_time = convertTimeToLong(formatter.format(date) + " " + binsWorkTimeBean.getData().getPm_end_time());

        long nowTime = System.currentTimeMillis();

        if(nowTime > am_start_time && nowTime < am_end_time){
            System.out.println("符合上午投放时间");

            return true;
        }else if(nowTime > pm_start_time && nowTime < pm_end_time){
            System.out.println("符合下午投放时间");

            return true;
        }else{
            System.out.println("非投放时间");

            return false;
        }
    }

    public static Long convertTimeToLong(String time) {
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = sdf.parse(time);
            return date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
}
