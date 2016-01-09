package com.mswqg.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.mswqg.mobilesafe.utils.StreamTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";
    private static final int ENTER_HOME = 1;
    private static final int SHOW_UPDATE_DIALOG = 0;
    private static final int URL_ERROR = 2;
    private static final int NETWORK_ERROR = 3;
    private static final int JSON_ERROR = 4;

    private TextView tv_splash_version;
    private String description;
    private String apkurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
        tv_splash_version.setText("版本号：" + getVersionName());
        //检查升级
        checkUpdate();
        AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
        aa.setDuration(1000);
        findViewById(R.id.rl_root_splash).startAnimation(aa);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_UPDATE_DIALOG://显示升级对话框。
                    Log.i(TAG, "显示升级对话框");
                    showUpdateDialog();
                    break;
                case ENTER_HOME://进入主页面
                    enterHome();
                    break;
                case URL_ERROR://URL错误
                    enterHome();
                    Toast.makeText(getApplicationContext(), "URL错误", Toast.LENGTH_SHORT).show();
                    break;
                case NETWORK_ERROR://网络错误
                    enterHome();
                    Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_SHORT).show();
                    break;
                case JSON_ERROR://JSON错误
                    enterHome();
                    Toast.makeText(SplashActivity.this, "JSON错误", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    /*
    * 弹出升级对话框
    * */
    private void showUpdateDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("提醒升级");
        builder.setMessage(description);
        builder.setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //下载apk，并且安装。
            }
        });
        builder.setNegativeButton("下次再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                enterHome();//进入主页面。
            }
        });
        builder.show();
    }

    private void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        //关闭当前页面。
        finish();
    }
    /**
     * 检查是否有新版本，如果有就升级。
     */
    private void checkUpdate() {
        new Thread() {
            public void run() {
                //URL http://192.168.1.102:8080/updateinfo.html
                Message mes = Message.obtain();
                long startTime = System.currentTimeMillis();
                try {
                    URL url = new URL(getString(R.string.serverurl));
                    //联网
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(4000);
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        //联网成功。
                        InputStream is = conn.getInputStream();
                        //把流转换成String类型。
                        String result = StreamTools.readFromStream(is);
                        Log.i(TAG, "联网成功" + result);
                        //json解析
                        JSONObject obj = new JSONObject(result);
                        //得到服务器的版本信息
                        String version = (String) obj.get("version");
                        description = (String) obj.get("description");
                        apkurl = (String)obj.get("apkurl");
                        //校验是否有新版本
                        if (getVersionName().equals(version)) {
                            //版本一致，进入主页面。
                            mes.what = ENTER_HOME;
                        } else {
                            //有新版本弹出升级对话框。
                            mes.what = SHOW_UPDATE_DIALOG;
                        }
                    }
                } catch (MalformedURLException e) {
                    mes.what = URL_ERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    mes.what = NETWORK_ERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    mes.what = JSON_ERROR;
                    e.printStackTrace();
                } finally {
                    long endTime = System.currentTimeMillis();
                    //我们花了多少时间。
                    long dTime = endTime - startTime;
                    if (dTime < 2000) {
                        try {
                            Thread.sleep(2000 - dTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    handler.sendMessage(mes);
                }

            }

        }.start();

    }

    /**
     * 获取应用程序的版本号  version name
     */
    private String getVersionName() {
        //用来管理手机的apk。
        PackageManager pm = getPackageManager();
        //得到指定APK的功能清单文件。
        try {
            //得到指定APK的功能清单文件。
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }

    }
}
