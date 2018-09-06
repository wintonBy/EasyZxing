package com.zxing.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zxing.R;
import com.zxing.UriUtils;
import com.zxing.camera.CameraManager;

/**
 * @author: winton
 * @time: 2018/9/6 下午5:13
 * @desc: 描述
 */
public class ScanActivity extends AppCompatActivity implements View.OnClickListener{

    private CaptureFragment mFragment;
    private CodeUtils.AnalyzeCallback mCallback;
    private LinearLayout mLightLayout;

    private boolean isLightOpen = false;


    private static final int REQ_CAMERA = 0x101;
    private static final int REQ_PHOTO = 0x102;
    private static final int REQ_CHOOSE_PHOTO = 0x103;

    
    public static void start(Activity context,int req){
        Intent intent = new Intent(context,ScanActivity.class);
        context.startActivityForResult(intent,req);
    }

    public static void start(Activity context,int req,boolean needAlbum){
        Intent intent = new Intent(context,ScanActivity.class);
        intent.putExtra("needAlbum",needAlbum);
        context.startActivityForResult(intent,req);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_scan);
        checkCameraPermission();
        initListener();
        initData();
    }

    private void initListener(){
        findViewById(R.id.iv_back).setOnClickListener(this);
        boolean needAlbum = getIntent().getBooleanExtra("needAlbum",false);
        if(needAlbum){
            findViewById(R.id.tv_ablumn).setOnClickListener(this);
            findViewById(R.id.tv_ablumn).setVisibility(View.INVISIBLE);
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        checkCameraPermission();
        CameraManager.init(getApplicationContext());
        SensorManager mSM = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor mLightSensor = mSM.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSM.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //获取光线强度
                float lux = event.values[0];
                if(lux > 10){
                    if(!isLightOpen && mLightLayout.getVisibility() == View.VISIBLE){
                        mLightLayout.setVisibility(View.GONE);
                    }
                }else {
                    //光线强度小于阈值时，显示电灯开关
                    if(mLightLayout.getVisibility() != View.VISIBLE){
                        mLightLayout.setVisibility(View.VISIBLE);
                        showLightAnim(mLightLayout);
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        },mLightSensor,SensorManager.SENSOR_DELAY_NORMAL);

    }

    /**
     * 灯展示出来时动画
     * @param view
     */
    private void showLightAnim(View view){
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(500);
        view.startAnimation(alphaAnimation);
    }

    private void initData(){
        mFragment = new CaptureFragment();
        mLightLayout = findViewById(R.id.ll_light);
        mLightLayout.setOnClickListener(this);
        CodeUtils.setFragmentArgs(mFragment,R.layout.layout_scan);
        mCallback = new CodeUtils.AnalyzeCallback() {
            @Override
            public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                Intent intent= new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt(CodeUtils.RESULT_TYPE,CodeUtils.RESULT_SUCCESS);
                bundle.putString(CodeUtils.RESULT_STRING,result);
                intent.putExtras(bundle);
                setResult(RESULT_OK,intent);
                finish();
            }

            @Override
            public void onAnalyzeFailed() {
                Intent intent= new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt(CodeUtils.RESULT_TYPE,CodeUtils.RESULT_FAILED);
                intent.putExtras(bundle);
                setResult(RESULT_OK,intent);
                finish();
            }
        };

        mFragment.setAnalyzeCallback(mCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_my_container,mFragment).commit();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.iv_back){
            this.finish();
            return;
        }
        if(id == R.id.tv_ablumn){
            checkAlbumPermission();
            return;
        }
        if(id == R.id.ll_light){
            openLight(!isLightOpen);
            return;
        }
    }

    /**
     * 控制闪光灯的打开关闭
     * @param isLightOpen
     */
    private void openLight(boolean isLightOpen) {
        this.isLightOpen = isLightOpen;
        CodeUtils.isLightEnable(isLightOpen);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQ_CHOOSE_PHOTO:
                if(resultCode == RESULT_OK){
                    String path  = "";
                    Uri uri = data.getData();
                    try {
                        path = UriUtils.getPath(getApplicationContext(),uri);
                        CodeUtils.analyzeBitmap(path,mCallback);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                default:break;
        }
    }

    /**
     * 检查相机权限
     */
    @TargetApi(23)
    private void checkCameraPermission(){
        int code = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if(code == PackageManager.PERMISSION_GRANTED){
            return;
        }
        if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
            Toast.makeText(getApplication(),"拒绝此权限将导致功能不可用！",Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQ_CAMERA);
        }else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},REQ_CAMERA);
        }
    }

    /**
     * 查看相册权限
     */
    @TargetApi(23)
    private void checkAlbumPermission(){
        int code = ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(code == PackageManager.PERMISSION_GRANTED){
            choosePhoto();
            return;
        }
        if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(getApplication(),"拒绝此权限将导致功能不可用！",Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQ_PHOTO);
        }else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQ_PHOTO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQ_CAMERA:
                break;
            case REQ_PHOTO:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    choosePhoto();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 跳转相册选择
     */
    private void choosePhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,REQ_CHOOSE_PHOTO);
    }
}
