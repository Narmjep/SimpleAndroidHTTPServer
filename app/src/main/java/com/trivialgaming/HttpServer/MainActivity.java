package com.trivialgaming.HttpServer;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private Server server;
    private Button btn_Start;
    private TextView txt_ip;
    private TextView txt_port;
    private EditText input_port;
    private boolean btn_Start_State = false;

    //METHODS
    private void Init(){
        btn_Start = (Button) findViewById(R.id.btn_start);
        btn_Start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                btn_Start_State = !btn_Start_State;
                if(btn_Start_State){
                    //Start
                    btn_Start.setText("Stop");
                    server.run();
                } else {
                    //Stop
                    btn_Start.setText("Start");
                    server.stop();
                }
            }
        });
        txt_ip = (TextView) findViewById(R.id.txt_ip);
        txt_port = (TextView) findViewById(R.id.txt_port);
        input_port = (EditText) findViewById(R.id.input_port);
    }

    //START
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager am = getApplicationContext().getAssets();
        //Start
        Init();
        server = new Server(am);
        try {
            server.Init();
        } catch (IOException e) {
            System.err.println("Failed to init server!\n");
            e.printStackTrace();
        }
        SetIp();
    }


    //Other methods
    void SetIp(){
        txt_ip.setText(server.IP);
    }
}