package com.ffst.dustbinbrain.kotlin_mvp.bean;

/**
 * 通过串口发送指令请求
 * */
public class ImlSerialPortRequest {

    public interface StrHEX {
        /**
         * 电机开门
         * @param doorNumber 第几扇门
         * */
        String openDoor(int doorNumber);


        /**
         * 电机关门
         * @param doorNumber 第几扇门
         * */
        String closeDoor(int doorNumber);


        /**
         * 测距
         * @param doorNumber 第几扇门
         * */
        String measureTheDistance(int doorNumber);


        /**
         * 测重量
         * @param doorNumber 第几扇门
         * */
        String measureTheWeight(int doorNumber);


        /**
         * 开启消毒
         * @param doorNumber 第几扇门
         * */
        String openTheDisinfection(int doorNumber);


        /**
         * 关闭消毒
         * @param doorNumber 第几扇门
         * */
        String closeTheDisinfection(int doorNumber);


        /**
         * 开启排气扇
         * @param doorNumber 第几扇门
         * */
        String openExhaustFan(int doorNumber);



        /**
         * 关闭排气扇
         * @param doorNumber 第几扇门
         * */
        String closeExhaustFan(int doorNumber);


        /**
         * 电磁开启
         * @param doorNumber 第几扇门
         * */
        String openElectromagnetism(int doorNumber);

        /**
         * 电磁关闭
         * @param doorNumber 第几扇门
         * */
        String closeElectromagnetism(int doorNumber);


        /**
         * 开启加热
         * @param doorNumber 第几扇门
         * */
        String openTheHeating(int doorNumber);


        /**
         * 关闭加热
         * @param doorNumber 第几扇门
         * */
        String closeTheHeating(int doorNumber);


        /**
         * 开启搅拌
         * @param doorNumber 第几扇门
         * */
        String openBlender(int doorNumber);


        /**
         * 关闭搅拌
         * @param doorNumber 第几扇门
         * */
        String closeBlender(int doorNumber);



        /**
         * 打开电磁投料口
         * @param doorNumber 第几扇门
         * */
        String openDogHouse(int doorNumber);



        /**
         * 关闭电磁投料口
         * @param doorNumber 第几扇门
         * */
        String closeDogHouse(int doorNumber);


        /**
         * 打开照明灯
         * @param doorNumber 第几扇门
         * */
        String openLight(int doorNumber);


        /**
         * 关闭照明灯
         * @param doorNumber 第几扇门
         * */
        String closeLight(int doorNumber);
    }

    public interface ByteHEX {
        /**
         * 电机开门
         * @param doorNumber 第几扇门
         * */
        byte[] openDoor(int doorNumber);


        /**
         * 电机关门
         * @param doorNumber 第几扇门
         * */
        byte[] closeDoor(int doorNumber);


        /**
         * 测距
         * @param doorNumber 第几扇门
         * */
        byte[] measureTheDistance(int doorNumber);


        /**
         * 测重量
         * @param doorNumber 第几扇门
         * */
        byte[] measureTheWeight(int doorNumber);


        /**
         * 开启消毒
         * @param doorNumber 第几扇门
         * */
        byte[] openTheDisinfection(int doorNumber);


        /**
         * 关闭消毒
         * @param doorNumber 第几扇门
         * */
        byte[] closeTheDisinfection(int doorNumber);


        /**
         * 开启排气扇
         * @param doorNumber 第几扇门
         * */
        byte[] openExhaustFan(int doorNumber);



        /**
         * 关闭排气扇
         * @param doorNumber 第几扇门
         * */
        byte[] closeExhaustFan(int doorNumber);


        /**
         * 电磁开启
         * @param doorNumber 第几扇门
         * */
        byte[] openElectromagnetism(int doorNumber);

        /**
         * 电磁关闭
         * @param doorNumber 第几扇门
         * */
        byte[] closeElectromagnetism(int doorNumber);


        /**
         * 开启加热
         * @param doorNumber 第几扇门
         * */
        byte[] openTheHeating(int doorNumber);


        /**
         * 关闭加热
         * @param doorNumber 第几扇门
         * */
        byte[] closeTheHeating(int doorNumber);


        /**
         * 开启搅拌
         * @param doorNumber 第几扇门
         * */
        byte[] openBlender(int doorNumber);


        /**
         * 关闭搅拌
         * @param doorNumber 第几扇门
         * */
        byte[] closeBlender(int doorNumber);



        /**
         * 打开投料口
         * @param doorNumber 第几扇门
         * */
        byte[] openDogHouse(int doorNumber);



        /**
         * 关闭投料口
         * @param doorNumber 第几扇门
         * */
        byte[] closeDogHouse(int doorNumber);


        /**
         * 打开照明灯
         * @param doorNumber 第几扇门
         * */
        byte[] openLight(int doorNumber);


        /**
         * 关闭照明灯
         * @param doorNumber 第几扇门
         * */
        byte[] closeLight(int doorNumber);


        /**
         * 重量校准 1
         * */
        byte[] weightCalibration_1(int doorNumber);

        /**
         * 退出校准模式
         * */
        byte[] exitWeightCalibrationMode(int doorNumber);


        /**
         * 重量校准 2
         * @param weight 重量
         * */
        byte[] weightCalibration_2(int doorNumber,int weight);


        /**
         * 读取数据
         * @param doorNumber
         * */
        byte[] getDate(int doorNumber);
    }
}
