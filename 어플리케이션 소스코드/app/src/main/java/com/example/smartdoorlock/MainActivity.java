package com.example.smartdoorlock;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class MainActivity extends AppCompatActivity {
    Button LogButton;
    Button PhotoButton;
    Button UserButton;
    Button DoorOpen;
    TextView role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        role = (TextView)findViewById(R.id.Role);
        role.setText(singleton.getInstance().name + " (" + singleton.getInstance().role + ")");
        LogButton = (Button)findViewById(R.id.Log);
        LogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LogLookUp.class);
                startActivity(intent);
            }
        });
        PhotoButton = (Button)findViewById(R.id.Photo);
        PhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PhotoLoopUp.class);
                startActivity(intent);
            }
        });
        UserButton = (Button)findViewById(R.id.User);
        UserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, manageuser.class);
                startActivity(intent);
            }
        });

        StrictMode.ThreadPolicy ourPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(ourPolicy);
        // 위에 두 문장을 추가해 줘야지 동작함. 아마도 권한, 정책관련 옵션같음.
        DoorOpen = (Button)findViewById(R.id.DoorOpen);
        DoorOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, DoorOpen.class);
//                startActivity(intent);
                try {
                    HttpClient client = new DefaultHttpClient();
                    String getURL = "http://192.168.1.191/on";
                    HttpGet get = new HttpGet(getURL);
                    HttpResponse responseGet = client.execute(get);
                    HttpEntity resEntityGet = responseGet.getEntity();
                    if (resEntityGet != null) {
                        // 결과를 처리합니다.
                        URI uri ;
                        String domain = new String(singleton.getInstance().URI);
                        try{
                            uri = new URI(domain);
                        } catch (URISyntaxException e){
                            e.printStackTrace();
                            return;
                        }

                        WebSocketClient webSocketClient = new WebSocketClient(uri,new Draft_17()) {
                            @Override
                            public void onOpen(ServerHandshake handshakedata) {
                                this.send("1");
                                this.send("");
                                this.send("");
                                this.send("");
                                this.send(singleton.getInstance().uid);
                            }

                            @Override
                            public void onMessage(final String message) {
                                if (message.equals("1")) {
                                    runOnUiThread(new Runnable() {
                                        @Override public void run() {
                                            Toast.makeText(MainActivity.this, "문을 열었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }else{
                                    runOnUiThread(new Runnable() {
                                        @Override public void run() {
                                            Toast.makeText(MainActivity.this, "문을 열지 못했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                this.close();
                            }

                            @Override
                            public void onClose(int code, String reason, boolean remote) {

                            }

                            @Override
                            public void onError(Exception ex) {
                                runOnUiThread(new Runnable() {
                                    @Override public void run() {
                                        Toast.makeText(MainActivity.this, "비정상적인 오류", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        };
                        webSocketClient.connect();
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                }
            }
        });

        if(Integer.parseInt(singleton.getInstance().perm) > 1) {
            LogButton.setVisibility(View.INVISIBLE);
            DoorOpen.setVisibility(View.INVISIBLE);
        }
    }
}
