package dev.aftermoon.indianpoker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

import dev.aftermoon.indianpoker.databinding.ActivityStartBinding;

public class StartActivity extends AppCompatActivity {
    private ActivityStartBinding binding;
    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        binding.cbEnableBgm.setChecked(prefs.getBoolean("isBGMOn", true));
        binding.cbEnableEffect.setChecked(prefs.getBoolean("isEffectSoundOn", true));

        setBtnEvent();

        // 이펙트 소리 미리 로드를 위해 여기서 Instance 획득
        EffectSoundManager.getInstance(this);
    }

    private void setBtnEvent() {
        final SharedPreferences.Editor editor = prefs.edit();

        // 각 버튼 이벤트 지정
        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GameActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("name", binding.etPlayername.getText().toString());
                i.putExtras(bundle);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(i);
            }
        });

        binding.btnRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RuleActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                startActivity(i);
            }
        });

        binding.cbEnableBgm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) startService(new Intent(StartActivity.this, BGMService.class));
                else stopService(new Intent(StartActivity.this, BGMService.class));
                buttonView.setChecked(isChecked);
                editor.putBoolean("isBGMOn", isChecked);
                editor.apply();
            }
        });

        binding.cbEnableEffect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setChecked(isChecked);
                editor.putBoolean("isEffectSoundOn", isChecked);
                editor.apply();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, BGMService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isBGMOn = prefs.getBoolean("isBGMOn", true);
        if(isBGMOn) startService(new Intent(this, BGMService.class));
        binding.cbEnableBgm.setChecked(prefs.getBoolean("isBGMOn", true));
        binding.cbEnableEffect.setChecked(prefs.getBoolean("isEffectSoundOn", true));
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        stopService(new Intent(this, BGMService.class));
    }
}