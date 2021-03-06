package com.example.smartvest;

import static app.akexorcist.bluetotohspp.library.BluetoothState.REQUEST_ENABLE_BT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class WelcomActivity extends AppCompatActivity {
    TextView logout_worker;
    LinearLayout location_worker;
    LinearLayout manual_worker;
    LinearLayout report_worker;
    TextView textTitle;
    ImageView dustLogo;
    ImageView coLogo;

    private long backKeyPressedTime = 0;
    private Toast toast;
    private List<UserLocation> userLocationList;
    private List<UserLocation> saveLocationList;
    private BluetoothSPP bt;
    ImageView bluetooth_connect;
    long now = System.currentTimeMillis();

    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        String userID= intent.getStringExtra("userID");
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "?????? ?????? ????????? ??? ??? ??? ???????????? ???????????????.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            finish();
            bt.disconnect();
            bt.stopService();
            toast.cancel();
            toast = Toast.makeText(this,"????????? ????????? ???????????????.",Toast.LENGTH_LONG);
            toast.show();

            Response.Listener<String> responseListener = new Response.Listener<String>(){
                @Override
                public void onResponse(String response)
                {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        boolean success = jsonResponse.getBoolean("success");
                        if (success) {
                            //userLocationList.remove(final i);
                            for(int i = 0; i<saveLocationList.size(); i++)
                            {
                                if(saveLocationList.get(i).getUserID().equals(userID))
                                {
                                    saveLocationList.remove(i);
                                    break;
                                }
                            }
                            // notifyDataSetChanged();
                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            };
            LocationDeleteRequest LocationdeleteRequest = new LocationDeleteRequest(userID, responseListener);
            RequestQueue queue = Volley.newRequestQueue(WelcomActivity.this);
            queue.add(LocationdeleteRequest);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);

        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String str_datetime = sdfNow.format(date);
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        Timer timer = new Timer();

        textTitle=findViewById(R.id.textTitle);
        textTitle.setText(userID);
        location_worker = findViewById(R.id.location_worker);
        manual_worker = findViewById(R.id.manual_worker);

        //???????????? ??????, ????????? ????????? ?????? ??????
        logout_worker = findViewById(R.id.logout_worker);
        logout_worker.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        logout_worker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bt.disconnect();
                bt.stopService();
                Toast.makeText(WelcomActivity.this, "???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                Intent in = new Intent(WelcomActivity.this, MainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(in);
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");
                            if (success) {
                                //userLocationList.remove(final i);
                                for (int i = 0; i < saveLocationList.size(); i++) {
                                    if (saveLocationList.get(i).getUserID().equals(userID)) {
                                        saveLocationList.remove(i);
                                        break;
                                    }
                                }
                                // notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                };
                LocationDeleteRequest LocationdeleteRequest = new LocationDeleteRequest(userID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(WelcomActivity.this);
                queue.add(LocationdeleteRequest);
            }
        });
        //?????????
        report_worker = findViewById(R.id.report_worker);
        report_worker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WorkerCalendarView.class);
                startActivity(intent);
                intent.putExtra("userID", userID);
            }
        });
        //????????? ?????? ?????? ??????
        location_worker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WorkerMapActivity.class);
                intent.putExtra("userID", userID);
                WelcomActivity.this.startActivity(intent);

            }
        });
        //????????? ?????????
        manual_worker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WorkerManualActivity.class);
                startActivity(intent);
            }
        });


        bt = new BluetoothSPP(this);
        if (!bt.isBluetoothAvailable()) { //???????????? ?????? ??????
            Toast.makeText(getApplicationContext()
                    , "???????????? ?????? ?????????"
                    , Toast.LENGTH_SHORT).show();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            TextView dustText = findViewById(R.id.dustText);
            TextView tempText = findViewById(R.id.tempText);
            TextView humText = findViewById(R.id.humText);
            TextView coText = findViewById(R.id.coText);
            //????????? ??????
            public void onDataReceived(byte[] data, String message) {
                String btn= message.substring(0,1);
                String dust = message.substring(1,5);
                String co= message.substring(5,10);
                String hum=message.substring(10,13);
                String temp = message.substring(13);

                String str_btn= btn;
                String str_dust=dust;
                String str_co=co;
                String str_hum=hum;
                String str_temp=temp;

                int bbtn = Integer.parseInt(btn);
                int ddust = Integer.parseInt(dust);
                int cco = Integer.parseInt(co);
                if(bbtn == 1)
                {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(WelcomActivity.this);
                    builder.setMessage("?????????????????????.")
                            .setPositiveButton("??????", null)
                            .create()
                            .show();

                }
                if(cco >=0 && cco <=20)
                {
                    coLogo.setImageResource(R.drawable.good);
                    coText.setText(co.concat("ppm"));
                }else if( ddust>=21 && ddust <=400)
                {
                    coLogo.setImageResource(R.drawable.soso);
                    coText.setText(co.concat("ppm"));
                }else if( ddust>=401 && ddust <= 800)
                {
                    coText.setText(co.concat("ppm"));
                    coLogo.setImageResource(R.drawable.bad);
                }else if( ddust>=801 )
                {   coText.setTextColor(Color.RED);
                    coText.setText(co.concat("ppm"));
                    coLogo.setImageResource(R.drawable.devil);
                }

                if(ddust >=0 && ddust <=30)
                {
                    dustText.setText(dust.concat("???/???"));
                    dustLogo.setImageResource(R.drawable.good);
                }else if( ddust>=31 && ddust <=80)
                {
                    dustLogo.setImageResource(R.drawable.soso);
                    dustText.setText(dust.concat("???/???"));
                }else if( ddust>=81 && ddust <= 150)
                {
                    dustLogo.setImageResource(R.drawable.bad);
                    dustText.setText(dust.concat("???/???"));
                }else if( ddust>=151 )
                {   dustText.setTextColor(Color.RED);
                    dustText.setText(dust.concat("???/???"));
                    dustLogo.setImageResource(R.drawable.devil);
                }

                tempText.setText(temp.concat(" ???"));
                humText.setText(hum.concat("%"));

                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Response.Listener<String> responseListener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    boolean success = jsonResponse.getBoolean("success");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        BluetoothRequest BluetoothRequest = new BluetoothRequest(userID, str_datetime, str_btn, str_dust, str_co, str_hum, str_temp, responseListener);
                        RequestQueue queue = Volley.newRequestQueue(WelcomActivity.this);
                        queue.add(BluetoothRequest);
                    }
                },1000);
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //???????????? ???
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeviceDisconnected() {//????????????
                Toast.makeText(getApplicationContext()
                        , "????????????", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeviceConnectionFailed() {//????????????
                Toast.makeText(getApplicationContext()
                        , "????????????", Toast.LENGTH_SHORT).show();
            }
        });

        //???????????? ??????
        bluetooth_connect = findViewById(R.id.bluetooth_connect);
        bluetooth_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

                }
            }
        });

    }
    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //???????????? ??????
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);

            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }

    }

}



