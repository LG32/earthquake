package com.example.livvlivv.andttest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.UartDevice;
import com.google.android.things.pio.UartDeviceCallback;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Example activity that provides a UART loopback on the
 * specified device. All data received at the specified
 * baud rate will be transferred back out the same UART.
 *
 * MyUAService服务类：
 * 继承自Android的Service，引用了观察者模式中MyUASubject接口。该服务在启动后，实
 * 时读取Android Things硬件设备的串口，并与其他Activity类或数据通信类形成1对n通讯
 * 的关系，当串口读取到可用数据，通知其他观察者执行updata()方法。
 *
 */

public class MyUAService extends Service implements MyUASubject{

    boolean start_flag = false;
    private String record = "";
    private static final int BAUD_RATE = 9600;
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int CHUNK_SIZE = 32;
    private HandlerThread mInputThread;
    private Handler mInputHandler;
    private UartDevice mLoopbackDevice;

    private static final String TAG = "LoopbackActivity";
    private ArrayList<MyUAObServer> myUaObservers;
    public MyUAService() {
        myUaObservers = new ArrayList<>();
    }
    /**串口通讯需要放在线程中进行
     * */
    private Runnable mTransferUartRunnable = new Runnable() {
        @Override
        public void run() {
            transferUartData();
        }
    };

    /**服务的生命周期函数，创建时运行
    * */
    @Override
    public void onCreate() {
        super.onCreate();

        // Create a background looper thread for I/O
        mInputThread = new HandlerThread("InputThread");
        mInputThread.start();
        mInputHandler = new Handler(mInputThread.getLooper());

        // Attempt to access the UART device
        try {
            openUart(BoardDefaults.getUartName(), BAUD_RATE);
            // Read any initially buffered data
            mInputHandler.post(mTransferUartRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Unable to open UART device", e);
        }
    }
    /**服务的生命周期函数，销毁时运行
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Loopback Destroyed");

        // Terminate the worker thread
        if (mInputThread != null) {
            mInputThread.quitSafely();
        }

        // Attempt to close the UART device
        try {
            closeUart();
        } catch (IOException e) {
            Log.e(TAG, "Error closing UART device:", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new LocalBinder();
        //throw new UnsupportedOperationException("Not yet implemented");
    }
    public final class LocalBinder extends Binder {
        public MyUAService getService() {
            return MyUAService.this;
        }
    }
    /**
     * Callback invoked when UART receives new incoming data.
     */
    private UartDeviceCallback mCallback = new UartDeviceCallback() {
        @Override
        public boolean onUartDeviceDataAvailable(UartDevice uart) {
            // Queue up a data transfer
            transferUartData();
            //Continue listening for more interrupts
            return true;
        }

        @Override
        public void onUartDeviceError(UartDevice uart, int error) {
            Log.w(TAG, uart + ": Error event " + error);
        }
    };

    /* Private Helper Methods */

    /**
     * Access and configure the requested UART device for 8N1.
     *
     * @param name     Name of the UART peripheral device to open.
     * @param baudRate Data transfer rate. Should be a standard UART baud,
     *                 such as 9600, 19200, 38400, 57600, 115200, etc.
     * @throws IOException if an error occurs opening the UART port.
     */
    private void openUart(String name, int baudRate) throws IOException {


        mLoopbackDevice = PeripheralManager.getInstance().openUartDevice(name);
        // Configure the UART
        mLoopbackDevice.setBaudrate(baudRate);
        mLoopbackDevice.setDataSize(DATA_BITS);
        mLoopbackDevice.setParity(UartDevice.PARITY_NONE);
        mLoopbackDevice.setStopBits(STOP_BITS);
        mLoopbackDevice.registerUartDeviceCallback(mInputHandler, mCallback);
    }

    /**
     * Close the UART device connection, if it exists
     */
    private void closeUart() throws IOException {
        if (mLoopbackDevice != null) {
            mLoopbackDevice.unregisterUartDeviceCallback(mCallback);
            try {
                mLoopbackDevice.close();
            } finally {
                mLoopbackDevice = null;
            }
        }
    }

    /**
     * Loop over the contents of the UART RX buffer, transferring each
     * one back to the TX buffer to create a loopback service.
     * <p>
     * Potentially long-running operation. Call from a worker thread.
     */
    private void transferUartData() {

        if (mLoopbackDevice != null) {
            // Loop until there is no more data in the RX buffer.
            try {
                byte[] buffer = new byte[CHUNK_SIZE];
                int read;
                //int index = 0;
                while ((read = mLoopbackDevice.read(buffer, buffer.length)) > 0) {
                    String a = DataOp.bytesToHexFun2(buffer);
                    Log.d(TAG,"收到"+read+"b数据："+a);
                    for(int i = 0 ; i <read;i++)
                    {
                        if(buffer[i]==(byte)0x53)
                        {
                            start_flag = true;
                            //Log.d(TAG,"开始！");
                        }
                        else if(buffer[i]==(byte)0x4e)
                        {
                            start_flag = false;
                            //Log.d(TAG,"结束！");
                            //int reint = Integer.parseInt(asciiToString(record), 16);
                            Log.d(TAG,"record:"+record);
                            if(record!="") {
                                notifyObserver(record);
                                //showEndText(tv_all,record);
                                //Integer res = Integer.valueOf(record, 16);

                            }
                            record="";
                        }
                        else if(start_flag&&((buffer[i]>=(byte)0x30&&buffer[i]<=(byte)0x39)||(buffer[i]>=(byte)0x41&&buffer[i]<=(byte)0x46)))
                        {
                            char res = (char)buffer[i];
                            //Log.d(TAG,"解析！"+res);
                            record = record+res;
                            Log.d(TAG,"解析！"+res);
                        }
                    }
                    //byteOp(buffer,read);
                }
            } catch (IOException e) {
                Log.w(TAG, "Unable to transfer data over UART", e);
            }

        }
    }

    /**
     * 向串口中发送数据方法 现为固定值
     * **/
    public  void sendMessagetoUA(int command)//command = 9527;
    {
        if (mLoopbackDevice != null) {
            byte[] byteNum = new byte[4];
            for (int ix = 0; ix < 4; ++ix) {
                int offset = 32 - (ix + 1) * 8;
                byteNum[ix] = (byte) ((command >> offset) & 0xff);
                try {
                    mLoopbackDevice.write(byteNum, 4);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public  void sendMessagetoUAandJ(int command)//command = 9527;
    {
        if (mLoopbackDevice != null) {
            byte[] byteNum1 = new byte[4];
            byte[] byteNum2 = new byte[4];

            for (int ix = 0; ix < 4; ++ix) {
                int offset = 32 - (ix + 1) * 8;
                byteNum1[ix] = (byte) ((command >> offset) & 0xff);
                //byteNum[ix] = (byte) ((com >> offset) & 0xff);
                try {
                    mLoopbackDevice.write(byteNum1, 4);
                    Log.d(TAG, "sendMessagetoUAandJ: byte1 = " +(byteNum1[0] &0xff));
                    Log.d(TAG, "sendMessagetoUAandJ: byte2 = " +(byteNum1[1] &0xff));
                    Log.d(TAG, "sendMessagetoUAandJ: byte3 = " +(byteNum1[2] &0xff));
                    Log.d(TAG, "sendMessagetoUAandJ: byte4 = " +(byteNum1[3] &0xff));

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            /*for (int ixx = 0; ixx < 4; ++ixx) {
                int offsett = 32 - (ixx + 1) * 8;
                //byteNum[ix] = (byte) ((command >> offset) & 0xff);
                byteNum2[ixx] = (byte) ((check >> offsett) & 0xff);
                try {
                    mLoopbackDevice.write(byteNum2, 4);
                    Log.d(TAG, "sendMessagetoUAandJ: byte2 = "+byteNum2);

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }*/


            }
        }


    /**
     * 观察者模式 注册
     * **/
    @Override
    public void registerObserver(MyUAObServer o) {
        if(myUaObservers!=null&&o!=null)
        {
            myUaObservers.add(o);
        }
    }
    /**
     * 观察者模式 移除
     * **/
    @Override
    public void removeObserver(MyUAObServer o) {
        if(o!=null) {
            int i = myUaObservers.indexOf(o);
            myUaObservers.remove(i);
        }

    }
    /**
     * 观察者模式 通知观察者
     * **/
    @Override
    public void notifyObserver(String data) {
        for(int i = 0 ;i<myUaObservers.size();i++)
        {
            MyUAObServer observer = myUaObservers.get(i);
            observer.updata(data);
        }

    }
}
