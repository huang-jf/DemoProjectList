package com.cy_life.aopdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cy_life.aopdemo.jdk.FontProvider;
import com.cy_life.aopdemo.jdk.ProviderFactory;
import com.cy_life.libaop.SingleClick;


public class MainActivity extends AppCompatActivity {


    private int num = 0;
    private FontProvider fontProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast(v);
                proxy();
            }
        });
    }


    @SingleClick
    private void toast(View view) {
        Toast.makeText(getApplicationContext(), "num->" + (++num), Toast.LENGTH_SHORT).show();
    }

    /**
     * 测试 jdk 动态代理 DynamicProxy
     */
    private void proxy() {
        if (fontProvider == null) {
            fontProvider = ProviderFactory.getFontProvider();
        }
        String font = fontProvider.getFont("微软雅黑");
        Log.d("DynamicProxy", "FontName: " + font);
    }
}
