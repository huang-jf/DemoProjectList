package com.cy_life.aopdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.cy_life.libaop.SingleClick;

/**
 * java.lang.NoSuchMethodError: No static method aspectOf()Lcom/cy_life/libcore/SingleClickAspect;
 * in class Lcom/cy_life/libcore/SingleClickAspect;
 * or its super classes (declaration of 'com.cy_life.libcore.SingleClickAspect' appears in /data/app/com.cy_life.aopdemo-2/base.apk)
 */
public class MainActivity extends AppCompatActivity {


    private int num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast(v);
            }
        });
    }


    @SingleClick
    private void toast(View view) {
        Toast.makeText(getApplicationContext(), "num->" + (++num), Toast.LENGTH_SHORT).show();
    }
}
