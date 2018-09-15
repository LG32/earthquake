package com.example.livvlivv.andttest;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;


public class HomeActivity extends Activity implements MyUAObServer{
    private static final String TAG = HomeActivity.class.getSimpleName();
    private Button bu_find,bu_time,bu_xbee,bu_finish,bu_conip;
    private MyUAService myuaService;
    private HomeActivity.MyServiceConn conn;
    public EditText et_conip;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atsmain);

        Log.i(TAG, "Starting BlinkActivity");
        bu_find =  findViewById(R.id.button_find);
        bu_time = findViewById(R.id.button_settime);
        bu_xbee = findViewById(R.id.button_xbee);
        bu_finish = findViewById(R.id.button_end);
        bu_conip = findViewById(R.id.button_conip);
        et_conip = findViewById(R.id.ip_con);

        conn = new HomeActivity.MyServiceConn();
        bindService(new Intent(this,MyUAService.class),conn,BIND_AUTO_CREATE);
        bu_xbee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this,ZigbeeActivity.class);
                Bundle bundle = new Bundle();

                HomeActivity.this.startActivity(i);
                bundle.putSerializable("socket", (Serializable) socket);
                i.putExtra("bundle", bundle);
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
                if(myuaService!=null)
                {
                    myuaService.sendMessagetoUA(5);
                }
            }
        });
        bu_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myuaService!=null)
                {
                    myuaService.sendMessagetoUA(6);
                }
            }
        });

        bu_conip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputIp = et_conip.getText().toString();
                //获取输入的ip并显示
                Log.d(TAG, "onClick: "+inputIp);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);

    }

    @Override
    public void updata(String urData) {

       Log.d("HomeActivity","String："+urData);

    }

    private class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myuaService = ( (MyUAService.LocalBinder)service).getService();
            myuaService.registerObserver(HomeActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myuaService.removeObserver(HomeActivity.this);
            myuaService = null;
        }
    }

    /**
     * homeActivity 页,连接成功后将socket值传入zigbee页
     */
    public class SocketConnect extends Thread {

        String ip;
        int port;

        /**
         * @param ip   ip地址
         * @param port 端口号
         */
        SocketConnect(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(ip, port);
                socket.setSoTimeout(5000); //5秒超时
//                ClientThread clientThread = new ClientThread(socket);
//                clientThread.start();
                if(socket.isClosed() == false && socket.isConnected() == true){
                    Log.d(TAG, "SocketConnect: 连接成功");
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                Log.e("UnknownHost", "来自服务器的数据");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("IOException", "来自服务器的数据");
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}