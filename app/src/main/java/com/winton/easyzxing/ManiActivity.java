package com.winton.easyzxing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zxing.activity.CodeUtils;
import com.zxing.activity.ScanActivity;

import javax.xml.transform.Result;

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 123:
            if(resultCode == RESULT_OK){
                int type = data.getIntExtra(CodeUtils.RESULT_TYPE,-1);
                String result = data.getStringExtra(CodeUtils.RESULT_STRING);
                if(type == CodeUtils.RESULT_SUCCESS){
                    Toast.makeText(this,result,Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this,"扫描失败",Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:break;
        }

    }
}
