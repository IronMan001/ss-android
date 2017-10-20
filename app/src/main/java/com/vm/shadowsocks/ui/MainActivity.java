package com.vm.shadowsocks.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vm.shadowsocks.R;
import com.vm.shadowsocks.bean.VpnInfoBean;
import com.vm.shadowsocks.constant.MsgConstant;
import com.vm.shadowsocks.core.AppInfo;
import com.vm.shadowsocks.core.AppProxyManager;
import com.vm.shadowsocks.core.LocalVpnService;
import com.vm.shadowsocks.core.ProxyConfig;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity implements
        View.OnClickListener,
        OnCheckedChangeListener,
        LocalVpnService.onStatusChangedListener {

    private static String GL_HISTORY_LOGS;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String CONFIG_URL_KEY = "CONFIG_URL_KEY";
    private static final String CONFIG_URL_KEY_SHOW = "CONFIG_URL_KEY_SHOW";

    private static final int START_VPN_SERVICE_REQUEST_CODE = 1985;

    private Switch switchProxy;
    private TextView textViewLog;
    private ScrollView scrollViewLog;
    private TextView textViewProxyUrl, textViewProxyApp,textViewProxyUrlshow;
    private Calendar mCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollViewLog = (ScrollView) findViewById(R.id.scrollViewLog);
        textViewLog = (TextView) findViewById(R.id.textViewLog);
        findViewById(R.id.ProxyUrlLayout).setOnClickListener(this);
        findViewById(R.id.ProxyUrlShow).setOnClickListener(this);
        findViewById(R.id.AppSelectLayout).setOnClickListener(this);

        textViewProxyUrl = (TextView) findViewById(R.id.textViewProxyUrl);
        textViewProxyUrlshow = (TextView) findViewById(R.id.textViewProxyUrlshow);
        String ProxyUrl = readProxyUrl();
        if (TextUtils.isEmpty(ProxyUrl)) {
            textViewProxyUrl.setText(R.string.config_not_set_value);
        } else {
            textViewProxyUrl.setText(ProxyUrl);
            String proxyUrl_show = readProxyUrlShow();
            textViewProxyUrlshow.setText(proxyUrl_show);
        }

        textViewLog.setText(GL_HISTORY_LOGS);
        scrollViewLog.fullScroll(ScrollView.FOCUS_DOWN);

        mCalendar = Calendar.getInstance();
        LocalVpnService.addOnStatusChangedListener(this);

        //Pre-App Proxy
        if (AppProxyManager.isLollipopOrAbove) {
            new AppProxyManager(this);
            textViewProxyApp = (TextView) findViewById(R.id.textViewAppSelectDetail);
        } else {
            ((ViewGroup) findViewById(R.id.AppSelectLayout).getParent()).removeView(findViewById(R.id.AppSelectLayout));
            ((ViewGroup) findViewById(R.id.textViewAppSelectLine).getParent()).removeView(findViewById(R.id.textViewAppSelectLine));
        }
    }

    String readProxyUrl() {
        SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE);
        return preferences.getString(CONFIG_URL_KEY, "");
    }

    /**
     * 读取 人类可读
     *
     * @return
     */
    String readProxyUrlShow() {
        SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrlshow", MODE_PRIVATE);
        return preferences.getString(CONFIG_URL_KEY_SHOW, "None");
    }

    /**
     * 保存 人类可读
     *
     * @param proxyUrlshow
     */
    void setProxyUrlShow(String proxyUrlshow) {
        SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrlshow", MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(CONFIG_URL_KEY_SHOW, proxyUrlshow);
        editor.apply();
    }

    void setProxyUrl(String ProxyUrl) {
        SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(CONFIG_URL_KEY, ProxyUrl);
        editor.apply();
    }

    String getVersionName() {
        PackageManager packageManager = getPackageManager();
        if (packageManager == null) {
            Log.e(TAG, "null package manager is impossible");
            return null;
        }

        try {
            return packageManager.getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "package not found is impossible", e);
            return null;
        }
    }

    boolean isValidUrl(String url) {
        try {
            if (url == null || url.isEmpty())
                return false;

            if (url.startsWith("ss://")) {//file path
                return true;
            } else { //url
                Uri uri = Uri.parse(url);
                if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme()))
                    return false;
                if (uri.getHost() == null)
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        if (switchProxy.isChecked()) {
            return;
        }

        if (v.getTag().toString().equals("ProxyUrl")) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.config_url)
                    .setItems(new CharSequence[]{
                            getString(R.string.config_url_scan),
                            getString(R.string.config_url_manual)
                    }, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    scanForProxyUrl();
                                    break;
                                case 1:
                                    showProxyUrlInputDialog();
                                    break;
                            }
                        }
                    })
                    .show();
        } else if (v.getTag().toString().equals("AppSelect")) {
            System.out.println("abc");
            startActivity(new Intent(this, AppManager.class));
        } else if (v.getTag().toString().equals("ProxyUrlshow")){
            System.out.println("radom");
            if(info.size()==0){
                Toast.makeText(this, "请先获取IP", Toast.LENGTH_SHORT).show();
            }else{
                VpnInfoBean vpnb = getRandom();
                while (vpnb.getPassword().contains("@")) {
                    vpnb = getRandom();
                }
                String show="Account:"+vpnb.getIpadd()+",\n密码:"+vpnb.getPassword()+",端口:"+vpnb.getPort()+",\n加密方式:"+vpnb.getSalt_pwd_type()+",国家地区:"+vpnb.getLocation()+",\n连接健康度:"+vpnb.getHealthe()+",时间:"+vpnb.getCurrt_time();
                textViewProxyUrlshow.setText(show);
                setProxyUrlShow(show);
                //ss://method:password@host:port
                String url = "ss://" + vpnb.getSalt_pwd_type() + ":" + vpnb.getPassword() + "@" + vpnb.getIpadd() + ":" + vpnb.getPort();
                setProxyUrl(url);
                textViewProxyUrl.setText(url);
            }
        }
    }

    /**
     * 随机获取bean
     *
     * @return
     */
    private VpnInfoBean getRandom() {
        int radom = (int) Math.floor(Math.random() * (info.size()));
        return info.get(radom);
    }

    private void scanForProxyUrl() {
        new IntentIntegrator(this)
                .setPrompt(getString(R.string.config_url_scan_hint))
                .initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    private void showProxyUrlInputDialog() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        editText.setHint(getString(R.string.config_url_hint));
        editText.setText(readProxyUrl());

        new AlertDialog.Builder(this)
                .setTitle(R.string.config_url)
                .setView(editText)
                .setPositiveButton(R.string.btn_ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText() == null) {
                            return;
                        }

                        String ProxyUrl = editText.getText().toString().trim();
                        if (isValidUrl(ProxyUrl)) {
                            setProxyUrl(ProxyUrl);
                            textViewProxyUrl.setText(ProxyUrl);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onLogReceived(String logString) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        logString = String.format("[%1$02d:%2$02d:%3$02d] %4$s\n",
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.SECOND),
                logString);

        System.out.println(logString);

        if (textViewLog.getLineCount() > 200) {
            textViewLog.setText("");
        }
        textViewLog.append(logString);
        scrollViewLog.fullScroll(ScrollView.FOCUS_DOWN);
        GL_HISTORY_LOGS = textViewLog.getText() == null ? "" : textViewLog.getText().toString();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    //访问网络
                    //访问一次google.如果成功，代表代理成功，如果不成功，就换一次地址。
                    //Q:是否需要开启一个子线程呢？

                    testNetProxyWork();
                    break;
                default:
                    onLogReceived("NO DATA handle");
                    break;
            }
        }
    };

    @Override
    public void onStatusChanged(String status, Boolean isRunning) {
        switchProxy.setEnabled(true);
        switchProxy.setChecked(isRunning);
        onLogReceived(status);
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        if(status.contains("已连接")){
            handler.sendEmptyMessageDelayed(1,3000);
        }else{
            handler.sendEmptyMessageDelayed(0,1000);
        }


    }

    private boolean global_flag=false;
    private void testNetProxyWork(){
//        global_flag=true;
        onLogReceived("请等待...翻墙中...");
//        createNETRequest("http://www.google.com");
        testNetWork("http://www.google.com");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (LocalVpnService.IsRunning != isChecked) {
            switchProxy.setEnabled(false);
            if (isChecked) {
                Intent intent = LocalVpnService.prepare(this);
                if (intent == null) {
                    startVPNService();
                } else {
                    startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
                }
            } else {
                LocalVpnService.IsRunning = false;
            }
        }
    }

    private void startVPNService() {
        String ProxyUrl = readProxyUrl();
        if (!isValidUrl(ProxyUrl)) {
            Toast.makeText(this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
            switchProxy.post(new Runnable() {
                @Override
                public void run() {
                    switchProxy.setChecked(false);
                    switchProxy.setEnabled(true);
                }
            });
            return;
        }

        textViewLog.setText("");
        GL_HISTORY_LOGS = null;
        onLogReceived("starting...");
        LocalVpnService.ProxyUrl = ProxyUrl;
        startService(new Intent(this, LocalVpnService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == START_VPN_SERVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVPNService();
            } else {
                switchProxy.setChecked(false);
                switchProxy.setEnabled(true);
                onLogReceived("canceled.");
            }
            return;
        }

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String ProxyUrl = scanResult.getContents();
            if (isValidUrl(ProxyUrl)) {
                setProxyUrl(ProxyUrl);
                textViewProxyUrl.setText(ProxyUrl);
            } else {
                Toast.makeText(MainActivity.this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_switch);
        if (menuItem == null) {
            return false;
        }

        switchProxy = (Switch) menuItem.getActionView();
        if (switchProxy == null) {
            return false;
        }

        switchProxy.setChecked(LocalVpnService.IsRunning);
        switchProxy.setOnCheckedChangeListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_about:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.app_name) + getVersionName())
                        .setMessage(R.string.about_info)
                        .setPositiveButton(R.string.btn_ok, null)
                        .setNegativeButton(R.string.btn_more, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/IronMan001/ss-android")));
                            }
                        })
                        .show();

                return true;
            case R.id.menu_item_exit:
                if (!LocalVpnService.IsRunning) {
                    finish();
                    return true;
                }

                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_item_exit)
                        .setMessage(R.string.exit_confirm_info)
                        .setPositiveButton(R.string.btn_ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LocalVpnService.IsRunning = false;
                                LocalVpnService.Instance.disconnectVPN();
                                stopService(new Intent(MainActivity.this, LocalVpnService.class));
                                System.runFinalization();
                                System.exit(0);
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();

                return true;
            case R.id.menu_item_refresh:
                request_count_failed=0;
//                global_flag=false;
                String url= MsgConstant.URL1 + System.currentTimeMillis();
                createNETRequest(url);
                return true;
            case R.id.menu_item_toggle_global:
                ProxyConfig.Instance.globalMode = !ProxyConfig.Instance.globalMode;
                if (ProxyConfig.Instance.globalMode) {
                    onLogReceived("Proxy global mode is on");
                } else {
                    onLogReceived("Proxy global mode is off");
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AppProxyManager.isLollipopOrAbove) {
            if (AppProxyManager.Instance.proxyAppInfo.size() != 0) {
                String tmpString = "";
                for (AppInfo app : AppProxyManager.Instance.proxyAppInfo) {
                    tmpString += app.getAppLabel() + ", ";
                }
                textViewProxyApp.setText(tmpString);
            }
        }
    }

    @Override
    protected void onDestroy() {
        LocalVpnService.removeOnStatusChangedListener(this);
        super.onDestroy();
    }

    ArrayList<VpnInfoBean> info = new ArrayList<VpnInfoBean>();

    //请求循环次数
    private int request_count_failed=0;

    /**
     * 测试网络是否通畅
     * @param url
     */
    private void testNetWork(String url){
        Request request = new Request.Builder().url(url).build();
        NetUtils.enqueue(request, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onLogReceived("换个IP吧兄弟~~~~");
                        }
                    });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseUrl = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(responseUrl)){
                                    onLogReceived("可以翻墙啦~~~~");
                                }
                            }
                        });
                }
            }
        });
    }

    /**
     * 发起网络请求操作
     */
    protected  void createNETRequest(String url){
        info.clear();
        String result="";

        Request request = new Request.Builder().url(url).build();
        NetUtils.enqueue(request, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
//                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
 /*               if(global_flag){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onLogReceived("换个IP吧兄弟~~~~");
                        }
                    });
                    return;
                }
                */
                if (e.getCause().equals(SocketTimeoutException.class)){
                   if( request_count_failed>5){
                       return; //停止网络请求
                   }
                    String url2=MsgConstant.URL2 + System.currentTimeMillis();
//                    NetUtils.getInstance().enqueue(request,url2);
                    createNETRequest(url2);
                    request_count_failed++;
                }

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseUrl = response.body().string();
                /*
                    if(global_flag){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(responseUrl)){
                                    onLogReceived("可以翻墙啦~~~~");
                                }


                            }
                        });
                        return;
                    }
                  */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onLogReceived("地址源已刷新");
                        }
                    });

                    JSONObject jsonObject=JSON.parseObject(responseUrl);
                    JSONArray jsonarr=jsonObject.getJSONArray("data");

                    for(int i=0;i<jsonarr.size();i++){
                        JSONArray arr=(JSONArray)jsonarr.get(i);
                        String zero=arr.get(0).toString();
                        int first=Integer.valueOf(zero).intValue();
                        VpnInfoBean vpn = new VpnInfoBean(first,arr.get(1).toString(),arr.get(2).toString(),arr.get(3).toString(),arr.get(4).toString(),arr.get(5).toString(),arr.get(6).toString());
                        info.add(vpn);
                    }
                }
            }
        });
    }

}
