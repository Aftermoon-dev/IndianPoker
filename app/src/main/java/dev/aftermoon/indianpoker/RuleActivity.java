package dev.aftermoon.indianpoker;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import dev.aftermoon.indianpoker.databinding.ActivityRuleBinding;

public class RuleActivity extends AppCompatActivity {
    private ActivityRuleBinding binding;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRuleBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }
}
