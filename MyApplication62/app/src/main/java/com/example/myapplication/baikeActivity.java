package com.example.myapplication;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class baikeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.baike);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        toolbar.setBackgroundResource(R.drawable.ll);
        toolbar.setTitle("笔石百科");
        setSupportActionBar(toolbar);
    }
}