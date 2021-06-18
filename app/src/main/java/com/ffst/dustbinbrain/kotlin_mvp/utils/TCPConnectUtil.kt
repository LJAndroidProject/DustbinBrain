package com.ffst.dustbinbrain.kotlin_mvp.utils

import com.littlegreens.netty.client.NettyTcpClient
import com.littlegreens.netty.client.NettyTcpClient.Builder
import com.littlegreens.netty.client.listener.NettyClientListener

/**
 * Created by LiuJW
 *on 2021/6/18
 */
class TCPConnectUtil {
    companion object {
        val tcpIp: String = "47.115.76.11"
        val port: Int = 2346
        val heartBeatData:ByteArray = byteArrayOf(0x03, 0x0F, 0xFE.toByte(), 0x05, 0x04, 0x0A)
        var tcpConnectUtil: TCPConnectUtil? = null
        var mNettyTcpClient: NettyTcpClient? = null
        fun getInstance() = tcpConnectUtil ?: synchronized(this) {
            mNettyTcpClient ?: Builder()
                .setHost(tcpIp)//设置服务端地址
                .setTcpPort(port) //设置服务端端口号
                .setMaxReconnectTimes(-1)//设置最大重连次数 -1时无限重连
                .setReconnectIntervalTime(1000)//设置重连间隔时间。单位 毫秒
                .setSendheartBeat(true)//设置是否发送心跳
                .setHeartBeatInterval(60*1000)//设置心跳间隔时间。单位：秒
                .setHeartBeatData(heartBeatData)//心跳可能存在问题
                .setIndex(0) //设置客户端标识.(因为可能存在多个tcp连接)
                .build().also { mNettyTcpClient = it }
            tcpConnectUtil ?:TCPConnectUtil().also { tcpConnectUtil = it }
        }
    }


    fun setListener(nettyClientListener: NettyClientListener<*>?) {
        mNettyTcpClient!!.setListener(nettyClientListener)
    }


    fun connect() {
        if (null == mNettyTcpClient) return
        mNettyTcpClient!!.connect() //连接服务器
    }


    fun reconnect() {
        if (null == mNettyTcpClient) return
        mNettyTcpClient!!.reconnect() //重连接服务器
    }

    fun disconnect() {
        if (null == mNettyTcpClient) return
        mNettyTcpClient!!.disconnect() //断开连接
    }


    fun sendData(b: String): Boolean {
        return mNettyTcpClient!!.sendMsgToServer(b.toByteArray())
    }
}