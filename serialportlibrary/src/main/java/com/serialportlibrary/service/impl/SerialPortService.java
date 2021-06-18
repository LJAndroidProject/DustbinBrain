package com.serialportlibrary.service.impl;

import android.os.SystemClock;

import android.serialport.SerialPort;
import android.text.TextUtils;
import android.util.Log;

import com.serialportlibrary.service.ISerialPortService;
import com.serialportlibrary.util.ByteStringUtil;
import com.serialportlibrary.util.LogUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

import static android.content.ContentValues.TAG;


public class SerialPortService implements ISerialPortService {

    /**
     * 尝试读取数据间隔时间
     */
    private static int RE_READ_WAITE_TIME = 10;

    /**
     * 读取返回结果超时时间
     */
    private Long mTimeOut = 100L;
    /**
     * 串口地址
     */
    private String mDevicePath;

    /**
     * 波特率
     */
    private int mBaudrate;

    private SerialPort mSerialPort;


    /**
     * 初始化串口
     *
     * @param devicePath 串口地址
     * @param baudrate   波特率
     * @param timeOut    数据返回超时时间
     * @throws IOException 打开串口出错
     */
    public SerialPortService(String devicePath, int baudrate, Long timeOut) throws IOException {
        mTimeOut = timeOut;
        mDevicePath = devicePath;
        mBaudrate = baudrate;
        mSerialPort = new SerialPort(new File(mDevicePath), mBaudrate);
    }

    @Override
    public byte[] sendData(byte[] data) {
        synchronized (SerialPortService.this) {
            try {
                InputStream inputStream = mSerialPort.getInputStream();
                OutputStream outputStream = mSerialPort.getOutputStream();
                int available = inputStream.available();
                byte[] returnData;
                if (available > 0) {
                    returnData = new byte[available];
                    inputStream.read(returnData);
                }
                outputStream.write(data);
                outputStream.flush();

                Long time = System.currentTimeMillis();
                //暂存每次返回数据长度，不变的时候为读取完数据
                int receiveLeanth = 0;
                while (System.currentTimeMillis() - time < mTimeOut) {
                    available = inputStream.available();

                    if (available > 0 && available == receiveLeanth) {
                        returnData = new byte[available];
                        inputStream.read(returnData);
                        return returnData;
                    } else {
                        receiveLeanth = available;
                    }
                    SystemClock.sleep(RE_READ_WAITE_TIME);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }




    /**
     * 注册监听线程
     * */
    public void receiveThread(final SerialResponseListener serialResponseListener) {
        final InputStream inputStream = mSerialPort.getInputStream();

        /* 开启一个线程进行读取 */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        byte[] buffer = new byte[1024];
                        int size = inputStream.read(buffer);
                        byte[] readBytes = new byte[size];
                        System.arraycopy(buffer, 0, readBytes, 0, size);

                        //  转换成16进制形式
                        StringBuilder getdata = new StringBuilder();
                        for (int i = 0; i < readBytes.length; i++) {
                            getdata.append(String.format("%02x", readBytes[i]));
                        }

                        serialResponseListener.response(getdata.toString());

                        Thread.sleep(100);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }


    public void receiveThread(final SerialResponseByteListener serialResponseByteListener) {
        final InputStream inputStream = mSerialPort.getInputStream();

        /* 开启一个线程进行读取 */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {

                        if(inputStream == null){
                            return;
                        }
                        byte[] buffer = new byte[1024];
                        int size = inputStream.read(buffer);

                        if(size > 0){
                            byte[] readBytes = new byte[size];
                            System.arraycopy(buffer, 0, readBytes, 0, size);

                            if(readBytes != null){
                                serialResponseByteListener.response(readBytes);
                            }
                        }

                        Thread.sleep(100);

                    } catch (Exception e) {
                        Log.i("结算调试","串口接收发生异常:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public void receiveICThread(final SerialResponseICListener serialResponseICListener) {
        final InputStream inputStream = mSerialPort.getInputStream();

        /* 开启一个线程进行读取 */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {

                        if(inputStream == null){
                            return;
                        }
                        byte[] buffer = new byte[1024];
                        int size = inputStream.read(buffer);

                        if(size > 0){
                            byte[] readBytes = new byte[size];
                            System.arraycopy(buffer, 0, readBytes, 0, size);

                            if(readBytes != null){
                                serialResponseICListener.response(readBytes);
                            }
                        }

                        Thread.sleep(1000);

                    } catch (Exception e) {
                        Log.i("结算调试","IC串口接收发生异常:" + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }









    /**
     * @deprecated
     * 响应串口的一个监听
     * */
    public interface SerialResponseListener{
        void response(String response);
    }


    public interface SerialResponseByteListener{
        void response(byte[] response);
    }

    //  ic卡
    public interface SerialResponseICListener{
        void response(byte[] response);
    }



    @Override
    public byte[] sendData(String date) {
        try {
            return sendData(ByteStringUtil.hexStrToByteArray(date));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void close() {
        if (mSerialPort != null) {
            mSerialPort.closePort();
        }
    }


    public void isOutputLog(boolean debug) {
        LogUtil.isDebug = debug;
    }


}
