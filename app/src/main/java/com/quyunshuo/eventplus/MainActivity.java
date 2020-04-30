package com.quyunshuo.eventplus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quyunshuo.eventplus.databinding.ActivityMainBinding;
import com.quyunshuo.evnetplus.EventPlus;
import com.quyunshuo.evnetplus.Subscribe;
import com.quyunshuo.evnetplus.ThreadModel;

/**
 * @Author: QuYunShuo
 * @Time:   2020/4/30
 * @Class:  MainActivity
 * @Remark:
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG  = MainActivity.class.getName();

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EventPlus.getDefault().register(this);
        initView();
    }

    private void initView() {
        binding.intentTv.setOnClickListener(v -> startActivity(new Intent(this, TwoActivity.class)));
    }

    @Subscribe(threadModel = ThreadModel.MAIN)
    public void getMsg1(String msg) {
        Log.d(TAG, ">>>>>>>>>>getMsg1: " + msg + ">>>" + Thread.currentThread().getName());
    }

    @Subscribe(threadModel = ThreadModel.MAIN)
    public void getMsg2(TestEvent event) {
        Log.d(TAG, ">>>>>>>>>>getMsg2: " + event.getString() + ">>>" + Thread.currentThread().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventPlus.getDefault().unregister(this);
    }
}
