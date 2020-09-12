package com.onelighone.oneLigthOn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

public class ScanActivity extends AppCompatActivity {

    private BluetoothAdapter bta;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private  ConnectedThread btt = null;

    private CheckedTextView firstChipScanTxt;
    private CheckedTextView secondChipScanTxt;
    private TextView notif_msg;

    public Handler mHandler;
    public final static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public final static String MODULE_MAC = "98:D3:B1:FD:71:A0";

    public final static String KNONW_CHIP = " I know this";
    public final static String UNKNONW_CHIP = " I do not";
    public final static String KNONW_CHIP2 = " The second chip";
    public final static String UNKNONW_CHIP2 = " Succesfully added ID";

    public final static String ZERO_CHIP_SCANNED = "State 0";
    public final static String ONE_CHIP_SCANNED = "State 1";
    public final static String TWO_CHIP_SCANNED = "State 2";
    public String state;

    private Button againBtn;
    private AlertDialog alertCo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        notif_msg = findViewById(R.id.notif_msg);
        firstChipScanTxt = findViewById(R.id.firstChipScan);
        secondChipScanTxt = findViewById(R.id.secondChipScan);
        firstChipScanTxt.setChecked(false);
        secondChipScanTxt.setChecked(false);
        firstChipScanTxt.setCheckMarkDrawable(null);
        secondChipScanTxt.setCheckMarkDrawable(null);
        notif_msg.setText("");

        againBtn = findViewById(R.id.addNailAgain);
        againBtn.setVisibility(View.GONE);

        bta = BluetoothAdapter.getDefaultAdapter();
        state = ZERO_CHIP_SCANNED;
        initiateBluetoothProcess();
        Log.i("[SCAN]", "MMSOCKET - - " + mmSocket.isConnected());
        if(!mmSocket.isConnected()){
            showNotConnectedAlert();

        }


    }

    public void initiateBluetoothProcess(){

        if(bta.isEnabled()){

            //attempt to connect to bluetooth module
            BluetoothSocket tmp = null;

            mmDevice = bta.getRemoteDevice(MODULE_MAC);

            //create socket
            try {

                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
                mmSocket = tmp;
                mmSocket.connect();
                Log.i("[BLUETOOTH]","Connected to: "+mmDevice.getName());
            }catch(IOException e){
                try{mmSocket.close();}catch(IOException c){return;}
            }

            Log.i("[BLUETOOTH]", "Creating handler");
            mHandler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message msg) {
                    //super.handleMessage(msg);
                    if(msg.what == ConnectedThread.RESPONSE_MESSAGE){

                        String txt = (String)msg.obj;
                        Log.i("[SCAN]", "message : " + txt);
                        String[] words = txt.split("\\s+");
                        String newString = "";
                        for (int i = 0; i < 3; i++) { newString = newString + " " + words[i];}

                        Log.i("[SCAN]", "etat" + state);

                        if(state.equals(ZERO_CHIP_SCANNED)) {
                            if (newString.equals(KNONW_CHIP)) {
                                Log.i("[SCAN]", "chip connue");
                                firstChipScanTxt.setChecked(false);
                                notif_msg.setText("");
                                notif_msg.setTextColor(Color.RED);
                                notif_msg.append(getResources().getString(R.string.already_scanned));
                            }
                            else if (newString.equals(UNKNONW_CHIP)) {
                                Log.i("[SCAN]", "chip inconnue");
                                firstChipScanTxt.setChecked(true);
                                firstChipScanTxt.setCheckMarkDrawable(R.drawable.checked_2);
                                notif_msg.setText("");
                                notif_msg.setTextColor(Color.GREEN);
                                notif_msg.append(getResources().getString(R.string.first_chip_scanned_valid));
                                state = ONE_CHIP_SCANNED;
                            }
                        }else if (state.equals(ONE_CHIP_SCANNED)){
                            Log.i("[SCAN]", "SCAN 1 REUSSI");
                            if (newString.equals(KNONW_CHIP2)) {
                                Log.i("[SCAN]", "chip connue apres 1e scan");
                                secondChipScanTxt.setChecked(false);
                                firstChipScanTxt.setChecked(false);
                                firstChipScanTxt.setCheckMarkDrawable(null);
                                notif_msg.setText("");
                                notif_msg.setTextColor(Color.RED);
                                notif_msg.append(getResources().getString(R.string.already_scanned_reset));
                                state=ZERO_CHIP_SCANNED;
                            }
                            else if (newString.equals(UNKNONW_CHIP2)) {
                                Log.i("[SCAN]", "chip inconnue apres 1e scan");
                                secondChipScanTxt.setChecked(true);
                                secondChipScanTxt.setCheckMarkDrawable(R.drawable.checked_2);
                                notif_msg.setText("");
                                notif_msg.setTextColor(Color.GREEN);
                                notif_msg.append(getResources().getString(R.string.second_chip_scanned_valid));
                                state = TWO_CHIP_SCANNED;
                                againBtn.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            };

            Log.i("[BLUETOOTH]", "Creating and running Thread");
            btt = new ConnectedThread(mmSocket,mHandler);
            btt.start();


        }
    }



    public void restartActivity(View v){
        firstChipScanTxt.setChecked(false);
        secondChipScanTxt.setChecked(false);
        firstChipScanTxt.setCheckMarkDrawable(null);
        secondChipScanTxt.setCheckMarkDrawable(null);
        notif_msg.setText("");
        state = ZERO_CHIP_SCANNED;
        againBtn.setVisibility(View.GONE);
    }

    public void toHome(View v){
        onBackPressed();
    }

    @Override
    public void onPause() {
        btt.cancel();
        super.onPause();
    }

    public void showNotConnectedAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bluetooth error");
        builder.setMessage("Device not found, turn the device ON");
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
            onBackPressed();
        });

        alertCo = builder.create();
        alertCo.show();
    }
}
