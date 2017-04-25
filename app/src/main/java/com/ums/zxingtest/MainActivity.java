package com.ums.zxingtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.ums.android.android.MipcaActivityCapture;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gotoscan(View view){
        Intent intent = new Intent(MainActivity.this, MipcaActivityCapture.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int reqCode,int resultCode,Intent data){
        if (reqCode == 1 && resultCode == RESULT_OK){
            ((TextView)findViewById(R.id.main_tv_content)).setText(data.getExtras().getString("content"));
        }
    }
}
