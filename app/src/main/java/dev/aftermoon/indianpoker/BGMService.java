package dev.aftermoon.indianpoker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BGMService extends Service {
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        boolean isBGMOn = prefs.getBoolean("isBGMOn", true);

        // BGM 활성화 상태이면
        if(isBGMOn) {
            // 미디어 플레이어 온
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bgm);
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(0.7f, 0.7f);
            mediaPlayer.start();
        }
        // 아니면 서비스 정지
        else {
            stopSelf();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        super.onDestroy();
    }
}
