package com.example.hjf.hookdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // hook activity 启动方法
        HookFuncByDynamicProxy.hookActivityOnStart();

        findViewById(R.id.btn_hook_java_by_reflect).setOnClickListener(this);
        findViewById(R.id.btn_hook_notify_by_dynamicProxy).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_hook_java_by_reflect:
                startActivity(new Intent(this, HookJavaByReflectActivity.class));
                HookFuncByDynamicProxy.testHookActivityOnStart();
                break;

            case R.id.btn_hook_notify_by_dynamicProxy:
                HookFuncByDynamicProxy.hookNotificationManager(getApplicationContext());
                break;
        }
    }
}