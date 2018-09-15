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




public class HomeActivity extends Activity implements MyUAObServer{
    private static final String TAG = HomeActivity.class.getSimpleName();
    private Button bu_find,bu_time,bu_xbee,bu_finish;
    private MyUAService myuaService;
    private HomeActivity.MyServiceConn conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atsmain);
        Log.i(TAG, "Starting BlinkActivity");
        bu_find =  findViewById(R.id.button_find);
        bu_time = findViewById(R.id.button_settime);
        bu_xbee = findViewById(R.id.button_xbee);
        bu_finish = findViewById(R.id.button_end);
        conn = new HomeActivity.MyServiceConn();
        bindService(new Intent(this,MyUAService.class),conn,BIND_AUTO_CREATE);
        bu_xbee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this,ZigbeeActivity.class);
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


}