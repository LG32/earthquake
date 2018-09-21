package com.example.livvlivv.andttest;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import static android.content.ContentValues.TAG;

public abstract class BaseActivity extends Activity implements MyUAObServer {

    Handler myhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg!=null)
            {
                updataUI(msg.obj.toString());

            }
        }
    };

    @Override
    public void updata(String urData) {
        Log.d(TAG, "updata: String :"+urData);
        Message mymessage = myhandler.obtainMessage(1,urData);
        myhandler.sendMessage(mymessage);
    }

    abstract void updataUI(String urData);



}
