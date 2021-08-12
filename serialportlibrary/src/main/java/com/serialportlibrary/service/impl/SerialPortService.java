package com.serialportlibrary.service.impl;

import android.serialport.SerialPort;
import android.util.Log;

import com.serialportlibrary.service.ISerialPortService;
import com.serialportlibrary.util.ByteStringUtil;
import com.serialportlibrary.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SerialPortService implements ISerialPortService {

    public final static String TAG = "SerialPortService";

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

    private List<byte[]> sendDataList = new ArrayList<>();


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
                Log.i(TAG, "发送：" + ByteStringUtil.byteArrayToHexStr(data));
                OutputStream outputStream = mSerialPort.getOutputStream();
                outputStream.write(data);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    /**
     * 注册监听线程
     */
    public void receiveThread(final SerialResponseListener serialResponseListener) {
        final InputStream inputStream = mSerialPort.getInputStream();

        /* 开启一个线程进行读取 */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
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
        new Thread(() -> {
            while (true) {
                try {

                    if (inputStream == null) {
                        return;
                    }
                    byte[] buffer = new byte[1024];
                    int size = inputStream.read(buffer);

                    if (size > 0) {
                        byte[] readBytes = new byte[size];
                        System.arraycopy(buffer, 0, readBytes, 0, size);

                        if (readBytes != null) {
                            if (sendDataList.size() > 0) {
                                byte[] sendByte = sendDataList.get(0);
                                if (Arrays.equals(new byte[]{sendByte[0], sendByte[1], sendByte[3], sendByte[4]}, new byte[]{readBytes[0], readBytes[1], readBytes[3], readBytes[4]})) {
                                    sendDataList.remove(0);
                                    isReceive = true;
                                }
                            }
                            serialResponseByteListener.response(readBytes);
                        }
                    }

                    Thread.sleep(100);

                } catch (Exception e) {
                    Log.i("结算调试", "串口接收发生异常:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void receiveICThread(final SerialResponseICListener serialResponseICListener) {
        final InputStream inputStream = mSerialPort.getInputStream();

        /* 开启一个线程进行读取 */
        new Thread(() -> {
            while (true) {
                try {

                    if (inputStream == null) {
                        return;
                    }
                    byte[] buffer = new byte[1024];
                    int size = inputStream.read(buffer);

                    if (size > 0) {
                        byte[] readBytes = new byte[size];
                        System.arraycopy(buffer, 0, readBytes, 0, size);

                        if (readBytes != null) {
                            serialResponseICListener.response(readBytes);
                        }
                    }

                    Thread.sleep(1000);

                } catch (Exception e) {
                    Log.i("结算调试", "IC串口接收发生异常:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }


    /**
     * @deprecated 响应串口的一个监听
     */
    public interface SerialResponseListener {
        void response(String response);
    }


    public interface SerialResponseByteListener {
        void response(byte[] response);
    }

    //  ic卡
    public interface SerialResponseICListener {
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


    private boolean isReceive = true;
    private long lastSendTime = 0;
    private Thread sendQueueThread = new Thread(() -> {
        int index = 1;
        while (true) {
            if (sendDataList.size() > 0) {//判断是否有需要发送的数据
                if (index <= 3) {//判断是否超过3次未接收到回码
                    if (!isReceive) {
                        if (System.currentTimeMillis() - lastSendTime >= 500) {
                            lastSendTime = System.currentTimeMillis();
                            sendData(sendDataList.get(0));
                            index++;
                        }
                    } else {
                        lastSendTime = System.currentTimeMillis();
                        sendData(sendDataList.get(0));
                        index = 1;
                        isReceive = false;
                    }
                } else {//超过3次无回码移除
                    sendDataList.remove(0);
                    index = 1;
                }
            }
        }
    });

    public void startSendQueue() {
        if (!sendQueueThread.isAlive()) {
            sendQueueThread.start();
        }
    }

    public byte[] insertSendQueue(byte[] data) {
        synchronized (SerialPortService.this) {
            if (sendDataList == null) {
                sendDataList = new ArrayList<>();
            }
            if (Arrays.equals(new byte[]{data[4]}, new byte[]{0x01})) {
                sendDataList.add(0, data);
            } else {
                sendDataList.add(data);
            }
//            startSendQueue();
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
