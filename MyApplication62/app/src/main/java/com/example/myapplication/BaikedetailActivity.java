package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class BaikedetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baikedetail_layout);
        Toolbar toolbar = findViewById(R.id.toolbar3);
        toolbar.setBackgroundResource(R.drawable.ll);
        toolbar.setTitle("笔石科普");
        setSupportActionBar(toolbar);
    }
}