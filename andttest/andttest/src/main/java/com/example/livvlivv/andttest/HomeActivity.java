package com.example.livvlivv.andttest;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.things.pio.UartDevice;

import java.io.IOException;
import java.util.Random;


public class HomeActivity extends BaseActivity implements MyUAObServer {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private Button bu_find, bu_time, bu_xbee, bu_finish, bu_conip;
    private MyUAService myuaService;
    private TextView tv_find, tv_time;
    private HomeActivity.MyServiceConn conn;
    private EditText et_conip;
    private int endSendsum = 0;
    private int endTimesum = 0;
    private int[] endsendNum = new int[65];
    private int[] endtimeNum = new int[65];
    private int randnum = 0;
    Random rand = new Random();


    private void showSendText(TextView tex, String record) {
        // Log.d(TAG, "showSendText: length =" + record.length());
        int rec = Integer.parseInt(record.substring(0, 2));
        if (endsendNum[rec] != 1) {
            endsendNum[rec] = 1;
            endSendsum++;
            tex.setText(rec + "号设备已上线,当前共" + endSendsum + "个");
        } else {
            Log.d(TAG, "showSendText1: 结点已上线，编号为：" + rec);

        }


    }

    //    private void showSendText(TextView tex ,String record) {
//        // Log.d(TAG, "showSendText: length =" + record.length());
//                endSendsum++;
//                tex.setText(record.charAt(1)+"号设备已上线,当前共"+endSendsum+"/"+5+"个" );
//
//    }
    private void showTimeText(TextView tex, String record) {
        // Log.d(TAG, "showSendText: length =" + record.length());
        int rec = Integer.parseInt(record.substring(0, 2));
        if (endtimeNum[rec] != 1) {
            endtimeNum[rec] = 1;
            endTimesum++;
            tex.setText(rec + "号设备已同步,当前共" + endTimesum + "个");
        } else {
            Log.d(TAG, "showTimeText: 结点已同步，编号为：" + rec);

        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atsmain);
        Log.i(TAG, "Starting BlinkActivity");
        bu_find = findViewById(R.id.button_find);
        bu_time = findViewById(R.id.button_settime);
        bu_xbee = findViewById(R.id.button_xbee);
        bu_finish = findViewById(R.id.button_end);
        bu_conip = findViewById(R.id.button_conip);
        et_conip = findViewById(R.id.ip_con);
        conn = new HomeActivity.MyServiceConn();
        tv_find = findViewById(R.id.textView_find);
        tv_time = findViewById(R.id.textView_endptime);
        bindService(new Intent(this, MyUAService.class), conn, BIND_AUTO_CREATE);
        bu_xbee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, ZigbeeActivity.class);
                HomeActivity.this.startActivity(i);

            }
        });
        bu_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeActivity.this.finish();
            }
        });

        bu_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myuaService != null) {
                    endSendsum = 0;
                    endTimesum = 0;
                    randnum = 5*100+(rand.nextInt(100));
                    //randnum =rad;
                    myuaService.sendMessagetoUAandJ(randnum);
                    //Log.d(TAG, "onClick: rad = "+rad+"randnum = "+randnum);

                    Log.d(TAG, "onClick: find start " + randnum);
                }
            }
        });
        bu_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myuaService != null) {
                    randnum = 6 * 100 + (rand.nextInt(100));

                    Log.d(TAG, "onClick: rad = " + randnum + "randnum = " + randnum);
                    myuaService.sendMessagetoUAandJ(randnum);
                    Log.d(TAG, "onClick: rad = " + randnum + "randnum = " + randnum);

                    Log.d(TAG, "onClick: time start " + randnum);

                }
            }
        });
        bu_conip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputIp = et_conip.getText().toString();
                //获取输入的ip并显示
                Log.d(TAG, "onClick: " + inputIp);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);

    }

    @Override
    void updataUI(String urData) {
        Log.d(TAG, "updataUI: urdata 45="+Integer.parseInt(urData.substring(4,5)));
        Log.d(TAG, "updataUI:randnum =" + randnum);

        if (urData.length() == 6 && Integer.parseInt(urData.substring(4, 5)) == randnum) {
            switch (urData.charAt(3)) {
                case '5':
                    showSendText(tv_find, urData);
                    break;
                case '6':
                    showTimeText(tv_time, urData);
                    break;
                default:
                    Log.d("text", "Data Worng！" + urData);

            }
            //}else{
            Log.d("text", "Data is not in HomeActity format." + urData);

            // }
        }


    }

    private class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myuaService = ((MyUAService.LocalBinder) service).getService();
            myuaService.registerObserver(HomeActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myuaService.removeObserver(HomeActivity.this);
            myuaService = null;
        }
    }
}


