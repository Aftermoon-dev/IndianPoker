package dev.aftermoon.indianpoker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BGMService extends Service {
    private boolean isPlaying;
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bgm);
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.7f, 0.7f);
        isPlaying = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!isPlaying) {
            isPlaying = true;
            mediaPlayer.start();
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
