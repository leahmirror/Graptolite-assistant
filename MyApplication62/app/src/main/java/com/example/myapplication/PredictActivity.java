package com.example.myapplication;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;

public class PredictActivity extends AppCompatActivity {
    private ImageView img;
    private ImageView predict_1;
    private ImageView predict_2;
    private ImageView predict_3;
    private TextView predict_txt1;
    private TextView predict_txt2;
    private TextView predict_txt3;
    private Bitmap bitmap1=null;
    private Bitmap bitmap2=null;
    private Bitmap bitmap3=null;
    private String[] predict=null;
    private LinearLayout linearLayout;
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_predict);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        toolbar.setBackgroundResource(R.drawable.ll);
        toolbar.setTitle("笔石识别");
        setSupportActionBar(toolbar);
        Intent intent=getIntent();
        //Bitmap bitmap = (Bitmap) intent.getParcelableExtra("bg");
        linearLayout=findViewById(R.id.line1);
        img = findViewById(R.id.iv1);
        predict_1=findViewById(R.id.predict_1);
        predict_2=findViewById(R.id.predict_2);
        predict_3=findViewById(R.id.predict_3);
        predict_txt1=findViewById(R.id.predict_txt1);
        predict_txt2=findViewById(R.id.predict_txt2);
        predict_txt3=findViewById(R.id.predict_txt3);
        Bitmap bmp=null;
       // Uri uri=intent.getData();
        String predictResult=intent.getStringExtra("predictResult");
        predict=predictResult.split("  ");
      /*  try {
            bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
//        Bundle extras = getIntent().getExtras();
//        byte[] b = extras.getByteArray("picture");
//        Bitmap bimp = BitmapFactory.decodeByteArray(b, 0, b.length);
        Bundle bundle = getIntent().getExtras();
        MainActivity.BitmapBinder bitmapBinder = (MainActivity.BitmapBinder) bundle.getBinder("bitmap");
        Bitmap bimp = bitmapBinder.getBitmap();
        img.setImageBitmap(bimp);
//        Bitmap bitmap=getAlplaBitmap(bmp,60);
  //      Drawable drawable = new BitmapDrawable(bitmap);
 //       linearLayout.setBackground(drawable);
        predict_txt1.setText(predict[0]);
        predict_txt2.setText(predict[1]);
        predict_txt3.setText(predict[2]);
        fang(predict_1,0);
        fang(predict_2,1);
        fang(predict_3,2);
    }
//    @SuppressLint("ResourceType")
//    public boolean onCreateOptionsMenu(Menu menu){
//        getMenuInflater().inflate(R.layout.menu,menu);
//        return true;
//    }
//    public boolean onOptionsItemSelected(MenuItem item){
//        switch (item.getItemId()){
//            case R.id.action_search:
//                Intent intent = new Intent(PredictActivity.this,baikeActivity.class);
//                //   Toast.makeText(this,"lala....",Toast.LENGTH_SHORT).show();
//                startActivity(intent);
//                break;
//            default:
//        }
//        return true;
//    }

    public static Bitmap getAlplaBitmap(Bitmap sourceImg, int number) {
        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0, sourceImg.getWidth(), sourceImg.getHeight());
        number = number * 255 / 100;
        for (int i = 0; i < argb.length; i++) {
            argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);
        }
         sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Bitmap.Config.ARGB_8888);
        return sourceImg;
    }
    public void fang(ImageView imageView,int i){
        AssetManager manager = getAssets();
        InputStream is = null;
        try {
            is = manager.open(panduan(predict[i]));
            Bitmap bitmap=BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bitmap);
            Bitmap take= ResizeBitmap(bitmap, 300);
            imageView.setImageBitmap(take);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Bitmap ResizeBitmap(Bitmap bitmap, int newWidth) {//拍照的图片太大，设置格式大小
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float temp = ((float) height) / ((float) width);
        int newHeight = (int) ((newWidth) * temp);
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();
        return resizedBitmap;
    }
    public String panduan(String s){
        if (s.contains("getograptus")){
            return "Agetograptus primus.jpg";
        }else if(s.contains("kidograptus ascensus")){
            return "akido.jpg";
        }else if(s.contains("mplexogratus latus")){
            return "amplex.jpg";
        }else if(s.contains("nticostia tenuissima")){
            return "anti.jpg";
        }else if(s.contains("ppendispinograptus supernus")){
            return "appendi.jpg";
        }else if(s.contains("ampograptus communis")){
            return "Campograptus.jpg";
        }else if(s.contains("tubuliferus")){
            return "eeeeee.jpg";
        }else if(s.contains("oronograptus cyphus")){
            return "Coronograptus cyphus.jpg";
        }else if(s.contains("ystograptus vesiculosus")){
            return "cys.jpg";
        }else if(s.contains("emirastrites triangulatus")){
            return "demir.jpg";
        }else if(s.contains("icellograptus complexus")){
            return "dicello.jpg";
        }else if(s.contains("imorphograptus")){
            return "dimor.jpg";
        }else if(s.contains("eodiplograptus")){
            return "neo.jpg";
        }else if(s.contains("ormalograptus persculptus")){
            return "normalo.jpg";
        }else if(s.contains("Ktavites spiralis")){
            return "oktavites.jpg";
        }else if(s.contains("rthograptus socilis")){
            return "ortho.jpg";
        }else if(s.contains("arakidograptus acuminatus")){
            return "parakido.jpg";
        }else if(s.contains("araorthograptus pacificus")){
            return "Paraorthograptus pacificus.jpg";
        }else if(s.contains("araplegmatograptus")){
            return "paraple.jpg";
        }else if(s.contains("etalolithus folium")){
            return "petalo.jpg";
        }else if(s.contains("ristiograptus regularis")){
            return "pristi.jpg";
        }else if(s.contains("seudorthorgraptus inopinatus")){
            return "Pseudor.jpg";
        }else if(s.contains("astrites orbitus")){
            return "rast.jpg";
        }else if(s.contains("ectograptus abbreviatus")){
            return "rect.jpg";
        }else if(s.contains("ectograptus obesas")){
            return "Recto.jpg";
        }else if(s.contains("angyagraptus typicus")){
            return "tangya.jpg";
        }else if(s.contains("oronograptus gregarius")){
            return "Cog.jpg";
        }else if(s.contains("ribylograptus incommodus")){
            return "priby.jpg";
        }else{
            return "wu.jpg";
        }
    }

}