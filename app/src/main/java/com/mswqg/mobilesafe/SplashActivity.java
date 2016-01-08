package com.mswqg.mobilesafe;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

public class SplashActivity extends Activity {
    private TextView tv_splash_version;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        tv_splash_version= (TextView) findViewById(R.id.tv_splash_version);
        tv_splash_version.setText("版本号："+getVersionName());
    }
    /**
     *获取应用程序的版本号  version name
     * */
    private String getVersionName(){
        //用来管理手机的apk。
        PackageManager pm=getPackageManager();
        //得到指定APK的功能清单文件。
        try {
            //得到指定APK的功能清单文件。
            PackageInfo pi=pm.getPackageInfo(getPackageName(), 0);
           return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }

    }
}
