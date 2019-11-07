package com.example.smartdoorlock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class manageuser extends AppCompatActivity {
    EditText UID;
    Button search;

    EditText name;
    EditText role;
    EditText perm;
    EditText id;
    EditText pw;
    LinearLayout layout;
    Button finish;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manageuser);
        UID = (EditText)findViewById(R.id.m_UID);
        search = (Button)findViewById(R.id.m_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        this.send("7");
                        this.send(UID.getText().toString());
                    }

                    @Override
                    public void onMessage(final String message) {
                        if (!message.equals("false")) {
                            final String[] words = message.split("\\s");
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    name.setText(new String(words[0]));
                                    role.setText(new String(words[1]));
                                    perm.setText(new String(words[2]));
                                    id.setText(new String(words[3]));
                                    pw.setText(new String(words[4]));

                                    int myPerm = Integer.parseInt(singleton.getInstance().perm);
                                    int compPerm = Integer.parseInt(perm.getText().toString());

                                    if(myPerm == 0 && id.getText().toString().equals("생성필요")){
                                        id.setFocusable(true);
                                        id.setClickable(true);
                                    }else{
                                        id.setFocusable(false);
                                        id.setClickable(false);
                                    }

                                    finish.setVisibility(View.VISIBLE);
                                    layout.setVisibility(View.VISIBLE);
                                    if(myPerm != 0)
                                        if(myPerm > compPerm || (myPerm == compPerm &&  !singleton.getInstance().uid.equals(UID.getText().toString()) ))
                                            finish.setVisibility(View.INVISIBLE);
                                }
                            });
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Toast.makeText(manageuser.this, "계정이 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(manageuser.this, "도메인 접속 실패", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                };
                webSocketClient.connect();
            }
        });

        name = (EditText)findViewById(R.id.m_nameedit);
        role = (EditText)findViewById(R.id.m_roleedit);
        perm = (EditText)findViewById(R.id.m_permedit);
        id = (EditText)findViewById(R.id.m_idedit);
        pw = (EditText)findViewById(R.id.m_pw2);
        layout = (LinearLayout)findViewById(R.id.m_linearLayout);
        finish = (Button)findViewById(R.id.m_finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                URI uri ;
                String domain = new String(singleton.getInstance().URI);

                if(id.getText().toString().equals("생성필요")){
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            Toast.makeText(manageuser.this, "아이디를 생성해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                try{
                    uri = new URI(domain);
                } catch (URISyntaxException e){
                    e.printStackTrace();
                    return;
                }

                WebSocketClient webSocketClient = new WebSocketClient(uri,new Draft_17()) {
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        this.send("9");
                        this.send(UID.getText().toString());
                        this.send(name.getText().toString());
                        this.send(role.getText().toString());
                        this.send(perm.getText().toString());
                        this.send(id.getText().toString());
                        this.send(pw.getText().toString());
                    }

                    @Override
                    public void onMessage(final String message) {
                        if (message.equals("true")) {
                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    Toast.makeText(manageuser.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(manageuser.this, "도메인 접속 실패", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                };
                webSocketClient.connect();
            }
        });

        layout.setVisibility(View.INVISIBLE);
    }
}
