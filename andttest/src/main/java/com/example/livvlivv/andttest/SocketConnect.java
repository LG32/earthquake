package com.example.livvlivv.andttest;

import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.content.ContentValues.TAG;


/**
 * homeActivity 页,连接成功后将socket值传入zigbee页
 */
public class SocketConnect extends Thread{

    private String ip;
    private int port;
    private static Socket socket;
    private boolean ifListen = true;
    private ZigbeeActivity.Myhandler myhandler;
    private MyUAService myUAService;


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
        Message message = new Message();
        try {
            socket = new Socket(ip, port);
            socket.setSoTimeout(5000); //5秒超时

            if(!socket.isClosed() && socket.isConnected()){
                Log.d(TAG, "SocketConnect: 连接成功");
                ClientThread clientThread = new ClientThread();
                clientThread.start();
                message.what = 2;
                myhandler.sendMessage(message);
            }
        } catch (UnknownHostException e) {
            String msg = "未找到服务器";
            Log.e("UnknownHost", msg);
            message.what = 3;
            message.obj = msg;
            myhandler.sendMessage(message);
            e.printStackTrace();
        } catch (IOException e) {
            String msg = "来自服务器的数据出错";
            Log.e("IOException", msg);
            message.what = 3;
            message.obj = msg;
            myhandler.sendMessage(message);
            e.printStackTrace();
        }
    }


    /**
     * 在zigbee页中，监听服务器通知
     */
    public class ClientThread extends Thread {

        Message message2 = new Message();


        public void run() {
            Log.d(TAG, "监听中...");
            while (ifListen) {
                try {
                    String content;
                    InputStream is;
                    is = socket.getInputStream();
                    InputStreamReader reader = new InputStreamReader(is);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    while ((content = bufferedReader.readLine()) != null) {
                        Log.i(TAG, "监听到！");
                        Log.i(TAG, "content: " + content);

//                        开始实验
                        if(content.equals ("start")){
                            Log.i ( TAG, "run: start come" + content);
//                            myUAService.sendMessagetoUA(8);
                            message2.what = 4;
                            myhandler.sendMessage(message2);
                        }

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

    /**
     * 传输数据给服务端
     */
    public static class SocketRequest extends Thread {

        private String data;

        SocketRequest(String data){this.data = data;}

        @Override
        public void run() {
            try {
                BufferedWriter write = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                write.write(data + '\n');
                write.flush();
//                write.close ();
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

    public void setMyUAService(MyUAService myUAService) {
        this.myUAService = myUAService;
        Log.i ( TAG, "setMyUAService: myUAService" + myUAService);
    }

    public void setMyhandler(ZigbeeActivity.Myhandler myhandler) {
        this.myhandler = myhandler;
    }

    public boolean isIfListen() {
        return ifListen;
    }

    public void setIfListen(boolean ifListen) {
        this.ifListen = ifListen;
    }

    public Socket getSocket() {
        return socket;
    }
}