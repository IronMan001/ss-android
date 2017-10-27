package com.vm.shadowsocks.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.vm.shadowsocks.R;
import com.vm.shadowsocks.constant.MsgConstant;

public class SplashManager extends Activity implements View.OnClickListener{
    private EditText et_activie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_manager);
        et_activie  =(EditText)findViewById(R.id.et_active);
        findViewById(R.id.btn_active).setOnClickListener(this);
        String code=readCode();
        if(!TextUtils.isEmpty(code)){
            startActivityForResult(new Intent(SplashManager.this,MainActivity.class),0);
        }
    }

    @Override
    public void onClick(View v) {
        String tag=v.getTag().toString();
        if(tag.equals("active")){
            //开始验证激活码的内容
            String content=et_activie.getText().toString();
            if(TextUtils.isEmpty(content)){
                Toast.makeText(getApplicationContext(),"验证码不可为空",Toast.LENGTH_SHORT).show();
                return;
            }
            Message msg=Message.obtain();
            msg.what=1;
            handler.sendMessage(msg);
        }
    }

    private Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    Message msg1=Message.obtain();
                    int status_actived=0;
                    Toast.makeText(getApplicationContext(), "激活中，请稍候！", Toast.LENGTH_SHORT).show();
                    String content=et_activie.getText().toString();
                    for (int i = 0; i<MsgConstant.ACTIVIE_CODES.length;i++){
                        if(content.equals(MsgConstant.ACTIVIE_CODES[i])){
                            msg1.what=2;
                            status_actived=1;
                            setCode(content);
                            break;
                        }
                    }
                    if(status_actived==0){
                        msg1.what=3;
                    }
                    handler.sendMessageDelayed(msg1,3000);
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "激活成功！", Toast.LENGTH_SHORT).show();
                    startActivityForResult(new Intent(SplashManager.this.getApplicationContext(),MainActivity.class),1);
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(), "激活失败！激活码不正确！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


    String readCode() {
        SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE);
        return preferences.getString(CONFIG_ACTIVECODE, "");
    }

    void setCode(String code) {
        SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(CONFIG_ACTIVECODE, code);
        editor.apply();
    }
    private static final String CONFIG_ACTIVECODE = "CONFIG_ACTIVECODE";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
