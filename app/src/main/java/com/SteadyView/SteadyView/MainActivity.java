package com.SteadyView.SteadyView;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastSensorTimestamp = -1;

    WebView web;

    int width = 1080;
    int height = 1920;

    double widthMeters;
    double heightMeters;

    double xdpm = 1;
    double ydpm = 1;

    double[] v = {0,0};
    double[] p = {0,0};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        web = (WebView)findViewById(R.id.webview);
        web.getSettings().setJavaScriptEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl("http://reddit.com");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        width = size.x;
        height = size.y;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        widthMeters = 0.0254*(double)width/metrics.xdpi;
        heightMeters = 0.0254*(double)height/metrics.ydpi;
        xdpm = metrics.xdpi/0.0254;
        ydpm = metrics.ydpi/0.0254;

        //float inchpix = (float) (metrics.xdpi);
        //web.setX(inchpix);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        System.out.println(event.values[0] + ","+event.values[1]+","+event.values[2]);
        //0 = x, 1 = y, 2 = z

        if(lastSensorTimestamp != -1) {
            double dt = (double)(event.timestamp-lastSensorTimestamp)/1.0e9;
            for(int i = 0; i < 2; i++) {
                if(Math.abs(event.values[i]) >  0.2){
                    v[i] += (event.values[i])*dt;
                }
                v[i] *=0.95;


                p[i] += v[i]*dt;
                p[i]*=0.9;

            }
            web.setX((float)(-xdpm*p[0]));
            web.setY((float)(-ydpm*p[1]));

            System.out.println(dt +",("+ p[0] +","+ p[1] +"),("+v[0] +","+ v[1]+")");

        }

        lastSensorTimestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        lastSensorTimestamp = -1;
    }
}