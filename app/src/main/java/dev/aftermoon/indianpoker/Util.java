package dev.aftermoon.indianpoker;

import android.view.View;
import android.view.Window;

public class Util {
    /*
     * 상단바 가리기
     * https://developer.android.com/training/system-ui/immersive
     */
    public static void hideSystemUI(Window w) {
        View decorView = w.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /*
     * 상단바 보이기
     * https://developer.android.com/training/system-ui/immersive
     */
    public static void showSystemUI(Window w) {
        View decorView = w.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
