package com.tencent.liteav.trtccalling.model;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;


/**
 * 语音播报类
 * */
public class VoiceUtil {
    public static VoiceUtil voiceUtil;
    public static MediaPlayer mediaPlayer;

    private VoiceUtil(){ }

    public static VoiceUtil getInstance(){
        if(voiceUtil == null){
            synchronized (VoiceUtil.class){
                if(voiceUtil == null){
                    voiceUtil = new VoiceUtil();

                    mediaPlayer = new MediaPlayer();
                }
            }
        }

        return voiceUtil;
    }



    /**
     * 打开assets下的音乐mp3文件
     */
    public static long useTime;
    public synchronized void openAssetMusics(final Context context,final String fileName) {

        if(System.currentTimeMillis() - useTime < 2000){
            return;
        }

        useTime = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //播放 assets/a2.mp3 音乐文件
                    AssetFileDescriptor fd = context.getAssets().openFd(fileName);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


    public synchronized void loopCallMusics(final Context context,final String fileName) {

        if(System.currentTimeMillis() - useTime < 2000){
            return;
        }

        useTime = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //播放 assets/a2.mp3 音乐文件
                    AssetFileDescriptor fd = context.getAssets().openFd(fileName);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
//                    mediaPlayer.setLooping(true);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        int index = 0;
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if(index<9){
                                mp.start();
                                index++;
                            }
                        }
                    });
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public synchronized  void stopMediaPlayer(){
        mediaPlayer.stop();
    }


}
