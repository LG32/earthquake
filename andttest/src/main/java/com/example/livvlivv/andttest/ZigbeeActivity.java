package com.example.livvlivv.andttest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ZigbeeActivity extends Activity implements MyUAObServer {
    private static final String TAG = "LoopbackActivity";
    TextView tv_get, tv_get1, tv_get2, tv_get3, tv_get4, tv_send;
    TextView[] tv_all;
    private Button bu_send, bu_finish;
    private MyUAService myuaService;
    private MyServiceConn conn;
    private boolean canshow = false;
    private SocketConnect socketConnect;
    private Myhandler myhandler = new Myhandler ();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.layout );
        String ip;

        tv_get = findViewById ( R.id.textView_get );
        tv_get1 = findViewById ( R.id.textView_get_two );
        tv_get2 = findViewById ( R.id.textView_get_three );
        tv_get3 = findViewById ( R.id.textView_get_four );
        tv_get4 = findViewById ( R.id.textView_get_five );
        tv_send = findViewById ( R.id.textView_info );
        bu_finish = findViewById ( R.id.button_finish );
        bu_send = findViewById ( R.id.button_send );
        tv_all = new TextView[5];
        tv_all[0] = tv_get;
        tv_all[1] = tv_get1;
        tv_all[2] = tv_get2;
        tv_all[3] = tv_get3;
        tv_all[4] = tv_get4;
        canshow = true;

        bu_finish.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                myuaService.sendMessagetoUA ( 9 );
                ZigbeeActivity.this.finish ();
            }
        } );

        bu_send.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                myuaService.sendMessagetoUA ( 8 );
            }
        } );

        conn = new MyServiceConn ();
        bindService ( new Intent ( this, MyUAService.class ), conn, BIND_AUTO_CREATE );

        Bundle ip_extras = getIntent ().getExtras ();
        if (ip_extras != null) {
            ip = ip_extras.getString ( "ip" );
            Log.i ( TAG, "onCreate: ip is " + ip );
            socketConnect = new SocketConnect ( ip, 2002 );
//            socketConnect.setMyUAService ( myuaService );
            socketConnect.setMyhandler ( myhandler );
            socketConnect.start ();
        }
    }

    @SuppressLint("HandlerLeak")
    public class Myhandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage ( msg );
            if (msg != null) {
                switch (msg.what) {
                    case 1:
                        showEndText ( tv_all, msg.obj.toString () );              //得到底端数据
                        break;
                    case 2:
                        Toast toast1 = Toast.makeText ( ZigbeeActivity.this,
                                "连接上服务器",
                                Toast.LENGTH_SHORT );
                        toast1.setGravity ( Gravity.CENTER, 0, 0 );
                        toast1.show ();
                        break;
                    case 3:
                        Toast toast2 = Toast.makeText ( ZigbeeActivity.this,
                                msg.obj.toString (),
                                Toast.LENGTH_SHORT );
                        toast2.setGravity ( Gravity.CENTER, 0, 0 );
                        toast2.show ();
                        break;
                    case 4:
                        myuaService.sendMessagetoUA ( 8 );
                        Toast toast3 = Toast.makeText ( ZigbeeActivity.this,
                                "开始实验",
                                Toast.LENGTH_SHORT);
                        toast3.setGravity ( Gravity.CENTER, 0, 0 );
                        toast3.show ();
                        break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        unbindService ( conn );
        canshow = false;
    }

    @SuppressLint("SetTextI18n")
    private void showEndText(TextView[] tv_list, String record) {
        if (tv_list.length > 0) {
            switch (record.charAt ( 1 )) {
                case '2':
                    tv_list[0].setText ( "收到数据：" + record );
                    SocketConnect.SocketRequest socketRequest1 = new SocketConnect.SocketRequest ( toDataString ( record ) );
                    socketRequest1.start ();
                    break;
                case '3':
                    tv_list[1].setText ( "收到数据：" + record );
                    SocketConnect.SocketRequest socketRequest2 = new SocketConnect.SocketRequest ( toDataString ( record ) );
                    socketRequest2.start ();
                    break;
                case '4':
                    tv_list[2].setText ( "收到数据：" + record );
                    SocketConnect.SocketRequest socketRequest3 = new SocketConnect.SocketRequest ( toDataString ( record ) );
                    socketRequest3.start ();
                    break;
                case '5':
                    tv_list[3].setText ( "收到数据：" + record );
                    SocketConnect.SocketRequest socketRequest4 = new SocketConnect.SocketRequest ( toDataString ( record ) );
                    socketRequest4.start ();
                    break;
                case '6':
                    tv_list[4].setText ( "收到数据：" + record );
                    SocketConnect.SocketRequest socketRequest5 = new SocketConnect.SocketRequest ( toDataString ( record ) );
                    socketRequest5.start ();
                    break;
                default:
                    Log.d ( "text", "Data Wrong！" + record );
            }
        }
    }

    @Override
    public void updata(String urData) {
        Message mymessage = myhandler.obtainMessage ( 1, urData );
        myhandler.sendMessage ( mymessage );
    }

    private class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myuaService = ((MyUAService.LocalBinder) service).getService ();
            myuaService.registerObserver ( ZigbeeActivity.this );
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myuaService.removeObserver ( ZigbeeActivity.this );
            myuaService = null;
        }
    }

    public String toDataString(String data) {
        return ("S" + data + "N");
    }
}

