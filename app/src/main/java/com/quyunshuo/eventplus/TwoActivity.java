package com.quyunshuo.eventplus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.quyunshuo.eventplus.databinding.ActivityTwoBinding;
import com.quyunshuo.evnetplus.EventPlus;
import com.quyunshuo.evnetplus.Subscribe;
import com.quyunshuo.evnetplus.ThreadModel;

/**
 * @Author: QuYunShuo
 * @Time: 2020/4/30
 * @Class: TwoActivity
 * @Remark:
 */
public class TwoActivity extends AppCompatActivity {

    private ActivityTwoBinding binding;

    private static final String TAG = TwoActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTwoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EventPlus.getDefault().register(this);
        initView();
    }

    private void initView() {
        binding.sendTv.setOnClickListener(v -> {
            new Thread(() -> EventPlus.getDefault().post("事件1")).start();
            EventPlus.getDefault().post(new TestEvent("事件2"));
        });
    }

    @Subscribe(threadModel = ThreadModel.BACKGROUND)
    public void getMsg1(String msg) {
        Log.d(TAG, ">>>>>>>>>>getMsg1: " + msg + ">>>" + Thread.currentThread().getName());
    }

    @Subscribe(threadModel = ThreadModel.BACKGROUND)
    public void getMsg2(TestEvent event) {
        Log.d(TAG, ">>>>>>>>>>getMsg2: " + event.getString() + ">>>" + Thread.currentThread().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventPlus.getDefault().unregister(this);
    }
}
