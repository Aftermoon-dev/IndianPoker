package dev.aftermoon.indianpoker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;

import dev.aftermoon.indianpoker.databinding.DialogSettingBinding;

public class SettingDialog extends Dialog {
    DialogSettingBinding binding;
    private Context context;

    public SettingDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        binding = DialogSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        binding.toolbarSetting.setTitle("설정");
    }
}
