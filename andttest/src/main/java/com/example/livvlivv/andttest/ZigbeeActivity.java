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
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.content.ContentValues.TAG;


public class ZigbeeActivity extends Activity implements MyUAObServer {
    private static final String TAG = "LoopbackActivity";
    TextView tv_get, tv_get1, tv_get2, tv_get3, tv_get4, tv_send;
    TextView[] tv_all;
    private Button bu_send, bu_finish;
    private MyUAService myuaService;
    private MyServiceConn conn;
    private boolean canshow = false;
    private Socket socket;
    private boolean ifListen = true;

    @SuppressLint("HandlerLeak")
    Handler myhandler =  new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                switch (msg.what) {
                    case 1:
                        showEndText(tv_all, msg.obj.toString());
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);

        Bundle bundle = (Bundle)getIntent().getExtras().get("bundle");
        socket = bundle.getParcelable("socket");

        tv_get = findViewById(R.id.textView_get);
        tv_get1 = findViewById(R.id.textView_get_two);
        tv_get2 = findViewById(R.id.textView_get_three);
        tv_get3 = findViewById(R.id.textView_get_four);
        tv_get4 = findViewById(R.id.textView_get_five);
        tv_send = findViewById(R.id.textView_info);
        bu_finish = findViewById(R.id.button_finish);
        bu_send = findViewById(R.id.button_send);
        tv_all = new TextView[5];
        tv_all[0] = tv_get;
        tv_all[1] = tv_get1;
        tv_all[2] = tv_get2;
        tv_all[3] = tv_get3;
        tv_all[4] = tv_get4;
        canshow = true;


        bu_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myuaService.sendMessagetoUA(9);
                ZigbeeActivity.this.finish();
            }
        });
        bu_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myuaService.sendMessagetoUA(8);
            }
        });

        conn = new MyServiceConn();
        bindService(new Intent(this, MyUAService.class), conn, BIND_AUTO_CREATE);

        ClientThread clientThread = new ClientThread(socket);
        clientThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        canshow = false;

    }

    @SuppressLint("SetTextI18n")
    private void showEndText(TextView[] tv_list, String record) {
        if (tv_list.length > 0) {
            switch (record.charAt(1)) {
                case '2':
                    tv_list[0].setText("收到数据：" + record);
                    break;
                case '3':
                    tv_list[1].setText("收到数据：" + record);
                    break;
                case '4':
                    tv_list[2].setText("收到数据：" + record);
                    break;
                case '5':
                    tv_list[3].setText("收到数据：" + record);
                    break;
                case '6':
                    tv_list[4].setText("收到数据：" + record);
                    break;
                default:
                    Log.d("text", "Data Wrong！" + record);
            }
        }
    }


    @Override
    public void updata(String urData) {
        Message mymessage = myhandler.obtainMessage(1, urData);
        myhandler.sendMessage(mymessage);
    }

    private class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myuaService = ((MyUAService.LocalBinder) service).getService();
            myuaService.registerObserver(ZigbeeActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myuaService.removeObserver(ZigbeeActivity.this);
            myuaService = null;
        }
    }

    /**
     * Zigbee class 页，使用homeactivity页中的socket
     */
    public class SocketRequest extends Thread {

        @Override
        public void run() {
            String line;
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            try {
                OutputStream os = socket.getOutputStream();
                String testdata = "S1122222222FFFFN";
                os.write(testdata.getBytes());
                os.flush();
                socket.shutdownOutput();
                InputStream is = socket.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(reader);
                line = br.readLine();
                if (line != null) {
                    Log.d(TAG, "socketRequest服务器返回的数据: " + line);
                } else {
                    Log.d(TAG, "socketRequest异常");
                    line = "异常";
                }
                br.close();
                is.close();
                os.close();

                Message message = new Message();
                message.what = 2;
                message.obj = line;
                myhandler.sendMessage(message);
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

    /**
     * 在zigbee页中，监听服务器通知
     */
    public class ClientThread extends Thread {

        private Socket socket;

        ClientThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            while (ifListen) {
                try {
                    String content;
                    Log.d(TAG, "监听中...");
                    InputStream is;
                    is = socket.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    while ((content = bufferedReader.readLine()) != null) {
                        Log.d(TAG, "监听到！");
                        Log.d(TAG, "content: " + content);
                        Bundle bundle = new Bundle();
                        bundle.putString("serverMes", content);
                        Message msg = new Message();
                        msg.what = 3;
                        msg.obj = bundle;
                        myhandler.sendMessage(msg);
                    }
                    is.close();
                    reader.close();
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

