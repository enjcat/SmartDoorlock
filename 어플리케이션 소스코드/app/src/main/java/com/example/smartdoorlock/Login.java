package com.example.smartdoorlock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class Login extends AppCompatActivity {
    Button Login;
    EditText ID;
    EditText PW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ID = (EditText)findViewById(R.id.Id);
        PW = (EditText)findViewById(R.id.Pw);
        Login = (Button)findViewById(R.id.Login);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URI uri ;
                String domain = "ws://" + "192.168.1.180" + ":180";
                singleton.getInstance().URI = new String(domain);
                try{
                    uri = new URI(domain);
                } catch (URISyntaxException e){
                    e.printStackTrace();
                    return;
                }
                WebSocketClient webSocketClient = new WebSocketClient(uri,new Draft_17()) {
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        this.send("8");
                        this.send(ID.getText().toString());
                        this.send(PW.getText().toString());
                    }

                    @Override
                    public void onMessage(final String message) {
                        if (!message.equals("false")) {
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                                }
                            });
                            String[] words = message.split("\\s");
                            singleton.getInstance().uid = new String(words[0]);
                            singleton.getInstance().perm = new String(words[1]);
                            singleton.getInstance().name = new String(words[2]);
                            singleton.getInstance().role = new String(words[3]);

                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Toast.makeText(Login.this, "계정이 맞지 않습니다", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(Login.this, "도메인 접속 실패", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                };
                webSocketClient.connect();
            }

        });
    }
}
