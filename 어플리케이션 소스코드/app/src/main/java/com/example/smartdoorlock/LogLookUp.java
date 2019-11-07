package com.example.smartdoorlock;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

public class LogLookUp extends AppCompatActivity {

    TextView start;
    TextView end;
    Button Search;
    int mYear, mMonth, mDay;
    Calendar cal;
    LinearLayout loglayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_look_up);

        cal = Calendar.getInstance();
        loglayout = (LinearLayout)findViewById(R.id.Loglayout);

        final DatePickerDialog.OnDateSetListener mDateSetListener1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mYear = year;
                mMonth = month;
                mDay = dayOfMonth;
                start.setText(String.format("%d-%d-%d",mYear,mMonth +1, mDay));
            }
        };
        final DatePickerDialog.OnDateSetListener mDateSetListener2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mYear = year;
                mMonth = month;
                mDay = dayOfMonth;
                end.setText(String.format("%d-%d-%d",mYear,mMonth +1, mDay));
            }
        };

        start = (TextView) findViewById(R.id.Start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(LogLookUp.this,mDateSetListener1, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        end = (TextView) findViewById(R.id.End);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(LogLookUp.this,mDateSetListener2, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        Search = (Button) findViewById(R.id.LogSearch);
        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                URI uri ;
                try{
                    uri = new URI(singleton.getInstance().URI);
                } catch (URISyntaxException e){
                    e.printStackTrace();
                    return;
                }
                    WebSocketClient webSocketClient = new WebSocketClient(uri,new Draft_17()) {
                        @Override
                        public void onOpen(ServerHandshake handshakedata) {
                            this.send("5");
                            this.send(start.getText().toString());
                            this.send(end.getText().toString());

                            runOnUiThread(new Runnable() {
                                @Override public void run() {
                                    loglayout.removeAllViews();
                                }
                            });


                        }

                        @Override
                        public void onMessage(final String message) {
                            if (message.equals("End")) {
                                this.close();
                            }
                            else{
                                runOnUiThread(new Runnable() {
                                    @Override public void run() {
                                        TextView t = new TextView(LogLookUp.this);
                                        t.setText(message);
                                        t.setTextSize(25);
                                        t.setPadding(0,10,0,10);
                                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT);
                                        lp.setMargins(0,0,0,50);
                                        t.setLayoutParams(lp);
                                        loglayout.addView(t);
                                    }
                                });
                            }


                        }

                        @Override
                        public void onClose(int code, String reason, boolean remote) {

                        }

                        @Override
                        public void onError(Exception ex) {

                        }
                    };
                    webSocketClient.connect();
            }
        });
    }


}
