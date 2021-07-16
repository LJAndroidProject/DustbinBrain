package com.ffst.dustbinbrain.kotlin_mvp.utils;


import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinENUM;
import com.ffst.dustbinbrain.kotlin_mvp.bean.DustbinStateBean;

import java.util.List;

/**
 * 垃圾箱管理工具类
 * */
public class DustbinUtil {
    public static DustbinStateBean getDustbinState(int number,List<DustbinStateBean> dustbinBeanList){
        for(DustbinStateBean dustbinStateBean:dustbinBeanList){
            if(dustbinStateBean.getDoorNumber() == number){
                return dustbinStateBean;
            }
        }
        //  抛出异常
        return null;
    }

    /**
     * A：厨余垃圾，B：其他垃圾，C：可回收垃圾，D：有害垃圾
     * */
    public static String getDustbinType(String text){
        if(text.equals("A")){
            return DustbinENUM.KITCHEN.toString();
        }else if(text.equals("B")){
            return DustbinENUM.OTHER.toString();
        }else if(text.equals("C")){
            return DustbinENUM.RECYCLABLES.toString();
        }else if(text.equals("D")){
            return DustbinENUM.HARMFUL.toString();
        }else if(text.equals("E")){
            return DustbinENUM.BOTTLE.toString();
        }else if(text.equals("F")){
            return DustbinENUM.WASTE_PAPER.toString();
        }else{
            return null;
        }
    }


    public static int getLeftOrRight(int door){
        boolean isOddNumber = door % 2 != 0;
        //  奇数 + 1，偶数 -1
        int adjoinDoorNumber = isOddNumber ? 1 : -1;

        return door + adjoinDoorNumber;
    }
}
