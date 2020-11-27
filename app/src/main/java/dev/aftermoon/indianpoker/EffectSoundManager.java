package dev.aftermoon.indianpoker;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap;

public class EffectSoundManager {
    private static EffectSoundManager effectSoundManager;
    private static SoundPool soundPool;
    private static HashMap<Integer, Integer> soundPoolMap;
    private static SharedPreferences prefs;

    private EffectSoundManager(Context context) {
        prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(5)
                .build();

        soundPoolMap = new HashMap<>();
        soundPoolMap.put(R.raw.turn, soundPool.load(context, R.raw.turn, 1));
        soundPoolMap.put(R.raw.start, soundPool.load(context, R.raw.start, 1));
        soundPoolMap.put(R.raw.lose, soundPool.load(context, R.raw.lose, 1));
        soundPoolMap.put(R.raw.win, soundPool.load(context, R.raw.win, 1));

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d("EffectSoundManager", "Load Complete");
            }
        });
    }

    public static EffectSoundManager getInstance(Context context) {
        if(effectSoundManager == null) {
            effectSoundManager = new EffectSoundManager(context);
        }
        return effectSoundManager;
    }

    public void play(final int raw_id) {
        boolean isEffectSoundOn = prefs.getBoolean("isEffectSoundOn", true);
        if(isEffectSoundOn && soundPoolMap.containsKey(raw_id)) {
            soundPool.play(soundPoolMap.get(raw_id), 1, 1, 1, 0, 1f);
        }
    }

    public void release() {
        soundPool.release();
    }
}
