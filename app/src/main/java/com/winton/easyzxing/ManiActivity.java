package com.winton.easyzxing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.zxing.activity.ScanActivity;

/**
 * @author: winton
 * @time: 2018/9/6 下午6:24
 * @desc: 描述
 */
public class ManiActivity extends AppCompatActivity {

    TextView textView  ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView = new TextView(this);
        textView.setText("点击这里");
        setContentView(textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanActivity.start(ManiActivity.this,123);
            }
        });
    }



}
