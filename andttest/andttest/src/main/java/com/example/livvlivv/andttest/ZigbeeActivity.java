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
import android.widget.TextView;

import java.util.Random;


public class ZigbeeActivity extends BaseActivity implements MyUAObServer{
    private static final String TAG = "LoopbackActivity";
    TextView tv_get,tv_get1,tv_get2,tv_get3,tv_get4, tv_send;
    TextView[] tv_all ;
    private Button bu_send,bu_finish;
    private MyUAService myuaService;
    private MyServiceConn conn;
    private boolean canshow = false;

//    Handler myhandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if(msg!=null)
//            {
//                showEndText(tv_all,msg.obj.toString());
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        tv_get = findViewById(R.id.textView_get);
        tv_get1 = findViewById(R.id.textView_get_two);
        tv_get2 = findViewById(R.id.textView_get_three);
        tv_get3 = findViewById(R.id.textView_get_four);
        tv_get4 = findViewById(R.id.textView_get_five);
        tv_send = findViewById(R.id.textView_info);
        bu_finish = findViewById(R.id.button_finish);
        bu_send = findViewById(R.id.button_send);
        tv_all = new TextView[5];
        tv_all[0]=tv_get;
        tv_all[1]=tv_get1;
        tv_all[2]=tv_get2;
        tv_all[3]=tv_get3;
        tv_all[4]=tv_get4;
        canshow = true;



        bu_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myuaService.sendMessagetoUA(9);
                ZigbeeActivity.this.finish();
            }
        });
        bu_send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                myuaService.sendMessagetoUA(8);
            }
        });

        conn = new MyServiceConn();
        bindService(new Intent(this,MyUAService.class),conn,BIND_AUTO_CREATE);



}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        canshow = false;

    }

    private void showEndText(TextView[] tv_list, String record) {
         if(tv_list.length>0)
         {
                 switch (record.charAt(1))
                 {
                     case '2':tv_list[0].setText("收到数据："+ record);break;
                     case '3':tv_list[1].setText("收到数据：" +record);break;
                     case '4':tv_list[2].setText("收到数据："+ record);break;
                     case '5':tv_list[3].setText("收到数据：" +record);break;
                     case '6':tv_list[4].setText("收到数据：" +record);break;
                     default:Log.d("text","Data Worng！"+record);
                 }
         }
    }


    @Override
    public void updata(String urData) {
        Message mymessage = myhandler.obtainMessage(1,urData);
        myhandler.sendMessage(mymessage);
    }

    @Override
    void updataUI(String urData) {
        showEndText(tv_all,urData);
    }

    private class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myuaService = ( (MyUAService.LocalBinder)service).getService();
            myuaService.registerObserver(ZigbeeActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myuaService.removeObserver(ZigbeeActivity.this);
            myuaService = null;
        }
    }
}

