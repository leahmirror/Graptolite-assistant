package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.util.BitmapUtil;
import com.example.myapplication.widget.MeituView;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener{
    private String mFilePath;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        findViewById(R.id.camera).setOnClickListener(this);
        findViewById(R.id.photograph).setOnClickListener(this);
        mFilePath=getApplicationContext().getFilesDir().getAbsolutePath();
        //Environment.getExternalStorageDirectory().getPath();
        mFilePath=mFilePath+"/"+"tem.jpg";
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.camera) {
            String str="";
            Intent intent=new Intent(MenuActivity.this,MainActivity.class);
            intent.putExtra("mFilePath",str);
            startActivity(intent);
        }else if (v.getId() == R.id.photograph) {
            Intent intent=new Intent(MenuActivity.this,MeituActivity.class);
          //  intent.putExtra("Path",mFilePath);
            startActivity(intent);
        }
    }
}
